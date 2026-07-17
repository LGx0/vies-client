# Latviešu (lv) — Testing

> [Valodu izvēle](../../LANGUAGES.md) · Šī lokalizācija uzlabo pieejamību. Atšķirību gadījumā noteicošais ir kanoniskais angļu tehniskais vai juridiskais avots. Saknes `LICENSE` un`NOTICE` paliek juridiski saistoši.

## Ko mēs saucam par vienības testu? / Kas ir vienības tests?

Jā: vienības tests pārbauda vienu klasi vai noteikumu atsevišķi, ārēju
bez tīkla, datu bāzes un tiešraides VIES. Ātri, deterministiski un katrā būvējumā
var skriet.
Jā: vienības tests pārbauda vienu klasi vai noteikumu atsevišķi, bez ārējā tīkla,
datu bāzē vai tiešraidē VIES. Tas ir ātrs, deterministisks un piemērots jebkurai konstrukcijai.
Šim modulim ir nepieciešami arī **lokālās integrācijas un sacensību testi**
arī tāpēc, ka taimauta, atkārtota mēģinājuma, viena lidojuma, atcelšanas un `close()` darbības ir vairāk
tas ir redzams no komponenta sadarbības. Viņi izmanto loopback viltus serveri, nē
zvaniet uz ES dienestu.
Šim modulim ir nepieciešamas arī **lokālās integrācijas un vienlaicības pārbaudes**, jo taimauts,
atkārtot, viens lidojums, atcelšana un izslēgšana ietver vairākus komponentus. Šie
testos tiek izmantots cilpas imitācijas serveris un nekad netiek zvanīts publiskajam ES dienestam.

## Ātrās komandas / Ātrās komandas

Pilna deterministiskā testa pakotne / Pilns deterministiskais komplekts:

```bash
./mvnw test
```

Tikai vienības testi:

```bash
./mvnw -Dtest=VatFormatTest,ViesRequesterTest,ViesResponseMappingTest,ViesErrorTest,ViesAvailabilityTest,ViesClientBuilderTest,MiniJsonTest,TtlCacheTest test
```

Tikai lokālie HTTP/laikalaika testi/Tikai lokālais HTTP un vienlaicība:

```bash
./mvnw -Dtest=ViesClientHttpTest test
```

Tīra verifikācija ar JAR/Javadoc paaudzi / Tīra verifikācija ar artefaktiem:

```bash
./mvnw clean verify
./mvnw package
```

Viens konkrēts tests / viena testa metode:

```bash
./mvnw -Dtest=ViesClientHttpTest#cancellationDoesNotLeakAsyncCapacity test
```

## Pašreizējais pārklājums / Pašreizējais pārklājums

Pašreizējā deterministiskā pakotne satur **73 testus**:

- **44 vienību ieskaite** astoņās klasēs;
- **29 lokālie HTTP/integrācijas/vienlaicīguma testi**;
- nulle obligāto ārējā tīkla zvanu.
  Deterministiskajā komplektā ir **73 testi**: 44 vienību testi, 29 lokāli
  HTTP/integrācijas/vienlaicības testi un nulle obligāto ārējo zvanu.

## Vienības pārbaudes katalogs / Vienības pārbaudes katalogs

### `VatFormatTest`— 8 testi

| ID       | Ungārijas vārti                                            | angļu mērķis                                       |
| -------- | ---------------------------------------------------------- | -------------------------------------------------- |
| U-FMT-01 | Kopējā nodokļu numura normalizēšana                        | Normalizēt pilnu PVN identifikatoru                |
| U-FMT-02 | Noņemiet atstarpi/punktu/defisi, rakstiet lielos burtus    | Sloksnes atdalītāji un lielie burti                |
| U-FMT-03 | `GR`→`EL`kartēšana                                         | Kartēt Grieķijas`GR`uz VIES`EL`                    |
| U-FMT-04 | Noraidīt nulli, tukšu, nezināmu valsti un nepareizu garumu | Noraidīt null/tukšs/nezināms/nepareizs garums      |
| U-FMT-05 | Reprezentatīvās valsts veidlapas                           | Reprezentatīvie valstu formāti                     |
| U-FMT-06 | Atsevišķs valsts kods+numurs API, pievienots prefikss      | Savienot API ar pievienotu prefiksu                |
| U-FMT-07 | Atbalstītie valstu kodi                                    | Atbalstītās valsts komplekts                       |
| U-FMT-08 | Visās 28 atbalstītajās valstīs ir vismaz viena forma       | Vismaz viena forma visiem 28 atbalstītajiem kodiem |

