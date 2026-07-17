# Nederlands (nl) — INTEGRATION

> [Alle talen](../../LANGUAGES.md) · Informatieve vertaling. Bij verschillen is de canonieke Engelse technische of juridische bron leidend. Alleen `LICENSE` en `NOTICE` in de hoofdmap zijn juridisch gezaghebbend; deze vertaling vervangt ze niet.

## 1. Levenscyclus / Levenscyclus

Gebruik één`ViesClient`-object in een toepassing of werkexemplaar.
Maak geen client aan per HTTP-verzoek: u verliest dan de verbindingspool, cache,
samenvoeging van één vlucht en lokale limieten.

Gebruik één`ViesClient` per applicatie/werkproces. Maak geen klant per aan
HTTP-verzoek; Als u dit doet, worden verbindingspooling, cache, single-flight en lokaal verwijderd
grenzen.

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

Na`close()`kan er geen nieuwe bewerking worden gestart. Uitschakeling onderbreekt de actieve interne
bewerkingen en retourneert`CLIENT_CLOSED`naar veelvoorkomende verzoeken tijdens het proces.
De synchronisatie- en asynchrone methoden die direct daarna worden aangeroepen, zijn beide synchroon
Er wordt een`IllegalStateException`-uitzondering gegenereerd.

Na`close()`wordt geen nieuw werk meer geaccepteerd. Uitschakeling onderbreekt actieve interne werkzaamheden
en voltooit gedeelde verzoeken tijdens de vlucht met`CLIENT_CLOSED`.
Nieuwe synchronisatie- en asynchrone API-aanroepen die daarna worden gedaan, genereren beide`IllegalStateException`
synchroon.

## 2. Synchrone API / Synchrone API

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

Ook het aantal synchrone API-callers is beperkt (`maxPendingSyncRequests`). Op de limiet
bovenstaand geldig verzoek krijgt onmiddellijk het`CLIENT_OVERLOADED`-resultaat.

Synchrone bellers zijn ook gebonden aan`maxPendingSyncRequests`.

## 3. Asynchrone API / Asynchrone API

```java
client.checkAsync("PL0000000000")
        .thenAccept(response -> {
            // Handle all four response variants / Kezeld mind a négy eredményt.
        });
```

De asynchrone API draait op een virtuele thread met de standaarduitvoerder; eigen executeur
de planningsregels zijn van toepassing. Identiteitsbelastingnummer + aanvraagverzoeken zijn één
ze verbinden zich met een interne gedeelde toekomst. De beller ontvangt een annulatiebestendige kopie:
de opzegging van een enkele consument onderbreekt het gezamenlijke verzoek van de anderen niet.

Met de standaarduitvoerder draait de asynchrone API op virtuele threads; een op maat gemaakte executeur
hanteert een eigen planningsbeleid. Identieke BTW-/aanvrageroproepen worden samengevoegd tot één intern gesprek
gedeelde toekomst. Elke beller ontvangt een annuleringsveilige kopie.

Bewaar geen miljoenen toekomstscenario's in het geheugen. De aanhoudende wachtrijconsument is altijd beperkt
venster actief houden.

Houd geen miljoenen futures vast. Een consument met een duurzame wachtrij moet een grens aanhouden
verwerkingsvenster.

## 4. HTTP API-contract / HTTP API-contract

Aanbevolen mapping:

| ViesReactie                      | HTTP |                 Opnieuw | Opmerking / Opmerking           |
| -------------------------------- | ---: | ----------------------: | ------------------------------- |
| `Valid`                          |  200 |                     nee | domein resultaat                |
| `Invalid`                        |  200 |                     nee | domeinresultaat, geen HTTP-fout |
| `MalformedInput`                 |  400 |                     nee | beller moet invoer repareren    |
| `Unavailable(CLIENT_OVERLOADED)` |  429 |               vertraagd | lokale tegendruk                |
| andere`Unavailable`              |  503 | door`error.retryable()` | geen geldigheidsbeslissing      |

Voorbeeldfout JSON / Voorbeeldfout JSON:

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

Gebruikerstekst`messageHu`/`messageEn`. Voor log-, metrische en clientlogica
gebruik altijd de stabiele waarde`errorCode`.

`messageHu`/`messageEn`zijn op de gebruiker gericht. Gebruik stabiele`errorCode`-waarden voor logboeken,
statistieken en clientlogica.

## 5. Lentelaars

### Configuratie

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

### Controleur

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

De bibliotheek heeft geen Spring-afhankelijkheden; de bovenstaande code maakt deel uit van de verbruikende applicatie.
De bibliotheek is niet afhankelijk van Spring; deze adapter hoort bij de consumerende app.

## 6. Gewoon JDK HTTP-server

Het volledige uitvoerbare voorbeeld is`examples/ViesDemoServer.java`.
Volledig uitvoerbaar voorbeeld:`examples/ViesDemoServer.java`.

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java 8085
curl "http://localhost:8085/vat-check?number=DE000000000"
```

In het voorbeeld worden virtuele threads, echte HTTP-statussen en tweetalige foutreacties gebruikt.
In het voorbeeld worden virtuele threads, betekenisvolle HTTP-statussen en tweetalige fouten gebruikt.

## 7. Aanvrager en consult-ID

```java
var client = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
        .build();
