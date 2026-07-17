# Română (ro) — Testare

> [Toate limbile](../../LANGUAGES.md) · Traducere informativă. În caz de diferență prevalează sursa canonică tehnică sau juridică în limba engleză. Numai `LICENSE` și `NOTICE` din rădăcină sunt texte juridice oficiale; traducerea nu le înlocuiește.

## Cum numim un test unitar?

Da: testul unitar verifică o singură clasă sau regulă izolat, extern
fără rețea, bază de date și VIES live. Rapid, determinist și în fiecare build
poate alerga.

Da: un test unitar verifică o clasă sau o regulă izolat, fără o rețea externă,
baza de date sau VIES live. Este rapid, determinist și potrivit pentru fiecare construcție.

Acest modul necesită, de asemenea, **integrare locală și teste de concurs**
De asemenea, pentru că comportamentele timeout, retry, single-flight, cancel și `close()` sunt mai multe
este evident din cooperarea componentei. Ei folosesc un server simulat de loopback, nu
apelați serviciul UE.

Acest modul necesită, de asemenea, **integrare locală și teste de concurență**, deoarece timeout,
reîncercarea, zborul unic, anularea și oprirea implică mai multe componente. Aceste
testele folosesc un server simulat de loopback și nu apelează niciodată serviciul public al UE.

## Comenzi rapide

Pachet complet de test determinist / Suită completă deterministă:

```bash
./mvnw test
```

Numai teste unitare:

```bash
./mvnw -Dtest=VatFormatTest,ViesRequesterTest,ViesResponseMappingTest,ViesErrorTest,ViesAvailabilityTest,ViesClientBuilderTest,MiniJsonTest,TtlCacheTest test
```

Numai teste locale HTTP/concurență / numai HTTP local și concurență:

```bash
./mvnw -Dtest=ViesClientHttpTest test
```

Verificare curată cu generarea JAR/Javadoc / Verificare curată cu artefacte:

```bash
./mvnw clean verify
./mvnw package
```

Un test specific / O metodă de testare:

```bash
./mvnw -Dtest=ViesClientHttpTest#cancellationDoesNotLeakAsyncCapacity test
```

## Acoperire curentă

Pachetul determinist actual conține **73 de teste**:

- **test de 44 de unități** în opt clase;
- **29 de teste locale HTTP/integrare/concurență**;
- zero apeluri obligatorii de rețea externă.

Suita deterministă conține **73 de teste**: 44 de teste unitare, 29 locale
Teste HTTP/integrare/concurență și zero apeluri externe obligatorii.

## Catalog test unitar

### `VatFormatTest`— 8 teste

| ID       | golul maghiar                                                      | scop englezesc                                             |
| -------- | ------------------------------------------------------------------ | ---------------------------------------------------------- |
| U-FMT-01 | Normalizarea codului fiscal total                                  | Normalizați identificatorul complet de TVA                 |
| U-FMT-02 | Eliminați spațiu/punt/cratima, scrieți cu majuscule                | Separatoare de bandă și majuscule                          |
| U-FMT-03 | `GR`→`EL`mapare                                                    | Harta Greciei`GR`la VIES`EL`                               |
| U-FMT-04 | Respingeți nul, necompletat, țară necunoscută și lungime incorectă | Respinge lungimea nulă/albă/necunoscută/proasta            |
| U-FMT-05 | Formulare reprezentative de țară                                   | Formate reprezentative de țară                             |
| U-FMT-06 | Cod de țară+număr API separat, prefix atașat                       | Asociați API-ul cu prefixul atașat                         |
| U-FMT-07 | Codurile de țară acceptate                                         | Set de țări acceptate                                      |
| U-FMT-08 | Toate cele 28 de țări acceptate au cel puțin o formă de            | Cel puțin o formă pentru toate cele 28 de coduri acceptate |

### `ViesRequesterTest`— 4 teste

| ID       | golul maghiar                                       | scop englezesc                        |
| -------- | --------------------------------------------------- | ------------------------------------- |
| U-REQ-01 | Completați din solicitantul propriului număr fiscal | Creați solicitant din TVA complet     |
| U-REQ-02 | Canonizarea solicitantului grec                     | Canonicalizează solicitantul grecesc  |
| U-REQ-03 | Constructor asociat cu prefixul atașat              | Constructor de perechi cu prefixul    |
| U-REQ-04 | Respingerea imediată a solicitantului defect        | Eșuează rapid la solicitantul nevalid |

### `ViesResponseMappingTest`— 11 teste

