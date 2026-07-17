# Lietuvių (lt) — vies-client — PVM kodo tikrintuvas

**Paieškos frazės:** PVM mokėtojo kodo tikrinimas, PVM kodo validatorius, ES PVM patikra, mokesčių mokėtojo identifikatoriaus tikrinimas, VIES Java klientas; `VAT checker`, `VAT number validator`, `EU VAT validation`, `tax ID checker`.

Tai nėra bendro pobūdžio mokesčių skaičiuoklė, o klientas ES PVM identifikaciniams numeriams tikrinti per VIES.

> [Kalbų pasirinkimas](../../LANGUAGES.md) · Ši lokalizacija skirta prieinamumui. Esant neatitikimui, pirmenybę turi kanoninis angliškas techninis ar teisinis šaltinis. Šakniniai `LICENSE` ir`NOTICE` lieka teisiškai privalomi.

[![Licencija: Apache-2.0](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](../../../LICENSE)[![Java 21+](https://img.shields.io/badge/Java-21%2B-orange.svg)](../../../pom.xml)
🌍 **[Nyelv / Kalba / Visos oficialios ES kalbos](../../LANGUAGES.md)**
Atskiras, **nulinės vykdymo trukmės priklausomybės** Java klientas, skirtas Europos Komisijai VIES
(PVM informacijos mainų sistema), skirta mokesčių numerių tikrintuvo REST API. Bet kokia Java
gali būti prijungtas prie API serverio ar programos šiomis kalbomis: Spring Boot, Quarkus, Micronaut,
arba net paprastas JDK `HttpServer`– modulis naudoja tik JDK (`java.net.http`),
nėra tranzityvinio priklausomybės medžio.
Nepriklausomas atvirojo kodo projektas; ne Europos Komisija, ES ar valstybės narės
yra oficialus mokesčių institucijų produktas ir nėra jų patvirtintas ar sertifikuotas.

- **Reikalavimas:** Java **21+** baitų kodas / API; visas paketas patikrintas JDK 21.
- **Oficiali VIES dokumentacija:** <https://ec.europa.eu/taxation_customs/vies/#/technical-information>
  – **Vadinamas galutinis taškas:**`https://ec.europa.eu/taxation_customs/vies/rest-api/ms/{countryCode}/vat/{vatNumber}`
- **Valstybės narės kontaktinis asmuo:**`https://ec.europa.eu/taxation_customs/vies/rest-api/check-status`

## Dokumentacija / Dokumentacija

- [Įdiegimas / montavimas](INSTALLATION.md)
- [Prijungimas / integravimas](INTEGRATION.md)
- [Techninė struktūra / Techninis projektas](TECHNICAL.md)
- [Padalinys, integracijos ir konkurencijos testai /](TESTING.md) testavimas
- [Atvirojo kodo ir licencijos sprendimas / Atvirojo kodo](OPEN_SOURCE.md)
- [Atleidimo vadovas / Atleidimas](RELEASING.md)
- [Prideda / prisideda prie](CONTRIBUTING.md)
- [Saugos politika / Sauga](SECURITY.md)
- [Elgesio kodeksas / Elgesio kodeksas](CODE_OF_CONDUCT.md)
- [Parama ir aukos / Parama ir aukos](SUPPORT.md)
- [Trečiųjų šalių pranešimai](THIRD_PARTY_NOTICES.md)
- [Changelog](CHANGELOG.md)

## Sukurti ir prijungti

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

**Tikrasis JPMS modulis.** Stiklainis veikia kaip pavadintas modulis (`jar --describe-module`):

```
vies.client@1.2.0
exports vies.client
requires java.net.http
contains vies.client.internal   ← nem exportált, zárt belső csomag
```

Prisijunkite iš modulinės programos, tokios kaip ši:

```java
module my.api.server {
    requires vies.client;
}
```

„Classpath“ (nemoduliuotame, „tradiciniame“ projekte) veikia taip pat –`module-info` šiuo atveju tiesiog negroja. Net ir be Maven/Gradle
gali būti naudojamas: šaltinio medį pagal `src/main/java` galima nukopijuoti į savo projektą
(jei jūsų projektas nėra modulinis, palikite `module-info.java`).

## Greitas pavyzdys

```java
import vies.client.*;

try (var vies = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER"))) // patikima paslaptis / konfigūracija
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

`switch` yra išsamus: kompiliatorius garantuoja, kad sutvarkėte visus keturis išėjimus
(užantspauduota sąsaja + modelio atitikimas).

## Kodėl `defaultRequester` svarbus?

`defaultRequester` turi būti jūsų organizacijos PVM mokėtojo kodas, gaunamas iš
patikimos paslapties arba konfigūracijos. Gavus galiojantį atsakymą VIES **gali**
grąžinti `consultationNumber`, tačiau jis yra neprivalomas ir niekada
negarantuojamas. Jo teisinė įrodomoji vertė priklauso nuo vietinių taisyklių;
saugokite jį kartu su užklausos data ir įvestimi.

## Prisijungimas prie Spring Boot API serverio

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

Klientas yra nekintamas ir saugus – programa sukuria vieną egzempliorių
(singleton bean) ir uždarykite jį išjungę (`close`).
Raštas be rėmelių: [`examples/ViesDemoServer.java`](../../../examples/ViesDemoServer.java) (paprastas JDK`HttpServer`, virtualiose gijose):

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java     # port 8085
curl "http://localhost:8085/vat-check?number=HU00000000"
```

## Asinchroninis naudojimas

```java
CompletableFuture<ViesResponse> future = vies.checkAsync("PL0000000000");
```

Pagal numatytuosius nustatymus asinchroniniai skambučiai vykdomi **virtualiose gijose** („Project Loom“) – daug
lygiagrečiai tikrinant taip pat nėra platformos sriegio atliekų. Nuosavas vykdytojas a
galima įvesti į statybininką (`executor(...)`— jo neuždarė klientas).
Iki 512 **individualių asinchroninio lyderio veiksmų** iš asinchroninių klientų bazės
gautas už kliento atvejį. Vienas talpyklos nuskaitymas užima vietą trumpam laikui;
sugedusi įvestis ir tas pats vieno skrydžio sekėjas neužima atskiros vietos. Perkrovos atveju nedelsiant pateikti naują individualų prašymą
Gaunate `Unavailable(..., "CLIENT_OVERLOADED")` rezultatą. Tai yra kliento viduje
riboja darbą; skambinančiojo saugoma įvesties eilė ir ateities sandoriai
turi būti apribotas.

## Kelių milijonų užklausų gamykla

Milijonams vartotojų nepradėkite milijonų ateities sandorių viename JVM ir nedarykite
leisti neribotą tiesioginį srautą į VIES. Siūloma struktūra:

> Klientas gali būti milijono darbo eilės apdorojimo komponentas, tačiau faktinis
> VIES perdavimą riboja besikeičiančios, negarantuotos ES ir valstybių narių sistemų ribos
> būti pasiryžęs. Virtualios gijos sumažina laukimo išlaidas; prieš srovę
> pajėgumai nepadidinami.

1. Gaunamus čekius gauna nuolatinė, suskaidyta pranešimų eilė.
2. Keletas horizontalių darbuotojų sunaudoja liniją ribotomis porcijomis.
3. Vienas pavienis `ViesClient` veikia su virtualiomis gijomis vienam darbuotojui.
4. Darbuotojai naudoja bendrą Redis talpyklą per `ViesCache` sąsają.
5. Visuotinis / paskirstytas greičio ribotuvas apsaugo ES ir valstybių narių VIES galinius taškus.
   JVM klientas sujungiamas ir atvyksta tuo pačiu metu
   mokesčių numerio / užklausos poros (vieno skrydžio), todėl talpyklos praleidimas to nesukelia
   „užklausos spūstis“.`maxConcurrentRequests` riboja tikrasis tinklas
   užklausų, o `maxPendingAsyncRequests` ir`maxPendingSyncRequests` atmintyje
   suteikia momentinį priešslėgį. Visi yra JVM vietiniai. Suvestinis kelių darbuotojų srautas
   turi būti reguliuojamas bendru, paskirstytu ribotuvu. Nuolatinė įvesties eilė ir
   vartotojų grupė vis tiek turėtų būti apribota programa.
   Sunkaus krovinio darbuotojo pavyzdys:

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

Rezultatas `Unavailable`, įskaitant reikšmę`CLIENT_OVERLOADED`, yra nuolatinis
turite bandyti dar kartą su vėlavimu eilėje.`MalformedInput` ir`Invalid` nereikėtų bandyti dar kartą.

## Užklausos kelias / užklausos gyvavimo ciklas

1. **Normalizuoti / Normalizuoti:** patikrinkite šalies kodą ir formatą be tinklo.
2. **Talpykla:** nedelsiant grąžinami galiojantys, vis dar galiojantys rezultatai.
3. **Vienas skrydis:** identiškos mokesčių kodo ir užklausos poros sujungiamos vienoje JVM.
4. **Įėjimas:** asinchronizavimo ir tinklo apribojimai apsaugo atmintį ir VIES.
5. **HTTP:** pakartotinai naudojamas JDK `HttpClient` ryšys su trumpu skirtuoju laiku.
6. **Patvirtinti / Atsakymų patikra:** neužbaigta loginė reikšmė arba data →`MALFORMED_RESPONSE`.
7. **Rašymas talpykloje:** talpykloje saugomas tik autentiškas `Valid` rezultatas.

## Dvikalbių klaidų atsakymai / Dvikalbių klaidų atsakymai

Mašinos klaidos kodas visada yra stabilus ir nepriklausomas nuo kalbos.`error()` yra vengrų ir anglų kalbos
pateikia vartotojui tekstą ir sprendimą bandyti dar kartą:

```java
var response = vies.check("HU00000000");
response.error().ifPresent(error -> {
    log.warn("{} | {} | retry={}", error.messageHu(), error.messageEn(), error.retryable());
});
```

| Rezultatas       |         HTTP |       Bandyti dar kartą |  Talpykla | Reikšmė                                                            |
| ---------------- | -----------: | ----------------------: | --------: | ------------------------------------------------------------------ |
| `Valid`          |          200 |                      ne | taip/taip | VIES patvirtintas kaip galiojantis / VIES patvirtintas galiojantis |
| `Invalid`        |          200 |                      ne |        ne | VIES nepatvirtino, kad jis galioja / VIES nepatvirtino galiojančio |
| `Unavailable`    | 503 arba 429 | dažniausiai/dažniausiai |        ne | Sprendimas dėl galiojimo nebuvo priimtas                           |
| `MalformedInput` |          400 |                      ne |        ne | Įvestis turi būti pataisyta / Įvestis turi būti pataisyta          |

## Konfigūracija (statybininkas)

| Nustatymas                        | Bazinė vertė            | Ką daro                                                                                                  |
| --------------------------------- | ----------------------- | -------------------------------------------------------------------------------------------------------- |
| `baseUrl(String)`                 | oficialus VIES REST URL | Peradresavimas imitaciniame teste                                                                        |
| `connectTimeout(Duration)`        | 5 s                     | TCP/TLS ryšio skirtasis laikas                                                                           |
| `requestTimeout(Duration)`        | 8 s                     | Bendras užklausos skirtasis laikas (VIES yra interaktyvus, trumpai)                                      |
| `admissionTimeout(Duration)`      | 2 s                     | Tiek laiko jis laukia laisvos vietos tinkle                                                              |
| `defaultRequester(ViesRequester)` | nėra                    | Nuosavas bendruomenės mokesčių numeris → konsultacijos ID                                                |
| `retries(int)`                    | 0                       | Automatinis pakartotinis bandymas dėl pereinamųjų klaidų (0–5, eksponentinis atsitraukimas + virpėjimas) |
| `retryDelay(Duration)`            | 400 ms                  | Eksponentinis atgalinis numatytasis                                                                      |
| `maxConcurrentRequests(int)`      | 32                      | Viršutinė lygiagrečių realių VIES tinklo užklausų riba                                                   |
| `maxPendingSyncRequests(int)`     | 512                     | Vienu metu sinchronizuojamų skambintojų atminties limitas; virš jo `CLIENT_OVERLOADED`                   |
| `maxPendingAsyncRequests(int)`    | 512                     | Atminties limitas aktyvioms / laukiančioms asinchronizavimo operacijoms; virš jo `CLIENT_OVERLOADED`     |
| `cacheTtl(Duration)`              | 24 valandos             | Galiojančių įvykių talpyklos laikas                                                                      |
| `cacheMaxEntries(int)`            | 10 000                  | Integruotos atminties talpyklos dydžio apribojimas                                                       |
| `cache(ViesCache)`                | įmontuotas              | Nuosavas talpyklos užpakalinė programa (pvz., Redis)                                                     |
| `disableCache()`                  | —                       | Nėra saugomos talpyklos; vienodos lygiagrečios užklausos gali dalytis vienu single-flight kvietimu       |
| `userAgent(String)`               | modulio ID              | Turėtumėte identifikuoti save ES                                                                         |
| `executor(ExecutorService)`       | virtuali gija/užduotis  | Mano asinchroninis vykdytojas; skambinantis asmuo yra atsakingas už jo gyvavimo ciklą                    |

## Nuosava talpykla (pvz., Redis)

```java
class RedisViesCache implements ViesCache {
    public Optional<ViesResponse.Valid> get(String key) { /* ... */ }
    public void put(String key, ViesResponse.Valid value, Duration ttl) { /* ... */ }
}

var vies = ViesClient.builder().cache(new RedisViesCache()).build();
```

Talpykloje saugomas tik rezultatas `Valid`– neteisingas numeris ir laikina klaida niekada.
Redis adapteriui naudokite trumpą talpyklos skirtąjį laiką, versijų vardų erdvę ir savo
klaidų metrika. Talpyklos skaitymo klaida `CACHE_ERROR` atsiranda, kai „Redis“ neveikia
nepradėkite nekontroliuojamo VIES užklausos štampo.
Iš talpyklos grąžinti `consultationNumber` ir `requestDate` priklauso **pradiniam**
patikrinimui; talpyklos įvykis nėra naujas VIES patikrinimas. `disableCache()` išjungia
tik saugomą talpyklą: vienodos lygiagrečios VAT+requester užklausos gali dalytis vienu
single-flight tinklo kvietimu, o vėlesnė užklausa po jo užbaigimo siunčia naują kvietimą.
`consultationNumber` ir tokiu atveju lieka neprivalomas.

## Semantika, kurią verta žinoti

1. **`Unavailable`≠`Invalid`.** Valstybės narės foninės sistemos reguliariai
   yra nukritę (`MS_UNAVAILABLE`,`MS_MAX_CONCURRENT_REQ`...). Šiuo atveju skaičius a
   **nevertinamas VIES** – komerciniu požiūriu draudžiama laikyti negaliojančiu.
2. **Išankstinis formato filtravimas nėra VIES autentifikavimas.** Modulis filtruoja registruotus
   neteisinga įvestis (`MalformedInput`) prieš prisijungiant prie tinklo, bet galiojimas
   jo šaltinis visada yra VIES atsakymas.
3. **Pakartotinis bandymas taip pat padidina apkrovą.** Tik keletą kartų pabandykite padaryti laikiną klaidą vietoje
   vėl; atliekant didelę operaciją, patvarios eilės uždelsto pakartotinio bandymo mechanizmas yra pagrindinis.
4. **Graikija `EL`, Šiaurės Airija`XI`.** Modulis valdo abu,`GR`automatiškai susieja įvestį į`EL`.

## Testai

```bash
./mvnw test    # unit + helyi HTTP konkurencia/retry/backpressure/lifecycle tesztek
```

---

## Greita pradžia anglų kalba

Nulinės priklausomybės Java 21+ klientas, skirtas ES Komisijos VIES PVM mokėtojo numeriui
patvirtinimo REST API. Sukurkite naudodami `./mvnw package`, tada:

```java
try (var vies = ViesClient.createDefault()) {
    if (vies.check("DE000000000") instanceof ViesResponse.Valid v) {
        System.out.println(v.traderName().orElse("?"));
    }
}
```

Rezultatai yra sandari hierarchija (`Valid`/`Invalid`/`Unavailable`/`MalformedInput`), skirtas išsamiam modelio derinimui`switch`. Galioja
rezultatai saugomi atmintyje 24 valandas; apie laikinus VIES gedimus pranešama kaip `Unavailable`, niekada kaip `Invalid`. Oficiali API dokumentacija:
<https://ec.europa.eu/taxation_customs/vies/#/technical-information>.

## Didelio masto veikimas

Šis klientas gali būti viena iš milijono prekių apdorojimo vamzdyno sudedamųjų dalių, tačiau
faktinis VIES pralaidumas ribojamas kintamos, negarantuotos ES ir valstybės narės
ribos. Virtualios gijos sumažina blokavimo išlaidas; jų nepadaugėja prieš srovę
talpa. Naudokite patvarią skaidytą eilę, ribotus vartotojus, bendrą Redis
talpyklą ir paskirstytą greičio ribotuvą visiems darbuotojams. Vietinis vienkartinis skrydis ir
semaforai apsaugo tik vieną JVM. Niekada nelaikykite `Unavailable` kaip `Invalid`; naudoti`response.error()` stabiliems kodams, pakartotiniam bandymui ir vengrų/angliškų pranešimų siuntimui.

## Atvirojo kodo / Atvirojo kodo

Projektas gali būti naudojamas, modifikuojamas ir
galima platinti. Licencija leidžia naudoti komerciniais tikslais ir skubiai
suteikia patento licenciją; būtina perskaityti licencijos, priskyrimo ir modifikavimo sąlygas
saugoti. Prieš prisidėdami, perskaitykite [CONTRIBUTING.md](CONTRIBUTING.md) ir
[CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) failai.
Šis projektas yra licencijuotas pagal [Apache License 2.0](../../../LICENSE), leistinas
licencija su aiškiu patento suteikimu. Žr. [CONTRIBUTING.md](CONTRIBUTING.md),
[SECURITY.md](SECURITY.md), ir išsamios [atvirojo kodo pastabos](OPEN_SOURCE.md).

## Parama ir aukos

Bendruomenės parama teikiama kaip įmanoma geriau, per „GitHub“ problemas ir diskusijas.
Visada praneškite apie saugos klaidas privačiame kanale. Projekto priežiūra yra svarbi
apima plėtros ir infrastruktūros išlaidas; Mygtukas „GitHub Donate/Sponsor“ yra prižiūrėtojas
pasirodo nustačius patvirtintą palaikymo URL.
Bendruomenės palaikymas yra geriausias per „GitHub“ problemas ir diskusijas. Saugumas
ataskaitos turi likti privačios. Mygtukas „Paaukoti / Rėmėti“ bus įjungtas po to
prižiūrėtojo patvirtintas finansavimo URL sukonfigūruotas.
