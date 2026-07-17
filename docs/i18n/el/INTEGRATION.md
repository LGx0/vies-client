# Ελληνικά (el) — Ενσωμάτωση

Το `disableCache()` απενεργοποιεί την αποθηκευμένη/μόνιμη cache, αλλά ταυτόχρονες κλήσεις με το ίδιο ζεύγος ΑΦΜ + requester μπορούν να μοιραστούν ένα single-flight αίτημα δικτύου. Μια μεταγενέστερη κλήση μετά την ολοκλήρωσή του εκτελεί νέο αίτημα VIES. Το `consultationNumber` είναι προαιρετικό και μπορεί να επιστραφεί από το VIES, αλλά δεν είναι ποτέ εγγυημένο· η αποδεικτική του αξία εξαρτάται από τους τοπικούς κανόνες. Φορτώνετε το `MY_EU_VAT_NUMBER` μόνο από αξιόπιστη πηγή secret/config.

**Languages:** [English](../../INTEGRATION.md) · [Ελληνικά](INTEGRATION.md) · [Όλες οι γλώσσες](../../LANGUAGES.md)

> Ενημερωτική μετάφραση. Σε περίπτωση διαφοράς υπερισχύει το αγγλικό τεχνικό και νομικό πρωτότυπο. Μόνο τα ριζικά LICENSE και NOTICE είναι νομικά έγκυρα· η μετάφραση δεν αποτελεί άδεια. [LICENSE](../../../LICENSE), [NOTICE](../../../NOTICE).

Χρησιμοποιήστε ένα thread-safe `ViesClient` ανά διεργασία και όχι ανά request. Το `close()` διακόπτει την εσωτερική εργασία, τα κοινά αιτήματα λαμβάνουν `CLIENT_CLOSED` και νέες κλήσεις ρίχνουν `IllegalStateException`.

```java
var client = ViesClient.builder()
.connectTimeout(Duration.ofSeconds(5)).requestTimeout(Duration.ofSeconds(8))
.admissionTimeout(Duration.ofSeconds(2)).maxConcurrentRequests(32)
.maxPendingSyncRequests(512).maxPendingAsyncRequests(512).retries(1).build();
```

Τα `check`/`checkAsync` επιστρέφουν τέσσερις variants. Η async διαδρομή χρησιμοποιεί virtual threads και cancellation-safe single-flight· μην κρατάτε εκατομμύρια futures.

| Result | HTTP | Retry |
|---|---:|---|
| Valid / Invalid | 200 | no |
| MalformedInput | 400 | no |
| CLIENT_OVERLOADED | 429 | delayed |
| other Unavailable | 503 | error.retryable() |

Το error JSON περιέχει `errorCode`, `messageHu`, `messageEn`, `retryable`. Στο Spring χρησιμοποιήστε singleton Bean με `destroyMethod=close` και controller για όλες τις variants. Παράδειγμα plain JDK: `examples/ViesDemoServer.java`.

Ο requester είναι ο δικός σας αριθμός ΦΠΑ. Τα cached `requestDate`/`consultationNumber` περιγράφουν την αρχική διαβούλευση, όχι νέο έλεγχο, και η αξία τους εξαρτάται από τους τοπικούς κανόνες. Το `ViesCache` πρέπει να είναι thread-safe, με σύντομο timeout, versioned namespace, metrics, bounded retry και πλήρη serialization. Read exception → `CACHE_ERROR` χωρίς fallback· write exception διατηρεί το `Valid`.

Σε κλίμακα: persist job και idempotency key, λίγα local retries, exponential delayed retry, max attempts και DLQ. Κάθε pod έχει τοπικά όρια, επομένως απαιτούνται global limiter, Redis, durable queue και deduplication.

Το liveness δεν εξαρτάται από το VIES. Μετρήστε p50/p95/p99, result/`errorCode`, cache, retry, overload, queue age/DLQ και αποκρύψτε VAT/name/address.

## Πλήρης adapter εφαρμογής

