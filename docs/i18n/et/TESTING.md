# Eesti (et) — Testing

> [Keelevalik](../../LANGUAGES.md) · See lokaliseering parandab ligipääsetavust. Lahknevuse korral kehtib kanooniline ingliskeelne tehniline või õiguslik allikas. Juure `LICENSE` ja`NOTICE` jäävad õiguslikult määravaks.

## Mida me nimetame ühikutestiks? / Mis on ühikutest?

Jah: ühiktest kontrollib ühte klassi või reeglit isoleeritult, väliselt
ilma võrgu, andmebaasita ja reaalajas VIES-ita. Kiire, deterministlik ja igas ehituses
saab joosta.
Jah: üksuse test kontrollib ühte klassi või reeglit isoleeritult, ilma välise võrguta,
andmebaasi või reaalajas VIES-i. See on kiire, deterministlik ja sobib iga ehituse jaoks.
See moodul nõuab ka **kohalikke integratsiooni- ja konkurentsiteste**
ka seetõttu, et ajalõpu, uuesti proovimise, ühe lennu, tühistamise ja `close()` käitumised on rohkem
see ilmneb komponendi koostööst. Nad kasutavad loopbacki näidisserverit, ei
helistage ELi talitusse.
See moodul vajab ka **kohaliku integratsiooni ja samaaegsuse teste**, kuna ajalõpp,
uuesti proovimine, üks lend, tühistamine ja sulgemine hõlmavad mitut komponenti. Need
testid kasutavad loopbacki näidisserverit ega helista kunagi ELi avalikku teenust.

## Kiirkäsklused / Kiirkäsklused

Täielik deterministlik testpakett / täielik deterministlik komplekt:

```bash
./mvnw test
```

Ainult ühikutestid:

```bash
./mvnw -Dtest=VatFormatTest,ViesRequesterTest,ViesResponseMappingTest,ViesErrorTest,ViesAvailabilityTest,ViesClientBuilderTest,MiniJsonTest,TtlCacheTest test
```

Ainult kohalikud HTTP/samaaegsuse testid / ainult kohalik HTTP ja samaaegsus:

```bash
./mvnw -Dtest=ViesClientHttpTest test
```

Puhas kinnitus JAR-i/Javadoci genereerimisega / Puhasta kinnitus artefaktidega:

```bash
./mvnw clean verify
./mvnw package
```

Üks konkreetne test / üks katsemeetod:

```bash
./mvnw -Dtest=ViesClientHttpTest#cancellationDoesNotLeakAsyncCapacity test
```

## Praegune levi / praegune levi

Praegune deterministlik pakett sisaldab **73 testi**:

- **44 ühiku test** kaheksas klassis;
- **29 kohalikku HTTP/integratsiooni/samaaegsuse testi**;
- null kohustuslikku välisvõrgu kõnet.
  Deterministlik komplekt sisaldab **73 testi**: 44 ühiktesti, 29 kohalikku
  HTTP/integratsiooni/samaaegsuse testid ja null kohustuslikku väliskõnet.

## Ühikutestide kataloog / Ühikutestide kataloog

### `VatFormatTest`— 8 testi

| ID       | Ungari värav                                    | Inglise otstarve                                  |
| -------- | ----------------------------------------------- | ------------------------------------------------- |
| U-FMT-01 | Kogu maksunumbri normaliseerimine               | Normaliseerida kogu käibemaksukohustuslase tunnus |
| U-FMT-02 | Eemalda tühik/punkt/sidekriips, suurtähtedega   | Ribade eraldajad ja suurtäht                      |
| U-FMT-03 | `GR`→`EL`kaardistamine                          | Kaart Kreeka`GR`kuni VIES`EL`                     |
| U-FMT-04 | Keeldu null, tühi, tundmatu riik ja vale pikkus | Keeldu null/tühi/tundmatu/halb pikkus             |
| U-FMT-05 | Esindusriigi vormid                             | Esinduslikud riigivormingud                       |
| U-FMT-06 | Eraldi riigikood+number API, lisatud eesliide   | Siduge API koos lisatud eesliitega                |
| U-FMT-07 | Toetatud riigikoodid                            | Toetatud riigi komplekt                           |
| U-FMT-08 | Kõigil 28 toetatud riigil on vähemalt üks       | Kõigi 28 toetatud koodi jaoks vähemalt üks kujund |

