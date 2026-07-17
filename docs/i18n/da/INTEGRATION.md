# Dansk (da) — INTEGRATION

`disableCache()` slår lagret/persistent cache fra, men samtidige kald med samme momsnummer + requester kan dele én single-flight-netværksanmodning. Et senere kald efter afslutningen udfører en ny VIES-anmodning. `consultationNumber` er en valgfri værdi, som VIES kan returnere, men den er aldrig garanteret; dens bevisværdi afhænger af lokale regler. Indlæs kun `MY_EU_VAT_NUMBER` fra en betroet secret-/konfigurationskilde.

> [Alle sprog](../../LANGUAGES.md) · Informativ oversættelse. Ved afvigelser er den kanoniske engelske tekniske eller juridiske kilde gældende. Kun `LICENSE` og `NOTICE` i roden er juridisk autoritative; oversættelsen erstatter dem ikke.

## 1. Livscyklus / Livscyklus

Brug et enkelt`ViesClient`-objekt i en applikations- eller arbejdsinstans.
Opret ikke en klient pr. HTTP-anmodning: du ville miste forbindelsespuljen, cachen,
enkeltflyvningssammenlægning og lokale grænser.

Brug én`ViesClient` pr. ansøgning/arbejdsproces. Opret ikke en klient pr
HTTP-anmodning; Dette kasserer forbindelsespooling, cache, enkeltflyvning og lokal
grænser.

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

En ny operation kan ikke startes efter`close()`. Nedlukning afbryder den aktive interne
operationer og returnerer`CLIENT_CLOSED`til almindelige anmodninger i processen.
Synkroniserings- og asynkroniseringsmetoderne, der kaldes direkte bagefter, er begge synkrone
En`IllegalStateException`undtagelse er kastet.

Efter`close()`accepteres intet nyt arbejde. Nedlukning afbryder aktivt internt arbejde
og fuldfører delte anmodninger under flyvningen med`CLIENT_CLOSED`.
Nye synkroniserings- og async-API-kald, der foretages efterfølgende, kaster begge`IllegalStateException`
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

Antallet af synkrone API-kaldere er også begrænset (`maxPendingSyncRequests`). På grænsen
ovenstående gyldige anmodning får straks`CLIENT_OVERLOADED`resultat.

Synkrone opkald er også adgangsbegrænset af`maxPendingSyncRequests`.

## 3. Asynkron API / Asynkron API

```java
client.checkAsync("PL0000000000")
        .thenAccept(response -> {
            // Handle all four response variants / Kezeld mind a négy eredményt.
        });
```

Async API'et kører på en virtuel tråd med standard executor; egen bobestyrer
dens planlægningsregler gælder. Identisk skattenummer + forespørgselsanmodninger er én
de forbinder til en intern fælles fremtid. Den, der ringer op, modtager en aflysningssikker kopi:
annullering af en enkelt forbruger afbryder ikke de andres fælles anmodning.

Med standard executor kører async API'et på virtuelle tråde; en tilpasset eksekutør
bruger sin egen planlægningspolitik. Identiske moms-/anmodningsopkald slutter sig til én intern
fælles fremtid. Hver opkalder modtager en aflysningssikker kopi.

Gem ikke millioner af futures i hukommelsen. Den vedvarende køforbruger er altid begrænset
holde vinduet aktivt.

Behold ikke millioner af futures. En varig-kø forbruger skal holde en bounded
behandlingsvindue.

## 4. HTTP API kontrakt / HTTP API kontrakt

Anbefalet kortlægning:

| ViesResponse                     | HTTP |             Prøv igen | Bemærk / Bemærk                   |
| -------------------------------- | ---: | --------------------: | --------------------------------- |
| `Valid`                          |  200 |                   nej | domæne resultat                   |
| `Invalid`                        |  200 |                   nej | domæneresultat, ikke en HTTP-fejl |
| `MalformedInput`                 |  400 |                   nej | den, der ringer, skal rette input |
| `Unavailable(CLIENT_OVERLOADED)` |  429 |             forsinket | lokalt modtryk                    |
| andet`Unavailable`               |  503 | af`error.retryable()` | ingen gyldighedsbeslutning        |

Eksempel fejl JSON / Eksempel fejl JSON:

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

Brugertekst`messageHu`/`messageEn`. Til log, metrisk og klientlogik
brug altid den stabile værdi`errorCode`.

`messageHu`/`messageEn`er brugervendte. Brug stabile`errorCode`-værdier til logfiler,
målinger og klientlogik.

## 5. Fjederstøvle

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

### Controller

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

Biblioteket har ingen Spring-afhængigheder; koden ovenfor er en del af den forbrugende applikation.
Biblioteket har ingen Spring-afhængighed; denne adapter tilhører den forbrugende app.

## 6. Almindelig JDK HTTP-server

Det fulde eksekverbare eksempel er`examples/ViesDemoServer.java`.
Fuldt kørebart eksempel:`examples/ViesDemoServer.java`.

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java 8085
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Eksemplet bruger virtuelle tråde, rigtige HTTP-statusser og tosprogede fejlsvar.
Eksemplet bruger virtuelle tråde, meningsfulde HTTP-statusser og tosprogede fejl.

