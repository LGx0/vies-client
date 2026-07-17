# Svenska (sv) — vies-client

> [Alla språk](../../LANGUAGES.md) · Informativ översättning. Vid avvikelser gäller den kanoniska engelska tekniska eller juridiska källan. Endast `LICENSE` och `NOTICE` i roten är juridiskt auktoritativa; översättningen ersätter dem inte.

[![Licens: Apache-2.0](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](../../../LICENSE)
[![Java 21+](https://img.shields.io/badge/Java-21%2B-orange.svg)](../../../pom.xml)
[![Köp mig en kaffe](https://img.shields.io/badge/Buy_Me_a_Coffee-support-ffdd00?logo=buymeacoffee&logoColor=000)](https://buymeacoffee.com/lgx0)

🌍 **[Alla officiella EU-språk](../../LANGUAGES.md)**

En fristående, **noll körtidsberoende** Java-klient för Europeiska kommissionen VIES
(VAT Information Exchange System) för REST API för en skattenummerkontroll. Vilken Java som helst
kan anslutas till en API-server eller ett program på följande språk: Spring Boot, Quarkus, Micronaut,
eller till och med vanlig JDK`HttpServer`— modulen använder bara JDK (`java.net.http`),
det finns inget transitivt beroendeträd.

**På andra söknamn / Sökord:** VIES-checkare, VAT-checker, VAT-nummer
checker, VAT number validator, EU VAT validering, EU tax ID checker, tax number
validering, verifiering av skattenummer, verifiering av gemenskapens skattenummer, verifiering av momsnummer.
Detta är inte en allmän skattekalkylator: endast för VIES-verifiering av EU-skattenummer
Detta är inte en allmän skattekalkylator; den validerar EU-momsnummer via VIES.

Oberoende öppen källkodsprojekt; inte EU-kommissionen, EU eller medlemsländerna
är en officiell produkt från skattemyndigheterna och är inte godkänd eller certifierad av dem.

- **Krav:** Java **21+** bytecode/API; hela paketet är verifierat på JDK 21.
- **Officiell VIES-dokumentation:** <https://ec.europa.eu/taxation_customs/vies/#/technical-information>
- **Kallad slutpunkt:**`https://ec.europa.eu/taxation_customs/vies/rest-api/ms/{countryCode}/vat/{vatNumber}`
- **Medlemsstatens kontaktperson:**`https://ec.europa.eu/taxation_customs/vies/rest-api/check-status`

## Dokumentation

- [Installation](INSTALLATION.md)
- [Integration](INTEGRATION.md)
- [Teknisk design](TECHNICAL.md)
- [Enhets-, integrations- och samtidighetstester](TESTING.md)
- [Öppen källkod och licensval](OPEN_SOURCE.md)
- [Utgivningsguide](RELEASING.md)
- [Bidra](CONTRIBUTING.md)
- [Säkerhetspolicy](SECURITY.md)
- [Uppförandekod](CODE_OF_CONDUCT.md)
- [Support och donationer](SUPPORT.md)
- [Meddelanden om tredjepartskomponenter](THIRD_PARTY_NOTICES.md)
- [Ändringslogg](CHANGELOG.md)

## Bygg och anslutning

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

**Grale:**

```kotlin
implementation("vies.client:vies-client:1.2.0")
```

**Riktig JPMS-modul.** Burken fungerar som en namngiven modul (`jar --describe-module`):

```
vies.client@1.2.0
exports vies.client
requires java.net.http
contains vies.client.internal   ← non-exported internal package
```

Anslut från en modulariserad applikation så här:

```java
module my.api.server {
    requires vies.client;
}
```

Från Classpath (i ett icke-modulariserat, "traditionellt" projekt) fungerar det på samma sätt -
`module-info`spelar helt enkelt inte i det här fallet. Även utan Maven/Gradle
kan användas: källträdet under`src/main/java`kan kopieras till ditt eget projekt
(lämna`module-info.java`om ditt projekt inte är modulariserat).

## Snabbt exempel

```java
import vies.client.*;

try (var vies = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER"))) // betrodd hemlighet/konfiguration
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

`switch`är uttömmande: kompilatorn garanterar att du har hanterat alla fyra utgångar
(förseglat gränssnitt + mönstermatchning).

### Varför är`defaultRequester`viktigt?

`defaultRequester` ska vara organisationens eget EU-momsnummer och hämtas från en
betrodd hemlighet eller konfiguration. Vid ett giltigt svar **kan** VIES returnera
`consultationNumber`; fältet är valfritt och aldrig garanterat. Dess juridiska
bevisvärde beror på lokala regler. Spara det tillsammans med förfrågningsdatum och indata.

## Anslutning till Spring Boot API-server

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

Klienten är oföränderlig och trådsäker – applikationen skapar en enda instans
livscykel (singleton bean) och stäng vid avstängning (`close`).

Oinramat mönster: [`examples/ViesDemoServer.java`](../../../examples/ViesDemoServer.java)
(vanlig JDK`HttpServer`, på virtuella trådar):

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java     # port 8085
curl "http://localhost:8085/vat-check?number=HU00000000"
```

## Asynkron användning

```java
CompletableFuture<ViesResponse> future = vies.checkAsync("PL0000000000");
```

Som standard körs asynkrona anrop på **virtuella trådar** (Project Loom) — mycket
det finns inget plattformsgängavfall vid parallell kontroll heller. Egen exekutor a
kan anges i builder (`executor(...)`— den stängs inte av klienten).

Upp till 512 **individuella asynkronledaråtgärder** från den asynkroniserade klientbasen
mottas per klientinstans. En enda cacheläsning förbrukar utrymme under en kort tid;
den felaktiga ingången och samma enkelflygföljare förbrukar inte separat utrymme. I händelse av överbelastning, den nya individuella begäran omedelbart
Du får ett resultat av`Unavailable(..., "CLIENT_OVERLOADED")`. Detta är in-client
begränsar arbetet; inmatningskön och terminer lagrade av den som ringer också
måste begränsas.

## Frågeanläggning för flera miljoner

För miljontals användare, starta inte miljontals terminer i en JVM, och gör det inte
tillåta obegränsad direkttrafik till VIES. Den föreslagna strukturen:

> Klienten kan vara en bearbetningskomponent i en miljon arbetskö, men den faktiska
> VIES-överföring begränsas av de förändrade, icke-garanterade gränserna för EU:s och medlemsstaternas system
> bestämmas. Virtuella trådar minskar kostnaden för att vänta; uppströms
> kapaciteten ökar inte.

1. Inkommande checkar tas emot av en ihållande, uppdelad meddelandekö.
2. Flera arbetare i horisontell skala konsumerar linan i begränsade portioner.
3. En singelton`ViesClient`fungerar med virtuella trådar per arbetare.
4. Arbetare använder en gemensam Redis-cache via`ViesCache`-gränssnittet.
5. Global/distribuerad hastighetsbegränsare skyddar EU och medlemsländernas VIES-slutpunkter.

Inom en JVM slår klienten ihop detsamma och anländer samtidigt
skattenummer/fråga par (single-flight), så en cachemiss orsakar det inte
"begäran stampede".`maxConcurrentRequests`begränsas av det verkliga nätverket
förfrågningar och`maxPendingAsyncRequests`och`maxPendingSyncRequests`i minnet
ger omedelbart mottryck. Alla är JVM-lokala. Aggregerad trafik av flera arbetare
måste regleras med en gemensam, distribuerad limiter. Den beständiga inmatningskön och
konsumentpoolen bör fortfarande begränsas av ansökan.

Exempel på arbetstagare med tung belastning:

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

Resultatet`Unavailable`– inklusive värdet`CLIENT_OVERLOADED`– är beständigt
du måste försöka igen med en fördröjning i kön.`MalformedInput`och`Invalid`bör inte provas igen.

## Sökväg för en begäran/förfrågans livscykel

1. **Normalisera / Normalisera:** kontrollera landskod och format utan nätverk.
2. **Cache:** Omedelbart återlämnande av giltiga, fortfarande levande resultat.
3. **Enstaka flygning:** identiska par av skattekoder+fråga slås samman i en JVM.
4. **Inträde:** asynkron- och nätverksgränser skyddar minne och VIES.
5. **HTTP:** återanvänd JDK`HttpClient`-anslutning med kort timeout.
6. **Validera / Svarskontroll:** ofullständig boolean eller datum →`MALFORMED_RESPONSE`.
7. **Cacheskrivning:** endast det autentiska`Valid`-resultatet cachelagras.

## Tvåspråkiga felsvar / Tvåspråkiga felsvar

Maskinfelkoden är alltid stabil och språkoberoende.`error()`är ungerska och engelska
ger användaren text och ett nytt försök:

```java
var response = vies.check("HU00000000");
response.error().ifPresent(error -> {
    log.warn("{} | {} | retry={}", error.messageHu(), error.messageEn(), error.retryable());
});
```

| Resultat         |          HTTP |          Försök igen | Cache | Betydelse                                                                           |
| ---------------- | ------------: | -------------------: | ----: | ----------------------------------------------------------------------------------- |
| `Valid`          |           200 |                  nej | ja/ja | VIES bekräftat som giltigt / VIES bekräftat giltigt                                 |
| `Invalid`        |           200 |                  nej |   nej | VIES bekräftade inte att det var giltigt / VIES bekräftade inte att det var giltigt |
| `Unavailable`    | 503 eller 429 | mestadels/vanligtvis |   nej | Inget giltighetsbeslut fattades                                                     |
| `MalformedInput` |           400 |                  nej |   nej | Inmatningen måste korrigeras / Ingången måste korrigeras                            |

## Konfiguration (byggare)

| Inställning                       | Basvärde                 | Vad gör                                                                             |
| --------------------------------- | ------------------------ | ----------------------------------------------------------------------------------- |
| `baseUrl(String)`                 | officiella VIES REST URL | Omdirigering i skentest                                                             |
| `connectTimeout(Duration)`        | 5 s                      | Timeout för TCP/TLS-anslutningen                                                    |
| `requestTimeout(Duration)`        | 8 s                      | Total tidsgräns för begäran (VIES är interaktiv, håll den kort)                     |
| `admissionTimeout(Duration)`      | 2 s                      | Så här länge väntar det på ledigt nätverksutrymme                                   |
| `defaultRequester(ViesRequester)` | det finns ingen          | Eget kommunalskattenummer → konsultations-ID                                        |
| `retries(int)`                    | 0                        | Automatiskt försök igen på övergående fel (0-5, exponentiell backoff + jitter)      |
| `retryDelay(Duration)`            | 400 ms                   | Exponentiell backoff default                                                        |
| `maxConcurrentRequests(int)`      | 32                       | Övre gräns för samtidiga verkliga VIES-nätverksbegäranden                           |
| `maxPendingSyncRequests(int)`     | 512                      | Minnesgräns för samtidiga synkroniseringsuppringare; ovanför den`CLIENT_OVERLOADED` |
| `maxPendingAsyncRequests(int)`    | 512                      | Minnesgräns för aktiva/väntande asynkronoperationer; ovanför den`CLIENT_OVERLOADED` |
| `cacheTtl(Duration)`              | 24 timmar                | Cachetid för giltiga träffar                                                        |
| `cacheMaxEntries(int)`            | 10 000                   | Storleksgräns för inbyggt minnescache                                               |
| `cache(ViesCache)`                | inbyggd                  | Egen cache-backend (t.ex. Redis)                                                    |
| `disableCache()`                  | —                        | Ingen lagrad cache; identiska parallella anrop kan dela en single-flight-begäran    |
| `userAgent(String)`               | modul-id                 | Du bör identifiera dig för EU                                                       |
| `executor(ExecutorService)`       | virtuell tråd/uppgift    | Min asynkrona exekutor; den som ringer är ansvarig för dess livscykel               |

### Egen cache (t.ex. Redis)

```java
class RedisViesCache implements ViesCache {
    public Optional<ViesResponse.Valid> get(String key) { /* ... */ }
    public void put(String key, ViesResponse.Valid value, Duration ttl) { /* ... */ }
}

var vies = ViesClient.builder().cache(new RedisViesCache()).build();
```

Enda resultatet`Valid`cachelagras — ogiltigt nummer och övergående fel aldrig.
För Redis-adapter, använd kort cache-timeout, versionsformat namnutrymme och eget
felmått. Cache-läsfel`CACHE_ERROR`uppstår när Redis är nere
starta inte en okontrollerad VIES-förfrågningsstamp.

`consultationNumber` och `requestDate` som returneras från cachen hör till den
**ursprungliga** kontrollen; en cacheträff är inte en ny VIES-kontroll. `disableCache()`
stänger bara av lagrad cache: identiska samtidiga VAT+requester-anrop kan dela en
single-flight-nätverksbegäran, medan ett senare anrop efter slutförandet gör en ny
begäran. `consultationNumber` är fortfarande valfritt.

## Semantik värd att veta

1. **`Unavailable`≠`Invalid`.** Medlemsstaternas bakgrundssystem regelbundet
   släpps (`MS_UNAVAILABLE`,`MS_MAX_CONCURRENT_REQ`...). I det här fallet är siffran a
   **inte bedömd av VIES** — kommersiellt förbjudet att betrakta som ogiltig.
2. **Formatförfiltrering är inte VIES-autentisering.** Modulen filtrerar bort de registrerade
   fel inmatning (`MalformedInput`) innan du går till nätverket, men giltigheten
   dess källa är alltid svaret från VIES.
3. **Försöket igen ökar också belastningen.** Försök bara ett tillfälligt fel lokalt några gånger
   igen; i en stor operation är den fördröjda återförsöksmekanismen för den hållbara kön den primära.
4. **Grekland`EL`, Nordirland`XI`.** Modulen hanterar båda,`GR`
   mappar automatiskt indata till`EL`.

## Tester

```bash
./mvnw test    # enhets- och lokala HTTP-tester för samtidighet, återförsök, mottryck och livscykel
```

---

## engelska snabbstart

Nollberoende Java 21+ klient för EU-kommissionens VIES VAT-nummer
validering REST API. Bygg med`./mvnw package`, sedan:

```java
try (var vies = ViesClient.createDefault()) {
    if (vies.check("DE000000000") instanceof ViesResponse.Valid v) {
        System.out.println(v.traderName().orElse("?"));
    }
}
```

Resultaten är en förseglad hierarki (`Valid`/`Invalid`/`Unavailable`/
`MalformedInput`) designad för uttömmande mönstermatchning`switch`. Giltig
resultaten cachelagras i minnet i 24 timmar; övergående VIES-avbrott rapporteras som
`Unavailable`, aldrig som`Invalid`. Officiell API-dokumentation:
<https://ec.europa.eu/taxation_customs/vies/#/technical-information>.

### Drift i hög skala

Den här klienten kan vara en komponent i en pipeline för bearbetning av miljoner artiklar, men
faktisk VIES-genomströmning begränsas av variabel, icke-garanterad EU och medlemsstat
gränser. Virtuella trådar minskar blockeringskostnaden; de ökar inte uppströms
kapacitet. Använd en hållbar partitionerad kö, avgränsade konsumenter, en delad Redis
cache och en distribuerad hastighetsbegränsare över alla arbetare. Lokalt enkelflyg och
semaforer skyddar endast en JVM. Behandla aldrig`Unavailable`som`Invalid`; använda
`response.error()`för stabila koder, återförsökbarhet och ungerska/engelska meddelanden.

## Öppen källkod / Öppen källkod

Projektet kan användas, modifieras och
kan delas ut. Licensen tillåter kommersiell användning och express
beviljar en patentlicens; licens-, tillskrivnings- och modifieringsvillkor måste läsas
att vakta. Innan du bidrar, läs [CONTRIBUTING.md](CONTRIBUTING.md)and
[CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)filer.

Detta projekt är licensierat under [Apache License 2.0](../../../LICENSE), en tillåtande
licens med ett uttryckligt patentbeviljande. Se [CONTRIBUTING.md](CONTRIBUTING.md),
[SECURITY.md](SECURITY.md), och de detaljerade [öppen källkodsanteckningar](OPEN_SOURCE.md).

## Stöd och donationer

Gemenskapsstöd tillhandahålls på bästa sätt, via GitHub-frågor och diskussioner.
Rapportera alltid säkerhetsfel på en privat kanal. Projektunderhåll är betydande
involverar utvecklings- och infrastrukturkostnader; om det sparade tid för dig, utvecklaren
[du kan bjuda in honom på en kaffe](https://buymeacoffee.com/lgx0).

Gemenskapsstöd är bäst genom GitHub-frågor och diskussioner. Säkerhet
rapporter måste förbli privata. Om projektet sparade tid, kan du
[köp utvecklaren en coffee](https://buymeacoffee.com/lgx0).
