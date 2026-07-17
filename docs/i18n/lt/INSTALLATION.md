# Lietuvių (lt) — Installation

> [Kalbų pasirinkimas](../../LANGUAGES.md) · Ši lokalizacija skirta prieinamumui. Esant neatitikimui, pirmenybę turi kanoninis angliškas techninis ar teisinis šaltinis. Šakniniai `LICENSE` ir`NOTICE` lieka teisiškai privalomi.

Šis modulis skirtas Java 21 ir vykdymo metu naudoja tik JDK. Nėra reikalo
į Spring arba išorinę JSON / HTTP biblioteką.
Šis modulis skirtas Java 21 ir vykdymo metu naudoja tik JDK. Pavasaris ir išorinis
JSON/HTTP bibliotekos nebūtinos.

## 1. Būtinos sąlygos / Būtinos sąlygos

- JDK 21 arba naujesnė versija / JDK 21 ar naujesnė versija
- Maven 3.9+ šaltinio kūrimui / Maven 3.9+ šaltinio kūrimui
- Išeinanti HTTPS prieiga prie adreso `ec.europa.eu:443`/ išeinanti HTTPS prieiga
  Patikrinkite / patikrinkite:

```bash
java -version
javac -version
./mvnw -version
```

Visos trys komandos turi nukreipti į tą pačią JDK 21+ aplinką. Jei Mavenas kitoks
Išspausdinkite „Java“ versiją, pirmiausia pataisykite `JAVA_HOME` reikšmę.
Visos trys komandos turi naudoti tą patį JDK 21+ diegimą. Jei Mavenas praneša a
skirtingą Java versiją, pirmiausia pataisykite `JAVA_HOME`.

## 2. Konfigūruokite JDK 21 / Konfigūruokite JDK 21

## macOS + Homebrew

```bash
brew install openjdk@21
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

Norėdami nustatyti nuolatinį nustatymą, pakeiskite dvi eilutes `export` į`~/.zprofile` ir `~/.zshrc` į failą. „Intel Mac“ sistemoje „Homebrew“ priešdėlis paprastai yra `/usr/local`.
Kad sąranka būtų nuolatinė, pridėkite abu eksportuotus duomenis į `~/.zprofile` ir `~/.zshrc`.
„Intel Mac“ kompiuteriuose „Homebrew“ priešdėlis paprastai yra `/usr/local`.

## Linux

Įdiekite savo platinimo OpenJDK 21 paketą, tada, pavyzdžiui:

```bash
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
```

Įdiekite platinimo OpenJDK 21 paketą ir nukreipkite į jį `JAVA_HOME`.

## Windows PowerShell

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

Norėdami nustatyti nuolatinius nustatymus, naudokite „Windows Environment Variables“ sąsają.
Norėdami nuolat konfigūruoti, naudokite Windows aplinkos kintamuosius.

## 3. Sukurti ir vietinis diegimas / Sukurti ir vietinis diegimas

Iš projekto šaknies:

```bash
./mvnw clean verify
./mvnw install
```

-`verify`: vertimas, vienetų ir vietinės integracijos testai, JAR kūrimas.

- `install`: tas pats, tada įdiekite į vietinę`~/.m2/repository` saugyklą.
- `verify`: kompiliuoja, vykdo vieneto / vietinės integracijos testus ir sukuria JAR.
- `install`: taip pat įdiegia artefaktus į vietinę Maven saugyklą.
  Sugeneruoti artefaktai:

```text
target/vies-client-1.2.0.jar
target/vies-client-1.2.0-sources.jar
target/vies-client-1.2.0-javadoc.jar
```

## 4. Maven ryšys / Maven priklausomybė

Jei `./mvnw install`/ Po paleidimo`./mvnw install` buvo paleistas anksčiau:

```xml
<dependency>
    <groupId>vies.client</groupId>
    <artifactId>vies-client</artifactId>
    <version>1.2.0</version>
