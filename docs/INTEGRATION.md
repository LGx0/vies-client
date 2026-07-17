# Bekötési útmutató / Integration guide

## 1. Életciklus / Lifecycle

Az all-zero példaszámok szintetikusak. Éles requesternek kizárólag a saját EU-
adószámodat add meg, lehetőleg secret/config forrásból. / All-zero VAT examples are
synthetic. Supply only your own authorized requester VAT from trusted secret/config.

Egy alkalmazás- vagy worker-példányban egyetlen `ViesClient` objektumot használj.
Ne hozz létre klienst HTTP-kérésenként: elveszítenéd a connection poolt, cache-t,
single-flight összevonást és a lokális limiteket.

Use one `ViesClient` per application/worker process. Do not create a client per
HTTP request; doing so discards connection pooling, cache, single-flight, and local
limits.

```java
var client = ViesClient.builder()
        .connectTimeout(Duration.ofSeconds(5))
        .requestTimeout(Duration.ofSeconds(8))
        .admissionTimeout(Duration.ofSeconds(2))
        .maxConcurrentRequests(32)
        .maxPendingSyncRequests(512)
        .maxPendingAsyncRequests(512)
        .retries(1)
        .build();

// Application shutdown / Alkalmazásleállítás
client.close();
```

`close()` után új művelet nem indítható. A leállítás megszakítja az aktív belső
műveleteket, és `CLIENT_CLOSED` eredményt ad a közös folyamatban lévő kéréseknek.
Az ezután közvetlenül meghívott sync és async metódusok egyaránt szinkron
`IllegalStateException` kivételt dobnak.

After `close()`, no new work is accepted. Shutdown interrupts active internal work
and completes shared in-flight requests with `CLIENT_CLOSED`.
New sync and async API calls made afterward both throw `IllegalStateException`
synchronously.

## 2. Szinkron API / Synchronous API

```java
ViesResponse response = client.check("DE 000 000 000"); // synthetic / szintetikus

switch (response) {
    case ViesResponse.Valid valid ->
        System.out.println("VALID: " + valid.vatNumber());
    case ViesResponse.Invalid invalid ->
        System.out.println("INVALID: " + invalid.vatNumber());
    case ViesResponse.Unavailable unavailable -> {
        var error = unavailable.error().orElseThrow();
        System.out.println(error.messageHu());
        System.out.println(error.messageEn());
    }
    case ViesResponse.MalformedInput malformed -> {
        var error = malformed.error().orElseThrow();
        System.out.println(error.messageHu());
        System.out.println(error.messageEn());
    }
}
```

A szinkron API hívóinak száma is korlátos (`maxPendingSyncRequests`). A limiten
felüli érvényes kérés azonnal `CLIENT_OVERLOADED` eredményt kap.

Synchronous callers are also admission-bounded by `maxPendingSyncRequests`.

## 3. Aszinkron API / Asynchronous API

```java
client.checkAsync("PL0000000000")
        .thenAccept(response -> {
            // Handle all four response variants / Kezeld mind a négy eredményt.
        });
```

Az async API az alapértelmezett executorral virtuális szálon fut; saját executor
esetén annak ütemezési szabályai érvényesek. Azonos adószám+lekérdező kérések egy
belső shared future-höz csatlakoznak. A hívó egy cancel-biztos másolatot kap:
egyetlen fogyasztó cancel-je nem szakítja meg a többiek közös kérését.

With the default executor, the async API runs on virtual threads; a custom executor
uses its own scheduling policy. Identical VAT/requester calls join one internal
shared future. Each caller receives a cancellation-safe copy.

Ne gyűjts milliónyi future-t memóriában. A tartós queue consumer mindig korlátos
ablakot tartson aktívan.

Do not retain millions of futures. A durable-queue consumer must keep a bounded
processing window.

## 4. HTTP API szerződés / HTTP API contract

Ajánlott leképezés / Recommended mapping:

| ViesResponse | HTTP | Retry | Megjegyzés / Note |
|---|---:|---:|---|
| `Valid` | 200 | no | domain result |
| `Invalid` | 200 | no | domain result, not an HTTP failure |
| `MalformedInput` | 400 | no | caller must fix input |
| `Unavailable(CLIENT_OVERLOADED)` | 429 | delayed | local backpressure |
| other `Unavailable` | 503 | by `error.retryable()` | no validity decision |

Példa hiba JSON / Example error JSON:

