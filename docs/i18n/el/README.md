# Ελληνικά (el) — vies-client — έλεγχος αριθμού ΦΠΑ

Το `disableCache()` απενεργοποιεί την αποθηκευμένη/μόνιμη cache, αλλά ταυτόχρονες κλήσεις με το ίδιο ζεύγος ΑΦΜ + requester μπορούν να μοιραστούν ένα single-flight αίτημα δικτύου. Μια μεταγενέστερη κλήση μετά την ολοκλήρωσή του εκτελεί νέο αίτημα VIES. Το `consultationNumber` είναι προαιρετικό και μπορεί να επιστραφεί από το VIES, αλλά δεν είναι ποτέ εγγυημένο· η αποδεικτική του αξία εξαρτάται από τους τοπικούς κανόνες. Φορτώνετε το `MY_EU_VAT_NUMBER` μόνο από αξιόπιστη πηγή secret/config.

**Όροι αναζήτησης:** έλεγχος αριθμού ΦΠΑ, επικύρωση αριθμού ΦΠΑ, επικύρωση ΦΠΑ ΕΕ, έλεγχος φορολογικού αναγνωριστικού, πελάτης VIES Java; `VAT checker`, `VAT number validator`, `EU VAT validation`, `tax ID checker`.

Δεν είναι γενικός φορολογικός υπολογιστής, αλλά client για την επικύρωση αναγνωριστικών ΦΠΑ της ΕΕ μέσω VIES.

**Languages:** [English](../../../README.md) · [Ελληνικά](README.md) · [Όλες οι γλώσσες](../../LANGUAGES.md)

> Ενημερωτική μετάφραση. Σε περίπτωση διαφοράς υπερισχύει το αγγλικό τεχνικό και νομικό πρωτότυπο. Μόνο τα ριζικά LICENSE και NOTICE είναι νομικά έγκυρα· η μετάφραση δεν αποτελεί άδεια. [LICENSE](../../../LICENSE), [NOTICE](../../../NOTICE).

Αυτόνομος πελάτης Java 21+ χωρίς εξαρτήσεις χρόνου εκτέλεσης για το REST API VIES της Ευρωπαϊκής Επιτροπής. Υποστηρίζει Spring Boot, Quarkus, Micronaut και JDK `HttpServer`, χρησιμοποιώντας μόνο `java.net.http`.

VIES: <https://ec.europa.eu/taxation_customs/vies/#/technical-information>

## Τεκμηρίωση

[Εγκατάσταση](INSTALLATION.md) · [Ενσωμάτωση](INTEGRATION.md) · [Τεχνική τεκμηρίωση](TECHNICAL.md) · [Δοκιμές](TESTING.md) · [Έργο ανοικτού κώδικα](OPEN_SOURCE.md) · [Εκδόσεις](RELEASING.md)

```bash
./mvnw clean verify
./mvnw install
```

```xml
<dependency><groupId>vies.client</groupId><artifactId>vies-client</artifactId><version>1.0.0</version></dependency>
```

JPMS: `requires vies.client;`· λειτουργεί και στο classpath.

```java
try (var vies = ViesClient.builder().defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER"))).retries(1).build()) {
  ViesResponse result = vies.check("DE000000000");
}
```

Το `defaultRequester` είναι ο δικός σας αριθμός ΦΠΑ. Σε έγκυρο έλεγχο το VIES μπορεί να επιστρέψει προαιρετικό `consultationNumber`· η αποδεικτική του αξία εξαρτάται από τους τοπικούς κανόνες, ενώ ένα cache hit διατηρεί τα στοιχεία της αρχικής διαβούλευσης.

Το `checkAsync` χρησιμοποιεί virtual threads. Μοιραστείτε ένα thread-safe singleton και καλέστε `close()`. Τα pending/network limits παρέχουν backpressure και το single-flight συνενώνει όμοια αιτήματα.

Για εκατομμύρια εργασίες χρησιμοποιήστε durable partitioned queue, bounded workers, κοινό Redis, distributed rate limiter, delayed retry, idempotency και DLQ. Τα virtual threads δεν αυξάνουν τη χωρητικότητα VIES· το `Unavailable` δεν είναι ποτέ `Invalid`.

Τα αποτελέσματα είναι `Valid`, `Invalid`, `Unavailable`, `MalformedInput`. Το `error()` δίνει σταθερό κωδικό, `messageHu`, `messageEn`, `retryable`. HTTP: 200/400/429/503. Η cache κρατά μόνο `Valid`· αποτυχία ανάγνωσης δίνει `CACHE_ERROR`. Η Ελλάδα χρησιμοποιεί `EL`, η Βόρεια Ιρλανδία `XI`.

