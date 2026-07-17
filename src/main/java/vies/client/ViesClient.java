/*
 * Copyright 2026 VIES Client contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package vies.client;

import vies.client.internal.MiniJson;
import vies.client.internal.TtlCache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Flow;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * Client for the European Commission's VIES VAT-number validation REST API
 * (<a href="https://ec.europa.eu/taxation_customs/vies/#/technical-information">technical
 * documentation</a>). Zero runtime dependencies — only the JDK.
 *
 * <p>Configuration is immutable and instances are thread-safe; create one per application and share it
 * (e.g. as a singleton bean). The client implements {@link AutoCloseable} to release
 * its HTTP connections and async executor.</p>
 *
 * <p>Magyarul: alkalmazáspéldányonként egyetlen, megosztott {@code ViesClient}
 * objektumot hozz létre. A konfiguráció nem változik, a belső cache és a párhuzamos
 * állapot pedig szálbiztos adatszerkezeteket használ.</p>
 *
 * <p>Request flow / Egy kérés útja: normalize input → read cache → join an identical
 * in-flight request → acquire bounded network capacity → call VIES → validate the
 * response → cache confirmed valid results. Magyarul: normalizálás → cache → azonos
 * folyamatban lévő kéréshez csatlakozás → korlátos hálózati hely → VIES-hívás →
 * válaszellenőrzés → az igazolt érvényes eredmény cache-elése.</p>
 *
 * {@snippet :
 * try (var vies = ViesClient.builder()
 *         .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
 *         .retries(1)
 *         .build()) {
 *
 *     switch (vies.check("DE 000 000 000")) { // synthetic example / szintetikus példa
 *         case ViesResponse.Valid v         -> System.out.println("OK: " + v.traderName().orElse("?"));
 *         case ViesResponse.Invalid i       -> System.out.println("Not registered");
 *         case ViesResponse.Unavailable u   -> System.out.println("Retry later: " + u.errorCode());
 *         case ViesResponse.MalformedInput m -> System.out.println("Bad input: " + m.reason());
 *     }
 * }
 * }
 *
 * <p>Semantics: transient VIES failures (member state backend down, throttling) are
 * reported as {@link ViesResponse.Unavailable}, never as {@code Invalid} — do not
 * treat an unavailable answer as a failed validation. Confirmed results are cached
 * (default: in-memory, 24 h, valid results only).</p>
 */
public final class ViesClient implements AutoCloseable {

    public static final String DEFAULT_BASE_URL = "https://ec.europa.eu/taxation_customs/vies/rest-api";

    /**
     * EN: Real VIES JSON is small; this cap prevents a peer from exhausting heap.
     * HU: A valódi VIES JSON kicsi; ez a korlát védi a heapet egy hibás/támadó végponttól.
     */
    private static final int MAX_RESPONSE_BYTES = 65_536;
    private static final int MAX_NAME_LENGTH = 512;
    private static final int MAX_ADDRESS_LENGTH = 4_096;
    private static final int MAX_CONSULTATION_LENGTH = 256;
    private static final int MAX_DATE_LENGTH = 64;
    private static final int MAX_ERROR_CODE_LENGTH = 64;
    private static final String FIELD_ADDRESS = "address";
    private static final String FIELD_COUNTRY_CODE = "countryCode";
    private static final String FIELD_IS_VALID = "isValid";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_REQUEST_DATE = "requestDate";
    private static final String FIELD_REQUEST_IDENTIFIER = "requestIdentifier";
    private static final String FIELD_USER_ERROR = "userError";
    private static final String FIELD_VALID = "valid";
    private static final String FIELD_VAT_NUMBER = "vatNumber";
    private static final String ERROR_CLIENT_CLOSED = "CLIENT_CLOSED";
    private static final String ERROR_CLIENT_OVERLOADED = "CLIENT_OVERLOADED";
    private static final String ERROR_INTERRUPTED = "INTERRUPTED";
    private static final String ERROR_MALFORMED_RESPONSE = "MALFORMED_RESPONSE";
    private static final String PARAM_REQUESTER = "requester";
    private static final List<String> RESPONSE_STRING_FIELDS = List.of(
            FIELD_USER_ERROR,
            FIELD_REQUEST_DATE,
            FIELD_NAME,
            FIELD_ADDRESS,
            FIELD_REQUEST_IDENTIFIER,
            FIELD_COUNTRY_CODE,
            FIELD_VAT_NUMBER);
    private static final java.util.regex.Pattern SAFE_ERROR_CODE =
            java.util.regex.Pattern.compile("[A-Z][A-Z0-9_]*");

    /** VIES input errors are permanent: retrying the same data cannot fix them. */
    private static final java.util.Set<String> INPUT_ERRORS = java.util.Set.of(
            "INVALID_INPUT",
            "INVALID_REQUESTER_INFO");

    private final HttpClient http;
    private final ExecutorService asyncExecutor;
    private final boolean ownsExecutor;
    private final String baseUrl;
    private final Duration requestTimeout;
    private final long admissionTimeoutNanos;
    private final ViesCache cache;
    private final Duration cacheTtl;
    private final ViesRequester defaultRequester;
    private final int retries;
    private final Duration retryDelay;
    private final String userAgent;
    /** Bounds real HTTP calls / A valódi HTTP-hívások számát korlátozza. */
    private final Semaphore requestSlots;
    /** Bounds synchronous callers / A szinkron hívók memóriaterhelését korlátozza. */
    private final Semaphore syncSlots;
    /** Bounds unique async operations / Az egyedi aszinkron műveleteket korlátozza. */
    private final Semaphore asyncSlots;
    /** JVM-local single-flight table / JVM-en belüli azonos-kérés összevonó tábla. */
    private final ConcurrentHashMap<String, CompletableFuture<ViesResponse>> inFlight =
            new ConcurrentHashMap<>();
    /**
     * Operations remain registered until their terminal result is published.
     * A művelet a végső eredmény publikálásáig regisztrálva marad.
     */
    private final ConcurrentHashMap<CompletableFuture<ViesResponse>, ViesResponse> openOperations =
            new ConcurrentHashMap<>();
    /** Cancellable async task handles / Megszakítható aszinkron feladat-handle-ök. */
    private final ConcurrentHashMap<CompletableFuture<ViesResponse>, Future<?>> asyncTaskHandles =
            new ConcurrentHashMap<>();
    private final java.util.Set<Thread> activeThreads = ConcurrentHashMap.newKeySet();
    private final ReentrantReadWriteLock lifecycle = new ReentrantReadWriteLock();
    private final AtomicBoolean closed = new AtomicBoolean();

    private ViesClient(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.requestTimeout = builder.requestTimeout;
        this.admissionTimeoutNanos = builder.admissionTimeout.toNanos();
        this.cache = createCache(builder);
        this.cacheTtl = builder.cacheTtl;
        this.defaultRequester = builder.defaultRequester;
        this.retries = builder.retries;
        this.retryDelay = builder.retryDelay;
        this.userAgent = builder.userAgent;
        this.requestSlots = new Semaphore(builder.maxConcurrentRequests);
        this.syncSlots = new Semaphore(builder.maxPendingSyncRequests);
        this.asyncSlots = new Semaphore(builder.maxPendingAsyncRequests);
        this.http = HttpClient.newBuilder()
                .connectTimeout(builder.connectTimeout)
                // EN: Never forward VAT/requester data to a redirected host.
                // HU: Adószámot és lekérdezői adatot soha nem küldünk átirányított hostnak.
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        this.ownsExecutor = builder.executor == null;
        this.asyncExecutor = builder.executor != null
                ? builder.executor
                : Executors.newVirtualThreadPerTaskExecutor();
    }

