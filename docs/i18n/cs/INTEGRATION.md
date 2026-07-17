# Čeština (cs) — Integrační příručka

`disableCache()` vypne uloženou/trvalou cache, ale souběžná volání se stejnou dvojicí DIČ + requester mohou sdílet jeden síťový požadavek single-flight. Pozdější volání po jeho dokončení provede nový požadavek VIES. `consultationNumber` je volitelná hodnota, kterou VIES může vrátit, ale nikdy není zaručena; její důkazní hodnota závisí na místních pravidlech. `MY_EU_VAT_NUMBER` načítejte pouze z důvěryhodného úložiště tajemství nebo konfigurace.

**Jazyky:** [English](../../INTEGRATION.md) · [Čeština](INTEGRATION.md) · [Polski](../pl/INTEGRATION.md) · [Slovenčina](../sk/INTEGRATION.md) · [Slovenščina](../sl/INTEGRATION.md) · [Hrvatski](../hr/INTEGRATION.md) · [Română](../ro/INTEGRATION.md) · [Български](../bg/INTEGRATION.md) · [Ελληνικά](../el/INTEGRATION.md)

> Informativní překlad; rozhodující je anglický technický/právní originál. [LICENSE](../../../LICENSE) nepřekládejte jako závaznou licenci.

## 1. Životní cyklus a API

Použijte jeden thread-safe `ViesClient` na aplikaci/worker. Klient per HTTP request zahazuje connection pool, cache, single-flight a limity. Po `close()` jsou rozpracované interní operace přerušeny a sdílené požadavky dostanou `CLIENT_CLOSED`; nové sync i async volání synchronně vyhodí `IllegalStateException`.

```java
var client = ViesClient.builder()
    .connectTimeout(Duration.ofSeconds(5)).requestTimeout(Duration.ofSeconds(8))
    .admissionTimeout(Duration.ofSeconds(2)).maxConcurrentRequests(32)
    .maxPendingSyncRequests(512).maxPendingAsyncRequests(512).retries(1).build();
```

`check()` vrací jednu ze čtyř variant. `checkAsync()` standardně běží na virtuálních vláknech; identické VAT/requester požadavky sdílejí interní future, ale každý volající dostane kopii bezpečnou vůči cancel. Nedržte miliony future v paměti.

## 2. HTTP smlouva

| Výsledek | HTTP | Retry |
|---|---:|---|
| `Valid`, `Invalid` | 200 | ne |
| `MalformedInput` | 400 | ne |
| `Unavailable(CLIENT_OVERLOADED)` | 429 | opožděný |
| ostatní `Unavailable` | 503 | podle `error.retryable()` |

```json
{"status":"UNAVAILABLE","vatNumber":"CZ12345678","errorCode":"MS_UNAVAILABLE","messageHu":"...","messageEn":"...","retryable":true}
```

Pro logy, metriky a logiku používejte stabilní `errorCode`, ne lokalizovanou zprávu.

## 3. Spring Boot a čistý JDK

V Springu registrujte singleton bean s `@Bean(destroyMethod = "close")`; controller musí explicitně mapovat všechny čtyři varianty. Knihovna sama nemá závislost na Springu. Úplný čistý JDK příklad je `examples/ViesDemoServer.java`:

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java 8085
curl "http://localhost:8085/vat-check?number=DE000000000"
```

## 4. Requester, cache a Redis

```java
var client = ViesClient.builder().defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER"))).build();
```

Requester je vaše DIČ. Cache hit nese původní `requestDate` a `consultationNumber`, ne nový důkaz; pravidla čerstvosti stavte na `fromCache`, TTL nebo `disableCache()`.

Implementace `ViesCache` musí být thread-safe, s krátkým timeoutem, verzovaným namespace, metrikami, bez nekonečného retry a s úplnou serializací všech polí. Výjimka při čtení vrací `CACHE_ERROR` bez fallbacku na VIES; chyba zápisu nemaže již potvrzený `Valid`.

## 5. Retry a více uzlů

Ve velkém provozu: úlohu nejprve persistujte, použijte idempotency key, malý počet lokálních retry, pro `retryable()` naplánujte exponenciálně opožděný retry a po maximu DLQ. `Invalid` a nezměněný `MalformedInput` se neopakují.

Každá replika má vlastní pool, cache, single-flight a semafory; `maxConcurrentRequests(32)` není globální limit 32. Potřebujete trvalou dělenou frontu, omezené consumer okno, distribuovaný limiter, sdílenou cache, deduplikaci a autoscaling podle stáří fronty.

## 6. Health, observability a produkční checklist

- Liveness nesmí záviset na VIES; `availability()` volejte řídce a cacheujte.
- Měřte p50/p95/p99, typ výsledku/`errorCode`, cache hit, retry, overload, queue age a DLQ.
- VAT, jméno a adresu v logu maskujte.
- Nepoužívejte klienta per request, neomezené future, okamžitý retry všech chyb ani živý VIES v unit testech.
- Ověřte singleton lifecycle, všechny čtyři výsledky, HU/EN chyby, sync/async/network limity, globální limiter, politiku čerstvosti, idempotenci, alerty a failure/load/soak testy.

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
