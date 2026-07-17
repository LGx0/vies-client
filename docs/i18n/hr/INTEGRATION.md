# Hrvatski (hr) — Integracija

`disableCache()` isključuje spremljenu/trajnu predmemoriju, ali istodobni pozivi s istim parom PDV broj + requester mogu dijeliti jedan single-flight mrežni zahtjev. Kasniji poziv nakon njegova završetka izvršava novi VIES zahtjev. `consultationNumber` je neobavezan i VIES ga može vratiti, ali nikada nije zajamčen; njegova pravna dokazna vrijednost ovisi o lokalnim pravilima. Učitavajte `MY_EU_VAT_NUMBER` samo iz pouzdanog izvora tajni/konfiguracije.

> [Svi jezici](../../LANGUAGES.md) · Informativni prijevod. U slučaju razlike mjerodavan je kanonski engleski tehnički ili pravni izvor. Samo su korijenski `LICENSE` i `NOTICE` pravno mjerodavni; prijevod ih ne zamjenjuje.

## 1. Životni ciklus

Koristite jedan objekt `ViesClient` u aplikaciji ili radnoj instanci.
Ne stvarajte klijenta po HTTP zahtjevu: izgubili biste skup veza, predmemoriju,
jednokratno spajanje i lokalna ograničenja.

Koristite jedan `ViesClient` po aplikaciji/radnom procesu. Ne stvarajte klijenta po
HTTP zahtjev; na taj se način odbacuje skupljanje veza, predmemorija, jednokratni let i lokalno
granice.

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

Nova operacija se ne može pokrenuti nakon `close()`. Isključivanje prekida aktivni interni
operacije i vraća `CLIENT_CLOSED` na uobičajene zahtjeve u procesu.
Metode sync i async koje se pozivaju neposredno nakon toga su sinkrone
Bačena je iznimka `IllegalStateException`.

Nakon `close()`, novi rad se ne prihvaća. Isključivanje prekida aktivni interni rad
i ispunjava zajedničke zahtjeve u letu sa `CLIENT_CLOSED`.
Novi sinkronizirani i asinkroni API pozivi napravljeni nakon toga bacaju `IllegalStateException`
sinkrono.

## 2. Sinkroni API

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

Broj sinkronih API pozivatelja također je ograničen (`maxPendingSyncRequests`). Na granici
iznad važećeg zahtjeva odmah dobiva rezultat `CLIENT_OVERLOADED`.

Sinkroni pozivatelji također su ograničeni pristupom `maxPendingSyncRequests`.

## 3. Asinkroni API

```java
client.checkAsync("PL0000000000")
        .thenAccept(response -> {
            // Handle all four response variants / Kezeld mind a négy eredményt.
        });
```

Async API radi na virtualnoj niti sa zadanim izvršiteljem; vlastiti izvršitelj
primjenjuju se njegova pravila rasporeda. Identičan porezni broj + zahtjevi za upit su jedno
povezuju se s unutarnjom zajedničkom budućnošću. Pozivatelj dobiva kopiju zaštićenu od poništavanja:
otkazivanje jednog potrošača ne prekida zajednički zahtjev ostalih.

Uz zadani izvršitelj, async API radi na virtualnim nitima; prilagođeni izvršitelj
koristi vlastitu politiku rasporeda. Identični PDV/pozivi podnositelja zahtjeva spajaju se u jedan interni
zajednička budućnost. Svaki pozivatelj dobiva kopiju sigurnu za otkazivanje.

Ne spremajte milijune budućnosti u memoriju. Potrošači trajnog reda čekanja uvijek su ograničeni
držati prozor aktivnim.

Ne zadržavajte milijune budućnosti. Potrošač trajnog reda čekanja mora zadržati ograničenje
prozor obrade.

## 4. HTTP API ugovor

Preporučeno mapiranje:

| ViesResponse                     | HTTP |           Pokušaj ponovno | Napomena / Napomena               |
| -------------------------------- | ---: | ------------------------: | --------------------------------- |
| `Valid`                          |  200 |                        ne | rezultat domene                   |
| `Invalid`                        |  200 |                        ne | rezultat domene, a ne greška HTTP |
| `MalformedInput`                 |  400 |                        ne | pozivatelj mora popraviti unos    |
| `Unavailable(CLIENT_OVERLOADED)` |  429 |                   odgođen | lokalni protutlak                 |
| ostalo `Unavailable`             |  503 | autor `error.retryable()` | odluka bez valjanosti             |

Primjer pogreške JSON / Primjer pogreške JSON:

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

Korisnički tekst `messageHu`/`messageEn`. Za logiku, metriku i klijentsku logiku
uvijek koristite stabilnu vrijednost `errorCode`.

`messageHu`/`messageEn`okrenuti su prema korisniku. Koristite stabilne vrijednosti`errorCode`za zapise,
metrika i logika klijenta.

## 5. Spring Boot

### Konfiguracija

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

### Upravljač

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

Knjižnica nema Spring ovisnosti; gornji kod je dio konzumne aplikacije.
Knjižnica nema Spring ovisnost; ovaj adapter pripada aplikaciji koja ga koristi.

## 6. Običan JDK HTTP poslužitelj

Potpuni izvršni primjer je `examples/ViesDemoServer.java`.
Cijeli primjer koji se može izvoditi:`examples/ViesDemoServer.java`.

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java 8085
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Primjer koristi virtualne niti, stvarne HTTP statuse i dvojezične odgovore na pogreške.
Primjer koristi virtualne niti, smislene HTTP statuse i dvojezične pogreške.

