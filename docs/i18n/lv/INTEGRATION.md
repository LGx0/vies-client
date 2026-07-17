# Latviešu (lv) — Integration

> [Valodu izvēle](../../LANGUAGES.md) · Šī lokalizācija uzlabo pieejamību. Atšķirību gadījumā noteicošais ir kanoniskais angļu tehniskais vai juridiskais avots. Saknes `LICENSE` un`NOTICE` paliek juridiski saistoši.

## 1. Dzīves cikls / dzīves cikls

Lietojumprogrammā vai darbinieka instancē izmantojiet vienu `ViesClient` objektu.
Neveidojiet klientu vienam HTTP pieprasījumam: jūs pazaudēsit savienojumu pūlu, kešatmiņu,
viena lidojuma apvienošana un vietējie ierobežojumi.
Izmantojiet vienu `ViesClient` vienam pieteikumam/darba procesam. Neveidojiet klientu vienam
HTTP pieprasījums; to darot, tiek atmesta savienojumu kopošana, kešatmiņa, viena lidojuma un lokālais savienojums
robežas.

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

Pēc `close()` jaunu darbību nevar sākt. Izslēgšana pārtrauc aktīvo iekšējo
operācijas un atgriež `CLIENT_CLOSED` uz bieži notiekošajiem pieprasījumiem.
Sinhronizācijas un asinhronizācijas metodes, kas tiek izsauktas tieši pēc tam, ir sinhronas
Tiek izmests `IllegalStateException` izņēmums.
Pēc `close()` jauns darbs netiek pieņemts. Izslēgšana pārtrauc aktīvo iekšējo darbu
un pabeidz koplietotos pieprasījumus lidojuma laikā ar `CLIENT_CLOSED`.
Jaunie sinhronizācijas un asinhronās API izsaukumi, kas veikti pēc tam, rada `IllegalStateException` sinhroni.

## 2. Sinhronā API / Sinhronā API

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

Ierobežots ir arī sinhrono API zvanītāju skaits (`maxPendingSyncRequests`). Uz robežas
virs derīga pieprasījuma nekavējoties iegūst `CLIENT_OVERLOADED` rezultātu.
Sinhrono zvanītāju uzņemšanu ierobežo arī `maxPendingSyncRequests`.

## 3. Asinhronā API / Asinhronā API

```java
client.checkAsync("PL0000000000")
        .thenAccept(response -> {
            // Handle all four response variants / Kezeld mind a négy eredményt.
        });
```

Asinhronā API darbojas virtuālā pavedienā ar noklusējuma izpildītāju; pašu izpildītājs
tiek piemēroti tā plānošanas noteikumi. Identisks nodokļu numurs + uzziņas pieprasījumi ir viens
tie savienojas ar iekšēju kopīgu nākotni. Zvanītājs saņem atcelšanas kopiju:
viena patērētāja atcelšana nepārtrauc pārējo kopīgo pieprasījumu.
Izmantojot noklusējuma izpildītāju, asinhronā API darbojas virtuālajos pavedienos; pasūtījuma izpildītājs
izmanto savu plānošanas politiku. Identiski PVN/pieprasītāja izsaukumi tiek apvienoti vienā iekšienē
kopīga nākotne. Katrs zvanītājs saņem atcelšanai drošu kopiju.
Neglabājiet atmiņā miljoniem nākotnes līgumu. Pastāvīgās rindas patērētājs vienmēr ir ierobežots
saglabāt logu aktīvu.
Nesaglabājiet miljoniem nākotnes līgumu. Ilgstošas rindas patērētājam ir jāierobežo
apstrādes logs.

## 4. HTTP API līgums / HTTP API līgums

