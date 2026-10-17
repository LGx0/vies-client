# Telepítés / Installation

Kész kiadásból telepítés, checksum/eredetellenőrzés és teljes futtatható példa:
[Gyors indulás / Quick start](QUICK_START.md). A könyvtár nincs Maven Centralon.

Ez a modul Java 21-re fordul, és futásidőben csak a JDK-t használja. Nincs szükség
Springre vagy külső JSON/HTTP könyvtárra.

This module targets Java 21 and uses only the JDK at runtime. Spring and external
JSON/HTTP libraries are not required.

## 1. Előfeltételek / Prerequisites

- JDK 21 vagy újabb / JDK 21 or newer
- a checksum-rögzített Maven Wrapper használata ajánlott; külön Maven opcionális
- use the checksum-pinned Maven Wrapper; a separate Maven install is optional
- Kimenő HTTPS-hozzáférés az `ec.europa.eu:443` címhez / outbound HTTPS access

Ellenőrzés / Verify:

```bash
java -version
javac -version
./mvnw -version          # Windows: .\mvnw.cmd -version
```

Mindhárom parancsnak ugyanazt a JDK 21+ környezetet kell mutatnia. Ha a Maven más
Java-verziót ír ki, előbb a `JAVA_HOME` értékét javítsd.

All three commands should use the same JDK 21+ installation. If Maven reports a
different Java version, fix `JAVA_HOME` first.

## 2. Fejlesztői JDK 25, Java 21 cél / Development JDK 25, Java 21 target

A könyvtár Java 21-es bytecode-ot ad ki, de fejlesztéshez a legfrissebb LTS-t,
a JDK 25-öt használjuk. A CI mindkét verzión lefuttatja ugyanazt a tesztcsomagot,
a kiadási build pedig JDK 21-en is igazolja a minimális futtatási követelményt.

The library emits Java 21 bytecode, while development uses the latest LTS, JDK 25.
CI runs the same test suite on both versions, and the release build also runs on
JDK 21 to enforce the minimum runtime requirement.

### macOS + Homebrew

```bash
brew install openjdk openjdk@25 openjdk@21
export JAVA_HOME="/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

A legfrissebb nem-LTS JDK opcionális kompatibilitási ellenőrzéshez telepíthető az
`openjdk` csomaggal. A VS Code workspace a Java 21, 25 és 26 környezetet is
regisztrálja, de alapértelmezetten a JDK 25 LTS-t használja.

The latest non-LTS JDK can be installed with the `openjdk` package for optional
forward-compatibility checks. The VS Code workspace registers Java 21, 25 and 26,
while keeping JDK 25 LTS as the default.

Tartós beállításhoz a két `export` sort tedd a `~/.zprofile` és `~/.zshrc`
fájlba. Intel Macen a Homebrew prefix rendszerint `/usr/local`.

For a persistent setup, add both exports to `~/.zprofile` and `~/.zshrc`.
The Homebrew prefix is usually `/usr/local` on Intel Macs.

### Linux

Telepítsd a disztribúciód OpenJDK 25 és OpenJDK 21 csomagját, majd fejlesztéshez például:

```bash
export JAVA_HOME="/usr/lib/jvm/java-25-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
```

Install your distribution's OpenJDK 25 and OpenJDK 21 packages. Point `JAVA_HOME`
to JDK 25 for development; retain JDK 21 for compatibility verification.

### Windows PowerShell

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25"
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

- `verify`: fordítás, unit és helyi integrációs tesztek, JAR-ok létrehozása.
- `install`: ugyanaz, majd telepítés a lokális `~/.m2/repository` tárba.
- `verify`: compiles, runs unit/local integration tests, and creates the JARs.
- `install`: also installs the artifacts into the local Maven repository.

The wrapper downloads Maven 3.9.16 only after verifying its pinned SHA-256 checksum.
A Wrapper-verzió és a Maven-disztribúció checksumja a `.mvn/wrapper/` alatt rögzített.

Létrejövő fájlok / Generated artifacts:

```text
target/vies-client-1.2.1.jar
target/vies-client-1.2.1-sources.jar
target/vies-client-1.2.1-javadoc.jar
```

## 4. Maven bekötés / Maven dependency

Ha előtte lefutott a `./mvnw install` / After running `./mvnw install`:

```xml
<dependency>
    <groupId>vies.client</groupId>
    <artifactId>vies-client</artifactId>
    <version>1.2.1</version>
