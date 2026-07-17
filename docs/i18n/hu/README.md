# Magyar (hu) — vies-client — közösségi adószám-ellenőrző

**Keresési kifejezések:** közösségi adószám ellenőrzés, áfaszám-validátor, EU áfaazonosító ellenőrzés, adóazonosító ellenőrzés, VIES Java kliens; `VAT checker`, `VAT number validator`, `EU VAT validation`, `tax ID checker`.

Ez nem általános adókalkulátor, hanem az EU VIES rendszerében közösségi áfaazonosítókat ellenőrző kliens.

> [Nyelvválasztó](../../LANGUAGES.md) · Ez a lokalizáció az elérhetőséget szolgálja. Eltérés esetén a kanonikus angol technikai vagy jogi forrás az irányadó. A gyökér `LICENSE` és`NOTICE` jogilag irányadó, fordítás nem helyettesíti.

[![Licenc: Apache-2.0](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](../../../LICENSE)[![Java 21+](https://img.shields.io/badge/Java-21%2B-orange.svg)](../../../pom.xml)
🌍 **[Nyelv / Nyelv / Az EU összes hivatalos nyelve](../../LANGUAGES.md)**
Önálló, **nulla futásidejű függőségű** Java kliens az Európai Bizottság VIES
(VAT Information Exchange System) adószám-ellenőrző REST API-jához. Bármely Java
nyelvű API-szerverbe vagy programba beköthető: Spring Boot, Quarkus, Micronaut,
vagy akár sima JDK `HttpServer`— a modul csak a JDK-t használja (`java.net.http`),
nincs tranzitív függőség-fa.
Független nyílt forráskódú projekt; nem az Európai Bizottság, az EU vagy tagállami
adóhatóság hivatalos terméke, és azok nem támogatják vagy hitelesítik.

- **Követelmény:** Java **21+** bytecode/API; a teljes csomag JDK 21-en ellenőrzött.
- **Hivatalos VIES dokumentáció:** <https://ec.europa.eu/taxation_customs/vies/#/technical-information>
- **Hívott végpont:**`https://ec.europa.eu/taxation_customs/vies/rest-api/ms/{countryCode}/vat/{vatNumber}`
- **Tagállami elérhetőség:**`https://ec.europa.eu/taxation_customs/vies/rest-api/check-status`

## Dokumentáció / Documentation

- [Telepítés / Installation](INSTALLATION.md)
- [Bekötés / Integration](INTEGRATION.md)
- [Technikai felépítés / Technical design](TECHNICAL.md)
- [Unit-, integrációs és konkurenciatesztek / Testing](TESTING.md)
- [Nyílt forráskód és licencdöntés / Open source](OPEN_SOURCE.md)
- [Kiadási útmutató / Releasing](RELEASING.md)
- [GitHub közzététel / GitHub publication](GITHUB_SETUP.md)
- [Közreműködés / Contributing](CONTRIBUTING.md)
- [Biztonsági szabályzat / Security](SECURITY.md)
- [Magatartási kódex / Code of Conduct](CODE_OF_CONDUCT.md)
- [Támogatás és adományozás / Support and donations](SUPPORT.md)
- [Harmadik féltől származó összetevők / Third-party notices](THIRD_PARTY_NOTICES.md)
- [Változásnapló / Changelog](CHANGELOG.md)

## Build és bekötés

```bash
./mvnw install    # tesztek + target/vies-client-1.2.0.jar (+ -sources.jar, -javadoc.jar)
               # és telepítés a lokális Maven-repóba
```

**Maven:**

```xml
<dependency>
    <groupId>vies.client</groupId>
    <artifactId>vies-client</artifactId>
    <version>1.2.0</version>
</dependency>
```

**Gradle:**

```kotlin
implementation("vies.client:vies-client:1.2.0")
```

**Valódi JPMS-modul.** A jar named module-ként viselkedik (`jar --describe-module`):

```
vies.client@1.2.0
exports vies.client
requires java.net.http
contains vies.client.internal   ← nem exportált, zárt belső csomag
```

Modularizált alkalmazásból így kötöd be:

```java
module my.api.server {
    requires vies.client;
}
```

Classpath-ról (nem modularizált, „hagyományos" projektben) ugyanúgy működik —
a `module-info` ilyenkor egyszerűen nem játszik. Maven/Gradle nélkül is
használható: a `src/main/java` alatti forrásfa átmásolható a saját projektbe
(a `module-info.java`-t hagyd el, ha nem modularizált a projekted).

## Gyors példa

```java
import vies.client.*;

try (var vies = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER"))) // megbízható secret/config
        .retries(1)
        .build()) {

    // Elfogad kötőjelet, szóközt, kisbetűt; a GR-t EL-re képezi.
    switch (vies.check("DE 000 000 000")) {
        case ViesResponse.Valid v ->
            System.out.println("Érvényes: " + v.traderName().orElse("(név nem publikus)")
                    + " — konzultációs szám: " + v.consultationNumber().orElse("-"));
        case ViesResponse.Invalid i ->
            System.out.println("Nem élő közösségi adószám: " + i.vatNumber());
        case ViesResponse.Unavailable u ->
            System.out.println("A VIES most nem elérhető (" + u.errorCode() + ") — később újra");
        case ViesResponse.MalformedInput m ->
            System.out.println("Hibás bemenet: " + m.reason());
    }
}
```

A `switch` kimerítő: a fordító garantálja, hogy mind a négy kimenetet lekezelted
(sealed interface + pattern matching).

## Miért fontos a `defaultRequester`?

A `defaultRequester` a szervezet saját közösségi adószáma legyen, és megbízható
secretből vagy konfigurációból érkezzen. A VIES érvényes válasznál **adhat**
`consultationNumber` értéket, de ez opcionális és soha nem garantált. Az azonosító
jogi bizonyító ereje a helyi szabályoktól függ; őrizd meg a lekérdezés dátumával és
bemenetével együtt.

## Bekötés Spring Boot API-szerverbe

```java
@Configuration
class ViesConfig {
    @Bean(destroyMethod = "close")
    ViesClient viesClient() {
        return ViesClient.builder()
                .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
                .retries(1)
                .build();
    }
}

@RestController
@RequestMapping("/api/vat")
class VatController {
    private final ViesClient vies;

    VatController(ViesClient vies) { this.vies = vies; }

    @GetMapping("/{number}")
    ResponseEntity<?> check(@PathVariable String number) {
        return switch (vies.check(number)) {
            case ViesResponse.Valid v -> ResponseEntity.ok(Map.of(
                    "valid", true,
                    "name", v.traderName().orElse(""),
                    "address", v.traderAddress().orElse(""),
                    "consultationNumber", v.consultationNumber().orElse("")));
            case ViesResponse.Invalid i -> ResponseEntity.ok(Map.of("valid", false));
            case ViesResponse.Unavailable u -> {
                var error = u.error().orElseThrow();
                yield ResponseEntity.status("CLIENT_OVERLOADED".equals(error.code()) ? 429 : 503)
                        .body(Map.of("errorCode", error.code(), "retryable", error.retryable(),
                                "messageHu", error.messageHu(), "messageEn", error.messageEn()));
            }
            case ViesResponse.MalformedInput m -> {
                var error = m.error().orElseThrow();
                yield ResponseEntity.badRequest().body(Map.of(
                        "errorCode", error.code(), "retryable", error.retryable(),
                        "messageHu", error.messageHu(), "messageEn", error.messageEn()));
            }
        };
    }
}
```

A kliens immutábilis és szálbiztos — egyetlen példányt hozz létre az alkalmazás
életciklusára (singleton bean), és zárd le leállításkor (`close`).
Keret nélküli minta: [`examples/ViesDemoServer.java`](../../../examples/ViesDemoServer.java) (sima JDK`HttpServer`, virtuális szálakon):

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java     # port 8085
curl "http://localhost:8085/vat-check?number=HU00000000"
```

## Aszinkron használat

```java
CompletableFuture<ViesResponse> future = vies.checkAsync("PL0000000000");
```

Az async hívások alapból **virtuális szálakon** futnak (Project Loom) — sok
párhuzamos ellenőrzésnél sincs platform-szál-pazarlás. Saját executor a
builderben adható meg (`executor(...)`— azt a kliens nem zárja le).
Az async kliens alapból legfeljebb 512 **egyedi async leader műveletet**
fogad klienspéldányonként. Egy egyedi cache-olvasás rövid ideig helyet fogyaszt;
a hibás bemenet és azonos single-flight követő nem fogyaszt külön helyet. Túlterheléskor az új egyedi kérés azonnal `Unavailable(..., "CLIENT_OVERLOADED")` eredményt kap. Ez a kliensen belüli
munkát korlátozza; a bemeneti sort és a hívó által megőrzött future-öket szintén
korlátozni kell.

## Több millió lekérdezéses üzem

Milliós felhasználószámhoz ne egy JVM-ben indíts el milliónyi future-t, és ne
engedj korlátlan közvetlen forgalmat a VIES felé. A javasolt felépítés:

> A kliens milliós munkasor feldolgozó komponense lehet, de a tényleges
> VIES-áteresztést az EU és a tagállami rendszerek változó, nem garantált korlátai
> határozzák meg. A virtuális szálak a várakozás költségét csökkentik; az upstream
> kapacitását nem növelik.

1. A beérkező ellenőrzéseket tartós, partícionált üzenetsor fogadja.
2. Több horizontálisan skálázott worker fogyasztja a sort korlátos adagokban.
3. Workerenként egy singleton `ViesClient` működik virtuális szálakkal.
4. A workerek közös Redis-cache-t használnak a `ViesCache` interfészen keresztül.
5. Globális/disztribútált rate limiter védi az EU és tagállami VIES végpontokat.
   A kliens egy JVM-en belül összevonja az azonos, egy időben érkező
   adószám/lekérdező párokat (single-flight), ezért egy cache miss nem okoz
   „request stampede”-et. A `maxConcurrentRequests` korlátozza a valódi hálózati
   kéréseket, a `maxPendingAsyncRequests` és`maxPendingSyncRequests` pedig memóriában
   ad azonnali backpressure-t. Mindegyik JVM-lokális. Több worker összesített forgalmát
   kötelező közös, disztribútált limiterrel szabályozni. A tartós bemeneti queue-t és a
   consumer poolt továbbra is az alkalmazásnak kell korlátoznia.
   Nagyterhelésű worker példa:

```java
var vies = ViesClient.builder()
        .cache(redisViesCache)
        .cacheTtl(Duration.ofHours(24))
        .maxConcurrentRequests(32)
        .maxPendingSyncRequests(512)
        .maxPendingAsyncRequests(512)
        .admissionTimeout(Duration.ofSeconds(2))
        .retries(2)
        .retryDelay(Duration.ofMillis(250))
        .build();
```

Az `Unavailable` eredményt — beleértve a`CLIENT_OVERLOADED` értéket — a tartós
sorban késleltetve kell újrapróbálni. A `MalformedInput` és`Invalid` nem retry-zandó.

## Egy kérés útja / Request lifecycle

1. **Normalize / Normalizálás:** országkód és formátum ellenőrzése hálózat nélkül.
2. **Cache:** érvényes, még élő találat azonnali visszaadása.
3. **Single-flight:** azonos adószám+lekérdező párok egy JVM-en belül egyesülnek.
4. **Admission:** az async és hálózati limitek megvédik a memóriát és a VIES-t.
5. **HTTP:** újrahasznált JDK `HttpClient` kapcsolat, rövid timeouttal.
6. **Validate / Válaszellenőrzés:** hiányos boolean vagy dátum →`MALFORMED_RESPONSE`.
7. **Cache write:** csak a hiteles `Valid` eredmény kerül cache-be.

## Kétnyelvű hibaválaszok / Bilingual error responses

A gépi hibakód mindig stabil és nyelvfüggetlen. Az `error()` magyar és angol
felhasználói szöveget, valamint retry-döntést ad:

```java
var response = vies.check("HU00000000");
response.error().ifPresent(error -> {
    log.warn("{} | {} | retry={}", error.messageHu(), error.messageEn(), error.retryable());
});
```

| Eredmény / Result |         HTTP |             Retry |    Cache | Jelentés / Meaning                                            |
| ----------------- | -----------: | ----------------: | -------: | ------------------------------------------------------------- |
| `Valid`           |          200 |            nem/no | igen/yes | A VIES érvényesként igazolta / VIES confirmed valid           |
| `Invalid`         |          200 |            nem/no |   nem/no | A VIES nem igazolta érvényesként / VIES did not confirm valid |
| `Unavailable`     | 503 vagy 429 | többnyire/usually |   nem/no | Nem született döntés / No validity decision was made          |
| `MalformedInput`  |          400 |            nem/no |   nem/no | A bemenetet javítani kell / Input must be corrected           |

## Konfiguráció (builder)

| Beállítás                         | Alapérték               | Mit csinál                                                                       |
| --------------------------------- | ----------------------- | -------------------------------------------------------------------------------- |
| `baseUrl(String)`                 | hivatalos VIES REST URL | Átirányítás mockra tesztben                                                      |
| `connectTimeout(Duration)`        | 5 s                     | TCP/TLS kapcsolódási időkorlát                                                   |
| `requestTimeout(Duration)`        | 8 s                     | Teljes kérés-időkorlát (a VIES interaktív, rövid legyen)                         |
| `admissionTimeout(Duration)`      | 2 s                     | Ennyi ideig vár szabad hálózati helyre                                           |
| `defaultRequester(ViesRequester)` | nincs                   | Saját közösségi adószám → konzultációs azonosító                                 |
| `retries(int)`                    | 0                       | Automatikus újrapróbálás átmeneti hibáknál (0–5, exponenciális backoff + jitter) |
| `retryDelay(Duration)`            | 400 ms                  | Exponenciális backoff alapértéke                                                 |
| `maxConcurrentRequests(int)`      | 32                      | Egyidejű valódi VIES hálózati kérések felső korlátja                             |
| `maxPendingSyncRequests(int)`     | 512                     | Egyidejű sync hívók memóriakorlátja; felette `CLIENT_OVERLOADED`                 |
| `maxPendingAsyncRequests(int)`    | 512                     | Aktív/várakozó async műveletek memóriakorlátja; felette `CLIENT_OVERLOADED`      |
| `cacheTtl(Duration)`              | 24 óra                  | Érvényes találatok cache-ideje                                                   |
| `cacheMaxEntries(int)`            | 10 000                  | Beépített memória-cache mérethatára                                              |
| `cache(ViesCache)`                | beépített               | Saját cache-backend (pl. Redis)                                                  |
| `disableCache()`                  | —                       | Nincs tárolt cache; azonos párhuzamos hívások egy single-flight kérést megoszthatnak |
| `userAgent(String)`               | modul-azonosító         | Illendő magadat azonosítani az EU felé                                           |
| `executor(ExecutorService)`       | virtuális szál/task     | Saját async executor; életciklusáért a hívó felel                                |

## Saját cache (pl. Redis)

```java
class RedisViesCache implements ViesCache {
    public Optional<ViesResponse.Valid> get(String key) { /* ... */ }
    public void put(String key, ViesResponse.Valid value, Duration ttl) { /* ... */ }
}

var vies = ViesClient.builder().cache(new RedisViesCache()).build();
```

Csak `Valid` eredmény kerül cache-be — érvénytelen szám és átmeneti hiba soha.
Redis-adapterhez használj rövid cache-timeoutot, verziózott namespace-t és saját
hibametrikát. Cache-olvasási hiba `CACHE_ERROR` eredményt ad, hogy Redis-kieséskor
ne induljon kontrollálatlan VIES request stampede.
A cache-ből visszaadott `consultationNumber` és `requestDate` az **eredeti**
ellenőrzéshez tartozik; a cache hit nem új VIES-ellenőrzés. A `disableCache()` csak
a tárolt cache-t kapcsolja ki: azonos VAT+requester párhuzamos hívásai egyetlen
single-flight hálózati kérést megoszthatnak, a befejezése után indított későbbi hívás
viszont új kérést küld. A `consultationNumber` ettől még opcionális marad.

## Szemantika, amit érdemes tudni

1. **`Unavailable`≠`Invalid`.** A tagállami háttérrendszerek rendszeresen
   kiesnek (`MS_UNAVAILABLE`,`MS_MAX_CONCURRENT_REQ`...). Ilyenkor a számot a
   VIES **nem bírálta el** — üzletileg tilos érvénytelennek tekinteni.
2. **A formátum-előszűrés nem VIES-hitelesítés.** A modul kiszűri a nyilván
   rossz bemenetet (`MalformedInput`), mielőtt hálózatra menne, de az érvényesség
   forrása mindig a VIES válasza.
3. **A retry terhelést is növel.** Átmeneti hibát helyben csak kevésszer próbálj
   újra; nagyüzemben a tartós sor késleltetett retry mechanizmusa az elsődleges.
4. **Görögország `EL`, Észak-Írország`XI`.** A modul mindkettőt kezeli, a`GR`bemenetet automatikusan`EL`-re képezi.

## Tesztek

```bash
./mvnw test    # unit + helyi HTTP konkurencia/retry/backpressure/lifecycle tesztek
```

---

## Angol rövid útmutató

Zéró függőségi Java 21+ kliens az EU Bizottság VIES ÁFA-számához
érvényesítési REST API. Építsd a `./mvnw package`-vel, majd:

```java
try (var vies = ViesClient.createDefault()) {
    if (vies.check("DE000000000") instanceof ViesResponse.Valid v) {
        System.out.println(v.traderName().orElse("?"));
    }
}
```

Az eredmények egy lezárt hierarchia (`Valid`/`Invalid`/`Unavailable`/`MalformedInput`) a`switch` kimerítő mintaillesztéshez tervezték. Érvényes
az eredmények 24 órán keresztül a memóriában tárolódnak; a tranziens VIES-kimaradások jelentése:`Unavailable`, soha nem mint`Invalid`. Hivatalos API dokumentáció:
<https://ec.europa.eu/taxation_customs/vies/#/technical-information>.

## Nagy léptékű működés

Ez a kliens lehet egy része egy millió tételes feldolgozási folyamatnak, de
A tényleges VIES átviteli sebességet változó, nem garantált EU és tagállam határolja
határait. A virtuális szálak csökkentik a blokkolási költségeket; felfelé nem növekednek
kapacitás. Használjon tartós particionált sort, korlátozott fogyasztókat, megosztott Redist
gyorsítótárat és egy elosztott sebességkorlátozót az összes dolgozó számára. Helyi egyjáratú és
A szemaforok csak egy JVM-et védenek. Soha ne kezelje a `Unavailable`-t`Invalid`-ként; használja`response.error()` stabil kódokhoz, újrapróbálhatósághoz és magyar/angol üzenetekhez.

## Nyílt forráskód / Open source

A projekt az [Apache License 2.0](../../../LICENSE) alatt használható, módosítható és
terjeszthető. A licenc megengedő kereskedelmi felhasználást és kifejezett
szabadalmi engedélyt ad; a licenc-, attribution- és módosítási feltételeket meg kell
őrizni. Közreműködés előtt olvasd el a [CONTRIBUTING.md](CONTRIBUTING.md) és
[CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) fájlokat.
Ez a projekt az [Apache License 2.0](../../../LICENSE) licenc alatt van, amely megengedő
engedély kifejezett szabadalommal. Lásd: [CONTRIBUTING.md](CONTRIBUTING.md),
[SECURITY.md](SECURITY.md), valamint a részletes [nyílt forráskódú megjegyzések](OPEN_SOURCE.md).

## Támogatás és adomány / Support and donations

A közösségi segítség best-effort alapon, GitHub issue-kon és discussionökön történik.
A biztonsági hibákat mindig privát csatornán jelentsd. A projekt fenntartása jelentős
fejlesztési és infrastruktúraköltséggel jár; a GitHub Donate/Sponsor gomb a maintainer
ellenőrzött támogatási URL-jének beállítása után jelenik meg.
A közösségi támogatás a legjobb erőfeszítés a GitHub-problémák és megbeszélések révén. Biztonság
a jelentéseknek privátnak kell maradniuk. Az Adományozás/Szponzorálás gomb ezután engedélyezve lesz
a fenntartó ellenőrzött finanszírozási URL-je be van állítva.
