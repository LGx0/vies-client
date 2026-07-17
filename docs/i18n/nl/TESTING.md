# Nederlands (nl) — TESTING

> [Alle talen](../../LANGUAGES.md) · Informatieve vertaling. Bij verschillen is de canonieke Engelse technische of juridische bron leidend. Alleen `LICENSE` en `NOTICE` in de hoofdmap zijn juridisch gezaghebbend; deze vertaling vervangt ze niet.

## Hoe noemen we een eenheidstest? / Wat is een eenheidstest?

Ja: de unittest controleert een enkele klasse of regel afzonderlijk, extern
zonder netwerk, database en live VIES. Snel, deterministisch en in elke build
kan rennen.

Ja: een unittest verifieert één klasse of regel afzonderlijk, zonder extern netwerk,
database, of live VIES. Het is snel, deterministisch en geschikt voor elke build.

Voor deze module zijn ook **lokale integratie- en concurrentietests** vereist
ook omdat het gedrag time-out, opnieuw proberen, enkele vlucht, annuleren en`close()`meer is
het blijkt uit de medewerking van het onderdeel. Ze gebruiken een loopback-nepserver, nee
bel de EU-service.

Deze module heeft ook **lokale integratie- en gelijktijdigheidstests** nodig, omdat er een time-out optreedt,
opnieuw proberen, enkele vlucht, annulering en afsluiten omvatten meerdere componenten. Deze
tests gebruiken een loopback-nepserver en bellen nooit de openbare EU-dienst.

## Snelle opdrachten / Snelle opdrachten

Volledig deterministisch testpakket / Volledig deterministisch pakket:

```bash
./mvnw test
```

Alleen eenheidstests:

```bash
./mvnw -Dtest=VatFormatTest,ViesRequesterTest,ViesResponseMappingTest,ViesErrorTest,ViesAvailabilityTest,ViesClientBuilderTest,MiniJsonTest,TtlCacheTest test
```

Alleen lokale HTTP/gelijktijdigheidstests / Alleen lokale HTTP en gelijktijdigheid:

```bash
./mvnw -Dtest=ViesClientHttpTest test
```

Schone verificatie met JAR/Javadoc-generatie / Schone verificatie met artefacten:

```bash
./mvnw clean verify
./mvnw package
```

Eén specifieke test / Eén testmethode:

```bash
./mvnw -Dtest=ViesClientHttpTest#cancellationDoesNotLeakAsyncCapacity test
```

## Huidige dekking / Huidige dekking

Het huidige deterministische pakket bevat **73 tests**:

- **44 eenheidstest** in acht klassen;
- **29 lokale HTTP-/integratie-/gelijktijdigheidstests**;
- nul verplichte externe netwerkoproepen.

De deterministische suite bevat **73 tests**: 44 eenheidstests, 29 lokale
HTTP-/integratie-/gelijktijdigheidstests en nul verplichte externe oproepen.

## Unit-testcatalogus / Unit-testcatalogus

###`VatFormatTest`— 8 tests

| Identiteitskaart | Hongaars doelpunt                                          | Engels doel                                         |
| ---------------- | ---------------------------------------------------------- | --------------------------------------------------- |
| U-FMT-01         | Normalisatie van het totale belastingnummer                | Normaliseer de volledige BTW-identificatie          |
| U-FMT-02         | Verwijder spatie/punt/koppelteken, hoofdletter             | Stripscheiders en hoofdletters                      |
| U-FMT-03         | `GR`→`EL`-toewijzing                                       | Kaart Griekenland`GR`naar VIES`EL`                  |
| U-FMT-04         | Null, blanco, onbekend land en onjuiste lengte afwijzen    | Null/leeg/onbekend/slechte lengte afwijzen          |
| U-FMT-05         | Formulieren voor representatieve landen                    | Representatieve landenformaten                      |
| U-FMT-06         | Aparte landcode+nummer-API, bijgevoegd voorvoegsel         | Koppel API met bijgevoegd voorvoegsel               |
| U-FMT-07         | Ondersteunde landcodes                                     | Ondersteunde landenset                              |
| U-FMT-08         | Alle 28 ondersteunde landen hebben ten minste één vorm van | Ten minste één vorm voor alle 28 ondersteunde codes |

