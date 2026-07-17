# Slovenčina (sk) — Inštalácia

**Jazyky:** [English](../../INSTALLATION.md) · [Čeština](../cs/INSTALLATION.md) · [Polski](../pl/INSTALLATION.md) · [Slovenčina](INSTALLATION.md) · [Slovenščina](../sl/INSTALLATION.md) · [Hrvatski](../hr/INSTALLATION.md) · [Română](../ro/INSTALLATION.md) · [Български](../bg/INSTALLATION.md) · [Ελληνικά](../el/INSTALLATION.md)

> Informatívny preklad; platí anglický technický/právny originál. [LICENSE](../../../LICENSE) sa neprekladá ako záväzná licencia.

Požiadavky: JDK 21+, Maven 3.9+ a odchádzajúce HTTPS na `ec.europa.eu:443`; API kľúč netreba.

```bash
java -version; javac -version; ./mvnw -version
```

Všetko musí používať rovnaký JDK 21+, inak opravte `JAVA_HOME`.

```bash
# macOS
brew install openjdk@21
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
# Linux
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
```

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

Trvalé nastavenie dajte do shell profilu/Windows Environment Variables. Intel Mac má obvykle prefix `/usr/local`.

## Build a použitie

```bash
./mvnw clean verify
./mvnw install
```

Vzniknú binary, sources a Javadoc JAR v `target/`; `install` ich uloží do lokálneho Maven repo.

```xml
<dependency><groupId>vies.client</groupId><artifactId>vies-client</artifactId><version>1.0.0</version></dependency>
```

```kotlin
repositories { mavenLocal() }
dependencies { implementation("vies.client:vies-client:1.0.0") }
java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }
```

Bez build nástroja použite `javac -cp ...`/`java -cp ...`; classpath separator je Windows `;`, Unix `:`. Pre JPMS pridajte `requires vies.client;` a `--module-path`.

`.vscode/settings.json` nastavte na vlastnú cestu JDK a reloadnite okno. Proxy a CA nakonfigurujte v JDK/truststore; endpoint je `https://ec.europa.eu/taxation_customs/vies/rest-api`.

```bash
./mvnw -q test
java -cp target/classes examples/ViesDemoServer.java
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Live smoke nesmie blokovať CI. Typické chyby: starý JDK (`release version 21`, `UnsupportedClassVersionError`), DNS/proxy/TLS (`NETWORK_ERROR`), sieť/VIES (`TIMEOUT`), príliš veľký ingress (`CLIENT_OVERLOADED`) alebo Redis (`CACHE_ERROR`).

## Úplné pripojenie artefaktu a riešenie problémov

### Maven

```xml
<dependency>
  <groupId>vies.client</groupId>
  <artifactId>vies-client</artifactId>
  <version>1.0.0</version>