Ieteicamā kartēšana:
| ViesResponse | HTTP | Mēģināt vēlreiz | Piezīme / Piezīme |
|---|---:|---:|---|
|`Valid`| 200 | nē | domēna rezultāts |
|`Invalid`| 200 | nē | domēna rezultāts, nevis HTTP kļūme |
|`MalformedInput`| 400 | nē | zvanītājam ir jālabo ievade |
|`Unavailable(CLIENT_OVERLOADED)`| 429 | kavējas | vietējais pretspiediens |
| cits `Unavailable`| 503 | autors`error.retryable()`| nav derīguma lēmuma |
JSON kļūdas piemērs/ JSON kļūdas piemērs:

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

Lietotāja teksts `messageHu`/`messageEn`. Žurnāla, metrikas un klienta loģikai
vienmēr izmantojiet stabilo vērtību `errorCode`.
`messageHu`/`messageEn`ir paredzēti lietotājam. Izmantojiet stabilas`errorCode` vērtības žurnāliem,
metrika un klienta loģika.

## 5. Spring Boot

## Konfigurācija

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

## Kontrolieris

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

Bibliotēkai nav Pavasara atkarību; iepriekš minētais kods ir daļa no patērējošās lietojumprogrammas.
Bibliotēkai nav Pavasara atkarību; šis adapteris pieder patērētajai lietotnei.

## 6. Vienkāršs JDK HTTP serveris

Pilns izpildāmais piemērs ir `examples/ViesDemoServer.java`.
Pilns izpildāms piemērs:`examples/ViesDemoServer.java`.

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java 8085
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Piemērā tiek izmantoti virtuālie pavedieni, reāli HTTP statusi un bilingvālās kļūdu atbildes.
Piemērā izmantoti virtuālie pavedieni, jēgpilni HTTP statusi un bilingvālās kļūdas.

## 7. Pieprasītāja un konsultācijas ID

```java
var client = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
        .build();
```

Pieprasītājam jābūt organizācijas PVN numuram, kas `MY_EU_VAT_NUMBER` veidā saņemts
no uzticama noslēpuma vai konfigurācijas. Derīgai atbildei VIES **var** atgriezt
`requestIdentifier`/`consultationNumber`, taču tas ir neobligāts, nav garantēts un
tā juridiskais pierādījuma spēks ir atkarīgs no vietējiem noteikumiem.

Kešatmiņas trāpījumā identifikators un `requestDate` pieder sākotnējai pārbaudei.
`disableCache()` izslēdz tikai saglabāto kešatmiņu: vienādi paralēli VAT+requester
zvani var koplietot vienu single-flight tīkla pieprasījumu; vēlāks zvans pēc tā
pabeigšanas veic jaunu pieprasījumu.

## 8. Redis kešatmiņas adapteris

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

Prasības / Prasības:

- pavedienu droša ieviešana;
- īss savienojuma/komandu taimauts;
- versijas atslēgas nosaukumvieta;
- metrika un brīdinājumi par veselību;
- nav neierobežota iekšēja atkārtota mēģinājuma;
- pareiza `requestDate`, izvēles un`fromCache` serializācija.
  Lasīšanas izņēmums rada rezultātu `CACHE_ERROR` un neaktivizē VIES atkāpšanos.
  Rakstīšanas izņēmums neizdzēš jau saņemto autentisko `Valid` rezultātu.
  Lasīšanas izņēmums atgriež `CACHE_ERROR` bez VIES atkāpšanās. Rakstīšanas izņēmums
  neizdzēš autoritatīvu `Valid` rezultātu.

## 9. Retry, queue un DLQ / Retry, queue, and DLQ

Liela mēroga apstrādē:

1. turpināt darbu pirms apstrādes;
2. izmantot idempotences atslēgu;
3. zvanīt VIES ar nelielu vietējo atkārtojumu skaitu;
4. ja `error.retryable()`, ieplānojiet aizkavētu atkārtotu mēģinājumu ar eksponenciālu aizkavi;
5. apstāties pēc konfigurētā maksimuma un pāriet uz DLQ/manuālo pārskatīšanu;
6. nekad nemēģiniet vēlreiz `Invalid` vai`MalformedInput` bez izmaiņām.
   Neglabājiet atmiņā ilgstošu atkārtotu mēģinājumu, izmantojot ķēdi `CompletableFuture`.
   Neieviesiet ilgstošus atkārtojumus kā `CompletableFuture` ķēdes atmiņā.

