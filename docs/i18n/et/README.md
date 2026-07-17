# Eesti (et) — vies-client — KMKR numbri kontrollija

`disableCache()` lülitab salvestatud/püsiva vahemälu välja, kuid sama KMKR numbri + requester'i paariga samaaegsed kutsed võivad jagada üht single-flight-võrgupäringut. Pärast selle lõppemist teeb hilisem kutse uue VIES-i päringu. `consultationNumber` on valikuline ja VIES võib selle tagastada, kuid see pole kunagi garanteeritud; selle õiguslik tõendusväärtus sõltub kohalikest reeglitest. Laadige `MY_EU_VAT_NUMBER` ainult usaldatud saladuste/konfiguratsiooni allikast.

**Otsinguterminid:** KMKR numbri kontroll, käibemaksukohustuslase numbri valideerimine, ELi käibemaksunumbri valideerimine, maksukohustuslase ID kontroll, VIES Java klient; `VAT checker`, `VAT number validator`, `EU VAT validation`, `tax ID checker`.

See ei ole üldine maksukalkulaator, vaid VIES-i kaudu ELi käibemaksukohustuslase numbreid kontrolliv klient.

> [Keelevalik](../../LANGUAGES.md) · See lokaliseering parandab ligipääsetavust. Lahknevuse korral kehtib kanooniline ingliskeelne tehniline või õiguslik allikas. Juure `LICENSE` ja`NOTICE` jäävad õiguslikult määravaks.

