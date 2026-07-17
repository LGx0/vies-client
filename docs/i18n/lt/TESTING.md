# Lietuvių (lt) — Testing

> [Kalbų pasirinkimas](../../LANGUAGES.md) · Ši lokalizacija skirta prieinamumui. Esant neatitikimui, pirmenybę turi kanoninis angliškas techninis ar teisinis šaltinis. Šakniniai `LICENSE` ir`NOTICE` lieka teisiškai privalomi.

## Ką vadiname vieneto testu? / Kas yra vienetinis testas?

Taip: vieneto testas tikrina vieną klasę arba taisyklę atskirai, išorinę
be tinklo, duomenų bazės ir tiesioginio VIES. Greitas, deterministinis ir visose konstrukcijose
gali bėgti.
Taip: vieneto testas patikrina vieną klasę arba taisyklę atskirai, be išorinio tinklo,
duomenų bazėje arba tiesioginiame VIES. Jis yra greitas, deterministinis ir tinka bet kokiai konstrukcijai.
Šiam moduliui taip pat reikia **vietinių integracijos ir konkurencijos testų**
Taip pat dėl to, kad laikas pasibaigia, bandoma pakartoti, vienas skrydis, atšaukti ir `close()` yra daugiau
tai matyti iš komponento bendradarbiavimo. Jie naudoja „loopback“ imitacinį serverį, Nr
paskambinti į ES tarnybą.
Šiam moduliui taip pat reikia **vietinės integracijos ir lygiagretumo testų**, nes skirtasis laikas,
Bandymas iš naujo, vienas skrydis, atšaukimas ir išjungimas apima kelis komponentus. Šios
bandymai naudoja „loopback“ imitacinį serverį ir niekada neskambina į viešąją ES tarnybą.

## Greitos komandos / Greitos komandos

Visas deterministinių bandymų paketas / Visas deterministinis rinkinys:

```bash
./mvnw test
```

Tik vienetų bandymai:

```bash
./mvnw -Dtest=VatFormatTest,ViesRequesterTest,ViesResponseMappingTest,ViesErrorTest,ViesAvailabilityTest,ViesClientBuilderTest,MiniJsonTest,TtlCacheTest test
```

Tik vietiniai HTTP / lygiagretumo testai / vietinis HTTP ir tik lygiagretumas:

```bash
./mvnw -Dtest=ViesClientHttpTest test
```

Švarus patvirtinimas naudojant JAR / Javadoc kartos / Išvalytas patikrinimas su artefaktais:

```bash
./mvnw clean verify
./mvnw package
```

Vienas konkretus bandymas / vienas bandymo metodas:

```bash
./mvnw -Dtest=ViesClientHttpTest#cancellationDoesNotLeakAsyncCapacity test
```

## Dabartinė aprėptis / Dabartinė aprėptis

Dabartiniame deterministiniame pakete yra **73 testai**:

- **44 vienetų testas** aštuoniose klasėse;
- **29 vietiniai HTTP / integracijos / lygiagretumo testai**;
- nulis privalomų išorinio tinklo skambučių.
  Deterministiniame rinkinyje yra **73 testai**: 44 vienetiniai testai, 29 vietiniai
  HTTP / integracijos / lygiagretumo testai ir nulis privalomų išorinių skambučių.

## Vienetų bandymų katalogas / Vienetų bandymų katalogas

### `VatFormatTest`— 8 testai

| ID       | Vengrijos įvartis                                             | Angliška paskirtis                                 |
| -------- | ------------------------------------------------------------- | -------------------------------------------------- |
| U-FMT-01 | Bendro mokesčio numerio normalizavimas                        | Normalizuoti visą PVM identifikatorių              |
| U-FMT-02 | Pašalinti tarpą/tašką/brūkšnelį, rašyti didžiosiomis raidėmis | Juostelių separatoriai ir didžioji raidė           |
| U-FMT-03 | `GR`→`EL`kartografavimas                                      | Graikijos žemėlapis`GR`į VIES`EL`                  |
| U-FMT-04 | Atmesti null, tuščia, nežinoma šalis ir neteisingas ilgis     | Atmesti nulinis/tuščias/nežinomas/netinkamas ilgis |
| U-FMT-05 | Reprezentatyvios šalies formos                                | Reprezentatyvūs šalių formatai                     |
| U-FMT-06 | Atskiras šalies kodas + numeris API, pridedamas priešdėlis    | Suporuokite API su pridėtu priešdėliu              |
| U-FMT-07 | Palaikomi šalių kodai                                         | Palaikomos šalies rinkinys                         |
| U-FMT-08 | Visose 28 palaikomose šalyse yra bent viena                   | Bent viena forma visiems 28 palaikomiems kodams    |

