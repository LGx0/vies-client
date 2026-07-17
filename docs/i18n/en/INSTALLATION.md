# English (en) — Installation

> [Language selector](../../LANGUAGES.md) · This localization is provided for accessibility. If it differs from the canonical English technical or legal source, the English source governs. The root `LICENSE` and `NOTICE` remain legally authoritative and are not replaced by translations.

This module targets Java 21 and uses only the JDK at runtime. There is no need
to Spring or an external JSON/HTTP library.
This module targets Java 21 and uses only the JDK at runtime. Spring and external
JSON/HTTP libraries are not required.

## 1. Prerequisites

- JDK 21 or newer / JDK 21 or newer
- Maven 3.9+ for source builds / Maven 3.9+ for source builds
- Outbound HTTPS access to the address `ec.europa.eu:443`/ outbound HTTPS access
  Check / Verify:

```bash
java -version
javac -version
./mvnw -version
```

All three commands should point to the same JDK 21+ environment. If Maven is different
Print Java version, first correct the value of `JAVA_HOME`.
All three commands should use the same JDK 21+ installation. If Maven reports a
different Java version, fix `JAVA_HOME` first.

## 2. Configure JDK 21

## macOS + Homebrew

```bash
brew install openjdk@21
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

For a permanent setting, change the two lines `export` to`~/.zprofile` and `~/.zshrc` to a file. On an Intel Mac, the Homebrew prefix is ​​usually `/usr/local`.
For a persistent setup, add both exports to `~/.zprofile` and `~/.zshrc`.
The Homebrew prefix is usually `/usr/local` on Intel Macs.

## Linux

Install the OpenJDK 21 package of your distribution, then for example:

```bash
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
```

Install your distribution's OpenJDK 21 package and point `JAVA_HOME` to it.

## Windows PowerShell

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

For permanent settings, use the Windows Environment Variables interface.
Use Windows Environment Variables for a persistent configuration.

## 3. Build and local installation

From the project root:

```bash
./mvnw clean verify
./mvnw install
```

- `verify`: compiles the project, runs unit and local integration tests, and creates the JARs.

- `install`: same, then install to local`~/.m2/repository` repository.
- `verify`: compiles, runs unit/local integration tests, and creates the JARs.
- `install`: also installs the artifacts into the local Maven repository.
  Generated artifacts:

```text
target/vies-client-1.2.0.jar
target/vies-client-1.2.0-sources.jar
target/vies-client-1.2.0-javadoc.jar
```

## 4. Maven dependency

If `./mvnw install`/ After running`./mvnw install` was run before:

```xml
<dependency>
    <groupId>vies.client</groupId>
    <artifactId>vies-client</artifactId>
    <version>1.2.0</version>
</dependency>
```

The module is currently a local artifact. The organization publishes in a team/CI environment
Nexus, Artifactory or GitHub Packages, and then use it from there.
The artifact is currently local. For teams and CI, publish it to your Nexus,
Artifactory, or GitHub Packages repository.

## 5. Gradle dependency

```kotlin
repositories {
    mavenLocal()
}

dependencies {
    implementation("vies.client:vies-client:1.2.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
```

## 6. Use without Maven/Gradle

Classpath application / Classpath application:

```bash
javac -cp /path/to/vies-client-1.2.0.jar MyApplication.java
java -cp "/path/to/vies-client-1.2.0.jar:." MyApplication
```

Under Windows the classpath separator is `;`, under Unix/macOS `:`.
Windows uses `;` as the classpath separator; Unix/macOS uses `:`.

## 7. JPMS module usage

In the `module-info.java` file of the application:

```java
module my.application {
    requires vies.client;
}
```

Compile and run:

```bash
javac --module-path vies-client-1.2.0.jar -d out src/module-info.java src/my/application/Main.java
java --module-path "vies-client-1.2.0.jar:out" -m my.application/my.application.Main
```

## 8. Windsurf and VS Code

`.vscode/settings.json` in the project sets the Java extension to JDK 21.
The Homebrew path in it is machine-specific: on Intel Mac, Linux or Windows
rewrite it to your own JDK 21 directory. After modification, run the `Developer: Reload Window` command.
The included `.vscode/settings.json` points the Java extension to JDK 21. Its
Homebrew path is machine-specific; replace it on Intel macOS, Linux, or Windows.
Run `Developer: Reload Window` after changing JDK settings.

## 9. Network and proxy

Required endpoint:

```text
https://ec.europa.eu/taxation_customs/vies/rest-api
```

The client does not require an API key. For corporate proxy, JDK `HttpClient` proxy setting must be specified in the runtime environment. Proxy breaking TLS traffic
In this case, the company CA certificate must be installed in the truststore of the used JDK.
No API key is required. Corporate proxy and CA trust must be configured in the
runtime JDK/environment.

## 10. Installation smoke check

```bash
./mvnw -q test
java -cp target/classes examples/ViesDemoServer.java
```

In another terminal:

```bash
curl "http://localhost:8085/vat-check?number=DE000000000"
```

The live smoke test is network dependent; It should not be mandatory in CI. The normal tests
they use a local mock server.
The live smoke check depends on the network and should not gate CI. Normal tests
use a local mock server.

## 11. Troubleshooting

| Error / Problem                    | Solution / Fix                                                                                                                               |
| ---------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| `switch expressions ... Java 14`   | Project SDK and language level should be 21 / Set SDK and language level to 21                                                               |
| `release version 21 not supported` | Maven runs with the old JDK; fix`JAVA_HOME`/ Maven uses an old JDK; fixed`JAVA_HOME`                                                         |
| `UnsupportedClassVersionError`     | The running JVM should be 21+ / Run with JVM 21+                                                                                             |
| `NETWORK_ERROR`                    | Check DNS/proxy/firewall/TLS setting / Check DNS, proxy, firewall, and TLS                                                                   |
| `TIMEOUT`                          | Do not increase the timeout indefinitely; investigate the network and VIES health / Investigate network/VIES health before raising timeouts  |
| `CLIENT_OVERLOADED`                | Delayed retry, bounded ingress, workers plus global limiter / Delayed retry, bounded ingress, workers plus global limiter                    |
| `CACHE_ERROR`                      | Check Redis timeout/health/metrics; don't bypass it with a mass direct VIES call / Check Redis health; do not bypass it with a VIES stampede |
