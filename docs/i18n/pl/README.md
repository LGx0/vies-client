# Polski (pl) — vies-client — sprawdzanie numeru VAT UE

**Frazy wyszukiwania:** sprawdzanie numeru VAT, walidator numeru VAT UE, walidacja VAT UE, sprawdzanie identyfikatora podatkowego, klient Java VIES; `VAT checker`, `VAT number validator`, `EU VAT validation`, `tax ID checker`.

To nie jest ogólny kalkulator podatkowy, lecz klient do sprawdzania unijnych numerów identyfikacyjnych VAT w systemie VIES.

[Wszystkie języki](../../LANGUAGES.md)

**Języki:** [English](../../../README.md) · [Magyar](../../../README.md) · [Čeština](../cs/README.md) · [Polski](README.md) · [Slovenčina](../sk/README.md) · [Slovenščina](../sl/README.md) · [Hrvatski](../hr/README.md) · [Română](../ro/README.md) · [Български](../bg/README.md) · [Ελληνικά](../el/README.md)

> To tłumaczenie ma charakter informacyjny. W razie rozbieżności obowiązuje angielski oryginał techniczny i prawny. Jedynym wiążącym tekstem licencji jest główny plik [LICENSE](../../../LICENSE); tłumaczenia nie są licencją.

`vies-client` to samodzielny klient Java 21+ bez zależności czasu wykonania dla REST API VIES Komisji Europejskiej. Działa ze Spring Boot, Quarkus, Micronaut i czystym JDK `HttpServer`, używając wyłącznie `java.net.http`.

- Dokumentacja VIES: <https://ec.europa.eu/taxation_customs/vies/#/technical-information>
- Walidacja: `https://ec.europa.eu/taxation_customs/vies/rest-api/ms/{countryCode}/vat/{vatNumber}`
- Status państw: `https://ec.europa.eu/taxation_customs/vies/rest-api/check-status`

## Dokumentacja

[Instalacja](INSTALLATION.md) · [Integracja](INTEGRATION.md) · [Technika](TECHNICAL.md) · [Testy](TESTING.md) · [Open source](OPEN_SOURCE.md) · [Wydania](RELEASING.md)

## Budowanie i użycie

```bash
./mvnw clean verify
./mvnw install
```

```xml
<dependency><groupId>vies.client</groupId><artifactId>vies-client</artifactId><version>1.2.0</version></dependency>
```

```kotlin
implementation("vies.client:vies-client:1.2.0")
```

Moduł JPMS nazywa się `vies.client`; w `module-info.java` dodaj `requires vies.client;`. Działa również na classpath.

```java
try (var vies = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
        .retries(1).build()) {
    switch (vies.check("DE 000 000 000")) {
        case ViesResponse.Valid v -> System.out.println(v.traderName().orElse("?"));
        case ViesResponse.Invalid i -> System.out.println("INVALID");
        case ViesResponse.Unavailable u -> System.out.println(u.errorCode());
        case ViesResponse.MalformedInput m -> System.out.println(m.reason());
    }
}
```

`defaultRequester` to własny numer VAT UE, wczytany przez `MY_EU_VAT_NUMBER` z
zaufanego sekretu lub konfiguracji. VIES **może** zwrócić `consultationNumber`, ale
pole jest opcjonalne, nigdy niegwarantowane, a jego wartość dowodowa zależy od
lokalnych przepisów. Przy trafieniu cache identyfikator i data dotyczą pierwotnej konsultacji.

## Asynchroniczność i skala

`checkAsync()` domyślnie używa wirtualnych wątków. Jeden thread-safe klient powinien być singletonem procesu i zostać zamknięty przez `close()`. `maxPendingSyncRequests`, `maxPendingAsyncRequests` oraz `maxConcurrentRequests` zapewniają lokalny backpressure, a single-flight łączy identyczne żądania w jednym JVM.

Przy milionach elementów stosuj trwałą partycjonowaną kolejkę, ograniczone workery, wspólny Redis, rozproszony rate limiter, opóźnione retry i DLQ. Wirtualne wątki nie zwiększają przepustowości VIES. Nigdy nie zamieniaj `Unavailable` na `Invalid`.

```java
var vies = ViesClient.builder().cache(redisViesCache).cacheTtl(Duration.ofHours(24))
    .maxConcurrentRequests(32).maxPendingSyncRequests(512).maxPendingAsyncRequests(512)
    .admissionTimeout(Duration.ofSeconds(2)).retries(2)
    .retryDelay(Duration.ofMillis(250)).build();
```

Sealed hierarchy wyników: `Valid`, `Invalid`, `Unavailable`, `MalformedInput`. `error()` udostępnia stabilny kod, `messageHu`, `messageEn` i `retryable`. Zalecane HTTP: decyzje 200, błędne dane 400, lokalne przeciążenie 429, pozostała niedostępność 503.

Cache przechowuje wyłącznie `Valid`; błąd odczytu cache daje `CACHE_ERROR`, zapobiegając stampede. Grecja używa `EL` (`GR` jest mapowane), Irlandia Północna `XI`.