</dependency>
```

A modul jelenleg lokális artifact. Csapat/CI környezetben publikáld a szervezet
Nexus, Artifactory vagy GitHub Packages tárába, majd onnan használd.

The artifact is currently local. For teams and CI, publish it to your Nexus,
Artifactory, or GitHub Packages repository.

## 5. Gradle bekötés / Gradle dependency

```kotlin
repositories {
    mavenLocal()
}

dependencies {
    implementation("vies.client:vies-client:1.2.1")
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
javac -cp /path/to/vies-client-1.2.1.jar MyApplication.java
java -cp "/path/to/vies-client-1.2.1.jar:." MyApplication
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
javac --module-path vies-client-1.2.1.jar -d out src/module-info.java src/my/application/Main.java
java --module-path "vies-client-1.2.1.jar:out" -m my.application/my.application.Main
```

## 8. Windsurf / VS Code

A gépspecifikus `.vscode/settings.json` nincs a publikus repóban. Állítsd az IDE
Java language leveljét 21-re, a fejlesztői JDK-t 25-re, és regisztráld a helyi
Java 21 futtatókörnyezetet kompatibilitási ellenőrzéshez. Módosítás után futtasd a
`Developer: Reload Window` parancsot.

Machine-specific `.vscode/settings.json` is not part of the public repository.
Set the IDE language level to 21, select JDK 25 for development, and register your
local JDK 21 installation for compatibility checks.
Run `Developer: Reload Window` after changing JDK settings.

A projektben a Maven a build hiteles forrása. Érdemes az automatikus IDE Java
háttér-buildet kikapcsolni. A `pom.xml` módosítása után futtasd a
`Java: Reload Projects` parancsot. Ha az editor hibát jelez, de a forrás helyesnek
tűnik, ne a `target/` alatti osztályfájlokból indulj ki: először ellenőrizd, hogy a
`java`, `javac` és Maven ugyanazt a kiválasztott JDK-t használja, majd futtasd:

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
```

Maven is the build authority for this project; disabling the IDE's automatic Java
background build avoids competing outputs. Run `Java: Reload Projects` after changing `pom.xml`. If the editor
shows errors while the source looks correct, do not rely on class files under `target/`:
first make sure `java`, `javac` and Maven use the same selected JDK, then run the clean verification
command above. This removes stale Eclipse/JDT error stubs before Maven recompiles the project.

A gyökérszintű `examples/` önálló, source-file módban futtatható példákat tartalmaz,
nem Maven-forráskészlet. Futtatásukhoz add meg a JAR-t a classpath-on; az offline
smoke parancs a [gyors indulásban](QUICK_START.md) szerepel.

The root-level `examples/` directory contains standalone source-file examples,
not a Maven source set. Supply the JAR on their classpath; the
[quick start](QUICK_START.md) includes an offline smoke command.

## 9. Hálózat és proxy / Network and proxy

Engedélyezendő végpont / Required endpoint:

```text
https://ec.europa.eu/taxation_customs/vies/rest-api
```

A kliens nem igényel API-kulcsot. Vállalati proxy esetén a JDK `HttpClient`
proxy-beállítását a futtatási környezetben kell megadni. TLS-forgalmat bontó proxy
esetén a vállalati CA tanúsítványt a használt JDK truststore-jába kell telepíteni.

No API key is required. Corporate proxy and CA trust must be configured in the
runtime JDK/environment.

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

The live smoke check depends on the network and should not gate CI. Normal tests
use a local mock server.

## 11. Gyakori hibák / Troubleshooting

| Hiba / Problem | Megoldás / Fix |
|---|---|
| `switch expressions ... Java 14` | Project SDK és language level legyen 21 / Set SDK and language level to 21 |
| `release version 21 not supported` | A Maven régi JDK-val fut; javítsd a `JAVA_HOME`-ot / Maven uses an old JDK; fix `JAVA_HOME` |
| `UnsupportedClassVersionError` | A futtató JVM legyen 21+ / Run with JVM 21+ |
| `NETWORK_ERROR` | Ellenőrizd a DNS/proxy/firewall/TLS beállítást / Check DNS, proxy, firewall, and TLS |
| `TIMEOUT` | Ne emeld korlátlanul a timeoutot; vizsgáld a hálózatot és a VIES állapotát / Investigate network/VIES health before raising timeouts |
| `CLIENT_OVERLOADED` | Késleltetett retry, kisebb ingress, több worker + globális limiter / Delayed retry, bounded ingress, workers plus global limiter |
| `CACHE_ERROR` | Redis timeout/health/metrics ellenőrzése; ne kerüld meg tömeges közvetlen VIES-hívással / Check Redis health; do not bypass it with a VIES stampede |