```json
{
  "status": "UNAVAILABLE",
  "vatNumber": "HU00000000",
  "errorCode": "MS_UNAVAILABLE",
  "messageHu": "A tagállami adóhatóság rendszere átmenetileg nem érhető el.",
  "messageEn": "The member state's tax system is temporarily unavailable.",
  "retryable": true
}
```

A `messageHu`/`messageEn` felhasználói szöveg. Loghoz, metrikához és klienslogikához
mindig a stabil `errorCode` értéket használd.

`messageHu`/`messageEn` are user-facing. Use stable `errorCode` values for logs,
metrics, and client logic.

## 5. Spring Boot

### Konfiguráció / Configuration

```java
@Configuration
class ViesConfiguration {
    @Bean(destroyMethod = "close")
    ViesClient viesClient(ViesCache cache) {
        return ViesClient.builder()
                .cache(cache)
                .connectTimeout(Duration.ofSeconds(5))
                .requestTimeout(Duration.ofSeconds(8))
                .admissionTimeout(Duration.ofSeconds(2))
                .maxConcurrentRequests(32)
                .maxPendingSyncRequests(512)
                .maxPendingAsyncRequests(512)
                .retries(1)
                .build();
    }
}
```

### Controller

```java
@RestController
@RequestMapping("/api/v1/vat")
class VatController {
    private final ViesClient vies;

    VatController(ViesClient vies) {
        this.vies = vies;
    }

    @GetMapping("/{vatNumber}")
    ResponseEntity<?> check(@PathVariable String vatNumber) {
        return switch (vies.check(vatNumber)) {
            case ViesResponse.Valid v -> ResponseEntity.ok(Map.of(
                    "status", "VALID",
                    "vatNumber", v.vatNumber(),
                    "name", v.traderName().orElse(""),
                    "address", v.traderAddress().orElse(""),
                    "requestDate", v.requestDate().toString(),
                    "fromCache", v.fromCache()));
            case ViesResponse.Invalid i -> ResponseEntity.ok(Map.of(
                    "status", "INVALID",
                    "vatNumber", i.vatNumber(),
                    "requestDate", i.requestDate().toString()));
            case ViesResponse.MalformedInput m -> problem(400, m);
            case ViesResponse.Unavailable u -> problem(
                    "CLIENT_OVERLOADED".equals(u.errorCode()) ? 429 : 503, u);
        };
    }

    private static ResponseEntity<?> problem(int status, ViesResponse response) {
        var error = response.error().orElseThrow();
        return ResponseEntity.status(status).body(Map.of(
                "errorCode", error.code(),
                "retryable", error.retryable(),
                "messageHu", error.messageHu(),
                "messageEn", error.messageEn()));
    }
}
```

A könyvtárnak nincs Spring-függősége; a fenti kód a fogyasztó alkalmazás része.
The library has no Spring dependency; this adapter belongs to the consuming app.

## 6. Plain JDK HTTP server

A teljes futtatható példa: `examples/ViesDemoServer.java`.
Full runnable example: `examples/ViesDemoServer.java`.

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java 8085
curl "http://localhost:8085/vat-check?number=DE000000000"
```

A példa virtuális szálakat, valós HTTP státuszokat és kétnyelvű hibaválaszokat használ.
The example uses virtual threads, meaningful HTTP statuses, and bilingual errors.

## 7. Requester és konzultációs azonosító / Requester and consultation ID

```java
var client = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
        .build();
```

A requester a saját közösségi adószámod. Érvényes lekérdezésnél a VIES adhat
`requestIdentifier`/`consultationNumber` értéket.

The requester is your own VAT number. VIES may return a consultation identifier for
a valid check.

Fontos: cache hit esetén ez az eredeti konzultáció azonosítója és dátuma, nem új
ellenőrzés. Friss, egymást követő lekérdezéshez `fromCache`, `requestDate`, rövid
TTL vagy `disableCache()` alapján alakíts ki üzleti szabályt. A cache kikapcsolása
nem kapcsolja ki a single-flight összevonást: az egyidejű, azonos VAT+requester
hívások egy hálózati kérést oszthatnak meg. A `consultationNumber` opcionális,
jogi vagy bizonyító ereje a vonatkozó szabályoktól függ.

Important: a cache hit contains the original consultation ID/date, not a new check.
Disabling stored caching does not disable single-flight: concurrent identical
VAT+requester calls may share one network request. A consultation identifier is
optional, and its legal or evidentiary significance depends on applicable rules.

## 8. Redis cache adapter

```java
final class RedisViesCache implements ViesCache {
    private final RedisClient redis; // application-specific adapter

    RedisViesCache(RedisClient redis) {
        this.redis = redis;
    }