###`ViesRequesterTest`— 4 testen

| Identiteitskaart | Hongaars doelpunt                                 | Engels doel                                       |
| ---------------- | ------------------------------------------------- | ------------------------------------------------- |
| U-REQ-01         | Invullen vanuit eigen belastingnummeraanvrager    | Aanvrager aanmaken op basis van volledige BTW     |
| U-REQ-02         | Heiligverklaring van Griekse aanvrager            | De Griekse aanvrager heilig verklaren             |
| U-REQ-03         | Gekoppelde constructor met bijgevoegd voorvoegsel | Koppel de constructor met bijbehorend voorvoegsel |
| U-REQ-04         | Onmiddellijke afwijzing van foutieve aanvrager    | Faal snel bij ongeldige aanvrager                 |

###`ViesResponseMappingTest`— 11 tests

| Identiteitskaart | Hongaars doelpunt                                          | Engels doel                                             |
| ---------------- | ---------------------------------------------------------- | ------------------------------------------------------- |
| U-MAP-01         | GET-stijl`Valid`met alle velden                            | Kaart GET-stijl Geldig antwoord                         |
| U-MAP-02         | POST-stijl`Valid`,`---`tijdelijke aanduiding               | Kaart POST-stijl Geldig en tijdelijke aanduidingen      |
| U-MAP-03         | Hiteles`Invalid`                                           | Kaart gezaghebbend Ongeldig                             |
| U-MAP-04         | Átmeneti hiba →`Unavailable`                               | Wijs een tijdelijke fout toe aan Niet beschikbaar       |
| U-MAP-05         | Invoerhiba →`MalformedInput`                               | VIES-invoerfout in kaart brengen                        |
| U-MAP-06         | Niet-object-JSON afwijzen                                  | Niet-object-JSON afwijzen                               |
| U-MAP-07         | Ontbrekende booleaanse waarde kan niet`Invalid`            | zijn Ontbrekende booleaanse waarde wordt nooit ongeldig |
| U-MAP-08         | A Boolean string cannot become `Invalid`                   | Niet-Booleaanse geldigheid afgewezen                    |
| U-MAP-09         | We vinden geen lokale tijd voor een ontbrekende auditdatum | Bedenk nooit een ontbrekend audittijdstempel            |
| U-MAP-10         | Onjuiste auditdatum afwijzen                               | Ongeldige audittijdstempel afwijzen                     |
| U-KAART-11       | Correcte conversie van offset-tijdstempel naar UTC         | Parseer de offset-tijdstempel naar UTC                  |

###`ViesErrorTest`— 6 tests

| Identiteitskaart | Hongaars doelpunt                                        | Engels doel                                                        |
| ---------------- | -------------------------------------------------------- | ------------------------------------------------------------------ |
| U-ERR-01         | Hongaars+Engels netwerkbericht                           | Tweetalige netwerkfout                                             |
| U-ERR-02         | Input errors are not retried                             | Invoerfout is permanent                                            |
| U-ERR-03         | HTTP 408/429/5xx opnieuw proberen                        | Classificatie van HTTP-pogingen                                    |
| U-ERR-04         | `Valid`/`Invalid`noch fout                               | Beslissingen leggen geen fouten bloot                              |
| U-ERR-05         | Alle publieke codes HU+EN en stabiele herhalingswaarde   | Elke openbare code heeft HU+EN en een beleid voor opnieuw proberen |
| U-ERR-06         | Onbekende code behouden zonder storm opnieuw te proberen | Behoud onbekende code zonder storm opnieuw te proberen             |

###`ViesAvailabilityTest`— 2 testen

