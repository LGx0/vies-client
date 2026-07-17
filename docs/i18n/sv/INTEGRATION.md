# Svenska (sv) — INTEGRATION

> [Alla språk](../../LANGUAGES.md) · Informativ översättning. Vid avvikelser gäller den kanoniska engelska tekniska eller juridiska källan. Endast `LICENSE` och `NOTICE` i roten är juridiskt auktoritativa; översättningen ersätter dem inte.

## 1. Livscykel / livscykel

Använd ett enda`ViesClient`-objekt i en applikations- eller arbetsinstans.
Skapa inte en klient per HTTP-begäran: du skulle förlora anslutningspoolen, cachen,
sammanslagning av ett flyg och lokala gränser.

Använd en`ViesClient` per ansökan/arbetarprocess. Skapa inte en klient per
HTTP-begäran; Om du gör det förkastas anslutningspooling, cache, enkelflyg och lokalt
gränser.

```java
var client = ViesClient.builder()
        .connectTimeout(Duration.ofSeconds(5))
        .requestTimeout(Duration.ofSeconds(8))
        .admissionTimeout(Duration.ofSeconds(2))
        .maxConcurrentRequests(32)
        .maxPendingSyncRequests(512)
        .maxPendingAsyncRequests(512)
        .retries(1)
        .build();

// Application shutdown / Alkalmazásleállítás
client.close();
```

En ny operation kan inte startas efter`close()`. Avstängning avbryter den aktiva interna
operationer och returnerar`CLIENT_CLOSED`till vanliga förfrågningar under processen.
Synk- och asynkronmetoderna som anropas direkt efteråt är båda synkrona
Ett`IllegalStateException`-undantag kastas.

Efter`close()`accepteras inget nytt arbete. Avstängning avbryter aktivt internt arbete
och slutför delade förfrågningar ombord med`CLIENT_CLOSED`.
Nya synkroniserings- och asynkron-API-anrop som görs efteråt ger båda`IllegalStateException`
synkront.

## 2. Synchronous API / Synchronous API

```java
ViesResponse response = client.check("DE 000 000 000");

switch (response) {
    case ViesResponse.Valid valid ->
        System.out.println("VALID: " + valid.vatNumber());
    case ViesResponse.Invalid invalid ->
        System.out.println("INVALID: " + invalid.vatNumber());
    case ViesResponse.Unavailable unavailable -> {
        var error = unavailable.error().orElseThrow();
        System.out.println(error.messageHu());
        System.out.println(error.messageEn());
    }
    case ViesResponse.MalformedInput malformed -> {
        var error = malformed.error().orElseThrow();
        System.out.println(error.messageHu());
        System.out.println(error.messageEn());
    }
}
```

Antalet synkrona API-anropare är också begränsat (`maxPendingSyncRequests`). På gränsen
ovanstående giltiga begäran får omedelbart`CLIENT_OVERLOADED`-resultat.

Synkrona uppringare är också tillträdesbegränsade av`maxPendingSyncRequests`.

## 3. Asynkront API / Asynkront API

```java
client.checkAsync("PL0000000000")
        .thenAccept(response -> {
            // Handle all four response variants / Kezeld mind a négy eredményt.
        });
```

Asynkron-API:et körs på en virtuell tråd med standardexekutorn; egen exekutor
dess schemaläggningsregler gäller. Identiskt skattenummer + förfrågningar är en
de ansluter till en intern gemensam framtid. Den som ringer får en avbokningssäker kopia:
uppsägningen av en enda konsument avbryter inte de andras gemensamma begäran.

Med standardexekutorn körs async API på virtuella trådar; en anpassad utförare
använder sin egen schemaläggningspolicy. Identiska moms/begäranrop ansluter till en intern
gemensam framtid. Varje uppringare får en avbokningssäker kopia.

Lagra inte miljontals terminer i minnet. Den ihärdiga kökonsumenten är alltid begränsad
håll fönstret aktivt.

Behåll inte miljontals terminer. En konsument med hållbar kö måste hålla en gräns
bearbetningsfönstret.

## 4. HTTP API-kontrakt / HTTP API-kontrakt

Rekommenderad kartläggning:

| ViesResponse                     | HTTP |           Försök igen | Anmärkning / Anmärkning          |
| -------------------------------- | ---: | --------------------: | -------------------------------- |
| `Valid`                          |  200 |                   nej | domänresultat                    |
| `Invalid`                        |  200 |                   nej | domänresultat, inte ett HTTP-fel |
| `MalformedInput`                 |  400 |                   nej | den som ringer måste fixa input  |
| `Unavailable(CLIENT_OVERLOADED)` |  429 |              försenad | lokalt mottryck                  |
| andra`Unavailable`               |  503 | av`error.retryable()` | inget giltighetsbeslut           |

