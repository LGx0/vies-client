# Български (bg) — Инсталиране

**Languages:** [English](../../INSTALLATION.md) · [Български](INSTALLATION.md) · [Всички езици](../../LANGUAGES.md)

> Информационен превод. При различие водещ е английският технически и правен оригинал. Само кореновите LICENSE и NOTICE са правно меродавни; преводът не е лиценз. [LICENSE](../../../LICENSE), [NOTICE](../../../NOTICE).

## Изисквания

JDK 21+, Maven 3.9+, outbound HTTPS към ec.europa.eu:443; API key не е нужен. java, javac и Maven трябва да ползват същия JDK; иначе коригирайте JAVA_HOME.

```bash
java -version
javac -version
./mvnw -version
brew install openjdk@21
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

Linux обикновено използва /usr/lib/jvm/java-21-openjdk. Windows PowerShell: `$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"`. Направете настройката постоянна.

## Build, Maven, Gradle, JPMS

```bash
./mvnw clean verify
./mvnw install
```

Получавате binary, sources и Javadoc JAR в target и локален Maven artifact.

```kotlin
repositories { mavenLocal() }
dependencies { implementation("vies.client:vies-client:1.2.0") }
java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }
```

Без build tool: javac/java -cp; separator Windows ;, Unix :. За JPMS: requires vies.client и --module-path.

## IDE, мрежа и smoke test

Настройте .vscode/settings.json към локалния JDK. Proxy/CA се настройват в JDK truststore. Endpoint: https://ec.europa.eu/taxation_customs/vies/rest-api.

```bash
./mvnw -q test
java -cp target/classes examples/ViesDemoServer.java
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Live smoke не блокира CI. Проверявайте JDK, DNS/proxy/TLS (NETWORK_ERROR), VIES (TIMEOUT), ingress (CLIENT_OVERLOADED), Redis (CACHE_ERROR).

## Пълно включване на артефакта и отстраняване на проблеми

### Maven

```xml
<dependency>
  <groupId>vies.client</groupId>
  <artifactId>vies-client</artifactId>
  <version>1.2.0</version>
</dependency>
```

### Без build tool / Fără instrument de build

```bash
javac -cp /path/to/vies-client-1.2.0.jar MyApplication.java
java -cp "/path/to/vies-client-1.2.0.jar:." MyApplication
```

### JPMS

```java
module my.application {
    requires vies.client;
}
```

```bash
javac --module-path vies-client-1.2.0.jar -d out src/module-info.java src/my/application/Main.java
java --module-path "vies-client-1.2.0.jar:out" -m my.application/my.application.Main
```

| Проблем | Решение |
|---|---|
| `release version 21 not supported` | Maven използва стар JDK; коригирайте JAVA_HOME |
| `UnsupportedClassVersionError` | Стартирайте на JVM 21+ |
| `NETWORK_ERROR` / `TIMEOUT` | Проверете DNS, proxy, firewall и TLS |
| `CLIENT_OVERLOADED` | Delayed retry, bounded ingress, workers и global limiter |
| `CACHE_ERROR` | Проверете Redis; не го заобикаляйте с VIES stampede |

Artifacts for a team/CI should be published to Nexus, Artifactory, GitHub Packages or Maven Central; `./mvnw install` alone only populates the local `~/.m2/repository`.

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
