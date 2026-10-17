# Gyors indulás / Quick start — 1.2.1

Java 21+ szükséges, nincs runtime függőség. A könyvtár önálló VIES-kliens, nem
hostolt API és nem adókalkulátor. A teljes angol útmutató az alábbi parancsokat
Linuxon és macOS-en közvetlenül használható formában adja meg.

You need JDK 21 or newer. Development/analysis uses JDK 25; published classes target
Java 21. This is a client library, not a hosted API or a general tax calculator.
There are no runtime dependencies. It is distributed through GitHub Releases and
source builds; **it is not published on Maven Central**.

## 1. Letöltés és ellenőrzés / Download and verify

Download the binary, sources, Javadoc and `SHA256SUMS` from
[release v1.2.1](https://github.com/LGx0/vies-client/releases/tag/v1.2.1).
Linux/macOS commands (run in a new empty directory):

```bash
for file in vies-client-1.2.1.jar vies-client-1.2.1-sources.jar vies-client-1.2.1-javadoc.jar SHA256SUMS; do
  curl --fail --location --proto '=https' --remote-name \
    "https://github.com/LGx0/vies-client/releases/download/v1.2.1/$file"
done
shasum -a 256 -c SHA256SUMS
```

Linuxon `sha256sum -c SHA256SUMS` is használható. Windows PowerShellben töltsd le
ugyanezeket a fájlokat, és hasonlítsd össze a `Get-FileHash -Algorithm SHA256`
eredményét a manifesttel. A checksum átviteli hibát észlel; a build eredetét külön
a GitHub CLI igazolja:

On Linux, `sha256sum -c SHA256SUMS` is equivalent. On Windows PowerShell, download
the same files and compare `Get-FileHash -Algorithm SHA256` with the manifest.
Checksums detect corruption. If you have GitHub CLI, verify repository provenance:

```bash
gh attestation verify vies-client-1.2.1.jar --repo LGx0/vies-client
gh attestation verify vies-client-1.2.1-sources.jar --repo LGx0/vies-client
gh attestation verify vies-client-1.2.1-javadoc.jar --repo LGx0/vies-client
```

## 2. Maven vagy Gradle / Maven or Gradle

A dependency csak helyi telepítés vagy saját artifact-tár után oldható fel.
Install the verified release into your local Maven repository:

```bash
mvn org.apache.maven.plugins:maven-install-plugin:3.1.4:install-file \
  -Dfile=vies-client-1.2.1.jar \
  -Dsources=vies-client-1.2.1-sources.jar \
  -Djavadoc=vies-client-1.2.1-javadoc.jar \
  -DgroupId=vies.client -DartifactId=vies-client -Dversion=1.2.1 -Dpackaging=jar
```

Maven project dependency:

```xml
<dependency>
  <groupId>vies.client</groupId>
  <artifactId>vies-client</artifactId>
  <version>1.2.1</version>
</dependency>
```

Gradle Kotlin DSL:

```kotlin
repositories { mavenLocal() }
dependencies { implementation("vies.client:vies-client:1.2.1") }
java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }
```

Csapat/CI használatnál a saját Nexus/Artifactory vagy más artifact-táradba publikálj;
a fejlesztői gép `mavenLocal()` tárát a CI nem látja.
For teams, publish to your organization's artifact repository; CI cannot see a
developer's local Maven cache.

## 3. Forrásból build / Build from source

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

Windows: `./mvnw` helyett `mvnw.cmd`. A három verzióparancs ugyanazt a JDK-t használja.
Az utolsó parancs **offline** ellenőrzés: kétnyelvű `INVALID_VAT_FORMAT` eredményt ad,
nem hívja a VIES-t. Valódi lekérdezéshez a `not-a-vat` helyére jogosan ellenőrzött
teljes EU-adószámot adj; `MY_EU_VAT_NUMBER` opcionálisan a saját requester számod.

On Windows use `mvnw.cmd`. All three version commands must use the same JDK. The
last command is an **offline sanity check** and prints a bilingual
`INVALID_VAT_FORMAT`; it makes no VIES request. Replace `not-a-vat` with a legitimate
full EU VAT identifier for a live check. Optionally set `MY_EU_VAT_NUMBER` to your
own authorized requester VAT. The complete example is [CheckVat.java](../examples/CheckVat.java).

## 4. Alkalmazásba bekötés / Integrate into an application

```java
try (var client = vies.client.ViesClient.builder().build()) {
    var result = client.check("not-a-vat"); // offline example / offline példa
    result.error().ifPresent(error -> {
        System.out.println(error.code());
        System.out.println(error.messageHu());
        System.out.println(error.messageEn());
    });
}
```

Szerverben egyetlen hosszú életű, megosztott klienspéldányt használj, és csak az
alkalmazás leállításakor hívd a `close()`-t. Kezeld a `Valid`, `Invalid`,
`Unavailable` és `MalformedInput` kimenetet külön. Az `isValid()==false` önmagában
nem bizonyítja, hogy az adószám érvénytelen: technikai hiba is lehet.

In a server, create one shared long-lived client and close it at application shutdown.
Handle `Valid`, `Invalid`, `Unavailable`, and `MalformedInput` separately. A false
`isValid()` alone does not establish an invalid VAT number; it can also mean a
technical failure. Cache hits carry the original consultation timestamp/ID.

`checkAsync()` returns a cancellation-safe future. Do not accumulate millions of
futures: use a bounded processing window, durable queue, shared cache and global
limiter across JVMs. See [integration](INTEGRATION.md),
[technical details](TECHNICAL.md), [tests](TESTING.md), and
[security risks](SECURITY_REVIEW.md). No throughput claim replaces load/soak
measurement against your own mock/staging environment.

## 5. Hibaelhárítás / Troubleshooting

| Tünet / Symptom | Teendő / Action |
|---|---|
| Maven cannot resolve `vies.client` | Install the release/source locally or configure your organization's repository; this artifact is not on Central |
| Switch/text-block syntax errors | Select JDK 21+ for the IDE language level, compiler and Maven; run a clean Maven build |
| `CLIENT_OVERLOADED` | Reduce the consumer window; retry later with backoff |
| `CACHE_ERROR` | Fix the cache adapter/connectivity; cache failures intentionally do not stampede VIES |
| `TIMEOUT`, `NETWORK_ERROR`, `HTTP_429`, `MS_UNAVAILABLE` | No validity decision; respect `retryable`, limit retries and check upstream health |
| `CLIENT_CLOSED` / `IllegalStateException` | Reuse the client for the whole application lifecycle; no calls after close |
| No consultation ID/name/address | Optional upstream fields; never invent missing values |

Apache-2.0: keep LICENSE/NOTICE when redistributing. Translations are selected at
[Languages](LANGUAGES.md). Donations are optional at
[Buy Me a Coffee](https://buymeacoffee.com/lgx0); use does not require payment.
