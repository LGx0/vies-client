# Slovenčina (sk) — Integrácia

**Jazyky:** [English](../../INTEGRATION.md) · [Čeština](../cs/INTEGRATION.md) · [Polski](../pl/INTEGRATION.md) · [Slovenčina](INTEGRATION.md) · [Slovenščina](../sl/INTEGRATION.md) · [Hrvatski](../hr/INTEGRATION.md) · [Română](../ro/INTEGRATION.md) · [Български](../bg/INTEGRATION.md) · [Ελληνικά](../el/INTEGRATION.md)

> Informatívny preklad; rozhoduje anglický originál. Záväzný licenčný text: [LICENSE](../../../LICENSE).

Použite jeden thread-safe `ViesClient` na proces, nie na request. `close()` preruší internú prácu a shared requests ukončí `CLIENT_CLOSED`; nové sync/async volania vyhodia synchronne `IllegalStateException`.

```java
var client = ViesClient.builder().connectTimeout(Duration.ofSeconds(5))
 .requestTimeout(Duration.ofSeconds(8)).admissionTimeout(Duration.ofSeconds(2))
 .maxConcurrentRequests(32).maxPendingSyncRequests(512).maxPendingAsyncRequests(512)
 .retries(1).build();
```

`check()`/`checkAsync()` vracajú štyri varianty. Async používa virtual threads; identické VAT/requester zdieľajú leader, cancel followera ostatných neruší. Držte len obmedzené okno futures.

| Výsledok | HTTP | Retry |
|---|---:|---|
| `Valid`/`Invalid` | 200 | nie |
| `MalformedInput` | 400 | nie |
| `CLIENT_OVERLOADED` | 429 | delayed |
| iné `Unavailable` | 503 | `error.retryable()` |

JSON chyby vracia `errorCode`, `messageHu`, `messageEn`, `retryable`; logika používa stabilný kód.

V Spring Boot použite `@Bean(destroyMethod = "close")`, controller musí spracovať všetky varianty. Plain JDK príklad je `examples/ViesDemoServer.java`.

Requester je vlastné IČ DPH, načítané cez `MY_EU_VAT_NUMBER` z dôveryhodného secretu alebo konfigurácie. VIES môže vrátiť `consultationNumber`, ale údaj je voliteľný, nezaručený a jeho právna hodnota závisí od miestnych pravidiel. Cache hit má pôvodný `requestDate`/`consultationNumber`. `disableCache()` vypne iba uloženú cache: rovnaké súbežné volania VAT+requester môžu zdieľať jednu požiadavku single-flight; neskoršie volanie po dokončení odošle novú požiadavku. `ViesCache` musí byť thread-safe, mať krátky timeout, versioned namespace, metrics, bounded retry a úplnú serializáciu. Read exception → `CACHE_ERROR` bez fallbacku; write exception zachová `Valid`.

Vo veľkom: persist job + idempotency key, malé local retry, exponential delayed retry, max attempts, DLQ. Každý pod má vlastné limity, preto treba globálny limiter, Redis, durable queue a dedup.

Liveness nesmie závisieť od VIES. Merajte p50/p95/p99, výsledok/`errorCode`, cache, retry, overload, queue age, DLQ; maskujte VAT/meno/adresu. Produkčný checklist: singleton lifecycle, 4 výsledky, HU/EN errors, všetky limity, globálny limiter, freshness policy, idempotency, alerts a load/soak/failure testy.

## Úplný aplikačný adaptér

Klient je immutable a thread-safe. Vytvorte jeden singleton bean na celý život aplikácie a zatvorte ho pri shutdown. Všetky štyri výsledky spracujte explicitne; pre logiku používajte stabilný errorCode, používateľovi vráťte messageHu/messageEn.

### Sync и async result handling

```java
switch (client.check("DE000000000")) {
 case ViesResponse.Valid v -> use(v);
 case ViesResponse.Invalid i -> recordInvalid(i);
 case ViesResponse.Unavailable u -> {
   var e = u.error().orElseThrow();
   scheduleOnlyWhenRetryable(e.code(), e.retryable());
 }
 case ViesResponse.MalformedInput m -> rejectInput(m.reason());
}
client.checkAsync("PL0000000000").thenAccept(this::handle);
```

### Spring Boot

```java
@Bean(destroyMethod = "close")
ViesClient viesClient(ViesCache cache) {
 return ViesClient.builder().cache(cache)
.connectTimeout(Duration.ofSeconds(5))
.requestTimeout(Duration.ofSeconds(8))
.admissionTimeout(Duration.ofSeconds(2))
.maxConcurrentRequests(32)
.maxPendingSyncRequests(512)
.maxPendingAsyncRequests(512)
.retries(1).build();
}
```

### Redis adapter contract

```java
final class RedisViesCache implements ViesCache {
 public Optional<ViesResponse.Valid> get(String key) {
   return redis.get("vies:v1:" + key).map(this::decode);
 }
 public void put(String key, ViesResponse.Valid value, Duration ttl) {
   redis.set("vies:v1:" + key, encode(value), ttl);
 }
}
```

Cache ukladá iba Valid. Kľúč obsahuje target aj requester VAT. Cache hit má fromCache=true a pôvodný requestDate/consultationNumber. Read failure v distributed cache vracia CACHE_ERROR bez VIES fallbacku; write failure nesmie zmazať potvrdený Valid.

Milliónová topológia: API ingress → durable partitioned queue → bounded consumer window → worker JVMs → shared Redis → distributed/global rate limiter → VIES. Retryable výsledky idú do delayed queue, po max attempts do DLQ. Per-JVM semaphore ani single-flight nie je globálny.

Produkčný checklist: singleton lifecycle; explicitné spracovanie všetkých štyroch výsledkov; stabilný kód a HU/EN správy; bounded ingress, sync, async a sieť; globálny limiter; schválená freshness policy; delayed retry, maximálny počet pokusov, idempotency a DLQ; p50/p95/p99, maskované logy; liveness nezávislý od VIES; unit, integration, load, soak a failure testy.

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