### `ViesRequesterTest`— 4 testai

| ID       | Vengrijos įvartis                               | Angliška paskirtis                                 |
| -------- | ----------------------------------------------- | -------------------------------------------------- |
| U-REQ-01 | Užpildyti iš savo mokesčių numerio prašytojo    | Sukurti užklausą iš viso PVM                       |
| U-REQ-02 | Graikijos prašytojo kanonizavimas               | Kanonizuoti graikų kalbos užklausą                 |
| U-REQ-03 | Suporuotas konstruktorius su pridėtu priešdėliu | Suporuokite konstruktorių su atitinkamu priešdėliu |
| U-REQ-04 | Neatidėliotinas klaidingo prašytojo atmetimas   | Greitai nepavyksta dėl netinkamo prašytojo         |

### `ViesResponseMappingTest`— 11 testų

| ID       | Vengrijos įvartis                                   | Angliška paskirtis                                        |
| -------- | --------------------------------------------------- | --------------------------------------------------------- |
| U-MAP-01 | GET stiliaus `Valid` su visais laukais              | Žemėlapis GET stiliaus Galiojantis atsakymas              |
| U-MAP-02 | POST stiliaus `Valid`,`---` rezervuota vieta        | Žemėlapis POST stiliaus Galiojantis ir rezervuotos vietos |
| U-MAP-03 | Autentiškas `Invalid`                               | Žemėlapis autoritetingas Neteisingas                      |
| U-MAP-04 | Laikinoji klaida →`Unavailable`                     | Žemėlapio laikina klaida į Nepasiekiamas                  |
| U-MAP-05 | Įvesties klaida →`MalformedInput`                   | Žemėlapio VIES įvesties klaida                            |
| U-MAP-06 | Atmesti neobjektinį JSON                            | Atmesti neobjektinį JSON                                  |
| U-MAP-07 | Trūkstamos loginės reikšmės negali būti `Invalid`   | Trūksta loginės reikšmės niekada netampa Neteisinga       |
| U-MAP-08 | Stygos loginis dydis negali būti `Invalid`          | Ne loginis galiojimas atmestas                            |
| U-MAP-09 | Nerandame vietos laiko trūkstamai audito datai      | Niekada nesugalvokite trūkstamos audito laiko žymos       |
| U-MAP-10 | Atmesti neteisingą audito datą                      | Atmesti netinkamą audito laiko žymą                       |
| U-MAP-11 | Teisingas poslinkio laiko žymos konvertavimas į UTC | Išanalizuoti poslinkio laiko žymą į UTC                   |

### `ViesErrorTest`— 6 testai

| ID       | Vengrijos įvartis                                              | Angliška paskirtis                                               |
| -------- | -------------------------------------------------------------- | ---------------------------------------------------------------- |
| U-ERR-01 | vengrų+anglų tinklo žinutė                                     | Dvikalbio tinklo klaida                                          |
| U-ERR-02 | Įvesties klaidos negalima bandyti iš naujo                     | Įvesties klaida yra nuolatinė                                    |
| U-ERR-03 | HTTP 408/429/5xx pakartokite klasifikaciją                     | HTTP pakartotinio bandymo klasifikacija                          |
| U-ERR-04 | `Valid`/`Invalid` nėra klaida                                  | Sprendimai neatskleidžia klaidų                                  |
| U-ERR-05 | Visi vieši kodai HU+EN ir stabili pakartotinio bandymo reikšmė | Kiekvienas viešas kodas turi HU+EN ir bandymo dar kartą politiką |
| U-ERR-06 | Išsaugoti nežinomą kodą nebandydami iš naujo                   | Išsaugoti nežinomą kodą nebandydami iš naujo                     |

### `ViesAvailabilityTest`— 2 bandymai

- apsaugos kopija ir įvesties žemėlapio nekintamumas;
- nedelsiant atmesti nulinės valstybės narės žemėlapį.
  Apsauginė nekintama momentinės kopijos kopija ir nulinio žemėlapio konstruktoriaus patvirtinimas.

### `MiniJsonTest`— 4 testai

