# Nederlands (nl) — THIRD_PARTY_NOTICES

> [Alle talen](../../LANGUAGES.md) · Informatieve vertaling. Bij verschillen is de canonieke Engelse technische of juridische bron leidend. Alleen `LICENSE` en `NOTICE` in de hoofdmap zijn juridisch gezaghebbend; deze vertaling vervangt ze niet.

## Gedistribueerde runtime-JAR / Gedistribueerde runtime-JAR

`vies-client-1.0.0.jar`bevat of vereist geen externe runtime-bibliotheken.
Het maakt alleen gebruik van de`java.base`- en`java.net.http`-modules van de JDK.

De gedistribueerde JAR bevat en vereist geen runtimebibliotheek van derden. Het alleen
maakt gebruik van de JDK-modules`java.base`en`java.net.http`.

## Afhankelijkheid bouwen en testen / Afhankelijkheid bouwen en testen

| Onderdeel / Onderdeel | Versie / Versie | Gebruik / reikwijdte          | Licentie / Licentie |
| --------------------- | --------------: | ----------------------------- | ------------------- |
| JUnit Jupiter         |           6.1.2 | alleen testen / alleen testen | EPL-2.0             |

Maven-buildplug-ins en transitieve testafhankelijkheden zijn niet opgenomen in de vrijgegeven JAR
naar binnen; ze worden door Maven gedownload volgens hun eigen artefactmetagegevens en licenties.

Maven-plug-ins en transitieve testafhankelijkheden zijn niet gebundeld in de JAR-release;
Maven lost ze op onder hun eigen artefactmetagegevens en licenties.

Bron / Bron: <https://github.com/junit-team/junit-framework>

Als er een nieuwe afhankelijkheid aan het project wordt toegevoegd, worden dit bestand en de licentiecompatibiliteit gecontroleerd
het moet in hetzelfde pull-verzoek worden bijgewerkt.

Elke pull-aanvraag die een afhankelijkheid toevoegt, moet deze bestands- en documentlicentie bijwerken
verenigbaarheid.
