# Čeština (cs) — vies-client — ověření DIČ pro DPH

`disableCache()` vypne uloženou/trvalou cache, ale souběžná volání se stejnou dvojicí DIČ + requester mohou sdílet jeden síťový požadavek single-flight. Pozdější volání po jeho dokončení provede nový požadavek VIES. `consultationNumber` je volitelná hodnota, kterou VIES může vrátit, ale nikdy není zaručena; její důkazní hodnota závisí na místních pravidlech. `MY_EU_VAT_NUMBER` načítejte pouze z důvěryhodného úložiště tajemství nebo konfigurace.

**Hledané výrazy:** ověření DIČ, validátor DIČ, ověření DPH v EU, kontrola daňového identifikačního čísla, VIES Java klient; `VAT checker`, `VAT number validator`, `EU VAT validation`, `tax ID checker`.

Nejde o obecnou daňovou kalkulačku, ale o klienta pro ověřování identifikačních čísel k DPH v systému EU VIES.

[Všechny jazyky](../../LANGUAGES.md)

**Jazyky:** [English](../../../README.md) · [Magyar](../../../README.md) · [Čeština](README.md) · [Polski](../pl/README.md) · [Slovenčina](../sk/README.md) · [Slovenščina](../sl/README.md) · [Hrvatski](../hr/README.md) · [Română](../ro/README.md) · [Български](../bg/README.md) · [Ελληνικά](../el/README.md)

> Toto je informativní překlad. Při rozdílu je rozhodující anglický technický a právní originál. Právně závazný text licence je pouze kořenový soubor [LICENSE](../../../LICENSE); jeho překlady nejsou licencí.

`vies-client` je samostatný klient Java 21+ bez běhových závislostí pro REST API systému VIES (VAT Information Exchange System) Evropské komise. Lze jej použít se Spring Boot, Quarkus, Micronaut nebo čistým JDK `HttpServer`; používá pouze `java.net.http`.

- Oficiální informace VIES: <https://ec.europa.eu/taxation_customs/vies/#/technical-information>
- Kontrola: `https://ec.europa.eu/taxation_customs/vies/rest-api/ms/{countryCode}/vat/{vatNumber}`
- Stav členských států: `https://ec.europa.eu/taxation_customs/vies/rest-api/check-status`

## Dokumentace

- [Instalace](INSTALLATION.md) · [Integrace](INTEGRATION.md) · [Technický návrh](TECHNICAL.md)
- [Testování](TESTING.md) · [Open source](OPEN_SOURCE.md) · [Vydávání](RELEASING.md)

## Sestavení a závislost

```bash
./mvnw clean verify
./mvnw install
```

```xml
<dependency>
  <groupId>vies.client</groupId><artifactId>vies-client</artifactId><version>1.0.0</version>
</dependency>
```

```kotlin
implementation("vies.client:vies-client:1.0.0")
```

Jde o skutečný JPMS modul `vies.client`; v `module-info.java` použijte `requires vies.client;`. Funguje i na classpath.

## Rychlý příklad

```java
try (var vies = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
        .retries(1)
        .build()) {
    switch (vies.check("DE 000 000 000")) {
        case ViesResponse.Valid v -> System.out.println(v.traderName().orElse("?"));
        case ViesResponse.Invalid i -> System.out.println("INVALID");
        case ViesResponse.Unavailable u -> System.out.println(u.errorCode());
        case ViesResponse.MalformedInput m -> System.out.println(m.reason());
    }
}
```

`defaultRequester` má být vaše vlastní DIČ pro DPH. U platné kontroly může VIES vrátit `consultationNumber`, který dokládá provedení kontroly. U cache hitu jde o původní konzultaci, ne novou kontrolu.

## Asynchronní a velkoobjemový provoz

`checkAsync()` standardně používá virtuální vlákna. Klient je thread-safe: vytvořte jeden singleton na proces a při ukončení zavolejte `close()`. `maxPendingSyncRequests`, `maxPendingAsyncRequests` a `maxConcurrentRequests` poskytují lokální backpressure; identické požadavky se v jednom JVM slučují pomocí single-flight.

Pro milionové dávky použijte trvalou dělenou frontu, omezené workery, sdílenou Redis cache, distribuovaný rate limiter, opožděné retry a DLQ. Virtuální vlákna nezvyšují kapacitu VIES. `Unavailable` nikdy nepřevádějte na `Invalid`.

