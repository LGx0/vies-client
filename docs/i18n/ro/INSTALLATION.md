# Română (ro) — Instalare

> [Toate limbile](../../LANGUAGES.md) · Traducere informativă. În caz de diferență prevalează sursa canonică tehnică sau juridică în limba engleză. Numai `LICENSE` și `NOTICE` din rădăcină sunt texte juridice oficiale; traducerea nu le înlocuiește.

Acest modul vizează Java 21 și utilizează numai JDK-ul în timpul execuției. Nu este nevoie
la Spring sau la o bibliotecă JSON/HTTP externă.

Acest modul vizează Java 21 și utilizează numai JDK-ul în timpul execuției. Primavara si exterior
Bibliotecile JSON/HTTP nu sunt necesare.

## 1. Cerințe preliminare

- JDK 21 sau mai nou / JDK 21 sau mai nou
- Maven 3.9+ pentru versiuni sursă / Maven 3.9+ pentru versiuni sursă
- Acces HTTPS de ieșire la adresa `ec.europa.eu:443`/ acces HTTPS de ieșire

Verificați / Verificați:

```bash
java -version
javac -version
./mvnw -version
```

Toate cele trei comenzi ar trebui să indice același mediu JDK 21+. Dacă Maven este diferit
Imprimați versiunea Java, mai întâi corectați valoarea `JAVA_HOME`.

Toate cele trei comenzi ar trebui să utilizeze aceeași instalare JDK 21+. Dacă Maven raportează a
versiune Java diferită, remediați mai întâi `JAVA_HOME`.

## 2. Configurați JDK 21

### macOS + Homebrew

```bash
brew install openjdk@21
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

Pentru o setare permanentă, schimbați cele două linii `export` în `~/.zprofile` și `~/.zshrc`
la un dosar. Pe un Mac Intel, prefixul Homebrew este de obicei `/usr/local`.

Pentru o configurare persistentă, adăugați ambele exporturi în `~/.zprofile` și `~/.zshrc`.
Prefixul Homebrew este de obicei `/usr/local` pe Mac-urile Intel.

### Linux

Instalați pachetul OpenJDK 21 al distribuției dvs., apoi, de exemplu:

```bash
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
```

Instalați pachetul OpenJDK 21 al distribuției dvs. și indicați-l pe `JAVA_HOME`.

### Windows PowerShell

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

Pentru setări permanente, utilizați interfața Windows Environment Variables.
Utilizați variabilele de mediu Windows pentru o configurație persistentă.

## 3. Construire și instalare locală

Din rădăcina proiectului:

```bash
./mvnw clean verify
./mvnw install
```

- `verify`: traducere, teste de integrare unitară și locală, crearea de JAR.

- `install`: același, apoi instalați în depozitul local`~/.m2/repository`.
- `verify`: compilează, rulează teste de integrare unitară/locală și creează JAR-urile.
- `install`: instalează și artefactele în depozitul local Maven.

Artefacte generate:

```text
target/vies-client-1.2.0.jar
target/vies-client-1.2.0-sources.jar
target/vies-client-1.2.0-javadoc.jar
```

## 4. Conexiune Maven/dependență Maven

Dacă `./mvnw install`/ După rulare `./mvnw install`a fost rulat înainte:

```xml
<dependency>
    <groupId>vies.client</groupId>
    <artifactId>vies-client</artifactId>
    <version>1.2.0</version>
</dependency>
```

Modulul este în prezent un artefact local. Organizația publică într-un mediu de echipă/CI
Pachete Nexus, Artifactory sau GitHub și apoi folosiți-l de acolo.

Artefactul este în prezent local. Pentru echipe și CI, publicați-l pe Nexus,
Artifactory sau depozitul de pachete GitHub.

## 5. Conexiune Gradle

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

## 6. Utilizare fără Maven sau Gradle

Aplicație Classpath / Aplicație Classpath:

```bash
javac -cp /path/to/vies-client-1.2.0.jar MyApplication.java
java -cp "/path/to/vies-client-1.2.0.jar:." MyApplication
```

Sub Windows, separatorul classpath este `;`, sub Unix/macOS este `:`.
Windows folosește `;` ca separator de classpath; Unix/macOS utilizează `:`.

## 7. Utilizarea modulului JPMS

În fișierul `module-info.java` al aplicației:

```java
module my.application {
    requires vies.client;
}
```

Compilați și rulați:

```bash
javac --module-path vies-client-1.2.0.jar -d out src/module-info.java src/my/application/Main.java
java --module-path "vies-client-1.2.0.jar:out" -m my.application/my.application.Main
```

## 8. Windsurf și VS Code

`.vscode/settings.json` în proiect setează extensia Java la JDK 21.
Calea Homebrew din ea este specifică mașinii: pe Intel Mac, Linux sau Windows
rescrie-l în propriul tău director JDK 21. După modificare, rulați
Comanda `Developer: Reload Window`.

`.vscode/settings.json` inclus indică extensia Java către JDK 21. Este
Calea homebrew este specifică mașinii; înlocuiți-l pe Intel macOS, Linux sau Windows.
Rulați `Developer: Reload Window` după modificarea setărilor JDK.

## 9. Rețea și proxy

Punct final necesar:

```text
https://ec.europa.eu/taxation_customs/vies/rest-api
```

Clientul nu necesită o cheie API. Pentru proxy corporativ, JDK `HttpClient`
setarea proxy trebuie specificată în mediul de rulare. Proxy care întrerupe traficul TLS
În acest caz, certificatul CA al companiei trebuie să fie instalat în depozitul de încredere al JDK-ului pe care îl utilizați.

Nu este necesară nicio cheie API. Proxy-ul corporativ și încrederea CA trebuie configurate în
runtime JDK/mediu.

## 10. Verificare rapidă a instalării

```bash
./mvnw -q test
java -cp target/classes examples/ViesDemoServer.java
```

Într-un alt terminal:

```bash
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Testul de fum viu depinde de rețea; Nu ar trebui să fie obligatoriu în CI. Testele normale
folosesc un server simulat local.

Verificarea fumului în timp real depinde de rețea și nu ar trebui să blocheze CI. Teste normale
utilizați un server simulat local.

## 11. Erori frecvente

| Eroare / Problemă                  | Soluție / Remediere                                                                                                                                   |
| ---------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| `switch expressions ... Java 14`   | Project SDK și nivelul de limbă ar trebui să fie 21 / Setați SDK și nivelul de limbă la 21                                                            |
| `release version 21 not supported` | Maven rulează cu vechiul JDK; reparați`JAVA_HOME`/ Maven folosește un vechi JDK; fix`JAVA_HOME`                                                       |
| `UnsupportedClassVersionError`     | JVM-ul care rulează ar trebui să fie 21+ / Run with JVM 21+                                                                                           |
| `NETWORK_ERROR`                    | Verificați setarea DNS/proxy/firewall/TLS / Verificați DNS, proxy, firewall și TLS                                                                    |
| `TIMEOUT`                          | Nu măriți timeout-ul la nesfârșit; investigați rețeaua și sănătatea VIES / Investigați starea rețelei/VIES înainte de a ridica timeouts               |
| `CLIENT_OVERLOADED`                | Reîncercare întârziată, intrare limitată, lucrători plus limitator global / Reîncercare întârziată, intrare limitată, lucrători plus limitator global |
| `CACHE_ERROR`                      | Verificați Redis timeout/sănătate/metrics; nu o ocoliți cu un apel VIES direct în masă / Verificați sănătatea Redis; nu-l ocoli cu o stampede VIES    |