[![Litsents: Apache-2.0](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](../../../LICENSE)[![Java 21+](https://img.shields.io/badge/Java-21%2B-orange.svg)](../../../pom.xml)
🌍 **[Nyelv / Keel / Kõik ELi ametlikud keeled ​​](../../LANGUAGES.md)**
Eraldiseisev, **null käitusajast sõltuv** Java klient Euroopa Komisjoni VIES jaoks
(käibemaksu teabevahetussüsteem) maksunumbrite kontrollija REST API jaoks. Igasugune Java
saab ühendada API serveri või programmiga järgmistes keeltes: Spring Boot, Quarkus, Micronaut,
või isegi tavaline JDK `HttpServer`- moodul kasutab ainult JDK-d (`java.net.http`),
transitiivset sõltuvuspuud ei ole.
Sõltumatu avatud lähtekoodiga projekt; mitte Euroopa Komisjon, EL ega liikmesriigid
on maksuameti ametlik toode ja see pole nende poolt kinnitatud ega sertifitseeritud.

- **Nõued:** Java **21+** baitkood/API; kogu pakett on kinnitatud JDK 21-s.
- **Ametlik VIES-dokumentatsioon:** <https://ec.europa.eu/taxation_customs/vies/#/technical-information>
- **Niinimetatud lõpp-punkt:**`https://ec.europa.eu/taxation_customs/vies/rest-api/ms/{countryCode}/vat/{vatNumber}`
- **Liikmesriigi kontakt:**`https://ec.europa.eu/taxation_customs/vies/rest-api/check-status`

## Dokumentatsioon / Dokumentatsioon

- [Paigaldamine / Paigaldamine](INSTALLATION.md)
- [Ühendus / integreerimine](INTEGRATION.md)
- [Tehniline struktuur / Tehniline projekt](TECHNICAL.md)
- [Ühiku-, integratsiooni- ja võistlustestid /](TESTING.md) testimine
- [Avatud lähtekoodi ja litsentsiotsus / Avatud lähtekoodiga](OPEN_SOURCE.md)
- [Vabastamisjuhend / Vabastamine](RELEASING.md)
- [GitHubi väljaanne / GitHubi väljaanne](GITHUB_SETUP.md)
- [Kaastöö / kaasaaitamine](CONTRIBUTING.md)
- [Turvapoliitika / Turvalisus](SECURITY.md)
- [Käitumiskoodeks / Käitumisjuhend](CODE_OF_CONDUCT.md)
- [Toetus ja annetused / Toetus ja annetused](SUPPORT.md)
- [Kolmanda osapoole teated](THIRD_PARTY_NOTICES.md)
- [Muudatustelog / Changelog](CHANGELOG.md)

## Ehitamine ja ühendamine

```bash
./mvnw install    # tesztek + target/vies-client-1.2.0.jar (+ -sources.jar, -javadoc.jar)
               # és telepítés a lokális Maven-repóba
```

**Maven:**

```xml
<dependency>
    <groupId>vies.client</groupId>
    <artifactId>vies-client</artifactId>
    <version>1.2.0</version>
</dependency>
```

**Gradle:**

```kotlin
implementation("vies.client:vies-client:1.2.0")
```

**Päris JPMS-moodul.** Purk käitub nimega moodulina (`jar --describe-module`):

```
vies.client@1.2.0
exports vies.client
requires java.net.http
contains vies.client.internal   ← nem exportált, zárt belső csomag
```

Ühendage sellisest modulaarsest rakendusest:

```java
module my.api.server {
    requires vies.client;
}
```

Classpathist (moduleerimata, "traditsioonilises" projektis) töötab see samamoodi

- `module-info` sel juhul lihtsalt ei mängi. Isegi ilma Maven/Gradleta
  saab kasutada:`src/main/java` all oleva lähtepuu saab kopeerida oma projekti
  (jätke `module-info.java`, kui teie projekt ei ole modulaarne).

## Kiire näide

```java
import vies.client.*;

try (var vies = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER"))) // usaldatud saladus/seadistus
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

`switch` on ammendav: kompilaator garanteerib, et olete käsitlenud kõiki nelja väljundit
(suletud liides + mustri sobitamine).

## Miks on `defaultRequester` oluline?

Andke requester'ina organisatsiooni enda KMKR number usaldatud saladuste/konfiguratsiooni allikast. Kehtiva tulemuse korral võib VIES tagastada valikulise `consultationNumber`-i, kuid see pole kunagi garanteeritud. Selle tõendusväärtus sõltub kohalikest reeglitest; säilitage ka aeg, küsitud KMKR number ja tulemus vastavalt auditi- ja andmekaitsepoliitikale.

## Ühendus Spring Boot API serveriga

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

Klient on muutumatu ja lõimekindel – rakendus loob ühe eksemplari
(singleton bean) ja sulgege see väljalülitamisel (`close`).
Raamita muster: [`examples/ViesDemoServer.java`](../../../examples/ViesDemoServer.java) (tavaline JDK`HttpServer`, virtuaalsetel lõimedel):

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java     # port 8085
curl "http://localhost:8085/vat-check?number=HU00000000"
```

## Asünkroonne kasutamine

```java
CompletableFuture<ViesResponse> future = vies.checkAsync("PL0000000000");
```

Vaikimisi töötavad asünkroonsed kõned **virtuaalsetel lõimedel** (Project Loom) – palju
paralleelkontrollis pole ka platvormi keerme raiskamist. Oma käsutäitja a
saab sisestada ehitajasse (`executor(...)`— see pole kliendi poolt suletud).
Kuni 512 **individuaalset asünkroonilise juhi toimingut** asünkroonilise kliendibaasist
saadud iga kliendi eksemplari kohta. Üks vahemälu lugemine võtab ruumi lühikeseks ajaks;
vigane sisend ja seesama ühelennuline järgija ei võta eraldi ruumi. Ülekoormuse korral uus individuaalne taotlus kohe
Saate tulemuseks `Unavailable(..., "CLIENT_OVERLOADED")`. See on kliendisisene
piirab tööd; ka helistaja salvestatud sisestusjärjekord ja futuurid
peab olema piiratud.

## Mitme miljoni päringu tehas

Miljonite kasutajate jaoks ärge alustage miljoneid futuuriid ühes JVM-is ega tee seda
lubada piiramatut otseliiklust VIES-ile. Kavandatud struktuur:

> Klient võib olla töötlemise komponent miljoni tööjärjekorras, kuid tegelik
> VIES-i edastamist piiravad EL-i ja liikmesriikide süsteemide muutuvad, garanteerimata piirangud
> olla kindlaks määratud. Virtuaalsed lõimed vähendavad ootamise kulusid; ülesvoolu
> võimsust ei suurendata.

1. Sissetulevad tšekid võetakse vastu püsivasse jaotatud sõnumijärjekorda.
2. Mitmed horisontaalse mõõtkavaga töötajad tarbivad liini piiratud portsjonites.
3. Üks üksik `ViesClient` töötab virtuaalse lõimega töötaja kohta.
4. Töötajad kasutavad ühist Redise vahemälu `ViesCache` liidese kaudu.
5. Globaalne/hajutatud kiiruse piiraja kaitseb EL-i ja liikmesriikide VIES-i lõpp-punkte.
   JVM-is liidab klient sama, saabudes kohale samal ajal
   maksunumbri/päringu paarid (ühe lennuga), nii et vahemälu vahelejäämine seda ei põhjusta
   "päringutempo".`maxConcurrentRequests` on piiratud tegeliku võrguga
   päringuid ning `maxPendingAsyncRequests` ja`maxPendingSyncRequests` mällu
   annab kohese vasturõhu. Kõik on JVM-i kohalikud. Mitme töölise koondliiklus
   tuleb reguleerida ühise, hajutatud piirajaga. Püsiv sisendjärjekord ja
   tarbijate kogumit peaks rakendus siiski piirama.
   Raske koormusega töötaja näide:

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

Tulemus `Unavailable`– sealhulgas väärtus`CLIENT_OVERLOADED`– on püsiv
sa pead uuesti proovima järjekorra hilinemisega.`MalformedInput` ja`Invalid` ei tohiks uuesti proovida.

## Taotluse tee / taotluse elutsükkel

1. **Normaliseeri / Normaliseeri:** kontrollige riigikoodi ja vormingut ilma võrguta.
2. **Vahemälu:** kehtivate, endiselt reaalajas tulemuste viivitamatu tagastamine.
3. **Ühekordne lend:** identsed maksukoodi+päringu paarid liidetakse ühte JVM-i.
4. **Sissepääs:** asünkroonimis- ja võrgupiirangud kaitsevad mälu ja VIES-i.
5. **HTTP:** taaskasutatud JDK `HttpClient` ühendus lühikese ajalõpuga.
6. **Kinnita / vastuste kontroll:** puudulik tõeväärtus või kuupäev →`MALFORMED_RESPONSE`.
7. ** Vahemällu kirjutamine:** vahemällu salvestatakse ainult autentne `Valid` tulemus.

## Kakskeelsed veavastused / Kakskeelsed veavastused

Masina veakood on alati stabiilne ja keelest sõltumatu.`error()` on ungari ja inglise keel
annab kasutajale teksti ja uuesti proovimise otsuse:

```java
var response = vies.check("HU00000000");
response.error().ifPresent(error -> {
    log.warn("{} | {} | retry={}", error.messageHu(), error.messageEn(), error.retryable());
});
```

| Tulemus          |        HTTP |       Proovi uuesti | Vahemälu | Tähendus                                                        |
| ---------------- | ----------: | ------------------: | -------: | --------------------------------------------------------------- |
| `Valid`          |         200 |                  ei |  jah/jah | VIES kinnitatud kehtivaks / VIES kinnitatud kehtiv              |
| `Invalid`        |         200 |                  ei |       ei | VIES ei kinnitanud seda kehtivaks / VIES ei kinnitanud kehtivat |
| `Unavailable`    | 503 või 429 | enamasti/tavaliselt |       ei | Kehtivusotsust ei tehtud                                        |
| `MalformedInput` |         400 |                  ei |       ei | Sisend tuleb parandada / Sisend tuleb parandada                 |

## Konfiguratsioon (ehitaja)

| Seade                             | Baasväärtus              | Mida teeb                                                                                       |
| --------------------------------- | ------------------------ | ----------------------------------------------------------------------------------------------- |
| `baseUrl(String)`                 | ametlik VIES REST URL    | Ümbersuunamine proovitestis                                                                     |
| `connectTimeout(Duration)`        | 5 s                      | TCP/TLS ühenduse ajalõpp                                                                        |
| `requestTimeout(Duration)`        | 8 s                      | Kogu päringu ajalõpp (VIES on interaktiivne, hoidke seda lühidalt)                              |
| `admissionTimeout(Duration)`      | 2 s                      | Nii kaua ootab see vaba võrguruumi                                                              |
| `defaultRequester(ViesRequester)` | ei ole                   | Oma kogukonna maksunumber → konsultatsiooni ID                                                  |
| `retries(int)`                    | 0                        | Automaatne uuesti proovimine mööduvate vigade korral (0–5, eksponentsiaalne taganemine + värin) |
| `retryDelay(Duration)`            | 400 ms                   | Eksponentsiaalne taganemise vaikeseade                                                          |
| `maxConcurrentRequests(int)`      | 32                       | Samaaegsete reaalsete VIES-võrgupäringute ülempiir                                              |
| `maxPendingSyncRequests(int)`     | 512                      | Samaaegsete sünkroonimishelistajate mälupiirang; selle kohal `CLIENT_OVERLOADED`                |
| `maxPendingAsyncRequests(int)`    | 512                      | Mälupiirang aktiivsete/ootel asünkroonimistoimingute jaoks; selle kohal `CLIENT_OVERLOADED`     |
| `cacheTtl(Duration)`              | 24 tundi                 | Vahemälu aeg kehtivate tabamuste jaoks                                                          |
| `cacheMaxEntries(int)`            | 10 000                   | Sisseehitatud mälu vahemälu suuruse piirang                                                     |
| `cache(ViesCache)`                | sisseehitatud            | Oma vahemälu taustaprogramm (nt Redis)                                                          |
| `disableCache()`                  | —                        | Salvestatud cache puudub; samaaegsed identsed kutsed võivad jagada single-flight'i               |
| `userAgent(String)`               | mooduli ID               | Peaksite end ELis identifitseerima                                                              |
| `executor(ExecutorService)`       | virtuaalne lõim/ülesanne | Minu asünkroniseerija; helistaja vastutab oma elutsükli eest                                    |

## Oma vahemälu (nt Redis)

```java
class RedisViesCache implements ViesCache {
    public Optional<ViesResponse.Valid> get(String key) { /* ... */ }
    public void put(String key, ViesResponse.Valid value, Duration ttl) { /* ... */ }
}

var vies = ViesClient.builder().cache(new RedisViesCache()).build();
```

Vahemällu salvestatakse ainult tulemus `Valid`— kehtetu number ja mööduv viga ei ole kunagi.
Redise adapteri jaoks kasutage lühikest vahemälu ajalõppu, versiooniga nimeruumi ja oma nimeruumi
veamõõdik. Vahemälu lugemise tõrge `CACHE_ERROR` ilmneb siis, kui Redis ei tööta
ärge käivitage kontrollimatut VIES-i päringut.
Vahemälust tagastatud `consultationNumber` ja`requestDate` on **originaal**
dokumenteerib konsultatsiooni; vahemälu tabamus ei ole uus VIES-i kontroll. Värske tunnistuse eest
kontrollige `fromCache`, kasutage lühikest TTL-i või`disableCache()`.

## Semantika, mida tasub teada

1. **`Unavailable`≠`Invalid`.** Liikmesriikide taustsüsteemid regulaarselt
   on maha kukkunud (`MS_UNAVAILABLE`,`MS_MAX_CONCURRENT_REQ`...). Sel juhul on number a
   **ei hinnanud VIES** — äriliselt keelatud lugeda kehtetuks.
2. **Vormingu eelfiltreerimine ei ole VIES autentimine.** Moodul filtreerib registreeritud
   vale sisend (`MalformedInput`) enne võrku minekut, kuid kehtivus
   selle allikaks on alati VIESi vastus.
3. **Uuesti proovimine suurendab ka koormust.** Proovige ajutist viga kohapeal vaid paar korda
   uuesti; suure toimingu puhul on püsiva järjekorra viivitatud uuesti proovimise mehhanism esmane.
4. **Kreeka `EL`, Põhja-Iirimaa`XI`.** Moodul käsitleb mõlemat,`GR`kaardistab sisendi automaatselt`EL`-ga.

## Testid

```bash
./mvnw test    # unit + helyi HTTP konkurencia/retry/backpressure/lifecycle tesztek
```

---

## Ingliskeelne kiirstart

Nullsõltuvusega Java 21+ klient ELi Komisjoni VIES-i käibemaksukohustuslase numbri jaoks
valideerimise REST API. Ehitage `./mvnw package`-ga, seejärel:

```java
try (var vies = ViesClient.createDefault()) {
    if (vies.check("DE000000000") instanceof ViesResponse.Valid v) {
        System.out.println(v.traderName().orElse("?"));
    }
}
```

Tulemused on suletud hierarhia (`Valid`/`Invalid`/`Unavailable`/`MalformedInput`), mis on mõeldud`switch` mustri ammendava sobitamise jaoks. Kehtiv
tulemused salvestatakse mällu 24 tunniks; mööduvad VIES-i katkestused on teatatud kui `Unavailable`, mitte kunagi nagu`Invalid`. Ametlik API dokumentatsioon:
<https://ec.europa.eu/taxation_customs/vies/#/technical-information>.

## Suuremahuline töö

See klient võib olla miljoniüksuse töötlemise konveieri üks komponent, kuid
tegelik VIES läbilaskevõime on piiratud muutuva, garanteerimata EL ja liikmesriigiga
piirid. Virtuaalsed lõimed vähendavad blokeerimiskulusid; need ülesvoolu ei suurene
mahutavus. Kasutage püsivat jaotatud järjekorda, piiratud tarbijaid ja jagatud Redist
vahemälu ja hajutatud kiiruse piiraja kõikidele töötajatele. Kohalikud ühelennulised ja
semaforid kaitsevad ainult ühte JVM-i. Ärge kunagi käsitlege `Unavailable`-d kui`Invalid`-d; kasutada`response.error()` stabiilsete koodide, uuesti proovimise ja ungari-/ingliskeelsete sõnumite jaoks.

## Avatud lähtekood / avatud lähtekoodiga

Projekti saab kasutada, muuta ja
saab levitada. Litsents lubab ärilist kasutamist ja ekspresskasutust
annab patendilitsentsi; litsentsi-, omistamis- ja muutmistingimused tuleb läbi lugeda
valvama. Enne panuse andmist lugege [CONTRIBUTING.md](CONTRIBUTING.md) ja
[CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) failid.
See projekt on litsentsitud [Apache License 2.0](../../../LICENSE), lubava
selgesõnalise patendiandmisega litsents. Vaadake [CONTRIBUTING.md](CONTRIBUTING.md),
[SECURITY.md](SECURITY.md) ja üksikasjalikud [avatud lähtekoodiga märkmed](OPEN_SOURCE.md).

## Toetus ja annetused

Kogukonna tuge pakutakse parimal viisil GitHubi probleemide ja arutelude kaudu.
Teatage alati privaatkanali turvatõrgetest. Projekti hooldus on märkimisväärne
hõlmab arendus- ja infrastruktuurikulusid; GitHubi annetamise/sponsoreerimise nupp on hooldaja
kuvatakse pärast teie kinnitatud toe URL-i määramist.
Kogukonna tugi on parim GitHubi probleemide ja arutelude kaudu. Turvalisus
aruanded peavad jääma privaatseks. Pärast seda lubatakse nupp Anneta/Sponsor
hooldaja kinnitatud rahastamise URL on konfigureeritud.
