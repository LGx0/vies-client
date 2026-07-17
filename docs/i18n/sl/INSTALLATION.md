# Slovenščina (sl) — Namestitev

**Jeziki:** [English](../../INSTALLATION.md) · [Slovenščina](INSTALLATION.md) · [Drugi prevodi](../cs/INSTALLATION.md)

> Informativni prevod; velja angleški izvirnik. [LICENSE](../../../LICENSE) se ne prevaja kot zavezujoča licenca.

Zahteve: JDK 21+, Maven 3.9+, izhodni HTTPS do `ec.europa.eu:443`; API key ni potreben. `java -version`, `javac -version`, `./mvnw -version` morajo pokazati isti JDK; sicer popravite `JAVA_HOME`.

```bash
brew install openjdk@21
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
# Linux: JAVA_HOME=/usr/lib/jvm/java-21-openjdk
```

Windows: `$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"`. Trajno nastavite v profilu/Environment Variables.

```bash
./mvnw clean verify
./mvnw install
```

Ustvari binary/sources/Javadoc JAR v `target/` in lokalni Maven artefakt.

```kotlin
repositories { mavenLocal() }
dependencies { implementation("vies.client:vies-client:1.2.0") }
java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }
```

Brez build tool: `javac -cp ...` in `java -cp ...`; separator je Windows `;`, Unix `:`. JPMS uporablja `requires vies.client;` in `--module-path`.

V `.vscode/settings.json` nastavite lokalni JDK. Proxy/CA konfigurirajte v JDK truststore. Endpoint: `https://ec.europa.eu/taxation_customs/vies/rest-api`.

```bash
./mvnw -q test
java -cp target/classes examples/ViesDemoServer.java
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Live smoke ne sme blokirati CI. Pri napakah preverite JDK (`release 21`), DNS/proxy/TLS (`NETWORK_ERROR`), VIES (`TIMEOUT`), ingress (`CLIENT_OVERLOADED`) ali Redis (`CACHE_ERROR`).

## Popolna vključitev artefakta in odpravljanje težav

### Maven

```xml
<dependency>
  <groupId>vies.client</groupId>
  <artifactId>vies-client</artifactId>
  <version>1.2.0</version>