Exempelfel JSON / Exempelfel JSON:

```json
{
  "status": "UNAVAILABLE",
  "vatNumber": "HU00000000",
  "errorCode": "MS_UNAVAILABLE",
  "messageHu": "A tagállami adóhatóság rendszere átmenetileg nem érhető el.",
  "messageEn": "The member state's tax system is temporarily unavailable.",
  "retryable": true
}
```

Användartext`messageHu`/`messageEn`. För log-, metrisk- och klientlogik
använd alltid det stabila värdet`errorCode`.

`messageHu`/`messageEn`är användarinriktade. Använd stabila`errorCode`-värden för loggar,
mätvärden och klientlogik.

## 5. Fjäderkänga

### Konfiguration

```java
@Configuration
class ViesConfiguration {
    @Bean(destroyMethod = "close")
    ViesClient viesClient(ViesCache cache) {
        return ViesClient.builder()
                .cache(cache)
                .connectTimeout(Duration.ofSeconds(5))
                .requestTimeout(Duration.ofSeconds(8))
                .admissionTimeout(Duration.ofSeconds(2))
                .maxConcurrentRequests(32)
                .maxPendingSyncRequests(512)
                .maxPendingAsyncRequests(512)
                .retries(1)
                .build();
    }
}
```

### Styrenhet

```java
@RestController
@RequestMapping("/api/v1/vat")
class VatController {
    private final ViesClient vies;

    VatController(ViesClient vies) {
        this.vies = vies;
    }

    @GetMapping("/{vatNumber}")
    ResponseEntity<?> check(@PathVariable String vatNumber) {
        return switch (vies.check(vatNumber)) {
            case ViesResponse.Valid v -> ResponseEntity.ok(Map.of(
                    "status", "VALID",
                    "vatNumber", v.vatNumber(),
                    "name", v.traderName().orElse(""),
                    "address", v.traderAddress().orElse(""),
                    "requestDate", v.requestDate().toString(),
                    "fromCache", v.fromCache()));
            case ViesResponse.Invalid i -> ResponseEntity.ok(Map.of(
                    "status", "INVALID",
                    "vatNumber", i.vatNumber(),
                    "requestDate", i.requestDate().toString()));
            case ViesResponse.MalformedInput m -> problem(400, m);
            case ViesResponse.Unavailable u -> problem(
                    "CLIENT_OVERLOADED".equals(u.errorCode()) ? 429 : 503, u);
        };
    }

    private static ResponseEntity<?> problem(int status, ViesResponse response) {
        var error = response.error().orElseThrow();
        return ResponseEntity.status(status).body(Map.of(
                "errorCode", error.code(),
                "retryable", error.retryable(),
                "messageHu", error.messageHu(),
                "messageEn", error.messageEn()));
    }
}
```

Biblioteket har inga Spring-beroenden; koden ovan är en del av den konsumerande applikationen.
Biblioteket har inget Spring-beroende; denna adapter tillhör den konsumerande appen.

## 6. Vanlig JDK HTTP-server

Det fullständiga exemplet är`examples/ViesDemoServer.java`.
Fullständigt körbart exempel:`examples/ViesDemoServer.java`.

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java 8085
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Exemplet använder virtuella trådar, riktiga HTTP-statusar och tvåspråkiga felsvar.
Exemplet använder virtuella trådar, meningsfulla HTTP-statusar och tvåspråkiga fel.

## 7. Begäran och konsultations-ID

```java
var client = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
        .build();
```

Beställaren ska vara organisationens eget EU-momsnummer, inläst via
`MY_EU_VAT_NUMBER` från en betrodd hemlighet eller konfiguration. Vid ett giltigt
svar **kan** VIES returnera `requestIdentifier`/`consultationNumber`, men fältet är
valfritt, aldrig garanterat och dess juridiska bevisvärde beror på lokala regler.

Vid en cacheträff hör identifieraren och `requestDate` till den ursprungliga kontrollen.
`disableCache()` stänger bara av lagrad cache: identiska samtidiga VAT+requester-anrop
kan dela en single-flight-nätverksbegäran; ett senare anrop efter slutförandet gör en
ny begäran.

## 8. Redis cache-adapter

