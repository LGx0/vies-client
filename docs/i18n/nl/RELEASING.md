# Nederlands (nl) — RELEASING

> [Alle talen](../../LANGUAGES.md) · Informatieve vertaling. Bij verschillen is de canonieke Engelse technische of juridische bron leidend. Alleen `LICENSE` en `NOTICE` in de hoofdmap zijn juridisch gezaghebbend; deze vertaling vervangt ze niet.

## 1. Vereisten / Vereisten

- schone`main`-tak en groene GitHub-acties;
- passende GitHub-autoriteit om de tag te maken en vrij te geven;
- JDK 21 en Maven 3.9+;
- releaseversie en datum vastgelegd in bestand`CHANGELOG.md`.

## 2. Versiebeheer

Het project maakt gebruik van semantisch versiebeheer:

- `PATCH`: compatibele bugfix;
- `MINOR`: compatibele nieuwe functie;
- `MAJOR`: openbare API of semantiek verbreken.

Het project maakt gebruik van Semantic Versioning: patch voor compatibele oplossingen, minor voor compatibel
functies, en belangrijk voor het doorbreken van API- of semantische wijzigingen.

## 3. Pre-releaseverificatie

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
jar --describe-module --file target/vies-client-1.0.0.jar
jdeps --print-module-deps target/vies-client-1.0.0.jar
```

Controleer ook:

- geen geheimen of persoonlijke gegevens in de gehele Git-geschiedenis;
- `LICENSE`,`NOTICE`,`SECURITY.md`en documentatie zijn actueel;
- Javadoc en bronnen JAR gemaakt;
- er is geen verplichte live VIES of belastingtest in CI;
- wijzigingen aan de publieke API worden opgenomen in de changelog.

## 4. GitHub-release

1. Stel de versie in het bestand`pom.xml`in.
2. Voer de changelog en versie door.
3. Maak een ondertekende geannoteerde tag:`git tag -s v1.0.0 -m "v1.0.0"`.
4. Push commit en tag:`git push origin main --follow-tags`.
5. De`release.yml`-workflow voert de tests opnieuw uit en voegt vervolgens het binaire bestand toe,
   bronnen en Javadoc JAR-bestanden voor GitHub Release.

Gebruik indien mogelijk ondertekende geannoteerde tags. Maak nooit een release op basis van een niet-beoordeelde of
mislukte commit.

## 5. Maven Central- of GitHub-pakketten

De huidige build is lokaal en klaar voor GitHub Release-distributie. Meer voor Maven Central
nodig:

- een verifieerbare, eigen reverse-DNS`groupId`;
- project`url`,`scm`,`developers`en metadata voor distributiebeheer;
- Centrale Portal registratie en token;
- GPG-handtekening en centraal compatibele publicatieconfiguratie.

De huidige build is klaar voor lokale installatie en GitHub-releases. Maven Centraal
publicatie vereist bovendien een eigen reverse-DNS-groeps-ID, compleet project/SCM
metagegevens, Central Portal-inloggegevens, ondertekening van artefacten en publicatieconfiguratie.

Plaats geen token of privé-GPG-sleutel in de repository. GitHub-acties zijn geheim en minimaal
autorisatie gebruiken. / Leg nooit tokens of privé-ondertekeningssleutels vast. Gebruik GitHub
Actiegeheimen en minste privileges.

## 6. Stappen na de release / Na de release

- controleer releasedownloads en SHA-256-waarden;
- start een nieuwe`[Unreleased]`-sessie;
- publiceer een GitHub-beveiligingsadvies voor een beveiligingspatch;
- update de gedocumenteerde afhankelijkheidsversie.
