# Installation — Java 21+, release 1.2.1

[Languages](../../LANGUAGES.md) · [Complete quick start](../../QUICK_START.md)

The library needs only JDK 21+ at runtime. It has no Spring, JSON-library or HTTP-library
runtime dependency. Development uses JDK 25 and verifies compatibility on JDK 21.

## Prebuilt release

Download the binary, sources, Javadoc and SHA256SUMS from
[GitHub release v1.2.1](https://github.com/LGx0/vies-client/releases/tag/v1.2.1).
The [quick start](../../QUICK_START.md) gives complete download, checksum,
provenance, and Maven install-file commands. The artifact is not on Maven Central:
install it locally or publish it to your organization's artifact repository first.

After installation, use:

```xml
<dependency>
  <groupId>vies.client</groupId>
  <artifactId>vies-client</artifactId>
  <version>1.2.1</version>
</dependency>
```

For Gradle, configure `mavenLocal()` or your organization's repository and use
`implementation("vies.client:vies-client:1.2.1")`.

## Source build

```bash
git clone https://github.com/LGx0/vies-client.git
cd vies-client
git checkout v1.2.1
java -version
javac -version
./mvnw -version
./mvnw --batch-mode --no-transfer-progress clean install
java -cp target/vies-client-1.2.1.jar examples/CheckVat.java not-a-vat
```

On Windows use `mvnw.cmd`. All version commands must select the same JDK.
The wrapper verifies its pinned Maven distribution checksum before use.
The final command tests invalid input offline and never calls VIES.

The build produces binary, sources, and Javadoc JARs under `target/`. JARs include
legal notices but not GitHub automation, local audit evidence, IDE files, or docs.
GitHub's automatic source ZIP represents the full repository and therefore includes
repository support files such as `.github` and `.mvn`.

## Classpath and JPMS

Without Maven or Gradle:

```bash
javac -cp vies-client-1.2.1.jar MyApplication.java
java -cp "vies-client-1.2.1.jar:." MyApplication
```

Use `;` instead of `:` as the classpath separator on Windows.

A modular application declares:

```java
module my.application {
    requires vies.client;
}
```

The library exports only `vies.client` and requires `java.net.http`. Its internal
JSON/cache package is not exported. The test fixture's `jdk.httpserver` is not a
published module dependency.

## IDE, network, and troubleshooting

Select Java language level 21 and JDK 25 for development. Machine-specific
`.vscode` settings are not distributed. Maven is authoritative: if the editor reports
switch/pattern errors, check the selected JDK and run a clean Maven verification.
Do not diagnose source errors from stale Eclipse/JDT files under `target/`.

Runtime needs outbound HTTPS access to `ec.europa.eu:443`; no VIES API key is
required. Configure corporate proxy/trust settings in the JDK environment without
disabling TLS verification. Keep custom base URLs trusted.

See the [quick start](../../QUICK_START.md) for error-code troubleshooting and the
[full bilingual installation guide](../../INSTALLATION.md) for platform-specific
JDK setup and network details.