### `ViesRequesterTest`— 4 testi

| ID       | Ungari värav                                | Inglise otstarve                       |
| -------- | ------------------------------------------- | -------------------------------------- |
| U-REQ-01 | Täida oma maksunumbri taotlejalt            | Loo taotleja täielikust käibemaksust   |
| U-REQ-02 | Kreeka taotleja kanoniseerimine             | Kanoniseeri kreeka taotleja            |
| U-REQ-03 | Seotud konstruktor lisatud eesliitega       | Paari konstruktor sobiva eesliitega    |
| U-REQ-04 | Vigase taotleja viivitamatu tagasilükkamine | Ebaõnnestumine kehtetu taotleja korral |

### `ViesResponseMappingTest`— 11 testi

| ID       | Ungari värav                                           | Inglise otstarve                                |
| -------- | ------------------------------------------------------ | ----------------------------------------------- |
| U-MAP-01 | GET-stiilis `Valid` kõigi väljadega                    | Kaart GET-stiilis Kehtiv vastus                 |
| U-MAP-02 | POST-stiilis `Valid`,`---` kohatäide                   | Kaart POST-stiilis Kehtivad ja kohahoidjad      |
| U-MAP-03 | Autentne `Invalid`                                     | Kaardi autoriteetne Kehtetu                     |
| U-MAP-04 | Mööduv viga →`Unavailable`                             | Kaardi mööduv viga olekusse Pole saadaval       |
| U-MAP-05 | Sisestusviga →`MalformedInput`                         | Kaardi VIES-i sisestusviga                      |
| U-MAP-06 | Keeldu mitteobjektilisest JSON-ist                     | Keeldu mitteobjektilisest JSON-ist              |
| U-MAP-07 | Puuduv tõeväärtus ei saa olla `Invalid`                | Puuduv tõeväärtus ei muutu kunagi kehtetuks     |
| U-MAP-08 | Stringi tõeväärtus ei saa olla `Invalid`               | Mittetõve kehtivus lükati tagasi                |
| U-MAP-09 | Me ei leia puuduva auditikuupäeva kohta kohalikku aega | Ärge kunagi leiutage puuduvat auditi ajatemplit |
| U-MAP-10 | Vale auditikuupäeva tagasilükkamine                    | Keeldu kehtetu auditi ajatempel                 |
| U-MAP-11 | Nihke ajatempli õige teisendamine UTC                  | Parsi nihke ajatempel UTC-ks                    |

### `ViesErrorTest`— 6 testi

| ID       | Ungari värav                                                | Inglise otstarve                                           |
| -------- | ----------------------------------------------------------- | ---------------------------------------------------------- |
| U-ERR-01 | Ungari+inglise võrgusõnum                                   | Kakskeelse võrgu viga                                      |
| U-ERR-02 | Sisestusviga ei saa uuesti proovida                         | Sisestusviga on püsiv                                      |
| U-ERR-03 | HTTP 408/429/5xx proovi klassifikatsiooni uuesti            | HTTP uuesti proovimise klassifikatsioon                    |
| U-ERR-04 | `Valid`/`Invalid` ei ole viga                               | Otsused ei paljasta viga                                   |
| U-ERR-05 | Kõik avalikud koodid HU+EN ja stabiilne korduskatse väärtus | Igal avalikul koodil on HU+EN ja uuesti proovimise eeskiri |
| U-ERR-06 | Säilitage tundmatu kood ilma tormi uuesti proovimiseta      | Säilitage tundmatu kood ilma tormi uuesti proovimiseta     |

### `ViesAvailabilityTest`— 2 testi

- kaitsekoopia ja sisendkaardi muutumatus;
- nullliikmesriigi kaardi viivitamatu tagasilükkamine.
  Kaitsev muutumatu hetktõmmise koopia ja nullkaardi konstruktori valideerimine.

