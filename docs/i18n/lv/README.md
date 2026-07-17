# Latviešu (lv) — vies-client — PVN numura pārbaudītājs

**Meklēšanas frāzes:** PVN numura pārbaude, PVN numura validācija, ES PVN validācija, nodokļu identifikatora pārbaude, VIES Java klients; `VAT checker`, `VAT number validator`, `EU VAT validation`, `tax ID checker`.

Tas nav vispārīgs nodokļu kalkulators, bet klients ES PVN identifikācijas numuru pārbaudei VIES sistēmā.

> [Valodu izvēle](../../LANGUAGES.md) · Šī lokalizācija uzlabo pieejamību. Atšķirību gadījumā noteicošais ir kanoniskais angļu tehniskais vai juridiskais avots. Saknes `LICENSE` un`NOTICE` paliek juridiski saistoši.

[![Licence: Apache-2.0](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](../../../LICENSE)[![Java 21+](https://img.shields.io/badge/Java-21%2B-orange.svg)](../../../pom.xml)
🌍 **[Nyelv / Valoda / Visas ES oficiālās valodas](../../LANGUAGES.md)**
Atsevišķs, **nulles izpildlaika atkarības** Java klients Eiropas Komisijai VIES
(PVN informācijas apmaiņas sistēma) nodokļu numura pārbaudītāja REST API. Jebkura Java
var savienot ar API serveri vai programmu šādās valodās: Spring Boot, Quarkus, Micronaut,
vai pat vienkāršs JDK `HttpServer`— modulis izmanto tikai JDK (`java.net.http`),
nav pārejošas atkarības koka.
Neatkarīgs atvērtā koda projekts; nevis Eiropas Komisija, ES vai dalībvalstis
ir oficiāls nodokļu iestāžu produkts, un tās nav apstiprinājušas vai sertificējušas.

- **Prasība:** Java **21+** baitu kods/API; visa pakete ir pārbaudīta JDK 21.
- **Oficiālā VIES dokumentācija:** <https://ec.europa.eu/taxation_customs/vies/#/technical-information>
  - **Izsauktais galapunkts:**`https://ec.europa.eu/taxation_customs/vies/rest-api/ms/{countryCode}/vat/{vatNumber}`
- **Dalībvalsts kontaktpersona:**`https://ec.europa.eu/taxation_customs/vies/rest-api/check-status`

## Dokumentācija / Dokumentācija

- [Uzstādīšana / Uzstādīšana](INSTALLATION.md)
- [Savienojums / Integrācija](INTEGRATION.md)
- [Tehniskā uzbūve / Tehniskais projekts](TECHNICAL.md)
- [Vienība, integrācijas un sacensību testi /](TESTING.md) testēšana
- [Atvērtā pirmkoda un licences lēmums / Atvērtā koda](OPEN_SOURCE.md)
- [Atbrīvošanas vadotne /](RELEASING.md) atlaišana
- [Ieguldījums / Ieguldījums](CONTRIBUTING.md)
- [Drošības politika / Drošība](SECURITY.md)
- [Rīcības kodekss / Rīcības kodekss](CODE_OF_CONDUCT.md)
- [Atbalsts un ziedojumi / Atbalsts un ziedojumi](SUPPORT.md)
- [Trešās puses paziņojumi](THIRD_PARTY_NOTICES.md)
- [Izmaiņu žurnāls / izmaiņu žurnāls](CHANGELOG.md)

## Būvēšana un savienošana

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

**Reāls JPMS modulis.** Jar darbojas kā nosaukts modulis (`jar --describe-module`):

```
vies.client@1.2.0
exports vies.client
requires java.net.http
contains vies.client.internal   ← nem exportált, zárt belső csomag
```

Izveidojiet savienojumu no modulāras lietojumprogrammas, piemēram:

```java
module my.api.server {
    requires vies.client;
}
```

No Classpath (nemodularizētā, "tradicionālā" projektā) tas darbojas tāpat —`module-info` šajā gadījumā vienkārši nespēlē. Pat bez Maven/Gradle
var izmantot: avota koku zem `src/main/java` var kopēt savā projektā
(atstājiet `module-info.java`, ja jūsu projekts nav modulārs).

## Ātrs piemērs

```java
import vies.client.*;

try (var vies = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER"))) // uzticams noslēpums / konfigurācija
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

`switch` ir izsmeļošs: kompilators garantē, ka esat apstrādājis visas četras izejas
(aizzīmogots interfeiss + raksta saskaņošana).

## Kāpēc `defaultRequester` ir svarīgs?

`defaultRequester` jābūt jūsu organizācijas PVN numuram, kas saņemts no uzticama
noslēpuma vai konfigurācijas. Derīgai atbildei VIES **var** atgriezt
`consultationNumber`, taču tas ir neobligāts un nekad nav garantēts. Tā juridiskais
pierādījuma spēks ir atkarīgs no vietējiem noteikumiem; glabājiet to kopā ar
pieprasījuma datumu un ievadi.

## Savienojums ar Spring Boot API serveri

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

Klients ir nemainīgs un drošs pavedieniem — lietojumprogramma izveido vienu gadījumu
(vienas pupiņas) un aizveriet to izslēgšanas brīdī (`close`).
Bezrāmju raksts: [`examples/ViesDemoServer.java`](../../../examples/ViesDemoServer.java) (vienkāršs JDK`HttpServer`, virtuālajos pavedienos):

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java     # port 8085
curl "http://localhost:8085/vat-check?number=HU00000000"
```

## Asinhrona lietošana

```java
CompletableFuture<ViesResponse> future = vies.checkAsync("PL0000000000");
```

Pēc noklusējuma asinhronie zvani darbojas **virtuālajos pavedienos** (Project Loom) — daudz
paralēlajā pārbaudē arī nav platformas vītnes atkritumu. Pašu izpildītājs a
var ievadīt celtniekā (`executor(...)`— to nav aizvēris klients).
Līdz 512 **individuālām asinhronā līdera darbībām** no asinhronās klientu bāzes
saņemts par katru klienta gadījumu. Viena kešatmiņas lasīšana aizņem vietu uz īsu laiku;
bojātā ievade un tas pats viena lidojuma sekotājs nepatērē atsevišķu vietu. Pārslodzes gadījumā jaunais individuālais pieprasījums nekavējoties
Jūs saņemat rezultātu `Unavailable(..., "CLIENT_OVERLOADED")`. Tas ir klienta iekšienē
ierobežo darbu; arī zvanītāja saglabātā ievades rinda un nākotnes līgumi
jābūt ierobežotam.

## Vairāku miljonu vaicājumu rūpnīca

Miljoniem lietotāju nesāciet miljoniem nākotnes līgumu vienā JVM un nedariet to
atļaut neierobežotu tiešu trafiku uz VIES. Piedāvātā struktūra:

> Klients var būt apstrādes sastāvdaļa miljonu darba rindā, bet faktiskais
> VIES pārraidi ierobežo mainīgie, negarantētie ES un dalībvalstu sistēmu ierobežojumi
> būt noteiktam. Virtuālie pavedieni samazina gaidīšanas izmaksas; augštecē
> jauda netiek palielināta.
> 1. Ienākošās pārbaudes saņem pastāvīga, sadalīta ziņojumu rinda. 2. Vairāki horizontāli mērogoti strādnieki patērē līniju ierobežotās porcijās. 3. Viens viens `ViesClient` darbojas ar virtuāliem pavedieniem uz vienu darbinieku. 4. Darbinieki izmanto kopējo Redis kešatmiņu, izmantojot saskarni`ViesCache`. 5. Globālais/izplatītā ātruma ierobežotājs aizsargā ES un dalībvalstu VIES galapunktus.
> JVM ietvaros klients apvienojas, ierodoties tajā pašā laikā
> nodokļu numura/vaicājuma pāri (viens lidojums), tāpēc kešatmiņas nokavēšana to neizraisa
> "pieprasīt štancējumu".`maxConcurrentRequests` ierobežo reālais tīkls
> pieprasījumus, kā arī `maxPendingAsyncRequests` un`maxPendingSyncRequests` atmiņā
> rada tūlītēju pretspiedienu. Visi ir JVM vietējie. Vairāku strādnieku kopējā satiksme
> jāregulē ar kopēju, sadalītu ierobežotāju. Pastāvīgā ievades rinda un
> lietojumprogrammai joprojām būtu jāierobežo patērētāju kopums.
> Smagas slodzes darbinieka piemērs:

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

Rezultāts `Unavailable`— ieskaitot vērtību`CLIENT_OVERLOADED`— ir noturīgs
jums ir jāmēģina vēlreiz ar kavēšanos rindā.`MalformedInput` un`Invalid` nevajadzētu mēģināt atkārtoti.

## Pieprasījuma ceļš / pieprasījuma dzīves cikls

1. **Normalizēt / Normalizēt:** pārbaudiet valsts kodu un formātu bez tīkla.
   2. **Kešatmiņa:** tūlītēja derīgu, joprojām aktuālu rezultātu atgriešana.
   3. ** viens lidojums:** vienā JVM tiek apvienoti identiski nodokļu koda un vaicājuma pāri.
2. **Ieeja:** asinhronizācijas un tīkla ierobežojumi aizsargā atmiņu un VIES.
3. **HTTP:** atkārtoti izmantots JDK `HttpClient` savienojums ar īsu taimautu.
4. **Pārbaudīt/atbilžu pārbaude:** nepilnīgs Būla vai datums →`MALFORMED_RESPONSE`.
5. **Kešatmiņas rakstīšana:** kešatmiņā tiek saglabāts tikai autentiskais `Valid` rezultāts.

## Bilingvālu kļūdu atbildes / Bilingvālu kļūdu atbildes

Iekārtas kļūdas kods vienmēr ir stabils un nav atkarīgs no valodas.`error()` ir ungāru un angļu valoda
sniedz lietotājam tekstu un lēmumu par atkārtotu mēģinājumu:

```java
var response = vies.check("HU00000000");
response.error().ifPresent(error -> {
    log.warn("{} | {} | retry={}", error.messageHu(), error.messageEn(), error.retryable());
});
```

| Rezultāts        |        HTTP |  Mēģināt vēlreiz | Kešatmiņa | Nozīme                                                        |
| ---------------- | ----------: | ---------------: | --------: | ------------------------------------------------------------- |
| `Valid`          |         200 |               nē |     jā/jā | VIES apstiprināts kā derīgs / VIES apstiprināts derīgs        |
| `Invalid`        |         200 |               nē |        nē | VIES neapstiprināja to kā derīgu / VIES neapstiprināja derīgu |
| `Unavailable`    | 503 vai 429 | pārsvarā/parasti |        nē | Lēmums par derīgumu netika pieņemts                           |
| `MalformedInput` |         400 |               nē |        nē | Ievade ir jālabo / Ievade jālabo                              |

## Konfigurācija (veidnieks)

| Iestatījums                       | Bāzes vērtība               | Ko dara                                                                                                |
| --------------------------------- | --------------------------- | ------------------------------------------------------------------------------------------------------ |
| `baseUrl(String)`                 | oficiālais VIES REST URL    | Pārvirzīšana izspēles testā                                                                            |
| `connectTimeout(Duration)`        | 5 s                         | TCP/TLS savienojuma taimauts                                                                           |
| `requestTimeout(Duration)`        | 8 s                         | Kopējais pieprasījuma noildze (VIES ir interaktīvs, saglabājiet to īsi)                                |
| `admissionTimeout(Duration)`      | 2 s                         | Tik ilgi tā gaida brīvu vietu tīklā                                                                    |
| `defaultRequester(ViesRequester)` | nav                         | Pašu kopienas nodokļu maksātāja numurs → konsultācijas ID                                              |
| `retries(int)`                    | 0                           | Automātisks atkārtots mēģinājums pārejošu kļūdu gadījumā (0–5, eksponenciāla atkāpšanās + nervozitāte) |
| `retryDelay(Duration)`            | 400 ms                      | Eksponenciālā atkāpšanās noklusējums                                                                   |
| `maxConcurrentRequests(int)`      | 32                          | Vienlaicīgu reālo VIES tīkla pieprasījumu augšējā robeža                                               |
| `maxPendingSyncRequests(int)`     | 512                         | Vienlaicīgas sinhronizācijas zvanītāju atmiņas ierobežojums; virs tā `CLIENT_OVERLOADED`               |
| `maxPendingAsyncRequests(int)`    | 512                         | Atmiņas ierobežojums aktīvajām/apstiprinātajām asinhronizācijas darbībām; virs tā `CLIENT_OVERLOADED`  |
| `cacheTtl(Duration)`              | 24 stundas                  | Kešatmiņas laiks derīgiem trāpījumiem                                                                  |
| `cacheMaxEntries(int)`            | 10 000                      | Iebūvētās atmiņas kešatmiņas izmēra ierobežojums                                                       |
| `cache(ViesCache)`                | iebūvēts                    | Savs kešatmiņas aizmugursistēma (piem., Redis)                                                         |
| `disableCache()`                  | —                           | Nav saglabātas kešatmiņas; vienādi paralēli zvani var koplietot vienu single-flight pieprasījumu        |
| `userAgent(String)`               | moduļa ID                   | Jums vajadzētu sevi identificēt ES                                                                     |
| `executor(ExecutorService)`       | virtuāls pavediens/uzdevums | Mans asinhronais izpildītājs; zvanītājs ir atbildīgs par tā dzīves ciklu                               |

## Paša kešatmiņa (piem., Redis)

```java
class RedisViesCache implements ViesCache {
    public Optional<ViesResponse.Valid> get(String key) { /* ... */ }
    public void put(String key, ViesResponse.Valid value, Duration ttl) { /* ... */ }
}

var vies = ViesClient.builder().cache(new RedisViesCache()).build();
```

Kešatmiņā tiek saglabāts tikai rezultāts `Valid`— nederīgs numurs un pārejoša kļūda nekad.
Redis adapterim izmantojiet īsu kešatmiņas taimautu, versijas nosaukumvietu un savu
kļūdu metrika. Kešatmiņas lasīšanas kļūda `CACHE_ERROR` rodas, kad Redis nedarbojas
nesāciet nekontrolētu VIES pieprasījuma satricinājumu.
No kešatmiņas atgrieztie `consultationNumber` un `requestDate` pieder **sākotnējai**
pārbaudei; kešatmiņas trāpījums nav jauna VIES pārbaude. `disableCache()` izslēdz tikai
saglabāto kešatmiņu: vienādi paralēli VAT+requester zvani var koplietot vienu
single-flight tīkla pieprasījumu, bet vēlāks zvans pēc tā pabeigšanas veic jaunu
pieprasījumu. `consultationNumber` arī tad paliek neobligāts.

## Semantika, ko vērts zināt

1. **`Unavailable`≠`Invalid`.** Dalībvalstu fona sistēmas regulāri
   ir nomesti (`MS_UNAVAILABLE`,`MS_MAX_CONCURRENT_REQ`...). Šajā gadījumā skaitlis a
   **nav vērtēts VIES** — komerciāli aizliegts uzskatīt par nederīgu.
2. **Formātu iepriekšēja filtrēšana nav VIES autentifikācija.** Modulis filtrē reģistrētos
   nepareiza ievade (`MalformedInput`) pirms došanās uz tīklu, bet derīgums
   tās avots vienmēr ir VIES atbilde.
   3. **Atkārtots mēģinājums arī palielina slodzi.** Izmēģiniet tikai dažas reizes lokāli pagaidu kļūdu
   atkal; lielās operācijās primārais ir ilgstošas ​​rindas aizkavētā atkārtotā mēģinājuma mehānisms.
3. **Grieķija `EL`, Ziemeļīrija`XI`.** Modulis apstrādā abus,`GR`automātiski kartē ievadi uz`EL`.

## Pārbaudes

```bash
./mvnw test    # unit + helyi HTTP konkurencia/retry/backpressure/lifecycle tesztek
```

---

## Īsais īss uzsākums angļu valodā

Nulles atkarības Java 21+ klients ES Komisijas VIES PVN numuram
validācijas REST API. Veidojiet ar `./mvnw package`, pēc tam:

```java
try (var vies = ViesClient.createDefault()) {
    if (vies.check("DE000000000") instanceof ViesResponse.Valid v) {
        System.out.println(v.traderName().orElse("?"));
    }
}
```

Rezultāti ir noslēgta hierarhija (`Valid`/`Invalid`/`Unavailable`/`MalformedInput`), kas paredzēts pilnīgai`switch` modeļa saskaņošanai. Derīgs
rezultāti tiek saglabāti atmiņā 24 stundas; pārejoši VIES pārtraukumi tiek ziņots kā `Unavailable`, nekad kā`Invalid`. Oficiālā API dokumentācija:
<https://ec.europa.eu/taxation_customs/vies/#/technical-information>.

## Liela mēroga darbība

Šis klients var būt viena no miljonu preču apstrādes konveijera sastāvdaļām, taču
faktisko VIES caurlaidspēju ierobežo mainīga, negarantēta ES un dalībvalsts
robežas. Virtuālie pavedieni samazina bloķēšanas izmaksas; tie nepalielinās augštecē
jaudu. Izmantojiet noturīgu sadalītu rindu, ierobežotus patērētājus, kopīgu Redis
kešatmiņu un sadalītu ātruma ierobežotāju visiem darbiniekiem. Vietējie viena lidojuma un
semafori aizsargā tikai vienu JVM. Nekad neapstrādājiet `Unavailable` kā`Invalid`; izmantot`response.error()` stabiliem kodiem, atkārtotai izmēģināšanai un ungāru/angļu ziņojumiem.

## Atvērtais pirmkods / Atvērtais avots

Projektu var izmantot, modificēt un
var izplatīt. Licence atļauj komerciālu izmantošanu un ekspress
piešķir patenta licenci; ir jāizlasa licences, attiecinājuma un modifikācijas noteikumi
sargāt. Pirms pievienošanas izlasiet [CONTRIBUTING.md](CONTRIBUTING.md) un
[CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) faili.
Šis projekts ir licencēts saskaņā ar [Apache License 2.0](../../../LICENSE), atļauja
licence ar nepārprotamu patenta piešķiršanu. Skatiet [CONTRIBUTING.md](CONTRIBUTING.md),
[SECURITY.md](SECURITY.md), un detalizētās [atvērtā koda piezīmes](OPEN_SOURCE.md).

## Atbalsts un ziedojumi

Kopienas atbalsts tiek nodrošināts pēc iespējas labāk, izmantojot GitHub problēmas un diskusijas.
Vienmēr ziņojiet par drošības kļūdām privātā kanālā. Projekta uzturēšana ir nozīmīga
ietver attīstības un infrastruktūras izmaksas; GitHub poga Ziedot/sponsorēt ir uzturētājs
parādās pēc verificētā atbalsta URL iestatīšanas.
Kopienas atbalsts ir vislabākais, izmantojot GitHub problēmas un diskusijas. Drošība
pārskatiem jāpaliek privātiem. Pēc tam tiks iespējota poga Ziedot/sponsorēt
uzturētāja verificēts finansējuma URL ir konfigurēts.