```java
var vies = ViesClient.builder()
    .cache(redisViesCache).cacheTtl(Duration.ofHours(24))
    .maxConcurrentRequests(32).maxPendingSyncRequests(512)
    .maxPendingAsyncRequests(512).admissionTimeout(Duration.ofSeconds(2))
    .retries(2).retryDelay(Duration.ofMillis(250)).build();
```

Výsledky tvoří sealed hierarchii `Valid`, `Invalid`, `Unavailable`, `MalformedInput`. `error()` poskytuje stabilní kód, `messageHu`, `messageEn` a `retryable`. Doporučené HTTP mapování: rozhodnutí 200, chybný vstup 400, lokální přetížení 429, jiná nedostupnost 503.

Do cache se ukládá pouze `Valid`. Chyba čtení distribuované cache vrací `CACHE_ERROR`, aby nevznikl request stampede. Řecko používá `EL` (vstup `GR` se převede) a Severní Irsko `XI`.

## Testy a licence

```bash
./mvnw test
```

Projekt používá jedinou licenci **Apache License 2.0**, nikoli kombinaci MIT/Apache. Nejde o oficiální ani schválený produkt EU či daňové správy.

## Úplná konfigurace a sémantika provozu

Životní cyklus požadavku: normalizace bez sítě → cache → JVM-local single-flight → omezený admission → sdílený HTTP klient → přísná validace → zápis pouze potvrzeného Valid.

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
| `disableCache()` | — | Bez uložené cache; souběžná shodná volání mohou sdílet single-flight |
| `userAgent(String)` | module ID | identify the integration |
| `executor(ExecutorService)` | virtual thread/task | caller owns custom executor lifecycle |

Unavailable znamená, že nebylo vydáno rozhodnutí; nesmí se účtovat jako Invalid. Formátová kontrola není ověření VIES. Retry zvyšuje zátěž, proto jej držte malý a ve velkém systému používejte delayed queue. Cache consultationNumber/requestDate patří původní kontrole.

| Result | HTTP | Retry | Cache |
|---|---:|---:|---:|
| `Valid` | 200 | no | yes |
| `Invalid` | 200 | no | no |
| `MalformedInput` | 400 | no | no |
| `Unavailable(CLIENT_OVERLOADED)` | 429 | delayed | no |
| other `Unavailable` | 503 | by `error.retryable()` | no |

## Provozní příručka pro úplnou implementaci

### Životní cyklus klienta a rozhodovací model

V jedné aplikaci nebo worker procesu vytvořte právě jednu instanci `ViesClient`. Sdílená instance znovu používá spojení JDK `HttpClient`, lokální TTL cache, tabulku single-flight a všechny admission limity. Vytváření klienta pro každý HTTP požadavek tyto ochrany ruší a zvyšuje počet spojení i paměťové nároky. Klient je thread-safe. Při řízeném ukončení vždy volejte `close()` nebo použijte `try`-with-resources či Spring `@Bean(destroyMethod = "close")`. Po uzavření nové synchronní i asynchronní volání okamžitě vyhodí `IllegalStateException`; již sdílené rozpracované operace skončí strukturovaným `CLIENT_CLOSED`.

Výsledková sealed hierarchie rozlišuje obchodní rozhodnutí od technické nejistoty. `Valid` znamená, že VIES číslo potvrdil, `Invalid` že je nepotvrdil, `MalformedInput` že vstup nelze odeslat, a `Unavailable` že žádné rozhodnutí nevzniklo. `Unavailable` se nikdy nesmí převést na `Invalid`. Pro programovou logiku, metriky a retry používejte stabilní `error.code()` a `error.retryable()`, nikoli lokalizovaný text. Uživateli lze vrátit `messageHu` a `messageEn`.

### Normalizace, requester a auditní důkaz

`VatFormat` odstraňuje povolené mezery, tečky a pomlčky, převádí písmena na velká a kontroluje tvar podle země ještě před sítí. Jde pouze o formátový filtr, nikoli potvrzení platnosti. Řecký prefix `GR` se kanonizuje na VIES `EL`; Severní Irsko používá `XI`. `defaultRequester` má být vlastní komunitární DIČ organizace. VIES může pro platnou kontrolu vrátit `consultationNumber`. U cache hitu však `consultationNumber` a `requestDate` dokumentují původní kontrolu, nikoli novou konzultaci. Požaduje-li obchodní proces čerstvý důkaz, zohledněte `fromCache`, zkraťte TTL nebo cache vypněte.

### Admission, asynchronní provoz a single-flight

