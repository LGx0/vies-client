# Malti (mt) — Testing

> [Għażla tal-lingwa](../../LANGUAGES.md) · Din il-lokalizzazzjoni hija għall-aċċessibbiltà. F’każ ta’ differenza, is-sors kanoniku tekniku jew legali bl-Ingliż jipprevali. `LICENSE` u `NOTICE` fir-root jibqgħu legalment awtorevoli.

## Xi nsejħu test unitarju? / X'inhu test unitarju?

Iva: it-test tal-unità jiċċekkja klassi waħda jew regola b'mod iżolat, estern
mingħajr netwerk, database u VIES ħajjin. Fast, deterministiku, u f'kull bini
tista 'taħdem.
Iva: test unitarju jivverifika klassi jew regola waħda waħedha, mingħajr netwerk estern,
database, jew VIES ħajjin. Huwa mgħaġġel, deterministiku, u adattat għal kull bini.
Dan il-modulu jeħtieġ ukoll **integrazzjoni lokali u testijiet tal-kompetizzjoni**
wkoll minħabba li l-imġieba timeout, retry, single-titjira, tikkanċella u `close()` huma aktar
huwa evidenti mill-kooperazzjoni tal-komponent. Huma jużaw loopback mock server, le
ċempel lis-servizz tal-UE.
Dan il-modulu jeħtieġ ukoll **integrazzjoni lokali u testijiet tal-konkorrenza**, minħabba li timeout,
ipprova mill-ġdid, titjira waħda, kanċellazzjoni, u għeluq jinvolvu komponenti multipli. Dawn
it-testijiet jużaw loopback mock server u qatt ma jċemplu lis-servizz pubbliku tal-UE.

## Kmandi ta' malajr / Kmandi ta' malajr

Pakkett sħiħ tat-test deterministiku / Suite deterministiku sħiħ:

```bash
./mvnw test
```

Testijiet unitarji biss:

```bash
./mvnw -Dtest=VatFormatTest,ViesRequesterTest,ViesResponseMappingTest,ViesErrorTest,ViesAvailabilityTest,ViesClientBuilderTest,MiniJsonTest,TtlCacheTest test
```

Testijiet HTTP/konmunita lokali biss/HTTP lokali u konmunita biss:

```bash
./mvnw -Dtest=ViesClientHttpTest test
```

Verifika nadifa bil-ġenerazzjoni JAR/Javadoc / Verifika nadifa bl-artifacts:

```bash
./mvnw clean verify
./mvnw package
```

Test wieħed speċifiku / Metodu wieħed tat-test:

```bash
./mvnw -Dtest=ViesClientHttpTest#cancellationDoesNotLeakAsyncCapacity test
```

## Kopertura kurrenti / Kopertura kurrenti

Il-pakkett deterministiku attwali fih **73 test**:

- **44 unit test** fi tmien klassijiet;
- **29 test lokali HTTP/integrazzjoni/konkorrenza**;
- żero sejħiet obbligatorji tan-netwerk estern.
  Is-suite deterministiku fih **73 test**: 44 test unitarju, 29 lokali
  Testijiet HTTP/integrazzjoni/konkorrenza, u żero sejħiet esterni obbligatorji.

## Katalgu tat-test tal-unità / Katalgu tat-test tal-unità

### `VatFormatTest`— 8 testijiet

| ID       | gowl Ungeriż                                                      | għan Ingliż                                             |
| -------- | ----------------------------------------------------------------- | ------------------------------------------------------- |
| U-FMT-01 | Normalizzazzjoni tan-numru totali tat-taxxa                       | Normalizza l-identifikatur sħiħ tal-VAT                 |
| U-FMT-02 | Neħħi spazju/punt/sing, ikkapitalizza                             | Separaturi ta' l-istrixxi u kaxxa ta' fuq               |
| U-FMT-03 | `GR`→`EL`mapping                                                  | Mappa tal-Greċja`GR`għal VIES`EL`                       |
| U-FMT-04 | Irrifjuta pajjiż null, vojt, mhux magħruf u tul mhux korrett      | Tiċħad null/vojt/mhux magħruf/tul ħażin                 |
| U-FMT-05 | Formoli rappreżentattivi tal-pajjiż                               | Formati rappreżentattivi tal-pajjiżi                    |
| U-FMT-06 | Kodiċi tal-pajjiż separat+numru API, prefiss mehmuż               | Par API mal-prefiss mehmuż                              |
| U-FMT-07 | Kodiċijiet tal-pajjiżi appoġġjati                                 | Sett ta' pajjiż appoġġjat                               |
| U-FMT-08 | It-28 pajjiż appoġġjat kollha għandhom mill-inqas forma waħda ta' | Mill-inqas forma waħda għat-28 kodiċi kollha appoġġjati |