    public static Builder builder() {
        return new Builder();
    }

    private static ViesCache createCache(Builder builder) {
        if (builder.cache != null) {
            return builder.cache;
        }
        return builder.cacheEnabled
                ? new TtlCache(builder.cacheMaxEntries, InstantSource.system())
                : null;
    }

    /** A client with default settings (in-memory cache, 8 s timeout, no requester). */
    public static ViesClient createDefault() {
        return builder().build();
    }

    // ------------------------------------------------------------------ sync API

    /**
     * Validates a full VAT identifier such as {@code "HU00000000"} (separators and
     * lowercase are tolerated, {@code GR} maps to {@code EL}). Uses the builder's
     * default requester, if one was configured.
     */
    public ViesResponse check(String fullVatNumber) {
        return runSync(
                () -> checkFull(fullVatNumber, defaultRequester),
                () -> overloaded(fullVatNumber));
    }

    private ViesResponse checkFull(String fullVatNumber, ViesRequester requester) {
        return VatFormat.normalize(fullVatNumber)
                .map(vat -> checkNormalized(vat, requester))
                .orElseGet(() -> malformed(fullVatNumber));
    }

    /** Validates a full VAT identifier on behalf of the given requester. */
    public ViesResponse check(String fullVatNumber, ViesRequester requester) {
        Objects.requireNonNull(requester, PARAM_REQUESTER);
        return runSync(
                () -> checkFull(fullVatNumber, requester),
                () -> overloaded(fullVatNumber));
    }

    /** Validates a (country code, national number) pair, e.g. {@code ("HU", "12345678")}. */
    public ViesResponse check(String countryCode, String vatNumber) {
        return runSync(
                () -> checkPair(countryCode, vatNumber, defaultRequester),
                () -> overloaded(countryCode, vatNumber));
    }

    private ViesResponse checkPair(String countryCode, String vatNumber, ViesRequester requester) {
        return VatFormat.normalize(countryCode, vatNumber)
                .map(vat -> checkNormalized(vat, requester))
                .orElseGet(() -> malformed(safeInput(countryCode) + " " + safeInput(vatNumber)));
    }

    /** Validates a (country code, national number) pair on behalf of the given requester. */
    public ViesResponse check(String countryCode, String vatNumber, ViesRequester requester) {
        Objects.requireNonNull(requester, PARAM_REQUESTER);
        return runSync(
                () -> checkPair(countryCode, vatNumber, requester),
                () -> overloaded(countryCode, vatNumber));
    }

    // ----------------------------------------------------------------- async API

    /**
     * Async variant of {@link #check(String)}, executed on virtual threads by default.
     * Magyarul: nem foglal platformszálat a hálózati várakozás teljes idejére.
     */
    public CompletableFuture<ViesResponse> checkAsync(String fullVatNumber) {
        return admit(() -> checkFullAsync(fullVatNumber, defaultRequester));
    }

    /** Async variant of {@link #check(String, ViesRequester)}. */
    public CompletableFuture<ViesResponse> checkAsync(String fullVatNumber, ViesRequester requester) {
        Objects.requireNonNull(requester, PARAM_REQUESTER);
        return admit(() -> checkFullAsync(fullVatNumber, requester));
    }

    /** Async variant of {@link #check(String, String)}. */
    public CompletableFuture<ViesResponse> checkAsync(String countryCode, String vatNumber) {
        return admit(() -> checkPairAsync(countryCode, vatNumber, defaultRequester));
    }

    private AsyncAdmission checkFullAsync(
            String fullVatNumber, ViesRequester requester) {
        return VatFormat.normalize(fullVatNumber)
                .map(vat -> checkNormalizedAsync(vat, requester))
                .orElseGet(() -> AsyncAdmission.completed(malformed(fullVatNumber)));
    }

    private AsyncAdmission checkPairAsync(
            String countryCode, String vatNumber, ViesRequester requester) {
        return VatFormat.normalize(countryCode, vatNumber)
                .map(vat -> checkNormalizedAsync(vat, requester))
                .orElseGet(() -> AsyncAdmission.completed(
                        malformed(safeInput(countryCode) + " " + safeInput(vatNumber))));
    }

    // -------------------------------------------------------------- diagnostics

    /**
     * Queries the VIES {@code /check-status} endpoint: overall availability plus the
     * member-state entries actually present in the response. The returned map can be
     * empty or partial when VIES omits those entries.
     *
     * <p>Magyarul: az összesített állapotot és a válaszban ténylegesen szereplő
     * tagállami állapotokat adja vissza; a térkép hiányos vagy üres is lehet.</p>
     *
     * @throws ViesException when VIES cannot be reached, returns a non-200 status,
     *                       or the response is not valid bounded JSON
     */
    public ViesAvailability availability() {
        return runSyncAvailability(this::queryAvailability);
    }

    private ViesAvailability queryAvailability() {
        var request = HttpRequest.newBuilder(URI.create(baseUrl + "/check-status"))
                .timeout(requestTimeout)
                .header("Accept", "application/json")
                .header("User-Agent", userAgent)
                .GET()
                .build();
        var response = sendAvailabilityRequest(request);
        if (response.statusCode() != 200) {
            throw new ViesException("VIES check-status returned HTTP / A VIES állapotlekérdezés HTTP-hibát adott: "
                    + response.statusCode());
        }
        return parseAvailability(response.body());
    }