- tipinis VIES dokumentas ir Unicode pabėgimas;
- įdėtas objektas/sąrašas/skaliaras/skaičius/nuliukas;
- JSON pabėga;
- neteisingos, sutrumpintos ir vėlesnės įvesties atmetimas.
  Įprasta VIES JSON, įdėtos / skaliarinės reikšmės, pabėgimai ir nesėkmingai uždaryta netinkamai suformuota įvestis.

### `TtlCacheTest`— 6 testai

- pataikyti prieš TTL, praleisti po;
- tiksli TTL riba;
- ignoruoti neteigiamą TTL;
- sukonfigūruotas dydžio limitas;
- pageidaujamas elemento, kurio galiojimo laikas pasibaigęs, poslinkis;
- 32 virtualios gijos vienu metu rašymas / skaitymas.
  TTL elgesys, tiksli galiojimo pabaigos riba, dydžio kontrolė, mėginių ėmimas, kurio galiojimo laikas pasibaigęs, ir
  vienu metu veikiantis slėgis iš 32 virtualių gijų.

### `ViesClientBuilderTest`– 3 bandymai

- galima sukurti pilną didelės apkrovos konfigūraciją;
- atmesti neteisingą URL / limitą / bandyti iš naujo;
- nulio, neigiamo ir perpildymo atmetimas Trukmė.
  Tinkama didelės apkrovos konfigūracija ir URL, apribojimas, bandymas pakartotinai, trukmė ir perpildymas
  patvirtinimas.

## Vietinis HTTP, lygiagretumas ir gyvavimo ciklas / Vietinė integracija ir lygiagretumas

`ViesClientHttpTest`– 29 bandymai atsitiktiniu laisvu atgalinio ryšio prievadu:
| ID | Bandomasis atvejis |
|---|---|
| I-01 | 503 bandymai du kartus, tada sėkmė / du 503 bandymai, tada sėkmė |
| C-01 | 200 asinchroninių skambintojų tuo pačiu klavišu → lygiai 1 HTTP užklausa / 200 asinchroninių skambintojų tuo pačiu klavišu → viena užklausa |
| C-02 | Aktyvios HTTP užklausos neviršija 4 ribos / aktyvūs skambučiai lieka per 4 |
| C-03 | Laukiama asinchronizavimo, viršijančio ribą, nedelsiant `CLIENT_OVERLOADED`|
| C-04 | Būsimas atšaukimas nenuteka, pajėgumai atkuriami |
| C-05 |`close()` neužblokuoja asinchroninio atgalinio skambinimo |
| C-06 |`admissionTimeout` riboja eilę |
| I-02 | Redis/cache skaitymo klaida →`CACHE_ERROR`, null VIES skambutis |
| C-07 | Leidimas ir vienkartinis skrydis | išleidžiami prieš nuoseklius asinchroninius skambučius
| C-08 | Asinchroninė talpykla / uždaryti lenktynes ​​→`CLIENT_CLOSED`, null HTTP |
| C-09 | Sinchronizuoti talpyklą / uždaryti lenktynes ​​→`CLIENT_CLOSED`, null HTTP |
| C-10 | Pasirinktinis vykdytojo darbas nutraukiamas, bet vykdytojas neuždaro |
| C-11 | Sinchronizavimo vadovas ir sekėjas gauna tą patį rezultatą `CLIENT_CLOSED`|
| C-12 | 100 identiškų asinchroninių sekėjų nesunaudoja 100 laukiančių laiko tarpsnių |
| C-13 | Vykdytojui atmetus leidimą ir skrydžio būsena atkuriama |
| C-14 | Sinchronizavimo vadovas + asinchroninis stebėtojas → viena HTTP užklausa |
| C-15 | Antrasis talpyklos patikrinimas uždaro pasenusios praleidimo lenktynes, nulinis HTTP |
| C-16 | Uždarius talpyklą rašant, sinchronizavimo lyderio / sekėjo rezultatas yra toks pat |
| C-17 | Laukiamas leidimas | taip pat išleidžiamas prieš grandininį asinchroninį skambutį su skirtingais klavišais
| C-18 | Eilėje esanti pasirinktinio vykdytojo užduotis iš tikrųjų atšaukiama uždarant |
| C-19 | Blokuojant naudotojo atgalinį skambutį, uždarymo / gyvavimo ciklo užraktas neužblokuojamas |
| C-20 | Asinchronizavimo lyderis + sinchronizavimo stebėtojas → viena HTTP užklausa |
| C-21 |`maxPendingSyncRequests` suteikia momentinį ribotą priešslėgį |
| C-22 | Fatal async `Error` taip pat toliau siekia išskirtinės ateities ir nepagauto prižiūrėtojo |