## 7. Requester i identifikator konzultacije

```java
var client = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
        .build();
```

Requester je vlastiti PDV broj organizacije i mora doći iz pouzdanog izvora tajni/konfiguracije. Za valjanu provjeru VIES može vratiti neobavezni `requestIdentifier`/`consultationNumber`, ali nije zajamčen i njegova dokazna vrijednost ovisi o lokalnim pravilima.

Pogodak predmemorije sadrži izvorni ID/datum konzultacije, a ne novu provjeru. `disableCache()` isključuje spremljenu predmemoriju, ali istodobni jednaki PDV+requester pozivi ipak mogu dijeliti jedan single-flight mrežni zahtjev; kasniji poziv nakon završetka šalje novi VIES zahtjev.

## 8. Redis cache adapter

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

Zahtjevi / Zahtjevi:

- thread-safe implementacija;
- kratko trajanje veze/naredbe;
- verzionirani prostor imena ključa;
- metrika i zdravstvena upozorenja;
- nema neograničenog internog ponovnog pokušaja;
- ispravna serijalizacija `requestDate`, opcija, i `fromCache`.

Iznimka čitanja uzrokuje rezultat `CACHE_ERROR` i ne pokreće povratni VIES.
Iznimka pisanja ne briše već primljeni autentični `Valid` rezultat.

Iznimka čitanja vraća `CACHE_ERROR` bez rezervnog VIES-a. Iznimka za pisanje
ne briše mjerodavni rezultat `Valid`.

## 9. Pokušaj ponovo, red čekanja i DLQ

U visokoj obradi:

1. ustrajati na poslu prije obrade;
2. koristiti ključ idempotencije;
3. pozvati VIES s malim brojem lokalnih ponovnih pokušaja;
4. ako je `error.retryable()`, raspored odgođenog ponovnog pokušaja s eksponencijalnom odgodom;
5. zaustaviti se nakon konfiguriranog maksimuma i prijeći na DLQ/ručni pregled;
6. nikada ne pokušavajte ponovno `Invalid` ili `MalformedInput` nepromijenjeni.

Nemojte držati trajni ponovni pokušaj u memoriji s lancem `CompletableFuture`.
Nemojte implementirati dugotrajne ponovne pokušaje kao `CompletableFuture` lance u memoriji.

## 10. Više čvorova ili podova

Svaka replika ima vlastito spremište veza, predmemoriju, tablicu s jednim letom i semafor
dobiti.`maxConcurrentRequests(32)` stoga **nije globalno 32**.

Svaka replika ima vlastito spremište veza, predmemoriju, tablicu s jednim letom i
semafori.`maxConcurrentRequests(32)` stoga **nije globalno ograničenje od 32**.

Potrebne komponente u velikoj mjeri / Potrebne u velikoj mjeri:

- izdržljivi podijeljeni red čekanja;
- ograničeni broj potrošača/prozor;
- distribuirani/globalni limitator brzine;
- zajednička predmemorija gdje to poslovna semantika dopušta;
- odgođeni ponovni pokušaj i DLQ;
- idempotencija/deduplikacija;
- automatsko skaliranje na temelju starosti čekanja, a ne izravne neograničene VIES konkurentnosti.

## 11. Health i observability

- Živost ne smije ovisiti o VIES-u.
- `availability()` bi trebao biti rijetka, predmemorirana dijagnostika / Anketa i štedljivo je predmemorirati.
- Mérd a p50/p95/p99 latency-t / Izmjerite p50/p95/p99 latency.
- Brojanje prema vrsti rezultata i `errorCode`/ Brojanje prema rezultatu i šifri pogreške.
- Mjerite pogodak predmemorije, ponovni pokušaj, preopterećenje, starost čekanja i DLQ.
- Mjerenje pogodaka predmemorije, ponovnih pokušaja, preopterećenja, starosti čekanja i veličine DLQ-a.
- Maskirajte PDV/ime/adresu u zapisima.

## 12. Anti-obrasci

- klijent po zahtjevu / novi klijent po zahtjevu;
- neograničen broj budućih ili sinkroniziranih pozivatelja
- tretiranje graničnika po podunu kao globalnog / gledanje lokalnog limitera kao globalnog;
- odmah ponovni pokušaj svake pogreške
- pretvaranje `Unavailable` u `Invalid`;
- tretiranje predmemoriranih podataka konzultacija kao svježeg dokaza;
- pozivanje VIES-a uživo iz provjera živosti ili zadanih jediničnih testova.

## 13. Popis za provjeru proizvodnje

- [ ] Životni ciklus jednog elementa i gašenje su konfigurirani.
- [ ] Sve četiri `ViesResponse` varijante se eksplicitno obrađuju.
- [ ] API vraća stabilni kod plus HU/EN poruke.
- [ ] Ograničenja sinkronizacije, asinkronizacije, ulaza i mreže su ograničena.
- [ ] Promet s više čvorova koristi globalni/distribuirani limiter.
- [ ] Svježina predmemorije i politika provjere konzultacija su odobreni.
- [ ] Postoje odgođeni ponovni pokušaji, maksimalni pokušaji, idempotencija i DLQ.
- [ ] Postoje metrike, upozorenja i maskirani zapisnici.
- [ ] Liveness je neovisan o VIES-u.
- [ ] Definirani su testovi jedinice, lokalne integracije, konkurentnosti, opterećenja, zadržavanja i kvara.
