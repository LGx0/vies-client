# Magyar (hu) — Testing

> [Nyelvválasztó](../../LANGUAGES.md) · Ez a lokalizáció az elérhetőséget szolgálja. Eltérés esetén a kanonikus angol technikai vagy jogi forrás az irányadó. A gyökér `LICENSE` és`NOTICE` jogilag irányadó, fordítás nem helyettesíti.

## Mit nevezünk unit tesztnek? / What is a unit test?

Igen: a unit teszt egyetlen osztályt vagy szabályt izoláltan ellenőriz, külső
hálózat, adatbázis és éles VIES nélkül. Gyors, determinisztikus, és minden buildben
futhat.
Igen: az egységteszt egy osztályt vagy szabályt elszigetelten, külső hálózat nélkül igazol,
adatbázis, vagy élő VIES. Gyors, determinisztikus és minden összeállításhoz alkalmas.
Ennél a modulnál emellett szükségesek **helyi integrációs és konkurenciatesztek**
is, mert a timeout, retry, single-flight, cancel és `close()` viselkedés több
komponens együttműködéséből derül ki. Ezek loopback mock szervert használnak, nem
hívják az EU szolgáltatását.
Ehhez a modulhoz **helyi integrációs és párhuzamossági tesztek** is szükségesek, mivel időtúllépés,
újrapróbálkozás, egyszeri járat, törlés és leállítás több összetevőből áll. Ezeket
A tesztek visszacsatolási álszervert használnak, és soha nem hívják fel a nyilvános EU-szolgáltatást.

## Gyors parancsok / Quick commands

Teljes determinisztikus tesztcsomag / Full deterministic suite:

```bash
./mvnw test
```

Csak unit tesztek / Unit tests only:

```bash
./mvnw -Dtest=VatFormatTest,ViesRequesterTest,ViesResponseMappingTest,ViesErrorTest,ViesAvailabilityTest,ViesClientBuilderTest,MiniJsonTest,TtlCacheTest test
```

Csak helyi HTTP/konkurencia tesztek / Local HTTP and concurrency only:

```bash
./mvnw -Dtest=ViesClientHttpTest test
```

Tiszta teljes ellenőrzés JAR/Javadoc generálással / Clean verification with artifacts:

```bash
./mvnw clean verify
./mvnw package
```

Egy konkrét teszt / One test method:

```bash
./mvnw -Dtest=ViesClientHttpTest#cancellationDoesNotLeakAsyncCapacity test
```

## Jelenlegi lefedettség / Current coverage

A jelenlegi determinisztikus csomag **73 tesztet** tartalmaz:

- **44 unit teszt** nyolc osztályban;
- **29 helyi HTTP/integráció/konkurencia teszt**;
- nulla kötelező külső hálózati hívás.
  A determinisztikus csomag **73 tesztet** tartalmaz: 44 egységtesztet, 29 helyi tesztet
  HTTP/integrációs/párhuzamossági tesztek, és nulla kötelező külső hívás.

## Unit tesztkatalógus / Unit-test catalog

### `VatFormatTest`— 8 teszt

| ID       | Magyar cél                                               | English purpose                               |
| -------- | -------------------------------------------------------- | --------------------------------------------- |
| U-FMT-01 | Teljes adószám normalizálása                             | Normalize a full VAT identifier               |
| U-FMT-02 | Szóköz/pont/kötőjel eltávolítása, nagybetűsítés          | Strip separators and uppercase                |
| U-FMT-03 | `GR`→`EL`leképezés                                       | Map Greece`GR`to VIES`EL`                     |
| U-FMT-04 | Null, üres, ismeretlen ország és hibás hossz elutasítása | Reject null/blank/unknown/bad length          |
| U-FMT-05 | Reprezentatív országformák                               | Representative country formats                |
| U-FMT-06 | Külön országkód+szám API, csatolt prefix                 | Pair API with attached prefix                 |
| U-FMT-07 | Támogatott országkódok                                   | Supported-country set                         |
| U-FMT-08 | Mind a 28 támogatott ország legalább egy formája         | At least one shape for all 28 supported codes |

### `ViesRequesterTest`— 4 teszt

| ID       | Magyar cél                           | English purpose                       |
| -------- | ------------------------------------ | ------------------------------------- |
| U-REQ-01 | Teljes saját adószámból requester    | Create requester from full VAT        |
| U-REQ-02 | Görög requester kanonizálása         | Canonicalize Greek requester          |
| U-REQ-03 | Páros konstruktor csatolt prefixszel | Pair constructor with matching prefix |
| U-REQ-04 | Hibás requester azonnali elutasítása | Fail fast on invalid requester        |

### `ViesResponseMappingTest`— 11 teszt

