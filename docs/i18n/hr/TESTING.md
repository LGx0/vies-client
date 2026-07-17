# Hrvatski (hr) — Testiranje

> [Svi jezici](../../LANGUAGES.md) · Informativni prijevod. U slučaju razlike mjerodavan je kanonski engleski tehnički ili pravni izvor. Samo su korijenski `LICENSE` i `NOTICE` pravno mjerodavni; prijevod ih ne zamjenjuje.

## Što nazivamo jediničnim testom?

Da: jedinični test provjerava jednu klasu ili pravilo u izolaciji, eksterno
bez mreže, baze podataka i VIES-a uživo. Brz, determinističan i u svakoj gradnji
može trčati.

Da: jedinični test provjerava jednu klasu ili pravilo u izolaciji, bez vanjske mreže,
bazu podataka ili VIES uživo. Brz je, determinističan i prikladan za svaku gradnju.

Ovaj modul također zahtijeva **lokalne integracije i testove natjecanja**
također zato što su ponašanja timeout, ponovni pokuÅ¡aj, single-flight i otkazivanje i `close()` više
vidljivo je iz suradnje komponente. Oni koriste loopback lažni poslužitelj, ne
nazovite EU službu.

Ovaj modul također treba **lokalne integracije i testove paralelnosti**, jer timeout,
ponovni pokušaj, jednokratni let, otkazivanje i isključivanje uključuju više komponenti. ove
testovi koriste loopback lažni poslužitelj i nikada ne pozivaju javnu EU uslugu.

## Brze naredbe

Potpuni deterministički testni paket / Potpuni deterministički paket:

```bash
./mvnw test
```

Samo jedinični testovi:

```bash
./mvnw -Dtest=VatFormatTest,ViesRequesterTest,ViesResponseMappingTest,ViesErrorTest,ViesAvailabilityTest,ViesClientBuilderTest,MiniJsonTest,TtlCacheTest test
```

Samo lokalni HTTP/testovi istovremenosti / Samo lokalni HTTP i istovremenost:

```bash
./mvnw -Dtest=ViesClientHttpTest test
```

Čista provjera s JAR/Javadoc generacijom / Čista provjera s artefaktima:

```bash
./mvnw clean verify
./mvnw package
```

Jedan specifični test / jedna metoda ispitivanja:

```bash
./mvnw -Dtest=ViesClientHttpTest#cancellationDoesNotLeakAsyncCapacity test
```

## Trenutna pokrivenost

Trenutačni deterministički paket sadrži **73 testova**:

- **44 jedinična testa** u osam razreda;
- **29 lokalna testa HTTP/integracije/konkurentnosti**;
- nula obveznih vanjskih mrežnih poziva.

Deterministički paket sadrži **73 testova**: 44 jedinična testa, 29 lokalna
HTTP/integracija/testovi paralelnosti i nula obveznih vanjskih poziva.

## Katalog jediničnih testova

### `VatFormatTest`— 8 testova

| ID       | Mađarski gol                                            | engleska svrha                                |
| -------- | ------------------------------------------------------- | --------------------------------------------- |
| U-FMT-01 | Normalizacija ukupnog poreznog broja                    | Normalizirati puni identifikator PDV-a        |
| U-FMT-02 | Uklonite razmak/točku/crticu, veliko slovo              | Razdjelnici trake i velika slova              |
| U-FMT-03 | `GR`→`EL` preslikavanje                                 | Karta Grčke`GR`do VIES`EL`                    |
| U-FMT-04 | Odbaci nulu, prazno, nepoznatu zemlju i netočnu duljinu | Odbaci null/prazno/nepoznato/lošu duljinu     |
| U-FMT-05 | Obrasci reprezentativne zemlje                          | Reprezentativni formati zemalja               |
| U-FMT-06 | Odvojeni kod države+broj API, priloženi prefiks         | Upari API s priloženim prefiksom              |
| U-FMT-07 | Podržani kodovi zemalja                                 | Skup podržanih zemalja                        |
| U-FMT-08 | Svih 28 podržanih zemalja ima barem jedan oblik         | Barem jedan oblik za svih 28 podržanih kodova |

