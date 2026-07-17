# Polski (pl) — Dokumentacja techniczna

**Języki:** [English](../../TECHNICAL.md) · [Čeština](../cs/TECHNICAL.md) · [Polski](TECHNICAL.md) · [Slovenčina](../sk/TECHNICAL.md) · [Slovenščina](../sl/TECHNICAL.md) · [Hrvatski](../hr/TECHNICAL.md) · [Română](../ro/TECHNICAL.md) · [Български](../bg/TECHNICAL.md) · [Ελληνικά](../el/TECHNICAL.md)

> Tłumaczenie informacyjne; rozstrzygający jest angielski oryginał. Licencję stanowi wyłącznie [LICENSE](../../../LICENSE).

## Zakres i model

`vies-client` to Java 21 bez zależności runtime. Moduł `vies.client` eksportuje publiczne `ViesClient`, `ViesResponse`, `ViesError`, `VatFormat`, `ViesRequester`, `ViesAvailability`, `ViesCache`, `ViesException`; `MiniJson` i `TtlCache` są wewnętrzne. Biblioteka nie zastępuje trwałej kolejki, globalnego limitera ani wspólnego cache.

`Valid`/`Invalid` są decyzją VIES, `Unavailable` oznacza brak decyzji, `MalformedInput` błędne dane. Inwariant: `Unavailable` nigdy nie staje się `Invalid`.

## Przepływ i współbieżność

Normalizacja → cache → JVM-local single-flight → bounded admission → współdzielony `HttpClient` → ścisłe mapowanie → zapis tylko `Valid`. Sync czyta cache na caller thread, async w ograniczonym workerze. Limity pamięci: `maxPendingSyncRequests`/`maxPendingAsyncRequests`; sieci: `maxConcurrentRequests`; oczekiwania: `admissionTimeout`. Async używa virtual-thread-per-task, cancel followera nie anuluje leadera.

Retry 0–5, exponential backoff + jitter. Tylko przejściowe błędy sieci/VIES; nie `CLIENT_OVERLOADED`, `CLIENT_CLOSED` i input. Domyślny concurrent cache: 10 000 wpisów, TTL 24 h, klucz target+requester. Read failure → `CACHE_ERROR` bez stampede, write failure zachowuje `Valid`.

JSON jest niezaufany: obiekt, prawdziwy boolean `isValid`/`valid`, poprawny ISO-8601 `requestDate`, bez nadrzędnego `userError`. Inaczej `MALFORMED_RESPONSE`, nigdy wymyślony czas/`Invalid`.

`close()` jest idempotentne, nie przyjmuje nowej pracy, przerywa operacje i zamyka HTTP; zewnętrznego executora nie zamyka. Callbacki są finalizowane poza lifecycle lockiem.

## Topologia, metryki, wydajność i bezpieczeństwo

Produkcja: ingress → trwała partycjonowana kolejka → ograniczone JVM workers → Redis → distributed limiter → VIES; delayed retry/DLQ. Upstream jest twardym limitem.

Mierz typ/`errorCode`, p50/p95/p99, cache/`CACHE_ERROR`, pending/overload, retry, queue/DLQ, kraj, heap/GC/threads/CPU/sockets.

Lokalny JDK 21 loopback, 2026-07-17, mediana 3: cache 8,91 M/s; format 9,02 M/s; HTTP sekwencyjny 4 044/s; async 5 000 przy 256: 21 640/s; 10 000 same-key → 1 HTTP. To mikrotest, nie SLA ani gwarancja VIES.

W produkcji używaj oficjalnego HTTPS, nie przyjmuj `baseUrl` od użytkownika, maskuj dane i loguj stabilne kody.

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
