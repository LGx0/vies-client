# Suomi (fi) — Installation

> [Kielivalitsin](../../LANGUAGES.md) · Tämä lokalisointi parantaa saavutettavuutta. Jos se poikkeaa kanonisesta englanninkielisestä teknisestä tai oikeudellisesta lähteestä, englanninkielinen lähde määrää. Juuren `LICENSE` ja`NOTICE` ovat oikeudellisesti määrääviä.

Tämä moduuli on kohdistettu Java 21:een ja käyttää vain JDK:ta ajon aikana. Ei ole tarvetta
Springiin tai ulkoiseen JSON/HTTP-kirjastoon.
Tämä moduuli on kohdistettu Java 21:een ja käyttää vain JDK:ta ajon aikana. Kevät ja ulkona
JSON/HTTP-kirjastoja ei vaadita.

## 1. Esitiedot / Esitiedot

- JDK 21 tai uudempi / JDK 21 tai uudempi
- Maven 3.9+ lähdeversioille / Maven 3.9+ lähdeversioille
- Lähtevä HTTPS-yhteys osoitteeseen `ec.europa.eu:443`/ lähtevä HTTPS-yhteys
  Tarkista / vahvista:

```bash
java -version
javac -version
./mvnw -version
```

Kaikkien kolmen komennon tulee osoittaa samaan JDK 21+ -ympäristöön. Jos Maven on erilainen
Tulosta Java-versio, korjaa ensin `JAVA_HOME`:n arvo.
Kaikkien kolmen komennon tulee käyttää samaa JDK 21+ -asennusta. Jos Maven raportoi a
eri Java-versio, korjaa ensin `JAVA_HOME`.

## 2. Määritä JDK 21 / Määritä JDK 21

## macOS + Homebrew

```bash
brew install openjdk@21
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

Pysyvän asetuksen saamiseksi vaihda kaksi riviä `export` muotoon`~/.zprofile` ja `~/.zshrc` tiedostoon. Intel Macissa Homebrew-etuliite on yleensä `/usr/local`.
Pysyvän asennuksen saamiseksi lisää molemmat vientitiedostot tiedostoihin `~/.zprofile` ja `~/.zshrc`.
Homebrew-etuliite on yleensä `/usr/local` Intel Maceissa.

## Linux

Asenna jakelusi OpenJDK 21 -paketti ja sitten esimerkiksi:

```bash
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
```

Asenna jakelusi OpenJDK 21 -paketti ja osoita `JAVA_HOME` siihen.

## Windows PowerShell

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

Pysyviä asetuksia varten käytä Windowsin ympäristömuuttujien käyttöliittymää.
Käytä Windowsin ympäristömuuttujia pysyvään kokoonpanoon.

## 3. Rakenna ja paikallinen asennus / Rakenna ja paikallinen asennus

Projektin juuresta:

```bash
./mvnw clean verify
./mvnw install
```

-`verify`: käännös, yksikkö- ja paikallisintegraatiotestit, JAR:ien luominen.

- `install`: sama, asenna sitten paikalliseen`~/.m2/repository`-arkistoon.
- `verify`: kokoaa, suorittaa yksikkö/paikallinen integraatiotestejä ja luo JAR:t.
- `install`: myös asentaa artefaktit paikalliseen Maven-arkistoon.
  Luodut artefaktit:

```text
target/vies-client-1.0.0.jar
target/vies-client-1.0.0-sources.jar
target/vies-client-1.0.0-javadoc.jar
```

## 4. Maven-yhteys / Maven-riippuvuus

Jos `./mvnw install`/ ajon jälkeen`./mvnw install` ajettiin ennen:

```xml
<dependency>
    <groupId>vies.client</groupId>
    <artifactId>vies-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

Moduuli on tällä hetkellä paikallinen artefakti. Organisaatio julkaisee tiimi/CI-ympäristössä
Nexus-, Artifactory- tai GitHub-paketit ja käytä niitä sitten sieltä.
Esine on tällä hetkellä paikallinen. Jos kyseessä on tiimit ja CI, julkaise se Nexukselle,
Artifactory- tai GitHub-pakettien arkisto.

## 5. Gradle-liitäntä / Gradle-riippuvuus

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

## 6. Käytä ilman Maven/Gradlea / Käytä ilman rakennustyökalua

Classpath-sovellus / Classpath-sovellus:

```bash
javac -cp /path/to/vies-client-1.0.0.jar MyApplication.java
java -cp "/path/to/vies-client-1.0.0.jar:." MyApplication
```

Windowsissa luokkapolun erotin on `;` ja Unixissa/macOS:ssä `:`.
Windows käyttää `;` luokkapolun erottimena; Unix/macOS käyttää `:`:tä.

## 7. JPMS-moduulin käyttö / JPMS-moduulin käyttö

Sovelluksen `module-info.java`-tiedostossa:

```java
module my.application {
    requires vies.client;
}
```

Kääntää ja ajaa:

```bash
javac --module-path vies-client-1.0.0.jar -d out src/module-info.java src/my/application/Main.java
java --module-path "vies-client-1.0.0.jar:out" -m my.application/my.application.Main
```

## 8. Purjelautailu / USA Code

Projektin `.vscode/settings.json` asettaa Java-laajennukseksi JDK 21.
Siinä oleva Homebrew-polku on konekohtainen: Intel Macissa, Linuxissa tai Windowsissa
kirjoita se uudelleen omaan JDK 21 -hakemistoosi. Suorita muokkauksen jälkeen `Developer: Reload Window`-komento.
Mukana oleva `.vscode/settings.json` osoittaa Java-laajennuksen JDK 21:een
Homebrew-polku on konekohtainen; vaihda se Intel macOS:ssä, Linuxissa tai Windowsissa.
Suorita `Developer: Reload Window` JDK-asetusten muuttamisen jälkeen.

## 9. Verkko ja välityspalvelin / Verkko ja välityspalvelin

Vaadittu päätepiste:

```text
https://ec.europa.eu/taxation_customs/vies/rest-api
```

Asiakas ei vaadi API-avainta. Yrityksen välityspalvelimelle JDK `HttpClient` välityspalvelinasetus on määritettävä ajonaikaisessa ympäristössä. Välityspalvelin rikkoo TLS-liikennettä
Tässä tapauksessa yrityksen CA-sertifikaatti on asennettava käytetyn JDK:n luotettavuussäilöön.
API-avainta ei vaadita. Yrityksen välityspalvelin ja varmentajan luottamus on määritettävä
ajonaikainen JDK/ympäristö.

## 10. Pikaasennuksen tarkistus / Asennuksen savutarkistus

```bash
./mvnw -q test
java -cp target/classes examples/ViesDemoServer.java
```

Toisessa terminaalissa:

```bash
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Live savutesti on verkkoriippuvainen; Sen ei pitäisi olla pakollinen CI:ssä. Normaalit testit
he käyttävät paikallista valepalvelinta.
Reaaliaikainen savutarkistus riippuu verkosta, eikä sen pitäisi olla CI:n portti. Normaalit testit
käytä paikallista valepalvelinta.

## 11. Yleiset virheet / Vianetsintä

| Virhe / Ongelma                    | Ratkaisu / Korjaa                                                                                                                                                             |
| ---------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `switch expressions ... Java 14`   | Projektin SDK:n ja kielitason tulee olla 21 / Aseta SDK:ksi ja kielitasoksi 21                                                                                                |
| `release version 21 not supported` | Maven toimii vanhan JDK:n kanssa; korjata`JAVA_HOME`/ Maven käyttää vanhaa JDK:ta; kiinteä`JAVA_HOME`                                                                         |
| `UnsupportedClassVersionError`     | Käynnissä olevan JVM:n tulee olla 21+ / Suorita JVM:llä 21+                                                                                                                   |
| `NETWORK_ERROR`                    | Tarkista DNS/välityspalvelin/palomuuri/TLS-asetukset / Tarkista DNS, välityspalvelin, palomuuri ja TLS                                                                        |
| `TIMEOUT`                          | Älä pidennä aikakatkaisua loputtomiin; tutki verkkoa ja VIES:n kuntoa / Tutki verkon/VIES:n kuntoa ennen aikakatkaisujen lisäämistä                                           |
| `CLIENT_OVERLOADED`                | Viivästetty uudelleenyritys, rajoitettu sisääntulo, työntekijät plus yleinen rajoitin / Viivästetty uudelleenyritys, rajoitettu sisääntulo, työntekijät plus yleinen rajoitin |
| `CACHE_ERROR`                      | Tarkista Redis-aikakatkaisu/terveys/mittarit; älä ohita sitä massasuoralla VIES-kutsulla / Tarkista Redisin kunto; älä ohita sitä VIES-iskulla                                |
