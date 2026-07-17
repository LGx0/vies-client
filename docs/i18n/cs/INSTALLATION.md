# Čeština (cs) — Instalace

**Jazyky:** [English](../../INSTALLATION.md) · [Čeština](INSTALLATION.md) · [Polski](../pl/INSTALLATION.md) · [Slovenčina](../sk/INSTALLATION.md) · [Slovenščina](../sl/INSTALLATION.md) · [Hrvatski](../hr/INSTALLATION.md) · [Română](../ro/INSTALLATION.md) · [Български](../bg/INSTALLATION.md) · [Ελληνικά](../el/INSTALLATION.md)

> Informativní překlad; při rozdílu platí anglický technický/právní originál. [LICENSE](../../../LICENSE) se jako právně závazná licence nepřekládá.

## 1. Požadavky

- JDK 21 nebo novější; Maven 3.9+ pro sestavení ze zdrojů.
- Odchozí HTTPS na `ec.europa.eu:443`; není potřeba API klíč.

```bash
java -version
javac -version
./mvnw -version
```

Všechny příkazy musí používat stejný JDK 21+. Jinak opravte `JAVA_HOME`.

## 2. Nastavení JDK 21

macOS/Homebrew:

```bash
brew install openjdk@21
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

Na Intel Macu bývá prefix `/usr/local`. Pro trvalé nastavení vložte exporty do `~/.zprofile` a `~/.zshrc`.

Linux:

```bash
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
```

Windows PowerShell:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

## 3. Sestavení a artefakty

```bash
./mvnw clean verify
./mvnw install
```

`verify` kompiluje, spustí unit a lokální integrační testy a vytvoří `target/vies-client-1.2.0.jar`, `-sources.jar` a `-javadoc.jar`. `install` je navíc uloží do lokálního Maven repozitáře.

## 4. Maven, Gradle a classpath

```xml
<dependency><groupId>vies.client</groupId><artifactId>vies-client</artifactId><version>1.2.0</version></dependency>
```

```kotlin
repositories { mavenLocal() }
dependencies { implementation("vies.client:vies-client:1.2.0") }
java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }
```

Bez build nástroje:

```bash
javac -cp /path/to/vies-client-1.2.0.jar MyApplication.java
java -cp "/path/to/vies-client-1.2.0.jar:." MyApplication
```

Ve Windows odděluje classpath `;`, v Unix/macOS `:`. Pro JPMS přidejte `requires vies.client;` a použijte `--module-path`.

## 5. IDE, síť a smoke test

Strojově specifickou cestu v `.vscode/settings.json` nastavte na vlastní JDK 21 a spusťte `Developer: Reload Window`. Firemní proxy a CA pro TLS inspekci konfigurujte v běhovém JDK/truststore. Povolený základ URL je `https://ec.europa.eu/taxation_customs/vies/rest-api`.

