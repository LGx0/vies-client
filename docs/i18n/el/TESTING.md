# Ελληνικά (el) — Δοκιμές

**Languages:** [English](../../TESTING.md) · [Ελληνικά](TESTING.md) · [Όλες οι γλώσσες](../../LANGUAGES.md)

> Ενημερωτική μετάφραση. Σε περίπτωση διαφοράς υπερισχύει το αγγλικό τεχνικό και νομικό πρωτότυπο. Μόνο τα ριζικά LICENSE και NOTICE είναι νομικά έγκυρα· η μετάφραση δεν αποτελεί άδεια. [LICENSE](../../../LICENSE), [NOTICE](../../../NOTICE).

Ένα unit test απομονώνει έναν κανόνα χωρίς δίκτυο, βάση δεδομένων ή live VIES. Τα retry, timeout, single-flight, cancel και `close()` απαιτούν επιπλέον τοπικά deterministic concurrency tests.

```bash
./mvnw test
./mvnw -Dtest=VatFormatTest,ViesRequesterTest,ViesResponseMappingTest,ViesErrorTest,ViesAvailabilityTest,ViesClientBuilderTest,MiniJsonTest,TtlCacheTest test
./mvnw -Dtest=ViesClientHttpTest test
./mvnw clean verify
```

Συνολικά υπάρχουν 73 tests: 44 unit σε 8 κλάσεις και 29 τοπικά HTTP/integration/concurrency, χωρίς υποχρεωτική εξωτερική σύνδεση.

Κατάλογος unit: `VatFormat` 8, `Requester` 4, `ResponseMapping` 11, `Error` 6, `Availability` 2, `MiniJson` 4, `TtlCache` 6, `Builder` 3. Η integration suite καλύπτει retry, same-key one HTTP, sync/async/network limits, cancellation recovery, close races/deadlock, cache anti-stampede/write, custom executor interrupt/reject, ισότητα leader/follower, sync↔async, chained calls και fatal `Error`.

Το live VIES δεν είναι CI gate: opt-in έως μία κλήση availability και έναν έλεγχο, concurrency=1 και retries=0. Load tests μόνο σε mock/staging. Soak/chaos για 30–60 λεπτά με bounded heap, JFR, restarts και failures· οι πόροι πρέπει να φτάνουν σε plateau.

CI: `./mvnw --batch-mode clean verify`. Κάθε bug λαμβάνει deterministic regression test· χρησιμοποιήστε latch/barrier και όχι `sleep` ως correctness oracle.

## Πλήρης κατάλογος και ερμηνεία δοκιμών

Το unit test απομονώνει τον κανόνα χωρίς δίκτυο. Τα τοπικά HTTP/concurrency tests χρησιμοποιούν τυχαία loopback port και deterministic latch/barrier· το public VIES δεν καλείται στη default suite.

| Test class | Count | Scope |
|---|---:|---|
| `VatFormatTest` | 8 | normalization, separators, GR→EL, 28 country formats |
| `ViesRequesterTest` | 4 | canonical requester and fail-fast validation |
| `ViesResponseMappingTest` | 11 | GET/POST, strict boolean and timestamp mapping |
| `ViesErrorTest` | 6 | HU/EN catalog and retry classification |
| `ViesAvailabilityTest` | 2 | defensive immutable snapshot |
| `MiniJsonTest` | 4 | JSON values, escapes, malformed fail-closed |
| `TtlCacheTest` | 6 | TTL, bound, eviction, 32 virtual threads |
| `ViesClientBuilderTest` | 3 | URL/limit/retry/duration/overflow validation |
| **Unit total** | **44** | no external network |

### `ViesClientHttpTest` — 29 local tests

