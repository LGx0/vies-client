# Dansk (da) — CONTRIBUTING

> [Alle sprog](../../LANGUAGES.md) · Informativ oversættelse. Ved afvigelser er den kanoniske engelske tekniske eller juridiske kilde gældende. Kun `LICENSE` og `NOTICE` i roden er juridisk autoritative; oversættelsen erstatter dem ikke.

Tak for at forbedre`vies-client`-projektet. Målet er en forudsigelig,
afhængighedsfri og sikker Java VIES-klient selv under hård belastning.

Tak for at forbedre`vies-client`. Projektet har til formål at forblive forudsigeligt,
afhængighedsfri ved kørsel og sikker under høj samtidighed.

## Før start / Før start

- For fejlretning skal du åbne et problem eller henvise til et eksisterende.
- Lad os diskutere større API-, licens- eller arkitekturændringer først i et problem.
- Rapporter ikke en sikkerhedsfejl i et offentligt spørgsmål; se [SECURITY.md](SECURITY.md).
- Åbn eller referer til et problem for fejlrettelser.
- Diskuter større API-, licens- eller arkitekturændringer før implementering.
- Afslør aldrig en sårbarhed i et offentligt spørgsmål; følg [SECURITY.md](SECURITY.md).

## Udviklingsmiljø

Kræver: JDK 21+, Maven 3.9+, Git. Kontrollere:

```bash
java -version
javac -version
./mvnw -version
```

Fuld lokal bekræftelse:

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
```

Detaljeret installation: [docs/INSTALLATION.md](INSTALLATION.md).

## Udviklingsproces / Udviklingsarbejdsgang

1. Fork repo'en, og opret derefter en kortvarig gren fra`main`-grenen.
2. Lav en lille, målfokuseret ændring.
3. Tilføj en deterministisk regressionstest for hver fejlrettelse.
4. Kør den fulde`./mvnw clean verify`-kommando.
5. Åbn en pull request og udfyld alle relevante dele af skabelonen.

6. Fork repository og opret en kortvarig gren fra`main`.
7. Hold ændringer små og fokuserede.
8. Tilføj en deterministisk regressionstest for hver fejlrettelse.
9. Kør den fulde Maven-verifikation.
10. Åbn en pull-anmodning, og udfyld den medfølgende skabelon.

Foreslåede filialnavne:

```text
fix/close-race
feat/cache-adapter-hook
docs/redis-example
```

## Kodningsregler

- Java 21 sprogniveau; den offentlige API bør forblive lille og typesikker.
- Runtime-modulet bør forblive uden eksterne afhængigheder, medmindre der træffes en separat beslutning.
- `Unavailable`teknisk usikkerhed, kan aldrig konverteres til`Invalid`resultat.
- Alle delte tilstande bør være trådsikre og hukommelsesbegrænset.
- Engelsk og ungarsk Javadoc/kommentarer er påkrævet for offentlig API og kompleks samtidig logik.
- Log ikke fuldt skattenummer, firmanavn, adresse eller eget rekvirent skattenummer ind som testdata.
- Brug Java 21 og hold den offentlige API lille og typesikker.
- Hold runtime-modulet afhængighedsfrit, medmindre andet er udtrykkeligt aftalt.
- Konverter aldrig`Unavailable`til`Invalid`.
- Delt tilstand skal være trådsikker og hukommelsesbegrænset.
- Dokumenter offentlig API og ikke-indlysende samtidighedslogik på engelsk og ungarsk.
- Undlad at forpligte eller logge private moms-, virksomheds-, adresse- eller anmoderdata.

## Testregler / Testregler

- Enhedstest bør ikke bruge et offentligt netværk.
- HTTP og konkurrencetests kan kun kalde en loopback mock-server.
- Belastnings-, stress- eller CI-tests mod offentlige VIES er forbudt.
- Race situation bør kontrolleres af låse/barriere; fast`Thread.sleep`kan ikke være et orakel af korrekthed.
- Testen skulle mislykkes uden rettelsen.
- Enhedstest må ikke bruge det offentlige netværk.
- HTTP og samtidighedstest må kun kalde en loopback mock-server.
- Kør aldrig belastning, stress eller påkrævede CI-tests mod offentlige VIES.
- Kør løb med låse/barrierer; faste søvn er ikke korrekthed orakler.
- En regressionstest skal mislykkes uden den tilsvarende rettelse.

Testkatalog: [docs/TESTING.md](TESTING.md).

## Krav til Pull-anmodning / Pull-request-krav

PR'en er klar til gennemgang, hvis:

- konstruktionen og alle tests er vellykkede;
- offentlig adfærd er dokumenteret;
- ingen ny, uberettiget afhængighed;
- kompatibilitetsbruddet er fremhævet separat;
- effektjustering inkluderer reproducerbar lokal måling;
- forfatteren er berettiget til at indsende koden.

En PR er klar, når byggeriet er grønt, offentlig adfærd er dokumenteret, nyt
afhængigheder er berettigede, brydende ændringer er eksplicitte, præstationskrav er
reproducerbar, og forfatteren har ret til at indsende værket.

## AI-assisterede ændringer / AI-assisterede ændringer

AI kan bruges, men indsenderen tager det fulde ansvar for resultatet. Hver linje
skal kontrolleres, testes og gennemgås med henblik på licens. Betydelig AI-
angive bidrag i PR; leverer ikke fortrolige eller tredjepartsbeskyttede data
model og indsend ikke ubekræftet genereret kode.

AI-værktøjer er tilladt, men bidragsyderen forbliver fuldt ud ansvarlig. Gennemgå og test
hver linje, verificere herkomst og licensering, afsløre betydelig AI-hjælp i
PR, og indsend aldrig fortroligt eller ikke-gennemgået genereret materiale.

## Licens / Licens

Projektet er licenseret under Apache-2.0. Samtykke ved bevidst at sende en pull-anmodning
indsendes ubetinget i henhold til afsnit 5 i Apache-licensen 2.0,
medmindre afsenderen udtrykkeligt angiver andet.

Projektet bruger Apache-2.0. Ved bevidst at indsende et bidrag, indsender du
det under afsnit 5 i Apache-licensen 2.0 uden yderligere vilkår, medmindre du
udtrykkeligt angive andet.

## Adfærd/adfærd

Alle deltagere er underlagt [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).
Alle deltagere skal følge [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).
