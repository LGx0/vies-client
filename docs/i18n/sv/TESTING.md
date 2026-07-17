# Svenska (sv) — TESTING

> [Alla språk](../../LANGUAGES.md) · Informativ översättning. Vid avvikelser gäller den kanoniska engelska tekniska eller juridiska källan. Endast `LICENSE` och `NOTICE` i roten är juridiskt auktoritativa; översättningen ersätter dem inte.

## Vad kallar vi ett enhetstest? / Vad är ett enhetstest?

Ja: enhetstestet kontrollerar en enskild klass eller regel isolerat, extern
utan nätverk, databas och live VIES. Snabbt, deterministiskt och i varje byggnad
kan springa.

Ja: ett enhetstest verifierar en klass eller regel isolerat, utan externt nätverk,
databas eller live VIES. Det är snabbt, deterministiskt och lämpar sig för varje byggnad.

Denna modul kräver också **lokal integration och tävlingstester**
även för att beteendet timeout, försök igen, enkelflyg, avbryt och`close()`är fler
det framgår av komponentens samarbete. De använder en loopback mock-server, nej
ring EU-tjänsten.

Den här modulen behöver också **lokal integration och samtidighetstester**, eftersom timeout,
försök igen, enkelflyg, avbokning och avstängning involverar flera komponenter. Dessa
tester använder en loopback-mock-server och ringer aldrig den offentliga EU-tjänsten.

## Snabbkommandon / Snabbkommandon

Fullständigt deterministiskt testpaket / Fullständig deterministisk svit:

```bash
./mvnw test
```

Endast enhetstester:

```bash
./mvnw -Dtest=VatFormatTest,ViesRequesterTest,ViesResponseMappingTest,ViesErrorTest,ViesAvailabilityTest,ViesClientBuilderTest,MiniJsonTest,TtlCacheTest test
```

Endast lokala HTTP/samtidighetstester / Lokal HTTP och endast samtidighet:

```bash
./mvnw -Dtest=ViesClientHttpTest test
```

Ren verifiering med JAR/Javadoc generation / Ren verifiering med artefakter:

```bash
./mvnw clean verify
./mvnw package
```

Ett specifikt test / En testmetod:

```bash
./mvnw -Dtest=ViesClientHttpTest#cancellationDoesNotLeakAsyncCapacity test
```

## Nuvarande täckning / Nuvarande täckning

Det nuvarande deterministiska paketet innehåller **73 test**:

- **44 enheter test** i åtta klasser;
- **29 lokala HTTP/integration/samtidighetstester**;
- noll obligatoriska externa nätverksanrop.

Den deterministiska sviten innehåller **73 test**: 44 enhetstester, 29 lokala
HTTP/integration/samtidighetstester och noll obligatoriska externa samtal.

## Enhetstestkatalog / Enhetstestkatalog

###`VatFormatTest`— 8 tester

| ID       | Ungerskt mål                                      | engelska syfte                            |
| -------- | ------------------------------------------------- | ----------------------------------------- |
| U-FMT-01 | Normalisering av totalt skattenummer              | Normalisera hela momsregistret            |
| U-FMT-02 | Ta bort mellanslag/punkt/bindestreck, versal      | Remsavskiljare och versaler               |
| U-FMT-03 | `GR`→`EL`mappning                                 | Karta Grekland`GR`till VIES`EL`           |
| U-FMT-04 | Avvisa null, blank, okänt land och felaktig längd | Avvisa null/tom/okänd/dålig längd         |
| U-FMT-05 | Representativa landsformulär                      | Representativa landsformat                |
| U-FMT-06 | Separat landskod+nummer API, bifogat prefix       | Para API med bifogat prefix               |
| U-FMT-07 | Landskoder som stöds                              | Land som stöds                            |
| U-FMT-08 | Alla 28 länder som stöds har minst en form av     | Minst en form för alla 28 koder som stöds |

###`ViesRequesterTest`— 4 tester

| ID       | Ungerskt mål                                 | engelska syfte                         |
| -------- | -------------------------------------------- | -------------------------------------- |
| U-REQ-01 | Komplettera från egen skattenummerbeställare | Skapa förfrågan från full moms         |
| U-REQ-02 | Kanonisering av grekisk begärande            | Kanonisera grekisk begärande           |
| U-REQ-03 | Parad konstruktor med bifogat prefix         | Para konstruktor med matchande prefix  |
| U-REQ-04 | Omedelbart avslag på felaktig begärande      | Misslyckas snabbt på ogiltig begärande |

###`ViesResponseMappingTest`— 11 tester

