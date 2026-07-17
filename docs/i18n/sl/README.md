# Slovenščina (sl) — vies-client — preverjanje ID za DDV

**Iskalni izrazi:** preverjanje ID za DDV, validator ID za DDV, preverjanje DDV v EU, preverjanje davčne številke, odjemalec Java VIES; `VAT checker`, `VAT number validator`, `EU VAT validation`, `tax ID checker`.

To ni splošni davčni kalkulator, temveč odjemalec za preverjanje identifikacijskih številk za DDV v sistemu EU VIES.

[Vsi jeziki](../../LANGUAGES.md)

**Jeziki:** [English](../../../README.md) · [Čeština](../cs/README.md) · [Polski](../pl/README.md) · [Slovenčina](../sk/README.md) · [Slovenščina](README.md) · [Hrvatski](../hr/README.md) · [Română](../ro/README.md) · [Български](../bg/README.md) · [Ελληνικά](../el/README.md)

> Informativni prevod. Ob razhajanju velja angleški tehnični in pravni izvirnik. Edino zavezujoče licenčno besedilo je korenski [LICENSE](../../../LICENSE); prevod ni licenca.

`vies-client` je samostojen odjemalec Java 21+ brez izvajalnih odvisnosti za REST API VIES Evropske komisije. Deluje s Spring Boot, Quarkus, Micronaut in JDK `HttpServer`, samo z `java.net.http`.

VIES: <https://ec.europa.eu/taxation_customs/vies/#/technical-information>. Končna točka: `https://ec.europa.eu/taxation_customs/vies/rest-api/ms/{countryCode}/vat/{vatNumber}`.

**Dokumenti:** [Namestitev](INSTALLATION.md) · [Integracija](INTEGRATION.md) · [Tehnika](TECHNICAL.md) · [Testi](TESTING.md) · [Odprta koda](OPEN_SOURCE.md) · [Izdaje](RELEASING.md)

```bash
./mvnw clean verify
./mvnw install
```

```xml
<dependency><groupId>vies.client</groupId><artifactId>vies-client</artifactId><version>1.0.0</version></dependency>
```

JPMS: `requires vies.client;`; podprt je tudi classpath.

```java
try (var vies = ViesClient.builder().defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER"))).retries(1).build()) {
  switch (vies.check("DE000000000")) {
    case ViesResponse.Valid v -> System.out.println(v.traderName().orElse("?"));
    case ViesResponse.Invalid i -> System.out.println("INVALID");
    case ViesResponse.Unavailable u -> System.out.println(u.errorCode());
    case ViesResponse.MalformedInput m -> System.out.println(m.reason());
  }
}
```

`defaultRequester` je lastna ID za DDV, prebrana prek `MY_EU_VAT_NUMBER` iz
zaupanja vredne skrivnosti ali konfiguracije. VIES **lahko** vrne
`consultationNumber`, vendar je podatek neobvezen, nikoli zagotovljen, njegova pravna
dokazna vrednost pa je odvisna od lokalnih pravil. Cache hit predstavlja prvotno posvetovanje.

`checkAsync()` uporablja virtual threads. Delite en thread-safe singleton in pokličite `close()`. Pending/network omejitve zagotavljajo backpressure, single-flight združi enake zahteve. Za milijone opravil uporabite durable partitioned queue, omejene workerje, Redis, distributed limiter, delayed retry in DLQ. `Unavailable` ni `Invalid`.

Rezultati: `Valid`/`Invalid`/`Unavailable`/`MalformedInput`; `error()` daje code, `messageHu`, `messageEn`, `retryable`. HTTP 200/400/429/503. Cache hrani le `Valid`; read failure je `CACHE_ERROR`. Grčija je `EL` (`GR` se preslika), Severna Irska `XI`.

Projekt uporablja samo **Apache License 2.0**, ni uraden izdelek EU. Testi: `./mvnw test`.

## Celotna konfiguracija in operativna semantika

Pot zahteve: offline normalizacija → cache → JVM-local single-flight → bounded admission → shared HTTP client → strict validation → zapis samo potrjenega Valid.

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
| `disableCache()` | — | brez shranjenega cache; enaki vzporedni klici lahko delijo en single-flight |
| `userAgent(String)` | module ID | identify the integration |
| `executor(ExecutorService)` | virtual thread/task | caller owns custom executor lifecycle |

Unavailable pomeni brez odločitve in ni Invalid. Format check ni VIES potrditev. Retry poveča obremenitev; lokalno naj bo majhen, pri skali uporabite delayed queue. Cached consultationNumber/requestDate pripada prvotni kontroli. `disableCache()` izključi samo shranjeni cache: enaki sočasni klici VAT+requester lahko delijo eno omrežno zahtevo single-flight; poznejši klic po njenem zaključku pošlje novo zahtevo.

| Result | HTTP | Retry | Cache |
|---|---:|---:|---:|
| `Valid` | 200 | no | yes |
| `Invalid` | 200 | no | no |
| `MalformedInput` | 400 | no | no |
| `Unavailable(CLIENT_OVERLOADED)` | 429 | delayed | no |
| other `Unavailable` | 503 | by `error.retryable()` | no |

## Spring Boot, odzivi napak in velika arhitektura

Odjemalec je immutable in thread-safe. Ustvarite en singleton bean za življenjsko dobo aplikacije in ga ob zaustavitvi zaprite. Izrecno obravnavajte vse štiri rezultate; za logiko uporabite stabilni errorCode, uporabniku pa messageHu/messageEn.

```java
@Configuration
class ViesConfig {
  @Bean(destroyMethod = "close")
  ViesClient viesClient() {
    return ViesClient.builder()
      .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
      .maxConcurrentRequests(32)
.maxPendingSyncRequests(512)
.maxPendingAsyncRequests(512)
.retries(1).build();
  }
}
```

```java
return switch (vies.check(number)) {
 case ViesResponse.Valid v -> ResponseEntity.ok(Map.of("valid", true));
 case ViesResponse.Invalid i -> ResponseEntity.ok(Map.of("valid", false));
 case ViesResponse.MalformedInput m -> problem(400, m);
 case ViesResponse.Unavailable u -> problem(
   "CLIENT_OVERLOADED".equals(u.errorCode()) ? 429 : 503, u);
};
```

Milijonska topologija: API ingress → durable partitioned queue → bounded consumer window → worker JVMs → shared Redis → distributed/global rate limiter → VIES. Retryable rezultati gredo v delayed queue, po max attempts v DLQ. Per-JVM semaphore in single-flight nista globalna.

A durable queue consumer must keep a bounded active window. Autoscaling should follow queue age, not unlimited direct VIES concurrency. Liveness must not depend on VIES; availability() should be sparse and cached.

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
