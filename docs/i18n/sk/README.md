# Slovenčina (sk) — vies-client — overenie IČ DPH

**Vyhľadávacie výrazy:** overenie IČ DPH, validátor IČ DPH, overenie DPH v EÚ, kontrola daňového identifikátora, VIES Java klient; `VAT checker`, `VAT number validator`, `EU VAT validation`, `tax ID checker`.

Nejde o všeobecnú daňovú kalkulačku, ale o klienta na overovanie identifikačných čísel DPH v systéme EÚ VIES.

[Všetky jazyky](../../LANGUAGES.md)

**Jazyky:** [English](../../../README.md) · [Magyar](../../../README.md) · [Čeština](../cs/README.md) · [Polski](../pl/README.md) · [Slovenčina](README.md) · [Slovenščina](../sl/README.md) · [Hrvatski](../hr/README.md) · [Română](../ro/README.md) · [Български](../bg/README.md) · [Ελληνικά](../el/README.md)

> Toto je informatívny preklad. Pri rozdiele je rozhodujúci anglický technický a právny originál. Jediným záväzným textom licencie je koreňový [LICENSE](../../../LICENSE); jeho preklad nie je licenciou.

`vies-client` je samostatný klient Java 21+ bez runtime závislostí pre REST API VIES Európskej komisie. Funguje so Spring Boot, Quarkus, Micronaut aj čistým JDK `HttpServer` a používa iba `java.net.http`.

- VIES: <https://ec.europa.eu/taxation_customs/vies/#/technical-information>
- Kontrola: `https://ec.europa.eu/taxation_customs/vies/rest-api/ms/{countryCode}/vat/{vatNumber}`
- Stav: `https://ec.europa.eu/taxation_customs/vies/rest-api/check-status`

## Dokumentácia

[Inštalácia](INSTALLATION.md) · [Integrácia](INTEGRATION.md) · [Technika](TECHNICAL.md) · [Testovanie](TESTING.md) · [Open source](OPEN_SOURCE.md) · [Vydávanie](RELEASING.md)

```bash
./mvnw clean verify
./mvnw install
```

```xml
<dependency><groupId>vies.client</groupId><artifactId>vies-client</artifactId><version>1.0.0</version></dependency>
```

JPMS modul je `vies.client` (`requires vies.client;`), funguje aj na classpath.

```java
try (var vies = ViesClient.builder().defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER"))).retries(1).build()) {
    switch (vies.check("DE 000 000 000")) {
        case ViesResponse.Valid v -> System.out.println(v.traderName().orElse("?"));
        case ViesResponse.Invalid i -> System.out.println("INVALID");
        case ViesResponse.Unavailable u -> System.out.println(u.errorCode());
        case ViesResponse.MalformedInput m -> System.out.println(m.reason());
    }
}
```

`defaultRequester` je vlastné IČ DPH, načítané cez `MY_EU_VAT_NUMBER` z dôveryhodného
secretu alebo konfigurácie. VIES **môže** vrátiť `consultationNumber`, ale údaj je
voliteľný, nikdy nie je zaručený a jeho právna dôkazná hodnota závisí od miestnych
pravidiel. Pri cache hit ide o pôvodnú konzultáciu.

`checkAsync()` používa virtuálne vlákna. Jeden thread-safe klient zdieľajte ako singleton a pri shutdown zavolajte `close()`. `maxPendingSyncRequests`, `maxPendingAsyncRequests`, `maxConcurrentRequests` dávajú lokálny backpressure a single-flight spája rovnaké požiadavky.

Pre milióny položiek treba trvalý partitioned queue, obmedzené workery, spoločný Redis, distribuovaný rate limiter, delayed retry a DLQ. Virtuálne vlákna nezvýšia kapacitu VIES. `Unavailable` nikdy nie je `Invalid`.

Výsledky sú `Valid`, `Invalid`, `Unavailable`, `MalformedInput`; `error()` obsahuje stabilný kód, `messageHu`, `messageEn`, `retryable`. HTTP: 200/400/429/503. Cache ukladá iba `Valid`; read failure → `CACHE_ERROR`. Grécko používa `EL` (`GR` sa mapuje), Severné Írsko `XI`.

```bash
./mvnw test
```

Jediná licencia je **Apache License 2.0**, nie dual MIT/Apache. Projekt nie je oficiálnym produktom EÚ ani daňovej správy.

