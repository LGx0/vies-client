# Lietuvių (lt) — Integration

> [Kalbų pasirinkimas](../../LANGUAGES.md) · Ši lokalizacija skirta prieinamumui. Esant neatitikimui, pirmenybę turi kanoninis angliškas techninis ar teisinis šaltinis. Šakniniai `LICENSE` ir`NOTICE` lieka teisiškai privalomi.

## 1. Gyvenimo ciklas / Gyvenimo ciklas

Programoje arba darbuotojo egzemplioriuje naudokite vieną `ViesClient` objektą.
Nekurkite kliento pagal HTTP užklausą: prarasite ryšio telkinį, talpyklą,
vieno skrydžio sujungimas ir vietinės ribos.
Vienai programai / darbuotojo procesui naudokite vieną `ViesClient`. Nekurkite kliento pagal
HTTP užklausa; taip atmetamas ryšio telkimas, talpykla, vieno skrydžio ir vietinis
ribos.

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

Po `close()` negalima pradėti naujos operacijos. Išjungimas pertraukia aktyvų vidinį
operacijas ir grąžina `CLIENT_CLOSED` į įprastas vykdomas užklausas.
Sinchronizavimo ir asinchronizavimo metodai, iškviečiami iškart po to, yra sinchroniniai
Išmesta `IllegalStateException` išimtis.
Po `close()` nauji darbai nepriimami. Išjungimas nutraukia aktyvų vidinį darbą
ir užpildo bendras skrydžio užklausas su `CLIENT_CLOSED`.
Vėliau atlikti nauji sinchronizavimo ir asinchronizavimo API iškvietimai sukelia `IllegalStateException` sinchroniškai.

## 2. Sinchroninė API / Sinchroninė API

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

Sinchroninių API skambintojų skaičius taip pat ribotas (`maxPendingSyncRequests`). Ant ribos
virš galiojančios užklausos iš karto gauna `CLIENT_OVERLOADED` rezultatą.
Sinchroniniai skambintojai taip pat yra apriboti `maxPendingSyncRequests`.

## 3. Asinchroninė API / Asinchroninė API

```java
client.checkAsync("PL0000000000")
        .thenAccept(response -> {
            // Handle all four response variants / Kezeld mind a négy eredményt.
        });
```

Asinchroninė API veikia virtualioje gijoje su numatytuoju vykdytoju; nuosavas vykdytojas
taikomos jo planavimo taisyklės. Identiškas mokesčių numeris + užklausos užklausos yra viena
jie susijungia su vidine bendra ateitimi. Skambinantis asmuo gauna neatšaukiamą kopiją:
vieno vartotojo atsisakymas nenutraukia kitų bendro prašymo.
Naudojant numatytąjį vykdytoją, async API veikia virtualiose gijose; nestandartinis vykdytojas
naudoja savo planavimo politiką. Identiški PVM / prašytojo skambučiai sujungiami į vieną vidinį
bendra ateitis. Kiekvienas skambinantis asmuo gauna atšaukimo saugią kopiją.
Nelaikykite atmintyje milijonų ateities sandorių. Nuolatinis eilės vartotojas visada yra ribotas
išlaikyti langą aktyvų.
Neišsaugokite milijonų ateities sandorių. Ilgalaikės eilės vartotojas turi laikytis ribotos eilės
apdorojimo langas.

## 4. HTTP API sutartis / HTTP API sutartis

Rekomenduojamas žemėlapis:
| ViesResponse | HTTP | Bandyti dar kartą | Pastaba / Pastaba |
|---|---:|---:|---|
|`Valid`| 200 | ne | domeno rezultatas |
|`Invalid`| 200 | ne | domeno rezultatas, o ne HTTP klaida |
|`MalformedInput`| 400 | ne | skambinantysis turi pataisyti įvestį |
|`Unavailable(CLIENT_OVERLOADED)`| 429 | atidėtas | vietinis priešslėgis |
| kitas `Unavailable`| 503 | pateikė`error.retryable()`| nėra galiojimo sprendimo |
JSON klaidos pavyzdys / JSON klaidos pavyzdys:

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

Vartotojo tekstas `messageHu`/`messageEn`. Žurnalui, metrikai ir kliento logikai
visada naudokite stabilią reikšmę `errorCode`.
`messageHu`/`messageEn`skirti naudotojui. Žurnalams naudokite stabilias`errorCode` reikšmes,
metrika ir kliento logika.

## 5. Spring Boot

## Konfigūracija

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

## Valdiklis

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

Biblioteka neturi pavasario priklausomybių; aukščiau pateiktas kodas yra vartojančios programos dalis.
Biblioteka neturi pavasario priklausomybių; šis adapteris priklauso naudojančiai programai.

## 6. Paprastas JDK HTTP serveris

Visas vykdomasis pavyzdys yra `examples/ViesDemoServer.java`.
Visas paleidimo pavyzdys:`examples/ViesDemoServer.java`.

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java 8085
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Pavyzdyje naudojamos virtualios gijos, tikros HTTP būsenos ir dvikalbiai klaidų atsakymai.
Pavyzdyje naudojamos virtualios gijos, prasmingos HTTP būsenos ir dvikalbės klaidos.

## 7. Užklausos ir konsultacijos ID

```java
var client = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
        .build();
```

Prašymo teikėjas turi būti organizacijos PVM mokėtojo kodas, gaunamas per
`MY_EU_VAT_NUMBER` iš patikimos paslapties arba konfigūracijos. Gavus galiojantį
atsakymą VIES **gali** grąžinti `requestIdentifier`/`consultationNumber`, tačiau jis
neprivalomas, negarantuojamas, o jo teisinė įrodomoji vertė priklauso nuo vietinių taisyklių.