### `ViesRequesterTest`— 4 testa

| ID       | Mađarski gol                                                | engleska svrha                                 |
| -------- | ----------------------------------------------------------- | ---------------------------------------------- |
| U-REQ-01 | Kompletno od podnositelja zahtjeva za vlastiti porezni broj | Kreirajte podnositelja zahtjeva iz punog PDV-a |
| U-REQ-02 | Kanonizacija grčkog molitelja                               | Kanoniziraj grčkog podnositelja zahtjeva       |
| U-REQ-03 | Upareni konstruktor s pridruženim prefiksom                 | Konstruktor parova s ​​odgovarajućim prefiksom |
| U-REQ-04 | Trenutačno odbijanje neispravnog podnositelja               | Brzi neuspjeh na nevažećem zahtjevatelju       |

### `ViesResponseMappingTest`— 11 testova

| ID         | Mađarski gol                                              | engleska svrha                                                     |
| ---------- | --------------------------------------------------------- | ------------------------------------------------------------------ |
| U-MAP-01   | GET stil `Valid` sa svim poljima                          | Karta GET stil Valjani odgovor                                     |
| U-MAP-02   | POST-stílusú `Valid`,`---` rezervirano mjesto             | Karta POST stil Valid i rezervirana mjesta                         |
| U-MAP-03   | Hiteles `Invalid`                                         | Karta mjerodavna Nevažeće                                          |
| U-MAP-04   | Átmeneti hiba →`Unavailable`                              | Preslikaj prolaznu pogrešku na Nedostupno                          |
| U-MAP-05   | Inputhiba →`MalformedInput`                               | Pogreška pri unosu karte VIES                                      |
| U-MAP-06   | Odbaci neobjektni JSON                                    | Odbaci neobjektni JSON                                             |
| U-MAP-07   | Booleov izraz koji nedostaje ne može biti `Invalid`       | Nedostajući booleov nikad ne postaje nevažeći                      |
| U-MAP-08   | Booleov niz nema `Invalid`                                | Valjanost koja nije Booleova odbijena                              |
| U-MAP-09   | Ne nalazimo lokalno vrijeme za nedostajući datum revizije | Nikada nemojte izmišljati vremensku oznaku revizije koja nedostaje |
| U-KARTA-10 | Odbaci netočan datum revizije                             | Odbaci nevažeću vremensku oznaku revizije                          |
| U-KARTA-11 | Ispravna konverzija vremenske oznake pomaka u UTC         | Raščlanite pomak vremenske oznake na UTC                           |

### `ViesErrorTest`— 6 testova

| ID       | Mađarski gol                                                   | engleska svrha                                         |
| -------- | -------------------------------------------------------------- | ------------------------------------------------------ |
| U-ERR-01 | mađarski+engleski mrežna poruka                                | Pogreška dvojezične mreže                              |
| U-ERR-02 | Pogrešku unosa ne treba ponavljati                             | Pogreška pri unosu je trajna                           |
| U-ERR-03 | HTTP 408/429/5xx retry-besorolása                              | HTTP ponovni pokušaj klasifikacije                     |
| U-ERR-04 | `Valid`/`Invalid`niti greška                                   | Odluke ne otkrivaju pogreške                           |
| U-ERR-05 | Svi javni kodovi HU+EN i stabilna vrijednost ponovnog pokušaja | Svaki javni kod ima HU+EN i politiku ponovnog pokušaja |
| U-ERR-06 | Zadrži nepoznati kod bez ponovnog pokušaja storm               | Sačuvaj nepoznati kod bez ponovnog pokušaja oluje      |

### `ViesAvailabilityTest`— 2 testa

- zaštitni primjerak i nepromjenjivost ulazne karte;
- trenutačno odbijanje karte nulte države članice.

Obrambena nepromjenjiva kopija snimke i provjera valjanosti konstruktora nulte karte.

### `MiniJsonTest`— 4 testa