### `MiniJsonTest`— 4 testi

- tüüpiline VIES dokument ja Unicode põgenemine;
- pesastatud objekt/loend/skalaar/arv/null;
- JSON põgeneb;
- vale, kärbitud ja lõpus oleva sisendi tagasilükkamine.
  Tüüpiline VIES JSON, pesastatud/skalaarsed väärtused, paotused ja tõrkekinnitusega valesti vormindatud sisend.

### `TtlCacheTest`— 6 testi

- tabas enne TTL-i, miss pärast;
- täpne TTL limiit;
- ignoreerida mittepositiivset TTL-i;
- konfigureeritud suuruse piirang;
- aegunud elemendi eelistatud nihkumine;
- 32 virtuaalset lõime samaaegne kirjutamine/lugemine.
  TTL käitumine, täpne aegumispiir, suuruse kontroll, aegunud-esimene proovide võtmine ja
  samaaegne rõhk 32 virtuaalsest lõimest.

### `ViesClientBuilderTest`— 3 testi

- saab ehitada täieliku suure koormusega konfiguratsiooni;
- lükka tagasi vale URL/limit/retry;
- nulli, negatiivse ja ülevoolu tagasilükkamine Kestus.
  Kehtiv suure koormusega konfiguratsioon pluss URL, limiit, korduskatse, kestus ja ületäitumine
  kinnitamine.

## Kohalik HTTP, samaaegsus ja elutsükkel / Kohalik integratsioon ja samaaegsus

`ViesClientHttpTest`— 29 testi juhuslikus vabas loopback-pordis:
| ID | Katsejuhtum |
|---|---|
| I-01 | 503 korduskatset kaks korda, siis edu / kaks 503 korduskatset ja siis edu |
| C-01 | 200 sama klahviga asünkroonilist helistajat → täpselt 1 HTTP päring / 200 sama klahviga asünkroonilist helistajat → üks päring |
| C-02 | Aktiivsed HTTP-päringud ei ületa 4. piirangut / aktiivsed kõned jäävad 4 |
| C-03 | Asünkroonimine ootel üle limiidi koheselt `CLIENT_OVERLOADED`|
| C-04 | Tulevane tühistamine ei leki, võimsus on taastatud |
| C-05 |`close()` ei lähe ummikseisu asünkroonilisest tagasihelistamisest |
| C-06 |`admissionTimeout` piirab järjekorda |
| I-02 | Redis/cache lugemisviga →`CACHE_ERROR`, null VIES kõne |
| C-07 | Luba ja ühekordne lend | vabastatakse enne järjestikuseid asünkroonimiskõnesid
| C-08 | Asünkrooni vahemälu/sulge võistlus →`CLIENT_CLOSED`, null HTTP |
| C-09 | Sünkrooni vahemälu / sulge võistlus →`CLIENT_CLOSED`, null HTTP |
| C-10 | Kohandatud täitja töö katkestatakse, kuid täitja ei sulgu |
| C-11 | Sünkroonimise juht ja järgija saavad sama tulemuse `CLIENT_CLOSED`|
| C-12 | 100 identset asünkroonset jälgijat ei kasuta 100 ootel olevat pesa |
| C-13 | Pärast täitja tagasilükkamist taastatakse luba ja olek lennu ajal |
| C-14 | Sünkroonimise juht + asünkrooniline jälgija → üks HTTP-päring |
| C-15 | Teine vahemälu kontroll sulgeb aegunud vahelejäämise võistluse, null HTTP |
| C-16 | Vahemällu kirjutamise ajal sulgemisel on sünkroonimise juhi/järgija tulemus sama |
| C-17 | Ootel luba | vabastatakse ka enne aheldatud asünkroonkõnet erinevate klahvidega
| C-18 | Järjekorras olev kohandatud täitja ülesanne tühistatakse sulgemisega |
| C-19 | Kasutaja tagasihelistamise blokeerimine ei blokeeri sulgemise/elutsükli lukku |
| C-20 | Asünkroonimise juht + sünkroonimise jälgija → üks HTTP-päring |
| C-21 |`maxPendingSyncRequests` annab kohese piiratud vasturõhu |
| C-22 | Fatal async `Error` jätkab ka erakordse tuleviku ja tabamata käitleja poole

