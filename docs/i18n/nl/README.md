# Nederlands (nl) — vies-client

> [Alle talen](../../LANGUAGES.md) · Informatieve vertaling. Bij verschillen is de canonieke Engelse technische of juridische bron leidend. Alleen `LICENSE` en `NOTICE` in de hoofdmap zijn juridisch gezaghebbend; deze vertaling vervangt ze niet.

[![Licentie: Apache-2.0](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](../../../LICENSE)
[![Java 21+](https://img.shields.io/badge/Java-21%2B-orange.svg)](../../../pom.xml)
[![Koop een koffie voor mij](https://img.shields.io/badge/Buy_Me_a_Coffee-support-ffdd00?logo=buymeacoffee&logoColor=000)](https://buymeacoffee.com/lgx0)

🌍 **[Alle officiële EU-talen](../../LANGUAGES.md)**

Een stand-alone, **zero runtime dependency** Java-client voor VIES van de Europese Commissie
(VAT Information Exchange System) voor de REST API van een belastingnummerchecker. Elke Java
kan worden aangesloten op een API-server of programma in de volgende talen: Spring Boot, Quarkus, Micronaut,
of zelfs gewone JDK`HttpServer`- de module gebruikt alleen de JDK (`java.net.http`),
er is geen transitieve afhankelijkheidsboom.

**Op andere zoeknamen / Zoekwoorden:** VIES-checker, BTW-checker, BTW-nummer
checker, BTW-nummer validator, EU BTW-validatie, EU belasting-ID checker, belastingnummer
validatie, verificatie van belastingnummers, verificatie van gemeenschapsbelastingnummers, validatie van BTW-nummers.
Dit is geen algemene belastingcalculator: alleen voor VIES-verificatie van EU-belastingnummers
Dit is geen algemene belastingcalculator; het valideert EU-btw-nummers via VIES.

Onafhankelijk open source-project; niet de Europese Commissie, de EU of de lidstaten
is een officieel product van de belastingdienst en wordt niet door hen onderschreven of gecertificeerd.

- **Vereiste:** Java **21+** bytecode/API; het hele pakket is geverifieerd op JDK 21.
- **Officiële VIES-documentatie:** <https://ec.europa.eu/taxation_customs/vies/#/technical-information>
- **Eindpunt genoemd:**`https://ec.europa.eu/taxation_customs/vies/rest-api/ms/{countryCode}/vat/{vatNumber}`
- **Contactpersoon lidstaat:**`https://ec.europa.eu/taxation_customs/vies/rest-api/check-status`

## Documentatie

- [Installatie](INSTALLATION.md)
- [Integratie](INTEGRATION.md)
- [Technisch ontwerp](TECHNICAL.md)
- [Eenheids-, integratie- en concurrentietests](TESTING.md)
- [Open source en licentiekeuze](OPEN_SOURCE.md)
- [Releasehandleiding](RELEASING.md)
- [Publiceren op GitHub](GITHUB_SETUP.md)
- [Bijdragen](CONTRIBUTING.md)
- [Beveiligingsbeleid](SECURITY.md)
- [Gedragscode](CODE_OF_CONDUCT.md)
- [Ondersteuning en donaties](SUPPORT.md)
- [Kennisgevingen van derden](THIRD_PARTY_NOTICES.md)
- [Wijzigingslogboek](CHANGELOG.md)

## Bouw en verbinding

```bash
./mvnw install    # tests + target/vies-client-1.0.0.jar (+ -sources.jar, -javadoc.jar)
               # install into the local Maven repository
```

**Maven:**

```xml
<dependency>
    <groupId>vies.client</groupId>
    <artifactId>vies-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Grade:**

```kotlin
implementation("vies.client:vies-client:1.0.0")
```

**Echte JPMS-module.** De jar gedraagt ​​zich als een benoemde module (`jar --describe-module`):

```
vies.client@1.0.0
exports vies.client
requires java.net.http
contains vies.client.internal   ← non-exported internal package
```

Maak verbinding vanuit een gemodulariseerde applicatie zoals deze:

```java
module my.api.server {
    requires vies.client;
}
```

Vanuit Classpath (in een niet-gemoduleerd, "traditioneel" project) werkt het op dezelfde manier:
`module-info`speelt in dit geval simpelweg niet. Zelfs zonder Maven/Gradle
kan worden gebruikt: de bronboom onder`src/main/java`kan naar uw eigen project worden gekopieerd
(laat`module-info.java`staan ​​als uw project niet gemodulariseerd is).

## Snel voorbeeld

```java
import vies.client.*;

try (var vies = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER"))) // vertrouwd geheim/configuratie
        .retries(1)
        .build()) {

    // Accepts hyphens, spaces, and lowercase; maps GR to EL.
    switch (vies.check("DE 000 000 000")) {
        case ViesResponse.Valid v ->
            System.out.println("Valid: " + v.traderName().orElse("(name not public)")
                    + " — consultation number: " + v.consultationNumber().orElse("-"));
        case ViesResponse.Invalid i ->
            System.out.println("Invalid VAT number: " + i.vatNumber());
        case ViesResponse.Unavailable u ->
            System.out.println("VIES unavailable (" + u.errorCode() + "), retry later");
        case ViesResponse.MalformedInput m ->
            System.out.println("Malformed input: " + m.reason());
    }
}
```

`switch`is volledig: de compiler garandeert dat u alle vier de uitgangen hebt verwerkt
(verzegelde interface + patroonafstemming).

### Waarom is`defaultRequester`belangrijk?

`defaultRequester` hoort het eigen EU-btw-nummer van de organisatie te zijn en uit
een vertrouwd geheim of configuratie te komen. Bij een geldig antwoord **kan** VIES
een `consultationNumber` retourneren; dit veld is optioneel en nooit gegarandeerd.
De juridische bewijskracht hangt af van lokale regels. Bewaar het nummer samen met
de aanvraagdatum en invoer.

## Verbinding met Spring Boot API-server

```java
@Configuration
class ViesConfig {
    @Bean(destroyMethod = "close")
    ViesClient viesClient() {
        return ViesClient.builder()
                .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
                .retries(1)
                .build();
    }
}

@RestController
@RequestMapping("/api/vat")
class VatController {
    private final ViesClient vies;

    VatController(ViesClient vies) { this.vies = vies; }

    @GetMapping("/{number}")
    ResponseEntity<?> check(@PathVariable String number) {
        return switch (vies.check(number)) {
            case ViesResponse.Valid v -> ResponseEntity.ok(Map.of(
                    "valid", true,
                    "name", v.traderName().orElse(""),
                    "address", v.traderAddress().orElse(""),
                    "consultationNumber", v.consultationNumber().orElse("")));
            case ViesResponse.Invalid i -> ResponseEntity.ok(Map.of("valid", false));
            case ViesResponse.Unavailable u -> {
                var error = u.error().orElseThrow();
                yield ResponseEntity.status("CLIENT_OVERLOADED".equals(error.code()) ? 429 : 503)
                        .body(Map.of("errorCode", error.code(), "retryable", error.retryable(),
                                "messageHu", error.messageHu(), "messageEn", error.messageEn()));
            }
            case ViesResponse.MalformedInput m -> {
                var error = m.error().orElseThrow();
                yield ResponseEntity.badRequest().body(Map.of(
                        "errorCode", error.code(), "retryable", error.retryable(),
                        "messageHu", error.messageHu(), "messageEn", error.messageEn()));
            }
        };
    }
}
```

De client is onveranderlijk en thread-safe: de applicatie maakt één exemplaar
levenscyclus (singleton bean) en sluiten bij afsluiten (`close`).

Patroon zonder lijst: [`examples/ViesDemoServer.java`](../../../examples/ViesDemoServer.java)
(gewone JDK`HttpServer`, op virtuele threads):

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java     # port 8085
curl "http://localhost:8085/vat-check?number=HU00000000"
```

## Asynchroon gebruik

```java
CompletableFuture<ViesResponse> future = vies.checkAsync("PL0000000000");
```

Standaard worden asynchrone oproepen uitgevoerd op **virtuele threads** (Project Loom) – veel
er is ook geen verspilling van platformdraad bij parallelle controle. Eigen executeur a
kan worden gespecificeerd in builder (`executor(...)`- het wordt niet gesloten door de klant).

Maximaal 512 **individuele asynchrone leideracties** uit het asynchrone klantenbestand
ontvangen per klantexemplaar. Een enkele cache-lezing neemt korte tijd ruimte in beslag;
de defecte ingang en dezelfde single-flight-volger nemen geen afzonderlijke ruimte in beslag. Bij overbelasting wordt de nieuwe individuele aanvraag onmiddellijk ingediend
U krijgt een resultaat van`Unavailable(..., "CLIENT_OVERLOADED")`. Dit is in de klant
beperkt het werk; de invoerwachtrij en futures die ook door de beller zijn opgeslagen
moet beperkt zijn.

## Multi-miljoen query-installatie

Voor miljoenen gebruikers: start geen miljoenen futures in één JVM, en doe dat ook niet
staat onbeperkt direct verkeer naar VIES toe. De voorgestelde structuur:

> De klant kan een verwerkingscomponent zijn van een miljoen werkwachtrijen, maar dan de werkelijke
> VIES-transmissie wordt beperkt door de veranderende, niet-gegarandeerde grenzen van de systemen van de EU en de lidstaten
> bepaald worden. Virtuele threads verlagen de kosten van wachten; de stroomopwaarts
> capaciteit wordt niet vergroot.

1. Inkomende cheques worden ontvangen door een permanente, gepartitioneerde berichtenwachtrij.
2. Verschillende horizontaal geschaalde werknemers verbruiken de lijn in beperkte porties.
3. Eén singleton`ViesClient`werkt met virtuele threads per werknemer.
4. Werknemers gebruiken een gemeenschappelijke Redis-cache via de`ViesCache`-interface.
5. Mondiale/gedistribueerde snelheidsbegrenzer beschermt VIES-eindpunten in de EU en de lidstaten.

Binnen een JVM voegt de client hetzelfde samen en arriveert op hetzelfde tijdstip
belastingnummer/query-paren (enkele vlucht), dus een cachemisser veroorzaakt dit niet
"verzoek stormloop".`maxConcurrentRequests`wordt beperkt door het echte netwerk
verzoeken, en`maxPendingAsyncRequests`en`maxPendingSyncRequests`in het geheugen
geeft onmiddellijke tegendruk. Ze zijn allemaal JVM-lokaal. Geaggregeerd verkeer van verschillende werknemers
moet worden geregeld met een gemeenschappelijke, gedistribueerde limiter. De persistente invoerwachtrij en de
consumentenpool moet nog steeds worden beperkt door de toepassing.

Voorbeeld van een zwaarlastwerker:

```java
var vies = ViesClient.builder()
        .cache(redisViesCache)
        .cacheTtl(Duration.ofHours(24))
        .maxConcurrentRequests(32)
        .maxPendingSyncRequests(512)
        .maxPendingAsyncRequests(512)
        .admissionTimeout(Duration.ofSeconds(2))
        .retries(2)
        .retryDelay(Duration.ofMillis(250))
        .build();
```

Het resultaat`Unavailable`— inclusief de waarde`CLIENT_OVERLOADED`— is persistent
je moet het opnieuw proberen met een vertraging in de wachtrij.`MalformedInput`en`Invalid`mogen niet opnieuw worden geprobeerd.

## Pad van een aanvraag/levenscyclus van aanvraag

1. **Normaliseren / Normaliseren:** controleer de landcode en het formaat zonder netwerk.
2. **Cache:** Onmiddellijke terugkeer van geldige, nog steeds live resultaten.
3. **Single-flight:** identieke belastingcode+query-paren worden samengevoegd binnen één JVM.
4. **Toelating:** asynchrone en netwerklimieten beschermen het geheugen en VIES.
5. **HTTP:** hergebruikte JDK`HttpClient`-verbinding met korte time-out.
6. **Valideren/antwoordcontrole:** onvolledige booleaanse waarde of datum →`MALFORMED_RESPONSE`.
7. **Schrijven in cache:** alleen het authentieke`Valid`-resultaat wordt in de cache opgeslagen.

## Tweetalige foutreacties / Tweetalige foutreacties

De machinefoutcode is altijd stabiel en taalonafhankelijk.`error()`is Hongaars en Engels
geeft gebruikerstekst en een beslissing om opnieuw te proberen:

```java
var response = vies.check("HU00000000");
response.error().ifPresent(error -> {
    log.warn("{} | {} | retry={}", error.messageHu(), error.messageEn(), error.retryable());
});
```

| Resultaat        |       HTTP |         Opnieuw | Cache | Betekenis                                                                           |
| ---------------- | ---------: | --------------: | ----: | ----------------------------------------------------------------------------------- |
| `Valid`          |        200 |             nee | ja/ja | VIES bevestigd als geldig / VIES bevestigd als geldig                               |
| `Invalid`        |        200 |             nee |   nee | VIES heeft het niet als geldig bevestigd / VIES heeft het niet als geldig bevestigd |
| `Unavailable`    | 503 of 429 | meestal/meestal |   nee | Er is geen geldigheidsbeslissing genomen                                            |
| `MalformedInput` |        400 |             nee |   nee | De invoer moet gecorrigeerd worden / Invoer moet gecorrigeerd worden                |

## Configuratie (bouwer)

| Instelling                        | Basiswaarde             | Wat doet                                                                                             |
| --------------------------------- | ----------------------- | ---------------------------------------------------------------------------------------------------- |
| `baseUrl(String)`                 | officiële VIES REST-URL | Omleiding in proeftest                                                                               |
| `connectTimeout(Duration)`        | 5 sec                   | Time-out voor TCP/TLS-verbinding                                                                     |
| `requestTimeout(Duration)`        | 8 seconden              | Totale time-out van het verzoek (VIES is interactief, houd het kort)                                 |
| `admissionTimeout(Duration)`      | 2 sec                   | Zo lang wacht het op vrije netwerkruimte                                                             |
| `defaultRequester(ViesRequester)` | er is geen              | Eigen gemeenschapsbelastingnummer → consultatie-ID                                                   |
| `retries(int)`                    | 0                       | Automatisch opnieuw proberen bij tijdelijke fouten (0-5, exponentiële uitstel + jitter)              |
| `retryDelay(Duration)`            | 400 ms                  | Standaard exponentieel uitstel                                                                       |
| `maxConcurrentRequests(int)`      | 32                      | Bovengrens van gelijktijdige echte VIES-netwerkverzoeken                                             |
| `maxPendingSyncRequests(int)`     | 512                     | Geheugenlimiet voor gelijktijdige synchronisatie-bellers; erboven`CLIENT_OVERLOADED`                 |
| `maxPendingAsyncRequests(int)`    | 512                     | Geheugenlimiet voor actieve/in behandeling zijnde asynchrone bewerkingen; erboven`CLIENT_OVERLOADED` |
| `cacheTtl(Duration)`              | 24 uur                  | Cachetijd voor geldige treffers                                                                      |
| `cacheMaxEntries(int)`            | 10.000                  | Groottelimiet voor ingebouwde geheugencache                                                          |
| `cache(ViesCache)`                | ingebouwd               | Eigen cache-backend (bijv. Redis)                                                                    |
| `disableCache()`                  | —                       | Geen opgeslagen cache; identieke parallelle oproepen kunnen één single-flight-aanvraag delen         |
| `userAgent(String)`               | module-id               | U dient zich bij de EU te identificeren                                                              |
| `executor(ExecutorService)`       | virtuele draad/taak     | Mijn asynchrone uitvoerder; de beller is verantwoordelijk voor zijn levenscyclus                     |

### Eigen cache (bijv. Redis)

```java
class RedisViesCache implements ViesCache {
    public Optional<ViesResponse.Valid> get(String key) { /* ... */ }
    public void put(String key, ViesResponse.Valid value, Duration ttl) { /* ... */ }
}

var vies = ViesClient.builder().cache(new RedisViesCache()).build();
```

Alleen resultaat`Valid`wordt in de cache opgeslagen: ongeldig nummer en tijdelijke fout nooit.
Gebruik voor de Redis-adapter korte cache-time-out, naamruimte met versiebeheer en eigen
foutmetriek. Cacheleesfout`CACHE_ERROR`ontstaat wanneer Redis niet beschikbaar is
start geen ongecontroleerde VIES-verzoekstorm.

De uit de cache geretourneerde `consultationNumber` en `requestDate` horen bij de
**oorspronkelijke** controle; een cachetreffer is geen nieuwe VIES-controle.
`disableCache()` schakelt alleen opgeslagen caching uit: identieke gelijktijdige
VAT+requester-oproepen kunnen één single-flight-netwerkaanvraag delen, terwijl een
latere oproep na voltooiing een nieuwe aanvraag uitvoert. `consultationNumber` blijft optioneel.

## Semantiek die het waard is om te weten

1. **`Unavailable`≠`Invalid`.** Achtergrondsystemen van de lidstaten regelmatig
   worden verwijderd (`MS_UNAVAILABLE`,`MS_MAX_CONCURRENT_REQ`...). In dit geval is het getal a
   **niet beoordeeld door VIES** — commercieel verboden om als ongeldig te beschouwen.
2. **Voorfiltering van formaten is geen VIES-authenticatie.** De module filtert de geregistreerde eruit
   verkeerde invoer (`MalformedInput`) voordat u naar het netwerk gaat, maar de geldigheid
   de bron is altijd het antwoord van VIES.
3. **De nieuwe poging verhoogt ook de belasting.** Probeer een tijdelijke fout slechts een paar keer lokaal
   opnieuw; bij een grote operatie is het mechanisme voor vertraagde nieuwe pogingen van de duurzame wachtrij het belangrijkste.
4. **Griekenland`EL`, Noord-Ierland`XI`.** De module verwerkt beide,`GR`
   wijst invoer automatisch toe aan`EL`.

## Testen

```bash
./mvnw test    # unit- en lokale HTTP-tests voor gelijktijdigheid, herhaling, tegendruk en levenscyclus
```

---

## Engelse snelstart

Zero-dependency Java 21+ client voor het VIES BTW-nummer van de Europese Commissie
validatie REST API. Bouw met`./mvnw package`en vervolgens:

```java
try (var vies = ViesClient.createDefault()) {
    if (vies.check("DE000000000") instanceof ViesResponse.Valid v) {
        System.out.println(v.traderName().orElse("?"));
    }
}
```

De resultaten zijn een verzegelde hiërarchie (`Valid`/`Invalid`/`Unavailable`/
`MalformedInput`) ontworpen voor uitgebreide patroonafstemming`switch`. Geldig
de resultaten worden 24 uur in het geheugen opgeslagen; tijdelijke VIES-storingen worden gerapporteerd als
`Unavailable`, nooit als`Invalid`. Officiële API-documentatie:
<https://ec.europa.eu/taxation_customs/vies/#/technical-information>.

### Grootschalige operatie

Deze client kan een onderdeel zijn van een verwerkingspijplijn van miljoenen items, maar
de werkelijke VIES-doorvoer wordt begrensd door de variabele, niet-gegarandeerde EU en lidstaat
grenzen. Virtuele threads verlagen de blokkeringskosten; ze stijgen stroomopwaarts niet
capaciteit. Gebruik een duurzame gepartitioneerde wachtrij, begrensde consumenten, een gedeelde Redis
cache en een gedistribueerde snelheidsbegrenzer over alle werknemers. Lokale enkele vlucht en
semaforen beschermen slechts één JVM. Behandel`Unavailable`nooit als`Invalid`; gebruik
`response.error()`voor stabiele codes, herkansing en Hongaars/Engelse berichten.

## Open source-code / Open source

Het project kan worden gebruikt, gewijzigd en
kan worden gedistribueerd. De licentie staat commercieel en uitdrukkelijk gebruik toe
verleent een patentlicentie; licentie-, toeschrijvings- en wijzigingsvoorwaarden moeten worden gelezen
bewaken. Lees voordat u een bijdrage levert [CONTRIBUTING.md](CONTRIBUTING.md)en
[CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)-bestanden.

Dit project is gelicentieerd onder de [Apache-licentie 2.0](../../../LICENSE), een permissieve
licentie met een expliciete octrooiverlening. Zie [BIJDRAGEN.md](CONTRIBUTING.md),
[SECURITY.md](SECURITY.md), en de gedetailleerde [open-source notes](OPEN_SOURCE.md).

## Steun en donaties

Community-ondersteuning wordt op basis van de beste inspanningen geleverd, via GitHub-problemen en discussies.
Meld beveiligingsfouten altijd op een privékanaal. Het projectonderhoud is aanzienlijk
brengt ontwikkelings- en infrastructuurkosten met zich mee; als het jou, de ontwikkelaar, tijd heeft bespaard
[je kunt hem uitnodigen voor een koffie](https://buymeacoffee.com/lgx0).

Community-ondersteuning is de beste inspanning via GitHub-problemen en discussies. Beveiliging
rapporten moeten privé blijven. Als het project u tijd heeft bespaard, kunt u dat doen
[koop de ontwikkelaar een koffie](https://buymeacoffee.com/lgx0).
