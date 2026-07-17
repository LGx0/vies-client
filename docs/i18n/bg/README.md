# Български (bg) — vies-client — проверка на ДДС номер

`disableCache()` изключва съхраняваното/постоянното кеширане, но едновременни заявки за една и съща комбинация ДДС номер + requester могат да споделят една single-flight мрежова заявка. След нейното завършване по-късна заявка извършва ново обръщение към VIES. `consultationNumber` е незадължително поле, което VIES може да върне, но никога не е гарантирано; доказателствената му стойност зависи от местните правила. Зареждайте `MY_EU_VAT_NUMBER` само от доверена secret/config система.

**Ключови думи за търсене:** проверка на ДДС номер, валидиране на ДДС номер, валидиране на ДДС в ЕС, проверка на данъчен идентификатор, VIES Java клиент; `VAT checker`, `VAT number validator`, `EU VAT validation`, `tax ID checker`.

Това не е общ данъчен калкулатор, а клиент за проверка на идентификационни номера по ДДС в системата VIES на ЕС.

**Languages:** [English](../../../README.md) · [Български](README.md) · [Всички езици](../../LANGUAGES.md)

> Информационен превод. При различие водещ е английският технически и правен оригинал. Само кореновите LICENSE и NOTICE са правно меродавни; преводът не е лиценз. [LICENSE](../../../LICENSE), [NOTICE](../../../NOTICE).

Самостоятелен Java 21+ клиент без зависимости по време на изпълнение за REST API VIES на Европейската комисия. Поддържа Spring Boot, Quarkus, Micronaut и JDK `HttpServer`, като използва само `java.net.http`.

VIES: <https://ec.europa.eu/taxation_customs/vies/#/technical-information>

## Документация

[Инсталиране](INSTALLATION.md) · [Интеграция](INTEGRATION.md) · [Техническа документация](TECHNICAL.md) · [Тестване](TESTING.md) · [Отворен код](OPEN_SOURCE.md) · [Издания](RELEASING.md)

```bash
./mvnw clean verify
./mvnw install
```

```xml
<dependency><groupId>vies.client</groupId><artifactId>vies-client</artifactId><version>1.0.0</version></dependency>
```

JPMS: `requires vies.client;`; работи и на classpath.

```java
try (var vies = ViesClient.builder().defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER"))).retries(1).build()) {
  ViesResponse result = vies.check("DE000000000");
}
```

`defaultRequester` е собственият ДДС номер. При валидна проверка VIES може да върне незадължителен `consultationNumber`; неговата доказателствена стойност зависи от местните правила, а cache hit пази данните от първоначалната консултация.

checkAsync използва virtual threads. Споделяйте един thread-safe singleton и извикайте close. Pending/network limits дават backpressure, single-flight обединява еднакви заявки.

За милиони задачи: durable partitioned queue, bounded workers, shared Redis, distributed rate limiter, delayed retry, idempotency и DLQ. Virtual threads не увеличават VIES capacity; Unavailable никога не е Invalid.

Резултатите са `Valid`, `Invalid`, `Unavailable`, `MalformedInput`. `error()` дава стабилен код, `messageHu`, `messageEn`, `retryable`. HTTP: 200/400/429/503. Cache пази само `Valid`; грешка при четене е `CACHE_ERROR`. Гърция използва `EL`, Северна Ирландия `XI`.

Test: `./mvnw test`. Единствен лиценз: Apache License 2.0. Проектът не е официален продукт на ЕС.

## Пълна конфигурация и оперативна семантика

Път на заявката: offline normalization → cache → JVM-local single-flight → bounded admission → shared HTTP client → strict validation → запис само на потвърден Valid.

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
| `disableCache()` | — | Без съхраняван cache; едновременни еднакви заявки могат да споделят single-flight |
| `userAgent(String)` | module ID | identify the integration |
| `executor(ExecutorService)` | virtual thread/task | caller owns custom executor lifecycle |

Unavailable означава липса на решение и не е Invalid. Format check не е VIES verification. Retry увеличава товара; малко local attempts и delayed queue при scale. Cached consultationNumber/requestDate е от първоначалната проверка.

| Result | HTTP | Retry | Cache |
|---|---:|---:|---:|
| `Valid` | 200 | no | yes |
| `Invalid` | 200 | no | no |
| `MalformedInput` | 400 | no | no |
| `Unavailable(CLIENT_OVERLOADED)` | 429 | delayed | no |
| other `Unavailable` | 503 | by `error.retryable()` | no |

## Spring Boot, отговори за грешки и мащабна архитектура

Клиентът е immutable и thread-safe. Създайте един singleton bean за живота на приложението и го затворете при shutdown. Обработвайте изрично и четирите резултата; за логика използвайте стабилен errorCode, а към потребителя върнете messageHu/messageEn.

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

