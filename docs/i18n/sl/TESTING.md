# Slovenščina (sl) — Testiranje

**Jeziki:** [English](../../TESTING.md) · [Slovenščina](TESTING.md) · [Drugi prevodi](../cs/TESTING.md)

> Informativni prevod; velja angleški izvirnik in [LICENSE](../../../LICENSE).

Unit test izolira pravilo brez omrežja/DB/live VIES; retry, timeout, single-flight, cancel in close zahtevajo tudi lokalne deterministične concurrency teste.

```bash
./mvnw test
./mvnw -Dtest=VatFormatTest,ViesRequesterTest,ViesResponseMappingTest,ViesErrorTest,ViesAvailabilityTest,ViesClientBuilderTest,MiniJsonTest,TtlCacheTest test
./mvnw -Dtest=ViesClientHttpTest test
./mvnw clean verify
```

Skupaj **73**: 44 unit v 8 razredih + 29 HTTP/integration/concurrency, brez obveznega zunanjega klica. Unit katalog: format 8, requester 4, mapping 11, errors 6, availability 2, JSON 4, TTL cache 6, builder 3. Pokriti so normalizacija vseh držav, strict response mapping, HU/EN katalog, TTL/concurrency in overflow validacija.

Integracija pokriva retry, same-key one-HTTP, sync/async/network limits, cancellation recovery, close races/deadlock, timeout, cache anti-stampede/write failure, custom executor interruption/rejection, leader/follower equality, oba sync↔async single-flight, chained calls in fatal `Error` propagation.

Živi VIES ni CI vrata; po izbiri največ ena kontrola dostopnosti in ena validacija, `concurrency=1`, `retries=0`. Load samo proti mocku ali stagingu, vključno z distinct keys, hot-key, overload, mešanimi odzivi in cache pressure. Soak/chaos traja 30–60 minut z bounded heap, JFR, restarti in failures; viri morajo doseči plato.

`./mvnw --batch-mode clean verify`. Vsak bug dobi determinističen regression test; uporabite latch/barrier, ne `sleep` kot oracle.

## Celoten katalog in razlaga testov

Unit test izolira pravilo brez omrežja. Lokalni HTTP/concurrency test uporablja naključni loopback port in deterministic latch/barrier; public VIES se v default suite ne kliče.

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

Živi VIES je samo izbirni smoke test: največ ena dostopnost in ena kontrola, `concurrency=1`, `retries=0`, nikoli zasebni requester podatki. Load test cilja lokalni mock ali lastni staging, ne javnega VIES. Soak/chaos traja 30–60 minut z bounded heap, JFR, ponovljenim lifecycle, latency spike, resetom, cache failures in cancellation; heap, niti, in-flight in socketi morajo doseči plato.

```bash
JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=target/vies-soak.jfr,settings=profile" ./mvnw test
```

Every fixed bug requires a deterministic regression test. Use latch/barrier for races; fixed Thread.sleep is not a correctness oracle.

## Celovit operativni priročnik

### Življenjski cikel odjemalca in model odločitev

V aplikaciji ali worker procesu ustvarite natanko eno instanco `ViesClient`. Skupni odjemalec ponovno uporablja povezave JDK `HttpClient`, lokalni TTL cache, tabelo single-flight in vse admission omejitve. Ustvarjanje odjemalca za vsak HTTP request te zaščite izniči ter poveča število povezav in porabo pomnilnika. Instanca je thread-safe. Ob nadzorovani zaustavitvi vedno pokličite `close()`, uporabite `try`-with-resources ali Spring `@Bean(destroyMethod = "close")`. Po zaprtju novi sync in async klici sinhrono vržejo `IllegalStateException`, skupne operacije v teku pa se končajo s strukturiranim `CLIENT_CLOSED`.

Sealed hierarhija loči poslovno odločitev od tehnične negotovosti. `Valid` pomeni, da je VIES veljavnost potrdil, `Invalid` da je ni potrdil, `MalformedInput` da vnosa ni mogoče poslati, `Unavailable` pa da odločitev ni bila sprejeta. `Unavailable` se nikoli ne sme pretvoriti v `Invalid`. Za programsko logiko, metrike in retry uporabljajte stabilna `error.code()` in `error.retryable()`, ne lokaliziranega besedila. Uporabniku lahko vrnete `messageHu` in `messageEn`.

### Normalizacija, requester in revizijski dokaz

`VatFormat` odstrani dovoljene presledke, pike in vezaje, črke spremeni v velike ter pred omrežjem preveri obliko države. To je le preverjanje formata, ne potrditev veljavnosti. Grški `GR` se kanonizira v VIES `EL`, Severna Irska uporablja `XI`. `defaultRequester` naj bo lastna identifikacijska številka za DDV organizacije. VIES lahko pri veljavnem rezultatu vrne `consultationNumber`. Pri cache hit pa `consultationNumber` in `requestDate` pripadata prvotnemu preverjanju. Če proces zahteva svež dokaz, upoštevajte `fromCache`, skrajšajte TTL ali cache izključite.

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