| ID       | Magyar cél                                          | English purpose                       |
| -------- | --------------------------------------------------- | ------------------------------------- |
| U-MAP-01 | GET-stílusú `Valid` minden mezővel                  | Map GET-style Valid response          |
| U-MAP-02 | POST-stílusú `Valid`,`---` placeholder              | Map POST-style Valid and placeholders |
| U-MAP-03 | Hiteles `Invalid`                                   | Map authoritative Invalid             |
| U-MAP-04 | Átmeneti hiba →`Unavailable`                        | Map transient error to Unavailable    |
| U-MAP-05 | Inputhiba →`MalformedInput`                         | Map VIES input error                  |
| U-MAP-06 | Nem objektum JSON elutasítása                       | Reject non-object JSON                |
| U-MAP-07 | Hiányzó boolean nem lehet `Invalid`                 | Missing boolean never becomes Invalid |
| U-MAP-08 | String boolean nem lehet `Invalid`                  | Non-boolean validity rejected         |
| U-MAP-09 | Hiányzó audit dátumhoz nem találunk ki lokális időt | Never invent missing audit timestamp  |
| U-MAP-10 | Hibás audit dátum elutasítása                       | Reject invalid audit timestamp        |
| U-MAP-11 | Offset timestamp helyes UTC-vé alakítása            | Parse offset timestamp to UTC         |

### `ViesErrorTest`— 6 teszt

| ID       | Magyar cél                                       | English purpose                              |
| -------- | ------------------------------------------------ | -------------------------------------------- |
| U-ERR-01 | Magyar+angol hálózati üzenet                     | Bilingual network error                      |
| U-ERR-02 | Inputhiba nem retry-zható                        | Input error is permanent                     |
| U-ERR-03 | HTTP 408/429/5xx retry-besorolása                | HTTP retry classification                    |
| U-ERR-04 | `Valid`/`Invalid` nem error                      | Decisions expose no error                    |
| U-ERR-05 | Minden publikus kód HU+EN és stabil retry értéke | Every public code has HU+EN and retry policy |
| U-ERR-06 | Ismeretlen kód megőrzése retry storm nélkül      | Preserve unknown code without retry storm    |

### `ViesAvailabilityTest`— 2 teszt

- a bemeneti map védelmi másolata és megváltoztathatatlansága;
- null tagállami map azonnali elutasítása.
  Védekező megváltoztathatatlan pillanatképmásolat és null-map konstruktor érvényesítése.

### `MiniJsonTest`— 4 teszt

- tipikus VIES dokumentum és Unicode escape;
- nested objektum/lista/scalar/number/null;
- JSON escape-ek;
- hibás, csonka és trailing input elutasítása.
  Tipikus VIES JSON, beágyazott/skaláris értékek, escape-kódok és hibazárású, hibásan formázott bemenet.

### `TtlCacheTest`— 6 teszt

- hit a TTL előtt, miss utána;
- pontos TTL-határ;
- nem pozitív TTL figyelmen kívül hagyása;
- konfigurált méretkorlát;
- lejárt elem preferált kiszorítása;
- 32 virtuális szálas konkurens írás/olvasás.
  TTL viselkedés, pontos lejárati határ, méretszabályozás, lejárt első mintavétel és
  egyidejű nyomás 32 virtuális szálról.

### `ViesClientBuilderTest`— 3 teszt

- teljes nagyterhelésű konfiguráció felépíthető;
- hibás URL/limit/retry elutasítása;
- nulla, negatív és overflow Duration elutasítása.
  Érvényes nagy terhelésű konfiguráció, valamint URL, korlátozás, újrapróbálkozás, időtartam és túlcsordulás
  érvényesítését.

## Helyi HTTP, konkurencia és életciklus / Local integration and concurrency