1. 503 retries then success; same-key callers produce exactly one HTTP call.
2. Active HTTP, pending async and pending sync bounds; admission timeout.
3. Cancellation restores capacity; executor rejection releases permits and in-flight state.
4. close from callback does not deadlock; blocked cache sync/async races return CLIENT_CLOSED.
5. Custom executor running and queued tasks are interrupted/cancelled but external executor stays open.
6. Sync leader/follower receive the same result; same-key followers consume no extra permits.
7. Both sync→async and async→sync single-flight directions produce one HTTP call.
8. Cache leadership recheck avoids HTTP; read failure returns CACHE_ERROR; write/close race stays consistent.
9. Chained same/unique async calls release pending capacity before callbacks.
10. Blocking callback cannot hold lifecycle shutdown; fatal Error reaches future and uncaught handler.

### Commands

```bash
./mvnw test
./mvnw -Dtest=VatFormatTest,ViesRequesterTest,ViesResponseMappingTest,ViesErrorTest,ViesAvailabilityTest,ViesClientBuilderTest,MiniJsonTest,TtlCacheTest test
./mvnw -Dtest=ViesClientHttpTest test
./mvnw -Dtest=ViesClientHttpTest#cancellationDoesNotLeakAsyncCapacity test
./mvnw --batch-mode clean verify
```

Live VIES is opt-in only: at most one availability and one check, concurrency=1, retries=0, never private requester data. Load tests target local mock/owned staging, never public VIES. Soak/chaos: 30–60 min, bounded heap, JFR, repeated lifecycle, latency/reset/cache failures/cancellation; heap, threads, in-flight and sockets must plateau.

```bash
JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=target/vies-soak.jfr,settings=profile" ./mvnw test
```

Every fixed bug requires a deterministic regression test. Use latch/barrier for races; fixed Thread.sleep is not a correctness oracle.

## Εκτεταμένος οδηγός production

Μία instance `ViesClient` μοιράζεται από όλη την εφαρμογή ή worker. Έτσι επαναχρησιμοποιούνται JDK `HttpClient`, connection pool, TTL cache, single-flight και admission limits. Client ανά request χάνει αυτές τις προστασίες. Η instance είναι thread-safe και κλείνει με `close()`, `try`-with-resources ή Spring `@Bean(destroyMethod = "close")`. Μετά το κλείσιμο νέες sync/async κλήσεις ρίχνουν `IllegalStateException` και οι κοινές εργασίες ολοκληρώνονται με `CLIENT_CLOSED`.

`Valid` είναι επιβεβαίωση VIES, `Invalid` μη επιβεβαίωση, `MalformedInput` είσοδος που δεν αποστέλλεται και `Unavailable` απουσία απόφασης. Το `Unavailable` δεν γίνεται ποτέ `Invalid`. Για λογική, metrics και retry χρησιμοποιούνται `error.code()` και `error.retryable()`, ενώ προς τον χρήστη `messageHu` και `messageEn`. Ο έλεγχος μορφής δεν είναι επικύρωση VIES.

Το `VatFormat` αφαιρεί επιτρεπτά διαχωριστικά, κεφαλαιοποιεί και ελέγχει το σχήμα χώρας πριν από το δίκτυο. Το `GR` κανονικοποιείται σε `EL` και η Βόρεια Ιρλανδία χρησιμοποιεί `XI`. Το `defaultRequester` είναι ο αριθμός ΦΠΑ της οργάνωσης. Τα `consultationNumber` και `requestDate` τεκμηριώνουν συγκεκριμένη διαβούλευση. Σε cache hit ανήκουν στον αρχικό έλεγχο· για νέο αποδεικτικό χρησιμοποιήστε `fromCache`, μικρότερο TTL ή `disableCache()`.

Το `maxPendingSyncRequests` περιορίζει sync callers, το `maxPendingAsyncRequests` μοναδικούς async leaders, το `maxConcurrentRequests` πραγματικά HTTP requests και το `admissionTimeout` την αναμονή slot. Υπέρβαση επιστρέφει `CLIENT_OVERLOADED`. Η async εκτέλεση χρησιμοποιεί virtual threads. Caller-provided executor ανήκει στην εφαρμογή. Cancel follower δεν ακυρώνει leader. Single-flight συνενώνει ίδιο target/requester key μέσα σε ένα JVM, όχι μεταξύ pods.

