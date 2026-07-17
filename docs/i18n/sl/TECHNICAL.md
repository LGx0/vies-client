# Slovenščina (sl) — Tehnična dokumentacija

**Jeziki:** [English](../../TECHNICAL.md) · [Slovenščina](TECHNICAL.md) · [Drugi prevodi](../cs/TECHNICAL.md)

> Informativni prevod; velja angleški izvirnik in [LICENSE](../../../LICENSE).

Java 21 modul `vies.client` brez runtime deps izvaža javni API; `MiniJson`/`TtlCache` sta interna. Ni nadomestilo za queue, global limiter ali shared cache. `Valid`/`Invalid` sta odločitev, `Unavailable` pomeni brez odločitve, `MalformedInput` nepravilen vnos; `Unavailable` se ne sme pretvoriti v `Invalid`.

Tok: normalize → cache → JVM single-flight → bounded admission → shared `HttpClient` → strict JSON mapping → cache samo `Valid`. Sync cache bere caller, async worker. Pending limiti varujejo pomnilnik, `maxConcurrentRequests` omrežje, `admissionTimeout` čakanje. Async uporablja virtual threads; cancel followerja ne prekine leaderja.

Retry 0–5 z exponential backoff+jitter le za transient napake. Default cache 10.000/24 h, ključ target+requester; read failure `CACHE_ERROR`, write failure ohrani odločitev. JSON zahteva object, pravi boolean, ISO-8601 `requestDate`, brez overriding `userError`; sicer `MALFORMED_RESPONSE`.

`close()` je idempotent, prekine interno delo, zapre HTTP, ne caller executorja in callbacke konča zunaj lifecycle locka.

Topologija: ingress → durable queue → bounded workers → Redis → distributed limiter → VIES + delayed/DLQ. Merite result/code, p50/95/99, cache, pending, retry, queue, country, JVM.

JDK 21 loopback 2026-07-17: cache 8,91 M/s; format 9,02 M/s; sync HTTP 4.044/s; async 21.640/s; 10.000 same-key → 1 HTTP. Ni SLA. Uporabljajte official HTTPS, maskirajte podatke, `baseUrl` naj ne bo user input.

## Javni modul, invariant in zaustavitev

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

| Rezultat | Pomen | Retry | Cache |
|---|---|---:|---:|
| `Valid` | VIES je potrdil veljavnost | no | yes |
| `Invalid` | VIES veljavnosti ni potrdil | no | no |
| `Unavailable` | Brez odločitve; retry glede na kodo | by code | no |
| `MalformedInput` | Napačen vnos; brez retry | no | no |

### Retry

The client allows 0–5 local retries:

```text
delay ~= retryDelay × 2^(attempt-1) + random(0 .. delay/2)
```

CLIENT_OVERLOADED, CLIENT_CLOSED and input errors are not retried locally. At scale the durable delayed queue is the primary retry mechanism.

### Cache and strict mapping

Cache hrani samo Valid. Ključ vsebuje target in requester VAT. Cache hit ima fromCache=true ter prvotni requestDate/consultationNumber. Read failure distributed cache vrne CACHE_ERROR brez VIES fallbacka; write failure ne izbriše potrjenega Valid.

Zunanji JSON ni zaupanja vreden. Valid/Invalid zahteva object, pravi boolean isValid/valid, veljaven ISO-8601 requestDate in brez overriding userError. Sicer MALFORMED_RESPONSE, nikoli izmišljeni Invalid ali lokalni timestamp.

### Shutdown and observability

close() je idempotent, ne sprejema novega dela, prekine interne naloge in shared leaders konča kot CLIENT_CLOSED zunaj lifecycle locka. Caller-provided executorja ne zapre. Novi sync/async klic po close sinhrono vrže IllegalStateException.

Measure counts by result/errorCode, p50/p95/p99, cache hit/CACHE_ERROR, pending/CLIENT_OVERLOADED, retry outcomes, queue depth/age/DLQ, country availability, heap/GC/virtual threads/CPU/sockets. Official HTTPS only; mask VAT/name/address and never take baseUrl from user input.

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