## Mida me vaikimisi ei testi? / Mida vaikekomplektis pole?

## Live VIES suitsutest / Live VIES suitsutest

Reaalajas teenus on muutuv ja kiirusega piiratud, seega ei saa olla kohustuslikku CI tingimust.
Käsitsi suitsutest on maksimaalselt üks `availability()` ja teadaolev, mittesalajane või
küsige keskkonnamuutujast saadud maksunumbrit, concurrency=1 ja retries=0
seadistus.
Live VIES on muutuv ja kiirusega piiratud, seega ei tohi see tavalist CI-d piirata. Osalemine
suitsutest peaks läbi viima maksimaalselt ühe saadavuse kontrolli ja ühe valideerimise koos
concurrency=1 ja proovid=0. Ärge kunagi sisestage ega logige sisse privaatseid taotleja KMKR-numbreid.

## Koormustest / Koormustest

Astuge alati vastu kohalikule pilale või oma lavastusteenusele, mitte kunagi avalikule
VIESi vastu. Absoluutsed nõuded on informatiivsed; korrektsus, piirmäär, p95/p99 ja veakoodid
väravad.
Sihtige alati kohalikku näidisteenust või omanduses olevat lavastusteenust, mitte kunagi avalikku VIES-i. Absoluutne
päringud/sekund on informatiivne; korrektsus, piirid, p95/p99 ja veasemantika
on väravad.
Soovitatavad juhtumid:

- palju erinevaid võtmeid;
- 100 000 kiirklahviga helistajat väikesel klahvikomplektil / kiirklahvide löögiga;
- ülekoormus ja taastumine üle piiri;
- 200/429/503/timeout/valformeerunud segavastus;
- vahemälu rõhk konfigureeritud max.

## Soak and chaos test / Soak and chaos test

30-60 minutit fikseeritud koormus, piiratud hunnik, JFR, korduv sulgemine/taaskäivitus, latentsus
spike, ühenduse lähtestamine, vahemälu rike ja tühistamine. Hunnik, aktiivsed lõimed,
lennu ajal/ootel olevate ja pistikupesade platoo arv; ei tohiks olla ummikseisu ega lekkeid.
Käitage 30–60 minutit fikseeritud kuhjaga, JFR, korduv elutsükkel, latentsusajad,
ühenduse lähtestamine, vahemälu tõrked ja tühistamine. Kuhja/niidid/lennu ajal/pistikupesad
peab platoo olema ilma ummikseisuta või leketeta.
JFR näide / JFR näide:

```bash
JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=target/vies-soak.jfr,settings=profile" ./mvnw test
```

## AINULT soovitus / AINULT soovitatav

Minimaalne torujuhe:

```bash
./mvnw --batch-mode clean verify
```

Soovitatav maatriks: vähemalt JDK 21 LTS; täiendavaid JDK-sid saab määrata alles siis
toetatud, kui sama komplekt ka tegelikult töötab.
Soovitatav maatriks: vähemalt JDK 21 LTS. Taotlege JDK täiendavat tuge alles pärast seda
töötab nendes versioonides sama komplekti.

## Regressioonireegel / Regressioonireegel

Igal parandatud veal peaks olema deterministlik test, mis on parandatav
oleks varem läbi kukkunud. Lukk/tõke peaks olema võistlustesti prioriteet
sünkroniseerimine; fikseeritud `sleep` saab olla vaid lühike küsitluse taganemine, mitte õigsuse oraakel.
Iga parandatud viga peab läbima deterministliku testi, mis enne parandamist ebaõnnestus.
Eelista samaaegsustestide sulgurid/tõkked; fikseeritud uned võivad olla lühikesed
ainult taganemine, mitte kunagi korrektsuse oraakel.
