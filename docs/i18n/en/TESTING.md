# English (en) — Testing

> [Language selector](../../LANGUAGES.md) · This localization is provided for accessibility. If it differs from the canonical English technical or legal source, the English source governs. The root `LICENSE` and `NOTICE` remain legally authoritative and are not replaced by translations.

## What is a unit test?

Yes: the unit test checks a single class or rule in isolation, external
without network, database and live VIES. Fast, deterministic, and in every build
can run.
Yes: a unit test verifies one class or rule in isolation, without external network,
database, or live VIES. It is fast, deterministic, and suitable for every build.
This module also requires **local integration and concurrency tests**
also because the timeout, retry, single-flight, cancel and `close()` behaviors are more
it is evident from the cooperation of the component. They use a loopback mock server, no
call the EU service.
This module also needs **local integration and concurrency tests**, because timeout,
retry, single-flight, cancellation, and shutdown involve multiple components. These
tests use a loopback mock server and never call the public EU service.

## Quick commands

Full deterministic test package / Full deterministic suite:

```bash
./mvnw test
```

Unit tests only:

```bash
./mvnw -Dtest=VatFormatTest,ViesRequesterTest,ViesResponseMappingTest,ViesErrorTest,ViesAvailabilityTest,ViesClientBuilderTest,MiniJsonTest,TtlCacheTest test
```

Only local HTTP/concurrency tests / Local HTTP and concurrency only:

```bash
./mvnw -Dtest=ViesClientHttpTest test
```

Clean verification with JAR/Javadoc generation / Clean verification with artifacts:

```bash
./mvnw clean verify
./mvnw package
```

One specific test / One test method:

```bash
./mvnw -Dtest=ViesClientHttpTest#cancellationDoesNotLeakAsyncCapacity test
```

## Current coverage

The current deterministic package contains **73 tests**:

- **44 unit test** in eight classes;
- **29 local HTTP/integration/concurrency tests**;
- zero mandatory external network calls.
  The deterministic suite contains **73 tests**: 44 unit tests, 29 local
  HTTP/integration/concurrency tests, and zero mandatory external calls.

## Unit test catalog

### `VatFormatTest`— 8 tests

| ID       | Hungarian goal                                           | English purpose                               |
| -------- | -------------------------------------------------------- | --------------------------------------------- |
| U-FMT-01 | Normalization of total tax number                        | Normalize the full VAT identifier             |
| U-FMT-02 | Remove space/period/hyphen, capitalize                   | Strip separators and upper case               |
| U-FMT-03 | `GR`→`EL`mapping                                         | Map Greece`GR`to VIES`EL`                     |
| U-FMT-04 | Reject null, blank, unknown country and incorrect length | Reject null/blank/unknown/bad length          |
| U-FMT-05 | Representative country forms                             | Representative country formats                |
| U-FMT-06 | Separate country code+number API, attached prefix        | Pair API with attached prefix                 |
| U-FMT-07 | Supported country codes                                  | Supported-country set                         |
| U-FMT-08 | All 28 supported countries have at least one form of     | At least one shape for all 28 supported codes |

### `ViesRequesterTest`— 4 tests

| ID       | Hungarian goal                          | English purpose                       |
| -------- | --------------------------------------- | ------------------------------------- |
| U-REQ-01 | Complete from own tax number requester  | Create requester from full VAT        |
| U-REQ-02 | Canonization of Greek requester         | Canonicalize Greek requester          |
| U-REQ-03 | Paired constructor with attached prefix | Pair constructor with matching prefix |
| U-REQ-04 | Reject an invalid requester immediately | Fail fast on invalid requester        |

### `ViesResponseMappingTest`— 11 tests

| ID       | Hungarian goal                                     | English purpose                       |
| -------- | -------------------------------------------------- | ------------------------------------- |
| U-MAP-01 | GET-style `Valid` with all fields                  | Map GET-style Valid response          |
| U-MAP-02 | POST-style `Valid`,`---` placeholder               | Map POST-style Valid and placeholders |
| U-MAP-03 | Authentic `Invalid`                                | Map authoritative Invalid             |
| U-MAP-04 | Transient error →`Unavailable`                     | Map transient error to Unavailable    |
| U-MAP-05 | Input error →`MalformedInput`                      | Map VIES input error                  |
| U-MAP-06 | Reject non-object JSON                             | Reject non-object JSON                |
| U-MAP-07 | Missing boolean cannot be `Invalid`                | Missing boolean never becomes Invalid |
| U-MAP-08 | String boolean cannot be `Invalid`                 | Non-boolean validity rejected         |
| U-MAP-09 | We cannot find local time for a missing audit date | Never invent missing audit timestamp  |
| U-MAP-10 | Reject incorrect audit date                        | Reject invalid audit timestamp        |
| U-MAP-11 | Correct conversion of offset timestamp to UTC      | Parse offset timestamp to UTC         |

### `ViesErrorTest`— 6 tests

| ID       | Hungarian goal                                | English purpose                              |
| -------- | --------------------------------------------- | -------------------------------------------- |
| U-ERR-01 | Hungarian+English network message             | Bilingual network error                      |
| U-ERR-02 | Input error cannot be retried                 | Input error is permanent                     |
| U-ERR-03 | HTTP 408/429/5xx retry classification         | HTTP retry classification                    |
| U-ERR-04 | `Valid`/`Invalid` is not an error             | Decisions expose no error                    |
| U-ERR-05 | All public codes HU+EN and stable retry value | Every public code has HU+EN and retry policy |
| U-ERR-06 | Retain unknown code without retry storm       | Preserve unknown code without retry storm    |

