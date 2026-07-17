# Dansk (da) — TESTING

> [Alle sprog](../../LANGUAGES.md) · Informativ oversættelse. Ved afvigelser er den kanoniske engelske tekniske eller juridiske kilde gældende. Kun `LICENSE` og `NOTICE` i roden er juridisk autoritative; oversættelsen erstatter dem ikke.

## Hvad kalder vi en enhedstest? / Hvad er en enhedstest?

Ja: Enhedstesten kontrollerer en enkelt klasse eller regel isoleret, eksternt
uden netværk, database og live VIES. Hurtig, deterministisk og i enhver bygning
kan løbe.

Ja: en enhedstest verificerer én klasse eller regel isoleret, uden eksternt netværk,
database eller live VIES. Den er hurtig, deterministisk og egnet til enhver bygning.

Dette modul kræver også **lokal integration og konkurrencetest**
også fordi timeout, genforsøg, enkeltflyvning, annuller og`close()`adfærd er mere
det fremgår af komponentens samarbejde. De bruger en loopback mock-server, nej
ringe til EU-tjenesten.

Dette modul har også brug for **lokal integration og samtidighedstest**, fordi timeout,
genforsøg, enkeltflyvning, aflysning og nedlukning involverer flere komponenter. Disse
tests bruger en loopback mock-server og ringer aldrig til den offentlige EU-tjeneste.

## Hurtige kommandoer / Hurtige kommandoer

Fuld deterministisk testpakke / Fuld deterministisk suite:

```bash
./mvnw test
```

Kun enhedstest:

```bash
./mvnw -Dtest=VatFormatTest,ViesRequesterTest,ViesResponseMappingTest,ViesErrorTest,ViesAvailabilityTest,ViesClientBuilderTest,MiniJsonTest,TtlCacheTest test
```

Kun lokale HTTP/samtidighedstests / Lokal HTTP og kun samtidighed:

```bash
./mvnw -Dtest=ViesClientHttpTest test
```

Ren verifikation med JAR/Javadoc generation / Ren verifikation med artefakter:

```bash
./mvnw clean verify
./mvnw package
```

Én specifik test / én testmetode:

```bash
./mvnw -Dtest=ViesClientHttpTest#cancellationDoesNotLeakAsyncCapacity test
```

## Nuværende dækning / Nuværende dækning

Den nuværende deterministiske pakke indeholder **73 test**:

- **44 enheder test** i otte klasser;
- **29 lokale HTTP/integration/samtidig test**;
- nul obligatoriske eksterne netværksopkald.

Den deterministiske suite indeholder **73 test**: 44 enhedstest, 29 lokale
HTTP/integration/samtidighedstests og ingen obligatoriske eksterne opkald.

## Enhedstestkatalog / Enhedstestkatalog

###`VatFormatTest`— 8 tests

| ID       | Ungarsk mål                                        | Engelsk formål                                 |
| -------- | -------------------------------------------------- | ---------------------------------------------- |
| U-FMT-01 | Normalisering af det samlede skattetal             | Normaliser den fulde momsidentifikator         |
| U-FMT-02 | Fjern mellemrum/punktum/bindestreg, brug stort     | Stripseparatorer og store bogstaver            |
| U-FMT-03 | `GR`→`EL`kortlægning                               | Kort Grækenland`GR`til VIES`EL`                |
| U-FMT-04 | Afvis null, blank, ukendt land og forkert længde   | Afvis null/blank/ukendt/dårlig længde          |
| U-FMT-05 | Repræsentative landeformularer                     | Repræsentative landeformater                   |
| U-FMT-06 | Separat landekode+nummer API, vedhæftet præfiks    | Par API med vedhæftet præfiks                  |
| U-FMT-07 | Understøttede landekoder                           | Understøttet landesæt                          |
| U-FMT-08 | Alle 28 understøttede lande har mindst én form for | Mindst én form for alle 28 understøttede koder |

###`ViesRequesterTest`— 4 tests

| ID       | Ungarsk mål                                      | Engelsk formål                        |
| -------- | ------------------------------------------------ | ------------------------------------- |
| U-REQ-01 | Udfyldes fra egen skattenummeranmoder            | Opret anmoder fra fuld moms           |
| U-REQ-02 | Kanonisering af græsk ansøger                    | Kanonaliser græsk anmoder             |
| U-REQ-03 | Parret konstruktør med vedhæftet præfiks         | Par konstruktør med matchende præfiks |
| U-REQ-04 | Øjeblikkelig afvisning af fejlbehæftet rekvirent | Mislykkes hurtigt på ugyldig anmoder  |

###`ViesResponseMappingTest`— 11 tests

