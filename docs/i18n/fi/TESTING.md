# Suomi (fi) — Testing

> [Kielivalitsin](../../LANGUAGES.md) · Tämä lokalisointi parantaa saavutettavuutta. Jos se poikkeaa kanonisesta englanninkielisestä teknisestä tai oikeudellisesta lähteestä, englanninkielinen lähde määrää. Juuren `LICENSE` ja`NOTICE` ovat oikeudellisesti määrääviä.

## Mitä kutsumme yksikkötestiksi? / Mikä on yksikkötesti?

Kyllä: yksikkötesti tarkistaa yksittäisen luokan tai säännön erikseen, ulkoisesti
ilman verkkoa, tietokantaa ja live VIES:iä. Nopea, deterministinen ja jokaisessa rakenteessa
voi juosta.
Kyllä: yksikkötesti varmistaa yhden luokan tai säännön erikseen, ilman ulkoista verkkoa,
tietokanta tai live VIES. Se on nopea, deterministinen ja sopii jokaiseen rakentamiseen.
Tämä moduuli vaatii myös **paikallisia integraatio- ja kilpailutestejä**
myös siksi, että aikakatkaisu-, uudelleenyritys-, yksittäislennon-, peruutus- ja `close()`-käyttäytymiset ovat enemmän
se käy ilmi komponentin yhteistyöstä. He käyttävät silmukan valepalvelinta, ei
soita EU-palveluun.
Tämä moduuli tarvitsee myös **paikallisia integraatio- ja samanaikaisuustestejä**, koska aikakatkaisu,
uudelleenyritys, yksi lento, peruutus ja sulkeminen sisältävät useita osia. Nämä
testit käyttävät silmukkapalvelinta eivätkä koskaan soita EU:n julkiseen palveluun.

## Pikakomennot / Pikakomennot

Täysi deterministinen testipaketti / Täysi deterministinen testipaketti:

```bash
./mvnw test
```

Vain yksikkötestit:

```bash
./mvnw -Dtest=VatFormatTest,ViesRequesterTest,ViesResponseMappingTest,ViesErrorTest,ViesAvailabilityTest,ViesClientBuilderTest,MiniJsonTest,TtlCacheTest test
```

Vain paikalliset HTTP/samanaikaisuustestit / Paikallinen HTTP ja vain samanaikaisuus:

```bash
./mvnw -Dtest=ViesClientHttpTest test
```

Puhdas vahvistus JAR/Javadoc-sukupolven avulla / Puhdista varmennus artefakteilla:

```bash
./mvnw clean verify
./mvnw package
```

Yksi erityinen testi / yksi testimenetelmä:

```bash
./mvnw -Dtest=ViesClientHttpTest#cancellationDoesNotLeakAsyncCapacity test
```

## Nykyinen kattavuus / Nykyinen kattavuus

Nykyinen deterministinen paketti sisältää **73 testiä**:

- **44 yksikkökoe** kahdeksassa luokassa;
- **29 paikallista HTTP-/integraatio-/samanaikaisuustestiä**;
- nolla pakollisia ulkoisia verkkopuheluita.
  Deterministinen sarja sisältää **73 testiä**: 44 yksikkötestiä, 29 paikallista
  HTTP/integraatio/samanaikaisuustestit ja nolla pakollisia ulkopuheluita.

## Yksikkötestiluettelo / Yksikkötestiluettelo

### `VatFormatTest`— 8 testiä

| ID       | Unkarin maali                                             | Englanninkielinen tarkoitus                        |
| -------- | --------------------------------------------------------- | -------------------------------------------------- |
| U-FMT-01 | Kokonaisveronumeron normalisointi                         | Normalisoi koko ALV-tunniste                       |
| U-FMT-02 | Poista välilyönti/piste/yhdysmerkki, kirjain isolla       | Nauhaerottimet ja isot kirjaimet                   |
| U-FMT-03 | `GR`→`EL`-kartoitus                                       | Kartta Kreikka`GR`kohteeseen VIES`EL`              |
| U-FMT-04 | Hylkää null, tyhjä, tuntematon maa ja virheellinen pituus | Hylkää tyhjä/tyhjä/tuntematon/huono pituus         |
| U-FMT-05 | Edustavan maan lomakkeet                                  | Edustavat maamuodot                                |
| U-FMT-06 | Erillinen maakoodi+numero API, liitteenä etuliite         | Yhdistä API liitettyyn etuliitteeseen              |
| U-FMT-07 | Tuetut maakoodit                                          | Tuetun maan sarja                                  |
| U-FMT-08 | Kaikissa 28 tuetussa maassa on vähintään yksi muoto       | Vähintään yksi muoto kaikille 28 tuetulle koodille |

