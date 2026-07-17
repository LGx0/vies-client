# Dansk (da) — INSTALLATION

> [Alle sprog](../../LANGUAGES.md) · Informativ oversættelse. Ved afvigelser er den kanoniske engelske tekniske eller juridiske kilde gældende. Kun `LICENSE` og `NOTICE` i roden er juridisk autoritative; oversættelsen erstatter dem ikke.

Dette modul er målrettet mod Java 21 og bruger kun JDK under kørsel. Der er ikke behov
til Spring eller et eksternt JSON/HTTP-bibliotek.

Dette modul er målrettet mod Java 21 og bruger kun JDK under kørsel. Fjeder og udvendig
JSON/HTTP-biblioteker er ikke påkrævet.

## 1. Forudsætninger / Forudsætninger

- JDK 21 eller nyere / JDK 21 eller nyere
- Maven 3.9+ til kildebyggeri / Maven 3.9+ til kildebyg
- Udgående HTTPS-adgang til adressen`ec.europa.eu:443`/ udgående HTTPS-adgang

Tjek/bekræft:

```bash
java -version
javac -version
./mvnw -version
```

Alle tre kommandoer skal pege på det samme JDK 21+ miljø. Hvis Maven er anderledes
Udskriv Java-version, korriger først værdien af ​​`JAVA_HOME`.

Alle tre kommandoer skal bruge den samme JDK 21+ installation. Hvis Maven rapporterer en
anden Java-version, ret`JAVA_HOME`først.

## 2. Konfigurer JDK 21 / Konfigurer JDK 21

### macOS + Homebrew

```bash
brew install openjdk@21
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

For en permanent indstilling skal du ændre de to linjer`export`til`~/.zprofile`og`~/.zshrc`
til en fil. På en Intel Mac er Homebrew-præfikset normalt`/usr/local`.

For en vedvarende opsætning skal du tilføje begge eksporter til`~/.zprofile`og`~/.zshrc`.
Homebrew-præfikset er normalt`/usr/local` på Intel Macs.

### Linux

Installer OpenJDK 21-pakken til din distribution, så f.eks.:

```bash
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
```

Installer din distributions OpenJDK 21-pakke og peg`JAVA_HOME`til den.

### Windows PowerShell

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

Brug grænsefladen Windows Environment Variables til permanente indstillinger.
Brug Windows-miljøvariabler til en vedvarende konfiguration.

## 3. Byg og lokal installation / Byg og lokal installation

Fra projektroden:

```bash
./mvnw clean verify
./mvnw install
```

- `verify`: oversættelse, enheds- og lokale integrationstest, oprettelse af JAR'er.
- `install`: samme, installer derefter til det lokale`~/.m2/repository`-lager.
- `verify`: kompilerer, kører enheds/lokale integrationstests og opretter JAR'erne.
- `install`: installerer også artefakterne i det lokale Maven-lager.

Genererede artefakter:

```text
target/vies-client-1.0.0.jar
target/vies-client-1.0.0-sources.jar
target/vies-client-1.0.0-javadoc.jar
```

## 4. Maven-forbindelse / Maven-afhængighed

Hvis`./mvnw install`/ Efter at have kørt`./mvnw install`blev kørt før:

```xml
<dependency>
    <groupId>vies.client</groupId>
    <artifactId>vies-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

Modulet er i øjeblikket en lokal artefakt. Organisationen udgiver i et team/CI-miljø
Nexus, Artifactory eller GitHub-pakker, og brug det derefter derfra.

Artefaktet er i øjeblikket lokalt. For teams og CI, udgiv det til din Nexus,
Artifactory eller GitHub Packages repository.

## 5. Gradle-forbindelse / Gradle-afhængighed

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

## 6. Brug uden Maven/Gradle / Brug uden et byggeværktøj

Classpath-applikation / Classpath-applikation:

```bash
javac -cp /path/to/vies-client-1.0.0.jar MyApplication.java
java -cp "/path/to/vies-client-1.0.0.jar:." MyApplication
```

Under Windows er klassestiseparatoren`;`, under Unix/macOS er det`:`.
Windows bruger`;`som klassesti-separator; Unix/macOS bruger`:`.

## 7. JPMS-modulbrug / JPMS-modulbrug

I applikationens`module-info.java`-fil:

```java
module my.application {
    requires vies.client;
}
```

Kompiler og kør:

```bash
javac --module-path vies-client-1.0.0.jar -d out src/module-info.java src/my/application/Main.java
java --module-path "vies-client-1.0.0.jar:out" -m my.application/my.application.Main
```

## 8. Windsurf / USA-kode

`.vscode/settings.json`i projektet indstiller Java-udvidelsen til JDK 21.
Homebrew-stien i den er maskinspecifik: på Intel Mac, Linux eller Windows
omskriv det til din egen JDK 21-mappe. Efter ændring skal du køre
`Developer: Reload Window`kommando.

Den medfølgende`.vscode/settings.json` peger Java-udvidelsen til JDK 21. Dens
Homebrew-stien er maskinspecifik; erstatte det på Intel macOS, Linux eller Windows.
Kør`Developer: Reload Window`efter at have ændret JDK-indstillinger.

## 9. Netværk og proxy / Netværk og proxy

Påkrævet slutpunkt:

```text
https://ec.europa.eu/taxation_customs/vies/rest-api
```

Klienten kræver ikke en API-nøgle. For virksomheds proxy, JDK`HttpClient`
proxy-indstilling skal angives i runtime-miljøet. Proxy bryder TLS-trafik
I dette tilfælde skal virksomhedens CA-certifikat installeres i truststore for det brugte JDK.

Der kræves ingen API-nøgle. Firmaproxy og CA-tillid skal konfigureres i
runtime JDK/miljø.

## 10. Hurtigt installationstjek / Installationsrøgtjek

```bash
./mvnw -q test
java -cp target/classes examples/ViesDemoServer.java
```

I en anden terminal:

```bash
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Den levende røgtest er netværksafhængig; Det burde ikke være obligatorisk i CI. De normale prøver
de bruger en lokal mock server.

Live-røgkontrollen afhænger af netværket og bør ikke gate CI. Normale prøver
bruge en lokal mock server.

## 11. Almindelige fejl / Fejlfinding

| Fejl/problem                       | Løsning / Fix                                                                                                                                       |
| ---------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------- |
| `switch expressions ... Java 14`   | Projekt SDK og sprogniveau skal være 21 / Indstil SDK og sprogniveau til 21                                                                         |
| `release version 21 not supported` | Maven kører med det gamle JDK; fix`JAVA_HOME`/ Maven bruger en gammel JDK; fast`JAVA_HOME`                                                          |
| `UnsupportedClassVersionError`     | Den kørende JVM skal være 21+ / Kør med JVM 21+                                                                                                     |
| `NETWORK_ERROR`                    | Tjek DNS/proxy/firewall/TLS-indstilling / Tjek DNS, proxy, firewall og TLS                                                                          |
| `TIMEOUT`                          | Forøg ikke timeout på ubestemt tid; undersøg netværket og VIES-sundheden / Undersøg netværket/VIES-sundheden før timeouts øges                      |
| `CLIENT_OVERLOADED`                | Forsinket genforsøg, begrænset indtrængen, arbejdere plus global limiter / Forsinket genforsøg, begrænset indtrængen, arbejdere plus global limiter |
| `CACHE_ERROR`                      | Tjek Redis timeout/sundhed/metrics; omgå det ikke med et massedirekte VIES-opkald / Tjek Redis sundhed; omgå det ikke med et VIES-stampede          |
