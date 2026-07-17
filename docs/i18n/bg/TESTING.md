# Български (bg) — Тестване

**Languages:** [English](../../TESTING.md) · [Български](TESTING.md) · [Всички езици](../../LANGUAGES.md)

> Информационен превод. При различие водещ е английският технически и правен оригинал. Само кореновите LICENSE и NOTICE са правно меродавни; преводът не е лиценз. [LICENSE](../../../LICENSE), [NOTICE](../../../NOTICE).

Unit test изолира правило без network/DB/live VIES; retry, timeout, single-flight, cancel и close изискват local deterministic concurrency tests.

```bash
./mvnw test
./mvnw -Dtest=VatFormatTest,ViesRequesterTest,ViesResponseMappingTest,ViesErrorTest,ViesAvailabilityTest,ViesClientBuilderTest,MiniJsonTest,TtlCacheTest test
./mvnw -Dtest=ViesClientHttpTest test
./mvnw clean verify
```

Общо 73: 44 unit в 8 класа + 29 local HTTP/integration/concurrency, без задължителна външна мрежа.

Unit catalog: VatFormat 8, Requester 4, ResponseMapping 11, Error 6, Availability 2, MiniJson 4, TtlCache 6, Builder 3. Integration покрива retry, same-key one HTTP, sync/async/network limits, cancellation recovery, close races/deadlock, cache anti-stampede/write, custom executor interrupt/reject, leader/follower equality, sync↔async, chained calls, fatal Error.

Live VIES не е CI gate: opt-in max 1 availability + 1 check, concurrency=1/retries=0. Load само mock/staging; soak/chaos 30–60 min с bounded heap, JFR, restarts/failures; ресурсите трябва да достигнат plateau.

CI: ./mvnw --batch-mode clean verify. Всеки bug получава deterministic regression test; latch/barrier, не sleep като correctness oracle.

## Пълен каталог и тълкуване на тестовете

Unit test изолира правилото без мрежа. Локалният HTTP/concurrency test използва случаен loopback port и deterministic latch/barrier; public VIES не се извиква в default suite.

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

## Разширено ръководство за production

Една инстанция на `ViesClient` се споделя от цялото приложение или worker. Така се използват повторно JDK `HttpClient`, connection pool, TTL cache, single-flight и admission ограниченията. Клиент за всяка заявка губи тези защити. Инстанцията е thread-safe и се затваря с `close()`, `try`-with-resources или Spring `@Bean(destroyMethod = "close")`. След затваряне новите sync и async извиквания хвърлят `IllegalStateException`, а общите операции в ход приключват с `CLIENT_CLOSED`.

`Valid` е потвърждение от VIES, `Invalid` е непотвърдена валидност, `MalformedInput` е неизпращаем вход, а `Unavailable` означава, че решение няма. `Unavailable` никога не се преобразува в `Invalid`. За логика, метрики и retry се използват `error.code()` и `error.retryable()`, а към потребителя могат да се върнат `messageHu` и `messageEn`. Проверка на формата не е VIES валидиране.

`VatFormat` премахва разрешени разделители, преобразува буквите в главни и проверява държавния формат преди мрежата. `GR` се канонизира до `EL`, Северна Ирландия използва `XI`. `defaultRequester` е собственият ДДС номер. Върнатите `consultationNumber` и `requestDate` са доказателство за конкретната консултация. При cache hit те принадлежат на първоначалната проверка; за ново доказателство използвайте `fromCache`, по-кратък TTL или `disableCache()`.

`maxPendingSyncRequests` ограничава sync callers, `maxPendingAsyncRequests` уникалните async leaders, `maxConcurrentRequests` реалните HTTP заявки, а `admissionTimeout` чакането за slot. Превишението връща `CLIENT_OVERLOADED`. Async по подразбиране използва virtual threads. Външен executor остава собственост на приложението. Cancel на един follower не отменя общия leader. Single-flight обединява еднакъв target/requester ключ в един JVM, но не е разпределен между pod-ове.

Cache съхранява само `Valid` и е ограничен по TTL и размер. Redis adapter трябва да е thread-safe, с кратък timeout, versioned namespace, metrics и пълна сериализация. Read exception връща `CACHE_ERROR` без VIES fallback, за да предотврати stampede; write exception не изтрива вече потвърден `Valid`. Локалният retry е до пет опита с exponential backoff и jitter само за временни network/VIES грешки. Голямата система използва durable delayed queue, idempotency key, max attempts и DLQ.

Външният JSON е недоверен. Решение изисква root object, истински boolean `isValid` или `valid`, валиден ISO-8601 `requestDate` и липса на overriding `userError`. Нарушението връща `MALFORMED_RESPONSE`, без измислен timestamp или `Invalid`. Production използва официалния HTTPS endpoint; `baseUrl` не идва от user input. В log се маскират ДДС номер, име, адрес и requester.

Архитектурата за милиони записи е API ingress → durable partitioned queue → bounded consumer window → worker JVMs → shared Redis → distributed rate limiter → VIES → delayed retry/DLQ. Всеки pod има локални semaphore, cache и single-flight, затова е нужен глобален limiter. Autoscaling следва възрастта на queue. Virtual threads намаляват цената на чакането, но не увеличават upstream capacity.

Наблюдавайте резултати по тип и `errorCode`, p50/p95/p99 latency, cache hit, `CACHE_ERROR`, `CLIENT_OVERLOADED`, retry outcomes, queue depth/age, DLQ, грешки по държави, heap, GC, CPU, virtual threads и sockets. Liveness не зависи от VIES; `availability()` е рядка кеширана диагностика.

Преди release изпълнете `./mvnw --batch-mode clean verify`. 44 unit теста проверяват формат, requester, mapping, errors, availability, JSON, TTL cache и builder. 29 локални HTTP/concurrency теста проверяват retry, limits, cancellation, close races, custom executor, sync↔async single-flight, cache anti-stampede и fatal `Error`. Live VIES не е CI gate или load target. Load се изпълнява срещу mock/staging, а soak/chaos продължава 30–60 минути с bounded heap и JFR; ресурсите трябва да достигнат plateau.

### Критерии за приемане

Промяната се приема само ако всички permits и `inFlight` записи се освобождават при rejection, cancellation, interruption и cache exception. Leader и followers трябва да получат еднакъв резултат при `close()`, а user callback не може да задържа lifecycle lock. Тестът използва latch, blocking cache и контролиран executor, проверява точния брой HTTP заявки, възстановяването на pending capacity и липсата на останали threads или sockets. Pull request описва public behavior, compatibility, security impact и възпроизводима performance методика. Авторът потвърждава правото да предостави кода и проверява всеки AI-generated ред, provenance и лиценз. Нов dependency изисква актуализация на third-party notices и отделно обосноваване защо zero-runtime-dependency целта се променя.