### `ViesRequesterTest`— 4 testiä

| ID       | Unkarin maali                                       | Englanninkielinen tarkoitus                          |
| -------- | --------------------------------------------------- | ---------------------------------------------------- |
| U-REQ-01 | Täytä omalta veronumeron hakijalta                  | Luo pyynnön esittäjä täydestä ALV:sta                |
| U-REQ-02 | Kreikkalaisen pyytäjän kanonisointi                 | Kanonisoi kreikkalainen pyyntö                       |
| U-REQ-03 | Parillinen konstruktori, johon on liitetty etuliite | Yhdistä konstruktori vastaavalla etuliitteellä       |
| U-REQ-04 | Viallisen pyynnön esittäjän välitön hylkääminen     | Epäonnistui nopeasti virheellisen pyynnön yhteydessä |

### `ViesResponseMappingTest`— 11 testiä

| ID       | Unkarin maali                                                      | Englanninkielinen tarkoitus                         |
| -------- | ------------------------------------------------------------------ | --------------------------------------------------- |
| U-MAP-01 | GET-tyylinen `Valid` kaikilla kentillä                             | Kartta GET-tyylinen Kelvollinen vastaus             |
| U-MAP-02 | POST-tyylinen `Valid`,`---` paikkamerkki                           | Kartta POST-tyylinen Kelvollinen ja paikkamerkit    |
| U-MAP-03 | Aito `Invalid`                                                     | Kartan arvovaltainen Virheellinen                   |
| U-MAP-04 | Ohimenevä virhe →`Unavailable`                                     | Kartta ohimenevä virhe Ei saatavilla                |
| U-MAP-05 | Syöttövirhe →`MalformedInput`                                      | Kartta VIES -syöttövirhe                            |
| U-MAP-06 | Hylkää ei-objekti JSON                                             | Hylkää ei-objekti JSON                              |
| U-MAP-07 | Puuttuva looginen arvo ei voi olla `Invalid`                       | Puuttuva boolean ei koskaan muutu virheelliseksi    |
| U-MAP-08 | Merkkijonon looginen arvo ei voi olla `Invalid`                    | Ei-boolen kelpoisuus hylätty                        |
| U-MAP-09 | Emme löydä paikallista aikaa puuttuvan tarkastuspäivämäärän osalta | Älä koskaan keksi puuttuvaa tarkastuksen aikaleimaa |
| U-MAP-10 | Hylkää virheellinen tarkastuspäivä                                 | Hylkää virheellinen tarkastuksen aikaleima          |
| U-MAP-11 | Siirtymäaikaleiman oikea muunnos UTC                               | Jäsennä siirtymäaikaleima UTC:ksi                   |

### `ViesErrorTest`— 6 testiä

| ID       | Unkarin maali                                             | Englanninkielinen tarkoitus                                        |
| -------- | --------------------------------------------------------- | ------------------------------------------------------------------ |
| U-ERR-01 | Unkarin+Englannin verkkoviesti                            | Kaksikielinen verkkovirhe                                          |
| U-ERR-02 | Syöttövirhettä ei voi yrittää uudelleen                   | Syöttövirhe on pysyvä                                              |
| U-ERR-03 | HTTP 408/429/5xx yritä luokitusta uudelleen               | HTTP-uudelleenyritysluokitus                                       |
| U-ERR-04 | `Valid`/`Invalid` ei ole virhe                            | Päätökset eivät paljasta virhettä                                  |
| U-ERR-05 | Kaikki julkiset koodit HU+EN ja vakaa uudelleenyritysarvo | Jokaisella julkisella koodilla on HU+EN ja uudelleenyrityskäytäntö |
| U-ERR-06 | Säilytä tuntematon koodi ilman uudelleenyritystä          | Säilytä tuntematon koodi ilman uudelleenyritystä                   |

### `ViesAvailabilityTest`— 2 testiä

- suojakopio ja syöttökartan muuttumattomuus;
- tyhjän jäsenvaltion kartan välitön hylkääminen.
  Puolustava muuttumaton tilannekuvakopio ja nollakartan rakentajan validointi.