## Úplná konfigurácia a prevádzková sémantika

Cesta požiadavky: offline normalizácia → cache → JVM-local single-flight → bounded admission → shared HTTP client → strict validation → zápis iba potvrdeného Valid.

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
| `disableCache()` | — | bez uloženej cache; rovnaké paralelné volania môžu zdieľať jeden single-flight |
| `userAgent(String)` | module ID | identify the integration |
| `executor(ExecutorService)` | virtual thread/task | caller owns custom executor lifecycle |

Unavailable znamená bez rozhodnutia a nesmie sa chápať ako Invalid. Format check nie je VIES validácia. Retry zvyšuje load; lokálne ho držte malý, vo veľkom použite delayed queue. Cached consultationNumber/requestDate patrí pôvodnej kontrole. `disableCache()` vypne iba uloženú cache: rovnaké súbežné volania VAT+requester môžu zdieľať jednu sieťovú požiadavku single-flight; neskoršie volanie po jej dokončení odošle novú požiadavku.

| Result | HTTP | Retry | Cache |
|---|---:|---:|---:|
| `Valid` | 200 | no | yes |
| `Invalid` | 200 | no | no |
| `MalformedInput` | 400 | no | no |
| `Unavailable(CLIENT_OVERLOADED)` | 429 | delayed | no |
| other `Unavailable` | 503 | by `error.retryable()` | no |

## Spring Boot, chybové odpovede a veľká architektúra

Klient je immutable a thread-safe. Vytvorte jeden singleton bean na celý život aplikácie a zatvorte ho pri shutdown. Všetky štyri výsledky spracujte explicitne; pre logiku používajte stabilný errorCode, používateľovi vráťte messageHu/messageEn.

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

Milliónová topológia: API ingress → durable partitioned queue → bounded consumer window → worker JVMs → shared Redis → distributed/global rate limiter → VIES. Retryable výsledky idú do delayed queue, po max attempts do DLQ. Per-JVM semaphore ani single-flight nie je globálny.

A durable queue consumer must keep a bounded active window. Autoscaling should follow queue age, not unlimited direct VIES concurrency. Liveness must not depend on VIES; availability() should be sparse and cached.

## Kompletná prevádzková príručka

### Životný cyklus klienta a model rozhodnutí

V aplikácii alebo worker procese vytvorte presne jednu inštanciu `ViesClient`. Zdieľaný klient opakovane využíva spojenia JDK `HttpClient`, lokálnu TTL cache, tabuľku single-flight a všetky admission limity. Klient vytváraný pre každý HTTP request tieto ochrany ruší, zvyšuje počet spojení a spotrebu pamäte. Inštancia je thread-safe. Pri riadenom ukončení vždy volajte `close()`, použite `try`-with-resources alebo Spring `@Bean(destroyMethod = "close")`. Po zatvorení nové sync aj async volanie synchronne vyhodí `IllegalStateException`; spoločné rozpracované operácie sa ukončia štruktúrovaným `CLIENT_CLOSED`.

Sealed hierarchia oddeľuje obchodné rozhodnutie od technickej neistoty. `Valid` znamená potvrdenie platnosti systémom VIES, `Invalid` jej nepotvrdenie, `MalformedInput` neopraviteľný vstup pre request a `Unavailable` stav bez rozhodnutia. `Unavailable` sa nikdy nesmie zmeniť na `Invalid`. V programovej logike, metrikách a retry používajte stabilné `error.code()` a `error.retryable()`, nie lokalizovaný text. Používateľovi možno vrátiť `messageHu` a `messageEn`.

### Normalizácia, requester a auditný dôkaz

`VatFormat` odstráni povolené medzery, bodky a pomlčky, písmená zmení na veľké a ešte pred sieťou overí tvar krajiny. Je to iba kontrola formátu, nie potvrdenie platnosti. Grécky prefix `GR` sa kanonizuje na VIES `EL`; Severné Írsko používa `XI`. `defaultRequester` má byť vlastné IČ DPH organizácie. VIES môže pri platnom výsledku vrátiť voliteľný, nezaručený `consultationNumber`; jeho právna hodnota závisí od miestnych pravidiel. Pri cache hit však `consultationNumber` a `requestDate` patria pôvodnej kontrole.

### Admission, async prevádzka a single-flight

