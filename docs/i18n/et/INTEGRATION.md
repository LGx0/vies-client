# Eesti (et) — Integration

`disableCache()` lülitab salvestatud/püsiva vahemälu välja, kuid sama KMKR numbri + requester'i paariga samaaegsed kutsed võivad jagada üht single-flight-võrgupäringut. Pärast selle lõppemist teeb hilisem kutse uue VIES-i päringu. `consultationNumber` on valikuline ja VIES võib selle tagastada, kuid see pole kunagi garanteeritud; selle õiguslik tõendusväärtus sõltub kohalikest reeglitest. Laadige `MY_EU_VAT_NUMBER` ainult usaldatud saladuste/konfiguratsiooni allikast.

> [Keelevalik](../../LANGUAGES.md) · See lokaliseering parandab ligipääsetavust. Lahknevuse korral kehtib kanooniline ingliskeelne tehniline või õiguslik allikas. Juure `LICENSE` ja`NOTICE` jäävad õiguslikult määravaks.

## 1. Elutsükkel / elutsükkel

Kasutage rakenduses või töötaja eksemplaris ühte `ViesClient`-objekti.
Ärge looge klienti HTTP-päringu alusel: kaotaksite ühenduse basseini, vahemälu,
ühe lennu ühendamine ja kohalikud piirangud.
Kasutage ühte `ViesClient` ühe rakenduse/töötaja protsessi kohta. Ärge looge klienti
HTTP päring; see loobub ühenduse kogumisest, vahemälust, ühe lennu ja kohalikust
piirid.

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

Uut toimingut ei saa alustada pärast `close()`. Väljalülitamine katkestab aktiivse sisemise
toiminguid ja tagastab `CLIENT_CLOSED` tavapärastele pooleliolevatele päringutele.
Vahetult hiljem kutsutavad sünkroonimis- ja asünkroonimismeetodid on mõlemad sünkroonsed
Tehakse erand `IllegalStateException`.
Pärast `close()` uut tööd vastu ei võeta. Väljalülitamine katkestab aktiivse sisemise töö
ja täidab jagatud pardapäringuid `CLIENT_CLOSED`-ga.
Hiljem tehtud uued sünkroonimise ja asünkroonimise API kutsed annavad mõlemad `IllegalStateException` sünkroonselt.

## 2. Sünkroonne API / sünkroonne API

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

Piiratud on ka sünkroonse API helistajate arv (`maxPendingSyncRequests`). Piiril
ülaltoodud kehtiv taotlus saab kohe tulemuse `CLIENT_OVERLOADED`.
Sünkroonhelistajad on samuti lubatud `maxPendingSyncRequests`-ga.

## 3. Asünkroonne API / asünkroonne API

```java
client.checkAsync("PL0000000000")
        .thenAccept(response -> {
            // Handle all four response variants / Kezeld mind a négy eredményt.
        });
```

Async API töötab vaiketäitjaga virtuaalsel lõimel; oma testamenditäitja
kehtivad selle ajakava reeglid. Identne maksunumber + päringupäringud on üks
nad ühendavad sisemise jagatud tulevikuga. Helistaja saab tühistamiskindla koopia:
ühe tarbija taganemine ei katkesta teiste ühist taotlust.
Vaiketäitjaga töötab asünkroonimise API virtuaalsetel lõimedel; kohandatud testamenditäitja
kasutab oma ajakava poliitikat. Identsed käibemaksu/taotleja kõned ühinevad üheks sisemiseks
jagatud tulevik. Iga helistaja saab tühistamiskindla koopia.
Ärge salvestage mällu miljoneid futuure. Püsiva järjekorra tarbija on alati piiratud
hoia aken aktiivsena.
Ärge hoidke miljoneid futuure. Kestva järjekorra tarbija peab hoidma piiri
töötlemise aken.

## 4. HTTP API leping / HTTP API leping

Soovitatav kaardistus:
| ViesResponse | HTTP | Proovi uuesti | Märkus / Märkus |
|---|---:|---:|---|
|`Valid`| 200 | ei | domeeni tulemus |
|`Invalid`| 200 | ei | domeeni tulemus, mitte HTTP tõrge |
|`MalformedInput`| 400 | ei | helistaja peab parandama sisendi |
|`Unavailable(CLIENT_OVERLOADED)`| 429 | hilinenud | lokaalne vasturõhk |
| muu `Unavailable`| 503 | autor`error.retryable()`| ei kehti otsust |
JSON-i näidisvea / JSON-i vea näide:

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

Kasutajatekst `messageHu`/`messageEn`. Logi, mõõdiku ja kliendiloogika jaoks
kasutage alati stabiilset väärtust `errorCode`.
`messageHu`/`messageEn`on kasutajale suunatud. Kasutage logide jaoks stabiilseid`errorCode` väärtusi,
mõõdikud ja kliendiloogika.

## 5. Spring Boot

## Konfiguratsioon

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

## Kontroller

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

Raamatukogul pole kevadisi sõltuvusi; ülaltoodud kood on osa tarbivast rakendusest.
Raamatukogul pole kevadisi sõltuvusi; see adapter kuulub tarbivale rakendusele.

## 6. Tavaline JDK HTTP-server

Täielik käivitatav näide on `examples/ViesDemoServer.java`.
Täielikult käivitatav näide:`examples/ViesDemoServer.java`.

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java 8085
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Näites kasutatakse virtuaalseid lõime, tegelikke HTTP-olekuid ja kakskeelseid veavastuseid.
Näites kasutatakse virtuaalseid lõime, tähendusrikkaid HTTP-olekuid ja kakskeelseid vigu.

## 7. Taotleja ja konsultatsiooni ID