### `MiniJsonTest`— 4 testiä

- tyypillinen VIES-dokumentti ja Unicode-pakokoodi;
- sisäkkäinen objekti/luettelo/skalaari/luku/nolla;
- JSON pakenee;
- virheellisen, katkaistun ja perässä olevan syötteen hylkääminen.
  Tyypillinen VIES JSON, sisäkkäiset/skalaariarvot, erot ja virheellisesti muotoiltu virheellinen syöte.

### `TtlCacheTest`— 6 testiä

- osuma ennen TTL:ää, miss jälkeen;
- tarkka TTL-raja;
- ohita ei-positiivinen TTL;
- määritetty kokorajoitus;
- vanhentuneen elementin edullinen siirto;
- 32 virtuaalisen säikeen samanaikainen kirjoitus/luku.
  TTL-käyttäytyminen, tarkka vanhenemisraja, koon valvonta, vanhentunut-ensimmäinen näytteenotto ja
  samanaikainen paine 32 virtuaalisäikeestä.

### `ViesClientBuilderTest`— 3 testiä

- täydellinen suuren kuormituksen kokoonpano voidaan rakentaa;
- hylkää väärä URL-osoite/rajoitus/yritä uudelleen;
- nollan, negatiivisen ja ylivuodon hylkääminen Kesto.
  Kelvollinen suuren kuormituksen määritys sekä URL-osoite, raja, uudelleenyritys, kesto ja ylivuoto
  validointi.

## Paikallinen HTTP, samanaikaisuus ja elinkaari / Paikallinen integraatio ja samanaikaisuus

`ViesClientHttpTest`— 29 testiä satunnaisessa vapaassa loopback-portissa:
| ID | Testitapaus |
|---|---|
| I-01 | 503 uudelleenyritystä kahdesti, sitten onnistuminen / kaksi 503 uudelleenyritystä sitten menestys |
| C-01 | 200 saman näppäimen asynkronista soittajaa → täsmälleen 1 HTTP-pyyntö / 200 saman näppäimen asynkronoitua soittajaa → yksi pyyntö |
| C-02 | Aktiiviset HTTP-pyynnöt eivät ylitä 4:n rajaa / aktiiviset puhelut pysyvät 4 |
| C-03 | Async odottaa rajan yläpuolella välittömästi `CLIENT_OVERLOADED`|
| C-04 | Tuleva peruutus ei vuoda, kapasiteetti palautetaan |
| C-05 |`close()` ei lukkiudu asynkronisesta takaisinkutsusta |
| C-06 |`admissionTimeout` rajoittaa jonotusta |
| I-02 | Redis/cache lukuvirhe →`CACHE_ERROR`, nolla VIES-kutsu |
| C-07 | Lupa ja yksi lento | vapautetaan ennen peräkkäisiä asynk.kutsuja
| C-08 | Async-välimuisti/sulje kilpailu →`CLIENT_CLOSED`, null HTTP |
| C-09 | Synkronoi välimuisti/sulje kilpailu →`CLIENT_CLOSED`, null HTTP |
| C-10 | Mukautettu suoritintyö keskeytetään, mutta suorittaja ei sulje |
| C-11 | Synkronoinnin johtaja ja seuraaja saavat saman tuloksen `CLIENT_CLOSED`|
| C-12 | 100 identtistä asynkronista seuraajaa eivät käytä 100 odottavaa paikkaa |
| C-13 | Toimeksiantajan hylkäämisen jälkeen lupa ja lennon aikana tila palautetaan |
| C-14 | Synkronointijohtaja + asynkroninen seuraaja → yksi HTTP-pyyntö |
| C-15 | Toinen välimuistin tarkistus sulkee vanhentuneen epäonnistumisen kilpailun, nolla HTTP |
| C-16 | Suljettaessa välimuistin kirjoittamisen aikana synkronoinnin johtajan/seuraajan tulos on sama |
| C-17 | Odottava lupa | vapautetaan myös ennen ketjutettua asynk.puhelua eri näppäimillä
| C-18 | Jonossa oleva mukautetun suorittimen tehtävä itse asiassa peruutetaan sulkemalla |
| C-19 | Käyttäjän takaisinsoittojen estäminen ei estä sulkemisen/elinkaaren lukitusta |
| C-20 | Async leader + synkronointiseuraaja → yksi HTTP-pyyntö |
| C-21 |`maxPendingSyncRequests` antaa välittömän rajoitetun vastapaineen |
| C-22 | Fatal async `Error` jatkaa myös kohti poikkeuksellista tulevaisuutta ja kiinnittymätöntä käsittelijää |