| ID       | Ungarsk mål                                             | Engelsk formål                               |
| -------- | ------------------------------------------------------- | -------------------------------------------- |
| U-MAP-01 | GET-stil`Valid`med alle felter                          | Kort GET-stil Gyldigt svar                   |
| U-MAP-02 | POST-stil`Valid`,`---` pladsholder                      | Kort POST-stil Gyldig og pladsholdere        |
| U-MAP-03 | Hiteles`Invalid`                                        | Kort autoritativt Ugyldigt                   |
| U-MAP-04 | Atmeneti hiba →`Unavailable`                            | Kort forbigående fejl til Utilgængelig       |
| U-MAP-05 | Inputfejl →`MalformedInput`                             | Kortlæg VIES-inputfejl                       |
| U-MAP-06 | Afvis ikke-objekt JSON                                  | Afvis ikke-objekt JSON                       |
| U-MAP-07 | Manglende boolean kan ikke være`Invalid`                | Manglende boolean bliver aldrig ugyldig      |
| U-MAP-08 | A Boolean string cannot become `Invalid`                | Ikke-boolesk gyldighed afvist                |
| U-MAP-09 | Vi finder ikke lokal tid for en manglende revisionsdato | Opfind aldrig manglende revisionstidsstempel |
| U-MAP-10 | Afvis forkert revisionsdato                             | Afvis ugyldig revisionstidsstempel           |
| U-MAP-11 | Korrekt konvertering af offset tidsstempel til UTC      | Parse offset tidsstempel til UTC             |

###`ViesErrorTest`— 6 tests

| ID       | Ungarsk mål                                           | Engelsk formål                                       |
| -------- | ----------------------------------------------------- | ---------------------------------------------------- |
| U-ERR-01 | Ungarsk+engelsk netværksmeddelelse                    | Tosproget netværksfejl                               |
| U-ERR-02 | Input errors are not retried                          | Indtastningsfejl er permanent                        |
| U-ERR-03 | HTTP 408/429/5xx prøv igen-besorolása                 | HTTP-forsøg igen klassificering                      |
| U-ERR-04 | `Valid`/`Invalid`eller fejl                           | Beslutninger viser ingen fejl                        |
| U-ERR-05 | Alle offentlige koder HU+EN og stabil genforsøgsværdi | Enhver offentlig kode har HU+EN og genforsøgspolitik |
| U-ERR-06 | Behold ukendt kode uden at prøve storm igen           | Bevar ukendt kode uden at prøve storm igen           |

###`ViesAvailabilityTest`— 2 tests

- beskyttelseskopi og uforanderlighed af inputkortet;
- øjeblikkelig afvisning af et null medlemslandskort.

Defensiv, uforanderlig snapshot-kopi og null-map-konstruktørvalidering.

###`MiniJsonTest`— 4 tests

- typisk VIES-dokument og Unicode-escape;
- indlejret objekt/liste/skalær/nummer/nul;
- JSON escape-ek;
- afvisning af ukorrekt, trunkeret og efterfølgende input.

Typisk VIES JSON, indlejrede/skalære værdier, escapes og fejllukket forkert udformet input.

###`TtlCacheTest`— 6 tests

- hit før TTL, miss efter;
- nøjagtig TTL-grænse;
- ignorere ikke-positiv TTL;
- konfigureret størrelsesgrænse;
- foretrukket forskydning af et udløbet element;
- 32 virtuelle tråde samtidig skrive/læse.

TTL-adfærd, nøjagtig udløbsgrænse, størrelseskontrol, udløbet-først prøveudtagning og
samtidig tryk fra 32 virtuelle tråde.

###`ViesClientBuilderTest`— 3 tests

- komplet højbelastningskonfiguration kan bygges;
- afvis forkert URL/grænse/forsøg igen;
- afvisning af nul, negativ og overløb Varighed.

Gyldig højbelastningskonfiguration plus URL, grænse, forsøg igen, varighed og overløb
validering.

## Lokal HTTP, samtidighed og livscyklus / Lokal integration og samtidighed

`ViesClientHttpTest`— 29 tests på en tilfældig fri loopback-port:

| ID | Test case |
| ---- | -------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------ |
| I-01 | 503 genforsøg to gange, derefter succes / to 503 genforsøg derefter succes |
| C-01 | 200 asynkrone opkaldere med samme tast → nøjagtig 1 HTTP-anmodning / 200 asynkrone opkaldere med samme tast → én anmodning |
| C-02 | Aktive HTTP-anmodninger overskrider ikke grænsen på 4 / aktive opkald forbliver inden for 4 |
| C-03 | Asynkronisering afventer over grænsen øjeblikkelig`CLIENT_OVERLOADED` |
| C-04 | Fremtidig annullering lækker ikke tillader, kapacitet er genoprettet |
| C-05 | `close()`from async callback does not deadlock |
| C-06 | `admissionTimeout`begrænser kødannelse |
| I-02 | Redis/cache-læsefejl →`CACHE_ERROR`, null VIES-kald |
| C-07 | Tilladelse og enkeltflyvning | frigives før på hinanden følgende asynkrone opkald |
| C-08 | Asynkron cache/luk race →`CLIENT_CLOSED`, nulla HTTP |
| C-09 | Synkroniser cache/luk race →`CLIENT_CLOSED`, nulla HTTP |
| C-10 | Custom executor-job er afbrudt, men executoren lukker ikke |
| C-11 | Synkroniseringsleder og følger får det samme resultat`CLIENT_CLOSED` |
| C-12 | 100 identiske asynkrone følgere bruger ikke 100 ventende slots |
| C-13 | Efter afvisning af eksekutør, genoprettes tilladelse og status under flyvning |
| C-14 | Synkroniseringsleder + asynkronfølger → én HTTP-anmodning |
| C-15 | Andet cache-tjek lukker stale-miss race, null HTTP |
| C-16 | Ved afslutning under cacheskrivning er resultatet af synkroniseringsleder/følger det samme |
| C-17 | Den afventende tilladelse | frigives også før et kædet asynkront opkald med forskellige taster |
| C-18 | En tilpasset eksekveringsopgave i kø er faktisk annulleret ved at lukke |
| C-19 | Blokering af brugertilbagekald blokerer ikke luk/livscykluslås |
| C-20 | Async leader + sync follower → én HTTP-anmodning |
| C-21 | `maxPendingSyncRequests`giver øjeblikkeligt begrænset modtryk |
| C-22 | Fatal async fortsætter til`Error`usædvanlig fremtid og ufanget handler |

## Hvad tester vi ikke som standard? / Hvad er ikke i standardpakken?

### Live VIES røgtest / Live VIES røgtest

Live-tjenesten er variabel og hastighedsbegrænset, så der kan ikke være en obligatorisk CI-betingelse.
Manuel røgtest er højst en`availability()`og en kendt, ikke-hemmelig el
forespørg på skattenummeret fra en miljøvariabel, concurrency=1 og genforsøg=0
indstilling.

Live VIES er variabel og hastighedsbegrænset, så den må ikke gate normal CI. En opt-in
røgtest bør højst udføre én tilgængelighedskontrol og én validering, med
concurrency=1 og genforsøg=0. Forpligt eller log aldrig private anmoder-momsnumre.

### Belastningstest / Belastningstest

Kør altid mod en lokal hån eller din egen iscenesættelsestjeneste, aldrig den offentlige
mod VIES. Absolutte krav er informative; korrekthed, grænse, p95/p99 og fejlkoder
portene.

Mål altid mod en lokal mock eller ejet iscenesættelsestjeneste, aldrig offentlige VIES. Absolut
anmodninger/sekund er informativ; korrekthed, grænser, p95/p99 og fejlsemantik
er portene.

Anbefalede tilfælde:

- mange forskellige nøgler;
- 100.000 hot-key opkald kis kulcshalmazon / hot-key stampede;
- overbelastning og genvinding over grænsen;
- 200/429/503/timeout/misformet blandet respons;
- cache-tryk omkring det konfigurerede max.

### Soak and chaos test / Soak and chaos test

30-60 minutter fast belastning, begrænset heap, JFR, gentagen luk/genstart, latency
spike, nulstilling af forbindelse, cachefejl og annullering. Dyngen, aktive tråde,
antal under flyvning/afventende og sockets plateau; der må ikke være blokeringer eller tillade utætheder.

Kør 30-60 minutter med en fast heap, JFR, gentagen livscyklus, latency spikes,
nulstilling af forbindelse, cachefejl og annullering. Dynge/tråde/under flyvning/stikkontakter
skal plateau uden dødvande eller tillade utætheder.

JFR-eksempel / JFR-eksempel:

```bash
JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=target/vies-soak.jfr,settings=profile" ./mvnw test
```

## KUN recommendation / KUN anbefales

Minimum pipeline:

```bash
./mvnw --batch-mode clean verify
```

Anbefalet matrix: mindst JDK 21 LTS; yderligere JDK'er kan kun indstilles derefter
understøttes, hvis den samme suite rent faktisk kører på dem.

Anbefalet matrix: mindst JDK 21 LTS. Få først yderligere JDK-support efter
kører den samme suite på disse versioner.

## Regressionsregel / Regressionsregel

Hver rettet fejl skal have en deterministisk test, der er rettelsen
ville have fejlet før. Lås/barriere bør prioriteres ved konkurrencetest
synkronisering; fast`sleep`kan kun være en kort polling backoff, ikke et orakel af korrekthed.

Hver rettet fejl skal modtage en deterministisk test, der mislykkedes før rettelsen.
Foretrækker låse/barrierer til samtidighedstest; faste søvner kan være korte afstemninger
kun backoff, aldrig korrekthedsoraklet.
