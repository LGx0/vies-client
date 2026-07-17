# Svenska (sv) — INSTALLATION

> [Alla språk](../../LANGUAGES.md) · Informativ översättning. Vid avvikelser gäller den kanoniska engelska tekniska eller juridiska källan. Endast `LICENSE` och `NOTICE` i roten är juridiskt auktoritativa; översättningen ersätter dem inte.

Den här modulen är inriktad på Java 21 och använder endast JDK under körning. Det finns inget behov
till Spring eller ett externt JSON/HTTP-bibliotek.

Den här modulen är inriktad på Java 21 och använder endast JDK under körning. Fjäder och yttre
JSON/HTTP-bibliotek krävs inte.

## 1. Förutsättningar / Förutsättningar

- JDK 21 eller nyare / JDK 21 eller nyare
- Maven 3.9+ för källbyggen / Maven 3.9+ för källbyggen
- Utgående HTTPS-åtkomst till adressen`ec.europa.eu:443`/ utgående HTTPS-åtkomst

Kontrollera/verifiera:

```bash
java -version
javac -version
./mvnw -version
```

Alla tre kommandon ska peka på samma JDK 21+-miljö. Om Maven är annorlunda
Skriv ut Java-version, korrigera först värdet på`JAVA_HOME`.

Alla tre kommandon ska använda samma JDK 21+-installation. Om Maven rapporterar en
annan Java-version, fixa`JAVA_HOME`först.

## 2. Konfigurera JDK 21 / Konfigurera JDK 21

### macOS + Homebrew

```bash
brew install openjdk@21
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

För en permanent inställning, ändra de två raderna`export`till`~/.zprofile`och`~/.zshrc`
till en fil. På en Intel Mac är Homebrew-prefixet vanligtvis`/usr/local`.

För en beständig installation, lägg till båda exporterna till`~/.zprofile`och`~/.zshrc`.
Homebrew-prefixet är vanligtvis`/usr/local` på Intel Mac-datorer.

### Linux

Installera OpenJDK 21-paketet för din distribution, sedan till exempel:

```bash
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
```

Installera din distributions OpenJDK 21-paket och peka`JAVA_HOME`till det.

### Windows PowerShell

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

Använd gränssnittet Windows Environment Variables för permanenta inställningar.
Använd Windows miljövariabler för en beständig konfiguration.

## 3. Bygg och lokal installation / Bygg och lokal installation

Från projektroten:

```bash
./mvnw clean verify
./mvnw install
```

- `verify`: översättning, enhetstester och lokala integrationstester, skapande av JAR.
- `install`: samma, installera sedan till det lokala`~/.m2/repository`-förrådet.
- `verify`: kompilerar, kör enhets-/lokala integrationstester och skapar JAR.
- `install`: installerar också artefakterna i det lokala Maven-förvaret.

Genererade artefakter:

```text
target/vies-client-1.0.0.jar
target/vies-client-1.0.0-sources.jar
target/vies-client-1.0.0-javadoc.jar
```

## 4. Maven-anslutning / Maven-beroende

Om`./mvnw install`/ Efter körning`./mvnw install`kördes tidigare:

```xml
<dependency>
    <groupId>vies.client</groupId>
    <artifactId>vies-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

Modulen är för närvarande en lokal artefakt. Organisationen publicerar i en team/CI-miljö
Nexus, Artifactory eller GitHub-paket och använd det sedan därifrån.

Artefakten är för närvarande lokal. För team och CI, publicera den på din Nexus,
Artifactory, eller GitHub-paketförråd.

## 5. Gradle-anslutning / Gradle-beroende

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

## 6. Använd utan Maven/Gradle / Använd utan byggverktyg

Classpath-applikation / Classpath-applikation:

```bash
javac -cp /path/to/vies-client-1.0.0.jar MyApplication.java
java -cp "/path/to/vies-client-1.0.0.jar:." MyApplication
```

Under Windows är klasssökvägsseparatorn`;`, under Unix/macOS är det`:`.
Windows använder`;`som klasssökvägsseparator; Unix/macOS använder`:`.

## 7. JPMS-modulanvändning / JPMS-modulanvändning

I programmets`module-info.java`-fil:

```java
module my.application {
    requires vies.client;
}
```

Kompilera och kör:

```bash
javac --module-path vies-client-1.0.0.jar -d out src/module-info.java src/my/application/Main.java
java --module-path "vies-client-1.0.0.jar:out" -m my.application/my.application.Main
```

## 8. Vindsurfing / USA-kod

`.vscode/settings.json`i projektet ställer Java-tillägget till JDK 21.
Hembryggningsvägen i den är maskinspecifik: på Intel Mac, Linux eller Windows
skriv om den till din egen JDK 21-katalog. Efter modifiering, kör
`Developer: Reload Window`kommando.

Den medföljande`.vscode/settings.json` pekar Java-tillägget till JDK 21. Dess
Hembryggningsbanan är maskinspecifik; ersätt den på Intel macOS, Linux eller Windows.
Kör`Developer: Reload Window`efter att ha ändrat JDK-inställningarna.

## 9. Nätverk och proxy / Nätverk och proxy

Obligatorisk slutpunkt:

```text
https://ec.europa.eu/taxation_customs/vies/rest-api
```

Klienten kräver ingen API-nyckel. För företagsproxy, JDK`HttpClient`
proxyinställning måste anges i runtime-miljön. Proxy bryter TLS-trafik
I det här fallet måste företagets CA-certifikat installeras i truststore för den använda JDK.

Ingen API-nyckel krävs. Corporate proxy och CA-trust måste konfigureras i
runtime JDK/miljö.

## 10. Snabb installationskontroll / Installationsrökkontroll

```bash
./mvnw -q test
java -cp target/classes examples/ViesDemoServer.java
```

I en annan terminal:

```bash
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Röktestet är nätverksberoende; Det borde inte vara obligatoriskt i CI. De normala testerna
de använder en lokal mock-server.

Kontrollen av levande rök beror på nätverket och bör inte gate CI. Normala tester
använd en lokal mock-server.

## 11. Vanliga fel / Felsökning

| Fel / problem                      | Lösning / Fix                                                                                                                                             |
| ---------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `switch expressions ... Java 14`   | Projektets SDK och språknivå bör vara 21 / Ställ in SDK och språknivå till 21                                                                             |
| `release version 21 not supported` | Maven kör med den gamla JDK; fixa`JAVA_HOME`/ Maven använder en gammal JDK; fast`JAVA_HOME`                                                               |
| `UnsupportedClassVersionError`     | Den körande JVM bör vara 21+ / Kör med JVM 21+                                                                                                            |
| `NETWORK_ERROR`                    | Kontrollera DNS/proxy/brandvägg/TLS-inställning / Kontrollera DNS, proxy, brandvägg och TLS                                                               |
| `TIMEOUT`                          | Öka inte timeouten på obestämd tid; undersök nätverket och VIES hälsa / Undersök nätverket/VIES hälsa innan du höjer timeouts                             |
| `CLIENT_OVERLOADED`                | Delayed retry, bounded ingress, workers plus global limiter / Delayed retry, bounded ingress, workers plus global limiter                                 |
| `CACHE_ERROR`                      | Kontrollera Redis timeout/hälsa/metrics; kringgå det inte med ett massdirekt VIES-samtal / Kontrollera Redis hälsa; gå inte förbi det med en VIES-stämpel |
