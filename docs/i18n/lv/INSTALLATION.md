# Latviešu (lv) — Installation

> [Valodu izvēle](../../LANGUAGES.md) · Šī lokalizācija uzlabo pieejamību. Atšķirību gadījumā noteicošais ir kanoniskais angļu tehniskais vai juridiskais avots. Saknes `LICENSE` un`NOTICE` paliek juridiski saistoši.

Šis modulis ir paredzēts Java 21, un izpildlaikā tiek izmantots tikai JDK. Nav vajadzības
uz Spring vai ārēju JSON/HTTP bibliotēku.
Šis modulis ir paredzēts Java 21, un izpildlaikā tiek izmantots tikai JDK. Pavasaris un ārējais
JSON/HTTP bibliotēkas nav nepieciešamas.

## 1. Priekšnoteikumi / Priekšnosacījumi

- JDK 21 vai jaunāka versija / JDK 21 vai jaunāka versija
- Maven 3.9+ avota būvēšanai / Maven 3.9+ avota būvēšanai
- Izejošā HTTPS piekļuve adresei `ec.europa.eu:443`/ izejošā HTTPS piekļuve
  Pārbaudīt/pārbaudīt:

```bash
java -version
javac -version
./mvnw -version
```

Visām trim komandām jānorāda uz to pašu JDK 21+ vidi. Ja Mavens ir savādāks
Drukājiet Java versiju, vispirms izlabojiet `JAVA_HOME` vērtību.
Visām trim komandām jāizmanto viena un tā pati JDK 21+ instalācija. Ja Mavens ziņo a
dažādas Java versijas, vispirms salabojiet `JAVA_HOME`.

## 2. Konfigurējiet JDK 21 / Konfigurējiet JDK 21

## macOS + Homebrew

```bash
brew install openjdk@21
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

Lai iegūtu pastāvīgu iestatījumu, nomainiet divas līnijas `export` uz`~/.zprofile` un `~/.zshrc` uz failu. Intel Mac datorā Homebrew prefikss parasti ir `/usr/local`.
Noturīgai iestatīšanai pievienojiet abus eksportēšanas datus `~/.zprofile` un `~/.zshrc`.
Intel Mac datoros Homebrew prefikss parasti ir `/usr/local`.

## Linux

Instalējiet sava izplatīšanas pakotni OpenJDK 21, pēc tam, piemēram:

```bash
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
```

Instalējiet sava izplatīšanas OpenJDK 21 pakotni un norādiet uz to `JAVA_HOME`.

## Windows PowerShell

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

Pastāvīgajiem iestatījumiem izmantojiet Windows vides mainīgo saskarni.
Pastāvīgai konfigurācijai izmantojiet Windows vides mainīgos.

## 3. Build and local instalācija / Build and local installation

No projekta saknes:

```bash
./mvnw clean verify
./mvnw install
```

-`verify`: tulkošana, vienību un lokālās integrācijas testi, JAR izveide.

- `install`: tas pats, pēc tam instalējiet vietējā`~/.m2/repository` repozitorijā.
- `verify`: apkopo, veic vienības/lokālās integrācijas testus un izveido JAR.
- `install`: arī instalē artefaktus vietējā Maven repozitorijā.
  Ģenerētie artefakti:

```text
target/vies-client-1.0.0.jar
target/vies-client-1.0.0-sources.jar
target/vies-client-1.0.0-javadoc.jar
```

## 4. Maven savienojums / Maven atkarība

Ja `./mvnw install`/ pēc palaišanas`./mvnw install` tika palaists pirms:

```xml
<dependency>
    <groupId>vies.client</groupId>
    <artifactId>vies-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

Modulis pašlaik ir vietējais artefakts. Organizācija publicē komandas/CI vidē
Nexus, Artifactory vai GitHub pakotnes un pēc tam izmantojiet to no turienes.
Pašlaik artefakts ir vietējs. Komandām un CI publicējiet to savā Nexus ierīcē,
Artifactory vai GitHub pakotņu krātuve.

## 5. Gradle savienojums / Gradle atkarība

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

## 6. Izmantojiet bez Maven/Gradle / Izmantojiet bez veidošanas rīka

Classpath lietojumprogramma / Classpath lietojumprogramma:

```bash
javac -cp /path/to/vies-client-1.0.0.jar MyApplication.java
java -cp "/path/to/vies-client-1.0.0.jar:." MyApplication
```