```bash
./mvnw test
```

Projekt ma jedną licencję **Apache License 2.0**, nie MIT/Apache dual license. Nie jest oficjalnym ani zatwierdzonym produktem UE lub administracji podatkowej.

## Pełna konfiguracja i semantyka operacyjna

Cykl requestu: normalizacja offline → cache → JVM-local single-flight → bounded admission → współdzielony HTTP client → strict validation → zapis tylko potwierdzonego Valid.

| Builder option | Default | Operational role |
|---|---:|---|
| `baseUrl(String)` | official VIES REST | test/mock override only |
| `connectTimeout(Duration)` | 5 s | TCP/TLS connection bound |
| `requestTimeout(Duration)` | 8 s | complete request bound |
| `admissionTimeout(Duration)` | 2 s | maximum wait for network slot |
| `defaultRequester(ViesRequester)` | none | own VAT, consultation identifier |
| `retries(int)` | 0 | 0–5 transient retries, exponential backoff+jitter |
| `retryDelay(Duration)` | 400 ms | backoff base |
| `maxConcurrentRequests(int)` | 32 | active VIES HTTP calls per instance |
| `maxPendingSyncRequests(int)` | 512 | bounded sync callers; overflow → CLIENT_OVERLOADED |
| `maxPendingAsyncRequests(int)` | 512 | bounded unique async leaders |
| `cacheTtl(Duration)` | 24 h | Valid result lifetime |
| `cacheMaxEntries(int)` | 10,000 | in-memory cache bound |
| `cache(ViesCache)` | built-in | Redis/other shared cache extension |
| `disableCache()` | — | brak zapisanego cache; identyczne równoległe wywołania mogą współdzielić jeden single-flight |
| `userAgent(String)` | module ID | identify the integration |
| `executor(ExecutorService)` | virtual thread/task | caller owns custom executor lifecycle |

Unavailable oznacza brak decyzji i nie może być liczone jako Invalid. Walidacja formatu nie jest potwierdzeniem VIES. Retry zwiększa ruch; utrzymuj małą liczbę lokalną, a w skali używaj delayed queue. Cache consultationNumber/requestDate należy do pierwotnej kontroli. `disableCache()` wyłącza tylko zapisany cache: identyczne równoległe wywołania VAT+requester mogą współdzielić jedno żądanie sieciowe single-flight; późniejsze wywołanie po jego zakończeniu wysyła nowe żądanie.

| Result | HTTP | Retry | Cache |
|---|---:|---:|---:|
| `Valid` | 200 | no | yes |
| `Invalid` | 200 | no | no |
| `MalformedInput` | 400 | no | no |
| `Unavailable(CLIENT_OVERLOADED)` | 429 | delayed | no |
| other `Unavailable` | 503 | by `error.retryable()` | no |

## Kompletny przewodnik operacyjny

### Cykl życia klienta i model decyzji

W aplikacji lub procesie workera twórz dokładnie jedną instancję `ViesClient`. Współdzielony klient ponownie wykorzystuje połączenia JDK `HttpClient`, lokalny cache TTL, tabelę single-flight oraz wszystkie limity admission. Tworzenie klienta dla każdego requestu usuwa te zabezpieczenia i zwiększa liczbę połączeń oraz zużycie pamięci. Klient jest thread-safe. Podczas kontrolowanego zamknięcia wywołuj `close()`, użyj `try`-with-resources albo Spring `@Bean(destroyMethod = "close")`. Po zamknięciu nowe wywołania sync i async rzucają synchronicznie `IllegalStateException`, a wspólne operacje w toku kończą się strukturalnym `CLIENT_CLOSED`.

Sealed hierarchy rozdziela decyzję biznesową od niepewności technicznej. `Valid` oznacza potwierdzenie przez VIES, `Invalid` brak potwierdzenia, `MalformedInput` dane niemożliwe do wysłania, a `Unavailable` brak decyzji. `Unavailable` nigdy nie może zostać zamienione na `Invalid`. W logice, metrykach i retry używaj stabilnego `error.code()` oraz `error.retryable()`, a nie lokalizowanego tekstu. Użytkownikowi można zwrócić `messageHu` i `messageEn`.

### Normalizacja, requester i dowód kontroli

`VatFormat` usuwa dozwolone spacje, kropki i myślniki, zamienia litery na wielkie i sprawdza kształt właściwy dla państwa przed użyciem sieci. To filtr formatu, nie potwierdzenie ważności. Greckie `GR` jest kanonizowane do VIES `EL`, a Irlandia Północna używa `XI`. `defaultRequester` powinien być własnym numerem VAT UE organizacji. Dla prawidłowej kontroli VIES może zwrócić opcjonalny, niegwarantowany `consultationNumber`; jego wartość prawna zależy od lokalnych przepisów. Przy cache hit `consultationNumber` i `requestDate` opisują jednak pierwotną kontrolę.

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
