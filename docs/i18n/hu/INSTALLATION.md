# Magyar (hu) — Installation

> [Nyelvválasztó](../../LANGUAGES.md) · Ez a lokalizáció az elérhetőséget szolgálja. Eltérés esetén a kanonikus angol technikai vagy jogi forrás az irányadó. A gyökér `LICENSE` és`NOTICE` jogilag irányadó, fordítás nem helyettesíti.

Ez a modul Java 21-re fordul, és futásidőben csak a JDK-t használja. Nincs szükség
Springre vagy külső JSON/HTTP könyvtárra.
Ez a modul a Java 21-et célozza meg, és csak a JDK-t használja futás közben. Tavaszi és külső
JSON/HTTP-könyvtárak nem szükségesek.

## 1. Előfeltételek / Prerequisites

- JDK 21 vagy újabb / JDK 21 or newer
- Maven 3.9+ a forrásból történő buildhez / Maven 3.9+ for source builds
- Kimenő HTTPS-hozzáférés az `ec.europa.eu:443` címhez / outbound HTTPS access
  Ellenőrzés / Verify:

```bash
java -version
javac -version
./mvnw -version
```

Mindhárom parancsnak ugyanazt a JDK 21+ környezetet kell mutatnia. Ha a Maven más
Java-verziót ír ki, előbb a `JAVA_HOME` értékét javítsd.
Mindhárom parancsnak ugyanazt a JDK 21+ telepítést kell használnia. Ha Maven beszámol a
eltérő Java verziót, először javítsa ki a `JAVA_HOME` fájlt.

## 2. JDK 21 beállítása / Configure JDK 21

## macOS + Homebrew

```bash
brew install openjdk@21
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

Tartós beállításhoz a két `export` sort tedd a`~/.zprofile` és `~/.zshrc` fájlba. Intel Macen a Homebrew prefix rendszerint `/usr/local`.
A folyamatos beállításhoz adja hozzá mindkét exportálást a `~/.zprofile` és a `~/.zshrc` fájlokhoz.
A Homebrew előtag általában `/usr/local` Intel Mac számítógépeken.

## Linux

Telepítsd a disztribúciód OpenJDK 21 csomagját, majd például:

```bash
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
```

Telepítsd a disztribúciód OpenJDK 21 csomagját, és mutasd rá a `JAVA_HOME`-t.

## Windows PowerShell

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

Állandó beállításhoz használd a Windows Environment Variables felületét.
Use Windows Environment Variables for a persistent configuration.

## 3. Build és lokális telepítés / Build and local installation

A projekt gyökerében / From the project root:

```bash
./mvnw clean verify
./mvnw install
```

-`verify`: fordítás, unit és helyi integrációs tesztek, JAR-ok létrehozása.

- `install`: ugyanaz, majd telepítés a lokális`~/.m2/repository` tárba.
- `verify`: compiles, runs unit/local integration tests, and creates the JARs.
- `install`: also installs the artifacts into the local Maven repository.
  Létrejövő fájlok / Generated artifacts:

```text
target/vies-client-1.0.0.jar
target/vies-client-1.0.0-sources.jar
target/vies-client-1.0.0-javadoc.jar
```

## 4. Maven bekötés / Maven dependency

Ha előtte lefutott a `./mvnw install`/ After running`./mvnw install`:

```xml
<dependency>
    <groupId>vies.client</groupId>
    <artifactId>vies-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

A modul jelenleg lokális artifact. Csapat/CI környezetben publikáld a szervezet
Nexus, Artifactory vagy GitHub Packages tárába, majd onnan használd.
A műtárgy jelenleg helyi. Csapatok és CI esetén tegye közzé Nexus készülékén,
Artifactory vagy GitHub Packages tárház.

## 5. Gradle bekötés / Gradle dependency

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

## 6. Maven/Gradle nélküli használat / Use without a build tool

Classpath alkalmazás / Classpath application:

```bash
javac -cp /path/to/vies-client-1.0.0.jar MyApplication.java
java -cp "/path/to/vies-client-1.0.0.jar:." MyApplication
```

Windows alatt a classpath elválasztó `;`, Unix/macOS alatt `:`.
Windows uses `;` as the classpath separator; Unix/macOS uses `:`.

## 7. JPMS modul használata / JPMS module usage

Az alkalmazás `module-info.java` fájljában:

```java
module my.application {
    requires vies.client;
}
```

Fordítás és futtatás / Compile and run:

```bash
javac --module-path vies-client-1.0.0.jar -d out src/module-info.java src/my/application/Main.java
java --module-path "vies-client-1.0.0.jar:out" -m my.application/my.application.Main
```

## 8. Windsurf / USA Code

A projektben található `.vscode/settings.json` JDK 21-re állítja a Java extensiont.
A benne lévő Homebrew útvonal gépspecifikus: Intel Macen, Linuxon vagy Windowson
írd át a saját JDK 21 könyvtáradra. Módosítás után futtasd a `Developer: Reload Window` parancsot.
A mellékelt `.vscode/settings.json` a Java kiterjesztést a JDK 21-re irányítja
A Homebrew elérési útja gépspecifikus; cserélje ki Intel macOS, Linux vagy Windows rendszeren.
A JDK-beállítások módosítása után futtassa a `Developer: Reload Window`-t.

## 9. Hálózat és proxy / Network and proxy

Engedélyezendő végpont / Required endpoint:

```text
https://ec.europa.eu/taxation_customs/vies/rest-api
```

A kliens nem igényel API-kulcsot. Vállalati proxy esetén a JDK `HttpClient` proxy-beállítását a futtatási környezetben kell megadni. TLS-forgalmat bontó proxy
esetén a vállalati CA tanúsítványt a használt JDK truststore-jába kell telepíteni.
Nincs szükség API-kulcsra. A vállalati proxyt és a hitelesítésszolgáltatói bizalmat konfigurálni kell a
futásidejű JDK/környezet.

## 10. Gyors telepítési ellenőrzés / Installation smoke check

```bash
./mvnw -q test
java -cp target/classes examples/ViesDemoServer.java
```

Másik terminálban / In another terminal:

```bash
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Az éles smoke teszt hálózatfüggő; CI-ben ne legyen kötelező. A normál tesztek
helyi mock szervert használnak.
Az élő füstellenőrzés a hálózattól függ, és nem szabad, hogy a CI-t bekapja. Normál tesztek
helyi álszervert használjon.

## 11. Gyakori hibák / Troubleshooting

| Hiba / Problem                     | Megoldás / Fix                                                                                                                                      |
| ---------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------- |
| `switch expressions ... Java 14`   | Project SDK és language level legyen 21 / Set SDK and language level to 21                                                                          |
| `release version 21 not supported` | A Maven régi JDK-val fut; javítsd a`JAVA_HOME`-ot / Maven uses an old JDK; fix`JAVA_HOME`                                                           |
| `UnsupportedClassVersionError`     | A futtató JVM legyen 21+ / Run with JVM 21+                                                                                                         |
| `NETWORK_ERROR`                    | Ellenőrizd a DNS/proxy/firewall/TLS beállítást / Check DNS, proxy, firewall, and TLS                                                                |
| `TIMEOUT`                          | Ne emeld korlátlanul a timeoutot; vizsgáld a hálózatot és a VIES állapotát / Investigate network/VIES health before raising timeouts                |
| `CLIENT_OVERLOADED`                | Késleltetett retry, kisebb ingress, több worker + globális limiter / Delayed retry, bounded ingress, workers plus global limiter                    |
| `CACHE_ERROR`                      | Redis timeout/health/metrics ellenőrzése; ne kerüld meg tömeges közvetlen VIES-hívással / Check Redis health; do not bypass it with a VIES stampede |
