# Slovenščina (sl) — Integracija

**Jeziki:** [English](../../INTEGRATION.md) · [Slovenščina](INTEGRATION.md) · [Drugi prevodi](../cs/INTEGRATION.md)

> Informativni prevod; velja angleški izvirnik in [LICENSE](../../../LICENSE).

Uporabite en thread-safe `ViesClient` na proces. `close()` prekine interno delo in vrne `CLIENT_CLOSED`; poznejši klici vržejo `IllegalStateException`.

```java
var client = ViesClient.builder().connectTimeout(Duration.ofSeconds(5))
 .requestTimeout(Duration.ofSeconds(8)).admissionTimeout(Duration.ofSeconds(2))
 .maxConcurrentRequests(32).maxPendingSyncRequests(512).maxPendingAsyncRequests(512)
 .retries(1).build();
```

`check()` in `checkAsync()` vrneta štiri variante; async uporablja virtual threads in cancellation-safe single-flight. Ne hranite milijonov futures.

HTTP mapping: `Valid`/`Invalid` 200, `MalformedInput` 400, `CLIENT_OVERLOADED` 429, drugi `Unavailable` 503 po `retryable()`. Error JSON naj ima `errorCode`, `messageHu`, `messageEn`, `retryable`.

Spring: singleton `@Bean(destroyMethod = "close")`, controller obravnava vse variante. Plain JDK: `examples/ViesDemoServer.java`.

Requester je lastna ID za DDV, prebrana prek `MY_EU_VAT_NUMBER` iz zaupanja vredne skrivnosti ali konfiguracije. VIES lahko vrne `consultationNumber`, vendar je podatek neobvezen, nezagotovljen, njegova pravna vrednost pa je odvisna od lokalnih pravil. Cached `requestDate`/`consultationNumber` pripada prvotni kontroli. `disableCache()` izključi samo shranjeni cache: enaki sočasni klici VAT+requester lahko delijo eno zahtevo single-flight; poznejši klic po zaključku pošlje novo zahtevo. `ViesCache` mora biti thread-safe, bounded, merjen in pravilno serializiran; read exception → `CACHE_ERROR` brez VIES fallback, write exception ohrani `Valid`.

Na skali: persist job/idempotency, malo local retry, exponential delayed retry, max attempts, DLQ; global limiter + Redis + durable queue + dedup, ker so limiti per-pod.

Liveness ni odvisen od VIES. Merite p50/95/99, result/code, cache, retry, overload, queue/DLQ; maskirajte osebne podatke. Pred produkcijo preverite lifecycle, 4 rezultate, HU/EN napake, vse omejitve, freshness, observability ter load/soak/failure teste.

## Celoten aplikacijski adapter

Odjemalec je immutable in thread-safe. Ustvarite en singleton bean za življenjsko dobo aplikacije in ga ob zaustavitvi zaprite. Izrecno obravnavajte vse štiri rezultate; za logiko uporabite stabilni errorCode, uporabniku pa messageHu/messageEn.

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

Cache hrani samo Valid. Ključ vsebuje target in requester VAT. Cache hit ima fromCache=true ter prvotni requestDate/consultationNumber. Read failure distributed cache vrne CACHE_ERROR brez VIES fallbacka; write failure ne izbriše potrjenega Valid.

Milijonska topologija: API ingress → durable partitioned queue → bounded consumer window → worker JVMs → shared Redis → distributed/global rate limiter → VIES. Retryable rezultati gredo v delayed queue, po max attempts v DLQ. Per-JVM semaphore in single-flight nista globalna.

Produkcijski checklist: singleton lifecycle; izrecna obravnava vseh štirih rezultatov; stabilna koda ter HU/EN sporočila; bounded ingress, sync, async in omrežje; globalni limiter; potrjena freshness policy; delayed retry, največ poskusov, idempotency in DLQ; p50/p95/p99 ter zakriti logi; liveness neodvisen od VIES; unit, integration, load, soak in failure testi.

## Celovit operativni priročnik

### Življenjski cikel odjemalca in model odločitev

V aplikaciji ali worker procesu ustvarite natanko eno instanco `ViesClient`. Skupni odjemalec ponovno uporablja povezave JDK `HttpClient`, lokalni TTL cache, tabelo single-flight in vse admission omejitve. Ustvarjanje odjemalca za vsak HTTP request te zaščite izniči ter poveča število povezav in porabo pomnilnika. Instanca je thread-safe. Ob nadzorovani zaustavitvi vedno pokličite `close()`, uporabite `try`-with-resources ali Spring `@Bean(destroyMethod = "close")`. Po zaprtju novi sync in async klici sinhrono vržejo `IllegalStateException`, skupne operacije v teku pa se končajo s strukturiranim `CLIENT_CLOSED`.

Sealed hierarhija loči poslovno odločitev od tehnične negotovosti. `Valid` pomeni, da je VIES veljavnost potrdil, `Invalid` da je ni potrdil, `MalformedInput` da vnosa ni mogoče poslati, `Unavailable` pa da odločitev ni bila sprejeta. `Unavailable` se nikoli ne sme pretvoriti v `Invalid`. Za programsko logiko, metrike in retry uporabljajte stabilna `error.code()` in `error.retryable()`, ne lokaliziranega besedila. Uporabniku lahko vrnete `messageHu` in `messageEn`.