Топология за милиони: API ingress → durable partitioned queue → bounded consumer window → worker JVMs → shared Redis → distributed/global rate limiter → VIES. Retryable резултатите отиват в delayed queue, след max attempts в DLQ. Per-JVM semaphore и single-flight не са глобални.

A durable queue consumer must keep a bounded active window. Autoscaling should follow queue age, not unlimited direct VIES concurrency. Liveness must not depend on VIES; availability() should be sparse and cached.

## Разширено ръководство за production

Една инстанция на `ViesClient` се споделя от цялото приложение или worker. Така се използват повторно JDK `HttpClient`, connection pool, TTL cache, single-flight и admission ограниченията. Клиент за всяка заявка губи тези защити. Инстанцията е thread-safe и се затваря с `close()`, `try`-with-resources или Spring `@Bean(destroyMethod = "close")`. След затваряне новите sync и async извиквания хвърлят `IllegalStateException`, а общите операции в ход приключват с `CLIENT_CLOSED`.

`Valid` е потвърждение от VIES, `Invalid` е непотвърдена валидност, `MalformedInput` е неизпращаем вход, а `Unavailable` означава, че решение няма. `Unavailable` никога не се преобразува в `Invalid`. За логика, метрики и retry се използват `error.code()` и `error.retryable()`, а към потребителя могат да се върнат `messageHu` и `messageEn`. Проверка на формата не е VIES валидиране.

`VatFormat` премахва разрешени разделители, преобразува буквите в главни и проверява държавния формат преди мрежата. `GR` се канонизира до `EL`, Северна Ирландия използва `XI`. `defaultRequester` е собственият ДДС номер. Ако са върнати, `consultationNumber` и `requestDate` могат да помогнат за документиране на конкретна консултация, но правната им стойност зависи от местните правила. При cache hit те принадлежат на първоначалната проверка. `disableCache()` не съхранява резултати, но едновременни еднакви заявки пак могат да споделят една single-flight заявка.

`maxPendingSyncRequests` ограничава sync callers, `maxPendingAsyncRequests` уникалните async leaders, `maxConcurrentRequests` реалните HTTP заявки, а `admissionTimeout` чакането за slot. Превишението връща `CLIENT_OVERLOADED`. Async по подразбиране използва virtual threads. Външен executor остава собственост на приложението. Cancel на един follower не отменя общия leader. Single-flight обединява еднакъв target/requester ключ в един JVM, но не е разпределен между pod-ове.

Cache съхранява само `Valid` и е ограничен по TTL и размер. Redis adapter трябва да е thread-safe, с кратък timeout, versioned namespace, metrics и пълна сериализация. Read exception връща `CACHE_ERROR` без VIES fallback, за да предотврати stampede; write exception не изтрива вече потвърден `Valid`. Локалният retry е до пет опита с exponential backoff и jitter само за временни network/VIES грешки. Голямата система използва durable delayed queue, idempotency key, max attempts и DLQ.

Външният JSON е недоверен. Решение изисква root object, истински boolean `isValid` или `valid`, валиден ISO-8601 `requestDate` и липса на overriding `userError`. Нарушението връща `MALFORMED_RESPONSE`, без измислен timestamp или `Invalid`. Production използва официалния HTTPS endpoint; `baseUrl` не идва от user input. В log се маскират ДДС номер, име, адрес и requester.

Архитектурата за милиони записи е API ingress → durable partitioned queue → bounded consumer window → worker JVMs → shared Redis → distributed rate limiter → VIES → delayed retry/DLQ. Всеки pod има локални semaphore, cache и single-flight, затова е нужен глобален limiter. Autoscaling следва възрастта на queue. Virtual threads намаляват цената на чакането, но не увеличават upstream capacity.

Наблюдавайте резултати по тип и `errorCode`, p50/p95/p99 latency, cache hit, `CACHE_ERROR`, `CLIENT_OVERLOADED`, retry outcomes, queue depth/age, DLQ, грешки по държави, heap, GC, CPU, virtual threads и sockets. Liveness не зависи от VIES; `availability()` е рядка кеширана диагностика.

Преди release изпълнете `./mvnw --batch-mode clean verify`. 44 unit теста проверяват формат, requester, mapping, errors, availability, JSON, TTL cache и builder. 24 локални HTTP/concurrency теста проверяват retry, limits, cancellation, close races, custom executor, sync↔async single-flight, cache anti-stampede и fatal `Error`. Live VIES не е CI gate или load target. Load се изпълнява срещу mock/staging, а soak/chaos продължава 30–60 минути с bounded heap и JFR; ресурсите трябва да достигнат plateau.