Δοκιμή: `./mvnw test`. Η μοναδική άδεια είναι Apache License 2.0. Το έργο δεν είναι επίσημο προϊόν της ΕΕ.

## Πλήρης διαμόρφωση και λειτουργική σημασιολογία

Διαδρομή αιτήματος: offline normalization → cache → JVM-local single-flight → bounded admission → shared HTTP client → strict validation → εγγραφή μόνο επιβεβαιωμένου Valid.

| Builder option | Default | Operational role |
|---|---:|---|
| `baseUrl(String)` | official VIES REST | test/mock override only |
| `connectTimeout(Duration)` | 5 s | TCP/TLS connection bound |
| `requestTimeout(Duration)` | 8 s | complete request bound |
| `admissionTimeout(Duration)` | 2 s | maximum wait for network slot |
| `defaultRequester(ViesRequester)` | none | own VAT, consultation identifier |
| `retries(int)` | 0 | 0–5 transient retries, exponential backoff+jitter |
| `retryDelay(Duration)` | 400 ms | backoff base |
| `maxConcurrentRequests(int)` | 32 | active VIES HTTP calls per instance |
| `maxPendingSyncRequests(int)` | 512 | bounded sync callers; overflow → CLIENT_OVERLOADED |
| `maxPendingAsyncRequests(int)` | 512 | bounded unique async leaders |
| `cacheTtl(Duration)` | 24 h | Valid result lifetime |
| `cacheMaxEntries(int)` | 10,000 | in-memory cache bound |
| `cache(ViesCache)` | built-in | Redis/other shared cache extension |
| `disableCache()` | — | Χωρίς αποθηκευμένη cache· ίδιες ταυτόχρονες κλήσεις μπορούν να μοιραστούν single-flight |
| `userAgent(String)` | module ID | identify the integration |
| `executor(ExecutorService)` | virtual thread/task | caller owns custom executor lifecycle |

Unavailable σημαίνει ότι δεν λήφθηκε απόφαση και δεν είναι Invalid. Ο format check δεν είναι VIES verification. Το retry αυξάνει το φορτίο· λίγες local προσπάθειες και delayed queue σε κλίμακα. Cached consultationNumber/requestDate ανήκει στον αρχικό έλεγχο.

| Result | HTTP | Retry | Cache |
|---|---:|---:|---:|
| `Valid` | 200 | no | yes |
| `Invalid` | 200 | no | no |
| `MalformedInput` | 400 | no | no |
| `Unavailable(CLIENT_OVERLOADED)` | 429 | delayed | no |
| other `Unavailable` | 503 | by `error.retryable()` | no |

## Spring Boot, αποκρίσεις σφαλμάτων και αρχιτεκτονική κλίμακας

Ο client είναι immutable και thread-safe. Δημιουργήστε ένα singleton bean για όλη τη ζωή της εφαρμογής και κλείστε το στο shutdown. Χειριστείτε ρητά και τα τέσσερα αποτελέσματα· για λογική χρησιμοποιήστε το σταθερό errorCode και προς τον χρήστη τα messageHu/messageEn.

```java
@Configuration
class ViesConfig {
  @Bean(destroyMethod = "close")
  ViesClient viesClient() {
    return ViesClient.builder()
      .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
      .maxConcurrentRequests(32)
.maxPendingSyncRequests(512)
.maxPendingAsyncRequests(512)
.retries(1).build();
  }
}
```

```java
return switch (vies.check(number)) {
 case ViesResponse.Valid v -> ResponseEntity.ok(Map.of("valid", true));
 case ViesResponse.Invalid i -> ResponseEntity.ok(Map.of("valid", false));
 case ViesResponse.MalformedInput m -> problem(400, m);
 case ViesResponse.Unavailable u -> problem(
   "CLIENT_OVERLOADED".equals(u.errorCode()) ? 429 : 503, u);
};
```

Τοπολογία εκατομμυρίων: API ingress → durable partitioned queue → bounded consumer window → worker JVMs → shared Redis → distributed/global rate limiter → VIES. Τα retryable αποτελέσματα πηγαίνουν σε delayed queue και μετά τα max attempts σε DLQ. Τα per-JVM semaphore και single-flight δεν είναι global.

A durable queue consumer must keep a bounded active window. Autoscaling should follow queue age, not unlimited direct VIES concurrency. Liveness must not depend on VIES; availability() should be sparse and cached.

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