- kopieerbeveiliging en onveranderlijkheid van de invoerkaart;
- onmiddellijke afwijzing van de nullidstaatkaart.

Defensieve onveranderlijke snapshot-kopie en validatie van null-map-constructor.

###`MiniJsonTest`— 4 testen

- typisch VIES-document en Unicode-escape;
- genest object/lijst/scalair/getal/null;
- JSON escape-ek;
- afwijzing van onjuiste, ingekorte en achterblijvende invoer.

Typische VIES JSON, geneste/scalaire waarden, escapes en foutief gesloten invoer.

###`TtlCacheTest`— 6 tests

- hit vóór TTL, miss na;
- exacte TTL-limiet;
- negeer niet-positieve TTL;
- geconfigureerde groottelimiet;
- voorkeursverplaatsing van een verlopen element;
- 32 virtuele threads gelijktijdig schrijven/lezen.

TTL-gedrag, exacte vervalgrens, groottecontrole, eerst verlopen bemonstering, en
gelijktijdige druk van 32 virtuele threads.

###`ViesClientBuilderTest`— 3 tests

- volledige configuratie voor hoge belasting kan worden gebouwd;
- verkeerde URL/limiet/opnieuw proberen afwijzen;
- afwijzing van nul-, negatieve en overloopduur.

Geldige configuratie voor hoge belasting plus URL, limiet, nieuwe poging, duur en overloop
geldigmaking.

## Lokale HTTP, gelijktijdigheid en levenscyclus / Lokale integratie en gelijktijdigheid

`ViesClientHttpTest`— 29 tests op een willekeurige vrije loopback-poort:

| Identiteitskaart | Testcase |
| ---------------- | -------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------- |
| I-01 | 503 nieuwe pogingen tweemaal, daarna succes / twee 503 nieuwe pogingen, daarna succes |
| C-01 | 200 asynchrone bellers met dezelfde sleutel → precies 1 HTTP-verzoek / 200 asynchrone bellers met dezelfde sleutel → één verzoek |
| C-02 | Actieve HTTP-verzoeken overschrijden de limiet van 4 niet / actieve oproepen blijven binnen 4 |
| C-03 | Async in behandeling boven limiet onmiddellijk`CLIENT_OVERLOADED` |
| C-04 | Toekomstige annulering lekt niet, de capaciteit is hersteld |
| C-05 | `close()`asynchrone callback zonder deadlock |
| C-06 | `admissionTimeout`beperkt wachtrijen |
| I-02 | Leesfout redis/cache →`CACHE_ERROR`, null VIES-oproep |
| C-07 | Vergunning en enkele vlucht | worden vrijgegeven vóór opeenvolgende asynchrone oproepen |
| C-08 | Asynchrone cache/close race →`CLIENT_CLOSED`, nulla HTTP |
| C-09 | Cache synchroniseren/race sluiten →`CLIENT_CLOSED`, nulla HTTP |
| C-10 | Aangepaste uitvoerdertaak wordt onderbroken, maar de uitvoerder sluit niet |
| C-11 | Synchronisatieleider en volger krijgen hetzelfde resultaat`CLIENT_CLOSED` |
| C-12 | 100 identieke asynchrone volgers verbruiken niet 100 openstaande slots |
| C-13 | Na afwijzing door de executeur wordt de vergunning en de vluchtstatus hersteld |
| C-14 | Synchronisatieleider + asynchrone volger → één HTTP-verzoek |
| C-15 | Tweede cachecontrole sluit muffe-miss-race, null HTTP |
| C-16 | Bij het afsluiten tijdens het schrijven in de cache is het resultaat van de synchronisatieleider/volger hetzelfde |
| C-17 | De aangevraagde vergunning | wordt ook vrijgegeven vóór een gekoppelde asynchrone oproep met verschillende toetsen |
| C-18 | Een aangepaste uitvoerdertaak in de wachtrij wordt feitelijk geannuleerd door close |
| C-19 | Het blokkeren van het terugbellen van gebruikers blokkeert niet de afsluiting/levenscyclusvergrendeling |
| C-20 | Asynchrone leider + synchronisatievolger → één HTTP-verzoek |
| C-21 | `maxPendingSyncRequests`geeft onmiddellijk beperkte tegendruk |
| C-22 | Fatale async blijft`Error`uitzonderlijke toekomstige en niet-gevangen handler |