</dependency>
```

### Без build tool / Fără instrument de build

```bash
javac -cp /path/to/vies-client-1.2.0.jar MyApplication.java
java -cp "/path/to/vies-client-1.2.0.jar:." MyApplication
```

### JPMS

```java
module my.application {
    requires vies.client;
}
```

```bash
javac --module-path vies-client-1.2.0.jar -d out src/module-info.java src/my/application/Main.java
java --module-path "vies-client-1.2.0.jar:out" -m my.application/my.application.Main
```

| Težava | Rešitev |
|---|---|
| `release version 21 not supported` | Maven uporablja star JDK; popravite JAVA_HOME |
| `UnsupportedClassVersionError` | Zaženite na JVM 21+ |
| `NETWORK_ERROR` / `TIMEOUT` | Preverite DNS, proxy, firewall in TLS |
| `CLIENT_OVERLOADED` | Delayed retry, bounded ingress, workers in global limiter |
| `CACHE_ERROR` | Preverite Redis; ne obidite ga z VIES stampede |

Artifacts for a team/CI should be published to Nexus, Artifactory, GitHub Packages or Maven Central; `./mvnw install` alone only populates the local `~/.m2/repository`.

## Celovit operativni priročnik

### Življenjski cikel odjemalca in model odločitev

V aplikaciji ali worker procesu ustvarite natanko eno instanco `ViesClient`. Skupni odjemalec ponovno uporablja povezave JDK `HttpClient`, lokalni TTL cache, tabelo single-flight in vse admission omejitve. Ustvarjanje odjemalca za vsak HTTP request te zaščite izniči ter poveča število povezav in porabo pomnilnika. Instanca je thread-safe. Ob nadzorovani zaustavitvi vedno pokličite `close()`, uporabite `try`-with-resources ali Spring `@Bean(destroyMethod = "close")`. Po zaprtju novi sync in async klici sinhrono vržejo `IllegalStateException`, skupne operacije v teku pa se končajo s strukturiranim `CLIENT_CLOSED`.

Sealed hierarhija loči poslovno odločitev od tehnične negotovosti. `Valid` pomeni, da je VIES veljavnost potrdil, `Invalid` da je ni potrdil, `MalformedInput` da vnosa ni mogoče poslati, `Unavailable` pa da odločitev ni bila sprejeta. `Unavailable` se nikoli ne sme pretvoriti v `Invalid`. Za programsko logiko, metrike in retry uporabljajte stabilna `error.code()` in `error.retryable()`, ne lokaliziranega besedila. Uporabniku lahko vrnete `messageHu` in `messageEn`.

### Normalizacija, requester in revizijski dokaz

`VatFormat` odstrani dovoljene presledke, pike in vezaje, črke spremeni v velike ter pred omrežjem preveri obliko države. To je le preverjanje formata, ne potrditev veljavnosti. Grški `GR` se kanonizira v VIES `EL`, Severna Irska uporablja `XI`. `defaultRequester` naj bo lastna identifikacijska številka za DDV organizacije. VIES lahko pri veljavnem rezultatu vrne `consultationNumber`. Pri cache hit pa `consultationNumber` in `requestDate` pripadata prvotnemu preverjanju. Če proces zahteva svež dokaz, upoštevajte `fromCache`, skrajšajte TTL ali cache izključite.

### Admission, async izvajanje in single-flight

`maxPendingSyncRequests` omejuje sočasne sync klicalce, `maxPendingAsyncRequests` edinstvene async leaderje, `maxConcurrentRequests` pa dejanske odhodne HTTP requeste. Presežen pending limit vrne `CLIENT_OVERLOADED`; čakanje na network slot upošteva `admissionTimeout`. Privzeti async executor ustvari virtual thread za sprejeto nalogo. Zunanjega executorja odjemalec ne zapre, saj njegov lifecycle pripada aplikaciji. Preklic future enega followerja ne prekliče skupne operacije.

Single-flight znotraj enega JVM združi sočasne requeste z enako kombinacijo target DDV in requester DDV. Stotine ali tisoči followerjev lahko delijo en upstream request in ne porabijo ločenih pending slotov. Mehanizem ni distribuiran. Vsak pod ima svojo tabelo, semaphore, connection pool in pomnilniški cache, zato vsota per-pod omejitev ni globalna omejitev VIES.

### Cache, retry in zaščita pred stampede

Vgrajeni cache je concurrent ter časovno in velikostno omejen. Shrani samo avtoritativni `Valid`; `Invalid`, napačen vnos in tehnična napaka se ne shranijo. Zunanji `ViesCache`, na primer Redis adapter, mora biti thread-safe, imeti kratek connection/command timeout, versioned namespace ter popolno serializacijo datuma, optionals in cache zastavice. Izjema pri branju vrne `CACHE_ERROR` brez VIES fallbacka, da izpad Redis ne sproži stampede. Napaka pisanja po uspehu ne sme izbrisati avtoritativnega `Valid`.

Lokalni retry je namenoma majhen, največ pet poskusov, z exponential backoff in jitter. Ponovijo se samo prehodne network/VIES napake. `CLIENT_OVERLOADED`, `CLIENT_CLOSED`, blokade in input errors se lokalno ne ponavljajo. V velikem sistemu je glavni mehanizem durable delayed queue z idempotency key, največjim številom poskusov in DLQ. Takojšnji retry vseh workerjev lahko izpad upstreama poslabša.

### Strogo preslikovanje, varnost in opazljivost

Odziv VIES je nezaupanja vreden zunanji JSON. Avtoritativna odločitev zahteva root object, pravi boolean `isValid` ali `valid`, veljaven ISO-8601 `requestDate` in brez nadrejenega `userError`. Manjkajoč ali tekstovni boolean in neveljaven timestamp povzročijo `MALFORMED_RESPONSE`; odjemalec ne izmisli lokalnega časa ali `Invalid`. Produkcija uporablja uradni HTTPS endpoint. Testni `baseUrl` ne sme izvirati iz uporabniškega vnosa. V logih zakrijte DDV, ime, naslov in requester.

Merite rezultate po tipu in `errorCode`, celotno ter upstream p50/p95/p99 latency, cache hit ratio, `CACHE_ERROR`, `CLIENT_OVERLOADED`, retry outcomes, globino in starost queue, delayed retry in DLQ, napake po državah, heap, GC, virtual threads, CPU in sockete. Liveness ne sme biti odvisen od VIES; `availability()` naj bo redka cacheirana diagnostika.

### Arhitektura milijonskih paketov in preverjanje

Produkcijski tok: API ingress → durable partitioned queue → bounded consumer window → worker JVMs → shared Redis → distributed rate limiter → EU in državne storitve VIES. Autoscaling vodite po starosti queue, ne po neomejeni VIES concurrency. Virtual threads znižajo ceno čakanja, vendar ne povečajo upstream kapacitete. Lokalni benchmark ni SLA ali priporočena evropska omejitev.

Pred uvedbo zaženite `./mvnw --batch-mode clean verify`. Unit testi pokrivajo format, requester, mapping, errors, availability, JSON, TTL cache in builder. Lokalna HTTP/concurrency suite deterministično preveri retry, omejitve, cancellation, close races, custom executor, obe smeri sync/async single-flight, cache anti-stampede in fatal `Error`. Live VIES ne sme biti obvezna CI vrata ali cilj load testa; load izvajajte na mocku ali lastnem stagingu. Soak/chaos naj teče 30–60 minut z bounded heapom in JFR, viri pa morajo doseči stabilen plato.
