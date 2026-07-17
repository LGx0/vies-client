# Čeština (cs) — Testování

**Jazyky:** [English](../../TESTING.md) · [Čeština](TESTING.md) · [Polski](../pl/TESTING.md) · [Slovenčina](../sk/TESTING.md) · [Slovenščina](../sl/TESTING.md) · [Hrvatski](../hr/TESTING.md) · [Română](../ro/TESTING.md) · [Български](../bg/TESTING.md) · [Ελληνικά](../el/TESTING.md)

> Informativní překlad; rozhodující je anglický technický/právní originál. [LICENSE](../../../LICENSE) se jako závazná licence nepřekládá.

Unit test izolovaně ověřuje třídu/pravidlo bez sítě, DB a živého VIES. Timeout, retry, single-flight, cancel a `close()` navíc vyžadují deterministické lokální HTTP/konkurenční testy s loopback mockem.

## Příkazy

```bash
./mvnw test
./mvnw -Dtest=VatFormatTest,ViesRequesterTest,ViesResponseMappingTest,ViesErrorTest,ViesAvailabilityTest,ViesClientBuilderTest,MiniJsonTest,TtlCacheTest test
./mvnw -Dtest=ViesClientHttpTest test
./mvnw clean verify
./mvnw -Dtest=ViesClientHttpTest#cancellationDoesNotLeakAsyncCapacity test
```

Aktuálně je **73 deterministických testů**: 44 unit v osmi třídách a 29 lokálních HTTP/integration/concurrency; žádné povinné externí volání.

## Unit katalog

- `VatFormatTest` (8): normalizace, oddělovače, `GR→EL`, chybný vstup, formáty zemí, pair API, podporované kódy, všech 28 tvarů.
- `ViesRequesterTest` (4): full VAT, řecká kanonizace, pair konstruktor, fail-fast.
- `ViesResponseMappingTest` (11): GET/POST valid, invalid, chyby, non-object JSON, chybějící/špatný boolean a čas, offset→UTC.
- `ViesErrorTest` (6): HU/EN zprávy, permanentní input, HTTP retry klasifikace, no-error rozhodnutí, úplný katalog, bezpečný unknown.
- `ViesAvailabilityTest` (2): defensive immutable map a odmítnutí null.
- `MiniJsonTest` (4): běžný/nested JSON, escapes, fail-closed malformed/trailing input.
- `TtlCacheTest` (6): TTL hit/miss/hranice, non-positive TTL, limit, expired-first, 32 virtuálních vláken.
- `ViesClientBuilderTest` (3): high-load konfigurace a validace URL/limit/retry/duration/overflow.

## Lokální HTTP a životní cyklus (29)

Pokrytí zahrnuje 503 retry→success; 200 stejných async→1 HTTP; limit aktivních HTTP; async/sync backpressure; cancel bez úniku permitu; callback `close()` bez deadlocku; admission timeout; `CACHE_ERROR` bez fallbacku; uvolnění single-flight/permitů; sync/async cache-close race; custom executor interruption a queued cancellation; shodný leader/follower výsledek; followeři bez extra slotu; rejection cleanup; oba směry sync↔async single-flight; cache leadership recheck; cache-write close race; chained unique async; blokující callback; fatální `Error` do future i uncaught handleru.

## Živé, load, soak a CI

Živý VIES nesmí být povinnou CI bránou. Opt-in smoke: maximálně jedna `availability()` a jedna kontrola, concurrency=1, retries=0; nikdy necommitujte soukromé requester DIČ.

Load testujte pouze lokální mock/vlastní staging: různé klíče, hot-key stampede, overload/recovery, směs 200/429/503/timeout/malformed a tlak na cache. Pro soak/chaos spusťte 30–60 minut s omezeným heapem, JFR, lifecycle restarty, latency spike, reset, cache failure a cancel; heap, vlákna, in-flight a sockety musí dosáhnout plata.

```bash
JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=target/vies-soak.jfr,settings=profile" ./mvnw test
./mvnw --batch-mode clean verify
```

Každá opravená chyba musí mít deterministický regresní test, který před opravou selhal. U concurrency preferujte latch/barrier; pevný `sleep` nesmí být orákulem správnosti.

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