### `ViesRequesterTest`— 4 testijiet

| ID       | gowl Ungeriż                                      | għan Ingliż                                    |
| -------- | ------------------------------------------------- | ---------------------------------------------- |
| U-REQ-01 | Imla minn min jitlob in-numru tat-taxxa stess     | Oħloq it-talba mill-VAT sħiħa                  |
| U-REQ-02 | Kanonizzazzjoni ta' min jagħmel it-talba Grieg    | Canonicalize Grieg li jagħmel it-talba         |
| U-REQ-03 | Kostruttur imqabbad bi prefiss mehmuż             | Kostruttur tal-par bi prefiss li jaqbel        |
| U-REQ-04 | Ċaħda immedjata ta' min jagħmel it-talba difettuż | Falli malajr fuq min jagħmel it-talba invalidu |

### `ViesResponseMappingTest`— 11-il test

| ID       | gowl Ungeriż                                       | għan Ingliż                                     |
| -------- | -------------------------------------------------- | ----------------------------------------------- |
| U-MAP-01 | `Valid` tal-istil GET bl-oqsma kollha              | Mappa GET-style Reazzjoni valida                |
| U-MAP-02 | `Valid` ta' stil POST ,`---` placeholder           | Mappa stil POST Validu u placeholders           |
| U-MAP-03 | Awtentiku `Invalid`                                | Mappa awtorevoli Invalida                       |
| U-MAP-04 | Żball temporanju →`Unavailable`                    | Immappja żball temporanju għal Mhux Disponibbli |
| U-MAP-05 | Żball tad-dħul →`MalformedInput`                   | Żball tal-input tal-Mappa VIES                  |
| U-MAP-06 | Irrifjuta JSON mhux oġġett                         | Irrifjuta JSON mhux oġġett                      |
| U-MAP-07 | Boolean nieqes ma jistax ikun `Invalid`            | Boolean nieqes qatt ma jsir Invalidu            |
| U-MAP-08 | String boolean ma jistax ikun `Invalid`            | Validità mhux booleana miċħuda                  |
| U-MAP-09 | Ma nsibux ħin lokali għal data ta' verifika nieqsa | Qatt tivvinta timestamp tal-verifika nieqsa     |
| U-MAP-10 | Irrifjuta data tal-verifika mhux korretta          | Tiċħad it-timestamp invalidu tal-verifika       |
| U-MAP-11 | Konverżjoni korretta tal-timestamp offset għal UTC | Analiżi l-timestamp tal-offset għal UTC         |

### `ViesErrorTest`— 6 testijiet

| ID       | gowl Ungeriż                                                              | għan Ingliż                                                |
| -------- | ------------------------------------------------------------------------- | ---------------------------------------------------------- |
| U-ERR-01 | Messaġġ tan-netwerk Ungeriż+Ingliż                                        | Żball tan-netwerk bilingwi                                 |
| U-ERR-02 | Żball tad-dħul ma jistax jerġa' jiġi ppruvat                              | L-iżball tad-dħul huwa permanenti                          |
| U-ERR-03 | HTTP 408/429/5xx klassifikazzjoni mill-ġdid ipprova                       | klassifikazzjoni mill-ġdid HTTP                            |
| U-ERR-04 | `Valid`/`Invalid` mhuwiex żball                                           | Deċiżjonijiet ma jesponu ebda żball                        |
| U-ERR-05 | Il-kodiċijiet pubbliċi kollha HU+EN u l-valur tal-prova mill-ġdid stabbli | Kull kodiċi pubbliku għandu HU+EN u erġa' pprova politika  |
| U-ERR-06 | Żomm kodiċi mhux magħruf mingħajr tempesta mill-ġdid                      | Ippreserva kodiċi mhux magħruf mingħajr tempesta mill-ġdid |

### `ViesAvailabilityTest`— 2 testijiet

- kopja ta' protezzjoni u immutabilità tal-mappa tal-input;
- rifjut immedjat tal-mappa null tal-istat membru.
  Kopja ta 'snapshot immutabbli difensiva u validazzjoni tal-kostruttur tal-mappa null.