Ο client είναι immutable και thread-safe. Δημιουργήστε ένα singleton bean για όλη τη ζωή της εφαρμογής και κλείστε το στο shutdown. Χειριστείτε ρητά και τα τέσσερα αποτελέσματα· για λογική χρησιμοποιήστε το σταθερό errorCode και προς τον χρήστη τα messageHu/messageEn.

### Χειρισμός sync και async αποτελεσμάτων

```java
switch (client.check("DE000000000")) {
 case ViesResponse.Valid v -> use(v);
 case ViesResponse.Invalid i -> recordInvalid(i);
 case ViesResponse.Unavailable u -> {
   var e = u.error().orElseThrow();
   scheduleOnlyWhenRetryable(e.code(), e.retryable());
 }
 case ViesResponse.MalformedInput m -> rejectInput(m.reason());
}
client.checkAsync("PL0000000000").thenAccept(this::handle);
```

### Spring Boot

```java
@Bean(destroyMethod = "close")
ViesClient viesClient(ViesCache cache) {
 return ViesClient.builder().cache(cache)
.connectTimeout(Duration.ofSeconds(5))
.requestTimeout(Duration.ofSeconds(8))
.admissionTimeout(Duration.ofSeconds(2))
.maxConcurrentRequests(32)
.maxPendingSyncRequests(512)
.maxPendingAsyncRequests(512)
.retries(1).build();
}
```

### Redis adapter contract

```java
final class RedisViesCache implements ViesCache {
 public Optional<ViesResponse.Valid> get(String key) {
   return redis.get("vies:v1:" + key).map(this::decode);
 }
 public void put(String key, ViesResponse.Valid value, Duration ttl) {
   redis.set("vies:v1:" + key, encode(value), ttl);
 }
}
```

Η cache κρατά μόνο Valid. Το κλειδί περιέχει target και requester VAT. Ένα cache hit έχει fromCache=true και τα αρχικά requestDate/consultationNumber. Read failure σε distributed cache επιστρέφει CACHE_ERROR χωρίς VIES fallback· write failure δεν διαγράφει επιβεβαιωμένο Valid.

Τοπολογία εκατομμυρίων: API ingress → durable partitioned queue → bounded consumer window → worker JVMs → shared Redis → distributed/global rate limiter → VIES. Τα retryable αποτελέσματα πηγαίνουν σε delayed queue και μετά τα max attempts σε DLQ. Τα per-JVM semaphore και single-flight δεν είναι global.

Production checklist: singleton lifecycle; all four results; stable code plus HU/EN messages; bounded ingress/sync/async/network; global limiter; freshness policy; delayed retry/max attempts/idempotency/DLQ; p50/p95/p99 and masked logs; liveness independent of VIES; unit/integration/load/soak/failure tests.

## Εκτεταμένος οδηγός production

Μία instance `ViesClient` μοιράζεται από όλη την εφαρμογή ή worker. Έτσι επαναχρησιμοποιούνται JDK `HttpClient`, connection pool, TTL cache, single-flight και admission limits. Client ανά request χάνει αυτές τις προστασίες. Η instance είναι thread-safe και κλείνει με `close()`, `try`-with-resources ή Spring `@Bean(destroyMethod = "close")`. Μετά το κλείσιμο νέες sync/async κλήσεις ρίχνουν `IllegalStateException` και οι κοινές εργασίες ολοκληρώνονται με `CLIENT_CLOSED`.

`Valid` είναι επιβεβαίωση VIES, `Invalid` μη επιβεβαίωση, `MalformedInput` είσοδος που δεν αποστέλλεται και `Unavailable` απουσία απόφασης. Το `Unavailable` δεν γίνεται ποτέ `Invalid`. Για λογική, metrics και retry χρησιμοποιούνται `error.code()` και `error.retryable()`, ενώ προς τον χρήστη `messageHu` και `messageEn`. Ο έλεγχος μορφής δεν είναι επικύρωση VIES.