`maxPendingSyncRequests` obmedzuje súčasných sync volajúcich, `maxPendingAsyncRequests` jedinečných async leaderov a `maxConcurrentRequests` skutočné odchádzajúce HTTP requesty. Prekročenie pending limitu vracia `CLIENT_OVERLOADED`; čakanie na sieťový slot rešpektuje `admissionTimeout`. Predvolený async executor vytvára virtuálne vlákno pre prijatú úlohu. Externe dodaný executor klient nezatvára, pretože jeho životný cyklus patrí aplikácii. Zrušenie future jedného followera nezruší spoločnú operáciu.

Single-flight spája v jednom JVM súčasné requesty s rovnakou kombináciou target IČ DPH a requester IČ DPH. Stovky či tisíce followerov tak zdieľajú jeden upstream request a nespotrebujú samostatné pending sloty. Mechanizmus nie je distribuovaný. Každý pod má vlastnú tabuľku, semaphore, connection pool a pamäťovú cache; súčet per-pod limitov preto nie je globálny limit VIES.

### Cache, retry a ochrana pred stampede

Vstavaná cache je concurrent, časovo aj veľkostne ohraničená. Ukladá iba autoritatívny `Valid`; `Invalid`, chybný vstup a technická chyba sa neukladajú. Externý `ViesCache`, napríklad Redis adaptér, musí byť thread-safe, mať krátky connection/command timeout, versioned namespace a úplnú serializáciu dátumu, optionals a príznaku cache. Výnimka pri čítaní vráti `CACHE_ERROR` bez VIES fallbacku, aby výpadok Redis nespustil stampede. Chyba zápisu po úspechu nesmie zmazať autoritatívny `Valid`.

Lokálny retry je zámerne malý, najviac päť pokusov, s exponential backoff a jitter. Opakujú sa iba prechodné network/VIES chyby. `CLIENT_OVERLOADED`, `CLIENT_CLOSED`, blokovanie a input error sa lokálne neopakujú. Vo veľkom systéme je primárnym mechanizmom durable delayed queue s idempotency key, maximálnym počtom pokusov a DLQ. Okamžité retry vo všetkých workeroch môže výpadok upstreamu zhoršiť.

### Strict mapping, bezpečnosť a observability

Odpoveď VIES je nedôveryhodný externý JSON. Autoritatívne rozhodnutie vyžaduje root object, skutočný boolean `isValid` alebo `valid`, platný ISO-8601 `requestDate` a žiadny nadradený `userError`. Chýbajúci alebo textový boolean a chybný timestamp vedú k `MALFORMED_RESPONSE`; klient nevymýšľa lokálny čas ani `Invalid`. Produkcia používa oficiálny HTTPS endpoint. Testovací `baseUrl` nesmie pochádzať z používateľského vstupu. V logoch maskujte IČ DPH, názov, adresu a requester.

Merajte počet výsledkov podľa typu a `errorCode`, celkovú aj upstream p50/p95/p99 latency, cache hit ratio, `CACHE_ERROR`, `CLIENT_OVERLOADED`, retry outcomes, hĺbku a vek queue, delayed retry a DLQ, chybovosť podľa krajiny, heap, GC, virtual threads, CPU a sockety. Liveness nesmie závisieť od VIES; `availability()` používajte ako zriedkavú cacheovanú diagnostiku.

### Architektúra miliónových dávok a overenie

Produkčný tok: API ingress → durable partitioned queue → bounded consumer window → worker JVMs → shared Redis → distributed rate limiter → EÚ a členské VIES. Autoscaling riaďte vekom queue, nie neobmedzenou VIES concurrency. Virtuálne vlákna znižujú cenu čakania, ale nezvyšujú upstream kapacitu. Lokálny benchmark nie je SLA ani odporúčaný európsky limit.

Pred nasadením spustite `./mvnw --batch-mode clean verify`. Unit testy pokrývajú formát, requester, mapping, errors, availability, JSON, TTL cache a builder. Lokálna HTTP/concurrency suite deterministicky overuje retry, limity, cancellation, close races, custom executor, oba smery sync/async single-flight, cache anti-stampede a fatal `Error`. Live VIES nesmie byť povinnou CI bránou ani cieľom load testu; load patrí na mock alebo vlastný staging. Soak/chaos má bežať 30–60 minút s bounded heap a JFR a zdroje musia dosiahnuť stabilné plateau.
