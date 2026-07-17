# Magyar (hu) — Integration

> [Nyelvválasztó](../../LANGUAGES.md) · Ez a lokalizáció az elérhetőséget szolgálja. Eltérés esetén a kanonikus angol technikai vagy jogi forrás az irányadó. A gyökér `LICENSE` és`NOTICE` jogilag irányadó, fordítás nem helyettesíti.

## 1. Életciklus / Lifecycle

Egy alkalmazás- vagy worker-példányban egyetlen `ViesClient` objektumot használj.
Ne hozz létre klienst HTTP-kérésenként: elveszítenéd a connection poolt, cache-t,
single-flight összevonást és a lokális limiteket.
Alkalmazásonként/munkafolyamatonként egy `ViesClient`-t használjon. Ne hozzon létre egy ügyfelet
HTTP kérés; ezzel elveti a kapcsolatkészletezést, a gyorsítótárat, az egyjáratú és a helyi szolgáltatást
határait.

```java
var client = ViesClient.builder()
        .connectTimeout(Duration.ofSeconds(5))
        .requestTimeout(Duration.ofSeconds(8))
        .admissionTimeout(Duration.ofSeconds(2))
        .maxConcurrentRequests(32)
        .maxPendingSyncRequests(512)
        .maxPendingAsyncRequests(512)
        .retries(1)
        .build();

// Application shutdown / Alkalmazásleállítás
client.close();
```

`close()` után új művelet nem indítható. A leállítás megszakítja az aktív belső
műveleteket, és `CLIENT_CLOSED` eredményt ad a közös folyamatban lévő kéréseknek.
Az ezután közvetlenül meghívott sync és async metódusok egyaránt szinkron `IllegalStateException` kivételt dobnak.
A `close()` után új munkát nem fogadunk el. A leállítás megszakítja az aktív belső munkát
és teljesíti a megosztott repülés közbeni kéréseket a `CLIENT_CLOSED`-vel.
Az ezt követően végrehajtott új szinkronizálási és aszinkron API-hívások egyaránt a `IllegalStateException`-t dobják
szinkronban.

## 2. Szinkron API / Synchronous API

```java
ViesResponse response = client.check("DE 000 000 000");

switch (response) {
    case ViesResponse.Valid valid ->
        System.out.println("VALID: " + valid.vatNumber());
    case ViesResponse.Invalid invalid ->
        System.out.println("INVALID: " + invalid.vatNumber());
    case ViesResponse.Unavailable unavailable -> {
        var error = unavailable.error().orElseThrow();
        System.out.println(error.messageHu());
        System.out.println(error.messageEn());
    }
    case ViesResponse.MalformedInput malformed -> {
        var error = malformed.error().orElseThrow();
        System.out.println(error.messageHu());
        System.out.println(error.messageEn());
    }
}
```

A szinkron API hívóinak száma is korlátos (`maxPendingSyncRequests`). A limiten
felüli érvényes kérés azonnal `CLIENT_OVERLOADED` eredményt kap.
A szinkron hívókat a `maxPendingSyncRequests` is korlátozza.

## 3. Aszinkron API / Asynchronous API

```java
client.checkAsync("PL0000000000")
        .thenAccept(response -> {
            // Handle all four response variants / Kezeld mind a négy eredményt.
        });
```

Az async API az alapértelmezett executorral virtuális szálon fut; saját executor
esetén annak ütemezési szabályai érvényesek. Azonos adószám+lekérdező kérések egy
belső shared future-höz csatlakoznak. A hívó egy cancel-biztos másolatot kap:
egyetlen fogyasztó cancel-je nem szakítja meg a többiek közös kérését.
Az alapértelmezett végrehajtóval az aszinkron API virtuális szálakon fut; egyedi végrehajtó
saját ütemezési szabályzatát használja. Azonos áfa-/kérelmező hívások egyesülnek egy belső
közös jövő. Minden hívó kap egy törlésbiztos másolatot.
Ne gyűjts milliónyi future-t memóriában. A tartós queue consumer mindig korlátos
ablakot tartson aktívan.
Ne tartson meg milliós határidős ügyleteket. A tartós sorban álló fogyasztónak be kell tartania a határt
feldolgozási ablak.

## 4. HTTP API szerződés / HTTP API contract

Ajánlott leképezés / Recommended mapping:
| ViesResponse | HTTP | Próbálja újra | Megjegyzés / Megjegyzés |
|---|---:|---:|---|
|`Valid`| 200 | nem | domain eredmény |
|`Invalid`| 200 | nem | domain eredmény, nem HTTP hiba |
|`MalformedInput`| 400 | nem | a hívónak ki kell javítania a | bemenetet
|`Unavailable(CLIENT_OVERLOADED)`| 429 | késik | helyi ellennyomás |
| egyéb `Unavailable`| 503 | írta:`error.retryable()`| nincs érvényességi határozat |
Példa hiba JSON / Example error JSON:

