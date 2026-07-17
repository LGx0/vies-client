# Svenska (sv) — CONTRIBUTING

> [Alla språk](../../LANGUAGES.md) · Informativ översättning. Vid avvikelser gäller den kanoniska engelska tekniska eller juridiska källan. Endast `LICENSE` och `NOTICE` i roten är juridiskt auktoritativa; översättningen ersätter dem inte.

Tack för att du förbättrade`vies-client`-projektet. Målet är en förutsägbar,
beroendefri och säker Java VIES-klient även under tung belastning.

Tack för att du förbättrar`vies-client`. Projektet syftar till att förbli förutsägbart,
beroendefri under körning och säker under hög samtidighet.

## Före start / Före start

- För felkorrigering, öppna ett problem eller hänvisa till ett befintligt.
  – Låt oss diskutera stora API-, licens- eller arkitekturändringar först i en fråga.
- Rapportera inte ett säkerhetsfel i en offentlig fråga; se [SECURITY.md](SECURITY.md).
- Öppna eller referera till ett problem för buggfixar.
- Diskutera större API-, licens- eller arkitekturändringar innan implementering.
- Avslöja aldrig en sårbarhet i en offentlig fråga; följ [SECURITY.md](SECURITY.md).

## Utvecklingsmiljö

Kräver: JDK 21+, Maven 3.9+, Git. Kontrollera:

```bash
java -version
javac -version
./mvnw -version
```

Fullständig lokal verifiering:

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
```

Detaljerad installation: [docs/INSTALLATION.md](INSTALLATION.md).

## Utvecklingsprocess / Utvecklingsarbetsflöde

1. Dela repet och skapa sedan en kortlivad gren från`main`-grenen.
2. Gör en liten, målfokuserad förändring.
3. Lägg till ett deterministiskt regressionstest för varje buggfix.
4. Kör hela kommandot`./mvnw clean verify`.
5. Öppna en pull-begäran och fyll i alla relevanta delar av mallen.

6. Dela förvaret och skapa en kortlivad gren från`main`.
7. Håll förändringar små och fokuserade.
8. Lägg till ett deterministiskt regressionstest för varje buggfix.
9. Kör hela Maven-verifieringen.
10. Öppna en pull-begäran och fyll i den medföljande mallen.

Föreslagna filialnamn:

```text
fix/close-race
feat/cache-adapter-hook
docs/redis-example
```

## Kodningsregler

- Java 21 språknivå; det offentliga API:et bör förbli litet och typsäkert.
- Runtime-modulen bör förbli utan externa beroenden, om inte ett separat beslut fattas.
- `Unavailable`teknisk osäkerhet, kan aldrig konverteras till`Invalid`resultat.
- Alla delade tillstånd bör vara trådsäkra och minnesbegränsat.
- Engelska och ungerska Javadoc/kommentarer krävs för offentliga API och komplex samtidig logik.
- Logga inte in fullständigt skattenummer, företagsnamn, adress eller eget beställare skattenummer som testdata.
- Använd Java 21 och håll det offentliga API:et litet och typsäkert.
- Håll runtime-modulen beroendefri om inte annat uttryckligen överenskommits.
- Konvertera aldrig`Unavailable`till`Invalid`.
- Delat tillstånd måste vara trådsäkert och minnesavgränsat.
- Dokumentera offentligt API och icke-uppenbar samtidighetslogik på engelska och ungerska.
- Förplikta eller logga inte privat moms-, företags-, adress- eller begärandedata.

## Testregler / Testregler

- Enhetstest ska inte använda ett publikt nätverk.
- HTTP och konkurrenstester kan bara anropa en loopback-mock-server.
- Belastnings-, stress- eller CI-tester mot offentliga VIES är förbjudna.
- Race situation bör kontrolleras av spärr/barriär; fix`Thread.sleep`kan inte vara ett orakel för korrekthet.
- Testet ska misslyckas utan fix.
  – Enhetstester får inte använda det publika nätet.
- HTTP- och samtidighetstester får bara anropa en loopback-mock-server.
- Kör aldrig belastning, stress eller obligatoriska CI-tester mot offentliga VIES.
- Kör lopp med spärrar/barriärer; fast sömn är inte korrekthet orakel.
- Ett regressionstest måste misslyckas utan motsvarande fix.

Testkatalog: [docs/TESTING.md](TESTING.md).

## Krav för Pull-begäran / Pull-request-krav

PR är redo för granskning om:

- konstruktionen och alla tester är framgångsrika;
- offentligt beteende är dokumenterat;
- inget nytt, omotiverat beroende;
- kompatibilitetsavbrottet markeras separat;
- effektjustering inkluderar reproducerbar lokal mätning;
- författaren har rätt att skicka in koden.

En PR är klar när bygget är grönt, offentligt beteende är dokumenterat, nytt
beroenden är motiverade, brytande förändringar är explicita, prestationsanspråk är det
reproducerbar, och upphovsmannen har rätt att lämna in verket.

## AI-assisterade ändringar / AI-assisterade ändringar

AI kan användas, men insändaren tar fullt ansvar för resultatet. Varje rad
måste kontrolleras, testas och granskas för licensiering. Betydande AI-
ange bidrag i PR; tillhandahåller inte konfidentiell eller tredjepartsskyddad data
modell och skicka inte in overifierad genererad kod.

AI-verktyg är tillåtna, men bidragsgivaren förblir fullt ansvarig. Granska och testa
varje rad, verifiera härkomst och licensiering, avslöja betydande AI-hjälp i
PR och skicka aldrig in konfidentiellt eller ogranskat genererat material.

## Licens / Licens

Projektet är licensierat under Apache-2.0. Samtycke genom att avsiktligt skicka en pull-förfrågan
skickas in ovillkorligt enligt avsnitt 5 i Apache-licensen 2.0,
om inte avsändaren uttryckligen anger annat.

Projektet använder Apache-2.0. Genom att avsiktligt skicka in ett bidrag skickar du
det enligt avsnitt 5 i Apache-licensen 2.0 utan ytterligare villkor om du inte
uttryckligen ange något annat.

## Beteende/uppförande

Alla deltagare är föremål för [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).
Alla deltagare måste följa [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).
