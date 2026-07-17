# Slovenčina (sk) — Testovanie

**Jazyky:** [English](../../TESTING.md) · [Čeština](../cs/TESTING.md) · [Polski](../pl/TESTING.md) · [Slovenčina](TESTING.md) · [Slovenščina](../sl/TESTING.md) · [Hrvatski](../hr/TESTING.md) · [Română](../ro/TESTING.md) · [Български](../bg/TESTING.md) · [Ελληνικά](../el/TESTING.md)

> Informatívny preklad; rozhoduje anglický originál. [LICENSE](../../../LICENSE) sa neprekladá ako licencia.

Unit test izoluje pravidlo bez siete/DB/live VIES; retry, timeout, single-flight, cancel a close vyžadujú aj lokálne deterministic HTTP/concurrency testy.

```bash
./mvnw test
./mvnw -Dtest=VatFormatTest,ViesRequesterTest,ViesResponseMappingTest,ViesErrorTest,ViesAvailabilityTest,ViesClientBuilderTest,MiniJsonTest,TtlCacheTest test
./mvnw -Dtest=ViesClientHttpTest test
./mvnw clean verify
```

Spolu **73 testov**: 44 unit (8 tried) + 29 lokálnych HTTP/integration/concurrency; bez povinnej externej siete.

Unit katalóg: `VatFormatTest` 8 (normalizácia a 28 krajín), `ViesRequesterTest` 4, `ViesResponseMappingTest` 11 (strict boolean/timestamp), `ViesErrorTest` 6 (HU/EN/retry/catalog), `ViesAvailabilityTest` 2, `MiniJsonTest` 4, `TtlCacheTest` 6 (TTL/limit/32 virtual threads), `ViesClientBuilderTest` 3 (URL/limity/duration overflow).

29 integračných testov pokrýva 503 retry, same-key single-flight, HTTP/pending/sync limity, cancel permit recovery, callback close, admission timeout, cache anti-stampede, cleanup, sync/async close race, custom executor interruption/queue cancellation, leader/follower rovnosť, rejection cleanup, oba sync↔async smery, cache recheck/write race, chained calls, blocking callback a fatal `Error` propagation.

Živý VIES nesmie byť CI bránou: voliteľne najviac jedna kontrola dostupnosti a jedna validácia, `concurrency=1`, `retries=0`. Load test iba proti mocku alebo stagingu, vrátane distinct keys, hot-key, overload, zmiešaných chýb a cache pressure. Soak/chaos beží 30–60 minút s bounded heap, JFR, restartom, latency spike, resetom, cache failure a cancellation; zdroje musia dosiahnuť plateau.

```bash
JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=target/vies-soak.jfr,settings=profile" ./mvnw test
./mvnw --batch-mode clean verify
```

Každý bug potrebuje deterministic regression test. Použite latch/barrier; pevný `sleep` nesmie byť correctness oracle.

## Úplný katalóg a interpretácia testov

Unit test izoluje pravidlo bez siete. Lokálny HTTP/concurrency test používa náhodný loopback port a deterministic latches/barriers; public VIES sa v default suite nikdy nevolá.

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

Živý VIES je iba voliteľný smoke test: najviac jedna dostupnosť a jedna kontrola, `concurrency=1`, `retries=0`, nikdy súkromné requester dáta. Load test smeruje na lokálny mock alebo vlastný staging, nie na verejný VIES. Soak/chaos trvá 30–60 minút s bounded heap, JFR, opakovaným lifecycle, latency spike, resetom, výpadkom cache a cancellation; heap, vlákna, in-flight a sockety musia dosiahnuť plateau.

```bash
JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=target/vies-soak.jfr,settings=profile" ./mvnw test
```

Every fixed bug requires a deterministic regression test. Use latch/barrier for races; fixed Thread.sleep is not a correctness oracle.

## Kompletná prevádzková príručka

### Životný cyklus klienta a model rozhodnutí

V aplikácii alebo worker procese vytvorte presne jednu inštanciu `ViesClient`. Zdieľaný klient opakovane využíva spojenia JDK `HttpClient`, lokálnu TTL cache, tabuľku single-flight a všetky admission limity. Klient vytváraný pre každý HTTP request tieto ochrany ruší, zvyšuje počet spojení a spotrebu pamäte. Inštancia je thread-safe. Pri riadenom ukončení vždy volajte `close()`, použite `try`-with-resources alebo Spring `@Bean(destroyMethod = "close")`. Po zatvorení nové sync aj async volanie synchronne vyhodí `IllegalStateException`; spoločné rozpracované operácie sa ukončia štruktúrovaným `CLIENT_CLOSED`.

Sealed hierarchia oddeľuje obchodné rozhodnutie od technickej neistoty. `Valid` znamená potvrdenie platnosti systémom VIES, `Invalid` jej nepotvrdenie, `MalformedInput` neopraviteľný vstup pre request a `Unavailable` stav bez rozhodnutia. `Unavailable` sa nikdy nesmie zmeniť na `Invalid`. V programovej logike, metrikách a retry používajte stabilné `error.code()` a `error.retryable()`, nie lokalizovaný text. Používateľovi možno vrátiť `messageHu` a `messageEn`.

### Normalizácia, requester a auditný dôkaz

`VatFormat` odstráni povolené medzery, bodky a pomlčky, písmená zmení na veľké a ešte pred sieťou overí tvar krajiny. Je to iba kontrola formátu, nie potvrdenie platnosti. Grécky prefix `GR` sa kanonizuje na VIES `EL`; Severné Írsko používa `XI`. `defaultRequester` má byť vlastné IČ DPH organizácie. VIES môže pri platnom výsledku vrátiť `consultationNumber`. Pri cache hit však `consultationNumber` a `requestDate` patria pôvodnej kontrole. Ak proces vyžaduje čerstvý dôkaz, vyhodnoťte `fromCache`, skráťte TTL alebo cache vypnite.

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
