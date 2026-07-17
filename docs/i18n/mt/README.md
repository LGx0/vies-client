# Malti (mt) — vies-client — kontrollur tan-numru tal-VAT

**Termini tat-tiftix:** verifika tan-numru tal-VAT, validazzjoni tan-numru tal-VAT, validazzjoni tal-VAT tal-UE, verifika tal-identifikatur tat-taxxa, klijent Java VIES; `VAT checker`, `VAT number validator`, `EU VAT validation`, `tax ID checker`.

Dan mhuwiex kalkulatur ġenerali tat-taxxa, iżda klijent għall-validazzjoni tan-numri ta’ identifikazzjoni tal-VAT tal-UE permezz ta’ VIES.

> [Għażla tal-lingwa](../../LANGUAGES.md) · Din il-lokalizzazzjoni hija għall-aċċessibbiltà. F’każ ta’ differenza, is-sors kanoniku tekniku jew legali bl-Ingliż jipprevali. `LICENSE` u `NOTICE` fir-root jibqgħu legalment awtorevoli.

[![Liċenzja: Apache-2.0](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](../../../LICENSE)[![Java 21+](https://img.shields.io/badge/Java-21%2B-orange.svg)](../../../pom.xml)
🌍 **[Nyelv / Lingwa / Il-lingwi uffiċjali kollha tal-UE](../../LANGUAGES.md)**
Klijent Java waħdu, **zero runtime dependency** għall-Kummissjoni Ewropea VIES
(Sistema ta' Skambju ta' Informazzjoni tal-VAT) għall-API REST ta' kontrollur tan-numru tat-taxxa. Kwalunkwe Java
jista’ jiġi konness ma’ server jew programm API fil-lingwi li ġejjin: Spring Boot, Quarkus, Micronaut,
jew saħansitra sempliċi JDK `HttpServer`— il-modulu juża biss il-JDK (`java.net.http`),
m'hemm l-ebda siġra ta' dipendenza tranżittiva.
Proġett ta' sors miftuħ indipendenti; mhux il-Kummissjoni Ewropea, l-UE jew l-istati membri
huwa prodott uffiċjali tal-awtoritajiet tat-taxxa u mhuwiex approvat jew iċċertifikat minnhom.

- **Rekwiżit:** Java **21+** bytecode/API; il-pakkett kollu huwa vverifikat fuq JDK 21.
- **Dokumentazzjoni VIES Uffiċjali:** <https://ec.europa.eu/taxation_customs/vies/#/technical-information>
- **Endpoint imsejjaħ:**`https://ec.europa.eu/taxation_customs/vies/rest-api/ms/{countryCode}/vat/{vatNumber}`
- **Kuntatt tal-Istat Membru:**`https://ec.europa.eu/taxation_customs/vies/rest-api/check-status`

## Dokumentazzjoni / Dokumentazzjoni

- [Installazzjoni / Installazzjoni](INSTALLATION.md)
- [Konnessjoni / Integrazzjoni](INTEGRATION.md)
- [Struttura teknika / Disinn tekniku](TECHNICAL.md)
- [Testijiet tal-unità, tal-integrazzjoni u tal-kompetizzjoni / Ittestjar](TESTING.md)
- [Kodiċi tas-sors miftuħ u deċiżjoni tal-liċenzja / Sors miftuħ](OPEN_SOURCE.md)
- [Gwida tar-rilaxx / Ir-rilaxx ta'](RELEASING.md)
- [Pubblikazzjoni GitHub / pubblikazzjoni GitHub](GITHUB_SETUP.md)
- [Kontribut / Kontribut](CONTRIBUTING.md)
- [Politika tas-sigurtà / Sigurtà](SECURITY.md)
- [Kodiċi ta' Kondotta / Kodiċi ta' Kondotta](CODE_OF_CONDUCT.md)
- [Appoġġ u donazzjonijiet / Appoġġ u donazzjonijiet](SUPPORT.md)
- [Avviżi ta' partijiet terzi](THIRD_PARTY_NOTICES.md)
- [Changelog](CHANGELOG.md)

## Ibni u konnessjoni

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

**Modulu JPMS reali.** Il-vażett iġib ruħu bħala modulu msemmi (`jar --describe-module`):

```
vies.client@1.0.0
exports vies.client
requires java.net.http
contains vies.client.internal   ← nem exportált, zárt belső csomag
```

Qabbad minn applikazzjoni modularizzata bħal din:

```java
module my.api.server {
    requires vies.client;
}
```

Minn Classpath (fi proġett "tradizzjonali" mhux modularizzat) jaħdem bl-istess mod

- `module-info` sempliċement ma jilgħabx f'dan il-każ. Anke mingħajr Maven/Gradle
  jista 'jintuża: is-siġra tas-sors taħt `src/main/java` tista' tiġi kkupjata fil-proġett tiegħek stess
  (ħalli lil `module-info.java` jekk il-proġett tiegħek mhuwiex modularizzat).

## Eżempju ta' malajr

```java
import vies.client.*;

try (var vies = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER"))) // sigriet / konfigurazzjoni fdati
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

`switch` huwa eżawrjenti: il-kompilatur jiggarantixxi li inti ttrattat l-erba' outputs
(interface ssiġillata + tqabbil tal-mudell).

## Għaliex `defaultRequester` huwa importanti?

`defaultRequester` għandu jkun in-numru tal-VAT tal-organizzazzjoni tiegħek u għandu
jiġi minn sigriet jew konfigurazzjoni fdati. Għal tweġiba valida VIES **jista'**
jirritorna `consultationNumber`, iżda dan huwa fakultattiv u qatt mhu garantit.
Il-valur tiegħu bħala prova legali jiddependi mir-regoli lokali; żommu flimkien
mad-data u l-input tat-talba.

## Konnessjoni mas-server tal-API Spring Boot

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

Il-klijent huwa immutabbli u bla periklu — l-applikazzjoni toħloq istanza waħda
(singleton bean) u agħlaqha mal-għeluq (`close`).
Mudell mingħajr qafas: [`examples/ViesDemoServer.java`](../../../examples/ViesDemoServer.java) (JDK sempliċi`HttpServer`, fuq ħjut virtwali):

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java     # port 8085
curl "http://localhost:8085/vat-check?number=HU00000000"
```

## Użu mhux sinkroniku

```java
CompletableFuture<ViesResponse> future = vies.checkAsync("PL0000000000");
```

B'mod awtomatiku, sejħiet asinkroniċi jimxu fuq **ħjut virtwali** (Project Loom) — ħafna
m'hemm l-ebda skart tal-ħajt tal-pjattaforma fl-iċċekkjar parallel lanqas. Eżekutur proprju a
jista 'jiddaħħal fil-bennej (`executor(...)`— mhuwiex magħluq mill-klijent).
Sa 512 **azzjonijiet individwali tal-mexxej asinkroniċi** mill-bażi tal-klijenti asinkroniċi
riċevuti għal kull istanza tal-klijent. Qari wieħed tal-cache jikkonsma spazju għal żmien qasir;
l-input difettuż u l-istess segwaċi ta 'titjira waħda ma jikkunsmawx spazju separat. F'każ ta 'tagħbija żejda, it-talba individwali ġdida immedjatament
Ikollok riżultat ta '`Unavailable(..., "CLIENT_OVERLOADED")`. Dan huwa fil-klijent
jirrestrinġi x-xogħol; il-kju tad-dħul u l-futures maħżuna minn min iċempel ukoll
għandhom ikunu limitati.

## Impjant ta' mistoqsija b'ħafna miljuni

Għal miljuni ta 'utenti, tibdiex miljuni ta' futuri f'JVM waħda, u ma tagħmilx
jippermettu traffiku dirett illimitat lejn VIES. L-istruttura proposta:

> Il-klijent jista 'jkun komponent tal-ipproċessar ta' kju ta 'xogħol miljun, iżda l-attwali
> It-trażmissjoni tal-VIES hija limitata mil-limiti li qed jinbidlu u mhux garantiti tas-sistemi tal-UE u tal-istati membri
> tkun determinata. Ħjut virtwali jnaqqsu l-ispiża ta 'stennija; l-upstream
> il-kapaċità ma tiżdiedx.

1. Iċċekkijiet li jkunu deħlin jiġu riċevuti minn kju tal-messaġġi persistenti u maqsum.
2. Diversi ħaddiema skalati orizzontalment jikkunsmaw il-linja f'porzjonijiet limitati.
3. Singleton `ViesClient` wieħed jaħdem b'ħjut virtwali għal kull ħaddiem.
4. Il-ħaddiema jużaw cache Redis komuni permezz tal-interface `ViesCache`.
5. Il-limitatur tar-rata globali/imqassam jipproteġi l-endpoints tal-VIES tal-UE u tal-istati membri.
   Fi ħdan JVM, il-klijent jingħaqad l-istess, u jasal fl-istess ħin
   numru tat-taxxa/pari ta' mistoqsijiet (titjira waħda), għalhekk cache miss ma tikkawżahiex
   "talba stampede".`maxConcurrentRequests` huwa limitat min-netwerk reali
   talbiet, u `maxPendingAsyncRequests` u`maxPendingSyncRequests` fil-memorja
   jagħti backpressure immedjata. Kollha huma JVM-lokali. Traffiku aggregat ta’ diversi ħaddiema
   għandhom ikunu regolati b'limitatur komuni u distribwit. Il-kju tad-dħul persistenti u l-
   pool tal-konsumaturi xorta għandu jkun limitat mill-applikazzjoni.
   Eżempju ta' ħaddiem ta' tagħbija tqila:

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

Ir-riżultat `Unavailable`— inkluż il-valur`CLIENT_OVERLOADED`— huwa persistenti
trid terġa' tipprova b'dewmien fil-kju.`MalformedInput` u`Invalid` m'għandhomx jerġgħu jiġu ppruvati.

## Mogħdija ta' talba / Ċiklu tal-ħajja tat-talba

1. **Normalizza / Normalizza:** iċċekkja l-kodiċi tal-pajjiż u l-format mingħajr netwerk.
2. **Cache:** Ritorn immedjat ta' riżultati validi, li għadhom ħajjin.
3. **Titjira waħda:** kodiċi tat-taxxa identiku+pari ta' mistoqsijiet huma magħquda fi ħdan JVM wieħed.
4. **Dħul:** async u limiti tan-netwerk jipproteġu l-memorja u VIES.
5. **HTTP:** konnessjoni JDK `HttpClient` użata mill-ġdid b'timeout qasir.
6. **Ivvalida / Irrispondi kontroll:** boolean mhux komplut jew data →`MALFORMED_RESPONSE`.
7. **Cache write:** ir-riżultat `Valid` awtentiku biss huwa cached.

## Tweġibiet ta’ żball bilingwi / Risposti ta’ żball bilingwi

Il-kodiċi tal-iżball tal-magna huwa dejjem stabbli u indipendenti mill-lingwa.`error()` huwa Ungeriż u Ingliż
jagħti test lill-utent u deċiżjoni mill-ġdid:

```java
var response = vies.check("HU00000000");
response.error().ifPresent(error -> {
    log.warn("{} | {} | retry={}", error.messageHu(), error.messageEn(), error.retryable());
});
```

| Riżultat         |        HTTP |  Ipprova mill-ġdid |   Cache | Tifsira                                                          |
| ---------------- | ----------: | -----------------: | ------: | ---------------------------------------------------------------- |
| `Valid`          |         200 |                 le | iva/iva | VIES ikkonfermat bħala validu / VIES ikkonfermat validu          |
| `Invalid`        |         200 |                 le |      le | Il-VIES ma kkonfermahiex bħala valida/VIES ma kkonfermatx valida |
| `Unavailable`    | 503 jew 429 | l-aktar/normalment |      le | Ma saret l-ebda deċiżjoni dwar il-validità                       |
| `MalformedInput` |         400 |                 le |      le | L-input għandu jiġi kkoreġut / L-input għandu jiġi kkoreġut      |

## Konfigurazzjoni (bennej)

| L-issettjar                       | Valur bażi                  | X'jagħmel                                                                                     |
| --------------------------------- | --------------------------- | --------------------------------------------------------------------------------------------- |
| `baseUrl(String)`                 | URL uffiċjali tal-VIES REST | Direzzjoni mill-ġdid f'test mock                                                              |
| `connectTimeout(Duration)`        | 5 s                         | Timeout tal-konnessjoni TCP/TLS                                                               |
| `requestTimeout(Duration)`        | 8 s                         | Timeout totali tat-talba (VIES huwa interattiv, żommu qasir)                                  |
| `admissionTimeout(Duration)`      | 2 s                         | Dan huwa kemm idum jistenna għal spazju tan-netwerk b'xejn                                    |
| `defaultRequester(ViesRequester)` | m'hemmx                     | Numru tat-taxxa tal-komunità proprja → ID tal-konsultazzjoni                                  |
| `retries(int)`                    | 0                           | Ipprova mill-ġdid awtomatiku fuq żbalji temporanji (0-5, backoff esponenzjali + jitter)       |
| `retryDelay(Duration)`            | 400 ms                      | Inadempjenza ta' backoff esponenzjali                                                         |
| `maxConcurrentRequests(int)`      | 32                          | Limitu ta' fuq ta' talbiet konkorrenti tan-netwerk VIES reali                                 |
| `maxPendingSyncRequests(int)`     | 512                         | Limitu tal-memorja għal min iċempel is-sinkronizzazzjoni simultanja; fuqu `CLIENT_OVERLOADED` |
| `maxPendingAsyncRequests(int)`    | 512                         | Limitu tal-memorja għal operazzjonijiet asinkroniċi attivi/pendenti; fuqu `CLIENT_OVERLOADED` |
| `cacheTtl(Duration)`              | 24 siegħa                   | Ħin tal-cache għal hits validi                                                                |
| `cacheMaxEntries(int)`            | 10,000                      | Limitu tad-daqs tal-cache tal-memorja integrata                                               |
| `cache(ViesCache)`                | mibnija                     | Cache-backend proprju (eż. Redis)                                                             |
| `disableCache()`                  | —                           | Ebda cache maħżuna; sejħiet paralleli identiċi jistgħu jaqsmu talba single-flight waħda       |
| `userAgent(String)`               | modulu-id                   | Għandek tidentifika ruħek mal-UE                                                              |
| `executor(ExecutorService)`       | ħajta/kompitu virtwali      | L-eżekutur asinkroniku tiegħi; min iċempel huwa responsabbli għaċ-ċiklu tal-ħajja tiegħu      |

## Cache proprju (eż. Redis)

```java
class RedisViesCache implements ViesCache {
    public Optional<ViesResponse.Valid> get(String key) { /* ... */ }
    public void put(String key, ViesResponse.Valid value, Duration ttl) { /* ... */ }
}

var vies = ViesClient.builder().cache(new RedisViesCache()).build();
```

Ir-riżultat biss `Valid` huwa fil-cache — numru invalidu u żball temporanju qatt.
Għall-adapter Redis, uża short cache-timeout, namespace versioned u stess
metrika ta' żball. Cache qari żball `CACHE_ERROR` jirriżulta li meta Redis huwa down
tibdiex talba VIES bla kontroll.
`consultationNumber` u `requestDate` ritornati mill-cache jappartjenu għall-kontroll
**oriġinali**; cache hit mhuwiex kontroll VIES ġdid. `disableCache()` jitfi biss
il-cache maħżuna: sejħiet paralleli identiċi VAT+requester jistgħu jaqsmu talba
single-flight waħda, filwaqt li sejħa aktar tard wara t-tlestija tibgħat talba ġdida.
`consultationNumber` jibqa' fakultattiv.

## Semantika ta’ min ikun jaf

1. **`Unavailable`≠`Invalid`.** Sistemi tal-isfond tal-Istati Membri regolarment
   jintefgħu (`MS_UNAVAILABLE`,`MS_MAX_CONCURRENT_REQ`...). F'dan il-każ, in-numru a
   **mhux iġġudikat minn VIES** — kummerċjalment ipprojbit li jitqies bħala invalidu.
2. **Pre-filtrazzjoni tal-format mhijiex awtentikazzjoni VIES.** Il-modulu jiffiltra l-irreġistrati
   input ħażin (`MalformedInput`) qabel tmur għan-netwerk, iżda l-validità
   is-sors tiegħu huwa dejjem ir-rispons tal-VIES.
3. **Il-prova mill-ġdid iżid ukoll it-tagħbija.** Ipprova biss żball temporanju lokalment għal ftit drabi
   għal darb'oħra; f'operazzjoni kbira, il-mekkaniżmu mdewwem mill-ġdid tal-kju durabbli huwa dak primarju.
4. ** Il-Greċja `EL`, l-Irlanda ta' Fuq`XI`.** Il-modulu jimmaniġġja t-tnejn,`GR`awtomatikament mapep input għal`EL`.

## Testijiet

```bash
./mvnw test    # unit + helyi HTTP konkurencia/retry/backpressure/lifecycle tesztek
```

---

## Quickstart bl-Ingliż

Klijent Java 21+ ta' dipendenza żero għan-numru tal-VAT VIES tal-Kummissjoni tal-UE
validazzjoni REST API. Ibni b'`./mvnw package`, imbagħad:

```java
try (var vies = ViesClient.createDefault()) {
    if (vies.check("DE000000000") instanceof ViesResponse.Valid v) {
        System.out.println(v.traderName().orElse("?"));
    }
}
```

Ir-riżultati huma ġerarkija ssiġillata (`Valid`/`Invalid`/`Unavailable`/`MalformedInput`) iddisinjat għal`switch` ta 'tqabbil tal-mudelli eżawrjenti. Valida
ir-riżultati jinżammu fil-cache fil-memorja għal 24 siegħa; qtugħ temporanju tal-VIES huma rrappurtati bħala `Unavailable`, qatt bħala`Invalid`. Dokumentazzjoni uffiċjali tal-API:
<https://ec.europa.eu/taxation_customs/vies/#/technical-information>.

## Operazzjoni fuq skala għolja

Dan il-klijent jista 'jkun komponent wieħed ta' pipeline ta 'proċessar ta' miljun oġġett, iżda
il-fluss attwali tal-VIES huwa limitat minn varjabbli, mhux garantit tal-UE u tal-istat membru
limiti. Ħjut virtwali jnaqqsu l-ispiża tal-imblukkar; ma jiżdiedux upstream
kapaċità. Uża kju diviżorju durabbli, konsumaturi konfinati, Redis kondiviż
cache, u limitatur tar-rata distribwit mal-ħaddiema kollha. Titjira waħda lokali u
is-semafori jipproteġu JVM wieħed biss. Qatt tittratta `Unavailable` bħala`Invalid`; użu`response.error()` għal kodiċijiet stabbli, riprovabilità, u messaġġi Ungeriżi/Ingliżi.

## Kodiċi tas-sors miftuħ / Sors miftuħ

Il-proġett jista 'jintuża, modifikat u
jistgħu jitqassmu. Il-liċenzja tippermetti użu kummerċjali u espress
jagħti liċenzja tal-privattiva; għandhom jinqraw it-termini tal-liċenzja, l-attribuzzjoni u l-modifika
għall-għassa. Qabel ma tikkontribwixxi, aqra [CONTRIBUTING.md](CONTRIBUTING.md) u
[CODE_OF_CONDUCT.md Fajls](CODE_OF_CONDUCT.md).
Dan il-proġett huwa liċenzjat taħt il-[Apache License 2.0](../../../LICENSE), permissiv
liċenzja b'għotja espliċita ta' privattiva. Ara [CONTRIBUTING.md](CONTRIBUTING.md),
[SIGURTÀ.md](SECURITY.md), u n-noti dettaljati [open-source](OPEN_SOURCE.md).

## Appoġġ u donazzjonijiet

L-appoġġ tal-komunità huwa pprovdut fuq bażi ta 'l-aħjar sforz, permezz ta' kwistjonijiet u diskussjonijiet GitHub.
Dejjem irrapporta l-iżbalji tas-sigurtà fuq kanal privat. Il-manutenzjoni tal-proġett hija sinifikanti
jinvolvi spejjeż ta' żvilupp u infrastruttura; il-buttuna GitHub Donate/Sponsor hija l-mantenitur
jidher wara li ssettja l-URL tal-appoġġ ivverifikat tiegħek.
L-appoġġ tal-komunità huwa l-aħjar sforz permezz ta’ kwistjonijiet u diskussjonijiet ta’ GitHub. Sigurtà
ir-rapporti għandhom jibqgħu privati. Buttuna Donate/Sponsor se tkun attivata wara l-
URL tal-finanzjament ivverifikat tal-mantenitur huwa kkonfigurat.
