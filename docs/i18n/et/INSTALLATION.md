# Eesti (et) — Installation

> [Keelevalik](../../LANGUAGES.md) · See lokaliseering parandab ligipääsetavust. Lahknevuse korral kehtib kanooniline ingliskeelne tehniline või õiguslik allikas. Juure `LICENSE` ja`NOTICE` jäävad õiguslikult määravaks.

See moodul sihib Java 21 ja kasutab käitusajal ainult JDK-d. Pole vaja
Spring või välisesse JSON/HTTP teeki.
See moodul sihib Java 21 ja kasutab käitusajal ainult JDK-d. Kevad ja välimine
JSON-/HTTP-teegid pole vajalikud.

## 1. Eeltingimused / Eeldused

- JDK 21 või uuem / JDK 21 või uuem
- Maven 3.9+ lähtetekstide jaoks / Maven 3.9+ lähtetekstide jaoks
- Väljaminev HTTPS-i juurdepääs aadressile `ec.europa.eu:443`/ väljaminev HTTPS-juurdepääs
  Kontrolli/kinnita:

```bash
java -version
javac -version
./mvnw -version
```

Kõik kolm käsku peaksid osutama samale JDK 21+ keskkonnale. Kui Maven on teistsugune
Printige Java versioon, esmalt parandage `JAVA_HOME` väärtus.
Kõik kolm käsku peaksid kasutama sama JDK 21+ installi. Kui Maven teatab a
erinev Java versioon, parandage esmalt `JAVA_HOME`.

## 2. Konfigureerige JDK 21 / Konfigureerige JDK 21

## macOS + Homebrew

```bash
brew install openjdk@21
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

Püsiseadete jaoks muutke kaks rida `export` väärtuseks`~/.zprofile` ja `~/.zshrc` failile. Intel Macis on Homebrewi eesliide tavaliselt `/usr/local`.
Püsiva seadistamise jaoks lisage mõlemad ekspordid kaustadesse `~/.zprofile` ja `~/.zshrc`.
Intel Mac-arvutites on Homebrewi eesliide tavaliselt `/usr/local`.

## Linux

Installige oma distributsiooni OpenJDK 21 pakett, seejärel tehke näiteks järgmist.

```bash
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
```

Installige oma distributsiooni OpenJDK 21 pakett ja suunake sellele `JAVA_HOME`.

## Windows PowerShell

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

Püsiseadete jaoks kasutage Windowsi keskkonnamuutujate liidest.
Kasutage püsiva konfiguratsiooni jaoks Windowsi keskkonnamuutujaid.

## 3. Ehitamine ja kohalik installimine / Ehitamine ja kohalik installimine

Projekti juurtest:

```bash
./mvnw clean verify
./mvnw install
```

-`verify`: tõlkimine, üksuse ja kohaliku integratsiooni testid, JAR-ide loomine.

- `install`: sama, seejärel installige kohalikku`~/.m2/repository` hoidlasse.
- `verify`: kompileerib, käitab üksuse/kohaliku integratsiooni teste ja loob JAR-id.
- `install`: installib artefaktid ka kohalikku Maveni hoidlasse.
  Loodud artefaktid:

```text
target/vies-client-1.2.0.jar
target/vies-client-1.2.0-sources.jar
target/vies-client-1.2.0-javadoc.jar
```

## 4. Maveni ühendus / Maveni sõltuvus

Kui `./mvnw install`/ pärast käivitamist käivitati`./mvnw install` enne:

```xml
<dependency>
    <groupId>vies.client</groupId>
    <artifactId>vies-client</artifactId>
    <version>1.2.0</version>
