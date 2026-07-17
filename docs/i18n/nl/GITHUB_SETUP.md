# Nederlands (nl) — GITHUB_SETUP

> [Alle talen](../../LANGUAGES.md) · Informatieve vertaling. Bij verschillen is de canonieke Engelse technische of juridische bron leidend. Alleen `LICENSE` en `NOTICE` in de hoofdmap zijn juridisch gezaghebbend; deze vertaling vervangt ze niet.

## 1. Verplichte controles vóór publicatie

- Bevestig dat u het recht heeft om de bron, documentatie en naam te publiceren.
- Scan de hele map en later de hele Git-geschiedenis op geheimen.
- Verwijder klantgegevens, echt belastingnummer van de aanvrager, token,`.env`-bestand en IDE
  machinespecifiek bestand.
- Controleer Apache-2.0`LICENSE`,`NOTICE`en het kennisgevingsbestand van derden.
- Uitvoering:`./mvnw --batch-mode --no-transfer-progress clean verify`.

- Bevestig het recht om alle code, documentatie en projectnamen te publiceren.
- Scan de directory en eventuele Git-geschiedenis op geheimen.
- Verwijder klantgegevens, echte btw-nummers van aanvragers, tokens,`.env`en machinebestanden.
- Controleer Apache-2.0 en kennisgevingen van derden.
- Voer de volledige Maven-verificatie uit.

## 2. Maak de repository aan / Maak de repository aan

Het project bevat al een README, licentie en bestand`.gitignore`, vandaar de GitHub
genereer geen nieuwe bij het maken van internet. Voorgestelde naam:`vies-client`, zichtbaarheid:
`Public`, standaardvertakking:`main`.

Het project bevat al README-, licentie- en negeerbestanden; niet genereren
duplicaten op GitHub. Voorgestelde naam:`vies-client`, zichtbaarheid:`Public`, standaard
vestiging:`main`.

Commando dat kan worden gebruikt nadat de exacte eigenaar is bevestigd:

```bash
git init -b main
git add --all
git commit -m "Initial open-source release"
gh repo create OWNER/vies-client --public --source . --remote origin --push
```

Voer de opdracht niet uit zonder`OWNER`te vervangen en de volledige gefaseerde diff te controleren.
Voer het niet uit voordat u de`OWNER`hebt vervangen en het volledige gefaseerde differentieel hebt beoordeeld.

## 3. Repository-instellingen

Aanbevolen:

- Problemen: ingeschakeld;
- Discussies: ingeschakeld voor gebruiksvragen;
- Wiki's: uitgeschakeld tenzij actief onderhouden;
- Projecten: optioneel;
- Bewaar deze repository: optioneel na de eerste stabiele release;
- Automatisch hoofdtakken verwijderen: ingeschakeld;
- Rapportage van privékwetsbaarheden: ingeschakeld;
- Dependabot-waarschuwingen en beveiligingsupdates: ingeschakeld;
- Geheim scannen en push-beveiliging: ingeschakeld waar GitHub ze aanbiedt.

## 4. Vage regelset voor takbescherming / Takbescherming of regelset

Een`main`-filiaal:

- pull-verzoek vereist;
- minimaal één goedkeuring / minimaal één goedkeuring;
- ontslag op staande voet na nieuwe commits;
- vereiste controles:`CI`,`CodeQL`,`Dependency review`, indien beschikbaar;
- gespreksresolutie vereist;
- forceren en verwijderen uitgeschakeld;
- beheerdersbypass alleen gebruikt voor noodgevallen;
- ondertekende commits/tags aanbevolen voor releases.

Vóór de eerste push bestaat de naam van de vereiste cheque nog niet; voer de workflow één keer uit
kat, het kan een regelset zijn. / Namen van verplichte cheques verschijnen na de eerste workflow
loop; configureer daarna de regelset.

## 5. Labels en onderwerpen

Voorgestelde tags:

```text
bug, enhancement, documentation, localization, security, dependencies,
performance, concurrency, breaking-change, good-first-issue, help-wanted, triage
```

Voorgestelde beschrijving van de opslagplaats:

```text
Fast Java 21 VIES VAT checker and EU VAT number validator with virtual threads, bounded concurrency, cache, and bilingual errors.
```

Voorgestelde onderwerpen:

```text
java, java21, java-library, vies, vat, vat-number, vat-checker, vat-validator,
vat-validation, vat-number-validation, eu-vat, tax-id, tax-id-checker,
tax-id-validation, rest-client, virtual-threads, single-flight, jpms,
zero-dependency
```

## 6. Knop Doneren/Sponsoren

GitHub geeft de knop **Sponsor** weer op basis van`.github/FUNDING.yml`. Alleen de
voer een door de beheerder geverifieerde financiële URL in. Voorbeeld:

```yaml
buy_me_a_coffee: lgx0
```

Een speciale sleutel kan ook worden gebruikt bij een ondersteunde provider, bijvoorbeeld`github`,`ko_fi`
of`custom`. Een donatie vormt geen SLA of recht van voorrang; zie`SUPPORT.md`.

GitHub geeft de Sponsorknop van`.github/FUNDING.yml`weer. Configureer alleen een
financieringsbestemming geverifieerd door de beheerder. Voor donaties wordt geen SLA of
bestuursrechten.

## 7. Updates na de eerste push / Updates na de eerste push

Zodra je de daadwerkelijke GitHub-URL kent, update je:

- `pom.xml`:`url`,`scm`,`developers`;
- README: CI- en CodeQL-badge-URL's;
- `SECURITY.md`: privé advies-URL, indien nodig;
- releasedocumentatie en Maven Central-coördinaten;
- `.github/FUNDING.yml`:`https://buymeacoffee.com/lgx0`.

## 8. Eerste release / Eerste release

Maak pas een ondertekende`v1.0.0`-tag na groene CI/CodeQL. De release-workflowtests,
bouw de binaire/sources/Javadoc JAR's en SHA-256 checksum, en vervolgens GitHub
Creëert een release. Details: [RELEASING.md](RELEASING.md).
