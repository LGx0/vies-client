# Polski (pl) — Instalacja

**Języki:** [English](../../INSTALLATION.md) · [Čeština](../cs/INSTALLATION.md) · [Polski](INSTALLATION.md) · [Slovenčina](../sk/INSTALLATION.md) · [Slovenščina](../sl/INSTALLATION.md) · [Hrvatski](../hr/INSTALLATION.md) · [Română](../ro/INSTALLATION.md) · [Български](../bg/INSTALLATION.md) · [Ελληνικά](../el/INSTALLATION.md)

> Tłumaczenie informacyjne; rozstrzygający jest angielski oryginał techniczny/prawny. [LICENSE](../../../LICENSE) nie jest tłumaczony jako wiążąca licencja.

## 1. Wymagania

JDK 21+, Maven 3.9+ do budowania i wychodzący HTTPS do `ec.europa.eu:443`; klucz API nie jest potrzebny.

```bash
java -version
javac -version
./mvnw -version
```

Wszystkie polecenia muszą wskazywać ten sam JDK 21+. W przeciwnym razie popraw `JAVA_HOME`.

## 2. JDK 21

```bash
# macOS/Homebrew
brew install openjdk@21
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

# Linux — po instalacji pakietu OpenJDK 21
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
```

Na Intel Mac prefiks to zwykle `/usr/local`; ustawienia trwałe dodaj do `~/.zprofile` i `~/.zshrc`.

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

## 3. Build, Maven, Gradle, JPMS

```bash
./mvnw clean verify
./mvnw install
```

Powstają `target/vies-client-1.0.0.jar`, `-sources.jar`, `-javadoc.jar`; `install` kopiuje je też do lokalnego repo Maven.

```xml
<dependency><groupId>vies.client</groupId><artifactId>vies-client</artifactId><version>1.0.0</version></dependency>
```

```kotlin
repositories { mavenLocal() }
dependencies { implementation("vies.client:vies-client:1.0.0") }
java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }
```

Bez narzędzia build:

```bash
javac -cp /path/to/vies-client-1.0.0.jar MyApplication.java
java -cp "/path/to/vies-client-1.0.0.jar:." MyApplication
```

Separator classpath: Windows `;`, Unix/macOS `:`. W JPMS dodaj `requires vies.client;` i używaj `--module-path`.

## 4. IDE, sieć i weryfikacja

Zmień maszynową ścieżkę JDK w `.vscode/settings.json`, potem `Developer: Reload Window`. Proxy i firmowy CA do inspekcji TLS skonfiguruj w środowisku/truststore JDK. Dozwolony endpoint: `https://ec.europa.eu/taxation_customs/vies/rest-api`.