</dependency>
```

Moodul on praegu kohalik artefakt. Organisatsioon avaldab meeskonna/CI keskkonnas
Nexuse, Artifactory või GitHubi paketid ja seejärel kasutage seda sealt.
Artefakt on praegu kohalik. Meeskondade ja CI jaoks avaldage see oma Nexuses,
Artifactory või GitHubi pakettide hoidla.

## 5. Gradle ühendus / Gradle sõltuvus

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

## 6. Kasutage ilma Maveni/Gradle'ita / Kasutage ilma ehitustööriistata

Klassitee rakendus / klassitee rakendus:

```bash
javac -cp /path/to/vies-client-1.2.0.jar MyApplication.java
java -cp "/path/to/vies-client-1.2.0.jar:." MyApplication
```

Windowsi puhul on klassitee eraldaja `;`, Unixi/macOS-i puhul `:`.
Windows kasutab klassitee eraldajana `;`; Unix/macOS kasutab `:`.

## 7. JPMS-mooduli kasutamine / JPMS-mooduli kasutamine

Rakenduse failis `module-info.java`:

```java
module my.application {
    requires vies.client;
}
```

Kompileerige ja käivitage:

```bash
javac --module-path vies-client-1.2.0.jar -d out src/module-info.java src/my/application/Main.java
java --module-path "vies-client-1.2.0.jar:out" -m my.application/my.application.Main
```

## 8. Purjelaud / USA kood

`.vscode/settings.json` projektis seab Java laienduse väärtusele JDK 21.
Selles olev Homebrew tee on masinaspetsiifiline: Intel Macis, Linuxis või Windowsis
kirjutage see ümber oma JDK 21 kataloogi. Pärast muutmist käivitage `Developer: Reload Window` käsk.
Kaasasolev `.vscode/settings.json` osutab Java laiendile JDK 21. Selle
Homebrew tee on masinaspetsiifiline; asendage see Inteli macOS-is, Linuxis või Windowsis.
Pärast JDK sätete muutmist käivitage `Developer: Reload Window`.

## 9. Võrk ja puhverserver / Võrk ja puhverserver

Nõutav lõpp-punkt:

```text
https://ec.europa.eu/taxation_customs/vies/rest-api
```

Klient ei vaja API-võtit. Ettevõtte puhverserveri jaoks JDK `HttpClient` puhverserveri säte tuleb määrata käituskeskkonnas. Puhverserver rikub TLS-i liiklust
Sel juhul tuleb ettevõtte CA sertifikaat installida kasutatud JDK usaldussalve.
API võtit pole vaja. Ettevõtte puhverserver ja CA usaldus peab olema konfigureeritud
käitusaegne JDK/keskkond.

## 10. Kiire paigalduskontroll / paigaldus suitsukontroll

```bash
./mvnw -q test
java -cp target/classes examples/ViesDemoServer.java
```

Teises terminalis:

```bash
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Reaalajas suitsu test sõltub võrgust; See ei tohiks olla CI-s kohustuslik. Tavalised testid
nad kasutavad kohalikku valeserverit.
Reaalajas suitsukontroll sõltub võrgust ja ei tohiks piirata CI-d. Tavalised testid
kasutage kohalikku näidisserverit.

## 11. Levinud vead / tõrkeotsing

| Viga / probleem                    | Lahendus / Paranda                                                                                                                                                          |
| ---------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `switch expressions ... Java 14`   | Projekti SDK ja keeletase peaksid olema 21 / Määrake SDK ja keeletase väärtusele 21                                                                                         |
| `release version 21 not supported` | Maven töötab vana JDK-ga; fix`JAVA_HOME`/ Maven kasutab vana JDK-d; fikseeritud`JAVA_HOME`                                                                                  |
| `UnsupportedClassVersionError`     | Töötav JVM peaks olema 21+ / Käita JVM-iga 21+                                                                                                                              |
| `NETWORK_ERROR`                    | Kontrollige DNS-i/puhverserveri/tulemüüri/TLS-i sätteid / Kontrollige DNS-i, puhverserverit, tulemüüri ja TLS-i                                                             |
| `TIMEOUT`                          | Ärge suurendage aegumist lõputult; uurige võrku ja VIES-i tervist / Uurige võrgu/VIES-i seisundit enne ajalõpu suurendamist                                                 |
| `CLIENT_OVERLOADED`                | Viivitatud uuesti proovimine, piiratud sissepääs, töötajad pluss globaalne piiraja / Viivitatud uuesti proovimine, piiratud sisenemine, töötajad pluss globaalne piiraja    |
| `CACHE_ERROR`                      | Kontrollige Redise aegumist / tervist / mõõdikuid; ärge jätke sellest mööda massilise otsese VIES-kõnega / Kontrollige Redise tervist; ärge jätke seda VIES-i löögiga mööda |
