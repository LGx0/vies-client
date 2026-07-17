# Български (bg) — Техническа документация

**Languages:** [English](../../TECHNICAL.md) · [Български](TECHNICAL.md) · [Всички езици](../../LANGUAGES.md)

> Информационен превод. При различие водещ е английският технически и правен оригинал. Само кореновите LICENSE и NOTICE са правно меродавни; преводът не е лиценз. [LICENSE](../../../LICENSE), [NOTICE](../../../NOTICE).

Java 21 модулът vies.client export-ва public API; MiniJson и TtlCache са internal. Не замества durable queue, global limiter или shared cache.

Valid/Invalid са решения, Unavailable означава липса на решение, MalformedInput е грешен вход. Unavailable никога не се преобразува в Invalid.

Поток: normalize → cache → JVM single-flight → bounded admission → shared HttpClient → strict JSON mapping → cache само Valid. Sync чете cache на caller thread, async на bounded worker. Pending limits пазят паметта, maxConcurrentRequests мрежата, admissionTimeout чакането. Cancel на follower не спира leader.

Retry 0–5 с exponential backoff+jitter само за transient network/VIES errors. Default cache: concurrent 10,000 entries, 24h, key target+requester; read failure CACHE_ERROR, write failure не променя Valid.

External JSON е недоверен: object, реален boolean isValid/valid, валиден ISO-8601 requestDate, без overriding userError; иначе MALFORMED_RESPONSE, никога измислен Invalid/timestamp.

close() е idempotent, прекъсва internal work, затваря HTTP, но не caller executor; callbacks се завършват извън lifecycle lock.

Topology: ingress → durable queue → bounded workers → Redis → distributed limiter → VIES + delayed retry/DLQ. Metrics: result/code, p50/95/99, cache, pending, retry, queue, country, JVM.

JDK 21 loopback (2026-07-17): cache 8.91 M/s; format 9.02 M/s; sync HTTP 4,044/s; async 21,640/s; 10,000 same-key → 1 HTTP. Това не е SLA. В production ползвайте official HTTPS, mask data, no user-controlled baseUrl.

## Публичен модул, инварианти и изключване

```text
module vies.client
├── exports vies.client
│   ├── ViesClient, ViesResponse, ViesError
│   ├── VatFormat, ViesRequester, ViesAvailability
│   └── ViesCache, ViesException
└── vies.client.internal
    ├── MiniJson
    └── TtlCache
```

| Резултат | Значение | Retry | Cache |
|---|---|---:|---:|
| `Valid` | VIES потвърди валидността | no | yes |
| `Invalid` | VIES не потвърди валидността | no | no |
| `Unavailable` | Няма решение; retry според кода | by code | no |
| `MalformedInput` | Грешен вход; без retry | no | no |

### Retry

The client allows 0–5 local retries:

```text
delay ~= retryDelay × 2^(attempt-1) + random(0 .. delay/2)
```

CLIENT_OVERLOADED, CLIENT_CLOSED and input errors are not retried locally. At scale the durable delayed queue is the primary retry mechanism.

### Cache and strict mapping

Cache пази само Valid. Ключът съдържа target и requester VAT. Cache hit има fromCache=true и първоначалните requestDate/consultationNumber. Read failure в distributed cache връща CACHE_ERROR без VIES fallback; write failure не изтрива потвърден Valid.

Външният JSON е недоверен. Valid/Invalid изисква object, истински boolean isValid/valid, валиден ISO-8601 requestDate и без overriding userError. Иначе MALFORMED_RESPONSE, никога измислен Invalid или локален timestamp.

### Shutdown and observability

close() е idempotent, не приема нова работа, прекъсва вътрешните задачи и завършва shared leaders като CLIENT_CLOSED извън lifecycle lock. Не затваря caller-provided executor. Нов sync/async call след close хвърля синхронно IllegalStateException.

Measure counts by result/errorCode, p50/p95/p99, cache hit/CACHE_ERROR, pending/CLIENT_OVERLOADED, retry outcomes, queue depth/age/DLQ, country availability, heap/GC/virtual threads/CPU/sockets. Official HTTPS only; mask VAT/name/address and never take baseUrl from user input.

## Разширено ръководство за production

Една инстанция на `ViesClient` се споделя от цялото приложение или worker. Така се използват повторно JDK `HttpClient`, connection pool, TTL cache, single-flight и admission ограниченията. Клиент за всяка заявка губи тези защити. Инстанцията е thread-safe и се затваря с `close()`, `try`-with-resources или Spring `@Bean(destroyMethod = "close")`. След затваряне новите sync и async извиквания хвърлят `IllegalStateException`, а общите операции в ход приключват с `CLIENT_CLOSED`.