Το `VatFormat` αφαιρεί επιτρεπτά διαχωριστικά, κεφαλαιοποιεί και ελέγχει το σχήμα χώρας πριν από το δίκτυο. Το `GR` κανονικοποιείται σε `EL` και η Βόρεια Ιρλανδία χρησιμοποιεί `XI`. Το `defaultRequester` είναι ο αριθμός ΦΠΑ της οργάνωσης. Αν επιστραφούν, τα `consultationNumber` και `requestDate` μπορούν να βοηθήσουν στην τεκμηρίωση μιας διαβούλευσης, αλλά η νομική τους αξία εξαρτάται από τους τοπικούς κανόνες. Σε cache hit ανήκουν στον αρχικό έλεγχο. Το `disableCache()` δεν αποθηκεύει αποτελέσματα, όμως ίδιες ταυτόχρονες κλήσεις μπορούν να μοιραστούν ένα single-flight αίτημα.

Το `maxPendingSyncRequests` περιορίζει sync callers, το `maxPendingAsyncRequests` μοναδικούς async leaders, το `maxConcurrentRequests` πραγματικά HTTP requests και το `admissionTimeout` την αναμονή slot. Υπέρβαση επιστρέφει `CLIENT_OVERLOADED`. Η async εκτέλεση χρησιμοποιεί virtual threads. Caller-provided executor ανήκει στην εφαρμογή. Cancel follower δεν ακυρώνει leader. Single-flight συνενώνει ίδιο target/requester key μέσα σε ένα JVM, όχι μεταξύ pods.

Η cache κρατά μόνο `Valid` και περιορίζεται σε TTL και μέγεθος. Redis adapter: thread-safe, σύντομο timeout, versioned namespace, metrics και πλήρης serialization. Read exception επιστρέφει `CACHE_ERROR` χωρίς VIES fallback για αποτροπή stampede· write exception δεν διαγράφει επιβεβαιωμένο `Valid`. Το local retry είναι έως πέντε προσπάθειες με exponential backoff και jitter μόνο για προσωρινά network/VIES errors. Σε κλίμακα χρησιμοποιήστε durable delayed queue, idempotency key, max attempts και DLQ.

Το εξωτερικό JSON είναι μη έμπιστο. Απόφαση απαιτεί root object, πραγματικό boolean `isValid` ή `valid`, έγκυρο ISO-8601 `requestDate` και χωρίς overriding `userError`. Παραβίαση επιστρέφει `MALFORMED_RESPONSE`, χωρίς επινοημένο timestamp ή `Invalid`. Production χρησιμοποιεί το επίσημο HTTPS endpoint, το `baseUrl` δεν προέρχεται από user input και τα VAT, όνομα, διεύθυνση και requester αποκρύπτονται στα logs.

Η αρχιτεκτονική εκατομμυρίων είναι API ingress → durable partitioned queue → bounded consumer window → worker JVMs → shared Redis → distributed rate limiter → VIES → delayed retry/DLQ. Κάθε pod έχει τοπικά semaphore, cache και single-flight, επομένως απαιτείται global limiter. Το autoscaling ακολουθεί την ηλικία queue. Τα virtual threads μειώνουν το κόστος αναμονής αλλά δεν αυξάνουν upstream capacity.

Μετρήστε results ανά type και `errorCode`, p50/p95/p99 latency, cache hit, `CACHE_ERROR`, `CLIENT_OVERLOADED`, retry outcomes, queue depth/age, DLQ, errors ανά χώρα, heap, GC, CPU, virtual threads και sockets. Το liveness δεν εξαρτάται από VIES· το `availability()` είναι σπάνια cached διάγνωση.

Πριν από release εκτελέστε `./mvnw --batch-mode clean verify`. Τα 44 unit tests ελέγχουν format, requester, mapping, errors, availability, JSON, TTL cache και builder. Τα 24 τοπικά HTTP/concurrency tests ελέγχουν retry, limits, cancellation, close races, custom executor, sync↔async single-flight, cache anti-stampede και fatal `Error`. Το live VIES δεν είναι CI gate ή load target. Load μόνο σε mock/staging και soak/chaos 30–60 λεπτά με bounded heap και JFR· οι πόροι πρέπει να φτάσουν σε plateau.
