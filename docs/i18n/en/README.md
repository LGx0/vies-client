# English (en) — vies-client — EU VAT number checker

`disableCache()` disables stored/persisted caching, but concurrent calls with the same VAT + requester pair may share one single-flight network request. A later call after that request completes performs a new VIES request. `consultationNumber` is optional and may be returned by VIES, but is never guaranteed; its legal evidentiary value depends on local rules. Load `MY_EU_VAT_NUMBER` only from a trusted secret/configuration source.

**Search terms:** VAT checker, VAT number validator, EU VAT validation, tax ID checker, VIES Java client; `VAT checker`, `VAT number validator`, `EU VAT validation`, `tax ID checker`.

This is not a general tax calculator; it is a client for validating EU VAT identification numbers through VIES.

> [Language selector](../../LANGUAGES.md) · This localization is provided for accessibility. If it differs from the canonical English technical or legal source, the English source governs. The root `LICENSE` and `NOTICE` remain legally authoritative and are not replaced by translations.

[![License: Apache-2.0](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](../../../LICENSE)
[![Java 21+](https://img.shields.io/badge/Java-21%2B-orange.svg)](../../../pom.xml)

🌍 **[All EU official languages](../../LANGUAGES.md)**

`vies-client` is a standalone Java client for the European Commission's VIES
(VAT Information Exchange System) VAT-number validation REST API. It has **no
third-party runtime dependencies** and uses only the JDK HTTP client
(`java.net.http`). It can be integrated into Spring Boot, Quarkus, Micronaut, a
plain JDK `HttpServer`, or any other Java application.

This is an independent open-source project. It is not an official product of,
endorsed by, or certified by the European Commission, the European Union, or any
member-state tax authority.

- **Requirement:** Java **21+** bytecode/API; the entire package is verified on JDK 21.
- **Official VIES documentation:** <https://ec.europa.eu/taxation_customs/vies/#/technical-information>
- **Validation endpoint:** `https://ec.europa.eu/taxation_customs/vies/rest-api/ms/{countryCode}/vat/{vatNumber}`
- **Member-state availability endpoint:** `https://ec.europa.eu/taxation_customs/vies/rest-api/check-status`

## Documentation

- [Installation](INSTALLATION.md)
- [Integration](INTEGRATION.md)
- [Technical design](TECHNICAL.md)
- [Testing](TESTING.md)
- [Open-source and licensing notes](OPEN_SOURCE.md)
- [Release guide](RELEASING.md)
- [GitHub publication guide](GITHUB_SETUP.md)
- [Contributing](CONTRIBUTING.md)
- [Security policy](SECURITY.md)
- [Code of Conduct](CODE_OF_CONDUCT.md)
- [Support and donations](SUPPORT.md)
- [Third-party notices](THIRD_PARTY_NOTICES.md)
- [Changelog](CHANGELOG.md)

## Build and integration

```bash
./mvnw install    # tests + target/vies-client-1.2.0.jar (+ -sources.jar, -javadoc.jar)
               # and installation into the local Maven repository
```

**Maven:**

```xml
<dependency>
    <groupId>vies.client</groupId>
    <artifactId>vies-client</artifactId>
    <version>1.2.0</version>
</dependency>
```

**Gradle:**

```kotlin
implementation("vies.client:vies-client:1.2.0")
```

**Named JPMS module.** The JAR exposes the following module descriptor
(`jar --describe-module`):

```
vies.client@1.2.0
exports vies.client
requires java.net.http
contains vies.client.internal   ← non-exported internal package
```

Require it from a modular application as follows:

```java
module my.api.server {
    requires vies.client;
}
```

It also works normally on the classpath in a non-modular application; the module
descriptor is simply ignored. Without Maven or Gradle, either add the JAR directly
or copy the source tree under `src/main/java`. Omit `module-info.java` when the
consuming project is not modularized.

## Quick example

```java
import vies.client.*;

try (var vies = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER"))) // trusted secret/config
        .retries(1)
        .build()) {

    // Accepts hyphens, spaces and lowercase; maps GR to EL.
    switch (vies.check("DE 000 000 000")) {
        case ViesResponse.Valid v ->
            System.out.println("Valid: " + v.traderName().orElse("(name not public)")
                    + " — consultation number: " + v.consultationNumber().orElse("-"));
        case ViesResponse.Invalid i ->
            System.out.println("Not a valid VAT number: " + i.vatNumber());
        case ViesResponse.Unavailable u ->
            System.out.println("VIES is currently unavailable (" + u.errorCode() + "), retry later");
        case ViesResponse.MalformedInput m ->
            System.out.println("Malformed input: " + m.reason());
    }
}
```

The pattern-matching `switch` is exhaustive because `ViesResponse` is a sealed
interface. The compiler therefore requires all four outcomes to be handled.

## Why is `defaultRequester` important?

Configure your own EU VAT number as the requester when you need an official
consultation identifier. For a valid result, VIES may return a
`consultationNumber` that can help demonstrate that the partner was checked before
invoicing. A consultation identifier read from cache belongs to the original
check; it is not evidence of a new check.

## Spring Boot integration

```java
@Configuration
class ViesConfig {
    @Bean(destroyMethod = "close")
    ViesClient viesClient() {
        return ViesClient.builder()
                .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
                .retries(1)
                .build();
    }
}

@RestController
@RequestMapping("/api/vat")
class VatController {
    private final ViesClient vies;

    VatController(ViesClient vies) { this.vies = vies; }

    @GetMapping("/{number}")
    ResponseEntity<?> check(@PathVariable String number) {
        return switch (vies.check(number)) {
            case ViesResponse.Valid v -> ResponseEntity.ok(Map.of(
                    "valid", true,
                    "name", v.traderName().orElse(""),
                    "address", v.traderAddress().orElse(""),
                    "consultationNumber", v.consultationNumber().orElse("")));
            case ViesResponse.Invalid i -> ResponseEntity.ok(Map.of("valid", false));
            case ViesResponse.Unavailable u -> {
                var error = u.error().orElseThrow();
                yield ResponseEntity.status("CLIENT_OVERLOADED".equals(error.code()) ? 429 : 503)
                        .body(Map.of("errorCode", error.code(), "retryable", error.retryable(),
                                "messageHu", error.messageHu(), "messageEn", error.messageEn()));
            }
            case ViesResponse.MalformedInput m -> {
                var error = m.error().orElseThrow();
                yield ResponseEntity.badRequest().body(Map.of(
                        "errorCode", error.code(), "retryable", error.retryable(),
                        "messageHu", error.messageHu(), "messageEn", error.messageEn()));
            }
        };
    }
}
```

The client is immutable and thread-safe — the application creates a single instance
(singleton bean) and close it on shutdown (`close`).
Framework-free example: [`examples/ViesDemoServer.java`](../../../examples/ViesDemoServer.java) (plain JDK`HttpServer`, on virtual threads):

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java     # port 8085
curl "http://localhost:8085/vat-check?number=HU00000000"
```

## Asynchronous use

```java
CompletableFuture<ViesResponse> future = vies.checkAsync("PL0000000000");
```

Asynchronous calls run on virtual threads by default, so blocking network waits do
not consume a platform thread per request. A custom executor can be supplied with
`executor(...)`; its lifecycle remains the caller's responsibility.

Each client accepts at most 512 unique asynchronous leader operations by default.
A unique cache read briefly consumes one slot, while malformed input and followers
of an existing single-flight operation do not consume additional slots. When the
limit is reached, a new unique request immediately returns
`Unavailable(..., "CLIENT_OVERLOADED")`. The application must also bound its input
queue and the number of futures it retains.

## Operating at million-request scale

For millions of users, don't start millions of futures in one JVM, and don't
allow unlimited direct traffic to VIES. The proposed structure:

> The client can be one component of a million-item processing pipeline, but actual
> VIES throughput is constrained by variable, non-guaranteed EU and member-state
> limits. Virtual threads reduce the cost of waiting; they do not increase upstream
> capacity.

1. Incoming checks are received by a persistent, partitioned message queue.
2. Horizontally scaled workers consume bounded batches from that queue.
3. Each worker process shares one singleton `ViesClient`.
4. Workers use a shared Redis cache through the `ViesCache` interface.
5. A distributed rate limiter protects the EU and member-state VIES endpoints.

Within one JVM, concurrent requests for the same VAT/requester pair are coalesced
through single-flight, preventing a cache miss from causing a request stampede.
`maxConcurrentRequests` bounds real HTTP calls, while
`maxPendingAsyncRequests` and `maxPendingSyncRequests` provide memory
backpressure. These protections are JVM-local; aggregate traffic across workers
must use a shared limiter, and the durable queue and consumer pool must remain
bounded by the application.

Example worker configuration:

```java
var vies = ViesClient.builder()
        .cache(redisViesCache)
        .cacheTtl(Duration.ofHours(24))
        .maxConcurrentRequests(32)
        .maxPendingSyncRequests(512)
        .maxPendingAsyncRequests(512)
        .admissionTimeout(Duration.ofSeconds(2))
        .retries(2)
        .retryDelay(Duration.ofMillis(250))
        .build();
```

Persist and retry `Unavailable` outcomes—including `CLIENT_OVERLOADED`—through
the queue's delayed-retry mechanism. Do not retry `MalformedInput` or `Invalid`
without changing the input or business state.

## Request lifecycle

1. **Normalize:** validate and canonicalize the country code and VAT shape locally.
2. **Cache:** return an unexpired valid result immediately.
3. **Single-flight:** coalesce identical VAT/requester pairs within one JVM.
4. **Admission:** async and network limits protect memory and VIES.
5. **HTTP:** reused JDK `HttpClient` connection with short timeout.
6. **Validate:** a missing/invalid boolean or audit date becomes `MALFORMED_RESPONSE`.
7. **Cache write:** cache only an authoritative `Valid` result.

## Bilingual error responses

Machine error codes are stable and language-independent. `error()` exposes
Hungarian and English user-facing messages plus a retry recommendation:

```java
var response = vies.check("HU00000000");
response.error().ifPresent(error -> {
    log.warn("{} | {} | retry={}", error.messageHu(), error.messageEn(), error.retryable());
});
```

| Result           |       HTTP |          Retry |   Cache | Meaning                                                       |
| ---------------- | ---------: | -------------: | ------: | ------------------------------------------------------------- |
| `Valid`          |        200 |             no | yes/yes | VIES confirmed as valid / VIES confirmed valid                |
| `Invalid`        |        200 |             no |      no | VIES did not confirm it as valid / VIES did not confirm valid |
| `Unavailable`    | 503 or 429 | mostly/usually |      no | No validity decision was made                                 |
| `MalformedInput` |        400 |             no |      no | The input must be corrected / Input must be corrected         |

## Configuration (builder)

| Setting                           | Default                | Purpose                                                                        |
| --------------------------------- | ---------------------- | ------------------------------------------------------------------------------ |
| `baseUrl(String)`                 | official VIES REST URL | Redirection in mock test                                                       |
| `connectTimeout(Duration)`        | 5 s                    | TCP/TLS connection timeout                                                     |
| `requestTimeout(Duration)`        | 8 s                    | Total request timeout (VIES is interactive, keep it short)                     |
| `admissionTimeout(Duration)`      | 2 s                    | This is how long it waits for free network space                               |
| `defaultRequester(ViesRequester)` | none                   | Requester VAT number → consultation ID                                         |
| `retries(int)`                    | 0                      | Automatic retry on transient errors (0-5, exponential backoff + jitter)        |
| `retryDelay(Duration)`            | 400 ms                 | Exponential backoff default                                                    |
| `maxConcurrentRequests(int)`      | 32                     | Upper limit of concurrent real VIES network requests                           |
| `maxPendingSyncRequests(int)`     | 512                    | Memory limit for simultaneous sync callers; above it `CLIENT_OVERLOADED`       |
| `maxPendingAsyncRequests(int)`    | 512                    | Memory limit for active/pending async operations; above it `CLIENT_OVERLOADED` |
| `cacheTtl(Duration)`              | 24 hours               | Cache time for valid hits                                                      |
| `cacheMaxEntries(int)`            | 10,000                 | Built-in memory cache size limit                                               |
| `cache(ViesCache)`                | built in               | Custom cache backend (e.g. Redis)                                              |
| `disableCache()`                  | —                      | No stored cache; concurrent identical calls may share single-flight            |
| `userAgent(String)`               | module-id              | You should identify yourself to the EU                                         |
| `executor(ExecutorService)`       | virtual thread/task    | Custom async executor; the caller is responsible for its life cycle            |

## Custom cache (for example, Redis)

```java
class RedisViesCache implements ViesCache {
    public Optional<ViesResponse.Valid> get(String key) { /* ... */ }
    public void put(String key, ViesResponse.Valid value, Duration ttl) { /* ... */ }
}

var vies = ViesClient.builder().cache(new RedisViesCache()).build();
```

Only `Valid` results are cached; `Invalid` and transient failures are never cached.
A Redis adapter should use short command timeouts, a versioned namespace, and
dedicated error metrics. A cache-read exception returns `CACHE_ERROR` instead of
falling through to VIES, which prevents an uncontrolled request stampede during a
Redis outage.

Cached `consultationNumber` and `requestDate` values describe the original
consultation. A cache hit is not a new VIES check. When fresh evidence is required,
inspect `fromCache`, use a suitably short TTL, or call with caching disabled.

## Semantics worth knowing

1. **`Unavailable` is not `Invalid`.** Member-state systems may return
   `MS_UNAVAILABLE`, `MS_MAX_CONCURRENT_REQ`, and similar errors. VIES made no
   validity decision, so the VAT number must not be treated as invalid.
2. **Format validation is not VIES verification.** Local checks reject clearly
   malformed input before network access, but only a VIES response establishes
   validity.
3. **Retries add load.** Keep local retry counts small. At scale, durable delayed
   retry is the primary recovery mechanism.
4. **Greece uses `EL`; Northern Ireland uses `XI`.** Input prefix `GR` is
   automatically normalized to `EL`.

## Tests

```bash
./mvnw test    # unit + local HTTP/concurrency/retry/backpressure/lifecycle tests
```

---

## English quickstart

Zero-dependency Java 21+ client for the EU Commission's VIES VAT-number
validation REST API. Build with `./mvnw package`, then:

```java
try (var vies = ViesClient.createDefault()) {
    if (vies.check("DE000000000") instanceof ViesResponse.Valid v) {
        System.out.println(v.traderName().orElse("?"));
    }
}
```

Results are a sealed hierarchy (`Valid`/`Invalid`/`Unavailable`/`MalformedInput`) designed for exhaustive pattern-matching`switch`. Valid
results are cached in-memory for 24 h; transient VIES outages are reported as `Unavailable`, never as`Invalid`. Official API documentation:
<https://ec.europa.eu/taxation_customs/vies/#/technical-information>.

## High-scale operation

This client can be one component of a million-item processing pipeline, but
actual VIES throughput is bounded by variable, non-guaranteed EU and member-state
limits. Virtual threads reduce blocking cost; they do not increase upstream
capacity. Use a durable partitioned queue, bounded consumers, a shared Redis
cache, and a distributed rate limiter across all workers. Local single-flight and
semaphores protect one JVM only. Never treat `Unavailable` as`Invalid`; use`response.error()` for stable codes, retryability, and Hungarian/English messages.

## Open source

The Apache-2.0 license permits the project to be used, modified, and
can be distributed. The license permits commercial use and express
grants a patent license; license, attribution and modification terms must be read
to guard. Before contributing, read [CONTRIBUTING.md](CONTRIBUTING.md) and
[CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) files.
This project is licensed under the [Apache License 2.0](../../../LICENSE), a permissive
license with an explicit patent grant. See [CONTRIBUTING.md](CONTRIBUTING.md),
[SECURITY.md](SECURITY.md), and the detailed [open-source notes](OPEN_SOURCE.md).

## Support and donations

Community support is provided on a best-effort basis, via GitHub issues and discussions.
Always report security errors on a private channel. Project maintenance is significant
involves development and infrastructure costs; the GitHub Donate/Sponsor button is the maintainer
appears after setting your verified support URL.
Community support is best-effort through GitHub issues and discussions. Security
reports must remain private. A Donate/Sponsor button will be enabled after the
maintainer's verified funding URL is configured.
