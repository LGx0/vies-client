# Nederlands (nl) — INSTALLATION

> [Alle talen](../../LANGUAGES.md) · Informatieve vertaling. Bij verschillen is de canonieke Engelse technische of juridische bron leidend. Alleen `LICENSE` en `NOTICE` in de hoofdmap zijn juridisch gezaghebbend; deze vertaling vervangt ze niet.

Deze module richt zich op Java 21 en gebruikt alleen de JDK tijdens runtime. Er is geen noodzaak
naar Spring of een externe JSON/HTTP-bibliotheek.

Deze module richt zich op Java 21 en gebruikt alleen de JDK tijdens runtime. Lente en extern
JSON/HTTP-bibliotheken zijn niet vereist.

## 1. Vereisten / Vereisten

- JDK 21 of nieuwer / JDK 21 of nieuwer
- Maven 3.9+ voor bronbuilds / Maven 3.9+ voor bronbuilds
- Uitgaande HTTPS-toegang tot het adres`ec.europa.eu:443`/ uitgaande HTTPS-toegang

Controleer / verifieer:

```bash
java -version
javac -version
./mvnw -version
```

Alle drie de opdrachten moeten naar dezelfde JDK 21+-omgeving verwijzen. Als Maven anders is
Print Java-versie, corrigeer eerst de waarde van`JAVA_HOME`.

Alle drie de opdrachten moeten dezelfde JDK 21+ installatie gebruiken. Als Maven een
andere Java-versie, repareer eerst`JAVA_HOME`.

## 2. Configureer JDK 21 / Configureer JDK 21

### macOS + Homebrew

```bash
brew install openjdk@21
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

Voor een permanente instelling wijzigt u de twee regels`export`in`~/.zprofile`en`~/.zshrc`
naar een bestand. Op een Intel Mac is het Homebrew-voorvoegsel meestal`/usr/local`.

Voor een permanente installatie voegt u beide exports toe aan`~/.zprofile`en`~/.zshrc`.
Het Homebrew-voorvoegsel is meestal`/usr/local`op Intel Macs.

###Linux

Installeer het OpenJDK 21-pakket van uw distributie en doe dan bijvoorbeeld:

```bash
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
```

Installeer het OpenJDK 21-pakket van uw distributie en wijs`JAVA_HOME`ernaar.

### Windows PowerShell

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

Gebruik voor permanente instellingen de interface Windows-omgevingsvariabelen.
Gebruik Windows-omgevingsvariabelen voor een permanente configuratie.

## 3. Build en lokale installatie / Build en lokale installatie

Vanuit de projectroot:

```bash
./mvnw clean verify
./mvnw install
```

- `verify`: vertaling, unit- en lokale integratietests, creatie van JAR's.
- `install`: hetzelfde, installeer vervolgens in de lokale`~/.m2/repository`-repository.
- `verify`: compileert, voert unit-/lokale integratietests uit en maakt de JAR's.
- `install`: installeert de artefacten ook in de lokale Maven-repository.

Gegenereerde artefacten:

```text
target/vies-client-1.0.0.jar
target/vies-client-1.0.0-sources.jar
target/vies-client-1.0.0-javadoc.jar
```

## 4. Maven-verbinding / Maven-afhankelijkheid

Als`./mvnw install`/ Na het uitvoeren van`./mvnw install`eerder werd uitgevoerd:

```xml
<dependency>
    <groupId>vies.client</groupId>
    <artifactId>vies-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

De module is momenteel een lokaal artefact. De organisatie publiceert in een team/CI-omgeving
Nexus-, Artifactory- of GitHub-pakketten en gebruik het vanaf daar.

Het artefact is momenteel lokaal. Voor teams en CI: publiceer het op uw Nexus,
Artifactory- of GitHub-pakkettenrepository.

## 5. Geleidelijke verbinding / Geleidelijke afhankelijkheid

```kotlin
repositories {
    mavenLocal()
}

dependencies {
    implementation("vies.client:vies-client:1.0.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
```

## 6. Gebruik zonder Maven/Gradle / Gebruik zonder bouwtool

Classpath-applicatie / Classpath-applicatie:

```bash
javac -cp /path/to/vies-client-1.0.0.jar MyApplication.java
java -cp "/path/to/vies-client-1.0.0.jar:." MyApplication
```

Onder Windows is het klassenpadscheidingsteken`;`, onder Unix/macOS is dit`:`.
Windows gebruikt`;`als klassenpadscheidingsteken; Unix/macOS gebruikt`:`.

## 7. JPMS-modulegebruik / JPMS-modulegebruik

In het`module-info.java`-bestand van de applicatie:

```java
module my.application {
    requires vies.client;
}
```

Compileren en uitvoeren:

```bash
javac --module-path vies-client-1.0.0.jar -d out src/module-info.java src/my/application/Main.java
java --module-path "vies-client-1.0.0.jar:out" -m my.application/my.application.Main
```

## 8. Windsurf / VS Code

`.vscode/settings.json`in het project stelt de Java-extensie in op JDK 21.
Het Homebrew-pad daarin is machinespecifiek: op Intel Mac, Linux of Windows
herschrijf het naar uw eigen JDK 21-directory. Voer na de wijziging de
`Developer: Reload Window`-opdracht.

De meegeleverde`.vscode/settings.json`verwijst de Java-extensie naar JDK 21
Het Homebrew-pad is machinespecifiek; vervang het op Intel macOS, Linux of Windows.
Voer`Developer: Reload Window`uit nadat u de JDK-instellingen hebt gewijzigd.

## 9. Netwerk en proxy / Netwerk en proxy

Vereist eindpunt:

```text
https://ec.europa.eu/taxation_customs/vies/rest-api
```

De client heeft geen API-sleutel nodig. Voor bedrijfsproxy, JDK`HttpClient`
proxy-instelling moet worden opgegeven in de runtime-omgeving. Proxy verbreekt TLS-verkeer
In dit geval moet het CA-certificaat van het bedrijf worden geïnstalleerd in de truststore van de gebruikte JDK.

Er is geen API-sleutel vereist. Bedrijfsproxy en CA-vertrouwen moeten worden geconfigureerd in het
runtime JDK/omgeving.

## 10. Snelle installatiecontrole / Installatierookcheck

```bash
./mvnw -q test
java -cp target/classes examples/ViesDemoServer.java
```

In een andere terminal:

```bash
curl "http://localhost:8085/vat-check?number=DE000000000"
```

De live rooktest is netwerkafhankelijk; Het zou niet verplicht moeten zijn in CI. De normale testen
ze gebruiken een lokale nepserver.

De live rookcontrole is afhankelijk van het netwerk en mag niet door Gate CI worden uitgevoerd. Normale testen
gebruik een lokale nepserver.

## 11. Veelvoorkomende fouten / Probleemoplossing

| Fout / probleem                    | Oplossing / oplossing                                                                                                                                                       |
| ---------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `switch expressions ... Java 14`   | Project-SDK en taalniveau moeten 21 zijn / SDK en taalniveau instellen op 21                                                                                                |
| `release version 21 not supported` | Maven draait met de oude JDK; fix`JAVA_HOME`/ Maven gebruikt een oude JDK; vast`JAVA_HOME`                                                                                  |
| `UnsupportedClassVersionError`     | De actieve JVM moet 21+ zijn / Uitvoeren met JVM 21+                                                                                                                        |
| `NETWORK_ERROR`                    | Controleer DNS/proxy/firewall/TLS-instelling / Controleer DNS, proxy, firewall en TLS                                                                                       |
| `TIMEOUT`                          | Verleng de time-out niet voor onbepaalde tijd; onderzoek het netwerk en de VIES-gezondheid / Onderzoek de netwerk-/VIES-gezondheid voordat time-outs worden verhoogd        |
| `CLIENT_OVERLOADED`                | Uitgestelde nieuwe poging, begrensde toegang, werkers plus globale begrenzer / Vertraagde nieuwe poging, begrensde toegang, werkers plus globale begrenzer                  |
| `CACHE_ERROR`                      | Controleer Redis-time-out/gezondheid/statistieken; omzeil het niet met een massale directe VIES-oproep / Controleer de Redis-status; omzeil het niet met een VIES-stormloop |