## Wat testen we standaard niet? / Wat zit er niet in de standaardsuite?

### Live VIES-rooktest / Live VIES-rooktest

De live service is variabel en heeft een snelheidsbeperking, er kan dus geen sprake zijn van een verplichte CI-voorwaarde.
Handmatige rooktest is maximaal één`availability()`en een bekende, niet-geheime of
vraag het belastingnummer op dat is verkregen uit een omgevingsvariabele, concurrency=1 en nieuwe pogingen=0
instelling.

Live VIES is variabel en heeft een snelheidsbeperking, dus het mag geen normale CI doorlaten. Een opt-in
De rooktest mag maximaal één beschikbaarheidscontrole en één validatie uitvoeren
gelijktijdigheid=1 en nieuwe pogingen=0. Leg nooit BTW-nummers van privéaanvragers vast en log ze nooit in.

### Belastingstest / Belastingstest

Strijd altijd tegen een lokale spot of je eigen ensceneringsdienst, nooit tegen de publieke
tegen VIES. Absolute vereisten zijn informatief; correctheid, limiet, p95/p99 en foutcodes
de poorten.

Richt u altijd op een lokale nep- of eigen staging-service, nooit op openbare VIES. Absoluut
verzoeken/tweede is informatief; correctheid, grenzen, p95/p99 en foutsemantiek
zijn de poorten.

Aanbevolen gevallen:

- veel verschillende toetsen;
- 100k sneltoets beller kis kulcshalmazon / sneltoets stormloop;
- overbelasting en herstel boven de limiet;
- 200/429/503/time-out/misvormde gemengde respons;
- cachedruk rond de geconfigureerde max.

### Soak- en chaos-test / Soak- en chaos-test

30-60 minuten vaste belasting, beperkte heap, JFR, herhaald sluiten/herstarten, latentie
piek, verbindingsreset, cachefout en annulering. De hoop, actieve draden,
aantal in-flight/in behandeling zijnde en stopcontactenplateau; er mogen geen impasses zijn of lekkages van vergunningen.

Voer 30-60 minuten uit met een vaste heap, JFR, herhaalde levenscyclus, latentiepieken,
verbindingsresets, cachefouten en annulering. Hoop/draden/in-flight/contactdozen
moet plat worden zonder dat er een impasse ontstaat of lekken toestaan.

JFR-voorbeeld / JFR-voorbeeld:

```bash
JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=target/vies-soak.jfr,settings=profile" ./mvnw test
```

## ALLEEN recommendation / ALLEEN aanbevolen

Minimale pijplijn:

```bash
./mvnw --batch-mode clean verify
```

Aanbevolen matrix: minimaal JDK 21 LTS; Alleen dan kunnen extra JDK's worden ingesteld
ondersteund als dezelfde suite er daadwerkelijk op draait.

Aanbevolen matrix: minimaal JDK 21 LTS. Claim pas daarna aanvullende JDK-ondersteuning
dezelfde suite op die versies uitvoeren.

## Regressieregel / Regressieregel

Elke opgeloste bug zou een deterministische test moeten hebben die de oplossing is
eerder zou hebben gefaald. Klink/barrière moet de prioriteit zijn bij een wedstrijdtest
synchronisatie; opgelost`sleep`kan slechts een kort uitstel van de peiling zijn, geen orakel van correctheid.

Elke opgeloste bug moet een deterministische test ondergaan die vóór de oplossing mislukte.
Geef de voorkeur aan grendels/barrières voor gelijktijdigheidstests; vaste slaapplaatsen kunnen korte peilingen zijn
alleen uitstel, nooit het juistheidsorakel.