### Normalizacija, requester in revizijski dokaz

`VatFormat` odstrani dovoljene presledke, pike in vezaje, črke spremeni v velike ter pred omrežjem preveri obliko države. To je le preverjanje formata, ne potrditev veljavnosti. Grški `GR` se kanonizira v VIES `EL`, Severna Irska uporablja `XI`. `defaultRequester` naj bo lastna identifikacijska številka za DDV organizacije. VIES lahko pri veljavnem rezultatu vrne neobvezen, nezagotovljen `consultationNumber`; njegova pravna vrednost je odvisna od lokalnih pravil. Pri cache hit pa `consultationNumber` in `requestDate` pripadata prvotnemu preverjanju.

### Admission, async izvajanje in single-flight

`maxPendingSyncRequests` omejuje sočasne sync klicalce, `maxPendingAsyncRequests` edinstvene async leaderje, `maxConcurrentRequests` pa dejanske odhodne HTTP requeste. Presežen pending limit vrne `CLIENT_OVERLOADED`; čakanje na network slot upošteva `admissionTimeout`. Privzeti async executor ustvari virtual thread za sprejeto nalogo. Zunanjega executorja odjemalec ne zapre, saj njegov lifecycle pripada aplikaciji. Preklic future enega followerja ne prekliče skupne operacije.

Single-flight znotraj enega JVM združi sočasne requeste z enako kombinacijo target DDV in requester DDV. Stotine ali tisoči followerjev lahko delijo en upstream request in ne porabijo ločenih pending slotov. Mehanizem ni distribuiran. Vsak pod ima svojo tabelo, semaphore, connection pool in pomnilniški cache, zato vsota per-pod omejitev ni globalna omejitev VIES.

### Cache, retry in zaščita pred stampede

Vgrajeni cache je concurrent ter časovno in velikostno omejen. Shrani samo avtoritativni `Valid`; `Invalid`, napačen vnos in tehnična napaka se ne shranijo. Zunanji `ViesCache`, na primer Redis adapter, mora biti thread-safe, imeti kratek connection/command timeout, versioned namespace ter popolno serializacijo datuma, optionals in cache zastavice. Izjema pri branju vrne `CACHE_ERROR` brez VIES fallbacka, da izpad Redis ne sproži stampede. Napaka pisanja po uspehu ne sme izbrisati avtoritativnega `Valid`.

Lokalni retry je namenoma majhen, največ pet poskusov, z exponential backoff in jitter. Ponovijo se samo prehodne network/VIES napake. `CLIENT_OVERLOADED`, `CLIENT_CLOSED`, blokade in input errors se lokalno ne ponavljajo. V velikem sistemu je glavni mehanizem durable delayed queue z idempotency key, največjim številom poskusov in DLQ. Takojšnji retry vseh workerjev lahko izpad upstreama poslabša.

### Strogo preslikovanje, varnost in opazljivost

Odziv VIES je nezaupanja vreden zunanji JSON. Avtoritativna odločitev zahteva root object, pravi boolean `isValid` ali `valid`, veljaven ISO-8601 `requestDate` in brez nadrejenega `userError`. Manjkajoč ali tekstovni boolean in neveljaven timestamp povzročijo `MALFORMED_RESPONSE`; odjemalec ne izmisli lokalnega časa ali `Invalid`. Produkcija uporablja uradni HTTPS endpoint. Testni `baseUrl` ne sme izvirati iz uporabniškega vnosa. V logih zakrijte DDV, ime, naslov in requester.

Merite rezultate po tipu in `errorCode`, celotno ter upstream p50/p95/p99 latency, cache hit ratio, `CACHE_ERROR`, `CLIENT_OVERLOADED`, retry outcomes, globino in starost queue, delayed retry in DLQ, napake po državah, heap, GC, virtual threads, CPU in sockete. Liveness ne sme biti odvisen od VIES; `availability()` naj bo redka cacheirana diagnostika.

### Arhitektura milijonskih paketov in preverjanje

Produkcijski tok: API ingress → durable partitioned queue → bounded consumer window → worker JVMs → shared Redis → distributed rate limiter → EU in državne storitve VIES. Autoscaling vodite po starosti queue, ne po neomejeni VIES concurrency. Virtual threads znižajo ceno čakanja, vendar ne povečajo upstream kapacitete. Lokalni benchmark ni SLA ali priporočena evropska omejitev.

Pred uvedbo zaženite `./mvnw --batch-mode clean verify`. Unit testi pokrivajo format, requester, mapping, errors, availability, JSON, TTL cache in builder. Lokalna HTTP/concurrency suite deterministično preveri retry, omejitve, cancellation, close races, custom executor, obe smeri sync/async single-flight, cache anti-stampede in fatal `Error`. Live VIES ne sme biti obvezna CI vrata ali cilj load testa; load izvajajte na mocku ali lastnem stagingu. Soak/chaos naj teče 30–60 minut z bounded heapom in JFR, viri pa morajo doseči stabilen plato.