</dependency>
```

Šiuo metu modulis yra vietinis artefaktas. Organizacija skelbia komandos/CI aplinkoje
„Nexus“, „Artifactory“ arba „GitHub“ paketus, tada naudokite juos iš ten.
Šiuo metu artefaktas yra vietinis. Komandoms ir CI paskelbkite ją „Nexus“,
„Artifactory“ arba „GitHub Packages“ saugykla.

## 5. Gradle ryšys / Gradle priklausomybė

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

## 6. Naudokite be Maven/Gradle / Naudokite be kūrimo įrankio

„Classpath“ programa / „Classpath“ programa:

```bash
javac -cp /path/to/vies-client-1.2.0.jar MyApplication.java
java -cp "/path/to/vies-client-1.2.0.jar:." MyApplication
```

Sistemoje „Windows“ klasės kelio skyriklis yra `;`, „Unix“ / „macOS“`:`.
„Windows“ naudoja `;` kaip klasės kelio skyriklį; „Unix“ / „macOS“ naudoja `:`.

## 7. JPMS modulio naudojimas / JPMS modulio naudojimas

Programos faile `module-info.java`:

```java
module my.application {
    requires vies.client;
}
```

Sukompiliuoti ir paleisti:

```bash
javac --module-path vies-client-1.2.0.jar -d out src/module-info.java src/my/application/Main.java
java --module-path "vies-client-1.2.0.jar:out" -m my.application/my.application.Main
```

## 8. Burlentės / JAV kodas

`.vscode/settings.json` projekte nustato „Java“ plėtinį į JDK 21.
Jame esantis „Homebrew“ kelias priklauso nuo įrenginio: „Intel Mac“, „Linux“ arba „Windows“.
perrašykite jį į savo JDK 21 katalogą. Po pakeitimo paleiskite `Developer: Reload Window` komanda.
Įtrauktas `.vscode/settings.json` nukreipia Java plėtinį į JDK 21. Jo
Homebrew kelias priklauso nuo mašinos; pakeiskite jį „Intel macOS“, „Linux“ arba „Windows“.
Pakeitę JDK nustatymus, paleiskite `Developer: Reload Window`.

## 9. Tinklas ir tarpinis serveris / Tinklas ir tarpinis serveris

Būtinas galutinis taškas:

```text
https://ec.europa.eu/taxation_customs/vies/rest-api
```

Klientui API rakto nereikia. Įmonės įgaliotajam serveriui JDK `HttpClient` tarpinio serverio nustatymas turi būti nurodytas vykdymo aplinkoje. Tarpinis serveris nutraukia TLS srautą
Tokiu atveju įmonės CA sertifikatas turi būti įdiegtas naudojamo JDK patikimumo saugykloje.
API rakto nereikia. Įmonės įgaliotasis serveris ir CA pasitikėjimas turi būti sukonfigūruotas
vykdymo JDK/aplinka.

## 10. Greitas montavimo patikrinimas / Montavimo dūmų patikrinimas

```bash
./mvnw -q test
java -cp target/classes examples/ViesDemoServer.java
```

Kitame terminale:

```bash
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Tiesioginis dūmų testas priklauso nuo tinklo; Tai neturėtų būti privaloma CI. Įprasti testai
jie naudoja vietinį netikrą serverį.
Tiesioginis dūmų patikrinimas priklauso nuo tinklo ir neturėtų užblokuoti CI. Įprasti testai
naudoti vietinį netikrą serverį.

## 11. Dažnos klaidos / Trikčių šalinimas

| Klaida / problema                  | Sprendimas / Pataisymas                                                                                                                                                     |
| ---------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `switch expressions ... Java 14`   | Projekto SDK ir kalbos lygis turi būti 21 / Nustatykite SDK ir kalbos lygį į 21                                                                                             |
| `release version 21 not supported` | Maven veikia su senu JDK; pataisyti`JAVA_HOME`/ Maven naudoja seną JDK; fiksuotas`JAVA_HOME`                                                                                |
| `UnsupportedClassVersionError`     | Veikiantis JVM turėtų būti 21+ / Vykdyti su JVM 21+                                                                                                                         |
| `NETWORK_ERROR`                    | Patikrinkite DNS / tarpinio serverio / ugniasienės / TLS nustatymą / Patikrinkite DNS, tarpinį serverį, ugniasienę ir TLS                                                   |
| `TIMEOUT`                          | Nedidinkite skirtojo laiko neribotą laiką; ištirti tinklą ir VIES būklę / Ištirti tinklo / VIES būklę prieš padidinant skirtąjį laiką                                       |
| `CLIENT_OVERLOADED`                | Atidėtas pakartotinis bandymas, ribotas įėjimas, darbuotojai ir visuotinis ribotuvas / Atidėtas pakartotinis bandymas, ribotas įėjimas, darbuotojai ir visuotinis ribotuvas |
| `CACHE_ERROR`                      | Patikrinkite Redis skirtąjį laiką / sveikatą / metriką; neaplenk jo masiniu tiesioginiu VIES skambučiu / Patikrinkite Redis sveikatą; neaplenk jo su VIES stampu            |
