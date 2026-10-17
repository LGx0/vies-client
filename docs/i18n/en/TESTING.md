# Testing

[Languages](../../LANGUAGES.md) · [Full bilingual test catalog](../../TESTING.md)

The deterministic suite contains **81 tests**: **47 unit tests** in eight classes
and **34 local HTTP/integration/concurrency tests**. It does not call live VIES.

## Run the checks

Use the same selected JDK for `java`, `javac`, and Maven:

```bash
java -version
javac -version
./mvnw -version
./mvnw --batch-mode --no-transfer-progress clean verify
```

CI runs JDK 21 and 25. Published bytecode remains Java 21. Surefire runs tests on
the classpath so the local `jdk.httpserver` fixture remains test-only.

Unit tests only:

```bash
./mvnw -Dtest=VatFormatTest,ViesRequesterTest,ViesResponseMappingTest,ViesErrorTest,ViesAvailabilityTest,ViesClientBuilderTest,MiniJsonTest,TtlCacheTest test
```

Local HTTP/concurrency only:

```bash
./mvnw -Dtest=ViesClientHttpTest test
```

## Unit-test catalog

| Class | Tests | Contract |
|---|---:|---|
| VatFormatTest | 8 | Normalization, separators, GR-to-EL mapping, invalid inputs, 28 supported codes |
| ViesRequesterTest | 4 | Full/pair requester validation and canonicalization |
| ViesResponseMappingTest | 11 | Strict types, echoed VAT, field limits, validity consistency, timestamps and outcomes |
| ViesErrorTest | 6 | Stable HU/EN errors, HTTP retry policy, safe unknown codes |
| ViesAvailabilityTest | 2 | Defensive immutable snapshot and null-map rejection |
| MiniJsonTest | 6 | JSON grammar, limits, escapes, ASCII Unicode hex, valid surrogate pairs |
| TtlCacheTest | 6 | TTL boundaries, bounded size, expiry preference, 32-thread write pressure |
| ViesClientBuilderTest | 4 | Limits/URLs/durations, including network-timeout overflow |

## HTTP, concurrency and security cases

The 34 local cases cover retry, redirect refusal, oversized/invalid-UTF-8 bodies,
single-flight, bounded network/sync/async admission, cancellation, rejected and
inline executors, mixed sync/async callers, cache leadership rechecks and errors,
close races, blocked callbacks, and fatal Error propagation.

New 1.2.1 regressions preserve an Irish requester's plus sign, reject malformed
availability schemas and duplicate country entries, retain valid partial snapshots,
and enforce deadlines when a body stalls after response headers. The stalled-body
tests verify actual timeout and reclaimed capacity rather than merely timing out
a caller's view. They use loopback fixtures and latches.

## Optional operational testing

Live VIES is not a CI gate or load target. A separately authorized live smoke test
should make at most one availability query and one validation with concurrency 1
and retries 0. Use only legitimate, nonsecret target data.

Load, soak and chaos testing must target your own mock/staging endpoint. Measure
throughput, p50/p95/p99 latency, queue depth, heap/GC, error rates and resource
plateaus. Simulate 429/5xx, slow/truncated bodies, cache failures, cancellation and
shutdown. A green unit suite is not evidence of million-request capacity.