```json
{
  "status": "UNAVAILABLE",
  "vatNumber": "HU00000000",
  "errorCode": "MS_UNAVAILABLE",
  "messageHu": "A tagállami adóhatóság rendszere átmenetileg nem érhető el.",
  "messageEn": "The member state's tax system is temporarily unavailable.",
  "retryable": true
}
```

A `messageHu`/`messageEn` felhasználói szöveg. Loghoz, metrikához és klienslogikához
mindig a stabil `errorCode` értéket használd.
A `messageHu`/`messageEn`a felhasználóra néz. Használjon stabil`errorCode` értékeket a naplókhoz,
mérőszámok és kliens logika.

## 5. Spring Boot

## Konfiguráció / Configuration

```java
@Configuration
class ViesConfiguration {
    @Bean(destroyMethod = "close")
    ViesClient viesClient(ViesCache cache) {
        return ViesClient.builder()
                .cache(cache)
                .connectTimeout(Duration.ofSeconds(5))
                .requestTimeout(Duration.ofSeconds(8))
                .admissionTimeout(Duration.ofSeconds(2))
                .maxConcurrentRequests(32)
                .maxPendingSyncRequests(512)
                .maxPendingAsyncRequests(512)
                .retries(1)
                .build();
    }
}
```

## Vezérlő

```java
@RestController
@RequestMapping("/api/v1/vat")
class VatController {
    private final ViesClient vies;

    VatController(ViesClient vies) {
        this.vies = vies;
    }

    @GetMapping("/{vatNumber}")
    ResponseEntity<?> check(@PathVariable String vatNumber) {
        return switch (vies.check(vatNumber)) {
            case ViesResponse.Valid v -> ResponseEntity.ok(Map.of(
                    "status", "VALID",
                    "vatNumber", v.vatNumber(),
                    "name", v.traderName().orElse(""),
                    "address", v.traderAddress().orElse(""),
                    "requestDate", v.requestDate().toString(),
                    "fromCache", v.fromCache()));
            case ViesResponse.Invalid i -> ResponseEntity.ok(Map.of(
                    "status", "INVALID",
                    "vatNumber", i.vatNumber(),
                    "requestDate", i.requestDate().toString()));
            case ViesResponse.MalformedInput m -> problem(400, m);
            case ViesResponse.Unavailable u -> problem(
                    "CLIENT_OVERLOADED".equals(u.errorCode()) ? 429 : 503, u);
        };
    }

    private static ResponseEntity<?> problem(int status, ViesResponse response) {
        var error = response.error().orElseThrow();
        return ResponseEntity.status(status).body(Map.of(
                "errorCode", error.code(),
                "retryable", error.retryable(),
                "messageHu", error.messageHu(),
                "messageEn", error.messageEn()));
    }
}
```

A könyvtárnak nincs Spring-függősége; a fenti kód a fogyasztó alkalmazás része.
The library has no Spring dependency; this adapter belongs to the consuming app.

## 6. Sima JDK HTTP szerver

A teljes futtatható példa:`examples/ViesDemoServer.java`.
Full runnable example:`examples/ViesDemoServer.java`.

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java 8085
curl "http://localhost:8085/vat-check?number=DE000000000"
```

A példa virtuális szálakat, valós HTTP státuszokat és kétnyelvű hibaválaszokat használ.
The example uses virtual threads, meaningful HTTP statuses, and bilingual errors.

## 7. Requester és konzultációs azonosító / Requester and consultation ID

```java
var client = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
        .build();
```

A requester a szervezet saját közösségi adószáma legyen, amelyet a példa szerinti
`MY_EU_VAT_NUMBER` megbízható secretből vagy konfigurációból kap. Érvényes válasznál
a VIES **adhat** `requestIdentifier`/`consultationNumber` értéket, de ez opcionális,
soha nem garantált, és jogi bizonyító ereje a helyi szabályoktól függ.

Cache hitnél az azonosító és a `requestDate` az eredeti ellenőrzéshez tartozik.
A `disableCache()` csak a tárolt cache-t kapcsolja ki: azonos VAT+requester párhuzamos
hívások megoszthatnak egy single-flight hálózati kérést; annak befejezése után egy
későbbi hívás új kérést küld.

## 8. Redis gyorsítótáradapter

```java
final class RedisViesCache implements ViesCache {
    private final RedisClient redis; // application-specific adapter

    RedisViesCache(RedisClient redis) {
        this.redis = redis;
    }

    @Override
    public Optional<ViesResponse.Valid> get(String key) {
        // Use a short, bounded Redis timeout / Használj rövid Redis timeoutot.
        return redis.get("vies:v1:" + key).map(this::decode);
    }