Operētājsistēmā Windows klases ceļa atdalītājs ir `;`, bet operētājsistēmā Unix/macOS `:`.
Windows izmanto `;` kā klases ceļa atdalītāju; Unix/macOS izmanto `:`.

## 7. JPMS moduļa izmantošana / JPMS moduļa izmantošana

Lietojumprogrammas failā `module-info.java`:

```java
module my.application {
    requires vies.client;
}
```

Apkopojiet un palaidiet:

```bash
javac --module-path vies-client-1.0.0.jar -d out src/module-info.java src/my/application/Main.java
java --module-path "vies-client-1.0.0.jar:out" -m my.application/my.application.Main
```

## 8. Vindsērfs / ASV kods

`.vscode/settings.json` projektā iestata Java paplašinājumu uz JDK 21.
Homebrew ceļš tajā ir atkarīgs no mašīnas: Intel Mac, Linux vai Windows
pārrakstiet to savā JDK 21 direktorijā. Pēc modifikācijas palaidiet `Developer: Reload Window` komanda.
Iekļautais `.vscode/settings.json` norāda Java paplašinājumu uz JDK 21. Tā ir
Homebrew ceļš ir atkarīgs no mašīnas; nomainiet to operētājsistēmā Intel macOS, Linux vai Windows.
Pēc JDK iestatījumu maiņas palaidiet `Developer: Reload Window`.

## 9. Tīkls un starpniekserveris / Tīkls un starpniekserveris

Nepieciešamais galapunkts:

```text
https://ec.europa.eu/taxation_customs/vies/rest-api
```

Klientam nav nepieciešama API atslēga. Uzņēmuma starpniekserveram JDK `HttpClient` starpniekservera iestatījums ir jānorāda izpildlaika vidē. Starpniekserveris pārtrauc TLS trafiku
Šajā gadījumā uzņēmuma CA sertifikāts ir jāinstalē izmantotā JDK uzticamības veikalā.
API atslēga nav nepieciešama. Korporatīvais starpniekserveris un CA uzticamība ir jākonfigurē
izpildlaika JDK/vide.

## 10. Ātrā uzstādīšanas pārbaude / uzstādīšanas dūmu pārbaude

```bash
./mvnw -q test
java -cp target/classes examples/ViesDemoServer.java
```

Citā terminālī:

```bash
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Tiešā dūmu pārbaude ir atkarīga no tīkla; CI tam nevajadzētu būt obligātam. Parastās pārbaudes
viņi izmanto vietējo viltus serveri.
Reāllaika dūmu pārbaude ir atkarīga no tīkla, un tai nevajadzētu bloķēt CI. Normāli testi
izmantojiet vietējo viltotu serveri.

## 11. Biežākās kļūdas / Traucējummeklēšana

| Kļūda/problēma                     | Risinājums / Labojums                                                                                                                                                                       |
| ---------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `switch expressions ... Java 14`   | Projekta SDK un valodas līmenim jābūt 21 / Iestatīt SDK un valodas līmeni uz 21                                                                                                             |
| `release version 21 not supported` | Maven darbojas ar veco JDK; labot`JAVA_HOME`/ Maven izmanto veco JDK; fiksēts`JAVA_HOME`                                                                                                    |
| `UnsupportedClassVersionError`     | Darbojošajam JVM jābūt 21+ / Palaist ar JVM 21+                                                                                                                                             |
| `NETWORK_ERROR`                    | Pārbaudiet DNS/starpniekservera/ugunsmūra/TLS iestatījumu / Pārbaudiet DNS, starpniekserveri, ugunsmūri un TLS                                                                              |
| `TIMEOUT`                          | Nepalieliniet taimautu bezgalīgi; izmeklēt tīklu un VIES stāvokli / Izpētīt tīkla/VIES stāvokli pirms taimauta palielināšanas                                                               |
| `CLIENT_OVERLOADED`                | Aizkavēts atkārtots mēģinājums, ierobežota iekļūšana, darbinieki plus globālais ierobežotājs / Aizkavēts atkārtots mēģinājums, ierobežota iekļūšana, darbinieki plus globālais ierobežotājs |
| `CACHE_ERROR`                      | Pārbaudiet Redis taimautu/veselību/metriku; neapejiet to ar masveida tiešo VIES zvanu / Pārbaudiet Redisa veselību; neapiet to ar VIES stampju                                              |
