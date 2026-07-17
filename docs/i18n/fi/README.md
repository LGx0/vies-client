# Suomi (fi) — vies-client — ALV-numeron tarkistin

`disableCache()` poistaa tallennetun/pysyvän välimuistin käytöstä, mutta samanaikaiset kutsut samalla ALV-tunnus + requester -parilla voivat jakaa yhden single-flight-verkkopyynnön. Sen valmistumisen jälkeen tehty myöhempi kutsu lähettää uuden VIES-pyynnön. `consultationNumber` on valinnainen ja VIES voi palauttaa sen, mutta sitä ei koskaan taata; sen oikeudellinen todistusarvo riippuu paikallisista säännöistä. Lataa `MY_EU_VAT_NUMBER` vain luotetusta salaisuus-/konfiguraatiolähteestä.

**Hakusanat:** ALV-numeron tarkistus, ALV-numeron validointi, EU-ALV-validointi, verotunnisteen tarkistus, VIES Java -asiakas; `VAT checker`, `VAT number validator`, `EU VAT validation`, `tax ID checker`.

Tämä ei ole yleinen verolaskuri vaan VIES-järjestelmän kautta EU:n ALV-tunnisteita tarkistava asiakasohjelma.

> [Kielivalitsin](../../LANGUAGES.md) · Tämä lokalisointi parantaa saavutettavuutta. Jos se poikkeaa kanonisesta englanninkielisestä teknisestä tai oikeudellisesta lähteestä, englanninkielinen lähde määrää. Juuren `LICENSE` ja`NOTICE` ovat oikeudellisesti määrääviä.