- tipični VIES dokument i Unicode escape;
- ugniježđeni objekt/lista/skalar/broj/nula;
- JSON escape-ek;
- odbijanje netočnog, skraćenog i pratećeg unosa.

Uobičajeni VIES JSON, ugniježđene/skalarne vrijednosti, izlazne oznake i neispravno zatvoren unos.

### `TtlCacheTest`— 6 testova

- pogodak prije TTL, promašaj poslije;
- točna TTL granica;
- zanemariti nepozitivan TTL;
- konfigurirano ograničenje veličine;
- preferirano pomicanje elementa kojem je istekao rok trajanja;
- 32 virtualne niti istovremeno pisanje/čitanje.

TTL ponašanje, točna granica roka valjanosti, kontrola veličine, prvo uzorkovanje koje je isteklo i
istodobni pritisak iz 32 virtualne niti.

### `ViesClientBuilderTest`— 3 testa

- može se izgraditi kompletna konfiguracija visokog opterećenja;
- odbaci pogrešan URL/ograniči/ponovi pokušaj;
- odbijanje nule, negativnog i preljevnog trajanja.

Važeća konfiguracija visokog opterećenja plus URL, ograničenje, ponovni pokušaj, trajanje i prekoračenje
validacija.

## Lokalni HTTP, konkurentnost i životni ciklus

`ViesClientHttpTest`— 29 testa na nasumičnim slobodnim portovima povratne petlje:

| ID   | Test slučaj                                                                                                                    |
| ---- | ------------------------------------------------------------------------------------------------------------------------------ | ---------------------------------------------------------------------------- |
| I-01 | 503 ponovna pokušaja dva puta, zatim uspjeh / dva 503 ponovna pokušaja pa uspjeh                                               |
| C-01 | 200 asinkronih pozivatelja s istim ključem → točno 1 HTTP zahtjev / 200 asinkronih pozivatelja s istim ključem → jedan zahtjev |
| C-02 | Aktivni HTTP zahtjevi ne prelaze ograničenje od 4 / aktivni pozivi ostaju unutar 4                                             |
| C-03 | Asinkroni na čekanju iznad ograničenja odmah `CLIENT_OVERLOADED`                                                               |
| C-04 | Buduće otkazivanje ne propušta permitet, kapacitet se vraća                                                                    |
| C-05 | `close()` iz asinkronog povratnog poziva ne blokira se                                                                         |
| C-06 | `admissionTimeout` ograničava čekanje                                                                                          |
| I-02 | Redis/cache read error →`CACHE_ERROR`, null VIES poziv                                                                         |
| C-07 | Dozvola i jednokratni let                                                                                                      | otpuštaju se prije uzastopnih asinkronih poziva                              |
| C-08 | Asinkrona predmemorija/zatvaranje utrke →`CLIENT_CLOSED`, nulla HTTP                                                           |
| C-09 | Sinkroniziraj predmemoriju/zatvori utrku →`CLIENT_CLOSED`, nulla HTTP                                                          |
| C-10 | Prilagođeni posao izvršitelja je prekinut, ali se izvršitelj ne zatvara                                                        |
| C-11 | Voditelj sinkronizacije i sljedbenik dobivaju isti rezultat `CLIENT_CLOSED`                                                    |
| C-12 | 100 identičnih asinkronih sljedbenika ne zauzimaju 100 mjesta na čekanju                                                       |
| C-13 | Nakon odbijanja izvršitelja, vraća se dozvola i status u letu                                                                  |
| C-14 | Voditelj sinkronizacije + asinkroni sljedbenik → jedan HTTP zahtjev                                                            |
| C-15 | Druga provjera predmemorije zatvara stale-miss race, null HTTP                                                                 |
| C-16 | Pri zatvaranju tijekom pisanja u predmemoriju, rezultat sinkronizacije vođa/sljedbenik je isti                                 |
| C-17 | Dozvola na čekanju                                                                                                             | također se oslobađa prije lančanog asinkronog poziva s različitim ključevima |
| C-18 | Zadatak prilagođenog izvršitelja koji je u redu čekanja zapravo se poništava zatvaranjem                                       |
| C-19 | Blokiranje povratnog poziva korisnika ne blokira zatvaranje/zaključavanje životnog ciklusa                                     |
| C-20 | Asinkroni vodeći + sinkronizirani sljedbenik → jedan HTTP zahtjev                                                              |
| C-21 | `maxPendingSyncRequests` daje trenutni ograničeni povratni pritisak                                                            |
| C-22 | Fatal async nastavlja se na `Error` izuzetnu budućnost i neuhvaćenog rukovatelja                                               |