| ID       | golul maghiar                                            | scop englezesc                                             |
| -------- | -------------------------------------------------------- | ---------------------------------------------------------- |
| U-MAP-01 | `Valid` în stil GET cu toate câmpurile                   | Hartă în stil GET Răspuns valid                            |
| U-MAP-02 | POST-stílusú `Valid`,`---` substituent                   | Hartă în stil POST Valabil și substituenți                 |
| U-MAP-03 | Hiteles `Invalid`                                        | Hartă autorizată Invalid                                   |
| U-MAP-04 | Átmeneti hiba →`Unavailable`                             | Hartați eroarea tranzitorie la Indisponibil                |
| U-MAP-05 | Inputhiba →`MalformedInput`                              | Eroare de introducere VIES pe hartă                        |
| U-MAP-06 | Respinge JSON non-obiect                                 | Respinge JSON non-obiect                                   |
| U-MAP-07 | Booleanul lipsă nu poate fi `Invalid`                    | Booleanul lipsă nu devine niciodată Invalid                |
| U-MAP-08 | Un șir boolean nu poate deveni `Invalid`                 | Valabilitate non-booleană respinsă                         |
| U-MAP-09 | Nu găsim ora locală pentru o dată de audit lipsă         | Nu inventați niciodată marcajul de timp al auditului lipsă |
| U-MAP-10 | Respinge data de audit incorectă                         | Respingeți marcajul de timp al auditului nevalid           |
| U-MAP-11 | Conversia corectă a marcajului de timp de decalaj în UTC | Analizați marca temporală a decalajului la UTC             |

### `ViesErrorTest`— 6 teste

| ID       | golul maghiar                                                   | scop englezesc                                          |
| -------- | --------------------------------------------------------------- | ------------------------------------------------------- |
| U-ERR-01 | mesaj de rețea maghiară+engleză                                 | Eroare de rețea bilingvă                                |
| U-ERR-02 | Eroarea de intrare nu se reîncearcă                             | Eroarea de intrare este permanentă                      |
| U-ERR-03 | HTTP 408/429/5xx reîncercați-besorolása                         | Clasificarea reîncercării HTTP                          |
| U-ERR-04 | `Valid`/`Invalid`nici eroare                                    | Deciziile nu expun nicio eroare                         |
| U-ERR-05 | Toate codurile publice HU+EN și valoarea de reîncercare stabilă | Fiecare cod public are HU+EN și politică de reîncercare |
| U-ERR-06 | Păstrați codul necunoscut fără reîncercați storm                | Păstrați codul necunoscut fără reîncercați storm        |

### `ViesAvailabilityTest`— 2 teste

- copie de protecție și imuabilitate a hărții de intrare;
- respingerea imediată a hărții statelor membre nule.

Copiere defensive imuabile instantanee și validare a constructorului de hartă nulă.

### `MiniJsonTest`— 4 teste

- document tipic VIES și evadare Unicode;
- obiect imbricat/listă/scalar/număr/null;
- JSON escape-ek;
- respingerea intrării incorecte, trunchiate și în urmă.

JSON tipic VIES, valori imbricate/scalare, escape și intrare defectuoasă închisă.

### `TtlCacheTest`— 6 teste

- lovit înainte de TTL, dor după;
- limita TTL exactă;
- ignora TTL non-pozitiv;
- limita de dimensiune configurata;
- deplasarea preferată a unui element expirat;
- 32 fire virtuale de scriere/citire simultană.

Comportamentul TTL, limita exactă de expirare, controlul dimensiunii, eșantionarea expirată-prima și
presiune concomitentă din 32 fire virtuale.

### `ViesClientBuilderTest`— 3 teste

- se poate construi o configurație completă de sarcină mare;
- respinge adresa URL/limita/reîncerca greșită;
- respingerea duratei zero, negative și de depășire.

Configurație validă de încărcare mare plus adresa URL, limită, reîncercare, durată și depășire
validare.

## HTTP local, concurență și ciclu de viață

`ViesClientHttpTest`— 29 de teste pe un port loopback liber aleatoriu:

| ID   | Caz de testare                                                                                                               |
| ---- | ---------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------- |
| I-01 | 503 reîncercări de două ori, apoi succes / două 503 reîncercări apoi succes                                                  |
| C-01 | 200 de apelanți asincroni cu aceeași cheie → exact 1 cerere HTTP / 200 de apelanți asincroni cu aceeași cheie → o solicitare |
| C-02 | Solicitările HTTP active nu depășesc limita de 4 / apelurile active rămân în 4                                               |
| C-03 | Async în așteptare peste limita imediată `CLIENT_OVERLOADED`                                                                 |
| C-04 | Anularea viitoare nu permite scurgeri, capacitatea este restabilită                                                          |
| C-05 | `close()` din callback asincron nu produce deadlock                                                                          |
| C-06 | `admissionTimeout` limitează coada                                                                                           |
| I-02 | Eroare de citire redis/cache →`CACHE_ERROR`, apel VIES nul                                                                   |
| C-07 | Permis și zbor unic                                                                                                          | sunt eliberate înaintea apelurilor asincrone consecutive                          |
| C-08 | Cache asincron/închide cursa →`CLIENT_CLOSED`, nulla HTTP                                                                    |
| C-09 | Sincronizare cache/închidere cursă →`CLIENT_CLOSED`, nulla HTTP                                                              |
| C-10 | Lucrarea de executant personalizat este întreruptă, dar executorul nu se închide                                             |
| C-11 | Liderul de sincronizare și urmăritorul obțin același rezultat `CLIENT_CLOSED`                                                |
| C-12 | 100 de urmăritori asincroni identici nu consumă 100 de sloturi în așteptare                                                  |
| C-13 | După respingerea executorului, permisul și starea în zbor sunt restabilite                                                   |
| C-14 | Lider de sincronizare + urmăritor asincron → o cerere HTTP                                                                   |
| C-15 | A doua verificare a memoriei cache închide cursa învechită, HTTP nul                                                         |
| C-16 | La închidere în timpul scrierii în cache, rezultatul sincronizării lider/follower este același                               |
| C-17 | Permisul în curs                                                                                                             | este de asemenea eliberat înainte de un apel asincron înlănțuit cu taste diferite |
| C-18 | O sarcină de executare personalizată aflată în coadă este de fapt anulată prin close                                         |
| C-19 | Blocarea apelului invers al utilizatorului nu blochează închiderea/blocarea ciclului de viață                                |
| C-20 | Lider asincron + urmăritor de sincronizare → o cerere HTTP                                                                   |
| C-21 | `maxPendingSyncRequests` oferă o contrapresiune limitată instantanee                                                         |
| C-22 | Fatal Async continuă să `Error` viitor excepțional și handler neprins                                                        |

## Ce nu testăm în mod implicit?

### Test de fum VIES în direct

Serviciul live este variabil și cu tarif limitat, deci nu poate exista o condiție CI obligatorie.
Testul manual de fum este cel mult un `availability()` și un cunoscut, non-secret sau
interogați numărul de taxă obținut dintr-o variabilă de mediu, concurență=1 și reîncercări=0
setare.

VIES în direct este variabil și limitat la rată, deci nu trebuie să poartă CI normal. O înscriere
testul de fum ar trebui să efectueze cel mult o verificare a disponibilității și o validare, cu
concurență=1 și reîncercări=0. Nu comiteți și nu înregistrați niciodată numere de TVA private ale solicitantului.

### Test de încărcare

Confruntați întotdeauna cu o imitație locală sau cu propriul serviciu de montaj, niciodată cu cel public
împotriva VIES. Cerințele absolute sunt informative; corectitudine, limită, p95/p99 și coduri de eroare
portile.

Întotdeauna țintiți un serviciu de montaj simulat local sau deținut, niciodată VIES public. Absolut
cereri/secunda este informativă; corectitudinea, limitele, p95/p99 și semantica erorii
sunt porțile.

Cazuri recomandate:

- multe chei distincte;
- 100.000 de taste de apelare rapidă kis kulcshalmazon / hot-key stampede;
- suprasarcina si recuperare peste limita;
- 200/429/503/timeout/răspuns mixt defect;
- presiunea cache în jurul valorii max.

### Test de înmuiere și haos

Sarcină fixă ​​de 30-60 de minute, heap limitată, JFR, închidere/repornire repetată, latență
spike, resetarea conexiunii, eșecul memoriei cache și anularea. Heap, fire active,
numărul de în zbor/în așteptare și platou de prize; nu ar trebui să existe blocaje sau scurgeri de permise.

Rulați 30-60 de minute cu un heap fix, JFR, ciclu de viață repetat, vârfuri de latență,
resetări de conexiune, eșecuri de cache și anulare. Heap/fire/în zbor/prize
trebuie să se stabilească fără blocaj sau să permită scurgeri.

Exemplu JFR / Exemplu JFR:

```bash
JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=target/vies-soak.jfr,settings=profile" ./mvnw test
```

## Recomandare pentru CI

Conducta minimă:

```bash
./mvnw --batch-mode clean verify
```

Matrice recomandată: cel puțin JDK 21 LTS; Numai atunci pot fi setate JDK-uri suplimentare
acceptat dacă aceeași suită rulează efectiv pe ele.

Matrice recomandată: cel puțin JDK 21 LTS. Solicitați asistență suplimentară JDK numai după
rulează aceeași suită pe acele versiuni.

## Regulă de regresie

Fiecare eroare remediată ar trebui să aibă un test determinist care este remedierea
ar fi eșuat înainte. Încuietoarea/bariera ar trebui să fie prioritatea testului de concurs
sincronizare;`sleep` fix poate fi doar o scurtă retragere a sondajului, nu un oracol al corectitudinii.

Fiecare eroare remediată trebuie să primească un test determinist care a eșuat înainte de remediere.
Preferați zăvoarele/barierele pentru testele de concurență; somnurile fixe pot fi sondaje scurte
doar backoff, niciodată oracolul corectitudinii.