`ViesClientHttpTest`— 29 teszt, véletlen szabad loopback porton:
| ID | Teszteset / Test case |
|---|---|
| I-01 | 503 retry kétszer, majd siker / two 503 retries then success |
| C-01 | 200 azonos async hívó → pontosan 1 HTTP-kérés / 200 same-key async callers → one request |
| C-02 | Aktív HTTP-kérések nem lépik túl a 4-es limitet / active calls stay within 4 |
| C-03 | Async pending limit felett azonnali `CLIENT_OVERLOADED`|
| C-04 | Future cancel nem szivárogtat permitet, a kapacitás helyreáll |
| C-05 |`close()` async callbackből nem deadlockol |
| C-06 |`admissionTimeout` korlátozza a sorban állást |
| I-02 | Redis/cache olvasási hiba →`CACHE_ERROR`, nulla VIES-hívás |
| C-07 | Egymás utáni async hívások előtt felszabadul a permit és single-flight |
| C-08 | Async cache/close race →`CLIENT_CLOSED`, nulla HTTP |
| C-09 | Sync cache/close race →`CLIENT_CLOSED`, nulla HTTP |
| C-10 | Custom executor munka megszakad, de az executor nem záródik be |
| C-11 | Sync leader és follower ugyanazt a `CLIENT_CLOSED` eredményt kapja |
| C-12 | 100 azonos async follower nem fogyaszt 100 pending slotot |
| C-13 | Executor rejection után permit és in-flight állapot helyreáll |
| C-14 | Sync leader + async follower → egy HTTP-kérés |
| C-15 | Második cache-check lezárja a stale-miss race-t, nulla HTTP |
| C-16 | Cache-írás közbeni close-nál a sync leader/follower eredménye azonos |
| C-17 | Különböző kulcsú láncolt async hívás előtt is felszabadul a pending permit |
| C-18 | Sorban álló custom-executor feladatot a close ténylegesen cancelöl |
| C-19 | Blokkoló felhasználói callback nem blokkolja a close/lifecycle lockot |
| C-20 | Async leader + sync follower → egy HTTP-kérés |
| C-21 |`maxPendingSyncRequests` azonnali, korlátos backpressure-t ad |
| C-22 | Fatális async `Error` exceptional future és uncaught handler felé is továbbmegy |

## Mit nem tesztelünk alapból? / What is not in the default suite?

## Élő VIES smoke teszt / Live VIES smoke test

Az élő szolgáltatás változó és rate-limited, ezért nem lehet kötelező CI-feltétel.
Kézi smoke teszt legfeljebb egy `availability()` és egy ismert, nem titkos vagy
környezeti változóból kapott adószám lekérdezése legyen, concurrency=1 és retries=0
beállítással.
Az élő VIES változó és sebességkorlátozott, ezért nem szabad normál CI-t kapuzni. Jelentkezés
a füsttesztnek legfeljebb egy rendelkezésre állási ellenőrzést és egy érvényesítést kell végrehajtania
concurrency=1 és próbálkozások=0. Soha ne kötelezze el vagy ne naplózza a privát kérelmezők áfaszámait.

## Load teszt / Load test

Mindig helyi mock vagy saját staging szolgáltatás ellen fusson, soha ne a publikus
VIES ellen. Az abszolút req/s informatív; a helyesség, limit, p95/p99 és hibakódok
a kapuk.
Mindig célozzon meg egy helyi színlelt vagy saját közvetítő szolgáltatást, soha ne nyilvános VIES-t. Abszolút
kérések/másodperc tájékoztató jellegű; helyesség, határok, p95/p99 és hibaszemantika
a kapuk.
Ajánlott esetek / Recommended cases:

- sok különböző kulcs / many distinct keys;
- 100k hot-key caller kis kulcshalmazon / hot-key stampede;
- limit feletti overload és recovery;
- 200/429/503/timeout/malformed vegyes válasz;
- cache-pressure a konfigurált max körül.

## Soak és chaos teszt / Soak and chaos test

30–60 perces fix terhelés, korlátozott heap, JFR, ismételt close/restart, latency
spike, connection reset, cache failure és cancellation. A heap, aktív szálak,
in-flight/pending és socketek száma platózzon; ne legyen deadlock vagy permit leak.
30–60 perces futás fix kupacokkal, JFR, ismételt életciklus, késleltetési csúcsok,
kapcsolat alaphelyzetbe állítása, gyorsítótár-hibák és törlés. Heap/szálak/repülés közben/aljzatok
fennsíkon fenn kell állnia holtpont vagy szivárgás nélkül.
JFR példa / JFR example:

```bash
JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=target/vies-soak.jfr,settings=profile" ./mvnw test
```

## ONLY javaslat / ONLY Ajánlott

Minimális csővezeték:

```bash
./mvnw --batch-mode clean verify
```

Ajánlott mátrix: legalább JDK 21 LTS; további JDK-k csak akkor állíthatók
támogatottnak, ha ugyanaz a suite ténylegesen lefut rajtuk.
Ajánlott mátrix: legalább JDK 21 LTS. Csak ezt követően igényeljen további JDK-támogatást
ugyanazt a csomagot futtatja azokon a verziókon.

## Regressziós szabály / Regression rule

Minden kijavított hibához olyan determinisztikus teszt tartozzon, amely a javítás
előtt elbukott volna. Konkurenciatesztnél latch/barrier legyen az elsődleges
szinkronizáció; fix `sleep` csak rövid polling backoff lehet, ne a helyesség orákuluma.
Minden javított hibának meg kell kapnia egy determinisztikus tesztet, amely sikertelen volt a javítás előtt.
Előnyben részesítse a reteszeket/korlátokat az egyidejűségi tesztekhez; a rögzített alvások rövid lekérdezések lehetnek
csak visszalépés, soha a helyesség orákulum.
