# Dansk (da) — vies-client

`disableCache()` slår lagret/persistent cache fra, men samtidige kald med samme momsnummer + requester kan dele én single-flight-netværksanmodning. Et senere kald efter afslutningen udfører en ny VIES-anmodning. `consultationNumber` er en valgfri værdi, som VIES kan returnere, men den er aldrig garanteret; dens bevisværdi afhænger af lokale regler. Indlæs kun `MY_EU_VAT_NUMBER` fra en betroet secret-/konfigurationskilde.

> [Alle sprog](../../LANGUAGES.md) · Informativ oversættelse. Ved afvigelser er den kanoniske engelske tekniske eller juridiske kilde gældende. Kun `LICENSE` og `NOTICE` i roden er juridisk autoritative; oversættelsen erstatter dem ikke.

[![Licens: Apache-2.0](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](../../../LICENSE)
[![Java 21+](https://img.shields.io/badge/Java-21%2B-orange.svg)](../../../pom.xml)
[![Køb mig en kaffe](https://img.shields.io/badge/Buy_Me_a_Coffee-support-ffdd00?logo=buymeacoffee&logoColor=000)](https://buymeacoffee.com/lgx0)

🌍 **[Alle officielle EU-sprog](../../LANGUAGES.md)**

En selvstændig, **nul runtime afhængighed** Java-klient til Europa-Kommissionens VIES
(VAT Information Exchange System) til REST API af en skattenummerkontrol. Enhver Java
kan tilsluttes en API-server eller et program på følgende sprog: Spring Boot, Quarkus, Micronaut,
eller endda almindelig JDK`HttpServer`- modulet bruger kun JDK (`java.net.http`),
der er intet transitivt afhængighedstræ.

**På andre søgenavne / Søgeord:** VIES-tjek, VAT-tjek, VAT-nummer
checker, VAT nummer validator, EU VAT validering, EU tax ID checker, skattenummer
validering, verifikation af skattenummer, verifikation af samfundsskattenummer, validering af momsnummer.
Dette er ikke en generel skatteberegner: kun til VIES-verifikation af EU-afgiftsnumre
Dette er ikke en generel skatteberegner; den validerer EU-momsnumre via VIES.

Uafhængigt open source-projekt; ikke Europa-Kommissionen, EU eller medlemslande
er et officielt produkt fra skattemyndighederne og er ikke godkendt eller certificeret af dem.

- **Krav:** Java **21+** bytekode/API; hele pakken er verificeret på JDK 21.
- **Officiel VIES-dokumentation:** <https://ec.europa.eu/taxation_customs/vies/#/technical-information>
- **Kaldet slutpunkt:**`https://ec.europa.eu/taxation_customs/vies/rest-api/ms/{countryCode}/vat/{vatNumber}`
- **Medlemsstatens kontaktperson:**`https://ec.europa.eu/taxation_customs/vies/rest-api/check-status`

## Dokumentation

- [Installation](INSTALLATION.md)
- [Integration](INTEGRATION.md)
- [Teknisk design](TECHNICAL.md)
- [Enheds-, integrations- og samtidighedstest](TESTING.md)
- [Åben kildekode og licensvalg](OPEN_SOURCE.md)
- [Udgivelsesvejledning](RELEASING.md)
- [Udgivelse på GitHub](GITHUB_SETUP.md)
- [Bidrag](CONTRIBUTING.md)
- [Sikkerhedspolitik](SECURITY.md)
- [Adfærdskodeks](CODE_OF_CONDUCT.md)
- [Support og donationer](SUPPORT.md)
- [Tredjepartsmeddelelser](THIRD_PARTY_NOTICES.md)
- [Ændringslog](CHANGELOG.md)

## Byg og tilslutning

```bash
./mvnw install    # tests + target/vies-client-1.2.0.jar (+ -sources.jar, -javadoc.jar)
               # install into the local Maven repository
```

**Maven:**

```xml
<dependency>
    <groupId>vies.client</groupId>
    <artifactId>vies-client</artifactId>
    <version>1.2.0</version>
</dependency>
```

**Gradel:**

```kotlin
implementation("vies.client:vies-client:1.2.0")
```

**Ægte JPMS-modul.** Krukken opfører sig som et navngivet modul (`jar --describe-module`):

```
vies.client@1.2.0
exports vies.client
requires java.net.http
contains vies.client.internal   ← non-exported internal package
```

Tilslut fra en modulær applikation som denne:

```java
module my.api.server {
    requires vies.client;
}
```

Fra Classpath (i et ikke-modulariseret, "traditionelt" projekt) fungerer det på samme måde -
`module-info`spiller simpelthen ikke i dette tilfælde. Selv uden Maven/Gradle
kan bruges: Kildetræet under`src/main/java`kan kopieres ind i dit eget projekt
(lad`module-info.java`, hvis dit projekt ikke er modulært).

## Hurtigt eksempel

```java
import vies.client.*;

try (var vies = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER"))) // betroet secret/config
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

`switch`er udtømmende: compileren garanterer, at du har håndteret alle fire udgange
(forseglet grænseflade + mønstertilpasning).

### Hvorfor er`defaultRequester`vigtig?

Angiv organisationens eget EU-momsnummer som requester fra en betroet secret-/konfigurationskilde. Ved et gyldigt resultat kan VIES returnere et valgfrit `consultationNumber`, men det er aldrig garanteret. Nummerets bevisværdi afhænger af lokale regler; gem også tidspunkt, forespurgt momsnummer og resultat efter jeres revisions- og databeskyttelsespolitik.

## Forbindelse til Spring Boot API-server

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

Klienten er uforanderlig og trådsikker - applikationen opretter en enkelt instans
livscyklus (singleton bean) og luk ved nedlukning (`close`).

Uindrammet mønster: [`examples/ViesDemoServer.java`](../../../examples/ViesDemoServer.java)
(almindelig JDK`HttpServer`, på virtuelle tråde):

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java     # port 8085
curl "http://localhost:8085/vat-check?number=HU00000000"
```

## Asynkron brug

```java
CompletableFuture<ViesResponse> future = vies.checkAsync("PL0000000000");
```

Som standard kører asynkrone opkald på **virtuelle tråde** (Project Loom) - meget
der er heller ikke spild af platformgevind ved parallel kontrol. Egen bobestyrer a
kan angives i builder (`executor(...)`— den er ikke lukket af klienten).

Op til 512 **individuelle async-lederhandlinger** fra async-klientbasen
modtaget pr klientinstans. En enkelt cache-læsning bruger plads i kort tid;
den fejlbehæftede indgang og den samme enkeltflyvningsfølger bruger ikke separat plads. I tilfælde af overbelastning, den nye individuelle anmodning straks
Du får et resultat af`Unavailable(..., "CLIENT_OVERLOADED")`. Dette er in-client
begrænser arbejdet; inputkøen og futures gemt af den, der ringer op
skal begrænses.

## Multi-million forespørgselsanlæg

For millioner af brugere, start ikke millioner af futures i én JVM, og lad være
tillade ubegrænset direkte trafik til VIES. Den foreslåede struktur:

> Klienten kan være en behandlingskomponent i en million arbejdskø, men den faktiske
> VIES-transmission er begrænset af de skiftende, ikke-garanterede grænser for EU's og medlemslandenes systemer
> bestemmes. Virtuelle tråde reducerer omkostningerne ved at vente; opstrøms
> kapaciteten øges ikke.

1. Indgående checks modtages af en vedvarende, opdelt beskedkø.
2. Flere arbejdere i vandret skala indtager linjen i begrænsede portioner.
3. En enkelt`ViesClient`arbejder med virtuelle tråde pr. arbejder.
4. Arbejdere bruger en fælles Redis-cache gennem`ViesCache`-grænsefladen.
5. Global/distribueret hastighedsbegrænser beskytter EU's og medlemslandenes VIES-endepunkter.

Inden for en JVM fusionerer klienten det samme og ankommer på samme tid
skattenummer/forespørgselspar (enkeltflyvning), så en cache-miss forårsager det ikke
"anmod om stampede".`maxConcurrentRequests`er begrænset af det rigtige netværk
anmodninger og`maxPendingAsyncRequests`og`maxPendingSyncRequests`i hukommelsen
giver øjeblikkeligt modtryk. Alle er JVM-lokale. Samlet trafik af flere arbejdere
skal reguleres med en fælles, distribueret begrænser. Den vedvarende inputkø og
forbrugerpuljen bør stadig være begrænset af ansøgningen.

Eksempel på arbejdstager med tung belastning:

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

Resultatet`Unavailable`- inklusive værdien`CLIENT_OVERLOADED`- er vedvarende
du skal prøve igen med en forsinkelse i køen.`MalformedInput`og`Invalid`bør ikke prøves igen.

## Sti til en anmodning/anmodningslivscyklus

1. **Normaliser / Normaliser:** tjek landekode og format uden netværk.
2. **Cache:** Øjeblikkelig returnering af gyldige, stadig levende resultater.
3. **Enkeltflyvning:** identiske skattekode+forespørgselspar er slået sammen i én JVM.
4. **Adgang:** asynkron- og netværksgrænser beskytter hukommelse og VIES.
5. **HTTP:** genbrugt JDK`HttpClient`forbindelse med kort timeout.
6. **Valider/Svar kontrol:** ufuldstændig boolean eller dato →`MALFORMED_RESPONSE`.
7. **Cacheskrivning:** kun det autentiske`Valid`-resultat er cachelagret.

## Tosprogede fejlsvar / Tosprogede fejlsvar

Maskinens fejlkode er altid stabil og sproguafhængig.`error()`er ungarsk og engelsk
giver brugertekst og en beslutning om at prøve igen:

```java
var response = vies.check("HU00000000");
response.error().ifPresent(error -> {
    log.warn("{} | {} | retry={}", error.messageHu(), error.messageEn(), error.retryable());
});
```

| Resultat         |          HTTP |    Prøv igen | Cache | Betydning                                                           |
| ---------------- | ------------: | -----------: | ----: | ------------------------------------------------------------------- |
| `Valid`          |           200 |          nej | ja/ja | VIES bekræftet som gyldig / VIES bekræftet gyldig                   |
| `Invalid`        |           200 |          nej |   nej | VIES bekræftede det ikke som gyldigt / VIES bekræftede ikke gyldigt |
| `Unavailable`    | 503 eller 429 | mest/normalt |   nej | Der blev ikke truffet nogen gyldighedsbeslutning                    |
| `MalformedInput` |           400 |          nej |   nej | Indtastningen skal korrigeres / Input skal korrigeres               |

## Konfiguration (bygger)

| Indstilling                       | Grundværdi             | Hvad gør                                                                                   |
| --------------------------------- | ---------------------- | ------------------------------------------------------------------------------------------ |
| `baseUrl(String)`                 | officiel VIES REST URL | Omdirigering i mock test                                                                   |
| `connectTimeout(Duration)`        | 5 s                    | Timeout for TCP/TLS-forbindelse                                                            |
| `requestTimeout(Duration)`        | 8 s                    | Samlet timeout for anmodningen (VIES er interaktiv, hold den kort)                         |
| `admissionTimeout(Duration)`      | 2 s                    | Så længe venter den på ledig netværksplads                                                 |
| `defaultRequester(ViesRequester)` | der er ingen           | Eget samfundsskattenummer → konsultations-id                                               |
| `retries(int)`                    | 0                      | Automatisk genforsøg på forbigående fejl (0-5, eksponentiel backoff + jitter)              |
| `retryDelay(Duration)`            | 400 ms                 | Eksponentiel backoff standard                                                              |
| `maxConcurrentRequests(int)`      | 32                     | Øvre grænse for samtidige reelle VIES-netværksanmodninger                                  |
| `maxPendingSyncRequests(int)`     | 512                    | Hukommelsesgrænse for samtidige synkroniseringsopkald; over det`CLIENT_OVERLOADED`         |
| `maxPendingAsyncRequests(int)`    | 512                    | Hukommelsesgrænse for aktive/afventende asynkrone operationer; over det`CLIENT_OVERLOADED` |
| `cacheTtl(Duration)`              | 24 timer               | Cachetid for gyldige hits                                                                  |
| `cacheMaxEntries(int)`            | 10.000                 | Indbygget hukommelse cache størrelse grænse                                                |
| `cache(ViesCache)`                | indbygget              | Egen cache-backend (f.eks. Redis)                                                          |
| `disableCache()`                  | —                      | Ingen lagret cache; samtidige identiske kald kan dele single-flight                         |
| `userAgent(String)`               | modul-id               | Du bør identificere dig selv over for EU                                                   |
| `executor(ExecutorService)`       | virtuel tråd/opgave    | Min async executor; den, der ringer, er ansvarlig for sin livscyklus                       |

### Egen cache (f.eks. Redis)

```java
class RedisViesCache implements ViesCache {
    public Optional<ViesResponse.Valid> get(String key) { /* ... */ }
    public void put(String key, ViesResponse.Valid value, Duration ttl) { /* ... */ }
}

var vies = ViesClient.builder().cache(new RedisViesCache()).build();
```

Eneste resultat`Valid`cachelagres — ugyldigt nummer og forbigående fejl aldrig.
Til Redis-adapter skal du bruge kort cache-timeout, versioneret navneområde og eget
fejlmetrik. Cachelæsefejl`CACHE_ERROR`opstår, når Redis er nede
start ikke en ukontrolleret VIES-anmodningsstampe.

`consultationNumber`og`requestDate`returneret fra cachen er de **originale**
dokumenterer en konsultation; cache-hittet er ikke et nyt VIES-tjek. For et nyt certifikat
tjek`fromCache`, brug kort TTL eller`disableCache()`.

## Semantik værd at kende

1. **`Unavailable`≠`Invalid`.** Medlemsstaternes baggrundssystemer regelmæssigt
   er droppet (`MS_UNAVAILABLE`,`MS_MAX_CONCURRENT_REQ`...). I dette tilfælde er tallet a
   **ikke bedømt af VIES** — kommercielt forbudt at betragte som ugyldig.
2. **Formatforfiltrering er ikke VIES-godkendelse.** Modulet bortfiltrerer de registrerede
   forkert input (`MalformedInput`) før du går til netværket, men gyldigheden
   dens kilde er altid svaret fra VIES.
3. **Forsøg igen øger også belastningen.** Prøv kun en midlertidig fejl lokalt et par gange
   igen; i en stor operation er den forsinkede genforsøgsmekanisme i den holdbare kø den primære.
4. **Grækenland`EL`, Nordirland`XI`.** Modulet håndterer begge,`GR`
   kortlægger automatisk input til`EL`.

## Tester

```bash
./mvnw test    # enheds- og lokale HTTP-test af samtidighed, gentagelse, modtryk og livscyklus
```

---

## Engelsk hurtigstart

Nulafhængig Java 21+ klient til EU-Kommissionens VIES VAT-nummer
validering REST API. Byg med`./mvnw package`, derefter:

```java
try (var vies = ViesClient.createDefault()) {
    if (vies.check("DE000000000") instanceof ViesResponse.Valid v) {
        System.out.println(v.traderName().orElse("?"));
    }
}
```

Resultaterne er et forseglet hierarki (`Valid`/`Invalid`/`Unavailable`/
`MalformedInput`) designet til udtømmende mønster-matching`switch`. Gyldig
resultater gemmes i hukommelsen i 24 timer; forbigående VIES-udfald rapporteres som
`Unavailable`, aldrig som`Invalid`. Officiel API-dokumentation:
<https://ec.europa.eu/taxation_customs/vies/#/technical-information>.

### Drift i høj skala

Denne klient kan være en komponent i en behandlingspipeline med millioner af varer, men
faktisk VIES-gennemstrømning er afgrænset af variabel, ikke-garanteret EU og medlemsland
grænser. Virtuelle tråde reducerer blokeringsomkostninger; de øges ikke opstrøms
kapacitet. Brug en holdbar opdelt kø, afgrænsede forbrugere, en delt Redis
cache og en distribueret hastighedsbegrænser på tværs af alle arbejdere. Lokal enkeltflyvning og
semaforer beskytter kun én JVM. Behandl aldrig`Unavailable`som`Invalid`; bruge
`response.error()`for stabile koder, genprøvbarhed og ungarske/engelske beskeder.

## Åben kildekode / Åben kildekode

Projektet kan bruges, ændres og
kan fordeles. Licensen tillader kommerciel brug og ekspres
udsteder en patentlicens; licens-, tilskrivnings- og ændringsvilkår skal læses
at vogte. Før du bidrager, skal du læse [CONTRIBUTING.md](CONTRIBUTING.md)og
[CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)filer.

Dette projekt er licenseret under [Apache License 2.0](../../../LICENSE), en tilladende
licens med eksplicit patentbevilling. Se [CONTRIBUTING.md](CONTRIBUTING.md),
[SECURITY.md](SECURITY.md), og de detaljerede [open source-noter](OPEN_SOURCE.md).

## Støtte og donationer

Fællesskabsstøtte ydes efter bedste indsats via GitHub-spørgsmål og diskussioner.
Rapporter altid sikkerhedsfejl på en privat kanal. Projektvedligeholdelse er væsentlig
involverer udviklings- og infrastrukturomkostninger; hvis det sparede dig, udvikleren, tid
[du kan invitere ham til en kaffe](https://buymeacoffee.com/lgx0).

Fællesskabsstøtte er den bedste indsats gennem GitHub-spørgsmål og diskussioner. Sikkerhed
rapporter skal forblive private. Hvis projektet har sparet dig tid, kan du
[køb udvikleren en coffee](https://buymeacoffee.com/lgx0).