### `ViesRequesterTest`— 4 testi

| ID       | Ungārijas vārti                                    | angļu mērķis                                  |
| -------- | -------------------------------------------------- | --------------------------------------------- |
| U-REQ-01 | Aizpildīt no sava nodokļu numura pieprasītāja      | Izveidot pieprasītāju no pilna PVN            |
| U-REQ-02 | Grieķu pieprasītāja kanonizācija                   | Kanonizēt grieķu pieprasītāju                 |
| U-REQ-03 | Pārī savienots konstruktors ar pievienotu prefiksu | Pārī konstruktors ar atbilstošu prefiksu      |
| U-REQ-04 | Tūlītēja kļūdaina pieprasītāja noraidīšana         | Ātra neveiksme nederīga pieprasītāja gadījumā |

### `ViesResponseMappingTest`— 11 testi

| ID       | Ungārijas vārti                                         | angļu mērķis                                        |
| -------- | ------------------------------------------------------- | --------------------------------------------------- |
| U-MAP-01 | GET stila `Valid` ar visiem laukiem                     | Karte GET stilā Derīga atbilde                      |
| U-MAP-02 | POST stila `Valid`,`---` vietturis                      | Karte POST stilā Derīgs un vietturi                 |
| U-MAP-03 | Autentisks `Invalid`                                    | Kartes autoritatīvs Nederīgs                        |
| U-MAP-04 | Pārejoša kļūda →`Unavailable`                           | Kartes pārejoša kļūda uz Nav pieejams               |
| U-MAP-05 | Ievades kļūda →`MalformedInput`                         | Kartes VIES ievades kļūda                           |
| U-MAP-06 | Noraidīt neobjekta JSON                                 | Noraidīt neobjekta JSON                             |
| U-MAP-07 | Trūkstošā Būla vērtība nevar būt `Invalid`              | Trūkstošais Būla vērtība nekad nekļūst par nederīgu |
| U-MAP-08 | Virknes Būla vērtība nevar būt `Invalid`                | Nebūla derīgums noraidīts                           |
| U-MAP-09 | Mēs neatrodam vietējo laiku trūkstošajam audita datumam | Nekad neizgudrojiet trūkstošo audita laikspiedolu   |
| U-MAP-10 | Noraidīt nepareizu audita datumu                        | Noraidīt nederīgu audita laikspiedolu               |
| U-MAP-11 | Pareiza nobīdes laika zīmoga konvertēšana uz UTC        | Parsēt nobīdes laikspiedolu uz UTC                  |

### `ViesErrorTest`— 6 testi

| ID       | Ungārijas vārti                                                   | angļu mērķis                                               |
| -------- | ----------------------------------------------------------------- | ---------------------------------------------------------- |
| U-ERR-01 | Ungāru+angļu tīkla ziņojums                                       | Divvalodu tīkla kļūda                                      |
| U-ERR-02 | Ievades kļūdu nevar mēģināt atkārtoti                             | Ievades kļūda ir pastāvīga                                 |
| U-ERR-03 | HTTP 408/429/5xx atkārtota klasifikācija                          | HTTP atkārtota mēģinājuma klasifikācija                    |
| U-ERR-04 | `Valid`/`Invalid` nav kļūda                                       | Lēmumi neatklāj kļūdu                                      |
| U-ERR-05 | Visi publiskie kodi HU+EN un stabila atkārtota mēģinājuma vērtība | Katram publiskajam kodam ir HU+EN un atkārtošanas politika |
| U-ERR-06 | Saglabājiet nezināmu kodu bez atkārtotas vētras                   | Saglabājiet nezināmu kodu bez atkārtotas vētras            |

### `ViesAvailabilityTest`— 2 testi

- aizsardzības kopija un ievades kartes nemainīgums;
- tūlītēja nulles dalībvalsts kartes noraidīšana.
  Aizsardzības nemainīga momentuzņēmuma kopija un nulles kartes konstruktora validācija.

### `MiniJsonTest`— 4 testi