`maxPendingSyncRequests` omezuje počet současných synchronních volajících, `maxPendingAsyncRequests` počet jedinečných asynchronních leaderů a `maxConcurrentRequests` skutečné odchozí HTTP požadavky. Při překročení pending limitu klient vrací `CLIENT_OVERLOADED`; při čekání na síťový slot respektuje `admissionTimeout`. Výchozí async executor vytváří virtuální vlákno pro přijatou úlohu. Vlastní executor klient nezavírá, protože jeho životní cyklus patří aplikaci. Zrušení future jednoho followera neruší sdílenou operaci ostatních.

Single-flight slučuje současné požadavky se stejnou kombinací cílového DIČ a requester DIČ uvnitř jednoho JVM. Sto nebo deset tisíc followerů tak může sdílet jeden upstream požadavek a nečerpá sto či deset tisíc pending slotů. Mechanismus není distribuovaný. Každý pod má vlastní tabulku, semafory, connection pool a paměťovou cache; součet lokálních limitů proto není globální limit vůči VIES.

### Cache, retry a ochrana proti stampede

Vestavěná cache je souběžná, časově omezená a velikostně ohraničená. Ukládá pouze autoritativní `Valid`; `Invalid`, chybný vstup ani technická chyba se neukládají. Externí `ViesCache`, například Redis adaptér, musí být thread-safe, používat krátký connection/command timeout, verzovaný namespace a úplně serializovat datum, optionals a příznak cache. Výjimka při čtení vrací `CACHE_ERROR` bez automatického přechodu na VIES, aby výpadek Redis nezpůsobil stampede. Chyba zápisu po úspěšném VIES výsledku autoritativní `Valid` nemaže.

Lokální retry je záměrně malé, nejvýše pět pokusů, s exponenciálním backoffem a jitterem. Opakují se pouze přechodné síťové nebo VIES chyby. `CLIENT_OVERLOADED`, `CLIENT_CLOSED`, blokace a vstupní chyby se lokálně neopakují. Ve velkém provozu má být hlavní retry mechanismus trvalá delayed queue s idempotency key, maximálním počtem pokusů a DLQ. Okamžité opakování ve všech workerech může zhoršit výpadek upstreamu.

### Přísná validace, bezpečnost a observability

Odpověď VIES je nedůvěryhodný externí JSON. Autoritativní výsledek vyžaduje kořenový objekt, skutečný boolean `isValid` nebo `valid`, platný ISO-8601 `requestDate` a žádný `userError`, který rozhodnutí přepisuje. Chybějící nebo řetězcový boolean a neplatný timestamp vedou k `MALFORMED_RESPONSE`; klient nevymýšlí lokální datum ani `Invalid`. Produkce musí používat oficiální HTTPS endpoint. Testovací `baseUrl` nesmí pocházet z uživatelského vstupu. V logu maskujte DIČ, název, adresu a requester.

Měřte počet výsledků podle typu a `errorCode`, celkovou a upstream p50/p95/p99 latenci, cache hit ratio, `CACHE_ERROR`, `CLIENT_OVERLOADED`, počty retry, stáří a hloubku fronty, delayed retry a DLQ, chybovost jednotlivých zemí, heap, GC, virtuální vlákna, CPU a sockety. Liveness nesmí záviset na VIES; `availability()` používejte jako řídkou cacheovanou diagnostiku.

### Architektura pro milionové dávky a ověření

Produkční tok je: API ingress → trvalá dělená fronta → omezené consumer okno → více worker JVM → sdílený Redis → distribuovaný rate limiter → EU a členské VIES. Autoscaling řiďte stářím fronty, ne neomezenou VIES concurrency. Virtuální vlákna snižují cenu čekání, ale nezvyšují upstream kapacitu. Absolutní lokální benchmark není SLA ani doporučený evropský limit.

Před nasazením spusťte `./mvnw --batch-mode clean verify`. Unit testy ověřují normalizaci, requester, mapování, chyby, dostupnost, JSON, TTL cache a builder. Lokální HTTP/concurrency suite deterministicky kontroluje retry, limity, cancellation, close races, custom executor, oba směry sync/async single-flight, cache anti-stampede a fatal `Error`. Živý VIES nesmí být povinnou CI bránou ani cílem load testu; load patří na lokální mock nebo vlastní staging. Soak/chaos má běžet 30–60 minut s omezeným heapem a JFR a zdroje musí dosáhnout stabilního plata.
