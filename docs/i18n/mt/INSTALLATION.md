# Malti (mt) — Installation

> [Għażla tal-lingwa](../../LANGUAGES.md) · Din il-lokalizzazzjoni hija għall-aċċessibbiltà. F’każ ta’ differenza, is-sors kanoniku tekniku jew legali bl-Ingliż jipprevali. `LICENSE` u `NOTICE` fir-root jibqgħu legalment awtorevoli.

Dan il-modulu jimmira għal Java 21 u juża biss il-JDK waqt ir-runtime. M'hemmx bżonn
għal Spring jew librerija esterna JSON/HTTP.
Dan il-modulu jimmira għal Java 21 u juża biss il-JDK waqt ir-runtime. Rebbiegħa u esterni
Il-libreriji JSON/HTTP mhumiex meħtieġa.

## 1. Prerekwiżiti / Prerekwiżiti

- JDK 21 jew aktar ġdid / JDK 21 jew aktar ġdid
- Maven 3.9+ għall-bini tas-sors / Maven 3.9+ għall-bini tas-sors
- Aċċess HTTPS barra għall-indirizz `ec.europa.eu:443`/ aċċess HTTPS barra
  Iċċekkja / Ivverifika:

```bash
java -version
javac -version
./mvnw -version
```

It-tliet kmandi għandhom jindikaw l-istess ambjent JDK 21+. Jekk Maven huwa differenti
Stampa l-verżjoni Java, l-ewwel ikkoreġi l-valur ta '`JAVA_HOME`.
It-tliet kmandi għandhom jużaw l-istess installazzjoni JDK 21+. Jekk Maven jirrapporta a
verżjoni Java differenti, waħħal `JAVA_HOME` l-ewwel.

## 2. Ikkonfigura JDK 21 / Ikkonfigura JDK 21

## macOS + Homebrew

```bash
brew install openjdk@21
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

Għal setting permanenti, ibdel iż-żewġ linji `export` għal`~/.zprofile` u `~/.zshrc` għal fajl. Fuq Intel Mac, il-prefiss Homebrew huwa normalment `/usr/local`.
Għal setup persistenti, żid iż-żewġ esportazzjonijiet ma '`~/.zprofile` u `~/.zshrc`.
Il-prefiss Homebrew huwa normalment `/usr/local` fuq Intel Macs.

## Linux

Installa l-pakkett OpenJDK 21 tad-distribuzzjoni tiegħek, imbagħad per eżempju:

```bash
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
```

Installa l-pakkett OpenJDK 21 tad-distribuzzjoni tiegħek u punt `JAVA_HOME` lejh.

## Windows PowerShell

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

Għal settings permanenti, uża l-interface tal-Varjabbli tal-Ambjent tal-Windows.
Uża Varjabbli tal-Ambjent tal-Windows għal konfigurazzjoni persistenti.

## 3. Ibni u installazzjoni lokali / Ibni u installazzjoni lokali

Mill-għerq tal-proġett:

```bash
./mvnw clean verify
./mvnw install
```

-`verify`: traduzzjoni, testijiet ta' integrazzjoni u lokali, ħolqien ta' JARs.

- `install`: l-istess, imbagħad installa fir-repożitorju`~/.m2/repository` lokali.
- `verify`: jikkompila, imexxi testijiet ta 'integrazzjoni/unità lokali, u joħloq il-JARs.
- `install`: jinstalla wkoll l-artifacts fir-repożitorju Maven lokali.
  Artifacts iġġenerati:

```text
target/vies-client-1.0.0.jar
target/vies-client-1.0.0-sources.jar
target/vies-client-1.0.0-javadoc.jar
```

## 4. Konnessjoni Maven / dipendenza Maven

Jekk `./mvnw install`/ Wara t-tħaddim`./mvnw install` kien immexxi qabel:

```xml
<dependency>
    <groupId>vies.client</groupId>
    <artifactId>vies-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

Il-modulu bħalissa huwa artifatt lokali. L-organizzazzjoni tippubblika f'ambjent ta' tim/CI
Pakketti Nexus, Artifactory jew GitHub, u mbagħad użah minn hemm.
L-artifact bħalissa huwa lokali. Għat-timijiet u CI, ippubblikaha fuq Nexus tiegħek,
Artifactory, jew repożitorju tal-Pakketti GitHub.

## 5. Konnessjoni Gradle / Dipendenza Gradle

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

## 6. Uża mingħajr Maven/Gradle / Uża mingħajr għodda tal-bini

Applikazzjoni Classpath / Applikazzjoni Classpath:

```bash
javac -cp /path/to/vies-client-1.0.0.jar MyApplication.java
java -cp "/path/to/vies-client-1.0.0.jar:." MyApplication
```

Taħt il-Windows is-separatur tal-classpath huwa `;`, taħt Unix/macOS `:`.
Windows juża `;` bħala s-separatur classpath; Unix/macOS juża `:`.

## 7. Użu tal-modulu JPMS / użu tal-modulu JPMS

Fil-fajl `module-info.java` tal-applikazzjoni:

```java
module my.application {
    requires vies.client;
}
```

Ikkompila u mexxi:

```bash
javac --module-path vies-client-1.0.0.jar -d out src/module-info.java src/my/application/Main.java
java --module-path "vies-client-1.0.0.jar:out" -m my.application/my.application.Main
```

## 8. Windsurf / Kodiċi tal-Istati Uniti

`.vscode/settings.json` fil-proġett jistabbilixxi l-estensjoni Java għal JDK 21.
It-triq Homebrew fiha hija speċifika għall-magna: fuq Intel Mac, Linux jew Windows
erġgħu iktebha fid-direttorju tiegħek JDK 21. Wara l-modifika, mexxi l-
kmand `Developer: Reload Window`.
L-inklużi `.vscode/settings.json` jindika l-estensjoni Java għal JDK 21. Tagħha
It-triq Homebrew hija speċifika għall-magna; ibdelha fuq Intel macOS, Linux, jew Windows.
Mexxi `Developer: Reload Window` wara li tbiddel is-settings tal-JDK.

## 9. Netwerk u prokura / Netwerk u prokura

Endpoint meħtieġ:

```text
https://ec.europa.eu/taxation_customs/vies/rest-api
```

Il-klijent ma jeħtieġx ċavetta API. Għal prokura korporattiva, JDK `HttpClient` l-issettjar tal-prokura għandu jiġi speċifikat fl-ambjent tar-runtime. Proxy tkissir tat-traffiku TLS
F'dan il-każ, iċ-ċertifikat CA tal-kumpanija għandu jiġi installat fil-truststore tal-JDK użat.
Mhi meħtieġa l-ebda ċavetta API. Prokura korporattiva u fiduċja CA għandhom jiġu kkonfigurati fil-
runtime JDK/ambjent.

## 10. Kontroll ta 'installazzjoni ta' malajr / Kontroll tad-duħħan ta 'installazzjoni

```bash
./mvnw -q test
java -cp target/classes examples/ViesDemoServer.java
```

F'terminal ieħor:

```bash
curl "http://localhost:8085/vat-check?number=DE000000000"
```

It-test tad-duħħan ħaj huwa dipendenti fuq in-netwerk; M'għandux ikun obbligatorju fis-CI. It-testijiet normali
huma jużaw server mock lokali.
Il-kontroll tad-duħħan ħaj jiddependi fuq in-netwerk u m'għandux gate CI. Testijiet normali
uża server mock lokali.

## 11. Żbalji komuni / Soluzzjoni ta' problemi

| Żball / Problema                   | Soluzzjoni / Waħħal                                                                                                                                           |
| ---------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `switch expressions ... Java 14`   | L-SDK tal-proġett u l-livell tal-lingwa għandhom ikunu 21 / Issettja l-SDK u l-livell tal-lingwa għal 21                                                      |
| `release version 21 not supported` | Maven jimxi mal-JDK antik; jiffissaw`JAVA_HOME`/ Maven juża JDK antik; fiss`JAVA_HOME`                                                                        |
| `UnsupportedClassVersionError`     | Il-JVM li qed jaħdem għandu jkun 21+ / Mexxi b'JVM 21+                                                                                                        |
| `NETWORK_ERROR`                    | Iċċekkja l-issettjar tad-DNS/proxy/firewall/TLS / Iċċekkja DNS, proxy, firewall, u TLS                                                                        |
| `TIMEOUT`                          | Iżżidx il-timeout indefinittivament; tinvestiga n-network u s-saħħa tal-VIES / Investigate network/VIES health before lifting timeouts                        |
| `CLIENT_OVERLOADED`                | Ipprova mill-ġdid ittardjat, dħul b'limiti, ħaddiema flimkien mal-limitatur globali / Delayed retry, ingress bounded, ħaddiema flimkien mal-limitatur globali |
| `CACHE_ERROR`                      | Iċċekkja Redis timeout/saħħa/metriċi; taqbiżhiex b'sejħa diretta tal-massa VIES / Iċċekkja s-saħħa Redis; ma taqbiżhiex bi stampede VIES                      |