### `MiniJsonTest`— 4 testijiet

- dokument VIES tipiku u ħarba Unicode;
- oġġett/lista/skala/numru/null imniżżel;
- JSON jaħrab;
- rifjut ta' input mhux korrett, maqtugħ u wara.
  VIES JSON tipiku, valuri nested/skalari, ħarbiet, u input ifformat ħażin magħluq.

### `TtlCacheTest`— 6 testijiet

- hit qabel TTL, miss wara;
- limitu TTL eżatt;
- jinjora TTL mhux pożittiv;
- limitu ta' daqs konfigurat;
- spostament preferut ta' element skadut;
- 32 ħajt virtwali kitba/qari konkorrenti.
  Imġieba TTL, limitu ta 'skadenza eżatta, kontroll tad-daqs, kampjun skaduti-ewwel, u
  pressjoni konkorrenti minn 32 ħajt virtwali.

### `ViesClientBuilderTest`— 3 testijiet

- konfigurazzjoni sħiħa ta 'tagħbija għolja tista' tinbena;
- tirrifjuta URL/limit/erġa' pprova ħażin;
- rifjut ta' żero, negattiv u Tul ta' overflow.
  Konfigurazzjoni valida ta' tagħbija għolja flimkien mal-URL, limitu, ipprova mill-ġdid, tul ta' żmien u overflow
  validazzjoni.

## HTTP lokali, konkorrenza u ċiklu tal-ħajja / Integrazzjoni lokali u konkorrenza

`ViesClientHttpTest`— 29 test fuq port loopback bla każwali:
| ID | Każ tat-test |
|---|---|
| I-01 | 503 jerġa' jipprova darbtejn, imbagħad suċċess / żewġ 503 jerġa' jipprova mbagħad suċċess |
| C-01 | 200 min iċempel asinkroniku bl-istess ċavetta → eżattament 1 talba HTTP / 200 ċempel asinkroniku bl-istess ċavetta → talba waħda |
| C-02 | It-talbiet HTTP attivi ma jaqbżux il-limitu ta' 4 / sejħiet attivi jibqgħu fi żmien 4 |
| C-03 | Async pendenti 'l fuq mil-limitu immedjat `CLIENT_OVERLOADED`|
| C-04 | Kanċellazzjoni futura ma tnixxix permess, il-kapaċità hija restawrata |
| C-05 |`close()` ma jimblokkax minn callback async |
| C-06 |`admissionTimeout` jillimita l-kju |
| I-02 | Redis/cache qari żball →`CACHE_ERROR`, sejħa VIES null |
| C-07 | Permess u titjira waħda | huma rilaxxati qabel sejħiet asinkroniċi konsekuttivi
| C-08 | Async cache/tiġrija mill-qrib →`CLIENT_CLOSED`, null HTTP |
| C-09 | Issinkronizza l-cache/agħlaq it-tiġrija →`CLIENT_CLOSED`, null HTTP |
| C-10 | Ix-xogħol tal-eżekutur personalizzat jiġi interrott, iżda l-eżekutur ma jagħlaqx |
| C-11 | Il-mexxej tas-sinkronizzazzjoni u s-segwaċi jiksbu l-istess riżultat `CLIENT_CLOSED`|
| C-12 | 100 segwaċi asinkroniku identiku ma jikkunsmawx 100 slots pendenti |
| C-13 | Wara r-rifjut tal-eżekutur, il-permess u l-istatus tat-titjira jiġu rrestawrati |
| C-14 | Mexxej tas-sinkronizzazzjoni + follower asinkroniku → talba HTTP waħda |
| C-15 | It-tieni cache-check jagħlaq it-tiġrija ta' stale-miss, null HTTP |
| C-16 | Fl-għeluq waqt il-kitba tal-cache, ir-riżultat tas-sinkronizzazzjoni tal-mexxej/segwaċi huwa l-istess |
| C-17 | Il-permess pendenti | jiġi wkoll rilaxxat qabel sejħa asinkronizzata b'katina b'ċwievet differenti
| C-18 | Kompitu ta' eżekutur tad-dwana fil-kju huwa attwalment ikkanċellat mill-qrib |
| C-19 | L-imblukkar ta' callback ta' l-utent ma jimblokkax il-qfil mill-qrib/ċiklu tal-ħajja |
| C-20 | Mexxej asinkroniku + segwaċi tas-sinkronizzazzjoni → talba HTTP waħda |
| C-21 |`maxPendingSyncRequests` jagħti pressjoni b'lura limitata immedjata |
| C-22 | Fatal async `Error` tkompli wkoll lejn futur eċċezzjonali u handler mhux maqbud |