### `ViesAvailabilityTest`— 2 tests

- protection copy and immutability of the input map;
- immediate rejection of null member state map.
  Defensive immutable snapshot copy and null-map constructor validation.

### `MiniJsonTest`— 4 tests

- typical VIES document and Unicode escape;
- nested object/list/scalar/number/null;
- JSON escapes;
- rejection of incorrect, truncated and trailing input.
  Typical VIES JSON, nested/scalar values, escapes, and fail-closed malformed input.

### `TtlCacheTest`— 6 tests

- hit before TTL, miss after;
- exact TTL limit;
- ignore non-positive TTL;
- configured size limit;
- preferred displacement of an expired element;
- 32 virtual thread concurrent write/read.
  TTL behavior, exact expiry boundary, size control, expired-first sampling, and
  concurrent pressure from 32 virtual threads.

### `ViesClientBuilderTest`— 3 tests

- complete high-load configuration can be built;
- reject wrong URL/limit/retry;
- rejection of zero, negative and overflow Duration.
  Valid high-load configuration plus URL, limit, retry, duration, and overflow
  validation.

## Local integration and concurrency

`ViesClientHttpTest`— 29 tests on a random free loopback port:
| ID | Test case |
|---|---|
| I-01 | 503 retries twice, then success / two 503 retries then success |
| C-01 | 200 same-key async callers → exactly 1 HTTP request / 200 same-key async callers → one request |
| C-02 | Active HTTP requests do not exceed the limit of 4 / active calls stay within 4 |
| C-03 | Async pending above limit immediate `CLIENT_OVERLOADED`|
| C-04 | Future cancel does not leak permitet, capacity is restored |
| C-05 |`close()` does not deadlock from async callback |
| C-06 |`admissionTimeout` limits queuing |
| I-02 | Redis/cache read error →`CACHE_ERROR`, null VIES call |
| C-07 | Permit and single-flight | are released before consecutive async calls
| C-08 | Async cache/close race →`CLIENT_CLOSED`, null HTTP |
| C-09 | Sync cache/close race →`CLIENT_CLOSED`, null HTTP |
| C-10 | Custom executor job is interrupted, but the executor does not close |
| C-11 | Sync leader and follower get the same result `CLIENT_CLOSED`|
| C-12 | 100 identical async followers do not consume 100 pending slots |
| C-13 | After executor rejection, permit and in-flight status is restored |
| C-14 | Sync leader + async follower → one HTTP request |
| C-15 | Second cache-check closes stale-miss race, null HTTP |
| C-16 | At close during cache writing, the result of sync leader/follower is the same |
| C-17 | The pending permit | is also released before a chained async call with different keys
| C-18 | A queued custom-executor task is actually canceled by close |
| C-19 | Blocking user callback does not block close/lifecycle lock |
| C-20 | Async leader + sync follower → one HTTP request |
| C-21 |`maxPendingSyncRequests` gives instant limited backpressure |
| C-22 | Fatal async `Error` also continues towards exceptional future and uncaught handler |

## What is not in the default suite?

## Live VIES smoke test

The live service is variable and rate-limited, so there cannot be a mandatory CI condition.
Manual smoke test is at most one `availability()` and a known, non-secret or
query the tax number obtained from an environment variable, concurrency=1 and retries=0
setting.
Live VIES is variable and rate-limited, so it must not gate normal CI. An opt-in
smoke test should perform at most one availability check and one validation, with
concurrency=1 and retries=0. Never commit or log private requester VAT numbers.

## Load test

Always run against a local mock or your own staging service, never the public one
against VIES. Absolute req/s are informative; correctness, limit, p95/p99 and error codes
the gates.
Always target a local mock or owned staging service, never public VIES. Absolute
requests/second is informational; correctness, bounds, p95/p99, and error semantics
are the gates.
Recommended cases:

- many distinct keys;
- 100k hot-key caller on a small set of keys / hot-key stampede;
- overload and recovery above the limit;
- 200/429/503/timeout/malformed mixed response;
- cache-pressure around the configured max.

## Soak and chaos test

30-60 minute fixed load, limited heap, JFR, repeated close/restart, latency
spike, connection reset, cache failure and cancellation. The heap, active threads,
number of in-flight/pending and sockets plateau; there should be no deadlocks or permit leaks.
Run 30–60 minutes with a fixed heap, JFR, repeated lifecycle, latency spikes,
connection resets, cache failures, and cancellation. Heap/threads/in-flight/sockets
must plateau without deadlock or permit leaks.
JFR example / JFR example:

```bash
JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=target/vies-soak.jfr,settings=profile" ./mvnw test
```

## CI recommendation

Minimum pipeline:

```bash
./mvnw --batch-mode clean verify
```

Recommended matrix: at least JDK 21 LTS; additional JDKs can only be set then
supported if the same suite actually runs on them.
Recommended matrix: at least JDK 21 LTS. Claim additional JDK support only after
running the same suite on those versions.

## Regression rule

Each fixed bug should have a deterministic test that is the fix
would have failed before the fix. Latch/barrier should be the priority for concurrency test
synchronization; fixed `sleep` can only be a short polling backoff, not an oracle of correctness.
Every fixed bug must receive a deterministic test that failed before the fix.
Prefer latches/barriers for concurrency tests; fixed sleeps may be short polling
backoff only, never the correctness oracle.