```java
final class RedisViesCache implements ViesCache {
    private final RedisClient redis; // application-specific adapter

    RedisViesCache(RedisClient redis) {
        this.redis = redis;
    }

    @Override
    public Optional<ViesResponse.Valid> get(String key) {
        // Use a short, bounded Redis timeout / Használj rövid Redis timeoutot.
        return redis.get("vies:v1:" + key).map(this::decode);
    }

    @Override
    public void put(String key, ViesResponse.Valid value, Duration ttl) {
        redis.set("vies:v1:" + key, encode(value), ttl);
    }

    // encode/decode are application-specific and must preserve every record field.
}
```

Krav / Krav:

- trådsäker implementering;
- kort anslutning/kommando timeout;
- versionerad nyckelnamnrymd;
- statistik och hälsovarningar;
- inget obegränsat internt försök;
- korrekt serialisering av`requestDate`, tillval och`fromCache`.

Läsundantag orsakar resultatet`CACHE_ERROR`och utlöser inte VIES-fallback.
Ett skrivundantag tar inte bort det redan mottagna autentiska`Valid`-resultatet.

Ett läsundantag returnerar`CACHE_ERROR`utan en VIES-återgång. Ett skrivundantag
raderar inte ett auktoritativt`Valid`-resultat.

## 9. Försök igen, kö eller DLQ / Försök igen, kö och DLQ

I högskalig bearbetning:

1. bevara jobbet före bearbetning;
2. använd en idempotensnyckel;
3. ring VIES med litet antal lokala försök;
4. om`error.retryable()`, schemalägg försenat försök med exponentiell fördröjning;
5. stoppa efter ett konfigurerat maximum och gå till DLQ/manuell granskning;
6. Försök aldrig igen`Invalid`eller`MalformedInput`oförändrad.

Förvara inte hållbara återförsök i minnet med kedjan`CompletableFuture`.
Implementera inte hållbara försök som`CompletableFuture`-kedjor i minnet.

## 10. Multiple nod/pod / Multiple noder eller pods

Varje replik har sin egen anslutningspool, cache, enkelflygbord och semafor
få.`maxConcurrentRequests(32)`är därför **inte global 32**.

Varje replik har sin egen anslutningspool, cache, enkelflygbord och
semaforer.`maxConcurrentRequests(32)`är därför **inte en global gräns på 32**.

Required komponenter i stor skala / Required at scale:

- hållbar partitionerad kö;
- begränsat antal konsumenter/fönster;
- distribuerad/global hastighetsbegränsare;
- delad cache där affärssemantik tillåter det;
- fördröjt nytt försök och DLQ;
- idempotens/deduplicering;
- Autoskalning baserat på köålder, inte direkt obegränsad VIES-samtid.

## 11. Hälsa är observerbarhet

– Liveness får inte bero på VIES.

- `availability()`bör vara en sällsynt, cachad diagnostik / poll och cache den sparsamt.
- Merd a p50/p95/p99 latens-t / Mät p50/p95/p99 latens.
- Räkna efter resultattyp och`errorCode`/ Räkna efter resultat och felkod.
- Mät cacheträff, försök igen, överbelastning, köålder och DLQ.
- Mät cacheträffar, återförsök, överbelastningar, köålder och DLQ-storlek.
- Maskera moms/namn/adress i loggar.

## 12. Anti-mönster / lösningar som bör undvikas

- klient per begäran / ny klient per begäran;
- obegränsade terminer eller synka uppringare
- behandla en per-pod limiter som global / se en lokal limiter som global;
- Försöker omedelbart varje fel igen
- konvertera`Unavailable`till`Invalid`;
- behandla cachade konsultationsdata som ett nytt bevis;
- anropa live VIES från liveness kontroller eller standard enhetstester.

## 13. Produktionschecklista

- [ ] Singleton livscykel och avstängning är konfigurerade.
- [ ] Alla fyra`ViesResponse`-varianter hanteras explicit.
- [ ] API returnerar stabil kod plus HU/EN-meddelanden.
- [ ] Synk-, asynkron-, ingångs- och nätverksgränser är begränsade.
- [ ] Trafik med flera noder använder en global/distribuerad limiter.
- [ ] Cache-färskhet och konsultationssäker policy är godkända.
- [ ] Fördröjt nytt försök, maximala försök, idempotens och DLQ finns.
- [ ] Det finns mätvärden, varningar och maskerade loggar.
- [ ] Liveness är oberoende av VIES.
- [ ] Enhet, lokal integration, samtidighet, belastning, blötläggning och feltest definieras.