- tipisks VIES dokuments un Unicode aizbēgšana;
- ligzdots objekts/saraksts/skalārs/skaitlis/nulle;
- JSON aizbēg;
- nepareizas, saīsinātas un beigu ievades noraidīšana.
  Tipiski VIES JSON, ligzdotās/skalārās vērtības, atkāpšanās un kļūdaini aizvērta nepareizi veidota ievade.

### `TtlCacheTest`— 6 testi

- sitiens pirms TTL, garām pēc;
- precīzs TTL limits;
- ignorēt nepozitīvo TTL;
- konfigurēts izmēra ierobežojums;
- vēlamā elementa pārvietošana, kam beidzies derīguma termiņš;
- 32 virtuālo pavedienu vienlaicīga rakstīšana/lasīšana.
  TTL uzvedība, precīza derīguma termiņa robeža, izmēra kontrole, paraugu ņemšana pēc derīguma termiņa beigām un
  vienlaicīgs spiediens no 32 virtuālajiem pavedieniem.

### `ViesClientBuilderTest`— 3 testi

- var uzbūvēt pilnīgu augstas slodzes konfigurāciju;
- noraidīt nepareizu URL/limit/retry;
- nulles, negatīvā un pārpildes noraidīšana Ilgums.
  Derīga augstas slodzes konfigurācija, kā arī URL, ierobežojums, atkārtots mēģinājums, ilgums un pārpilde
  apstiprināšanu.

## Vietējais HTTP, vienlaicīgums un dzīves cikls / Lokālā integrācija un vienlaicība

`ViesClientHttpTest`— 29 testi nejaušā brīvā atpakaļcilpas portā:
| ID | Pārbaudes gadījums |
|---|---|
| I-01 | 503 mēģinājumi divreiz, tad veiksme / divi 503 mēģinājumi, tad panākumi |
| C-01 | 200 vienas atslēgas asinhronie zvanītāji → tieši 1 HTTP pieprasījums / 200 vienas atslēgas asinhronie zvanītāji → viens pieprasījums |
| C-02 | Aktīvie HTTP pieprasījumi nepārsniedz 4 / aktīvie zvani nepārsniedz 4 |
| C-03 | Asinhrona gaidīšana virs limita tūlītēja `CLIENT_OVERLOADED`|
| C-04 | Nākotnes atcelšana neļauj noplūst, jauda tiek atjaunota |
| C-05 |`close()` nenokļūst strupceļā no asinhronās atzvanīšanas |
| C-06 |`admissionTimeout` ierobežo rindas |
| I-02 | Redis/cache lasīšanas kļūda →`CACHE_ERROR`, null VIES izsaukums |
| C-07 | Atļauja un vienreizējs lidojums | tiek atbrīvoti pirms secīgiem asinhroniem zvaniem
| C-08 | Asinhronā kešatmiņa/aizvērt sacīkstes →`CLIENT_CLOSED`, null HTTP |
| C-09 | Sinhronizēt kešatmiņu/aizvērt sacīksti →`CLIENT_CLOSED`, null HTTP |
| C-10 | Pielāgotais izpildītāja darbs tiek pārtraukts, bet izpildītājs neaizver |
| C-11 | Sinhronizācijas vadītājs un sekotājs iegūst vienādu rezultātu `CLIENT_CLOSED`|
| C-12 | 100 identiski asinhronie sekotāji nepatērē 100 neapstiprinātus slotus |
| C-13 | Pēc izpildītāja noraidīšanas atļauja un lidojuma statuss tiek atjaunots |
| C-14 | Sinhronizācijas vadītājs + asinhronais sekotājs → viens HTTP pieprasījums |
| C-15 | Otrā kešatmiņas pārbaude aizver novecojušo miss sacīksti, nulles HTTP |
| C-16 | Aizverot kešatmiņas rakstīšanas laikā, sinhronizācijas vadītāja/sekotāja rezultāts ir tāds pats |
| C-17 | Apstiprinātā atļauja | tiek atbrīvots arī pirms ķēdes asinhrona zvana ar dažādiem taustiņiem
| C-18 | Rindā ievietots pielāgotā izpildītāja uzdevums faktiski tiek atcelts, aizverot |
| C-19 | Lietotāja atzvanīšanas bloķēšana nebloķē aizvēršanas/dzīves cikla bloķēšanu |
| C-20 | Asinhronais vadītājs + sinhronizācijas sekotājs → viens HTTP pieprasījums |
| C-21 |`maxPendingSyncRequests` nodrošina tūlītēju ierobežotu pretspiedienu |
| C-22 | Fatal async `Error` arī turpina virzīties uz izcilu nākotni un nenoķerto hendleri |

