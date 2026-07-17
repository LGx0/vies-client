# Nederlands (nl) — CONTRIBUTING

> [Alle talen](../../LANGUAGES.md) · Informatieve vertaling. Bij verschillen is de canonieke Engelse technische of juridische bron leidend. Alleen `LICENSE` en `NOTICE` in de hoofdmap zijn juridisch gezaghebbend; deze vertaling vervangt ze niet.

Bedankt voor het verbeteren van het`vies-client`-project. Het doel is een voorspelbare,
afhankelijkheidsvrije en veilige Java VIES-client, zelfs onder zware belasting.

Bedankt voor het verbeteren van`vies-client`. Het project streeft ernaar voorspelbaar te blijven,
afhankelijkheidsvrij tijdens runtime en veilig bij hoge gelijktijdigheid.

## Voordat u begint / Voordat u begint

- Voor foutcorrectie opent u een probleem of verwijst u naar een bestaand probleem.
- Laten we eerst in een issue de belangrijkste API-, licentie- of architectuurwijzigingen bespreken.
- Rapporteer geen beveiligingsfout in een openbaar probleem; zie [BEVEILIGING.md](SECURITY.md).
- Open of verwijs naar een probleem voor bugfixes.
- Bespreek belangrijke API-, licentie- of architectuurwijzigingen vóór implementatie.
- Maak nooit een kwetsbaarheid in een publieke kwestie bekend; volg [SECURITY.md](SECURITY.md).

## Ontwikkelomgeving

Vereist: JDK 21+, Maven 3.9+, Git. Controle:

```bash
java -version
javac -version
./mvnw -version
```

Volledige lokale verificatie:

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
```

Gedetailleerde installatie: [docs/INSTALLATION.md](INSTALLATION.md).

## Ontwikkelingsproces / Ontwikkelingsworkflow

1. Fork de repository en maak vervolgens een kortstondige vertakking van de`main`-vertakking.
2. Breng een kleine, doelgerichte verandering door.
3. Voeg voor elke bugfix een deterministische regressietest toe.
4. Voer de volledige opdracht`./mvnw clean verify`uit.
5. Open een pull request en vul alle relevante delen van de template in.

6. Fork de repository en maak een kortstondige vertakking van`main`.
7. Houd veranderingen klein en doelgericht.
8. Voeg voor elke bugfix een deterministische regressietest toe.
9. Voer de volledige Maven-verificatie uit.
10. Open een pull-aanvraag en vul het meegeleverde sjabloon in.

Voorgestelde taknamen:

```text
fix/close-race
feat/cache-adapter-hook
docs/redis-example
```

## Coderingsregels

- Java 21 taalniveau; de publieke API moet klein en typeveilig blijven.
- De runtime-module moet zonder externe afhankelijkheden blijven, tenzij een afzonderlijke beslissing wordt genomen.
- `Unavailable`technische onzekerheid, kan nooit worden omgezet in`Invalid`resultaat.
- Alle gedeelde statussen moeten thread-safe en geheugenbeperkt zijn.
- Engelse en Hongaarse Javadoc/opmerkingen zijn vereist voor openbare API en complexe gelijktijdige logica.
- Log niet volledig belastingnummer, bedrijfsnaam, adres of eigen aanvrager belastingnummer in als testgegevens.
- Gebruik Java 21 en houd de openbare API klein en typeveilig.
- Houd de runtime-module afhankelijkheidsvrij, tenzij uitdrukkelijk anders overeengekomen.
- Converteer`Unavailable`nooit naar`Invalid`.
- De gedeelde status moet thread-safe en geheugengebonden zijn.
- Documenteer de openbare API en niet voor de hand liggende gelijktijdigheidslogica in het Engels en Hongaars.
- Leg geen privé-btw-, bedrijfs-, adres- of aanvragergegevens vast en registreer deze ook niet.

## Testregels / Testregels

- Unittest mag geen gebruik maken van een openbaar netwerk.
- HTTP- en concurrentietests kunnen alleen een loopback-nepserver aanroepen.
- Belasting-, stress- of CI-tests tegen openbare VIES zijn verboden.
- De racesituatie moet worden gecontroleerd door middel van een grendel/barrière; opgelost`Thread.sleep`kan geen orakel van correctheid zijn.
- De test zou moeten mislukken zonder de oplossing.
- Unittests mogen geen gebruik maken van het openbare netwerk.
- HTTP- en gelijktijdigheidstests mogen alleen een loopback-nepserver aanroepen.
- Voer nooit belasting-, stress- of vereiste CI-tests uit tegen openbare VIES.
- Rijd races met grendels/barrières; vaste slaapplaatsen zijn geen correctheidsorakels.
- Een regressietest moet mislukken zonder de bijbehorende oplossing.

Testcatalogus: [docs/TESTING.md](TESTING.md).

## Vereisten voor pull-aanvragen / vereisten voor pull-aanvragen

De PR is klaar voor beoordeling als:

- de build en alle tests zijn succesvol;
- publiek gedrag wordt gedocumenteerd;
- geen nieuwe, ongerechtvaardigde verslaving;
- de compatibiliteitsbreuk wordt afzonderlijk gemarkeerd;
- vermogensaanpassing omvat reproduceerbare lokale meting;
- de auteur heeft het recht om de code in te dienen.

Een PR is klaar als de build groen is, publiek gedrag is gedocumenteerd, nieuw
afhankelijkheden zijn gerechtvaardigd, fundamentele veranderingen zijn expliciet en prestatieclaims zijn dat wel
reproduceerbaar en de auteur heeft het recht het werk in te zenden.

## Door AI ondersteunde wijzigingen / Door AI ondersteunde wijzigingen

Er kan gebruik worden gemaakt van AI, maar de inzender neemt de volledige verantwoordelijkheid voor het resultaat. Elke lijn
moeten worden gecontroleerd, getest en beoordeeld voor licentieverlening. Aanzienlijke AI-
bijdrage in PR aangeven; verstrek geen vertrouwelijke of door derden beschermde gegevens
model en dien geen niet-geverifieerde gegenereerde code in.

AI-tools zijn toegestaan, maar de bijdrager blijft volledig verantwoordelijk. Beoordeel en test
elke regel, herkomst en licenties verifiëren, substantiële AI-hulp openbaar maken
de PR, en dien nooit vertrouwelijk of niet-beoordeeld gegenereerd materiaal in.

## Licentie / Licentie

Het project heeft een licentie onder Apache-2.0. Toestemming geven door opzettelijk een pull-verzoek te verzenden
onvoorwaardelijk wordt ingediend onder Sectie 5 van de Apache-licentie 2.0,
tenzij de afzender uitdrukkelijk anders aangeeft.

Het project maakt gebruik van Apache-2.0. Door opzettelijk een bijdrage in te dienen, dient u in
het onder Sectie 5 van de Apache-licentie 2.0 zonder aanvullende voorwaarden, tenzij u
uitdrukkelijk anders aangeven.

## Gedrag / Gedrag

Voor alle deelnemers geldt [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).
Alle deelnemers moeten [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)volgen.