Talpyklos įvykio atveju identifikatorius ir `requestDate` priklauso pradiniam patikrinimui.
`disableCache()` išjungia tik saugomą talpyklą: vienodos lygiagrečios VAT+requester
užklausos gali dalytis vienu single-flight tinklo kvietimu; vėlesnė užklausa po jo
užbaigimo siunčia naują kvietimą.

## 8. Redis talpyklos adapteris

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

Reikalavimai / Reikalavimai:

- saugus diegimas nuo siūlų;
- trumpas ryšio/komandos laikas;
- versijų rakto vardų erdvė;
- metrikos ir sveikatos įspėjimai;
- nėra neriboto vidinio pakartotinio bandymo;
- teisingas `requestDate`, pasirenkamų ir`fromCache` serijų sukūrimas.
  Skaitymo išimtis sukelia rezultatą `CACHE_ERROR` ir nesuaktyvina atsarginio VIES.
  Įrašymo išimtis nepanaikina jau gauto autentiško `Valid` rezultato.
  Skaitymo išimtis grąžina `CACHE_ERROR` be atsarginio VIES. Rašymo išimtis
  neištrina patikimo `Valid` rezultato.

## 9. Bandykite dar kartą, eilėje ir DLQ / Bandykite iš naujo, eilėje ir DLQ

Didelio masto apdorojimas:

1. tęsti darbą prieš apdorojimą;
2. naudoti idempotencijos raktą;
3. skambinti VIES su nedideliu vietiniu pakartotinių bandymų skaičiumi;
4. jei `error.retryable()`, suplanuokite atidėtą pakartotinį bandymą su eksponentiniu vėlavimu;
5. sustoti po sukonfigūruoto maksimumo ir pereiti prie DLQ/rankinės peržiūros;
6. niekada nebandykite dar kartą `Invalid` arba`MalformedInput` nepakitęs.
   Nelaikykite ilgalaikio pakartotinio bandymo atmintyje naudodami grandinę `CompletableFuture`.
   Nenaudokite ilgalaikių pakartotinių bandymų kaip atmintyje esančių `CompletableFuture` grandinių.

## 10. Daugiau mazgas/pod / Keli mazgai arba ankštys

Kiekviena kopija turi savo ryšio baseiną, talpyklą, vieno skrydžio lentelę ir semaforą
gauti `maxConcurrentRequests(32)` yra **ne globali 32**.
Kiekviena kopija turi savo ryšio baseiną, talpyklą, vieno skrydžio lentelę ir
semaforai. Todėl `maxConcurrentRequests(32)`**nėra visuotinė 32** riba.
Reikalingi komponentai dideliu mastu / Reikalingi dideliu mastu:

- patvari atskirta eilė;
- ribotas vartotojų skaičius/langas;
- paskirstytas/visuotinis tarifo ribotuvas;
- bendrinama talpykla, kur verslo semantika tai leidžia;
- atidėtas pakartotinis bandymas ir DLQ;
- idempotencija/deduplikacija;
- automatinis mastelio keitimas pagal eilės amžių, o ne tiesioginis neribotas VIES lygiagretumas.

## 11. Sveikata ir stebimumas

- Gyvumas neturi priklausyti nuo VIES.
- `availability()` turėtų būti reta, talpykloje saugoma diagnostinė / apklausa ir taupiai saugokite talpykloje.
  – Išmatuokite p50/p95/p99 delsą / išmatuokite p50/p95/p99 delsą.
- Skaičiuokite pagal rezultato tipą ir `errorCode`/ Skaičiuokite pagal rezultatą ir klaidos kodą.
- Išmatuokite talpyklos įvykį, bandykite dar kartą, perkrovą, eilės amžių ir DLQ.
- Išmatuokite talpyklos įvykius, pakartotinius bandymus, perkrovas, eilės amžių ir DLQ dydį.
- Užmaskuoti PVM/vardą/adresą žurnaluose.

## 12. Anti-patterns / Sprendimai, kurių reikia vengti

- klientas pagal užklausą / naujas klientas pagal užklausą;
- neribotas ateities sandorių ar sinchronizuotų skambintojų skaičius
- per-pod ribotuvą traktuoti kaip visuotinį / vietinį ribotuvą laikyti visuotiniu;
- nedelsdami iš naujo bandydami kiekvieną klaidą
- `Unavailable` konvertavimas į`Invalid`;
- talpykloje saugomus konsultacijų duomenis laikyti nauju įrodymu;
- tiesioginis VIES skambinimas iš gyvumo patikrinimų arba numatytųjų vienetų testų.

## 13. Gamybos kontrolinis sąrašas

- [ ] Sukonfigūruotas vieno gyvavimo ciklas ir išjungimas.
- [ ] Visi keturi `ViesResponse` variantai yra tvarkomi aiškiai.
- [ ] API grąžina stabilų kodą ir HU/EN pranešimus.
- [ ] Sinchronizavimo, asinchronizavimo, įėjimo ir tinklo apribojimai yra riboti.
- [ ] Kelių mazgų srautas naudoja visuotinį / paskirstytą ribotuvą.
- [ ] Patvirtinta talpyklos atnaujinimo ir konsultacijų patikimumo politika.
- [ ] Yra atidėtas pakartotinis bandymas, maksimalus bandymų skaičius, idempotencija ir DLQ.
- [ ] Yra metrikos, įspėjimai ir užmaskuoti žurnalai.
- [ ] Gyvumas nepriklauso nuo VIES.
- [ ] Apibrėžiami vieneto, vietinės integracijos, lygiagretumo, apkrovos, mirkymo ir gedimo testai.