```

De aanvrager moet het eigen EU-btw-nummer van de organisatie zijn, via
`MY_EU_VAT_NUMBER` geladen uit een vertrouwd geheim of configuratie. Bij een geldig
antwoord **kan** VIES `requestIdentifier`/`consultationNumber` retourneren, maar dit
is optioneel, nooit gegarandeerd en de juridische bewijskracht hangt af van lokale regels.

Bij een cachetreffer horen het nummer en `requestDate` bij de oorspronkelijke controle.
`disableCache()` schakelt alleen opgeslagen caching uit: identieke gelijktijdige
VAT+requester-oproepen kunnen één single-flight-netwerkaanvraag delen; een latere
oproep na voltooiing voert een nieuwe aanvraag uit.

## 8. Redis-cacheadapter

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

Vereisten / Vereisten:

- draadveilige implementatie;
- korte time-out voor verbinding/opdracht;
- Versiebeheerde sleutelnaamruimte;
- statistieken en gezondheidswaarschuwingen;
- geen onbegrensde interne nieuwe poging;
- correcte serialisatie van`requestDate`, optionele onderdelen en`fromCache`.

Leesuitzondering veroorzaakt resultaat`CACHE_ERROR`en activeert geen VIES-fallback.
Een schrijfuitzondering verwijdert het reeds ontvangen authentieke`Valid`-resultaat niet.

Een leesuitzondering retourneert`CACHE_ERROR`zonder een VIES-fallback. Een schrijfuitzondering
wist een gezaghebbend`Valid`-resultaat niet.

## 9. Opnieuw proberen, wachtrij is DLQ / Opnieuw proberen, wachtrij en DLQ

Bij grootschalige verwerking:

1. volhard de taak voordat deze wordt verwerkt;
2. gebruik een idempotentiesleutel;
3. bel VIES met een klein aantal lokale nieuwe pogingen;
4. indien`error.retryable()`, een uitgestelde nieuwe poging met exponentiële vertraging plannen;
5. stop na een geconfigureerd maximum en ga naar DLQ/handmatige beoordeling;
6. Probeer`Invalid`of`MalformedInput`nooit opnieuw, ongewijzigd.

Bewaar geen duurzame nieuwe pogingen in het geheugen met de keten`CompletableFuture`.
Implementeer geen duurzame nieuwe pogingen als`CompletableFuture`-ketens in het geheugen.

## 10. Multiple-knooppunt/pod / Meerdere knooppunten of pods

Elke replica heeft zijn eigen verbindingspool, cache, single-flight-tabel en semafoor
krijgen.`maxConcurrentRequests(32)`is daarom **niet globaal 32**.

Elke replica heeft zijn eigen verbindingspool, cache, tabel met één vlucht en
seinpalen.`maxConcurrentRequests(32)`is daarom **geen globale limiet van 32**.

Vereiste componenten op grote schaal / Vereist op schaal:

- duurzame gepartitioneerde wachtrij;
- begrensd aantal consumenten/venster;
- gedistribueerde/globale snelheidsbegrenzer;
- gedeelde cache waar de zakelijke semantiek dit toelaat;
- vertraagde nieuwe poging en DLQ;
- idempotentie/deduplicatie;
- automatisch schalen op basis van wachtrijleeftijd, niet directe onbeperkte VIES-gelijktijdigheid.

## 11. Gezondheid is waarneembaar

- Levendigheid mag niet afhankelijk zijn van VIES.
- `availability()`zou een zeldzame, in de cache opgeslagen diagnostische / poll moeten zijn en deze spaarzaam in de cache opslaan.
- Voeg een p50/p95/p99-latentie toe / Meet de p50/p95/p99-latentie.
- Tel op resultaattype en`errorCode`/ Tel op resultaat- en foutcode.
- Meet cachetreffers, nieuwe pogingen, overbelasting, wachtrijleeftijd en DLQ.
- Meet cachehits, nieuwe pogingen, overbelasting, wachtrijleeftijd en DLQ-grootte.
- Masker BTW/naam/adres in logboeken.

## 12. Antipatronen / te vermijden oplossingen

- klant per aanvraag / nieuwe klant per aanvraag;
- onbeperkte futures of synchronisatie-bellers
- een per-pod limiter als globaal behandelen / een lokale limiter als globaal beschouwen;
- elke fout onmiddellijk opnieuw proberen
- `Unavailable`omzetten naar`Invalid`;
- het behandelen van opgeslagen consultatiegegevens als nieuw bewijs;
- live VIES aanroepen vanuit liveness-checks of standaard unit-tests.

## 13. Productiechecklist

- [ ] Singleton-levenscyclus en afsluiten zijn geconfigureerd.
- [ ] Alle vier de`ViesResponse`-varianten worden expliciet behandeld.
- [ ] API retourneert stabiele code plus HU/EN-berichten.
- [ ] Synchronisatie-, async-, inkomend- en netwerklimieten zijn beperkt.
- [ ] Verkeer tussen meerdere knooppunten gebruikt een globale/gedistribueerde limiter.
- [ ] Cacheversheid en consultatiebestendig beleid zijn goedgekeurd.
- [ ] Er zijn vertraagde nieuwe pogingen, maximale pogingen, idempotentie en DLQ.
- [ ] Er bestaan ​​statistieken, waarschuwingen en gemaskeerde logboeken.
- [ ] Levendigheid is onafhankelijk van VIES.
- [ ] Er zijn eenheids-, lokale integratie-, gelijktijdigheids-, belastings-, soak- en faaltests gedefinieerd.