```bash
./mvnw -q test
java -cp target/classes examples/ViesDemoServer.java
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Live smoke nie powinien blokować CI; standardowe testy używają lokalnego mocka.

## 5. Typowe problemy

| Problem | Rozwiązanie |
|---|---|
| `switch expressions ... Java 14` | SDK i language level = 21 |
| `release version 21 not supported` | popraw `JAVA_HOME` używany przez Maven |
| `UnsupportedClassVersionError` | uruchamiaj na JVM 21+ |
| `NETWORK_ERROR` | DNS/proxy/firewall/TLS |
| `TIMEOUT` | sprawdź sieć i VIES, nie podnoś limitu bez końca |
| `CLIENT_OVERLOADED` | opóźnione retry, ograniczony ingress, workery, globalny limiter |
| `CACHE_ERROR` | sprawdź Redis; nie wywołuj masowo VIES jako obejścia |

## Kompletny przewodnik operacyjny

### Cykl życia klienta i model decyzji

W aplikacji lub procesie workera twórz dokładnie jedną instancję `ViesClient`. Współdzielony klient ponownie wykorzystuje połączenia JDK `HttpClient`, lokalny cache TTL, tabelę single-flight oraz wszystkie limity admission. Tworzenie klienta dla każdego requestu usuwa te zabezpieczenia i zwiększa liczbę połączeń oraz zużycie pamięci. Klient jest thread-safe. Podczas kontrolowanego zamknięcia wywołuj `close()`, użyj `try`-with-resources albo Spring `@Bean(destroyMethod = "close")`. Po zamknięciu nowe wywołania sync i async rzucają synchronicznie `IllegalStateException`, a wspólne operacje w toku kończą się strukturalnym `CLIENT_CLOSED`.

Sealed hierarchy rozdziela decyzję biznesową od niepewności technicznej. `Valid` oznacza potwierdzenie przez VIES, `Invalid` brak potwierdzenia, `MalformedInput` dane niemożliwe do wysłania, a `Unavailable` brak decyzji. `Unavailable` nigdy nie może zostać zamienione na `Invalid`. W logice, metrykach i retry używaj stabilnego `error.code()` oraz `error.retryable()`, a nie lokalizowanego tekstu. Użytkownikowi można zwrócić `messageHu` i `messageEn`.

### Normalizacja, requester i dowód kontroli

`VatFormat` usuwa dozwolone spacje, kropki i myślniki, zamienia litery na wielkie i sprawdza kształt właściwy dla państwa przed użyciem sieci. To filtr formatu, nie potwierdzenie ważności. Greckie `GR` jest kanonizowane do VIES `EL`, a Irlandia Północna używa `XI`. `defaultRequester` powinien być własnym numerem VAT UE organizacji. Dla prawidłowej kontroli VIES może zwrócić `consultationNumber`. Przy cache hit `consultationNumber` i `requestDate` opisują jednak pierwotną kontrolę. Jeśli proces wymaga świeżego dowodu, uwzględnij `fromCache`, skróć TTL albo wyłącz cache.

### Admission, async i single-flight

`maxPendingSyncRequests` ogranicza jednoczesnych callerów sync, `maxPendingAsyncRequests` unikalnych leaderów async, a `maxConcurrentRequests` rzeczywiste requesty HTTP. Przekroczenie pending limitu daje `CLIENT_OVERLOADED`; oczekiwanie na slot sieciowy respektuje `admissionTimeout`. Domyślny async executor tworzy virtual thread dla zaakceptowanego zadania. Przekazanego executora klient nie zamyka, ponieważ jego lifecycle należy do aplikacji. Anulowanie future jednego followera nie anuluje wspólnej operacji.

Single-flight łączy w jednym JVM równoczesne requesty o tej samej parze target VAT i requester VAT. Setki lub tysiące followerów mogą współdzielić jedno zapytanie upstream i nie zużywają osobnych pending slots. Mechanizm nie jest rozproszony. Każdy pod ma osobną tabelę, semafory, connection pool i pamięciowy cache, więc suma limitów per-pod nie jest globalnym limitem VIES.

### Cache, retry i ochrona przed stampede

Wbudowany cache jest concurrent, ograniczony czasowo i rozmiarem. Przechowuje wyłącznie autorytatywny `Valid`; `Invalid`, błędny input i awarie nie są cache’owane. Zewnętrzny `ViesCache`, na przykład adapter Redis, musi być thread-safe, mieć krótki connection/command timeout, wersjonowany namespace i pełną serializację daty, optionals oraz flagi cache. Wyjątek odczytu zwraca `CACHE_ERROR` bez fallbacku do VIES, aby awaria Redis nie spowodowała stampede. Błąd zapisu po sukcesie nie usuwa autorytatywnego `Valid`.

Lokalny retry jest celowo mały, maksymalnie pięć prób, z exponential backoff i jitter. Powtarzane są tylko przejściowe błędy sieci/VIES. `CLIENT_OVERLOADED`, `CLIENT_CLOSED`, blokady i input errors nie są lokalnie ponawiane. W dużej skali głównym mechanizmem jest trwała delayed queue z idempotency key, limitem prób i DLQ. Natychmiastowy retry we wszystkich workerach może pogłębić awarię upstream.

### Ścisłe mapowanie, bezpieczeństwo i obserwowalność

Odpowiedź VIES jest niezaufanym JSON-em. Decyzja wymaga obiektu root, prawdziwego boolean `isValid` lub `valid`, poprawnego ISO-8601 `requestDate` i braku nadrzędnego `userError`. Brakujący albo tekstowy boolean oraz błędny timestamp powodują `MALFORMED_RESPONSE`; klient nie wymyśla czasu lokalnego ani `Invalid`. Produkcja korzysta z oficjalnego HTTPS. Testowy `baseUrl` nie może pochodzić z inputu użytkownika. W logach maskuj VAT, nazwę, adres i requester.

Mierz liczbę wyników według typu i `errorCode`, p50/p95/p99 całkowitej oraz upstream latency, cache hit ratio, `CACHE_ERROR`, `CLIENT_OVERLOADED`, retry outcomes, głębokość i wiek kolejki, delayed retry, DLQ, błędy per-country, heap, GC, virtual threads, CPU i sockety. Liveness nie zależy od VIES; `availability()` jest rzadką, cache’owaną diagnostyką.

### Architektura milionowych partii i weryfikacja

Przepływ produkcyjny: API ingress → trwała partycjonowana kolejka → bounded consumer window → worker JVMs → shared Redis → distributed rate limiter → VIES UE i państw. Autoscaling opieraj na wieku kolejki, nie na nieograniczonej concurrency VIES. Virtual threads zmniejszają koszt oczekiwania, ale nie zwiększają pojemności upstream. Lokalny benchmark nie jest SLA ani zalecanym limitem europejskim.

Przed wdrożeniem uruchom `./mvnw --batch-mode clean verify`. Unit tests obejmują format, requester, mapping, errors, availability, JSON, TTL cache i builder. Lokalna suite HTTP/concurrency deterministycznie sprawdza retry, limity, cancellation, close races, custom executor, oba kierunki sync/async single-flight, cache anti-stampede i fatal `Error`. Live VIES nie może być obowiązkową bramą CI ani celem load testu; load uruchamiaj na mocku lub własnym staging. Soak/chaos powinien działać 30–60 minut z ograniczonym heapem i JFR, a zasoby muszą osiągnąć plateau.
