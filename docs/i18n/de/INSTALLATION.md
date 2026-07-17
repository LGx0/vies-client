# Deutsch (`de`) — Installation

[Sprachauswahl](../../LANGUAGES.md) · [English source](../../INSTALLATION.md)

> Bei Abweichungen ist das englische Original maßgeblich. `LICENSE` bleibt unübersetzt und allein rechtsverbindlich. Projektlizenz: Apache-2.0.

## Voraussetzungen

- JDK 21 oder neuer; Release und CI sollten mindestens JDK 21 prüfen.
- Maven 3.9+ für den Standard-Build.
- Ausgehendes HTTPS zu `ec.europa.eu:443`; für Unit- und lokale Integrationstests ist kein Internet erforderlich.

```bash
java -version
./mvnw -version
```

## JDK 21 einrichten

macOS mit Homebrew:

```bash
brew install openjdk@21
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
export PATH="$JAVA_HOME/bin:$PATH"
```

Linux: Installieren Sie das JDK-21-Paket Ihrer Distribution und setzen Sie `JAVA_HOME`. Windows PowerShell:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

## Bauen und lokal installieren

```bash
git clone <repository-url>
cd vies-client
./mvnw clean verify
./mvnw install
```

Artefakte liegen unter `target/`: Haupt-JAR, `-sources.jar` und `-javadoc.jar`. `./mvnw install` legt sie zusätzlich im lokalen Maven-Repository ab.

## Maven, Gradle und JPMS

```xml
<dependency>
  <groupId>vies.client</groupId><artifactId>vies-client</artifactId><version>1.0.0</version>
</dependency>
```

```kotlin
implementation("vies.client:vies-client:1.0.0")
```

```java
module my.application { requires vies.client; }
```

Im Classpath-Modus ist keine Moduldeklaration nötig. Ohne Build-Werkzeug kann das JAR mit `--module-path` oder `-cp` eingebunden werden. Beim Kopieren der Quellen in ein nichtmodulares Projekt `module-info.java` auslassen.

## IDE, Netzwerk und Proxy

Stellen Sie in IntelliJ IDEA, VS Code oder Windsurf Project SDK und Maven-JDK auf 21. Konfigurieren Sie Unternehmensproxys auf JVM- bzw. Infrastruktur-Ebene; Zugangsdaten gehören nicht in Quellcode oder Repository. Produktionssysteme sollten DNS, TLS-Zertifikate und die Erreichbarkeit der offiziellen VIES-Endpunkte überwachen.

## Smoke-Check

```bash
./mvnw -q clean test
./mvnw -q package
jar --describe-module --file target/vies-client-1.0.0.jar
```

Ein Live-Aufruf ist optional und darf nicht als Lasttest verwendet werden. Häufige Ursachen: falsches JDK (`release version 21 not supported`), blockierter Proxy/TLS, VIES- oder Mitgliedstaat-Ausfall und zu aggressive lokale Parallelität.

## Vollständige Plattform-, Build- und Netzwerkdetails

macOS dauerhaft: Homebrew-JDK-Pfad und `JAVA_HOME="$(/usr/libexec/java_home -v 21)"` in das Shellprofil. Linux-Beispiel: `sudo apt install openjdk-21-jdk maven`, danach `JAVA_HOME` setzen. Windows öffnet nach dauerhafter Environment-Änderung ein neues Terminal. Immer `java -version`, `javac -version`, `./mvnw -version` gemeinsam prüfen.

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
./mvnw install
ls -lh target/vies-client-1.0.0*.jar
javac --release 21 -cp vies-client-1.0.0.jar MyApp.java
java -cp .:vies-client-1.0.0.jar MyApp   # Windows: ; statt :
```

Das Release-JAR hat keine Drittanbieter-Runtimeabhängigkeit; JUnit und `jdk.httpserver` sind testseitig. IDE: Project SDK, Language Level, Maven JDK, `java.configuration.runtimes` und `java.jdt.ls.java.home` auf 21, Language Server/Maven neu laden. Keine absoluten maschinenspezifischen SDK-Pfade committen.

Erforderlich ist HTTPS zu `https://ec.europa.eu/taxation_customs/vies/`. Proxy und Truststore zentral konfigurieren, TLS nie deaktivieren. Container: JRE/JDK 21, CA-Zertifikate, non-root, feste CPU/Heap-/Connection-Limits.

| Problem | Korrektur |
|---|---|
| `switch expressions ... Java 14` | SDK/Language Level 21 |
| `release version 21 not supported` | Maven-JDK/`JAVA_HOME` korrigieren |
| `UnsupportedClassVersionError` | Runtime 21+ |
| `NETWORK_ERROR` | DNS, Proxy, Firewall, TLS |
| `TIMEOUT` | Netzwerk/VIES prüfen; Timeout nicht grenzenlos erhöhen |
| `CLIENT_OVERLOADED` | verzögerter Retry, Ingress begrenzen, Worker + globaler Limiter |
| `CACHE_ERROR` | Redis Health/Timeout/Metriken; keinen VIES-Stampede erzeugen |

## Reproduzierbare Installation und Release-Artefakte

`./mvnw clean verify` löscht alte Ausgaben, kompiliert mit `--release 21`, führt Unit- und Loopback-Integrationstests aus und baut anschließend das modulare JAR. `./mvnw install` legt Binary, Sources und Javadocs unter den Maven-Koordinaten im lokalen Repository ab. Prüfen Sie vor der Verwendung, dass Maven tatsächlich dasselbe JDK wie das Terminal verwendet. In CI sollte die Java-Version explizit fixiert und der lokale Maven-Cache nur als Beschleunigung, nicht als Quelle ungeprüfter Artefakte behandelt werden.

Maven Central oder GitHub Packages benötigen nach Veröffentlichung die entsprechende Repository-Konfiguration. Vorher ist `./mvnw install` die einfachste Integration. Für Gradle ist `mavenLocal()` nur für lokale Entwicklung sinnvoll; Produktionsbuilds sollen auf eine unveränderliche veröffentlichte Version zeigen. Das Sources-JAR unterstützt IDE-Navigation, das Javadoc-JAR die API-Dokumentation, und `jar --describe-module` bestätigt den echten Modulnamen.

Bei Unternehmensproxys sind DNS-Auflösung, CONNECT auf Port 443, CA-Truststore und eventuell Proxy-Authentisierung getrennt zu prüfen. Ein Proxy-Passwort gehört in Secret-Management, nicht in `pom.xml`, README oder JVM-Argumente in einer öffentlich sichtbaren Prozessliste. Die VIES-Basis-URL darf in Produktion nicht auf unverschlüsseltes HTTP umgestellt werden. In Kubernetes müssen Egress-Policy, DNS, CA-Bundle, Readiness, CPU/Heap und graceful termination zusammen getestet werden.

Nach der Installation empfiehlt sich zuerst die vollständig offline laufende Testsuite, danach der lokale Demo-Server und höchstens ein kleiner manueller Live-Smoke. Eine echte VAT-ID im Beispiel beweist nur die momentane Erreichbarkeit; sie ist kein stabiler Unit-Test. Load-, Stress- und CI-Verkehr gegen den öffentlichen Dienst ist untersagt.