```bash
./mvnw -q test
java -cp target/classes examples/ViesDemoServer.java
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Živý VIES smoke test je síťově závislý a nesmí blokovat CI; běžné testy používají lokální mock.

## 6. Řešení problémů

| Problém | Řešení |
|---|---|
| `switch expressions ... Java 14` | Project SDK/language level nastavte na 21 |
| `release version 21 not supported` | Maven běží na starém JDK; opravte `JAVA_HOME` |
| `UnsupportedClassVersionError` | Spusťte na JVM 21+ |
| `NETWORK_ERROR` | Prověřte DNS, proxy, firewall a TLS |
| `TIMEOUT` | Prověřte síť/VIES; timeout nezvyšujte bez omezení |
| `CLIENT_OVERLOADED` | Opožděný retry, omezený ingress, workery a globální limiter |
| `CACHE_ERROR` | Prověřte Redis; neobcházejte jej masovým přímým voláním VIES |

## Provozní příručka pro úplnou implementaci

### Životní cyklus klienta a rozhodovací model

V jedné aplikaci nebo worker procesu vytvořte právě jednu instanci `ViesClient`. Sdílená instance znovu používá spojení JDK `HttpClient`, lokální TTL cache, tabulku single-flight a všechny admission limity. Vytváření klienta pro každý HTTP požadavek tyto ochrany ruší a zvyšuje počet spojení i paměťové nároky. Klient je thread-safe. Při řízeném ukončení vždy volejte `close()` nebo použijte `try`-with-resources či Spring `@Bean(destroyMethod = "close")`. Po uzavření nové synchronní i asynchronní volání okamžitě vyhodí `IllegalStateException`; již sdílené rozpracované operace skončí strukturovaným `CLIENT_CLOSED`.

Výsledková sealed hierarchie rozlišuje obchodní rozhodnutí od technické nejistoty. `Valid` znamená, že VIES číslo potvrdil, `Invalid` že je nepotvrdil, `MalformedInput` že vstup nelze odeslat, a `Unavailable` že žádné rozhodnutí nevzniklo. `Unavailable` se nikdy nesmí převést na `Invalid`. Pro programovou logiku, metriky a retry používejte stabilní `error.code()` a `error.retryable()`, nikoli lokalizovaný text. Uživateli lze vrátit `messageHu` a `messageEn`.

### Normalizace, requester a auditní důkaz

`VatFormat` odstraňuje povolené mezery, tečky a pomlčky, převádí písmena na velká a kontroluje tvar podle země ještě před sítí. Jde pouze o formátový filtr, nikoli potvrzení platnosti. Řecký prefix `GR` se kanonizuje na VIES `EL`; Severní Irsko používá `XI`. `defaultRequester` má být vlastní komunitární DIČ organizace. VIES může pro platnou kontrolu vrátit `consultationNumber`. U cache hitu však `consultationNumber` a `requestDate` dokumentují původní kontrolu, nikoli novou konzultaci. Požaduje-li obchodní proces čerstvý důkaz, zohledněte `fromCache`, zkraťte TTL nebo cache vypněte.

### Admission, asynchronní provoz a single-flight

`maxPendingSyncRequests` omezuje počet současných synchronních volajících, `maxPendingAsyncRequests` počet jedinečných asynchronních leaderů a `maxConcurrentRequests` skutečné odchozí HTTP požadavky. Při překročení pending limitu klient vrací `CLIENT_OVERLOADED`; při čekání na síťový slot respektuje `admissionTimeout`. Výchozí async executor vytváří virtuální vlákno pro přijatou úlohu. Vlastní executor klient nezavírá, protože jeho životní cyklus patří aplikaci. Zrušení future jednoho followera neruší sdílenou operaci ostatních.

Single-flight slučuje současné požadavky se stejnou kombinací cílového DIČ a requester DIČ uvnitř jednoho JVM. Sto nebo deset tisíc followerů tak může sdílet jeden upstream požadavek a nečerpá sto či deset tisíc pending slotů. Mechanismus není distribuovaný. Každý pod má vlastní tabulku, semafory, connection pool a paměťovou cache; součet lokálních limitů proto není globální limit vůči VIES.

### Cache, retry a ochrana proti stampede

Vestavěná cache je souběžná, časově omezená a velikostně ohraničená. Ukládá pouze autoritativní `Valid`; `Invalid`, chybný vstup ani technická chyba se neukládají. Externí `ViesCache`, například Redis adaptér, musí být thread-safe, používat krátký connection/command timeout, verzovaný namespace a úplně serializovat datum, optionals a příznak cache. Výjimka při čtení vrací `CACHE_ERROR` bez automatického přechodu na VIES, aby výpadek Redis nezpůsobil stampede. Chyba zápisu po úspěšném VIES výsledku autoritativní `Valid` nemaže.

Lokální retry je záměrně malé, nejvýše pět pokusů, s exponenciálním backoffem a jitterem. Opakují se pouze přechodné síťové nebo VIES chyby. `CLIENT_OVERLOADED`, `CLIENT_CLOSED`, blokace a vstupní chyby se lokálně neopakují. Ve velkém provozu má být hlavní retry mechanismus trvalá delayed queue s idempotency key, maximálním počtem pokusů a DLQ. Okamžité opakování ve všech workerech může zhoršit výpadek upstreamu.

### Přísná validace, bezpečnost a observability

Odpověď VIES je nedůvěryhodný externí JSON. Autoritativní výsledek vyžaduje kořenový objekt, skutečný boolean `isValid` nebo `valid`, platný ISO-8601 `requestDate` a žádný `userError`, který rozhodnutí přepisuje. Chybějící nebo řetězcový boolean a neplatný timestamp vedou k `MALFORMED_RESPONSE`; klient nevymýšlí lokální datum ani `Invalid`. Produkce musí používat oficiální HTTPS endpoint. Testovací `baseUrl` nesmí pocházet z uživatelského vstupu. V logu maskujte DIČ, název, adresu a requester.

Měřte počet výsledků podle typu a `errorCode`, celkovou a upstream p50/p95/p99 latenci, cache hit ratio, `CACHE_ERROR`, `CLIENT_OVERLOADED`, počty retry, stáří a hloubku fronty, delayed retry a DLQ, chybovost jednotlivých zemí, heap, GC, virtuální vlákna, CPU a sockety. Liveness nesmí záviset na VIES; `availability()` používejte jako řídkou cacheovanou diagnostiku.

### Architektura pro milionové dávky a ověření

Produkční tok je: API ingress → trvalá dělená fronta → omezené consumer okno → více worker JVM → sdílený Redis → distribuovaný rate limiter → EU a členské VIES. Autoscaling řiďte stářím fronty, ne neomezenou VIES concurrency. Virtuální vlákna snižují cenu čekání, ale nezvyšují upstream kapacitu. Absolutní lokální benchmark není SLA ani doporučený evropský limit.

Před nasazením spusťte `./mvnw --batch-mode clean verify`. Unit testy ověřují normalizaci, requester, mapování, chyby, dostupnost, JSON, TTL cache a builder. Lokální HTTP/concurrency suite deterministicky kontroluje retry, limity, cancellation, close races, custom executor, oba směry sync/async single-flight, cache anti-stampede a fatal `Error`. Živý VIES nesmí být povinnou CI bránou ani cílem load testu; load patří na lokální mock nebo vlastní staging. Soak/chaos má běžet 30–60 minut s omezeným heapem a JFR a zdroje musí dosáhnout stabilního plata.