## Što ne testiramo prema zadanim postavkama?

### Live VIES dimni test

Usluga uživo je promjenjiva i ograničena brzinom, tako da ne može postojati obavezni CI uvjet.
Ručni test dima je najviše jedan `availability()` i poznata, netajna ili
upitajte porezni broj dobiven iz varijable okruženja, istodobnost=1 i ponovni pokušaji=0
postavljanje.

Live VIES je varijabilan i ograničen brzinom, tako da ne smije pristupiti normalnom CI. Uključivanje
ispitivanje dima treba izvršiti najviše jednu provjeru dostupnosti i jednu validaciju, sa
istovremenost=1 i ponovni pokušaji=0. Nikada nemojte predavati ili bilježiti privatne PDV brojeve podnositelja zahtjeva.

### Test opterećenja

Uvijek se suprotstavljajte lokalnoj laži ili vlastitoj inscenacijskoj službi, nikad javnoj
protiv VIES-a. Apsolutni zahtjevi su informativni; ispravnost, ograničenje, p95/p99 i šifre grešaka
vrata.

Uvijek ciljajte na lokalnu lažnu ili vlastitu uslugu postavljanja, nikada na javni VIES. Apsolutno
zahtjevi/drugi je informativan; ispravnost, granice, p95/p99 i semantika pogreške
su vrata.

Preporučeni slučajevi:

- mnogo različitih tipki;
- 100k pozivatelja s vrućim tipkama kis kulcshalmazon / stampedo s vrućim tipkama;
- preopterećenje i oporavak iznad granice;
- 200/429/503/timeout/neispravan mješoviti odgovor;
- cache-pressure oko konfiguriranog max.

### Test namakanja i kaosa

30-60 minuta fiksnog opterećenja, ograničena gomila, JFR, ponovljeno zatvaranje/ponovno pokretanje, latencija
skok, resetiranje veze, kvar predmemorije i otkazivanje. Hrpa, aktivne niti,
broj platoa u letu/na čekanju i utičnica; ne bi trebalo biti zastoja ili curenja dozvola.

Izvršite 30–60 minuta s fiksnom hrpom, JFR, ponovljenim životnim ciklusom, skokovima latencije,
resetiranje veze, kvarovi predmemorije i otkazivanje. Hrpa/niti/u letu/utičnice
mora stajati bez zastoja ili dopustiti curenje.

JFR primjer / JFR primjer:

```bash
JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=target/vies-soak.jfr,settings=profile" ./mvnw test
```

## Preporuka za CI

Minimalni cjevovod:

```bash
./mvnw --batch-mode clean verify
```

Preporučena matrica: najmanje JDK 21 LTS; samo tada se mogu postaviti dodatni JDK-ovi
podržani ako isti paket stvarno radi na njima.

Preporučena matrica: najmanje JDK 21 LTS. Zatražite dodatnu JDK podršku tek nakon
pokrenuti isti paket na tim verzijama.

## Pravilo regresije

Svaki ispravljeni bug trebao bi imati deterministički test koji je popravak
prije bi propao. Zasun/pregrada bi trebao biti prioritet za natjecateljski test
sinkronizacija; fiksni `sleep` može biti samo kratko odstupanje anketiranja, a ne proročanstvo ispravnosti.

Svaki ispravljeni bug mora proći deterministički test koji nije uspio prije popravka.
Preferirajte zasune/prepreke za testove istovremenosti; fiksna spavanja mogu biti kratka anketa
samo backoff, nikad proročanstvo ispravnosti.
