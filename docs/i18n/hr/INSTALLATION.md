# Hrvatski (hr) — Instalacija

> [Svi jezici](../../LANGUAGES.md) · Informativni prijevod. U slučaju razlike mjerodavan je kanonski engleski tehnički ili pravni izvor. Samo su korijenski `LICENSE` i `NOTICE` pravno mjerodavni; prijevod ih ne zamjenjuje.

Ovaj modul cilja na Javu 21 i koristi samo JDK tijekom izvođenja. Nema potrebe
na Spring ili vanjsku JSON/HTTP biblioteku.

Ovaj modul cilja na Javu 21 i koristi samo JDK tijekom izvođenja. Proljetni i vanjski
JSON/HTTP biblioteke nisu potrebne.

## 1. Preduvjeti

- JDK 21 ili noviji / JDK 21 ili noviji
- Maven 3.9+ za izvorne verzije / Maven 3.9+ za izvorne verzije
- Odlazni HTTPS pristup na adresu `ec.europa.eu:443`/ odlazni HTTPS pristup

Provjerite / potvrdite:

```bash
java -version
javac -version
./mvnw -version
```

Sve tri naredbe trebaju upućivati ​​na isto okruženje JDK 21+. Ako je Maven drugačiji
Ispiši Java verziju, prvo ispravi vrijednost `JAVA_HOME`.

Sve tri naredbe trebaju koristiti istu instalaciju JDK 21+. Ako Maven prijavi a
drugu verziju Jave, prvo popravite `JAVA_HOME`.

## 2. Konfigurirajte JDK 21

### macOS + Homebrew

```bash
brew install openjdk@21
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

Za trajnu postavku promijenite dvije linije `export` u `~/.zprofile` i `~/.zshrc`
u datoteku. Na Intel Macu, Homebrew prefiks je obično `/usr/local`.

Za trajnu postavku dodajte oba izvoza u `~/.zprofile` i `~/.zshrc`.
Homebrew prefiks obično je `/usr/local` na Intel Mac računalima.

### Linux

Instalirajte paket OpenJDK 21 vaše distribucije, a zatim, na primjer:

```bash
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
```

Instalirajte OpenJDK 21 paket svoje distribucije i usmjerite `JAVA_HOME` na njega.

### Windows PowerShell

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

Za trajne postavke koristite sučelje Windows Environment Variables.
Koristite varijable okruženja Windows za trajnu konfiguraciju.

## 3. Izrada i lokalna instalacija

Iz korijena projekta:

```bash
./mvnw clean verify
./mvnw install
```

- `verify`: prijevod, test jedinice i lokalne integracije, stvaranje JAR-ova.

- `install`: isto, zatim instalirajte u lokalno spremište`~/.m2/repository`.
- `verify`: kompilira, pokreće jedinične/lokalne testove integracije i stvara JAR-ove.
- `install`: također instalira artefakte u lokalno Mavenovo spremište.

Generirani artefakti:

```text
target/vies-client-1.0.0.jar
target/vies-client-1.0.0-sources.jar
target/vies-client-1.0.0-javadoc.jar
```

## 4. Maven veza

Ako je `./mvnw install`/ Nakon pokretanja `./mvnw install` pokrenut prije:

```xml
<dependency>
    <groupId>vies.client</groupId>
    <artifactId>vies-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

Modul je trenutno lokalni artefakt. Organizacija objavljuje u timskom/CI okruženju
Nexus, Artifactory ili GitHub pakete, a zatim ga upotrijebite od tamo.

Artefakt je trenutno lokalan. Za timove i CI, objavite to na svom Nexusu,
Artifactory ili GitHub Packages repozitorij.

## 5. Gradle veza

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

## 6. Uporaba bez Mavena ili Gradlea

Aplikacija Classpath / Aplikacija Classpath:

```bash
javac -cp /path/to/vies-client-1.0.0.jar MyApplication.java
java -cp "/path/to/vies-client-1.0.0.jar:." MyApplication
```

U sustavu Windows, separator staze klase je `;`, u sustavu Unix/macOS je `:`.
Windows koristi `;` kao separator classpath; Unix/macOS koristi `:`.

## 7. Korištenje JPMS modula

U datoteci `module-info.java` aplikacije:

```java
module my.application {
    requires vies.client;
}
```

Prevedi i pokreni:

```bash
javac --module-path vies-client-1.0.0.jar -d out src/module-info.java src/my/application/Main.java
java --module-path "vies-client-1.0.0.jar:out" -m my.application/my.application.Main
```

## 8. Windsurf i VS Code

`.vscode/settings.json` u projektu postavlja Java proširenje na JDK 21.
Homebrew staza u njemu specifična je za stroj: na Intel Macu, Linuxu ili Windowsu
prepišite ga u svoj JDK 21 direktorij. Nakon izmjene, pokrenite
`Developer: Reload Window` naredba.

Uključeni `.vscode/settings.json` upućuje Java ekstenziju na JDK 21. Njegova
Homebrew put je specifičan za stroj; zamijenite ga na Intel macOS, Linux ili Windows.
Pokrenite `Developer: Reload Window` nakon promjene JDK postavki.

## 9. Mreža i proxy

Potrebna krajnja točka:

```text
https://ec.europa.eu/taxation_customs/vies/rest-api
```

Klijent ne zahtijeva API ključ. Za korporativni proxy, JDK `HttpClient`
postavka proxyja mora biti navedena u okruženju za izvršavanje. Proxy prekida TLS promet
U ovom slučaju, CA certifikat tvrtke mora biti instaliran u truststoreu JDK-a koji koristite.

API ključ nije potreban. Korporativni proxy i CA trust moraju biti konfigurirani u
runtime JDK/okruženje.

## 10. Brza provjera instalacije

```bash
./mvnw -q test
java -cp target/classes examples/ViesDemoServer.java
```

U drugom terminalu:

```bash
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Dimni test uživo ovisi o mreži; Ne bi trebalo biti obavezno u CI. Normalni testovi
koriste lokalni lažni poslužitelj.

Provjera dima uživo ovisi o mreži i ne bi trebala zatvarati CI. Normalni testovi
koristite lokalni lažni poslužitelj.

## 11. Uobičajene pogreške

| Greška / Problem                   | Rješenje / Popravak                                                                                                                                             |
| ---------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `switch expressions ... Java 14`   | Project SDK i razina jezika trebaju biti 21 / Postavite SDK i razinu jezika na 21                                                                               |
| `release version 21 not supported` | Maven radi sa starim JDK-om; popravak`JAVA_HOME`/ Maven koristi stari JDK; fiksni`JAVA_HOME`                                                                    |
| `UnsupportedClassVersionError`     | Pokrenuti JVM trebao bi biti 21+ / Run with JVM 21+                                                                                                             |
| `NETWORK_ERROR`                    | Provjerite postavke DNS/proxy/vatrozida/TLS / Provjerite DNS, proxy, vatrozid i TLS                                                                             |
| `TIMEOUT`                          | Ne produžujte timeout na neodređeno vrijeme; istražiti stanje mreže i VIES-a / Istražite stanje mreže/VIES-a prije podizanja vremenskih ograničenja             |
| `CLIENT_OVERLOADED`                | Odgođeni ponovni pokušaj, ograničeni ulaz, radnici plus globalni limiter / Odgođeni ponovni pokušaj, ograničeni ulaz, radnici plus globalni limiter             |
| `CACHE_ERROR`                      | Provjerite Redis timeout/zdravlje/metriku; nemojte ga zaobići masovnim izravnim VIES pozivom / Provjerite Redis zdravlje; nemojte ga zaobilaziti VIES stampedom |