    private HttpResponse<String> sendAvailabilityRequest(HttpRequest request) {
        var acquired = false;
        try {
            if (!requestSlots.tryAcquire(admissionTimeoutNanos, TimeUnit.NANOSECONDS)) {
                throw new ViesException("VIES client overloaded / A VIES kliens túlterhelt");
            }
            acquired = true;
            return http.send(request, boundedUtf8BodyHandler());
        } catch (IOException e) {
            if (causedByInvalidBody(e)) {
                throw new ViesException(
                        "VIES check-status response is malformed or too large"
                                + " / A VIES állapotválasz hibás vagy túl nagy", e);
            }
            throw new ViesException("VIES check-status request failed / A VIES állapotlekérdezés sikertelen", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ViesException("VIES check-status request interrupted / A VIES állapotlekérdezés megszakadt", e);
        } finally {
            if (acquired) {
                requestSlots.release();
            }
        }
    }

    private static ViesAvailability parseAvailability(String responseBody) {
        try {
            if (!(MiniJson.parse(responseBody) instanceof Map<?, ?> root)) {
                throw new ViesException("Malformed check-status response / Hibás VIES állapotválasz");
            }
            var serviceAvailable = root.get("vow") instanceof Map<?, ?> vow
                    && Boolean.TRUE.equals(vow.get("available"));
            return new ViesAvailability(serviceAvailable, countryStatuses(root.get("countries")));
        } catch (MiniJson.ParseException e) {
            throw new ViesException("Malformed check-status response / Hibás VIES állapotválasz", e);
        }
    }

    private static Map<String, ViesAvailability.MemberStateStatus> countryStatuses(Object value) {
        var states = new TreeMap<String, ViesAvailability.MemberStateStatus>();
        if (value instanceof List<?> countries) {
            countries.forEach(item -> addCountryStatus(states, item));
        }
        return Collections.unmodifiableSortedMap(states);
    }

    private static void addCountryStatus(
            Map<String, ViesAvailability.MemberStateStatus> states, Object item) {
        if (item instanceof Map<?, ?> country
                && optString(country, FIELD_COUNTRY_CODE) instanceof String code
                && VatFormat.supportedCountries().contains(code)) {
            states.put(code, ViesAvailability.MemberStateStatus.fromLabel(
                    optString(country, "availability")));
        }
    }

    @Override
    public void close() {
        var writeLock = lifecycle.writeLock();
        writeLock.lock();
        try {
            if (closed.compareAndSet(false, true)) {
                asyncTaskHandles.values().forEach(task -> task.cancel(true));
                activeThreads.forEach(Thread::interrupt);
                var internalSnapshot = Map.copyOf(openOperations);
                completeForShutdown(internalSnapshot);
                awaitTerminalState(internalSnapshot.keySet());
                if (ownsExecutor) {
                    asyncExecutor.shutdownNow();
                }
                // Hard shutdown is bounded and also cancels calls running on a custom executor.
                http.shutdownNow();
                asyncTaskHandles.clear();
                openOperations.clear();
                inFlight.clear();
            }
        } finally {
            writeLock.unlock();
        }
    }

    // ------------------------------------------------------------------ internals

    /**
     * Catches {@link Error} only to publish the same fatal failure to single-flight
     * followers before rethrowing it; it is never converted to a normal VIES result.
     * Magyarul: az {@code Error} kizárólag a követők értesítéséig van elkapva, majd
     * változatlanul továbbdobódik.
     */
    @SuppressWarnings("java:S1181")
    private ViesResponse checkNormalized(VatFormat.Normalized vat, ViesRequester requester) {
        var cacheKey = operationKey(vat, requester);
        var cached = lookupCache(cacheKey, vat);
        if (closed.get()) {
            return closed(vat);
        }
        if (cached != null) {
            return cached;
        }

        // EN: Collapse concurrent misses for the same VAT/requester pair into one call.
        // HU: Az azonos adószám+lekérdező cache miss-ekből csak egy VIES-hívás indul.
        var ownResult = new CompletableFuture<ViesResponse>();
        openOperations.put(ownResult, closed(vat));
        var existing = inFlight.putIfAbsent(cacheKey, ownResult);
        if (existing != null) {
            openOperations.remove(ownResult);
            return awaitInFlight(existing, vat);
        }
        try {
            // EN: Recheck after leadership to close the cache-miss race.
            // HU: A vezetővé válás után újra nézzük a cache-t, így nincs duplikált hívás.
            cached = lookupCache(cacheKey, vat);
            if (closed.get()) {
                return publishSync(cacheKey, ownResult, closed(vat), vat);
            }
            if (cached != null) {
                return publishSync(cacheKey, ownResult, cached, vat);
            }
            var response = closed.get() ? closed(vat) : executeWithRetries(vat, requester);
            if (closed.get()) {
                response = closed(vat);
            }
            cacheValid(cacheKey, response);
            return publishSync(cacheKey, ownResult, response, vat);
        } catch (RuntimeException failure) {
            inFlight.remove(cacheKey, ownResult);
            var failurePublished = ownResult.completeExceptionally(failure);
            openOperations.remove(ownResult);
            if (!failurePublished) {
                return awaitInFlight(ownResult, vat);
            }
            throw failure;
        } catch (Error failure) {
            // EN: Fatal VM/application errors must never be converted into shutdown results.
            // HU: A végzetes VM/alkalmazáshiba soha nem alakítható leállási eredménnyé.
            inFlight.remove(cacheKey, ownResult);
            ownResult.completeExceptionally(failure);
            openOperations.remove(ownResult);
            throw failure;
        }
    }

    private ViesResponse publishSync(
            String cacheKey,
            CompletableFuture<ViesResponse> ownResult,
            ViesResponse response,
            VatFormat.Normalized vat) {
        // EN: Remove single-flight leadership before callbacks, but keep the operation
        // visible to close() until completion. This makes shutdown publication linearizable.
        // HU: Callback előtt elengedjük a single-flight vezetést, de a close() a befejezésig
        // még látja a műveletet; így a leállítás és az eredménypublikálás sorrendje egyértelmű.
        if (closed.get()) {
            response = closed(vat);
        }
        inFlight.remove(cacheKey, ownResult);
        ownResult.complete(response);
        openOperations.remove(ownResult);
        return awaitInFlight(ownResult, vat);
    }

    private AsyncAdmission checkNormalizedAsync(
            VatFormat.Normalized vat, ViesRequester requester) {
        var cacheKey = operationKey(vat, requester);
        // EN: Only an internal future is shared; callers receive cancellation-safe copies.
        // HU: A közös future belső marad; egy hívó cancel-je nem állítja le a többiek kérését.
        var shared = new CompletableFuture<ViesResponse>();
        openOperations.put(shared, closed(vat));
        var existing = inFlight.putIfAbsent(cacheKey, shared);
        if (existing != null) {
            openOperations.remove(shared);
            return AsyncAdmission.completedFuture(existing.copy());
        }

        if (!asyncSlots.tryAcquire()) {
            shared.complete(overloaded(vat));
            inFlight.remove(cacheKey, shared);
            openOperations.remove(shared);
            return AsyncAdmission.completedFuture(shared.copy());
        }

        var task = new FutureTask<Void>(() -> {
                runAsyncLeader(shared, cacheKey, vat, requester);
                return null;
            }) {
                @Override
                protected void setException(Throwable failure) {
                    super.setException(failure);
                    // EN: FutureTask normally swallows fatal VM/application Errors.
                    // HU: a FutureTask alapból elnyeli a végzetes VM/alkalmazás Error-t.
                    if (failure instanceof Error error) {
                        throw error;
                    }
                }
        };
        asyncTaskHandles.put(shared, task);
        var callerView = shared.copy();
        // EN: The executor is invoked only after the lifecycle read lock is released.
        // HU: A felhasználói executor csak az életciklus olvasási lock elengedése után indul.
        return new AsyncAdmission(callerView, () -> submitAsyncTask(shared, cacheKey, task));
    }

    /**
     * Submits a prepared task and rolls back admission if a custom executor rejects
     * or fatally fails inline. Fatal errors are rethrown after the shared state is safe.
     */
    @SuppressWarnings("java:S1181")
    private void submitAsyncTask(
            CompletableFuture<ViesResponse> shared, String cacheKey, FutureTask<Void> task) {
        try {
            asyncExecutor.execute(task);
        } catch (RuntimeException | Error failure) {
            // EN: Inline executors can finish the task before execute() rethrows Error.
            // Only the thread that removes the handle owns cleanup and permit release.
            // HU: Helyben futó executornál a task befejeződhet az Error visszadobása előtt;
            // csak a handle-t eltávolító szál takaríthat és adhatja vissza a permitet.
            if (asyncTaskHandles.remove(shared, task)) {
                task.cancel(false);
                inFlight.remove(cacheKey, shared);
                asyncSlots.release();
                shared.completeExceptionally(failure);
                openOperations.remove(shared);
            }
            if (failure instanceof Error error) {
                throw error;
            }
        }
    }

    /**
     * Runs the single async leader, always releases bounded capacity before invoking
     * completion callbacks, and propagates fatal failures after publication.
     */
    @SuppressWarnings("java:S1181")
    private void runAsyncLeader(
            CompletableFuture<ViesResponse> shared,
            String cacheKey,
            VatFormat.Normalized vat,
            ViesRequester requester) {
        activeThreads.add(Thread.currentThread());
        ViesResponse response = null;
        RuntimeException runtimeFailure = null;
        Error fatalFailure = null;
        try {
            response = executeAsyncLeader(shared, cacheKey, vat, requester);
        } catch (RuntimeException failure) {
            runtimeFailure = failure;
        } catch (Error failure) {
            fatalFailure = failure;
        } finally {
            // EN: Release capacity before callbacks can submit their next operation.
            // HU: Callback előtt szabadítjuk fel a helyet, így nincs hamis túlterhelés.
            activeThreads.remove(Thread.currentThread());
            asyncTaskHandles.remove(shared);
            inFlight.remove(cacheKey, shared);
            asyncSlots.release();
        }

        publishAsyncResult(shared, vat, response, runtimeFailure, fatalFailure);
        // EN: Keep registered through complete(), so close() cannot miss this operation.
        // HU: A complete() végéig regisztrált marad, így a close() nem hagyhatja ki.
        openOperations.remove(shared);
        if (fatalFailure != null) {
            throw fatalFailure;
        }
    }

    /** Performs cache recheck and network work without publishing callbacks. */
    private ViesResponse executeAsyncLeader(
            CompletableFuture<ViesResponse> shared,
            String cacheKey,
            VatFormat.Normalized vat,
            ViesRequester requester) {
        if (shared.isDone()) {
            return null;
        }
        var response = lookupCache(cacheKey, vat);
        if (response == null && !shared.isDone() && !closed.get()) {
            response = executeWithRetries(vat, requester);
            cacheValid(cacheKey, response);
        } else if (closed.get()) {
            response = closed(vat);
        }
        return response;
    }

    /** Publishes exactly one terminal async outcome after capacity has been released. */
    private void publishAsyncResult(
            CompletableFuture<ViesResponse> shared,
            VatFormat.Normalized vat,
            ViesResponse response,
            RuntimeException runtimeFailure,
            Error fatalFailure) {
        if (shared.isDone()) {
            return;
        }
        if (closed.get()) {
            response = closed(vat);
            runtimeFailure = null;
        }
        if (fatalFailure != null && closed.get()) {
            shared.complete(response);
        } else if (runtimeFailure != null) {
            shared.completeExceptionally(runtimeFailure);
        } else if (fatalFailure != null) {
            shared.completeExceptionally(fatalFailure);
        } else {
            shared.complete(response);
        }
    }

    private static void completeForShutdown(
            Map<CompletableFuture<ViesResponse>, ViesResponse> operations) {
        // EN: Completion may run arbitrary user callbacks. Isolate each callback chain on a
        // cheap virtual thread so a high-volume shutdown cannot exhaust native platform threads.
        // HU: A complete() tetszőleges felhasználói callbacket futtathat. Minden callback-lánc
        // olcsó virtuális szálat kap, így a nagyterhelésű leállítás nem fogyasztja el a natív szálakat.
        operations.forEach((operation, response) -> Thread.ofVirtual()
                .name("vies-close-completer")
                .start(() -> operation.complete(response)));
    }

    private static void awaitTerminalState(
            java.util.Set<CompletableFuture<ViesResponse>> operations) {
        for (var operation : operations) {
            while (!operation.isDone()) {
                LockSupport.parkNanos(100_000L);
            }
        }
    }

    /** Returns a cache hit/cache failure response, or {@code null} on a clean miss. */
    private ViesResponse lookupCache(String cacheKey, VatFormat.Normalized vat) {
        if (cache == null) {
            return null;
        }
        try {
            var hit = cache.get(cacheKey);
            if (hit.isEmpty()) {
                return null;
            }
            var value = hit.get();
            // EN: Treat external cache contents as untrusted and bind them to the key.
            // HU: A külső cache tartalma nem megbízható; az értéket a kulcshoz kötjük.
            if (!isSafeCacheHit(value, vat)) {
                return new ViesResponse.Unavailable(vat.countryCode(), vat.full(), "CACHE_ERROR");
            }
            return value.fromCache() ? value : asCached(value);
        } catch (RuntimeException cacheFailure) {
            // EN: Do not stampede VIES when the shared Redis/cache is unhealthy.
            // HU: Redis-hibánál nem zúdítjuk az összes cache miss-t közvetlenül a VIES-re.
            return new ViesResponse.Unavailable(vat.countryCode(), vat.full(), "CACHE_ERROR");
        }
    }

    private void cacheValid(String cacheKey, ViesResponse response) {
        if (cache != null && response instanceof ViesResponse.Valid valid) {
            try {
                // EN: A cache write failure must not erase an authoritative VIES result.
                // HU: A cache írási hibája nem írhatja felül a már megkapott VIES-eredményt.
                cache.put(cacheKey, asCached(valid), cacheTtl);
            } catch (RuntimeException ignored) {
                // Best effort. Production cache adapters should expose their own metrics/alerts.
            }
        }
    }

    private ViesResponse executeWithRetries(VatFormat.Normalized vat, ViesRequester requester) {
        // EN: Retry only transient failures; jitter prevents synchronized retry storms.
        // HU: Csak átmeneti hibát próbálunk újra; a jitter szétszórja a retry-hullámot.
        var attempt = 0;
        while (true) {
            var response = executeOnce(vat, requester);
            if (response instanceof ViesResponse.Unavailable unavailable
                    && attempt < retries && isRetryable(unavailable.errorCode())) {
                attempt++;
                if (!sleepBackoff(attempt)) {
                    return new ViesResponse.Unavailable(vat.countryCode(), vat.full(), ERROR_INTERRUPTED);
                }
                continue;
            }
            return response;
        }
    }

    private ViesResponse awaitInFlight(
            CompletableFuture<ViesResponse> existing, VatFormat.Normalized vat) {
        try {
            return existing.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // EN: shutdown interruption is a CLIENT_CLOSED outcome for every participant.
            // HU: leállítás miatti megszakítás minden résztvevőnek CLIENT_CLOSED eredmény.
            return closed.get()
                    ? closed(vat)
                    : new ViesResponse.Unavailable(vat.countryCode(), vat.full(), ERROR_INTERRUPTED);
        } catch (java.util.concurrent.CancellationException e) {
            return new ViesResponse.Unavailable(vat.countryCode(), vat.full(), ERROR_CLIENT_CLOSED);
        } catch (ExecutionException e) {
            var cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new ViesException("Concurrent VIES request failed / A párhuzamos VIES-kérés meghiúsult", cause);
        }
    }

    private ViesResponse executeOnce(VatFormat.Normalized vat, ViesRequester requester) {
        // All URI components are validated as [A-Z0-9+*]... by VatFormat, so plain
        // concatenation cannot produce a malformed or injected URI.
        var uri = new StringBuilder(baseUrl)
                .append("/ms/").append(vat.countryCode())
                .append("/vat/").append(vat.nationalNumber());
        if (requester != null) {
            uri.append("?requesterMemberStateCode=").append(requester.countryCode())
                    .append("&requesterNumber=").append(requester.vatNumber());
        }
        var request = HttpRequest.newBuilder(URI.create(uri.toString()))
                .timeout(requestTimeout)
                .header("Accept", "application/json")
                .header("User-Agent", userAgent)
                .GET()
                .build();
        var acquired = false;
        try {
            // EN: Timed admission bounds VIES pressure and total queueing delay.
            // HU: Az időkorlátos beléptetés védi a VIES-t és korlátozza a sorban állást.
            if (!requestSlots.tryAcquire(admissionTimeoutNanos, TimeUnit.NANOSECONDS)) {
                return new ViesResponse.Unavailable(vat.countryCode(), vat.full(), ERROR_CLIENT_OVERLOADED);
            }
            acquired = true;
            var response = http.send(request, boundedUtf8BodyHandler());
            if (response.statusCode() != 200) {
                return new ViesResponse.Unavailable(vat.countryCode(), vat.full(),
                        "HTTP_" + response.statusCode());
            }
            return mapBody(vat, MiniJson.parse(response.body()));
        } catch (MiniJson.ParseException e) {
            return malformedResponse(vat);
        } catch (HttpTimeoutException e) {
            return new ViesResponse.Unavailable(vat.countryCode(), vat.full(), "TIMEOUT");
        } catch (IOException e) {
            return new ViesResponse.Unavailable(vat.countryCode(), vat.full(),
                    causedByInvalidBody(e) ? ERROR_MALFORMED_RESPONSE : "NETWORK_ERROR");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ViesResponse.Unavailable(vat.countryCode(), vat.full(), ERROR_INTERRUPTED);
        } finally {
            if (acquired) {
                requestSlots.release();
            }
        }
    }

    /**
     * Maps an untrusted VIES JSON document to the sealed result type.
     * Magyarul: itt ellenőrizzük a külső választ; hiányos boolean vagy dátum soha
     * nem válhat tévesen {@code Invalid} eredménnyé.
     */
    static ViesResponse mapBody(VatFormat.Normalized vat, Object json) {
        if (!(json instanceof Map<?, ?> body)) {
            return malformedResponse(vat);
        }
        // EN: A present external field must have its documented JSON type. Treating
        // a wrong type as "missing" could turn an upstream error into Valid/Invalid.
        // HU: A jelen lévő külső mező típusa kötelező; a hibás típust hiányzóként
        // kezelve egy upstream hiba téves Valid/Invalid döntéssé válhatna.
        if (hasInvalidStringField(body)) {
            return malformedResponse(vat);
        }
        var userError = optString(body, FIELD_USER_ERROR);
        var errorOutcome = mapUserError(vat, userError);
        if (errorOutcome != null) {
            return errorOutcome;
        }
        var requestDateText = optString(body, FIELD_REQUEST_DATE);
        if (hasOversizedResponseField(body, requestDateText)) {
            return malformedResponse(vat);
        }
        var requestDate = parseInstant(requestDateText);
        if (requestDate.isEmpty()) {
            return malformedResponse(vat);
        }
        var validity = responseValidity(body, userError);
        if (validity.isEmpty() || !matchesEchoedVat(body, vat)) {
            return malformedResponse(vat);
        }
        boolean valid = validity.orElseThrow();
        if (!valid) {
            return new ViesResponse.Invalid(vat.countryCode(), vat.full(), requestDate.orElseThrow());
        }
        return new ViesResponse.Valid(
                vat.countryCode(),
                vat.full(),
                withoutPlaceholder(optString(body, FIELD_NAME)),
                withoutPlaceholder(optString(body, FIELD_ADDRESS)),
                requestDate.orElseThrow(),
                withoutPlaceholder(optString(body, FIELD_REQUEST_IDENTIFIER)),
                false);
    }

    /** Rejects present response fields whose JSON type is not a string. */
    private static boolean hasInvalidStringField(Map<?, ?> body) {
        return RESPONSE_STRING_FIELDS.stream().anyMatch(key -> hasNonStringValue(body, key));
    }

    /**
     * Maps an upstream error code without ever exposing attacker-controlled text as
     * a machine/log code. A {@code null} result means normal validity mapping continues.
     */
    private static ViesResponse mapUserError(VatFormat.Normalized vat, String userError) {
        if (userError == null || userError.isBlank()
                || "VALID".equals(userError) || "INVALID".equals(userError)) {
            return null;
        }
        if (userError.length() > MAX_ERROR_CODE_LENGTH
                || !SAFE_ERROR_CODE.matcher(userError).matches()) {
            return malformedResponse(vat);
        }
        if (INPUT_ERRORS.contains(userError)) {
            return new ViesResponse.MalformedInput(vat.full(), "VIES rejected the request: " + userError);
        }
        return new ViesResponse.Unavailable(vat.countryCode(), vat.full(), userError);
    }

    /** Enforces conservative bounds before copying optional upstream strings. */
    private static boolean hasOversizedResponseField(Map<?, ?> body, String requestDateText) {
        return exceedsLength(requestDateText, MAX_DATE_LENGTH)
                || exceedsLength(optString(body, FIELD_NAME), MAX_NAME_LENGTH)
                || exceedsLength(optString(body, FIELD_ADDRESS), MAX_ADDRESS_LENGTH)
                || exceedsLength(optString(body, FIELD_REQUEST_IDENTIFIER), MAX_CONSULTATION_LENGTH);
    }

    /**
     * Accepts the GET field ({@code isValid}) or POST field ({@code valid}), while
     * rejecting missing, non-boolean, contradictory or error-inconsistent decisions.
     */
    private static Optional<Boolean> responseValidity(Map<?, ?> body, String userError) {
        var getValidity = body.get(FIELD_IS_VALID);
        var postValidity = body.get(FIELD_VALID);
        if (body.containsKey(FIELD_IS_VALID) && body.containsKey(FIELD_VALID)
                && (!Objects.equals(getValidity, postValidity)
                || !(getValidity instanceof Boolean))) {
            return Optional.empty();
        }
        var rawValidity = body.containsKey(FIELD_IS_VALID) ? getValidity : postValidity;
        if (!(rawValidity instanceof Boolean boxedValidity)) {
            return Optional.empty();
        }
        boolean valid = boxedValidity.booleanValue();
        if (("VALID".equals(userError) && !valid) || ("INVALID".equals(userError) && valid)) {
            return Optional.empty();
        }
        return Optional.of(valid);
    }

    /**
     * Binds optional upstream echo fields to the requested VAT, preventing a mixed
     * proxy response from being relabelled as another company's result.
     */
    private static boolean matchesEchoedVat(Map<?, ?> body, VatFormat.Normalized vat) {
        if (!body.containsKey(FIELD_COUNTRY_CODE) && !body.containsKey(FIELD_VAT_NUMBER)) {
            return true;
        }
        var echoedCountry = body.containsKey(FIELD_COUNTRY_CODE)
                ? optString(body, FIELD_COUNTRY_CODE) : vat.countryCode();
        var echoedNumber = body.containsKey(FIELD_VAT_NUMBER)
                ? optString(body, FIELD_VAT_NUMBER) : vat.nationalNumber();
        return echoedCountry != null && echoedNumber != null
                && VatFormat.normalize(echoedCountry, echoedNumber).filter(vat::equals).isPresent();
    }

    private static ViesResponse.Unavailable malformedResponse(VatFormat.Normalized vat) {
        return new ViesResponse.Unavailable(vat.countryCode(), vat.full(), ERROR_MALFORMED_RESPONSE);
    }

    /** VIES sends "---" when a member state withholds a field. */
    private static Optional<String> withoutPlaceholder(String value) {
        if (value == null || value.isBlank() || "---".equals(value.strip())) {
            return Optional.empty();
        }
        return Optional.of(value.strip());
    }

    private static Optional<Instant> parseInstant(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Instant.parse(raw));
        } catch (DateTimeParseException e) {
            try {
                return Optional.of(OffsetDateTime.parse(raw).toInstant());
            } catch (DateTimeParseException ignored) {
                return Optional.empty();
            }
        }
    }

    private static String optString(Map<?, ?> map, String key) {
        return map.get(key) instanceof String s ? s : null;
    }

    private static boolean hasNonStringValue(Map<?, ?> map, String key) {
        return map.containsKey(key) && map.get(key) != null && !(map.get(key) instanceof String);
    }

    private static boolean exceedsLength(String value, int maximum) {
        return value != null && value.length() > maximum;
    }

    private static ViesResponse.Valid asCached(ViesResponse.Valid value) {
        return new ViesResponse.Valid(
                value.countryCode(), value.vatNumber(), value.traderName(), value.traderAddress(),
                value.requestDate(), value.consultationNumber(), true);
    }

    private static boolean isSafeCacheHit(
            ViesResponse.Valid value, VatFormat.Normalized vat) {
        return value != null
                && vat.countryCode().equals(value.countryCode())
                && vat.full().equals(value.vatNumber())
                && value.traderName() != null
                && value.traderAddress() != null
                && value.requestDate() != null
                && value.consultationNumber() != null
                && value.traderName().map(name -> name.length() <= MAX_NAME_LENGTH).orElse(true)
                && value.traderAddress().map(address -> address.length() <= MAX_ADDRESS_LENGTH).orElse(true)
                && value.consultationNumber().map(id -> id.length() <= MAX_CONSULTATION_LENGTH).orElse(true);
    }

    /**
     * EN: The subscriber stops reading immediately after the fixed response limit.
     * HU: A subscriber a fix válaszkorlát elérésekor azonnal megszakítja az olvasást.
     */
    private static HttpResponse.BodyHandler<String> boundedUtf8BodyHandler() {
        return ignored -> new BoundedUtf8Subscriber(MAX_RESPONSE_BYTES);
    }

    private static boolean causedByInvalidBody(Throwable failure) {
        for (var current = failure; current != null; current = current.getCause()) {
            if (current instanceof ResponseTooLargeException
                    || current instanceof CharacterCodingException) {
                return true;
            }
        }
        return false;
    }

    /** Bounded streaming body collector / Korlátos, folyamatos válaszgyűjtő. */
    private static final class BoundedUtf8Subscriber implements HttpResponse.BodySubscriber<String> {
        private final int maxBytes;
        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        private final java.util.concurrent.CompletableFuture<String> body = new java.util.concurrent.CompletableFuture<>();
        private Flow.Subscription subscription;

        private BoundedUtf8Subscriber(int maxBytes) {
            this.maxBytes = maxBytes;
        }

        @Override
        public CompletionStage<String> getBody() {
            return body;
        }

        @Override
        public void onSubscribe(Flow.Subscription next) {
            if (subscription != null) {
                next.cancel();
                return;
            }
            subscription = next;
            next.request(1);
        }

        @Override
        public void onNext(List<ByteBuffer> buffers) {
            if (body.isDone()) {
                return;
            }
            for (var buffer : buffers) {
                var length = buffer.remaining();
                if (length > maxBytes - bytes.size()) {
                    subscription.cancel();
                    body.completeExceptionally(new ResponseTooLargeException());
                    return;
                }
                var chunk = new byte[length];
                buffer.get(chunk);
                bytes.writeBytes(chunk);
            }
            subscription.request(1);
        }

        @Override
        public void onError(Throwable failure) {
            body.completeExceptionally(failure);
        }

        @Override
        public void onComplete() {
            try {
                var decoder = StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT);
                body.complete(decoder.decode(ByteBuffer.wrap(bytes.toByteArray())).toString());
            } catch (CharacterCodingException invalidUtf8) {
                body.completeExceptionally(invalidUtf8);
            }
        }
    }

    private static final class ResponseTooLargeException extends IOException {
        private static final long serialVersionUID = 1L;

        private ResponseTooLargeException() {
            super("VIES response exceeded " + MAX_RESPONSE_BYTES + " bytes");
        }
    }

    private static boolean isRetryable(String errorCode) {
        return !ERROR_CLIENT_OVERLOADED.equals(errorCode)
                && !ERROR_CLIENT_CLOSED.equals(errorCode)
                && ViesError.of(errorCode).retryable();
    }

    private static ViesResponse.MalformedInput malformed(String input) {
        return new ViesResponse.MalformedInput(safeInput(input),
                "Unrecognized member state prefix or invalid VAT number format");
    }

    private static String safeInput(String input) {
        var value = String.valueOf(input);
        var safe = new StringBuilder(Math.min(value.length(), 129));
        var offset = 0;
        var count = 0;
        while (offset < value.length() && count < 128) {
            var codePoint = value.codePointAt(offset);
            var unpairedSurrogate = codePoint <= Character.MAX_VALUE
                    && Character.isSurrogate((char) codePoint);
            if (unpairedSurrogate || Character.isISOControl(codePoint)
                    || codePoint == 0x2028 || codePoint == 0x2029) {
                safe.append('?');
            } else {
                safe.appendCodePoint(codePoint);
            }
            offset += Character.charCount(codePoint);
            count++;
        }
        return offset < value.length() ? safe.append('…').toString() : safe.toString();
    }

    /**
     * EN: External cache keys are opaque so VAT/requester identifiers do not leak
     * through Redis key listings, metrics or diagnostics.
     * HU: A külső cache-kulcs nem olvasható, így a cél- és lekérdezői adószám nem
     * szivárog Redis kulcslistába, metrikába vagy diagnosztikába.
     */
    private static String operationKey(VatFormat.Normalized vat, ViesRequester requester) {
        var material = vat.full() + "|" + (requester == null ? "anon" : requester.full());
        try {
            var digest = MessageDigest.getInstance("SHA-256")
                    .digest(material.getBytes(StandardCharsets.US_ASCII));
            return "vies:v1:" + Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException impossible) {
            throw new ExceptionInInitializerError(impossible);
        }
    }

    private static ViesResponse overloaded(String fullVatNumber) {
        return VatFormat.normalize(fullVatNumber)
                .<ViesResponse>map(ViesClient::overloaded)
                .orElseGet(() -> malformed(fullVatNumber));
    }

    private static ViesResponse overloaded(VatFormat.Normalized vat) {
        return new ViesResponse.Unavailable(vat.countryCode(), vat.full(), ERROR_CLIENT_OVERLOADED);
    }

    private static ViesResponse closed(VatFormat.Normalized vat) {
        return new ViesResponse.Unavailable(vat.countryCode(), vat.full(), ERROR_CLIENT_CLOSED);
    }

    private static ViesResponse overloaded(String countryCode, String vatNumber) {
        return VatFormat.normalize(countryCode, vatNumber)
                .<ViesResponse>map(ViesClient::overloaded)
                .orElseGet(() -> malformed(safeInput(countryCode) + " " + safeInput(vatNumber)));
    }

    /** Uses non-cryptographic jitter only to desynchronize retries; no secret is generated. */
    @SuppressWarnings("java:S2245")
    private boolean sleepBackoff(int attempt) {
        var exponential = retryDelay.multipliedBy(1L << (attempt - 1));
        var jitterBound = Math.max(1L, exponential.toMillis() / 2L);
        var delay = exponential.plusMillis(ThreadLocalRandom.current().nextLong(jitterBound));
        try {
            Thread.sleep(delay);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private ViesResponse runSync(
            Supplier<ViesResponse> operation, Supplier<ViesResponse> onOverload) {
        var readLock = lifecycle.readLock();
        readLock.lock();
        try {
            ensureOpen();
            if (!syncSlots.tryAcquire()) {
                return onOverload.get();
            }
            activeThreads.add(Thread.currentThread());
        } finally {
            readLock.unlock();
        }
        try {
            return operation.get();
        } finally {
            activeThreads.remove(Thread.currentThread());
            syncSlots.release();
        }
    }

    private ViesAvailability runSyncAvailability(Supplier<ViesAvailability> operation) {
        var readLock = lifecycle.readLock();
        readLock.lock();
        try {
            ensureOpen();
            if (!syncSlots.tryAcquire()) {
                throw new ViesException("VIES client overloaded / A VIES kliens túlterhelt");
            }
            activeThreads.add(Thread.currentThread());
        } finally {
            readLock.unlock();
        }
        try {
            return operation.get();
        } finally {
            activeThreads.remove(Thread.currentThread());
            syncSlots.release();
        }
    }

    private CompletableFuture<ViesResponse> admit(Supplier<AsyncAdmission> operation) {
        var readLock = lifecycle.readLock();
        readLock.lock();
        AsyncAdmission admission;
        try {
            ensureOpen();
            admission = operation.get();
        } finally {
            readLock.unlock();
        }
        admission.start().run();
        return admission.result();
    }

    /**
     * EN: Splits atomic lifecycle registration from possibly inline executor execution.
     * HU: Szétválasztja az atomi regisztrációt az akár helyben futó executor indításától.
     */
    private record AsyncAdmission(CompletableFuture<ViesResponse> result, Runnable start) {
        private static AsyncAdmission completed(ViesResponse response) {
            return completedFuture(CompletableFuture.completedFuture(response));
        }

        private static AsyncAdmission completedFuture(CompletableFuture<ViesResponse> result) {
            return new AsyncAdmission(result, () -> { });
        }
    }

    private void ensureOpen() {
        if (closed.get()) {
            throw new IllegalStateException("ViesClient is closed / A ViesClient már leállt");
        }
    }

    // -------------------------------------------------------------------- builder

    /**
     * Fluent configuration with safe standalone defaults.
     * Magyarul: éles, többgépes üzemben terhelésmérés alapján hangold a limiteket,
     * timeoutokat és a megosztott cache-t.
     */
    public static final class Builder {

        private String baseUrl = DEFAULT_BASE_URL;
        private Duration connectTimeout = Duration.ofSeconds(5);
        private Duration requestTimeout = Duration.ofSeconds(8);
        private Duration admissionTimeout = Duration.ofSeconds(2);
        private boolean cacheEnabled = true;
        private ViesCache cache;
        private Duration cacheTtl = Duration.ofHours(24);
        private int cacheMaxEntries = 10_000;
        private ViesRequester defaultRequester;
        private int retries = 0;
        private Duration retryDelay = Duration.ofMillis(400);
        private int maxConcurrentRequests = 32;
        private int maxPendingSyncRequests = 512;
        private int maxPendingAsyncRequests = 512;
        private String userAgent = "vies-client-java/1.0 (Java)";
        private ExecutorService executor;

        private Builder() {
        }

        /** Overrides the VIES REST base URL (e.g. to point at a mock in tests). */
        public Builder baseUrl(String baseUrl) {
            var value = Objects.requireNonNull(baseUrl, "baseUrl").strip();
            while (value.endsWith("/")) {
                value = value.substring(0, value.length() - 1);
            }
            var uri = URI.create(value);
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null) {
                throw new IllegalArgumentException(
                        "baseUrl must be an absolute HTTP(S) URL / A baseUrl abszolút HTTP(S) URL legyen");
            }
            if (uri.getRawUserInfo() != null || uri.getRawQuery() != null || uri.getRawFragment() != null) {
                throw new IllegalArgumentException(
                        "baseUrl must not contain user info, query, or fragment"
                                + " / A baseUrl nem tartalmazhat felhasználói adatot, queryt vagy fragmentet");
            }
            if ("http".equalsIgnoreCase(uri.getScheme()) && !isLoopbackHost(uri.getHost())) {
                throw new IllegalArgumentException(
                        "Plain HTTP is allowed only for loopback tests"
                                + " / Sima HTTP csak helyi tesztvégponthoz engedélyezett");
            }
            if (uri.getPort() > 65_535) {
                throw new IllegalArgumentException(
                        "baseUrl port must be between 0 and 65535 / A baseUrl portja 0 és 65535 közé essen");
            }
            this.baseUrl = value;
            return this;
        }

        public Builder connectTimeout(Duration timeout) {
            this.connectTimeout = requirePositive(timeout, "connectTimeout");
            return this;
        }

        /** Per-request timeout; VIES is an interactive service, keep this short. Default 8 s. */
        public Builder requestTimeout(Duration timeout) {
            this.requestTimeout = requirePositive(timeout, "requestTimeout");
            return this;
        }

        /**
         * Maximum wait for a free outbound slot. Default: 2 seconds.
         * Magyarul: ennyi ideig várhat a kérés szabad hálózati helyre.
         */
        public Builder admissionTimeout(Duration timeout) {
            this.admissionTimeout = requireNanos(timeout, "admissionTimeout");
            return this;
        }

        /** Replaces the built-in in-memory cache (e.g. with a Redis-backed one). */
        public Builder cache(ViesCache cache) {
            this.cache = Objects.requireNonNull(cache, "cache");
            return this;
        }

        /**
         * Disables persisted caching. Concurrent calls for the same VAT/requester may
         * still share one in-flight VIES request; a later call made after that request
         * completes performs a new request.
         *
         * <p>Magyarul: kikapcsolja a tárolt cache-t. Az egyidejű, azonos adószámot és
         * lekérdezőt használó hívások továbbra is megoszthatnak egy folyamatban lévő
         * VIES-kérést; annak befejezése után a következő hívás új kérést indít.</p>
         */
        public Builder disableCache() {
            this.cacheEnabled = false;
            this.cache = null;
            return this;
        }

        /** How long confirmed (valid) results are cached. Default 24 h. */
        public Builder cacheTtl(Duration ttl) {
            this.cacheTtl = requirePositive(ttl, "cacheTtl");
            return this;
        }

        /** Upper bound of the built-in in-memory cache. Default 10 000 entries. */
        public Builder cacheMaxEntries(int maxEntries) {
            if (maxEntries < 1 || maxEntries > 100_000) {
                throw new IllegalArgumentException(
                        "cacheMaxEntries must be between 1 and 100000"
                                + " / A cacheMaxEntries értéke 1 és 100000 közé essen");
            }
            this.cacheMaxEntries = maxEntries;
            return this;
        }

        /**
         * Requester applied to every check that does not pass one explicitly —
         * typically your own company's EU VAT number. VIES may then return a
         * consultation number in {@link ViesResponse.Valid#consultationNumber()}.
         * The value is optional and its legal/evidentiary significance depends on
         * the applicable rules.
         *
         * <p>Magyarul: a VIES ilyenkor konzultációs azonosítót adhat vissza, de az
         * érték nem garantált; bizonyító erejét a vonatkozó szabályok határozzák meg.</p>
         */
        public Builder defaultRequester(ViesRequester requester) {
            this.defaultRequester = Objects.requireNonNull(requester, PARAM_REQUESTER);
            return this;
        }

        /** Automatic retries on transient failures (0–5, default 0), with exponential jittered backoff. */
        public Builder retries(int retries) {
            if (retries < 0 || retries > 5) {
                throw new IllegalArgumentException(
                        "retries must be between 0 and 5 / A retries értéke 0 és 5 közé essen");
            }
            this.retries = retries;
            return this;
        }

        /**
         * Sets the initial retry delay; later attempts use exponential backoff and jitter.
         * Magyarul: a retry kezdő késleltetése; a további próbák exponenciális,
         * véletlen szórással kiegészített várakozást használnak.
         */
        public Builder retryDelay(Duration delay) {
            var value = requirePositive(delay, "retryDelay");
            if (value.compareTo(Duration.ofMillis(100)) < 0
                    || value.compareTo(Duration.ofSeconds(30)) > 0) {
                throw new IllegalArgumentException(
                        "retryDelay must be between 100 ms and 30 s"
                                + " / A retryDelay 100 ms és 30 s közé essen");
            }
            try {
                // EN: Five retries can reach 16x backoff plus almost 8x jitter.
                // HU: Öt retry esetén 16x backoff és közel 8x jitter is előfordulhat.
                if (value.multipliedBy(24).toMillis() < 0) {
                    throw new ArithmeticException("negative retry capacity");
                }
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException(
                        "retryDelay is too large / A retryDelay túl nagy", e);
            }
            this.retryDelay = value;
            return this;
        }

        /**
         * Maximum simultaneous network calls per client instance. Excess calls wait on
         * virtual threads, providing backpressure without consuming platform threads.
         * Default: 32. Use a lower value when the VIES service throttles your deployment.
         * Magyarul: ez példányonkénti, nem globális limit; több worker elé közös limiter kell.
         */
        public Builder maxConcurrentRequests(int maxConcurrentRequests) {
            if (maxConcurrentRequests < 1 || maxConcurrentRequests > 512) {
                throw new IllegalArgumentException("maxConcurrentRequests must be between 1 and 512"
                        + " / A maxConcurrentRequests értéke 1 és 512 közé essen");
            }
            this.maxConcurrentRequests = maxConcurrentRequests;
            return this;
        }

        /**
         * Maximum synchronous calls admitted per client instance. Default: 512.
         * Magyarul: a további sync hívások azonnal {@code CLIENT_OVERLOADED} eredményt kapnak.
         */
        public Builder maxPendingSyncRequests(int maxPendingSyncRequests) {
            if (maxPendingSyncRequests < 1 || maxPendingSyncRequests > 10_000) {
                throw new IllegalArgumentException("maxPendingSyncRequests must be between 1 and 10000"
                        + " / A maxPendingSyncRequests értéke 1 és 10000 közé essen");
            }
            this.maxPendingSyncRequests = maxPendingSyncRequests;
            return this;
        }

        /**
         * Maximum accepted async operations per client instance, including active
         * calls. Further submissions complete immediately with
         * {@code CLIENT_OVERLOADED}, providing bounded-memory backpressure. A unique
         * cache lookup briefly uses one slot; same-key single-flight followers do not.
         * Default: 512.
         * Magyarul: az egyedi cache-olvasás rövid ideig egy helyet használ, az azonos
         * single-flight követők viszont nem fogyasztanak további helyet.
         */
        public Builder maxPendingAsyncRequests(int maxPendingAsyncRequests) {
            if (maxPendingAsyncRequests < 1 || maxPendingAsyncRequests > 10_000) {
                throw new IllegalArgumentException("maxPendingAsyncRequests must be between 1 and 10000"
                        + " / A maxPendingAsyncRequests értéke 1 és 10000 közé essen");
            }
            this.maxPendingAsyncRequests = maxPendingAsyncRequests;
            return this;
        }

        public Builder userAgent(String userAgent) {
            if (userAgent == null || userAgent.isBlank() || userAgent.length() > 256
                    || userAgent.chars().anyMatch(c -> c < 0x20 || c > 0x7e)) {
                throw new IllegalArgumentException(
                        "userAgent must be 1-256 characters without controls"
                                + " / A userAgent 1-256 vezérlőkarakter nélküli jel legyen");
            }
            this.userAgent = userAgent;
            return this;
        }

        /**
         * Executor for {@code checkAsync}. Default: a virtual-thread-per-task executor
         * owned (and closed) by the client. A custom executor is never closed here.
         */
        public Builder executor(ExecutorService executor) {
            this.executor = Objects.requireNonNull(executor, "executor");
            return this;
        }

        public ViesClient build() {
            return new ViesClient(this);
        }

        private static Duration requirePositive(Duration duration, String name) {
            Objects.requireNonNull(duration, name);
            if (duration.isZero() || duration.isNegative()) {
                throw new IllegalArgumentException(name + " must be positive / pozitív érték szükséges");
            }
            return duration;
        }

        private static Duration requireNanos(Duration duration, String name) {
            var value = requirePositive(duration, name);
            try {
                if (value.toNanos() <= 0) {
                    throw new ArithmeticException("non-positive nanosecond duration");
                }
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException(name + " is too large / túl nagy", e);
            }
            return value;
        }

        private static boolean isLoopbackHost(String host) {
            var lower = host.toLowerCase(java.util.Locale.ROOT);
            return "localhost".equals(lower)
                    || "127.0.0.1".equals(lower)
                    || "::1".equals(lower)
                    || "[::1]".equals(lower);
        }
    }
}