## Ko mes netikriname pagal numatytuosius nustatymus? / Ko nėra numatytame rinkinyje?

## Tiesioginis VIES dūmų testas / Live VIES dūmų testas

Tiesioginė paslauga yra kintama ir ribojama, todėl negali būti privalomos CI sąlygos.
Rankinis dūmų testas yra daugiausia vienas `availability()` ir žinomas, neslaptas arba
užklausa mokesčių numerio, gauto iš aplinkos kintamojo, concurrency=1 ir bandomasi=0
nustatymą.
„Live VIES“ yra kintama ir ribojama sparta, todėl ji neturi perjungti įprasto CI. Pasirinkimas
dūmų testas turėtų atlikti ne daugiau kaip vieną prieinamumo patikrinimą ir vieną patvirtinimą
concurrency=1 ir pakartojimai=0. Niekada neįpareigokite ir neregistruokite privačių prašytojų PVM numerių.

## Apkrovos testas / Apkrovos testas

Visada kovokite su vietiniu pasityčiojimu ar savo pastatymo paslauga, o ne vieša
prieš VIES. Absoliutūs reikalavimai yra informatyvūs; teisingumas, riba, p95/p99 ir klaidų kodai
vartai.
Visada taikykite į vietinę padirbtą arba priklausančią rengimo paslaugą, niekada neviešinkite VIES. Absoliutus
užklausos/sekundė yra informacinė; teisingumas, ribos, p95/p99 ir klaidų semantika
yra vartai.
Rekomenduojami atvejai:

- daug skirtingų raktų;
- 100 000 skambinančiojo greituoju klavišu nedideliu klavišų rinkiniu / greitojo klavišo spaudimu;
- perkrova ir atsistatymas virš ribos;
- 200/429/503/timeout/netinkamai suformuotas mišrus atsakymas;
- talpyklos slėgis aplink sukonfigūruotą maks.

## Soak and chaos test / Soak and chaos test

30–60 minučių fiksuota apkrova, ribota krūva, JFR, pakartotinis uždarymas / paleidimas iš naujo, delsa
smaigalys, ryšio atstatymas, talpyklos gedimas ir atšaukimas. Krūva, aktyvios gijos,
skrydžio metu / laukiančių ir lizdų plokščiakalnio skaičius; neturėtų būti aklavietės ar leidimų nutekėjimo.
Veikia 30–60 minučių su fiksuota krūva, JFR, pasikartojančiu gyvavimo ciklu, delsos šuoliais,
ryšio atstatymas, talpyklos gedimai ir atšaukimas. Krūva / siūlai / skrydžio metu / lizdai
turi būti plokščiakalnis be aklavietės arba leisti nutekėti.
JFR pavyzdys / JFR pavyzdys:

```bash
JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=target/vies-soak.jfr,settings=profile" ./mvnw test
```

## ONLY pasiūlymas / TIK rekomenduojama

Minimalus dujotiekis:

```bash
./mvnw --batch-mode clean verify
```

Rekomenduojama matrica: ne mažiau JDK 21 LTS; papildomų JDK galima nustatyti tik tada
palaikoma, jei juose iš tikrųjų veikia tas pats rinkinys.
Rekomenduojama matrica: ne mažiau JDK 21 LTS. Reikalaukite papildomo JDK palaikymo tik po to
tose versijose veikia tas pats rinkinys.

## Regresijos taisyklė / Regresijos taisyklė

Kiekviena ištaisyta klaida turi turėti deterministinį testą, kuris yra pataisytas
anksčiau būtų nepavykę. Užraktas/barjeras turėtų būti pirmenybė atliekant varžybų testą
sinchronizavimas; fiksuotas `sleep` gali būti tik trumpas apklausos atsitraukimas, o ne teisingumo orakulas.
Kiekviena ištaisyta klaida turi būti atlikta deterministiniu testu, kuris nepavyko prieš pataisymą.
Pirmenybę teikite skląsčiams / kliūtims lygiagretumo testams; fiksuotas miegas gali būti trumpas
tik atsitraukimas, niekada teisingumo orakulas.