## Mitä emme testaa oletusarvoisesti? / Mitä oletuspaketissa ei ole?

## Live VIES -savutesti / Live VIES -savutesti

Live-palvelu on vaihteleva ja nopeusrajoitettu, joten pakollista CI-ehtoa ei voi olla.
Manuaalinen savutesti on enintään yksi `availability()` ja tunnettu, ei-salainen tai
kysy ympäristömuuttujasta saatu veronumero, concurrency=1 ja recries=0
asetusta.
Live VIES on muuttuva ja nopeusrajoitettu, joten se ei saa portoida normaalia CI:tä. Osallistuminen
savutestin tulee suorittaa enintään yksi käytettävyystarkistus ja yksi validointi
concurrency=1 ja yrittää uudelleen=0. Älä koskaan sitoudu tai kirjaa yksityisiä pyytäjien ALV-numeroita.

## Kuormitustesti / kuormitustesti

Aja aina paikallista pilkkaa tai omaa lavastuspalveluasi vastaan, älä koskaan julkista
VIESiä vastaan. Absoluuttiset vaatimukset ovat informatiivisia; oikeellisuus, raja, p95/p99 ja virhekoodit
portit.
Kohdista aina paikalliseen pilaan tai omistamaan lavastuspalveluun, älä koskaan julkiseen VIES-palveluun. Ehdoton
pyynnöt/sekunti on informatiivinen; oikeellisuus, rajat, p95/p99 ja virhesemantiikka
ovat portit.
Suositellut tapaukset:

- useita erillisiä avaimia;
- 100 000 pikanäppäinsoittajaa pienellä näppäinsarjalla / pikanäppäinten isku;
- ylikuormitus ja palautuminen rajan yli;
- 200/429/503/aikakatkaisu/virheellinen sekoitettu vastaus;
- välimuistin paine konfiguroidun max.

## Soak and chaos -testi / Soak and chaos -testi

30-60 minuutin kiinteä kuormitus, rajoitettu kasa, JFR, toistuva sulkeminen/uudelleenkäynnistys, latenssi
piikki, yhteyden nollaus, välimuistivirhe ja peruutus. Kasa, aktiiviset säikeet,
lennon aikana / vireillä olevien ja sockets-tasangon määrä; ei saa olla lukkiutumia tai sallia vuotoja.
Ajo 30–60 minuuttia kiinteällä kasalla, JFR, toistuva elinkaari, latenssipiikit,
yhteyden nollaukset, välimuistin viat ja peruuttaminen. Kasa/langat/lennon aikana/pistorasiat
pitää tasaantua ilman lukkiutumista tai sallia vuotoja.
JFR-esimerkki / JFR-esimerkki:

```bash
JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=target/vies-soak.jfr,settings=profile" ./mvnw test
```

## VAIN ehdotus / VAIN suositeltava

Vähimmäisputki:

```bash
./mvnw --batch-mode clean verify
```

Suositeltu matriisi: vähintään JDK 21 LTS; lisää JDK:ita voidaan asettaa vasta silloin
tuettu, jos sama sarja todella toimii niissä.
Suositeltu matriisi: vähintään JDK 21 LTS. Pyydä ylimääräistä JDK-tukea vasta sen jälkeen
käyttää samaa sarjaa näissä versioissa.

## Regressio-sääntö / Regressio-sääntö

Jokaisella korjatulla bugilla tulisi olla deterministinen testi, joka on korjaus
olisi epäonnistunut aiemmin. Salvan/esteen tulee olla kilpailutestin prioriteetti
synkronointi; kiinteä `sleep` voi olla vain lyhyt kyselyn peruutus, ei oikeellisuuden oraakkeli.
Jokaisen korjatun virheen on saatava deterministinen testi, joka epäonnistui ennen korjausta.
Anna mieluummin salvat/esteet samanaikaisuustesteissä; kiinteät unet voivat olla lyhyitä kyselyitä
vain backoff, ei koskaan oikeellisuuden oraakkeli.