## Ko mēs nepārbaudām pēc noklusējuma? / Kas nav noklusējuma komplektā?

## Live VIES dūmu tests / Live VIES dūmu tests

Tiešraides pakalpojums ir mainīgs un ar ierobežotu ātrumu, tāpēc nevar būt obligāts CI nosacījums.
Manuālais dūmu tests ir ne vairāk kā viens `availability()` un zināms, neslepens vai
vaicājiet nodokļu numuru, kas iegūts no vides mainīgā, concurrency=1 un retries=0
iestatījumu.
Tiešraides VIES ir mainīgs un ar ātruma ierobežojumiem, tāpēc tas nedrīkst nodrošināt normālu CI. Pieteikšanās
dūmu pārbaudei jāveic ne vairāk kā viena pieejamības pārbaude un viena validācija, ar
concurrency=1 un atkārtojumi=0. Nekad nepiesakiet un nereģistrējiet privāto pieprasītāja PVN numurus.

## Slodzes tests / Slodzes tests

Vienmēr stāties pretī vietējai izspēlei vai savam iestudējuma pakalpojumam, nevis publiskajam
pret VIES. Absolūtās prasības ir informatīvas; pareizība, limits, p95/p99 un kļūdu kodi
vārti.
Vienmēr atlasiet vietējo izspēles vai īpašumā esošu iestudēšanas pakalpojumu, nevis publisku VIES. Absolūti
pieprasījumi/sekunde ir informatīva; pareizība, robežas, p95/p99 un kļūdu semantika
ir vārti.
Ieteicamie gadījumi:

- daudzas atšķirīgas atslēgas;
- 100 000 karsto taustiņu zvanītāja uz nelielu taustiņu komplektu / karsto taustiņu spiedziens;
- pārslodze un atjaunošanās virs robežas;
- 200/429/503/taimauts/nepareizi veidota jaukta atbilde;
- kešatmiņas spiediens ap konfigurēto maks.

## Soak and chaos test / Soak and chaos test

30–60 minūšu fiksēta slodze, ierobežota kaudze, JFR, atkārtota aizvēršana/restartēšana, latentums
smaile, savienojuma atiestatīšana, kešatmiņas kļūme un atcelšana. Kaudze, aktīvie pavedieni,
lidojuma laikā/apstiprināto un kontaktligzdu plato skaits; nedrīkst būt strupceļu vai atļauju noplūdes.
Darbiniet 30–60 minūtes ar fiksētu kaudzi, JFR, atkārtotu dzīves ciklu, latentuma pieaugumu,
savienojuma atiestatīšana, kešatmiņas kļūmes un atcelšana. Kaudze/pavedieni/lidojuma laikā/ligzdas
jābūt plato bez strupceļa vai pieļaujamas noplūdes.
JFR piemērs / JFR piemērs:

```bash
JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=target/vies-soak.jfr,settings=profile" ./mvnw test
```

## TIKAI ierosinājums / TIKAI ieteicams

Minimālais cauruļvads:

```bash
./mvnw --batch-mode clean verify
```

Ieteicamā matrica: vismaz JDK 21 LTS; papildu JDK var iestatīt tikai tad
tiek atbalstīti, ja tajās faktiski darbojas tas pats komplekts.
Ieteicamā matrica: vismaz JDK 21 LTS. Pieprasiet papildu JDK atbalstu tikai pēc tam
šajās versijās darbojas tas pats komplekts.

## Regresijas noteikums / Regresijas noteikums

Katrai izlabotajai kļūdai ir jābūt deterministiskam testam, kas ir labojums
iepriekš būtu izgāzies. Sacensību testa prioritātei jābūt fiksatoram/barjerai
sinhronizācija; fiksētā `sleep` var būt tikai īsa aptaujas atkāpšanās, nevis pareizības orākuls.
Katrai izlabotajai kļūdai ir jāsaņem deterministisks tests, kas neizdevās pirms labošanas.
Vienlaicības testiem dodiet priekšroku fiksatoriem/barjerām; fiksētas miega var būt īsas aptaujas
tikai atkāpšanās, nekad pareizības orākuls.