## 7. Anmoder- og konsultations-id

```java
var client = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
        .build();
```

Requesteren er organisationens eget momsnummer og skal komme fra en betroet secret-/konfigurationskilde. VIES kan ved en gyldig kontrol returnere et valgfrit `requestIdentifier`/`consultationNumber`, men det er ikke garanteret, og dets bevisværdi afhænger af lokale regler.

Et cachehit indeholder den oprindelige konsultations ID/dato, ikke en ny kontrol. `disableCache()` deaktiverer lagret cache, men samtidige identiske momsnummer+requester-kald kan stadig dele én single-flight-netværksanmodning; et senere kald efter afslutningen sender en ny VIES-anmodning.

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

- trådsikker implementering;
- kort forbindelse/kommando timeout;
- versioneret nøglenavneområde;
- målinger og sundhedsadvarsler;
- intet ubegrænset internt forsøg;
- korrekt serialisering af`requestDate`, ekstraudstyr og`fromCache`.

Læsundtagelse forårsager resultatet`CACHE_ERROR`og udløser ikke VIES-faldback.
En skriveundtagelse sletter ikke det allerede modtagne autentiske`Valid`-resultat.

En læseundtagelse returnerer`CACHE_ERROR`uden et VIES-faldback. En skriveundtagelse
sletter ikke et autoritativt`Valid`-resultat.

## 9. Prøv igen, kø eller DLQ / Prøv igen, kø og DLQ

I højskala forarbejdning:

1. vedblive med jobbet før behandling;
2. brug en idempotensnøgle;
3. ring til VIES med lille lokale genforsøgstælling;
4. hvis`error.retryable()`, planlæg forsinket genforsøg med eksponentiel forsinkelse;
5. stop efter et konfigureret maksimum og flyt til DLQ/manuel gennemgang;
6. prøv aldrig`Invalid`eller`MalformedInput`uændret igen.

Hold ikke holdbare genforsøg i hukommelsen med kæden`CompletableFuture`.
Implementer ikke holdbare genforsøg som`CompletableFuture`-kæder i hukommelsen.

## 10. Multiple node/pod / Flere noder eller pods

Hver replika har sin egen forbindelsespool, cache, enkeltflyvningsbord og semafor
få.`maxConcurrentRequests(32)`er derfor **ikke global 32**.

Hver replika har sin egen forbindelsespool, cache, bord til enkeltflyvning og
semaforer.`maxConcurrentRequests(32)`er derfor **ikke en global grænse på 32**.

Nødvendige komponenter i stor skala / Required at scale:

- holdbar opdelt kø;
- afgrænset forbrugertælling/vindue;
- distribueret/global hastighedsbegrænser;
- delt cache, hvor forretningssemantik tillader det;
- forsinket genforsøg og DLQ;
- idempotens/deduplikation;
- Autoskalering baseret på køens alder, ikke direkte ubegrænset VIES samtidighed.

## 11. Sundhed og observerbarhed

- Liveness må ikke afhænge af VIES.
- `availability()`bør være en sjælden, cachelagret diagnostik/afstemning og cache den sparsomt.
- Mérd a p50/p95/p99 latency-t / Mål p50/p95/p99 latency.
- Tæl efter resultattype og`errorCode`/ Tæl efter resultat og fejlkode.
- Mål cachehit, forsøg igen, overbelastning, køalder og DLQ.
- Mål cachehits, genforsøg, overbelastninger, køens alder og DLQ-størrelse.
- Masker moms/navn/adresse i logfiler.

## 12. Anti-mønstre / løsninger, der skal undgås

- klient pr. anmodning / ny klient pr. anmodning;
- ubegrænset futures eller synkronisering af opkald
- at behandle en per-pod limiter som global / se en lokal limiter som global;
- prøver hver fejl igen med det samme
- konvertering af`Unavailable`til`Invalid`;
- at behandle cachelagrede konsultationsdata som et nyt bevis;
- kalder live VIES fra liveness-tjek eller standardenhedstest.

## 13. Produktionstjekliste

- [ ] Singleton-livscyklus og nedlukning er konfigureret.
- [ ] Alle fire`ViesResponse`varianter håndteres eksplicit.
- [ ] API returnerer stabil kode plus HU/EN-meddelelser.
- [ ] Synkroniserings-, asynkron-, indgangs- og netværksgrænser er afgrænsede.
- [ ] Multi-node trafik bruger en global/distribueret limiter.
- [ ] Cachefriskhed og konsultationssikker politik er godkendt.
- [ ] Forsinket genforsøg, maksimale forsøg, idempotens og DLQ eksisterer.
- [ ] Der findes målinger, advarsler og maskerede logfiler.
- [ ] Liveness er uafhængig af VIES.
- [ ] Test af enhed, lokal integration, samtidighed, belastning, iblødsætning og fejl er defineret.