[![Lisenssi: Apache-2.0](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](../../../LICENSE)[![Java 21+](https://img.shields.io/badge/Java-21%2B-orange.svg)](../../../pom.xml)
🌍 **[Nyelv / Kieli / Kaikki EU:n viralliset kielet ​​](../../LANGUAGES.md)**
Itsenäinen, **nolla ajonaikaista riippuvuutta** Java-asiakasohjelma Euroopan komission VIES:lle
(VAT Information Exchange System) veronumerotarkistuksen REST API:lle. Mikä tahansa Java
voidaan yhdistää API-palvelimeen tai ohjelmaan seuraavilla kielillä: Spring Boot, Quarkus, Micronaut,
tai jopa tavallinen JDK `HttpServer`- moduuli käyttää vain JDK:ta (`java.net.http`),
transitiivista riippuvuuspuuta ei ole olemassa.
Itsenäinen avoimen lähdekoodin projekti; eivät Euroopan komissio, EU tai jäsenvaltiot
on veroviranomaisten virallinen tuote, eikä se ole veroviranomaisten hyväksymä tai sertifioima.

- **Edellytys:** Java **21+** tavukoodi/API; koko paketti on vahvistettu JDK 21:ssä.
- **Virallinen VIES-dokumentaatio:** <https://ec.europa.eu/taxation_customs/vies/#/technical-information>
- **Kutsuttu päätepisteeksi:**`https://ec.europa.eu/taxation_customs/vies/rest-api/ms/{countryCode}/vat/{vatNumber}`
- **Jäsenvaltion yhteyshenkilö:**`https://ec.europa.eu/taxation_customs/vies/rest-api/check-status`

## Dokumentaatio / Dokumentaatio

- [Asennus / Asennus](INSTALLATION.md)
- [Liitäntä / Integrointi](INTEGRATION.md)
- [Tekninen rakenne / Tekninen suunnittelu](TECHNICAL.md)
- [Yksikkö-, integraatio- ja kilpailutestit /](TESTING.md):n testaus
- [Avoin lähdekoodi ja lisenssipäätös / Avoimen lähdekoodin](OPEN_SOURCE.md)
- [Vapautusopas / Irrotus](RELEASING.md)
- [GitHub-julkaisu / GitHub-julkaisu](GITHUB_SETUP.md)
- [Osallistuminen / Osallistuminen](CONTRIBUTING.md)
- [Turvallisuuspolitiikka / Turvallisuus](SECURITY.md)
- [Käyttäytymissäännöt / Käytännesäännöt](CODE_OF_CONDUCT.md)
- [Tuki ja lahjoitukset / Tuki ja lahjoitukset](SUPPORT.md)
- [Kolmannen osapuolen huomautukset](THIRD_PARTY_NOTICES.md)
- [Muutosloki / Muutosloki](CHANGELOG.md)

## Rakenna ja liitä

```bash
./mvnw install    # tesztek + target/vies-client-1.0.0.jar (+ -sources.jar, -javadoc.jar)
               # és telepítés a lokális Maven-repóba
```

**Maven:**

```xml
<dependency>
    <groupId>vies.client</groupId>
    <artifactId>vies-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle:**

```kotlin
implementation("vies.client:vies-client:1.0.0")
```

**Todellinen JPMS-moduuli.** Purkki toimii nimettynä moduulina (`jar --describe-module`):

```
vies.client@1.0.0
exports vies.client
requires java.net.http
contains vies.client.internal   ← nem exportált, zárt belső csomag
```

Yhdistä modulaarisesta sovelluksesta, kuten tämä:

```java
module my.api.server {
    requires vies.client;
}
```

Classpathista (ei-modulaarisessa, "perinteisessä" projektissa) se toimii samalla tavalla

- `module-info` ei yksinkertaisesti pelaa tässä tapauksessa. Jopa ilman Maven/Gradlea
  voidaan käyttää:`src/main/java`:n lähdepuu voidaan kopioida omaan projektiisi
  (jätä `module-info.java`, jos projektisi ei ole modulaarinen).

## Pikaesimerkki

```java
import vies.client.*;

try (var vies = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER"))) // luotettu salaisuus/asetus
        .retries(1)
        .build()) {

    // Elfogad kötőjelet, szóközt, kisbetűt; a GR-t EL-re képezi.
    switch (vies.check("DE 000 000 000")) {
        case ViesResponse.Valid v ->
            System.out.println("Érvényes: " + v.traderName().orElse("(név nem publikus)")
                    + " — konzultációs szám: " + v.consultationNumber().orElse("-"));
        case ViesResponse.Invalid i ->
            System.out.println("Nem élő közösségi adószám: " + i.vatNumber());
        case ViesResponse.Unavailable u ->
            System.out.println("A VIES most nem elérhető (" + u.errorCode() + ") — később újra");
        case ViesResponse.MalformedInput m ->
            System.out.println("Hibás bemenet: " + m.reason());
    }
}
```

`switch` on tyhjentävä: kääntäjä takaa, että olet käsitellyt kaikki neljä lähtöä
(sinetöity käyttöliittymä + kuvioiden sovitus).

## Miksi `defaultRequester` on tärkeä?

Anna requesteriksi organisaation oma ALV-tunnus luotetusta salaisuus-/konfiguraatiolähteestä. Kelvollisen tuloksen yhteydessä VIES voi palauttaa valinnaisen `consultationNumber`-arvon, mutta sitä ei koskaan taata. Sen todistusarvo riippuu paikallisista säännöistä; säilytä myös aika, tarkistettu ALV-tunnus ja tulos auditointi- ja tietosuojakäytäntöjen mukaisesti.

## Yhteys Spring Boot API -palvelimeen

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

Asiakas on muuttumaton ja säieturvallinen – sovellus luo yhden esiintymän
(singleton bean) ja sulje se sammutuksen yhteydessä (`close`).
Kehyksetön kuvio: [`examples/ViesDemoServer.java`](../../../examples/ViesDemoServer.java) (tavallinen JDK`HttpServer`, virtuaalisissa säikeissä):

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java     # port 8085
curl "http://localhost:8085/vat-check?number=HU00000000"
```

## Asynkroninen käyttö

```java
CompletableFuture<ViesResponse> future = vies.checkAsync("PL0000000000");
```

Oletuksena async-kutsut toimivat **virtuaalisissa säikeissä** (Project Loom) – paljon
rinnakkaisessa tarkistuksessa ei myöskään ole alusta-kierteen hukkaa. Oma toimeenpanija a
voidaan syöttää builderiin (`executor(...)`— asiakas ei ole sulkenut sitä).
Jopa 512 **yksittäistä async-johtajatoimintoa** asynkronisesta asiakaskannasta
vastaanotettu asiakaskohtaisesti. Yksi välimuistin luku vie tilaa lyhyen aikaa;
viallinen tulo ja sama yhden lennon seuraaja eivät vie erillistä tilaa. Ylikuormituksen sattuessa uusi yksilöllinen pyyntö välittömästi
Saat tuloksen `Unavailable(..., "CLIENT_OVERLOADED")`. Tämä on asiakkaan sisällä
rajoittaa työtä; myös soittajan tallentamat syöttöjonot ja futuurit
on rajoitettava.

## Usean miljoonan kyselyn tehdas

Miljoonat käyttäjät: älä aloita miljoonia futuureja yhdessä JVM:ssä, äläkä tee
salli rajattoman suoran liikenteen VIES:iin. Ehdotettu rakenne:

> Asiakas voi olla miljoonan työjonon käsittelykomponentti, mutta todellinen
> VIES-lähetystä rajoittavat EU:n ja jäsenmaiden järjestelmien muuttuvat, takaamattomat rajat
> olla päättäväinen. Virtuaaliset säikeet vähentävät odottamisen kustannuksia; ylävirtaan
> kapasiteettia ei lisätä.

1. Pysyvä, osioitu viestijono vastaanottaa saapuvat tarkistukset.
2. Useat vaakatasossa olevat työntekijät kuluttavat linjaa rajoitetuissa annoksissa.
3. Yksi yksittäinen `ViesClient` toimii virtuaalisten säikeiden kanssa työntekijää kohden.
4. Työntekijät käyttävät yhteistä Redis-välimuistia `ViesCache`-liitännän kautta.
5. Globaali/jaettu nopeuden rajoitin suojaa EU:n ja jäsenvaltioiden VIES-päätepisteitä.
   JVM:ssä asiakas yhdistää saman ja saapuu samaan aikaan
   veronumero/kyselyparit (yksi lento), joten välimuistin menetys ei aiheuta sitä
   "pyyntö stampede".`maxConcurrentRequests`:tä rajoittaa todellinen verkko
   pyynnöt ja `maxPendingAsyncRequests` ja`maxPendingSyncRequests` muistissa
   antaa välittömän vastapaineen. Kaikki ovat JVM-paikallisia. Useiden työntekijöiden yhteenlaskettu liikenne
   on säädettävä yhteisellä hajautetulla rajoittimella. Pysyvä syöttöjono ja
   Sovelluksen pitäisi silti rajoittaa kuluttajapoolia.
   Esimerkki raskaan kuorman työntekijästä:

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

Tulos `Unavailable`– mukaan lukien arvo`CLIENT_OVERLOADED`– on pysyvä
sinun on yritettävä uudelleen jonossa viiveellä.`MalformedInput`:tä ja`Invalid`:ta ei pidä yrittää uudelleen.

## Pyynnön polku / Pyynnön elinkaari

1. **Normaloi / Normalisoi:** tarkista maakoodi ja muoto ilman verkkoa.
2. **Välimuisti:** Kelvollisten, edelleen voimassa olevien tulosten välitön palautus.
3. **Yksittäinen lento:** identtiset verokoodi+kyselyparit yhdistetään yhdeksi JVM:ksi.
4. **Sisäänpääsy:** async- ja verkkorajoitukset suojaavat muistia ja VIES:iä.
5. **HTTP:** uudelleen käytetty JDK `HttpClient`-yhteys lyhyellä aikakatkaisulla.
6. **Vahvista/vastauksen tarkistus:** epätäydellinen looginen arvo tai päivämäärä →`MALFORMED_RESPONSE`.
7. **Kirjoitus välimuistiin:** vain aito `Valid`-tulos tallennetaan välimuistiin.

## Kaksikieliset virhevastaukset / Kaksikieliset virhevastaukset

Koneen virhekoodi on aina vakaa ja kielestä riippumaton.`error()` on unkaria ja englantia
antaa käyttäjälle tekstin ja uudelleenyrityspäätöksen:

```java
var response = vies.check("HU00000000");
response.error().ifPresent(error -> {
    log.warn("{} | {} | retry={}", error.messageHu(), error.messageEn(), error.retryable());
});
```

| Tulos            |        HTTP |     Yritä uudelleen |     Välimuisti | Merkitys                                                                 |
| ---------------- | ----------: | ------------------: | -------------: | ------------------------------------------------------------------------ |
| `Valid`          |         200 |                  ei | kyllä/kyllä ​​ | VIES vahvistettu voimassa / VIES vahvistettu voimassa                    |
| `Invalid`        |         200 |                  ei |             ei | VIES ei vahvistanut sitä päteväksi / VIES ei vahvistanut voimassa olevaa |
| `Unavailable`    | 503 tai 429 | enimmäkseen/yleensä |             ei | Pätevyyspäätöstä ei tehty                                                |
| `MalformedInput` |         400 |                  ei |             ei | Syöte on korjattava / Syöte on korjattava                                |

## Kokoonpano (rakennusohjelma)

| Asetus                            | Perusarvo                 | Mitä tekee                                                                                                |
| --------------------------------- | ------------------------- | --------------------------------------------------------------------------------------------------------- |
| `baseUrl(String)`                 | virallinen VIES REST URL  | Uudelleenohjaus valetestissä                                                                              |
| `connectTimeout(Duration)`        | 5 s                       | TCP/TLS-yhteyden aikakatkaisu                                                                             |
| `requestTimeout(Duration)`        | 8 s                       | Pyynnön kokonaisaikakatkaisu (VIES on interaktiivinen, pidä se lyhyenä)                                   |
| `admissionTimeout(Duration)`      | 2 s                       | Näin kauan se odottaa vapaata verkkotilaa                                                                 |
| `defaultRequester(ViesRequester)` | ei ole                    | Oma yhteisön veronumero → konsultaatiotunnus                                                              |
| `retries(int)`                    | 0                         | Automaattinen uudelleenyritys ohimenevien virheiden yhteydessä (0-5, eksponentiaalinen peruutus + värinä) |
| `retryDelay(Duration)`            | 400 ms                    | Eksponentiaalinen backoff oletusarvo                                                                      |
| `maxConcurrentRequests(int)`      | 32                        | Samanaikaisten todellisten VIES-verkkopyyntöjen yläraja                                                   |
| `maxPendingSyncRequests(int)`     | 512                       | Muistiraja samanaikaisille synkronoiduille soittajille; sen yläpuolella `CLIENT_OVERLOADED`               |
| `maxPendingAsyncRequests(int)`    | 512                       | Muistiraja aktiivisille/vireileville asynkronointitoiminnoille; sen yläpuolella `CLIENT_OVERLOADED`       |
| `cacheTtl(Duration)`              | 24 tuntia                 | Kelvollisten osumien välimuistiaika                                                                       |
| `cacheMaxEntries(int)`            | 10 000                    | Sisäänrakennetun välimuistin kokorajoitus                                                                 |
| `cache(ViesCache)`                | sisäänrakennettu          | Oma välimuisti-backend (esim. Redis)                                                                      |
| `disableCache()`                  | —                         | Ei tallennettua välimuistia; samanaikaiset identtiset kutsut voivat jakaa single-flightin                  |
| `userAgent(String)`               | moduulitunnus             | Sinun tulee tunnistaa itsesi EU:hun                                                                       |
| `executor(ExecutorService)`       | virtuaalinen säie/tehtävä | Oma asynkroninen toimeenpanijani; soittaja on vastuussa sen elinkaaresta                                  |

## Oma välimuisti (esim. Redis)

```java
class RedisViesCache implements ViesCache {
    public Optional<ViesResponse.Valid> get(String key) { /* ... */ }
    public void put(String key, ViesResponse.Valid value, Duration ttl) { /* ... */ }
}

var vies = ViesClient.builder().cache(new RedisViesCache()).build();
```

Vain tulos `Valid` tallennetaan välimuistiin – virheellinen numero ja ohimenevä virhe ei koskaan.
Käytä Redis-sovittimessa lyhyttä välimuistin aikakatkaisua, versioitua nimiavaruutta ja omaa nimiavaruutta
virhemittari. Välimuistin lukuvirhe `CACHE_ERROR` aiheuttaa sen, kun Redis ei ole käytössä
älä aloita hallitsematonta VIES-pyyntöä.
Välimuistista palautetut `consultationNumber` ja`requestDate` ovat **alkuperäisiä**
dokumentoida kuuleminen; välimuistiosuma ei ole uusi VIES-tarkistus. Tuoreelle todistukselle
tarkista `fromCache`, käytä lyhyttä TTL:tä tai`disableCache()`.

## Semantiikka, joka kannattaa tietää

1. **`Unavailable`≠`Invalid`.** Jäsenvaltioiden taustajärjestelmät säännöllisesti
   putoavat (`MS_UNAVAILABLE`,`MS_MAX_CONCURRENT_REQ`...). Tässä tapauksessa numero a
   **ei arvioi VIES** — kaupallisesti kiellettyä pitää mitättömänä.
2. **Formaattien esisuodatus ei ole VIES-todennus.** Moduuli suodattaa rekisteröidyt
   väärä syöttö (`MalformedInput`) ennen verkkoon menoa, mutta kelvollisuus
   sen lähde on aina VIESin vastaus.
3. **Uudelleenyritys lisää myös kuormitusta.** Kokeile vain väliaikaista virhettä paikallisesti muutaman kerran
   jälleen; suuressa operaatiossa kestävän jonon viivästetty uudelleenyritysmekanismi on ensisijainen.
4. **Kreikka `EL`, Pohjois-Irlanti`XI`.** Moduuli käsittelee molemmat,`GR`kartoittaa syötteen automaattisesti`EL`:hen.

## Testit

```bash
./mvnw test    # unit + helyi HTTP konkurencia/retry/backpressure/lifecycle tesztek
```

---

## Englanninkielinen pikaopas

Nollariippuvuus Java 21+ -asiakas EU:n komission VIES-alv-numerolle
validointi REST API. Rakenna `./mvnw package`:lla, sitten:

```java
try (var vies = ViesClient.createDefault()) {
    if (vies.check("DE000000000") instanceof ViesResponse.Valid v) {
        System.out.println(v.traderName().orElse("?"));
    }
}
```

Tulokset ovat suljettu hierarkia (`Valid`/`Invalid`/`Unavailable`/`MalformedInput`) suunniteltu tyhjentävälle kuvion yhteensovittamiselle`switch`. Voimassa
tulokset tallennetaan välimuistiin 24 tunnin ajan; ohimenevät VIES-katkokset raportoidaan muodossa `Unavailable`, ei koskaan kuin`Invalid`. Virallinen API-dokumentaatio:
<https://ec.europa.eu/taxation_customs/vies/#/technical-information>.

## Laaja-alainen toiminta

Tämä asiakas voi olla osa miljoonan tuotteen käsittelyputkea, mutta
VIES:n todellinen läpijuoksu rajoittuu vaihteleviin, takaamattomiin EU- ja jäsenmaihin
rajoja. Virtuaaliset säikeet vähentävät estokustannuksia; ne eivät kasva ylävirtaan
kapasiteettia. Käytä kestävää osioitua jonoa, rajoitettuja kuluttajia, jaettua Redistä
välimuisti ja hajautettu nopeuden rajoitin kaikille työntekijöille. Paikallinen yhden lennon ja
semaforit suojaavat vain yhtä JVM:ää. Älä koskaan käsittele `Unavailable`:tä`Invalid`:nä; käyttää`response.error()` vakaille koodeille, uudelleenkoettavuudelle ja unkarin/englanninkielisille viesteille.

## Avoin lähdekoodi / avoin lähdekoodi

Projektia voidaan käyttää, muokata ja
voidaan jakaa. Lisenssi sallii kaupallisen käytön ja pikakäytön
myöntää patenttilisenssin; lisenssi-, nimeämis- ja muokkausehdot on luettava
vartioimaan. Lue ennen osallistumista [CONTRIBUTING.md](CONTRIBUTING.md) and
[CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)-tiedostot.
Tämä projekti on lisensoitu [Apache License 2.0](../../../LICENSE)-lisenssillä, salliva
lisenssi, jossa on selkeä patentti. Katso [CONTRIBUTING.md](CONTRIBUTING.md),
[SECURITY.md](SECURITY.md), ja yksityiskohtaiset [avoimen lähdekoodin muistiinpanot](OPEN_SOURCE.md).

## Tuki ja lahjoitukset

Yhteisön tukea tarjotaan parhaan kykynsä mukaan GitHub-ongelmien ja -keskustelujen kautta.
Ilmoita turvallisuusvirheistä aina yksityisellä kanavalla. Projektin ylläpito on merkittävää
liittyy kehitys- ja infrastruktuurikustannuksiin; GitHubin Lahjoita/Sponsoroi-painike on ylläpitäjä
tulee näkyviin vahvistetun tuki-URL-osoitteen asettamisen jälkeen.
Yhteisön tuki on parasta GitHub-ongelmien ja keskustelujen kautta. Turvallisuus
raporttien tulee pysyä yksityisinä. Lahjoita/sponsoroi-painike otetaan käyttöön sen jälkeen
ylläpitäjän vahvistettu rahoituksen URL-osoite on määritetty.