## 10. Vairāk node/pod / Vairāki mezgli vai podi

Katrai kopijai ir savs savienojumu baseins, kešatmiņa, viena lidojuma tabula un semafors
get `maxConcurrentRequests(32)` tāpēc **nav globāls 32**.
Katrai kopijai ir savs savienojumu baseins, kešatmiņa, viena lidojuma tabula un
semafori. Tāpēc `maxConcurrentRequests(32)`**nav globāls ierobežojums 32**.
Nepieciešamās sastāvdaļas lielā mērogā / Nepieciešamas mērogā:

- izturīga sadalīta rinda;
- ierobežots patērētāju skaits/logs;
- izplatīts/globālais ātruma ierobežotājs;
- koplietota kešatmiņa, kur biznesa semantika to atļauj;
- aizkavēts atkārtots mēģinājums un DLQ;
- idempotence/dedublikācija;
- automātiskā mērogošana, pamatojoties uz rindas vecumu, nevis tiešo neierobežotu VIES vienlaicību.

## 11. Veselība un novērojamība

- Dzīvīgums nedrīkst būt atkarīgs no VIES.
- `availability()` ir jābūt retam, kešatmiņā saglabātam diagnostikas/aptaujai un kešatmiņai jābūt taupīgai.
  - Novērtējiet p50/p95/p99 latentumu / mēriet p50/p95/p99 latentumu.
- Skaitīt pēc rezultāta veida un `errorCode`/ skaitīt pēc rezultāta un kļūdas koda.
- Izmēriet kešatmiņas trāpījumu, mēģiniet vēlreiz, pārslodzi, rindas vecumu un DLQ.
- Izmēriet kešatmiņas trāpījumus, atkārtojumus, pārslodzes, rindu vecumu un DLQ lielumu.
- Maska PVN/nosaukums/adrese žurnālos.

## 12. Pretraksti / Risinājumi, no kuriem jāizvairās

- klients pēc pieprasījuma / jauns klients pēc pieprasījuma;
- neierobežots nākotnes līgums vai zvanītāju sinhronizācija
- katra poda ierobežotāja traktēšana kā globāla / lokālā ierobežotāja uztveršana kā globāla;
- nekavējoties atkārtot katru kļūdu
- `Unavailable` pārveidošana par`Invalid`;
- apstrādāt kešatmiņā saglabātos konsultāciju datus kā jaunu pierādījumu;
- tiešraides VIES izsaukšana no dzīvīguma pārbaudēm vai noklusējuma vienību testiem.

## 13. Ražošanas kontrolsaraksts

- [ ] Ir konfigurēts viens dzīves cikls un izslēgšana.
- [ ] Visi četri `ViesResponse` varianti ir īpaši apstrādāti.
- [ ] API atgriež stabilu kodu un HU/EN ziņojumus.
- [ ] Sinhronizācijas, asinhronizācijas, ieejas un tīkla ierobežojumi ir ierobežoti.
- [ ] Vairāku mezglu trafika izmanto globālo/izplatīto ierobežotāju.
- [ ] Kešatmiņas svaiguma un konsultāciju drošuma politika ir apstiprināta.
- [ ] Pastāv aizkavēts atkārtots mēģinājums, maksimālais mēģinājumu skaits, idempotence un DLQ.
- [ ] Pastāv metrika, brīdinājumi un maskēti žurnāli.
- [ ] Dzīvīgums ir neatkarīgs no VIES.
- [ ] Ir definēti vienību, lokālās integrācijas, vienlaicības, slodzes, uzsūkšanās un atteices testi.
