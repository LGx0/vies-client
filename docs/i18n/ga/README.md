# Gaeilge (ga) — vies-client — seiceálaí uimhir CBL

Díchumasaíonn `disableCache()` an taisce stóráilte/mharthanach, ach féadfaidh glaonna comhuaineacha leis an bpéire céanna CBL + requester aon iarratas líonra single-flight amháin a roinnt. Déanann glao níos déanaí, tar éis a chríochnaithe, iarratas nua VIES. Tá `consultationNumber` roghnach agus féadfaidh VIES é a thabhairt ar ais, ach ní ráthaítear riamh é; braitheann a luach fianaiseach dlíthiúil ar rialacha áitiúla. Ná lódáil `MY_EU_VAT_NUMBER` ach ó fhoinse rúin/cumraíochta iontaofa.

**Téarmaí cuardaigh:** seiceáil uimhir CBL, bailíochtú uimhir CBL, bailíochtú CBL AE, seiceáil aitheantais chánach, cliant Java VIES; `VAT checker`, `VAT number validator`, `EU VAT validation`, `tax ID checker`.

Ní áireamhán cánach ginearálta é seo, ach cliant chun uimhreacha aitheantais CBL an AE a bhailíochtú trí VIES.

> [Roghnóir teanga](../../LANGUAGES.md) · Cuirtear an logánú seo ar fáil ar mhaithe le hinrochtaineacht. Má bhíonn difríocht ann, is í an fhoinse chanónach theicniúil nó dhlíthiúil Bhéarla atá i réim. Fanann `LICENSE` agus`NOTICE` na fréimhe ceangailteach.