    @Override
    public Optional<ViesResponse.Valid> get(String key) {
        // Use a short, bounded Redis timeout / Használj rövid Redis timeoutot.
        return redis.get(key).map(this::decode); // key is already namespaced and opaque
    }

    @Override
    public void put(String key, ViesResponse.Valid value, Duration ttl) {
        redis.set(key, encode(value), ttl);
    }

    // encode/decode are application-specific and must preserve every record field.
}
```

Követelmények / Requirements:

- thread-safe implementation;
- short connection/command timeout;
- versioned key namespace;
- TLS in transit, authenticated Redis, least-privilege ACL and private networking;
- encryption and access control for snapshots/backups;
- approved retention/erasure policy for VAT, trader and consultation data;
- metrics and health alerts;
- no unbounded internal retry;
- correct serialization of `requestDate`, optionals, and `fromCache`.

The client supplies a versioned opaque SHA-256 cache key; do not replace it with a
raw VAT number in adapter metrics or key names. / A kliens verziózott, nem olvasható
SHA-256 kulcsot ad; az adapter metrikájában vagy kulcsnevében ne állítsd vissza nyers
adószámra.

Olvasási kivétel `CACHE_ERROR` eredményt okoz és nem indít VIES fallbacket.
Írási kivétel nem törli a már megkapott hiteles `Valid` eredményt.

A read exception returns `CACHE_ERROR` without a VIES fallback. A write exception
does not erase an authoritative `Valid` result.

## 9. Retry, queue és DLQ / Retry, queue, and DLQ

Nagyüzemi feldolgozásban / In high-scale processing:

1. persist the job before processing;
2. use an idempotency key;
3. call VIES with small local retry count;
4. if `error.retryable()`, schedule delayed retry with exponential delay;
5. stop after a configured maximum and move to DLQ/manual review;
6. never retry `Invalid` or `MalformedInput` unchanged.

Ne tartsd a durable retry-t memóriában `CompletableFuture` lánccal.
Do not implement durable retries as in-memory `CompletableFuture` chains.

## 10. Több node/pod / Multiple nodes or pods

Minden replika saját connection poolt, cache-t, single-flight táblát és semaphoret
kap. A `maxConcurrentRequests(32)` ezért **nem globális 32**.

Every replica has its own connection pool, cache, single-flight table, and
semaphores. `maxConcurrentRequests(32)` is therefore **not a global limit of 32**.

Kötelező komponensek nagy méretnél / Required at scale:

- durable partitioned queue;
- bounded consumer count/window;
- distributed/global rate limiter;
- shared cache where business semantics allow it;
- delayed retry and DLQ;
- idempotency/deduplication;
- autoscaling based on queue age, not direct unlimited VIES concurrency.

## 11. Health és observability

- A liveness ne függjön a VIES-től / Liveness must not depend on VIES.
- `availability()` ritka, cache-elt diagnosztika legyen / Poll and cache it sparingly.
- Mérd a p50/p95/p99 latency-t / Measure p50/p95/p99 latency.
- Számold eredménytípus és `errorCode` szerint / Count by result and error code.
- Mérd a cache hitet, retry-t, overloadot, queue age-et és DLQ-t.
- Measure cache hits, retries, overloads, queue age, and DLQ size.
- Adószámot/nevet/címet maszkoltan logolj / Mask VAT/name/address in logs.

## 12. Anti-patterns / Kerülendő megoldások

- client per request / kérésenként új kliens;
- unlimited futures or sync callers / korlátlan future vagy sync hívó;
- treating a per-pod limiter as global / lokális limiter globálisnak tekintése;
- retrying every error immediately / minden hiba azonnali retry-ja;
- converting `Unavailable` to `Invalid`;
- treating cached consultation data as a fresh proof;
- calling live VIES from liveness checks or default unit tests.

## 13. Production checklist / Éles üzemi ellenőrzőlista

- [ ] Singleton lifecycle and shutdown are configured.
- [ ] All four `ViesResponse` variants are handled explicitly.
- [ ] API returns stable code plus HU/EN messages.
- [ ] Sync, async, ingress, and network limits are bounded.
- [ ] Multi-node traffic uses a global/distributed limiter.
- [ ] Cache freshness and consultation-proof policy are approved.
- [ ] Delayed retry, maximum attempts, idempotency, and DLQ exist.
- [ ] Metrics, alerts, and masked logs exist.
- [ ] Liveness is independent of VIES.
- [ ] Unit, local integration, concurrency, load, soak, and failure tests are defined.