## X'ma nittestjawx b'mod awtomatiku? / X'inhu mhux fil-suit default?

## Live VIES smoke test / Live VIES smoke test

Is-servizz dirett huwa varjabbli u limitat bir-rata, għalhekk ma jistax ikun hemm kundizzjoni CI obbligatorja.
Test manwali tad-duħħan huwa l-aktar wieħed `availability()` u magħruf, mhux sigriet jew
mistoqsija n-numru tat-taxxa miksub minn varjabbli ambjentali, concurrency=1 u jerġa' jipprova=0
iffissar.
Live VIES huwa varjabbli u limitat bir-rata, għalhekk m'għandux iwassal għal CI normali. Opt-in
test tad-duħħan għandu jwettaq l-iktar kontroll wieħed tad-disponibbiltà u validazzjoni waħda, bil
concurrency=1 u jerġa' jipprova=0. Qatt tikkommetti jew tirreġistra n-numri tal-VAT ta' min jagħmel it-talba privat.

## Test tat-tagħbija / Test tat-tagħbija

Dejjem mexxi kontra mock lokali jew is-servizz ta' staging tiegħek stess, qatt ma dak pubbliku
kontra VIES. Req/s assoluti huma informattivi; korrettezza, limitu, p95/p99 u kodiċijiet ta 'żball
il-gradi.
Dejjem immira lejn servizz ta' staging mock lokali jew proprjetà, qatt VIES pubbliċi. Assoluta
talbiet/sekonda huwa informattiv; korrettezza, limiti, p95/p99, u semantika ta’ żball
huma l-gradi.
Każijiet rakkomandati:

- ħafna ċwievet distinti;
- 100k hot-key sejjieħ fuq sett żgħir ta 'ċwievet / hot-key stampede;
- tagħbija żejda u rkupru 'l fuq mil-limitu;
- 200/429/503/timeout/rispons imħallat ħażin;
- cache-pressjoni madwar il-mass konfigurat.

## Soak and chaos test / Soak and chaos test

Tagħbija fissa ta' 30-60 minuta, borġ limitat, JFR, għeluq ripetut/startjar mill-ġdid, latenza
spike, reset tal-konnessjoni, falliment tal-cache u kanċellazzjoni. Il-borġ, ħjut attivi,
numru ta 'in-titjira/pendenti u sockets plateau; m'għandux ikun hemm staġnar jew tnixxija ta' permessi.
Mexxi 30–60 minuta b'munzell fiss, JFR, ċiklu tal-ħajja ripetut, spikes ta' latenza,
resets tal-konnessjoni, fallimenti tal-cache, u kanċellazzjoni. Borġ / ħjut / waqt it-titjira / sokits
għandu plateau mingħajr deadlock jew permess tnixxijiet.
Eżempju JFR / eżempju JFR:

```bash
JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=target/vies-soak.jfr,settings=profile" ./mvnw test
```

## javaslat BISS / rakkomandat BISS

Pipeline minimu:

```bash
./mvnw --batch-mode clean verify
```

Matriċi rakkomandata: mill-inqas JDK 21 LTS; JDKs addizzjonali jistgħu jiġu stabbiliti biss imbagħad
appoġġjat jekk l-istess suite fil-fatt timxi fuqhom.
Matriċi rakkomandata: mill-inqas JDK 21 LTS. Itlob appoġġ addizzjonali JDK biss wara
taħdem l-istess suite fuq dawk il-verżjonijiet.

## Ir-regola tar-rigress / Regression regression

Kull bug fiss għandu jkollu test deterministiku li huwa l-iffissar
kien ifalli qabel. Lukkett/barriera għandha tkun il-prijorità għat-test tal-kompetizzjoni
sinkronizzazzjoni;`sleep` fiss jista 'jkun biss backoff qasir ta' votazzjoni, mhux oraklu ta 'korrettezza.
Kull bug fiss irid jirċievi test deterministiku li falla qabel it-tiswija.
Jippreferi lukketti/barrieri għat-testijiet tal-konkorrenza; irqad fissi jista 'jkun qosra polling
backoff biss, qatt l-oraklu korrettezza.