Η cache κρατά μόνο `Valid` και περιορίζεται σε TTL και μέγεθος. Redis adapter: thread-safe, σύντομο timeout, versioned namespace, metrics και πλήρης serialization. Read exception επιστρέφει `CACHE_ERROR` χωρίς VIES fallback για αποτροπή stampede· write exception δεν διαγράφει επιβεβαιωμένο `Valid`. Το local retry είναι έως πέντε προσπάθειες με exponential backoff και jitter μόνο για προσωρινά network/VIES errors. Σε κλίμακα χρησιμοποιήστε durable delayed queue, idempotency key, max attempts και DLQ.

Το εξωτερικό JSON είναι μη έμπιστο. Απόφαση απαιτεί root object, πραγματικό boolean `isValid` ή `valid`, έγκυρο ISO-8601 `requestDate` και χωρίς overriding `userError`. Παραβίαση επιστρέφει `MALFORMED_RESPONSE`, χωρίς επινοημένο timestamp ή `Invalid`. Production χρησιμοποιεί το επίσημο HTTPS endpoint, το `baseUrl` δεν προέρχεται από user input και τα VAT, όνομα, διεύθυνση και requester αποκρύπτονται στα logs.

Η αρχιτεκτονική εκατομμυρίων είναι API ingress → durable partitioned queue → bounded consumer window → worker JVMs → shared Redis → distributed rate limiter → VIES → delayed retry/DLQ. Κάθε pod έχει τοπικά semaphore, cache και single-flight, επομένως απαιτείται global limiter. Το autoscaling ακολουθεί την ηλικία queue. Τα virtual threads μειώνουν το κόστος αναμονής αλλά δεν αυξάνουν upstream capacity.

Μετρήστε results ανά type και `errorCode`, p50/p95/p99 latency, cache hit, `CACHE_ERROR`, `CLIENT_OVERLOADED`, retry outcomes, queue depth/age, DLQ, errors ανά χώρα, heap, GC, CPU, virtual threads και sockets. Το liveness δεν εξαρτάται από VIES· το `availability()` είναι σπάνια cached διάγνωση.

Πριν από release εκτελέστε `./mvnw --batch-mode clean verify`. Τα 44 unit tests ελέγχουν format, requester, mapping, errors, availability, JSON, TTL cache και builder. Τα 29 τοπικά HTTP/concurrency tests ελέγχουν retry, limits, cancellation, close races, custom executor, sync↔async single-flight, cache anti-stampede και fatal `Error`. Το live VIES δεν είναι CI gate ή load target. Load μόνο σε mock/staging και soak/chaos 30–60 λεπτά με bounded heap και JFR· οι πόροι πρέπει να φτάσουν σε plateau.

### Κριτήρια αποδοχής

Μια αλλαγή γίνεται αποδεκτή μόνο αν όλα τα permits και οι εγγραφές `inFlight` ελευθερώνονται σε rejection, cancellation, interruption και cache exception. Leader και followers λαμβάνουν ίδιο αποτέλεσμα κατά το `close()` και user callback δεν κρατά lifecycle lock. Το test χρησιμοποιεί latch, blocking cache και ελεγχόμενο executor, ελέγχει τον ακριβή αριθμό HTTP requests, την αποκατάσταση pending capacity και την απουσία υπολειπόμενων threads ή sockets. Το pull request τεκμηριώνει public behavior, compatibility, security impact και αναπαραγώγιμη performance μεθοδολογία. Ο δημιουργός επιβεβαιώνει δικαίωμα υποβολής και ελέγχει κάθε AI-generated γραμμή, provenance και άδεια. Νέα dependency απαιτεί ενημέρωση third-party notices και αιτιολόγηση αλλαγής του zero-runtime-dependency στόχου.