| ID       | Ungerskt mål                                           | engelska syfte                                  |
| -------- | ------------------------------------------------------ | ----------------------------------------------- |
| U-MAP-01 | GET-stil`Valid`med alla fält                           | Karta GET-stil Giltigt svar                     |
| U-MAP-02 | POST-stílusú`Valid`,`---` platshållare                 | Karta POST-stil Giltig och platshållare         |
| U-MAP-03 | Hiteles`Invalid`                                       | Karta auktoritativ Ogiltig                      |
| U-MAP-04 | Ätmeneti hiba →`Unavailable`                           | Mappa övergående fel till Ej tillgänglig        |
| U-MAP-05 | Inmatningsfel →`MalformedInput`                        | Mappa VIES-inmatningsfel                        |
| U-MAP-06 | Avvisa icke-objekt JSON                                | Avvisa icke-objekt JSON                         |
| U-MAP-07 | Saknade boolean kan inte vara`Invalid`                 | Saknad boolesk blir aldrig Ogiltig              |
| U-MAP-08 | A Boolean string cannot become `Invalid`               | Icke-boolesk giltighet avvisad                  |
| U-MAP-09 | Vi hittar inte lokal tid för ett saknat revisionsdatum | Uppfinn aldrig en saknad granskningstidsstämpel |
| U-MAP-10 | Avvisa felaktigt revisionsdatum                        | Avvisa ogiltig granskningstidsstämpel           |
| U-MAP-11 | Korrekt omvandling av offset tidsstämpel till UTC      | Analysera offset tidsstämpel till UTC           |

###`ViesErrorTest`— 6 tester

| ID       | Ungerskt mål                                         | engelska syfte                                           |
| -------- | ---------------------------------------------------- | -------------------------------------------------------- |
| U-ERR-01 | Ungerska+engelska nätverksmeddelande                 | Tvåspråkigt nätverksfel                                  |
| U-ERR-02 | Input errors are not retried                         | Inmatningsfelet är permanent                             |
| U-ERR-03 | HTTP 408/429/5xx försök igen-besorolása              | HTTP-försök igen klassificering                          |
| U-ERR-04 | `Valid`/`Invalid`eller fel                           | Beslut visar inga fel                                    |
| U-ERR-05 | Alla offentliga koder HU+EN och stabilt försöksvärde | Varje offentlig kod har HU+EN och policy för försök igen |
| U-ERR-06 | Behåll okänd kod utan att försöka storma igen        | Bevara okänd kod utan att försöka storma igen            |

###`ViesAvailabilityTest`— 2 tester

- skyddskopia och oföränderlighet av inmatningskartan;
- Omedelbart förkastande av en karta över en noll medlemsstat.

Defensiv oföränderlig ögonblicksbildkopia och noll-map-konstruktorvalidering.

###`MiniJsonTest`— 4 tester

- typisk VIES-dokument och Unicode-escape;
- kapslade objekt/lista/skalär/nummer/null;
- JSON escape-ek;
- avvisande av felaktig, trunkerad och efterföljande inmatning.

Typisk VIES JSON, kapslade/skalära värden, escapes och felstängd felaktig indata.

###`TtlCacheTest`— 6 tester

- träffa före TTL, missa efter;
- Exakt TTL-gräns;
- ignorera icke-positiv TTL;
- konfigurerad storleksgräns;
- föredragen förskjutning av ett utgått element;
- 32 virtuella trådar samtidigt skriva/läsa.

TTL-beteende, exakt utgångsgräns, storlekskontroll, utgånget-först provtagning och
samtidigt tryck från 32 virtuella trådar.

###`ViesClientBuilderTest`— 3 tester

- komplett högbelastningskonfiguration kan byggas;
- avvisa fel URL/gräns/försök igen;
- förkastning av noll, negativ och överflöde Varaktighet.

Giltig högbelastningskonfiguration plus URL, gräns, försök igen, varaktighet och spill
godkännande.

## Lokal HTTP, samtidighet och livscykel / Lokal integration och samtidighet

`ViesClientHttpTest`— 29 tester på en slumpmässig fri loopback-port:

| ID | Testfall |
| ---- | ----------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------- |
| I-01 | 503 försök två gånger, sedan framgång / två 503 försök igen sedan framgång |
| C-01 | 200 asynkrona anropare med samma nyckel → exakt 1 HTTP-förfrågan / 200 asynkrona anropare med samma nyckel → en begäran |
| C-02 | Aktiva HTTP-förfrågningar överskrider inte gränsen på 4 / aktiva samtal stannar inom 4 |
| C-03 | Asynkronisering väntar över gränsen omedelbar`CLIENT_OVERLOADED` |
| C-04 | Framtida avbrytning läcker inte, kapaciteten återställs |
| C-05 | `close()`from async callback does not deadlock |
| C-06 | `admissionTimeout`begränsar köbildning |
| I-02 | Redis/cache-läsfel →`CACHE_ERROR`, null VIES-anrop |
| C-07 | Tillstånd och enkelflygning | släpps före på varandra följande asynkrona samtal |
| C-08 | Async cache/close race →`CLIENT_CLOSED`, nulla HTTP |
| C-09 | Synkronisera cache/stäng race →`CLIENT_CLOSED`, nulla HTTP |
| C-10 | Anpassat executor-jobb avbryts, men executorn stänger inte |
| C-11 | Synkroniseringsledare och följare får samma resultat`CLIENT_CLOSED` |
| C-12 | 100 identiska asynkrona följare förbrukar inte 100 väntande slots |
| C-13 | Efter att exekutor avvisats, tillstånd och status under flygning återställs |
| C-14 | Synkledare + asynkronföljare → en HTTP-förfrågan |
| C-15 | Andra cachekontrollen stänger stale-miss race, null HTTP |
| C-16 | Vid slutet under cacheskrivning är resultatet av synkroniseringsledare/följare detsamma |
| C-17 | Det väntande tillståndet | släpps också före ett kedjat asynkronsamtal med olika nycklar |
| C-18 | En köad custom-executor-uppgift avbryts faktiskt av close |
| C-19 | Blockering av användaråteruppringning blockerar inte stängning/livscykellås |
| C-20 | Async leader + sync follower → en HTTP-förfrågan |
| C-21 | `maxPendingSyncRequests`ger omedelbart begränsat mottryck |
| C-22 | Fatal async fortsätter till`Error`exceptionell framtid och ofångad hanterare |

## Vad testar vi inte som standard? / Vad finns inte i standardsviten?

### Live VIES röktest / Live VIES röktest

Livetjänsten är variabel och hastighetsbegränsad, så det kan inte finnas ett obligatoriskt CI-villkor.
Manuellt röktest är högst en`availability()`och en känd, icke-hemlig eller
fråga efter skattenumret som erhålls från en miljövariabel, concurrency=1 och retries=0
miljö.

Live VIES är variabel och hastighetsbegränsad, så den får inte gate normal CI. En opt-in
röktest bör utföra högst en tillgänglighetskontroll och en validering, med
samtidighet=1 och försök igen=0. Förplikta eller logga aldrig privata begärande momsnummer.

### Lasttest / Lasttest

Kör alltid mot en lokal hån eller din egen iscensättningstjänst, aldrig den offentliga
mot VIES. Absoluta krav är informativa; korrekthet, limit, p95/p99 och felkoder
portarna.

Inrikta dig alltid på en lokal mock eller ägd iscensättningstjänst, aldrig offentliga VIES. Absolut
förfrågningar/sekund är informativ; korrekthet, gränser, p95/p99 och felsemantik
är portarna.

Rekommenderade fall:

- många distinkta nycklar;
- 100 000 snabbvalsanropare kis kulcshalmazon / hotkey-stamp;
- överbelastning och återhämtning över gränsen;
- 200/429/503/timeout/felformat blandat svar;
- cache-tryck runt det konfigurerade max.

### Soak and chaos test / Soak and chaos test

30-60 minuter fast belastning, begränsad heap, JFR, upprepad stängning/omstart, latens
spik, anslutningsåterställning, cachefel och avbrytning. Högen, aktiva trådar,
antal under flygning/väntande och sockets platå; det ska inte finnas blockerat låsläge eller tillståndsläckor.

Kör 30–60 minuter med en fast heap, JFR, upprepad livscykel, latency spikes,
anslutningsåterställningar, cachefel och avbrytning. Hög/trådar/under flygning/uttag
måste platå utan dödläge eller tillåta läckor.

JFR-exempel / JFR-exempel:

```bash
JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=target/vies-soak.jfr,settings=profile" ./mvnw test
```

## ENDAST recommendation / ENDAST rekommenderas

Minsta pipeline:

```bash
./mvnw --batch-mode clean verify
```

Rekommenderad matris: minst JDK 21 LTS; ytterligare JDK:er kan endast ställas in då
stöds om samma svit faktiskt körs på dem.

Rekommenderad matris: minst JDK 21 LTS. Ansök om ytterligare JDK-stöd först efter
kör samma svit på de versionerna.

## Regressionsregel / Regressionsregel

Varje fixad bugg bör ha ett deterministiskt test som är fixen
skulle ha misslyckats tidigare. Spärr/barriär bör prioriteras vid tävlingstest
synkronisering; fixerad`sleep`kan bara vara en kort pollingbackoff, inte ett orakel för korrekthet.

Varje fixad bugg måste få ett deterministiskt test som misslyckades före korrigeringen.
Föredrar spärrar/barriärer för samtidighetstester; fast sömn kan vara kort polling
endast backoff, aldrig korrekthetsoraklet.
