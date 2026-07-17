# Čeština (cs) — Technická dokumentace

**Jazyky:** [English](../../TECHNICAL.md) · [Čeština](TECHNICAL.md) · [Polski](../pl/TECHNICAL.md) · [Slovenčina](../sk/TECHNICAL.md) · [Slovenščina](../sl/TECHNICAL.md) · [Hrvatski](../hr/TECHNICAL.md) · [Română](../ro/TECHNICAL.md) · [Български](../bg/TECHNICAL.md) · [Ελληνικά](../el/TECHNICAL.md)

> Informativní překlad; při rozporu platí anglický technický/právní originál. Jediný závazný text licence je [LICENSE](../../../LICENSE).

## Účel, modul a výsledky

Java 21 knihovna bez běhových závislostí je komponentou systému, nikoli náhradou fronty, distribuovaného limiteru či sdílené cache. Modul `vies.client` exportuje veřejné typy `ViesClient`, `ViesResponse`, `ViesError`, `VatFormat`, `ViesRequester`, `ViesAvailability`, `ViesCache`, `ViesException`; interní `MiniJson` a `TtlCache` nejsou exportovány.

`Valid` a `Invalid` jsou autoritativní rozhodnutí, `Unavailable` znamená bez rozhodnutí a `MalformedInput` chybný vstup. Kritický invariant: `Unavailable` se nikdy nesmí změnit na `Invalid`.

## Cesta požadavku a konkurence

1. `VatFormat` odstraní povolené oddělovače, převede na velká písmena a validuje formát země.
2. Sync čte cache na volajícím vlákně; async až po bounded admission ve workeru.
3. JVM-lokální `inFlight` slučuje stejnou kombinaci VAT/requester.
4. `maxPendingSyncRequests` a `maxPendingAsyncRequests` omezují paměť; `requestSlots` omezuje HTTP.
5. HTTP používá sdílený JDK `HttpClient`, časové limity a přísné mapování JSON.
6. Jen potvrzený `Valid` se zapisuje do cache.

Výchozí async executor je virtual-thread-per-task. Cancel jednoho followera neruší společnou operaci. `admissionTimeout` zabraňuje nekonečnému čekání. Všechny limity i single-flight jsou pouze lokální pro JVM.

## Retry, cache a validace odpovědi

Je povoleno 0–5 lokálních retry s exponenciálním backoffem a jitterem: `retryDelay × 2^(attempt-1) + random`. Lokálně se opakují jen přechodné síťové/VIES chyby, nikoli `CLIENT_OVERLOADED`, `CLIENT_CLOSED` ani chybný vstup.

Výchozí cache je concurrent TTL, 10 000 položek, 24 hodin. Klíč obsahuje cílové i requester DIČ. Čtení sdílené cache při chybě vrací `CACHE_ERROR` (anti-stampede); chyba zápisu nemění autoritativní výsledek.

Externí JSON je nedůvěryhodný. `Valid`/`Invalid` vznikne jen z objektu s pravým boolean `isValid`/`valid`, platným ISO-8601 `requestDate` a bez přepisujícího `userError`. Jinak `MALFORMED_RESPONSE`; nikdy se nevymýšlí lokální čas ani `Invalid`.

## Ukončení a topologie

`close()` je idempotentní, odmítne novou práci, přeruší interní async operace a zavře HTTP klienta; caller-provided executor nezavírá. Terminalizace leader future probíhá mimo lifecycle lock, takže callback nezpůsobí deadlock.

Pro více uzlů: API ingress → trvalá dělená fronta → omezené worker JVM → sdílený Redis → distribuovaný rate limiter → VIES; retry jde do delayed queue/DLQ. Upstream kapacita je tvrdý limit.

## Observability, výkon a bezpečnost

Měřte počty podle výsledku/`errorCode`, p50/p95/p99, cache hit/`CACHE_ERROR`, pending/overload, retry, queue/DLQ, dostupnost dle země, heap/GC/virtuální vlákna/CPU/sockety.

Lokální JDK 21 loopback mikroměření (2026-07-17, medián 3 běhů): cache hit 8,91 M/s; lokální odmítnutí formátu 9,02 M/s; sekvenční HTTP 4 044/s; 5 000 async při concurrency 256: 21 640/s; 10 000 stejných klíčů: 1 HTTP požadavek. Není to SLA ani příslib VIES throughput.

V produkci používejte oficiální HTTPS URL, `baseUrl` nedávejte uživateli, citlivá data maskujte a logujte stabilní kódy.

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
