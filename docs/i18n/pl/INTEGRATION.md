# Polski (pl) — Integracja

**Języki:** [English](../../INTEGRATION.md) · [Čeština](../cs/INTEGRATION.md) · [Polski](INTEGRATION.md) · [Slovenčina](../sk/INTEGRATION.md) · [Slovenščina](../sl/INTEGRATION.md) · [Hrvatski](../hr/INTEGRATION.md) · [Română](../ro/INTEGRATION.md) · [Български](../bg/INTEGRATION.md) · [Ελληνικά](../el/INTEGRATION.md)

> Tłumaczenie informacyjne; obowiązuje angielski oryginał techniczny/prawny. Wiążący tekst licencji: [LICENSE](../../../LICENSE).

## Cykl życia i API

Używaj jednego thread-safe `ViesClient` na proces. Klient tworzony dla każdego requestu traci pool, cache, single-flight i limity. `close()` przerywa pracę wewnętrzną, kończy wspólne requesty jako `CLIENT_CLOSED`; późniejsze sync i async wywołania rzucają synchronicznie `IllegalStateException`.

```java
var client = ViesClient.builder()
    .connectTimeout(Duration.ofSeconds(5)).requestTimeout(Duration.ofSeconds(8))
    .admissionTimeout(Duration.ofSeconds(2)).maxConcurrentRequests(32)
    .maxPendingSyncRequests(512).maxPendingAsyncRequests(512).retries(1).build();
```

`check()` i `checkAsync()` zwracają cztery warianty. Async domyślnie używa wirtualnych wątków. Identyczne VAT/requester współdzielą pracę, lecz cancel jednego future nie anuluje pozostałych. Okno przetwarzania kolejki musi być ograniczone.

## Kontrakt HTTP

| Wynik | HTTP | Retry |
|---|---:|---|
| `Valid` / `Invalid` | 200 | nie |
| `MalformedInput` | 400 | nie |
| `Unavailable(CLIENT_OVERLOADED)` | 429 | opóźnione |
| inne `Unavailable` | 503 | `error.retryable()` |

JSON błędu powinien zawierać `errorCode`, `messageHu`, `messageEn`, `retryable`. Logika i metryki mają używać kodu, nie tekstu.

## Spring, JDK, requester i cache

W Spring Boot rejestruj singleton przez `@Bean(destroyMethod = "close")` i mapuj wszystkie warianty w controllerze. Biblioteka nie zależy od Springa. Przykład czystego serwera: `examples/ViesDemoServer.java`.

```java
var client = ViesClient.builder().defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER"))).build();
```

Requester jest własnym VAT UE, wczytanym przez `MY_EU_VAT_NUMBER` z zaufanego sekretu lub konfiguracji. VIES może zwrócić `consultationNumber`, lecz pole jest opcjonalne, niegwarantowane, a jego wartość dowodowa zależy od lokalnych przepisów. Trafienie cache zawiera pierwotny `requestDate`/`consultationNumber`. `disableCache()` wyłącza tylko zapisany cache: identyczne równoległe wywołania VAT+requester mogą współdzielić jedno żądanie single-flight; późniejsze wywołanie po zakończeniu wysyła nowe żądanie.

Adapter `ViesCache` musi być thread-safe, mieć krótki timeout, wersjonowany namespace, metryki, brak nieograniczonego retry i pełną serializację. Błąd odczytu daje `CACHE_ERROR` bez fallbacku do VIES; błąd zapisu nie usuwa potwierdzonego `Valid`.

## Skala, obserwowalność, checklist

Najpierw utrwal zadanie i idempotency key; stosuj małe lokalne retry, opóźnienie wykładnicze, limit prób i DLQ. Nie retry `Invalid` ani niezmienionego `MalformedInput`. Każdy pod ma własne semafory, więc wymagany jest rozproszony limiter, wspólny Redis, trwała kolejka i deduplikacja.

- Liveness niezależny od VIES; `availability()` rzadko i z cache.
- Mierz p50/p95/p99, result/`errorCode`, cache hit, retry, overload, queue age, DLQ.
- Maskuj VAT/nazwę/adres; zakazane są nieograniczone future i immediate retry wszystkich błędów.
- Przed produkcją potwierdź lifecycle, cztery warianty, HU/EN errors, wszystkie limity, global limiter, świeżość cache, idempotencję, alerty, load/soak/failure testy.

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
