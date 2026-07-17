# English (en) — Integration

`disableCache()` disables stored/persisted caching, but concurrent calls with the same VAT + requester pair may share one single-flight network request. A later call after that request completes performs a new VIES request. `consultationNumber` is optional and may be returned by VIES, but is never guaranteed; its legal evidentiary value depends on local rules. Load `MY_EU_VAT_NUMBER` only from a trusted secret/configuration source.

> [Language selector](../../LANGUAGES.md) · This localization is provided for accessibility. If it differs from the canonical English technical or legal source, the English source governs. The root `LICENSE` and `NOTICE` remain legally authoritative and are not replaced by translations.

## 1. Lifecycle

Use a single `ViesClient` object in an application or worker instance.
Do not create a client per HTTP request: you would lose the connection pool, cache,
single-flight merging and local limits.
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

// Application shutdown
client.close();
```

A new operation cannot be started after `close()`. Shutdown interrupts the active internal
operations, and returns `CLIENT_CLOSED` to common requests in progress.
The sync and async methods called directly afterwards are both synchronous
A `IllegalStateException` exception is thrown.
After `close()`, no new work is accepted. Shutdown interrupts active internal work
and completes shared in-flight requests with `CLIENT_CLOSED`.
New sync and async API calls made afterward both throw `IllegalStateException` synchronously.

## 2. Synchronous API

```java
ViesResponse response = client.check("DE 000 000 000");

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

The number of synchronous API callers is also limited (`maxPendingSyncRequests`). On the limit
above valid request immediately gets `CLIENT_OVERLOADED` result.
Synchronous callers are also admission-bounded by `maxPendingSyncRequests`.

## 3. Asynchronous API

```java
client.checkAsync("PL0000000000")
        .thenAccept(response -> {
            // Handle all four response variants.
        });
```

The async API runs on a virtual thread with the default executor; own executor
its scheduling rules apply. Identical tax number + inquiry requests are one
they connect to an internal shared future. The caller receives a cancel-proof copy:
the cancel of a single consumer does not interrupt the joint request of the others.
With the default executor, the async API runs on virtual threads; a custom executor
uses its own scheduling policy. Identical VAT/requester calls join one internal
shared future. Each caller receives a cancellation-safe copy.
Don't store millions of futures in memory. The persistent queue consumer is always limited
keep window active.
Do not retain millions of futures. A durable-queue consumer must keep a bounded
processing window.

## 4. HTTP API contract

Recommended mapping:
| ViesResponse | HTTP | Retry | Note |
|---|---:|---:|---|
|`Valid`| 200 | no | domain result |
|`Invalid`| 200 | no | domain result, not an HTTP failure |
|`MalformedInput`| 400 | no | caller must fix input |
|`Unavailable(CLIENT_OVERLOADED)`| 429 | delayed | local backpressure |
| other `Unavailable`| 503 | by`error.retryable()`| no validity decision |
Example error JSON / Example error JSON:

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

User text `messageHu`/`messageEn`. For log, metric and client logic
always use the stable value `errorCode`.
`messageHu`/`messageEn`are user-facing. Use stable`errorCode` values for logs,
metrics, and client logic.

## 5. Spring Boot

## Configuration

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

## Controller

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

The library has no Spring dependencies; the code above is part of the consuming application.
The library has no Spring dependencies; this adapter belongs to the consuming app.

## 6. Plain JDK HTTP server

The full executable example is `examples/ViesDemoServer.java`.
Full runnable example:`examples/ViesDemoServer.java`.

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java 8085
curl "http://localhost:8085/vat-check?number=DE000000000"
```

The example uses virtual threads, real HTTP statuses, and bilingual error responses.
The example uses virtual threads, meaningful HTTP statuses, and bilingual errors.

## 7. Requester and consultation ID

```java
var client = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
        .build();
```

The requester is your organization's own EU VAT number and must come from a trusted secret/configuration source. For a valid check, VIES may return an optional `requestIdentifier` or `consultationNumber`, but it is not guaranteed and its evidentiary value depends on local rules.

A cache hit contains the original consultation ID/date, not a new check. `disableCache()` disables stored caching, but concurrent identical VAT+requester calls may still share one single-flight network request; a later call after completion sends a new VIES request.

## 8. Redis cache adapter

```java
final class RedisViesCache implements ViesCache {
    private final RedisClient redis; // application-specific adapter

    RedisViesCache(RedisClient redis) {
        this.redis = redis;
    }

    @Override
    public Optional<ViesResponse.Valid> get(String key) {
        // Use a short, bounded Redis timeout.
        return redis.get("vies:v1:" + key).map(this::decode);
    }

    @Override
    public void put(String key, ViesResponse.Valid value, Duration ttl) {
        redis.set("vies:v1:" + key, encode(value), ttl);
    }

    // encode/decode are application-specific and must preserve every record field.
}
```

Requirements / Requirements:

- thread-safe implementation;
- short connection/command timeout;
- versioned key namespace;
- metrics and health alerts;
- no unbounded internal retry;
- correct serialization of `requestDate`, optionals, and`fromCache`.
  Read exception causes result `CACHE_ERROR` and does not trigger VIES fallback.
  A write exception does not delete the already received authentic `Valid` result.
  A read exception returns `CACHE_ERROR` without a VIES fallback. A write exception
  does not erase an authoritative `Valid` result.

## 9. Retry, queue, and DLQ

In high-scale processing:

1. persist the job before processing;
2. use an idempotency key;
3. call VIES with small local retry count;
4. if `error.retryable()`, schedule delayed retry with exponential delay;
5. stop after a configured maximum and move to DLQ/manual review;
6. never retry `Invalid` or`MalformedInput` unchanged.
   Don't keep durable retry in memory with chain `CompletableFuture`.
   Do not implement durable retries as in-memory `CompletableFuture` chains.

## 10. Multiple nodes or pods

Each replica has its own connection pool, cache, single-flight table and semaphore
get `maxConcurrentRequests(32)` is therefore **not global 32**.
Every replica has its own connection pool, cache, single-flight table, and
semaphores.`maxConcurrentRequests(32)` is therefore **not a global limit of 32**.
Required components at large scale / Required at scale:

- durable partitioned queue;
- bounded consumer count/window;
- distributed/global rate limiter;
- shared cache where business semantics allow it;
- delayed retry and DLQ;
- idempotency/deduplication;
- autoscaling based on queue age, not direct unlimited VIES concurrency.

## 11. Health and observability

- Liveness must not depend on VIES.
- `availability()` should be a rare, cached diagnostic / Poll and cache it sparingly.
- Measure p50/p95/p99 latency / Measure p50/p95/p99 latency.
- Count by result type and `errorCode`/ Count by result and error code.
- Measure cache hit, retry, overload, queue age and DLQ.
- Measure cache hits, retries, overloads, queue age, and DLQ size.
- Mask VAT/name/address in logs.

## 12. Anti-patterns

- client per request / new client per request;
- unlimited futures or sync callers
- treating a per-pod limiter as global / seeing a local limiter as global;
- retrying every error immediately
- converting `Unavailable` to`Invalid`;
- treating cached consultation data as a fresh proof;
- calling live VIES from liveness checks or default unit tests.

## 13. Production checklist

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
