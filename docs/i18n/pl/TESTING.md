# Polski (pl) — Testowanie

**Języki:** [English](../../TESTING.md) · [Čeština](../cs/TESTING.md) · [Polski](TESTING.md) · [Slovenčina](../sk/TESTING.md) · [Slovenščina](../sl/TESTING.md) · [Hrvatski](../hr/TESTING.md) · [Română](../ro/TESTING.md) · [Български](../bg/TESTING.md) · [Ελληνικά](../el/TESTING.md)

> Tłumaczenie informacyjne; obowiązuje angielski oryginał techniczny/prawny. [LICENSE](../../../LICENSE) nie jest tłumaczoną licencją.

Unit test izoluje klasę/regułę bez sieci, DB i live VIES. Timeout, retry, single-flight, cancel i `close()` wymagają także deterministycznych lokalnych testów HTTP/concurrency z loopback mockiem.

```bash
./mvnw test
./mvnw -Dtest=VatFormatTest,ViesRequesterTest,ViesResponseMappingTest,ViesErrorTest,ViesAvailabilityTest,ViesClientBuilderTest,MiniJsonTest,TtlCacheTest test
./mvnw -Dtest=ViesClientHttpTest test
./mvnw clean verify
./mvnw -Dtest=ViesClientHttpTest#cancellationDoesNotLeakAsyncCapacity test
```

Pakiet ma **73 testów**: 44 unit w 8 klasach i 29 lokalne HTTP/integration/concurrency; zero obowiązkowych wywołań zewnętrznych.

## Katalog unit

- `VatFormatTest` 8: normalizacja, separatory, `GR→EL`, błędy, formaty krajów, pair API, kody, 28 kształtów.
- `ViesRequesterTest` 4: full VAT, Grecja, pair, fail-fast.
- `ViesResponseMappingTest` 11: GET/POST valid, invalid, errors, JSON root, boolean i timestamp strictness.
- `ViesErrorTest` 6: HU/EN, retry classification, cały katalog, unknown bez storm.
- `ViesAvailabilityTest` 2; `MiniJsonTest` 4; `TtlCacheTest` 6 (w tym 32 virtual threads); `ViesClientBuilderTest` 3 (URL/limity/retry/duration/overflow).

## Lokalne HTTP/concurrency (29)

Retry 503→success, 200 same-key→1 HTTP, limit aktywnych requestów, sync/async backpressure, cancel bez permit leak, close z callbacka, admission timeout, cache error bez fallbacku, cleanup permit/single-flight, oba cache-close race, custom executor interruption/queued cancel, jednakowy leader/follower, followers bez slotów, rejection cleanup, sync↔async single-flight w obu kierunkach, cache recheck, close przy cache write, chained unique, blocking callback i fatal `Error` propagation.

## Live, load, soak, CI

Live VIES nie może blokować CI: opt-in najwyżej jedna `availability()` i jedna walidacja przy concurrency=1/retries=0, bez prywatnych requester VAT w repo/logach. Load wyłącznie na mock/staging: distinct, hot-key, overload/recovery, mix 200/429/503/timeout/malformed, cache pressure. Soak/chaos 30–60 min z ograniczonym heapem, JFR, restartami, latency/reset/cache failure/cancel; zasoby muszą osiągnąć plateau.

```bash
JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=target/vies-soak.jfr,settings=profile" ./mvnw test
./mvnw --batch-mode clean verify
```

Każdy bug dostaje deterministyczny regresyjny test. W concurrency używaj latch/barrier; `sleep` nie może być orakulum poprawności.

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

### Dodatkowe kryteria akceptacji technicznej

Przegląd techniczny musi potwierdzić, że wszystkie semafory i wpisy `inFlight` są zwalniane również po rejection, cancellation, przerwaniu i wyjątku cache. Leader i followers tej samej operacji muszą otrzymać semantycznie identyczny rezultat podczas `close()`. Callback użytkownika nie może przetrzymywać lifecycle lock ani blokować zamknięcia. Custom executor pozostaje własnością aplikacji, ale uchwyty wysłanych zadań muszą umożliwiać anulowanie kolejki. Bardzo duże dodatnie `Duration` są odrzucane przed konwersją do nanos lub millis, aby uniknąć overflow.

Test akceptacyjny powinien być deterministyczny: blokowana cache, kontrolowany executor i lokalny serwer używają latchy, a nie czasu procesora. Po każdym scenariuszu należy sprawdzić liczbę requestów HTTP, odzyskanie pending capacity, usunięcie single-flight oraz zgodny kod błędu. Powtarzane wykonanie suite nie może pozostawiać wątków, socketów ani rosnącej mapy. Zmiana wydajności wymaga tego samego JDK, konfiguracji, warm-upu i lokalnego mocka; wynik nie jest obietnicą przepustowości publicznego VIES.