```java
var client = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
        .build();
```

Requester on organisatsiooni enda KMKR number ja peab tulema usaldatud saladuste/konfiguratsiooni allikast. Kehtiva kontrolli korral võib VIES tagastada valikulise `requestIdentifier`/`consultationNumber`-i, kuid seda ei garanteerita ja selle tõendusväärtus sõltub kohalikest reeglitest.

Vahemälu tabamus sisaldab algse konsultatsiooni ID-d/kuupäeva, mitte uut kontrolli. `disableCache()` lülitab salvestatud vahemälu välja, kuid samaaegsed identsed KMKR+requester-kutsed võivad siiski jagada üht single-flight-võrgupäringut; pärast lõpetamist teeb hilisem kutse uue VIES-i päringu.

## 8. Redis vahemälu adapter

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

Nõuded / nõuded:

- keermekindel rakendamine;
- lühike ühenduse/käsu ajalõpp;
- versiooniga võtme nimeruum;
- mõõdikud ja tervisehoiatused;
- puudub piiramatu sisemine korduskatse;
- `requestDate`, valikuliste ja`fromCache` õige serialiseerimine.
  Lugemise erand põhjustab tulemuse `CACHE_ERROR` ega käivita VIES-i varundamist.
  Kirjutamise erand ei kustuta juba vastuvõetud autentset `Valid` tulemust.
  Lugemise erand tagastab `CACHE_ERROR` ilma VIES-i tagavarata. Kirjutamise erand
  ei kustuta autoriteetset `Valid` tulemust.

## 9. Proovi uuesti, järjekorda ja DLQ / Proovi uuesti, järjekorda ja DLQ

Suuremahulise töötlemise korral:

1. jätkake tööd enne töötlemist;
2. kasutada idempotentsusvõtit;
3. helistage VIES-ile väikese kohaliku korduskatsete arvuga;
4. kui `error.retryable()`, ajastage hilinenud korduskatse eksponentsiaalse viivitusega;
5. peatada pärast seadistatud maksimumi ja liikuda DLQ/käsitsi ülevaatuse juurde;
6. Ärge kunagi proovige `Invalid` või`MalformedInput` muutmata kujul uuesti.
   Ärge hoidke püsivat uuesti proovimist ketiga `CompletableFuture`.
   Ärge rakendage püsivaid korduskatseid mälusiseste `CompletableFuture` kettidena.

## 10. Több node/pod / Mitu sõlme või kauna

Igal koopial on oma ühenduse bassein, vahemälu, ühe lennu tabel ja semafor
hankige `maxConcurrentRequests(32)` on seega **ei globaalne 32**.
Igal koopial on oma ühenduse bassein, vahemälu, ühe lennu tabel ja
semaforid. Seetõttu ei ole `maxConcurrentRequests(32)`**ülemaailmne piirang 32**.
Nõutavad komponendid suures mahus / Nõutavad mastaabis:

- vastupidav jaotatud järjekord;
- piiratud tarbijate arv/aken;
- hajutatud/globaalse kiiruse piiraja;
- jagatud vahemälu, kus ärisemantika seda võimaldab;
- viivitatud uuesti proovimine ja DLQ;
- idempotentsus/dedublikatsioon;
- automaatne skaleerimine järjekorra vanuse alusel, mitte otsene piiramatu VIES-i samaaegsus.

## 11. Tervis ja jälgitavus

- Elavdus ei tohi sõltuda VIES-ist.
- `availability()` peaks olema haruldane, vahemällu salvestatud diagnostika / küsitlus ja salvestage seda säästlikult.
- Mõõtke p50/p95/p99 latentsust / Mõõtke p50/p95/p99 latentsust.
- Loendage tulemuse tüübi järgi ja `errorCode`/ loendage tulemuse ja veakoodi järgi.
- Mõõtke vahemälu tabamust, uuesti proovimist, ülekoormust, järjekorra vanust ja DLQ-d.
- Mõõtke vahemälu tabamust, korduskatseid, ülekoormusi, järjekorra vanust ja DLQ suurust.
- Maski logides KM/nimi/aadress.

## 12. Antimustrid / Lahendused, mida tuleb vältida

- klient päringu kohta / uus klient päringu kohta;
- piiramatud futuurid või helistajate sünkroonimine
- per-pod piiraja käsitlemine globaalsena / lokaalse piiraja nägemine globaalsena;
- iga vea viivitamatu proovimine
- `Unavailable` teisendamine`Invalid`-ks;
- vahemällu salvestatud konsultatsiooniandmete käsitlemine värske tõendina;
- reaalajas VIES-i kutsumine elulisuse kontrollidest või üksuse vaiketestidest.

## 13. Tootmise kontrollnimekiri

- [ ] Üksik elutsükkel ja seiskamine on konfigureeritud.
- [ ] Kõiki nelja `ViesResponse` varianti käsitletakse selgesõnaliselt.
- [ ] API tagastab stabiilse koodi ja HU/EN sõnumid.
- [ ] Sünkroonimise, asünkroonimise, sissepääsu ja võrgu piirangud on piiratud.
- [ ] Mitme sõlmega liiklus kasutab globaalset/hajutatud piirajat.
- [ ] Vahemälu värskuse ja konsultatsioonikindluse eeskirjad on heaks kiidetud.
- [ ] On hilinenud uuesti proovimine, maksimaalsed katsed, idempotentsus ja DLQ.
- [ ] Mõõdikud, hoiatused ja maskeeritud logid on olemas.
- [ ] Elavdus on VIES-ist sõltumatu.
- [ ] Määratakse ühiku-, kohaliku integratsiooni-, samaaegsuse-, koormus-, leotamis- ja tõrketestid.