    @Override
    public void put(String key, ViesResponse.Valid value, Duration ttl) {
        redis.set("vies:v1:" + key, encode(value), ttl);
    }

    // encode/decode are application-specific and must preserve every record field.
}
```

Követelmények / Requirements:

- szálbiztos megvalósítás;
- rövid kapcsolat/parancs időtúllépés;
- verziószámú kulcs névtér;
- mérőszámok és egészségügyi figyelmeztetések;
- nincs korlátlan belső újrapróbálkozás;
- a `requestDate`, az opcionális és a`fromCache` helyes sorosítása.
  Olvasási kivétel `CACHE_ERROR` eredményt okoz és nem indít VIES fallbacket.
  Írási kivétel nem törli a már megkapott hiteles `Valid` eredményt.
  Az olvasási kivétel VIES tartalék nélkül adja vissza a `CACHE_ERROR`-t. Írási kivétel
  nem törli a hiteles `Valid` eredményt.

## 9. Retry, queue és DLQ / Retry, queue, and DLQ

Nagyüzemi feldolgozásban / In high-scale processing:

1. feldolgozás előtt tartsa fenn a munkát;
2. használjon idempotencia kulcsot;
3. hívja a VIES-t kis helyi újrapróbálkozási számmal;
4. ha `error.retryable()`, ütemezze a késleltetett újrapróbálást exponenciális késleltetéssel;
5. állítsa le a beállított maximum után, és lépjen a DLQ/manuális ellenőrzésre;
6. soha ne próbálja meg újra a `Invalid` vagy a`MalformedInput` változatot.
   Ne tartsd a durable retry-t memóriában `CompletableFuture` lánccal.
   Do not implement durable retries as in-memory `CompletableFuture` chains.

## 10. Több node/pod / Multiple node or pods

Minden replika saját connection poolt, cache-t, single-flight táblát és semaphoret
kap. A `maxConcurrentRequests(32)` ezért **nem globális 32**.
Minden replikának van saját kapcsolati készlete, gyorsítótára, egyjáratú táblázata és
szemaforok. A `maxConcurrentRequests(32)` ezért **nem 32-es globális határ**.
Kötelező komponensek nagy méretnél / Required at scale:

- tartós particionált sor;
- korlátos fogyasztói szám/ablak;
- elosztott/globális sebességkorlátozó;
- megosztott gyorsítótár, ahol az üzleti szemantika ezt lehetővé teszi;
- késleltetett újrapróbálkozás és DLQ;
- idempotencia/deduplikáció;
- automatikus skálázás a sor kora alapján, nem közvetlen korlátlan VIES párhuzamosság.

## 11. Egészség és megfigyelhetőség

- A liveness ne függjön a VIES-től / Liveness must not depend on VIES.
- `availability()` ritka, cache-elt diagnosztika legyen / Poll and cache it sparingly.
- Mérd a p50/p95/p99 latency-t / Measure p50/p95/p99 latency.
- Számold eredménytípus és `errorCode` szerint / Count by result and error code.
- Mérd a cache hitet, retry-t, overloadot, queue age-et és DLQ-t.
- Measure cache hits, retries, overloads, queue age, and DLQ size.
- Adószámot/nevet/címet maszkoltan logolj / Mask VAT/name/address in logs.

## 12. Anti-patterns / Kerülendő megoldások

- client per request / kérésenként új kliens;
- unlimited futures or sync callers / korlátlan future vagy sync hívó;
- treating a per-pod limiter as global / lokális limiter globálisnak tekintése;
- retrying every error immediately / minden hiba azonnali retry-ja;
- converting `Unavailable` to`Invalid`;
- treating cached consultation data as a fresh proof;
- calling live VIES from liveness checks or default unit tests.

## 13. Production checklist / Éles üzemi ellenőrzőlista

- [ ] Egyetlen életciklus és leállítás konfigurálva.
- [ ] Mind a négy `ViesResponse` változatot kifejezetten kezelik.
- [ ] Az API stabil kódot és HU/EN üzeneteket ad vissza.
- [ ] A szinkronizálás, az aszinkron, a belépés és a hálózati korlátok korlátozottak.
- [ ] A több csomópontos forgalom globális/elosztott limitert használ.
- [ ] A gyorsítótár frissességére és a konzultációra vonatkozó szabályzat jóváhagyásra került.
- [ ] Késleltetett újrapróbálkozás, maximális próbálkozások száma, idempotencia és DLQ létezik.
- [ ] Léteznek mutatók, riasztások és maszkolt naplók.
- [ ] Az élénkség független a VIES-től.
- [ ] Meg van határozva az egység, a helyi integráció, a párhuzamosság, a terhelés, az átitatás és a hibateszt.
