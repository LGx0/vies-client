# Slovenčina (sk) — Technická dokumentácia

**Jazyky:** [English](../../TECHNICAL.md) · [Čeština](../cs/TECHNICAL.md) · [Polski](../pl/TECHNICAL.md) · [Slovenčina](TECHNICAL.md) · [Slovenščina](../sl/TECHNICAL.md) · [Hrvatski](../hr/TECHNICAL.md) · [Română](../ro/TECHNICAL.md) · [Български](../bg/TECHNICAL.md) · [Ελληνικά](../el/TECHNICAL.md)

> Informatívny preklad; platí anglický originál. Licencia je iba [LICENSE](../../../LICENSE).

Java 21 modul `vies.client` bez runtime deps exportuje `ViesClient`, `ViesResponse`, `ViesError`, `VatFormat`, `ViesRequester`, `ViesAvailability`, `ViesCache`, `ViesException`; `MiniJson`/`TtlCache` sú interné. Nie je náhradou queue, globálneho limitera ani shared cache.

`Valid`/`Invalid` sú rozhodnutia, `Unavailable` znamená bez rozhodnutia, `MalformedInput` chybný vstup. `Unavailable` sa nesmie zmeniť na `Invalid`.

Tok: normalize → cache → JVM single-flight → bounded admission → shared `HttpClient` → strict JSON mapping → cache iba `Valid`. Sync cache číta caller, async bounded worker. Pamäť chránia pending limity, sieť `maxConcurrentRequests`, čakanie `admissionTimeout`. Async je virtual-thread-per-task; cancel followera neruší leader.

Retry 0–5, exponential backoff+jitter, len transient network/VIES. Default cache: concurrent, 10 000 entries, 24 h, key target+requester; read failure `CACHE_ERROR`, write failure nemení `Valid`. JSON musí byť object, boolean `isValid`/`valid`, valid ISO-8601 `requestDate`, bez overriding `userError`; inak `MALFORMED_RESPONSE`.

`close()` je idempotentný, preruší internú prácu, zavrie HTTP a nie caller executor; callbacky sa dokončia mimo lifecycle locku.

Topológia: ingress → durable partitioned queue → bounded workers → Redis → distributed limiter → VIES + delayed retry/DLQ. Merajte result/code, p50/95/99, cache, pending, retry, queue, country, JVM.

JDK 21 loopback mikromeranie 2026-07-17: cache 8,91 M/s; format 9,02 M/s; sync HTTP 4 044/s; async 21 640/s; 10 000 same-key → 1 HTTP. Nie je SLA. Používajte official HTTPS, maskujte dáta a `baseUrl` neprijímajte od používateľa.

## Verejný modul, invariants a shutdown

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

| Výsledok | Význam | Retry | Cache |
|---|---|---:|---:|
| `Valid` | VIES potvrdil platnosť | no | yes |
| `Invalid` | VIES nepotvrdil platnosť | no | no |
| `Unavailable` | Bez rozhodnutia; retry podľa kódu | by code | no |
| `MalformedInput` | Chybný vstup; bez retry | no | no |

### Retry

The client allows 0–5 local retries:

```text
delay ~= retryDelay × 2^(attempt-1) + random(0 .. delay/2)
```

CLIENT_OVERLOADED, CLIENT_CLOSED and input errors are not retried locally. At scale the durable delayed queue is the primary retry mechanism.

### Cache and strict mapping

Cache ukladá iba Valid. Kľúč obsahuje target aj requester VAT. Cache hit má fromCache=true a pôvodný requestDate/consultationNumber. Read failure v distributed cache vracia CACHE_ERROR bez VIES fallbacku; write failure nesmie zmazať potvrdený Valid.

Externý JSON je nedôveryhodný. Valid/Invalid vyžaduje object, skutočný boolean isValid/valid, platný ISO-8601 requestDate a žiadny overriding userError. Inak sa vracia MALFORMED_RESPONSE, nikdy vymyslený Invalid alebo lokálny timestamp.

### Shutdown and observability

close() je idempotentný, neprijíma novú prácu, preruší interné úlohy a shared leaders dokončí ako CLIENT_CLOSED mimo lifecycle locku. Caller-provided executor nezatvára. Nové sync aj async volanie po close vyhodí synchronne IllegalStateException.

Measure counts by result/errorCode, p50/p95/p99, cache hit/CACHE_ERROR, pending/CLIENT_OVERLOADED, retry outcomes, queue depth/age/DLQ, country availability, heap/GC/virtual threads/CPU/sockets. Official HTTPS only; mask VAT/name/address and never take baseUrl from user input.

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