[![Ceadúnas: Apache-2.0](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](../../../LICENSE)[![Java 21+](https://img.shields.io/badge/Java-21%2B-orange.svg)](../../../pom.xml)
🌍 **[Nyelv / Language / Teangacha oifigiúla uile an AE](../../LANGUAGES.md)**
Cliant Java neamhspleách, **neamhspleáchas ama rite** don Choimisiún Eorpach VIES
(Córas Malartaithe Faisnéise CBL) don API REST de sheiceálaí uimhreacha cánach. Java ar bith
is féidir é a nascadh le freastalaí API nó le clár sna teangacha seo a leanas: Spring Boot, Quarkus, Micronaut,
nó fiú JDK `HttpServer` simplí — ní úsáideann an modúl ach an JDK (`java.net.http`),
níl aon chrann spleáchais trasdultach ann.
Tionscadal foinse oscailte neamhspleách; ní an Coimisiún Eorpach, an AE nó na Ballstáit
is táirge oifigiúil de chuid na n-údarás cánach é agus níl sé formhuinithe ná deimhnithe acu.

- **Ceanglas:** Java **21+** bytecode/API; déantar an pacáiste iomlán a fhíorú ar JDK 21.
- **Doiciméadúchán oifigiúil VIES:** <https://ec.europa.eu/taxation_customs/vies/#/technical-information>
- **Críochphointe ar a dtugtar:**`https://ec.europa.eu/taxation_customs/vies/rest-api/ms/{countryCode}/vat/{vatNumber}`
- **Teagmhálaí Ballstáit:**`https://ec.europa.eu/taxation_customs/vies/rest-api/check-status`

## Doiciméadú / Documentation

- [Suiteáil / Suiteáil](INSTALLATION.md)
- [Ceangal / Comhtháthú](INTEGRATION.md)
- [Struchtúr teicniúil / Dearadh teicniúil](TECHNICAL.md)
- [Trialacha aonaid, comhtháthú agus iomaíochta / Tástáil](TESTING.md)
- [Cód foinse oscailte agus cinneadh ceadúnais / Foinse oscailte](OPEN_SOURCE.md)
- [Treoir scaoileadh / Scaoileadh](RELEASING.md)
- [Foilseachán GitHub / foilseachán GitHub](GITHUB_SETUP.md)
- [Ag Cur / Ag Cur](CONTRIBUTING.md)
- [Beartas slándála / Slándáil](SECURITY.md)
- [Cód Iompair / Code of Conduct](CODE_OF_CONDUCT.md)
- [Tacaíocht agus síntiúis / Support and donations](SUPPORT.md)
- [Fógraí tríú páirtí](THIRD_PARTY_NOTICES.md)
- [Changelog](CHANGELOG.md)

## Tógáil agus ceangail

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

**Modúl fíor JPMS.** Iompraíonn an próca mar mhodúl ainmnithe (`jar --describe-module`):

```
vies.client@1.2.0
exports vies.client
requires java.net.http
contains vies.client.internal   ← nem exportált, zárt belső csomag
```

Ceangail ó fheidhmchlár modúlaithe mar seo:

```java
module my.api.server {
    requires vies.client;
}
```

Ó Classpath (i dtionscadal neamh-mhodúlach, "traidisiúnta") oibríonn sé ar an mbealach céanna -
Ní imríonn `module-info` ach sa chás seo. Fiú gan Maven/Gradle
is féidir é a úsáid: is féidir an crann foinse faoi `src/main/java` a chóipeáil isteach i do thionscadal féin
(fág `module-info.java` mura bhfuil do thionscadal modúlaithe).

## Sampla tapa

```java
import vies.client.*;

try (var vies = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER"))) // rún/config iontaofa
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

Tá `switch` uileghabhálach: ráthaíonn an tiomsaitheoir go bhfuil gach ceann de na ceithre aschur láimhseála agat
(comhéadan séalaithe + meaitseáil patrún).

## Cén fáth a bhfuil `defaultRequester` tábhachtach?

Úsáid uimhir CBL na heagraíochta féin mar requester ó fhoinse rúin/cumraíochta iontaofa. Le toradh bailí féadfaidh VIES `consultationNumber` roghnach a thabhairt ar ais, ach ní ráthaítear riamh é. Braitheann a luach fianaiseach ar rialacha áitiúla; coinnigh an t-am, an uimhir CBL a seiceáladh agus an toradh de réir do bheartais iniúchta agus cosanta sonraí.

## Ceangal leis an bhfreastalaí Spring Boot API

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

Tá an cliant do-aistrithe agus sábháilte ó thaobh snáitheanna - cruthaíonn an feidhmchlár cás amháin
(bean singil) agus dún ar múchadh é (`close`).
Patrún Gan Fráma: [`examples/ViesDemoServer.java`](../../../examples/ViesDemoServer.java) (JDK`HttpServer` simplí , ar snáitheanna fíorúla):

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java     # port 8085
curl "http://localhost:8085/vat-check?number=HU00000000"
```

## Úsáid asincrónach

```java
CompletableFuture<ViesResponse> future = vies.checkAsync("PL0000000000");
```

De réir réamhshocraithe, ritheann glaonna async ar ** snáitheanna fíorúil ** (Project Loom) - go leor
níl aon dramhaíl ardán-snáithe i seiceáil comhthreomhar ach an oiread. Seiceadóir féin a
Is féidir a chur isteach i tógálaí (`executor(...)`- nach bhfuil sé dúnta ag an gcliant).
Suas go dtí 512 **gníomhaíochtaí ceannaire asynciliúcháin aonair** ón mbonn cliant async
faighte in aghaidh an chliaint. ídíonn léamh taisce amháin spás ar feadh tamaill ghearr;
ní ídíonn an t-ionchur lochtach agus an leantóir aon-eitilte céanna spás ar leith. I gcás ró-ualú, an t-iarratas aonair nua láithreach
Faigheann tú toradh ar `Unavailable(..., "CLIENT_OVERLOADED")`. Is é seo an cliant i-
srian ar obair; an scuaine ionchuir agus na todhchaíochtaí arna stóráil ag an nglaoiteoir freisin
Ní mór a bheith teoranta.

## Gléasra ceist na milliún

Do na milliúin úsáideoirí, ná cuir tús leis na milliúin todhchaí in aon JVM amháin, agus ná cuir tús leis
trácht díreach gan teorainn a cheadú chuig VIES. An struchtúr molta:

> Is féidir leis an gcliant a bheith ina chomhpháirt próiseála de mhilliún scuaine oibre, ach an t-iarbhír
> Tá tarchur VIES teoranta ag teorainneacha athraitheacha, neamhráthaithe an AE agus córais na mBallstát
> a chinneadh. Laghdaíonn snáitheanna fíorúla an costas feithimh; an tsrutha
> ní mhéadaítear acmhainn.

1. Faightear seiceálacha isteach trí scuaine teachtaireachtaí leanúnacha deighilte.
2. Itheann roinnt oibrithe ar scála cothrománach an líne i gcodanna teoranta.
3. Oibríonn singleton `ViesClient` amháin le snáitheanna fíorúla in aghaidh an oibrí.
4. Úsáideann na hoibrithe taisce Redis coitianta tríd an gcomhéadan `ViesCache`.
5. Cosnaíonn teorannóir rátaí domhanda/dáilte críochphointí VIES an AE agus na mballstát.
   Laistigh de JVM, cumasc an cliant mar an gcéanna, ag teacht ag an am céanna
   uimhir chánach/péirí ceiste (eitilt aonair), mar sin ní chailleann taisce is cúis leis
   "iarratas stampede".`maxConcurrentRequests` teoranta ag an líonra fíor
   iarratais, agus `maxPendingAsyncRequests` agus`maxPendingSyncRequests` i gcuimhne
   tugann backpressure toirt. Tá siad go léir JVM-áitiúil. Trácht comhiomlán roinnt oibrithe
   a rialú le teorannóir coiteann, dáilte. An scuaine ionchuir leanúnach agus an
   ba cheart comhthiomsú tomhaltóirí a theorannú fós ag an iarratas.
   Sampla oibrí ualach trom:

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

Tá an toradh `Unavailable`- lena n-áirítear an luach`CLIENT_OVERLOADED`- leanúnach
caithfidh tú triail a bhaint as arís agus moill sa scuaine. Níor cheart `MalformedInput` agus`Invalid` a aisghabháil.

## Conair iarratais/saolré iarratais

1. ** Normalaigh / Normalaigh:** seiceáil an cód tíre agus an fhormáid gan líonra.
2. **Taisce:** Torthaí bailí atá fós beo ar ais láithreach.
3. **Eitilt aonair:** cód cánach comhionann+ déantar péirí ceisteanna a chumasc laistigh de JVM amháin.
4. **Iontráil:** cosnaíonn teorainneacha sioncronaithe agus líonra cuimhne agus VIES.
5. **HTTP:** nasc JDK `HttpClient` athúsáidte le teorainn ama gearr.
6. **Bailíochtaigh / Freagair seiceáil:** Boole neamhiomlán nó dáta →`MALFORMED_RESPONSE`.
7. **Taisce scríobh:** níl ach an toradh `Valid` barántúla i dtaisce.

## Bilingual error answers / Bilingual error answers

Tá an cód earráide meaisín i gcónaí cobhsaí agus teanga-neamhspleách. Is Ungáiris agus Béarla é `error()` tugann sé téacs úsáideora agus cinneadh atriail:

```java
var response = vies.check("HU00000000");
response.error().ifPresent(error -> {
    log.warn("{} | {} | retry={}", error.messageHu(), error.messageEn(), error.retryable());
});
```

| Toradh           |       HTTP |         Bain triail eile as | Taisce | Brí                                                                 |
| ---------------- | ---------: | --------------------------: | -----: | ------------------------------------------------------------------- |
| `Valid`          |        200 |                         níl |  tá/tá | VIES dearbhaithe mar bhailí / VIES confirmed valid                  |
| `Invalid`        |        200 |                         níl |    níl | Níor dheimhnigh VIES go raibh sé bailí / níor dheimhnigh VIES bailí |
| `Unavailable`    | 503 nó 429 | den chuid is mó/go hiondúil |    níl | Ní dhearnadh aon chinneadh bailíochta                               |
| `MalformedInput` |        400 |                         níl |    níl | Ní mór an t-ionchur a cheartú / Ionchur must be corrected           |

## Cumraíocht (tógálaí)

| Socrú                             | Bunluach                | Cad a dhéanann                                                                                                   |
| --------------------------------- | ----------------------- | ---------------------------------------------------------------------------------------------------------------- |
| `baseUrl(String)`                 | URL oifigiúil VIES REST | Atreorú sa triail bhréige                                                                                        |
| `connectTimeout(Duration)`        | 5 s                     | Teorainn ama ceangail TCP/TLS                                                                                    |
| `requestTimeout(Duration)`        | 8 s                     | Teorainn ama iomlán na n-iarratas (tá VIES idirghníomhach, coinnigh gearr é)                                     |
| `admissionTimeout(Duration)`      | 2 s                     | Seo é an fhad a fhanann sé le spás líonra saor in aisce                                                          |
| `defaultRequester(ViesRequester)` | níl aon                 | Uimhir chánach pobail féin → ID comhairliúcháin                                                                  |
| `retries(int)`                    | 0                       | Atriail uathoibríoch ar earráidí díomuana (0-5, cúl-aschur easpónantúil + Giodam)                                |
| `retryDelay(Duration)`            | 400 ms                  | Réamhshocrú cúltaca easpónantúil                                                                                 |
| `maxConcurrentRequests(int)`      | 32                      | Teorainn uasta na n-iarratas comhthráthach ar líonra VIES fíor                                                   |
| `maxPendingSyncRequests(int)`     | 512                     | Teorainn chuimhne do ghlaoiteoirí sioncronaithe comhuaineacha; os a chionn `CLIENT_OVERLOADED`                   |
| `maxPendingAsyncRequests(int)`    | 512                     | Teorainn chuimhne le haghaidh oibríochtaí sioncronaithe gníomhacha/ar feitheamh; os a chionn `CLIENT_OVERLOADED` |
| `cacheTtl(Duration)`              | 24 uair an chloig       | Am taisce le haghaidh amas bailí                                                                                 |
| `cacheMaxEntries(int)`            | 10,000                  | Teorainn méide taisce cuimhne ionsuite                                                                           |
| `cache(ViesCache)`                | tógtha i                | Inneall taisce féin (m.sh. Redis)                                                                                |
| `disableCache()`                  | —                       | Gan taisce stóráilte; féadfaidh glaonna comhuaineacha comhionanna single-flight a roinnt                         |
| `userAgent(String)`               | modúl-id                | Ba cheart duit tú féin a chur in aithne don AE                                                                   |
| `executor(ExecutorService)`       | snáithe fíorúil/tasc    | Mo sheiceadóir async; tá an glaoiteoir freagrach as a shaolré                                                    |

## Do thaisce féin (m.sh. Redis)

```java
class RedisViesCache implements ViesCache {
    public Optional<ViesResponse.Valid> get(String key) { /* ... */ }
    public void put(String key, ViesResponse.Valid value, Duration ttl) { /* ... */ }
}

var vies = ViesClient.builder().cache(new RedisViesCache()).build();
```

Níl ach toradh `Valid` i dtaisce - uimhir neamhbhailí agus earráid dhíomuan riamh.
Le haghaidh cuibheoir Redis, bain úsáid as teorainn ama gearr i dtaisce, spás ainm leagan agus úinéir
méadrach earráide. Earráid léite taisce Is é toradh `CACHE_ERROR` ná nuair a bhíonn Redis síos
ná cuir tús le hiarratas VIES neamhrialaithe stampede.
Is iad `consultationNumber` agus`requestDate` a cuireadh ar ais ón taisce **bunaidh**
comhairliúchán a dhoiciméadú; ní seiceáil VIES nua é an hit taisce. Le haghaidh teastas nua
seiceáil `fromCache`, bain úsáid as TTL gearr nó`disableCache()`.

## Séimeantaic fiú a fhios

1. **`Unavailable`≠`Invalid`.** Córais chúlra na mBallstát go rialta
   a thit (`MS_UNAVAILABLE`,`MS_MAX_CONCURRENT_REQ`...). Sa chás seo, an uimhir a
   **gan bhreith ag VIES** — toirmiscthe ar bhonn tráchtála a mheas neamhbhailí.
2. **Ní fíordheimhniú VIES é réamhscagadh formáide.** Scagtar an modúl cláraithe amach
   ionchur mícheart (`MalformedInput`) roimh dul go dtí an líonra, ach an bhailíocht
   is é a fhoinse freagra VIES i gcónaí.
3. **Méadaíonn an triail eile an t-ualach freisin.** Ná déan ach earráid shealadach go háitiúil cúpla uair
   arís; in oibríocht mhór, is é meicníocht athiarrachta moillithe na scuaine mharthanacha an príomh-mheicníocht.
4. **An Ghréig `EL`, Tuaisceart Éireann`XI`.** Láimhseálann an modúl an dá cheann,`GR`mapálann go huathoibríoch ionchur chuig`EL`.

## Tástálacha

```bash
./mvnw test    # unit + helyi HTTP konkurencia/retry/backpressure/lifecycle tesztek
```

---

## Béarla mear

Cliant Java 21+ gan spleáchas d'uimhir CBL VIES an Choimisiúin Eorpaigh
API REST bailíochtaithe. Tóg le `./mvnw package`, ansin:

```java
try (var vies = ViesClient.createDefault()) {
    if (vies.check("DE000000000") instanceof ViesResponse.Valid v) {
        System.out.println(v.traderName().orElse("?"));
    }
}
```

Is ordlathas séalaithe iad na torthaí (`Valid`/`Invalid`/`Unavailable`/`MalformedInput`) atá deartha le haghaidh patrún uileghabhálach meaitseáilte`switch`. Bailí
déantar na torthaí a thaisceadh i gcuimhne ar feadh 24 h; tuairiscítear bristeacha neamhbhuan VIES mar `Unavailable`, riamh mar`Invalid`. Doiciméadúchán oifigiúil API:
<https://ec.europa.eu/taxation_customs/vies/#/technical-information>.

## Oibríocht ardscála

Is féidir leis an gcliant seo a bheith ina chomhpháirt amháin de phíblíne próiseála milliún-mhír, ach
tá tréchur iarbhír VIES teoranta ag AE agus ballstáit athraitheach, neamhráthaithe
teorainneacha. Laghdaíonn snáitheanna fíorúla costas blocála; ní mhéadaíonn siad in aghaidh an tsrutha
acmhainn. Bain úsáid as scuaine deighilte marthanach, tomhaltóirí teorannacha, Redis roinnte
taisce, agus teorannóir ráta dáilte ar fud na n-oibrithe go léir. Áitiúil eitilt aonair agus
ní chosnaíonn semaphores ach JVM amháin. Ná caith `Unavailable` mar`Invalid` riamh ; úsáid`response.error()` le haghaidh cóid chobhsaí, in-triaileacht, agus teachtaireachtaí Ungáiris/Béarla.

## Cód foinse oscailte / Open source

Is féidir an tionscadal a úsáid, a mhodhnú agus
is féidir a dháileadh. Ceadaíonn an ceadúnas úsáid tráchtála agus mear
deonaíonn ceadúnas paitinne; ní mór téarmaí ceadúnais, sannadh agus modhnuithe a léamh
a garda. Sula ranníoc tú, léigh [CONTRIBUTING.md](CONTRIBUTING.md) agus
[CODE_OF_CONDUCT.md comhaid](CODE_OF_CONDUCT.md).
Tá an tionscadal seo ceadúnaithe faoi [ Cheadúnas Apache 2.0](../../../LICENSE), ceadmhach
ceadúnas le deontas sainráite paitinne. Féach [ CONTRIBUTING.md](CONTRIBUTING.md),
[ SECURITY.md](SECURITY.md), agus na [nótaí foinse oscailte mionsonraithe](OPEN_SOURCE.md).

## Tacaíocht agus síntiúis

Cuirtear tacaíocht phobail ar fáil ar bhonn na hiarrachta is fearr, trí cheisteanna agus trí phlé GitHub.
Tuairiscigh earráidí slándála ar chainéal príobháideach i gcónaí. Tá cothabháil an tionscadail suntasach
costais forbartha agus bonneagair i gceist; is é an cnaipe GitHub Síntiúis/Urraitheoir an cothaitheoir
le feiceáil tar éis do URL tacaíochta fíoraithe a shocrú.
Déantar an iarracht is fearr le tacaíocht an phobail trí cheisteanna agus trí phlé GitHub. Slándáil
caithfidh tuairiscí fanacht príobháideach. Cumasófar cnaipe Síntiúis/Urraitheora tar éis an
tá URL maoinithe fíoraithe an chothaitheora cumraithe.