</dependency>
```

### Без build tool / Fără instrument de build

```bash
javac -cp /path/to/vies-client-1.0.0.jar MyApplication.java
java -cp "/path/to/vies-client-1.0.0.jar:." MyApplication
```

### JPMS

```java
module my.application {
    requires vies.client;
}
```

```bash
javac --module-path vies-client-1.0.0.jar -d out src/module-info.java src/my/application/Main.java
java --module-path "vies-client-1.0.0.jar:out" -m my.application/my.application.Main
```

| Problém | Riešenie |
|---|---|
| `release version 21 not supported` | Maven používa starý JDK; opravte JAVA_HOME |
| `UnsupportedClassVersionError` | Spustite na JVM 21+ |
| `NETWORK_ERROR` / `TIMEOUT` | Skontrolujte DNS, proxy, firewall a TLS |
| `CLIENT_OVERLOADED` | Odložený retry, bounded ingress, workers a global limiter |
| `CACHE_ERROR` | Skontrolujte Redis; neobchádzajte ho VIES stampede |

Artifacts for a team/CI should be published to Nexus, Artifactory, GitHub Packages or Maven Central; `./mvnw install` alone only populates the local `~/.m2/repository`.

## Kompletná prevádzková príručka

### Životný cyklus klienta a model rozhodnutí

V aplikácii alebo worker procese vytvorte presne jednu inštanciu `ViesClient`. Zdieľaný klient opakovane využíva spojenia JDK `HttpClient`, lokálnu TTL cache, tabuľku single-flight a všetky admission limity. Klient vytváraný pre každý HTTP request tieto ochrany ruší, zvyšuje počet spojení a spotrebu pamäte. Inštancia je thread-safe. Pri riadenom ukončení vždy volajte `close()`, použite `try`-with-resources alebo Spring `@Bean(destroyMethod = "close")`. Po zatvorení nové sync aj async volanie synchronne vyhodí `IllegalStateException`; spoločné rozpracované operácie sa ukončia štruktúrovaným `CLIENT_CLOSED`.

Sealed hierarchia oddeľuje obchodné rozhodnutie od technickej neistoty. `Valid` znamená potvrdenie platnosti systémom VIES, `Invalid` jej nepotvrdenie, `MalformedInput` neopraviteľný vstup pre request a `Unavailable` stav bez rozhodnutia. `Unavailable` sa nikdy nesmie zmeniť na `Invalid`. V programovej logike, metrikách a retry používajte stabilné `error.code()` a `error.retryable()`, nie lokalizovaný text. Používateľovi možno vrátiť `messageHu` a `messageEn`.

### Normalizácia, requester a auditný dôkaz

`VatFormat` odstráni povolené medzery, bodky a pomlčky, písmená zmení na veľké a ešte pred sieťou overí tvar krajiny. Je to iba kontrola formátu, nie potvrdenie platnosti. Grécky prefix `GR` sa kanonizuje na VIES `EL`; Severné Írsko používa `XI`. `defaultRequester` má byť vlastné IČ DPH organizácie. VIES môže pri platnom výsledku vrátiť `consultationNumber`. Pri cache hit však `consultationNumber` a `requestDate` patria pôvodnej kontrole. Ak proces vyžaduje čerstvý dôkaz, vyhodnoťte `fromCache`, skráťte TTL alebo cache vypnite.

### Admission, async prevádzka a single-flight

`maxPendingSyncRequests` obmedzuje súčasných sync volajúcich, `maxPendingAsyncRequests` jedinečných async leaderov a `maxConcurrentRequests` skutočné odchádzajúce HTTP requesty. Prekročenie pending limitu vracia `CLIENT_OVERLOADED`; čakanie na sieťový slot rešpektuje `admissionTimeout`. Predvolený async executor vytvára virtuálne vlákno pre prijatú úlohu. Externe dodaný executor klient nezatvára, pretože jeho životný cyklus patrí aplikácii. Zrušenie future jedného followera nezruší spoločnú operáciu.

Single-flight spája v jednom JVM súčasné requesty s rovnakou kombináciou target IČ DPH a requester IČ DPH. Stovky či tisíce followerov tak zdieľajú jeden upstream request a nespotrebujú samostatné pending sloty. Mechanizmus nie je distribuovaný. Každý pod má vlastnú tabuľku, semaphore, connection pool a pamäťovú cache; súčet per-pod limitov preto nie je globálny limit VIES.

### Cache, retry a ochrana pred stampede

Vstavaná cache je concurrent, časovo aj veľkostne ohraničená. Ukladá iba autoritatívny `Valid`; `Invalid`, chybný vstup a technická chyba sa neukladajú. Externý `ViesCache`, napríklad Redis adaptér, musí byť thread-safe, mať krátky connection/command timeout, versioned namespace a úplnú serializáciu dátumu, optionals a príznaku cache. Výnimka pri čítaní vráti `CACHE_ERROR` bez VIES fallbacku, aby výpadok Redis nespustil stampede. Chyba zápisu po úspechu nesmie zmazať autoritatívny `Valid`.

Lokálny retry je zámerne malý, najviac päť pokusov, s exponential backoff a jitter. Opakujú sa iba prechodné network/VIES chyby. `CLIENT_OVERLOADED`, `CLIENT_CLOSED`, blokovanie a input error sa lokálne neopakujú. Vo veľkom systéme je primárnym mechanizmom durable delayed queue s idempotency key, maximálnym počtom pokusov a DLQ. Okamžité retry vo všetkých workeroch môže výpadok upstreamu zhoršiť.

### Strict mapping, bezpečnosť a observability

Odpoveď VIES je nedôveryhodný externý JSON. Autoritatívne rozhodnutie vyžaduje root object, skutočný boolean `isValid` alebo `valid`, platný ISO-8601 `requestDate` a žiadny nadradený `userError`. Chýbajúci alebo textový boolean a chybný timestamp vedú k `MALFORMED_RESPONSE`; klient nevymýšľa lokálny čas ani `Invalid`. Produkcia používa oficiálny HTTPS endpoint. Testovací `baseUrl` nesmie pochádzať z používateľského vstupu. V logoch maskujte IČ DPH, názov, adresu a requester.

Merajte počet výsledkov podľa typu a `errorCode`, celkovú aj upstream p50/p95/p99 latency, cache hit ratio, `CACHE_ERROR`, `CLIENT_OVERLOADED`, retry outcomes, hĺbku a vek queue, delayed retry a DLQ, chybovosť podľa krajiny, heap, GC, virtual threads, CPU a sockety. Liveness nesmie závisieť od VIES; `availability()` používajte ako zriedkavú cacheovanú diagnostiku.

### Architektúra miliónových dávok a overenie

Produkčný tok: API ingress → durable partitioned queue → bounded consumer window → worker JVMs → shared Redis → distributed rate limiter → EÚ a členské VIES. Autoscaling riaďte vekom queue, nie neobmedzenou VIES concurrency. Virtuálne vlákna znižujú cenu čakania, ale nezvyšujú upstream kapacitu. Lokálny benchmark nie je SLA ani odporúčaný európsky limit.

Pred nasadením spustite `./mvnw --batch-mode clean verify`. Unit testy pokrývajú formát, requester, mapping, errors, availability, JSON, TTL cache a builder. Lokálna HTTP/concurrency suite deterministicky overuje retry, limity, cancellation, close races, custom executor, oba smery sync/async single-flight, cache anti-stampede a fatal `Error`. Live VIES nesmie byť povinnou CI bránou ani cieľom load testu; load patrí na mock alebo vlastný staging. Soak/chaos má bežať 30–60 minút s bounded heap a JFR a zdroje musia dosiahnuť stabilné plateau.