`Valid` е потвърждение от VIES, `Invalid` е непотвърдена валидност, `MalformedInput` е неизпращаем вход, а `Unavailable` означава, че решение няма. `Unavailable` никога не се преобразува в `Invalid`. За логика, метрики и retry се използват `error.code()` и `error.retryable()`, а към потребителя могат да се върнат `messageHu` и `messageEn`. Проверка на формата не е VIES валидиране.

`VatFormat` премахва разрешени разделители, преобразува буквите в главни и проверява държавния формат преди мрежата. `GR` се канонизира до `EL`, Северна Ирландия използва `XI`. `defaultRequester` е собственият ДДС номер. Върнатите `consultationNumber` и `requestDate` са доказателство за конкретната консултация. При cache hit те принадлежат на първоначалната проверка; за ново доказателство използвайте `fromCache`, по-кратък TTL или `disableCache()`.

`maxPendingSyncRequests` ограничава sync callers, `maxPendingAsyncRequests` уникалните async leaders, `maxConcurrentRequests` реалните HTTP заявки, а `admissionTimeout` чакането за slot. Превишението връща `CLIENT_OVERLOADED`. Async по подразбиране използва virtual threads. Външен executor остава собственост на приложението. Cancel на един follower не отменя общия leader. Single-flight обединява еднакъв target/requester ключ в един JVM, но не е разпределен между pod-ове.

Cache съхранява само `Valid` и е ограничен по TTL и размер. Redis adapter трябва да е thread-safe, с кратък timeout, versioned namespace, metrics и пълна сериализация. Read exception връща `CACHE_ERROR` без VIES fallback, за да предотврати stampede; write exception не изтрива вече потвърден `Valid`. Локалният retry е до пет опита с exponential backoff и jitter само за временни network/VIES грешки. Голямата система използва durable delayed queue, idempotency key, max attempts и DLQ.

Външният JSON е недоверен. Решение изисква root object, истински boolean `isValid` или `valid`, валиден ISO-8601 `requestDate` и липса на overriding `userError`. Нарушението връща `MALFORMED_RESPONSE`, без измислен timestamp или `Invalid`. Production използва официалния HTTPS endpoint; `baseUrl` не идва от user input. В log се маскират ДДС номер, име, адрес и requester.

Архитектурата за милиони записи е API ingress → durable partitioned queue → bounded consumer window → worker JVMs → shared Redis → distributed rate limiter → VIES → delayed retry/DLQ. Всеки pod има локални semaphore, cache и single-flight, затова е нужен глобален limiter. Autoscaling следва възрастта на queue. Virtual threads намаляват цената на чакането, но не увеличават upstream capacity.

Наблюдавайте резултати по тип и `errorCode`, p50/p95/p99 latency, cache hit, `CACHE_ERROR`, `CLIENT_OVERLOADED`, retry outcomes, queue depth/age, DLQ, грешки по държави, heap, GC, CPU, virtual threads и sockets. Liveness не зависи от VIES; `availability()` е рядка кеширана диагностика.

Преди release изпълнете `./mvnw --batch-mode clean verify`. 44 unit теста проверяват формат, requester, mapping, errors, availability, JSON, TTL cache и builder. 24 локални HTTP/concurrency теста проверяват retry, limits, cancellation, close races, custom executor, sync↔async single-flight, cache anti-stampede и fatal `Error`. Live VIES не е CI gate или load target. Load се изпълнява срещу mock/staging, а soak/chaos продължава 30–60 минути с bounded heap и JFR; ресурсите трябва да достигнат plateau.

### Критерии за приемане

Промяната се приема само ако всички permits и `inFlight` записи се освобождават при rejection, cancellation, interruption и cache exception. Leader и followers трябва да получат еднакъв резултат при `close()`, а user callback не може да задържа lifecycle lock. Тестът използва latch, blocking cache и контролиран executor, проверява точния брой HTTP заявки, възстановяването на pending capacity и липсата на останали threads или sockets. Pull request описва public behavior, compatibility, security impact и възпроизводима performance методика. Авторът потвърждава правото да предостави кода и проверява всеки AI-generated ред, provenance и лиценз. Нов dependency изисква актуализация на third-party notices и отделно обосноваване защо zero-runtime-dependency целта се променя.
