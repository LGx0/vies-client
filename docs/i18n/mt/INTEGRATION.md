# Malti (mt) — Integration

> [Għażla tal-lingwa](../../LANGUAGES.md) · Din il-lokalizzazzjoni hija għall-aċċessibbiltà. F’każ ta’ differenza, is-sors kanoniku tekniku jew legali bl-Ingliż jipprevali. `LICENSE` u `NOTICE` fir-root jibqgħu legalment awtorevoli.

## 1. Ċiklu tal-Ħajja / Ċiklu tal-Ħajja

Uża oġġett `ViesClient` wieħed f'applikazzjoni jew istanza ta' ħaddiem.
Toħloqx klijent għal kull talba HTTP: int titlef il-pool tal-konnessjoni, il-cache,
amalgamazzjoni ta' titjira waħda u limiti lokali.
Uża `ViesClient` wieħed għal kull applikazzjoni/proċess tal-ħaddiem. Toħloqx klijent per
talba HTTP; jekk tagħmel dan, twarrab il-ġbir tal-konnessjonijiet, il-cache, it-titjira waħda, u lokali
limiti.

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

Operazzjoni ġdida ma tistax tinbeda wara `close()`. It-tifi jinterrompi l-intern attiv
operazzjonijiet, u jirritorna `CLIENT_CLOSED` għal talbiet komuni li jkunu għaddejjin.
Il-metodi sync u async imsejħa direttament wara huma t-tnejn sinkroniċi
Eċċezzjoni `IllegalStateException` tintefa'.
Wara `close()`, l-ebda xogħol ġdid ma huwa aċċettat. L-għeluq jinterrompi x-xogħol intern attiv
u jlesti talbiet kondiviżi matul it-titjira ma'`CLIENT_CLOSED`.
Is-sinkronizzazzjoni u l-async sejħiet API ġodda magħmula wara t-tnejn jitfgħu `IllegalStateException` b'mod sinkroniku.

## 2. API Sinkroniku / API Sinkroniku

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

In-numru ta' min iċempel API sinkroniku huwa wkoll limitat (`maxPendingSyncRequests`). Fuq il-limitu
hawn fuq talba valida immedjatament tikseb riżultat `CLIENT_OVERLOADED`.
Dawk li jċemplu sinkroniċi huma wkoll imdawra mid-dħul minn `maxPendingSyncRequests`.

## 3. Asinkronu API / Asynchronous API

```java
client.checkAsync("PL0000000000")
        .thenAccept(response -> {
            // Handle all four response variants / Kezeld mind a négy eredményt.
        });
```

L-API async timxi fuq ħajt virtwali bl-eżekutur default; eżekutur stess
ir-regoli tal-iskedar tagħha japplikaw. Numru tat-taxxa identiku + talbiet ta' inkjesta huma wieħed
huma jgħaqqdu ma 'futur kondiviż intern. Min iċempel jirċievi kopja li ma tistax tikkanċella:
il-kanċellazzjoni ta' konsumatur wieħed ma tinterrompix it-talba konġunta tal-oħrajn.
Bl-eżekutur default, l-API async timxi fuq ħjut virtwali; eżekutur tad-dwana
juża l-politika tal-iskedar tiegħu stess. Sejħiet identiċi tal-VAT/tal-applikant jingħaqdu ma' waħda interna
futur kondiviż. Kull min iċempel jirċievi kopja mingħajr periklu għall-kanċellazzjoni.
Taħżinx miljuni ta’ futuri fil-memorja. Il-konsumatur tal-kju persistenti huwa dejjem limitat
żomm it-tieqa attiva.
Żommx miljuni ta’ futuri. Konsumatur tal-kju durabbli għandu jżomm bounded
tieqa tal-ipproċessar.

## 4. Kuntratt HTTP API / kuntratt HTTP API

Immappjar rakkomandat:
| ViesResponse | HTTP | Ipprova mill-ġdid | Megjegyzés / Nota |
|---|---:|---:|---|
|`Valid`| 200 | le | riżultat tad-dominju |
|`Invalid`| 200 | le | riżultat tad-dominju, mhux falliment HTTP |
|`MalformedInput`| 400 | le | min iċempel irid jiffissa l-input |
|`Unavailable(CLIENT_OVERLOADED)`| 429 | ittardjat | kontropressjoni lokali |
| oħra `Unavailable`| 503 | minn`error.retryable()`| ebda deċiżjoni ta' validità |
Eżempju ta' żball JSON / Eżempju ta' żball JSON:

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

Test tal-utent `messageHu`/`messageEn`. Għal log, metrika u loġika tal-klijent
dejjem uża l-valur stabbli `errorCode`.
`messageHu`/`messageEn`huma jiffaċċjaw l-utent. Uża valuri`errorCode` stabbli għal zkuk,
metriċi, u loġika tal-klijent.

## 5. Boot tar-Rebbiegħa

## Konfigurazzjoni

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

## Kontrollur

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

Il-librerija m'għandha l-ebda dipendenzi tar-Rebbiegħa; il-kodiċi ta 'hawn fuq huwa parti mill-applikazzjoni li tikkonsma.
Il-librerija m'għandha l-ebda dipendenzi tar-Rebbiegħa; dan l-adapter jappartjeni għall-app li tikkonsma.

## 6. Server HTTP JDK sempliċi

L-eżempju eżekutibbli sħiħ huwa `examples/ViesDemoServer.java`.
Eżempju runnable sħiħ:`examples/ViesDemoServer.java`.

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java 8085
curl "http://localhost:8085/vat-check?number=DE000000000"
```

L-eżempju juża ħjut virtwali, status HTTP reali, u tweġibiet ta 'żball bilingwi.
L-eżempju juża ħjut virtwali, status HTTP sinifikanti, u żbalji bilingwi.

## 7. ID tal-applikant u tal-konsultazzjoni

```java
var client = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
        .build();
```

Min jagħmel it-talba għandu jkun in-numru tal-VAT tal-organizzazzjoni, miksub permezz
ta' `MY_EU_VAT_NUMBER` minn sigriet jew konfigurazzjoni fdati. Għal tweġiba valida
VIES **jista'** jirritorna `requestIdentifier`/`consultationNumber`, iżda dan huwa
fakultattiv, mhux garantit, u l-valur legali tiegħu jiddependi mir-regoli lokali.

F'cache hit l-identifikatur u `requestDate` jappartjenu għall-kontroll oriġinali.
`disableCache()` jitfi biss il-cache maħżuna: sejħiet paralleli identiċi VAT+requester
jistgħu jaqsmu talba single-flight waħda; sejħa aktar tard wara t-tlestija tibgħat
talba ġdida.

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

Rekwiżiti / Rekwiżiti:

- implimentazzjoni bla periklu għall-ħajt;
- konnessjoni qasira/timeout tal-kmand;
- namespace taċ-ċavetta b'verżjoni;
- metriċi u allerti tas-saħħa;
- l-ebda prova interna mingħajr limiti;
- serialization korretta ta '`requestDate`, optionals, u`fromCache`.
  L-eċċezzjoni tal-qari tikkawża riżultat `CACHE_ERROR` u ma tiskattax il-VIES fallback.
  Eċċezzjoni tal-kitba ma tħassarx ir-riżultat `Valid` awtentiku diġà riċevut.
  Eċċezzjoni tal-qari tirritorna `CACHE_ERROR` mingħajr fallback VIES. Eċċezzjoni tal-kitba
  ma jħassarx riżultat awtorevoli `Valid`.

## 9. Ipprova mill-ġdid, kju és DLQ / Ipprova mill-ġdid, kju, u DLQ

Fl-ipproċessar fuq skala għolja:

1. jippersistu l-impjieg qabel l-ipproċessar;
2. uża ċavetta idempotenza;
3. ċempel lil VIES b'għadd żgħir ta' tentattivi lokali;
4. jekk `error.retryable()`, iskeda dewmien mill-ġdid ipprova b'dewmien esponenzjali;
5. tieqaf wara massimu kkonfigurat u timxi għal DLQ/reviżjoni manwali;
6. qatt ma jerġa 'jipprova `Invalid` jew`MalformedInput` mhux mibdul.
   Żommx iżomm lura fil-memorja bil-katina `CompletableFuture`.
   Timplimentax tentattivi mill-ġdid durabbli bħala ktajjen `CompletableFuture` fil-memorja.

## 10. Több node/pod / Nodi jew imżiewed multipli

Kull replika għandha l-pool ta’ konnessjoni, il-cache, it-tabella ta’ titjira waħda u s-semaforu tagħha stess
get `maxConcurrentRequests(32)` għalhekk **mhux globali 32**.
Kull replika għandha pool ta 'konnessjoni tagħha stess, cache, tabella ta' titjira waħda, u
semafori.`maxConcurrentRequests(32)` għalhekk **mhux limitu globali ta' 32**.
Komponenti meħtieġa fuq skala kbira / Meħtieġa fuq skala:

- kju partitioned durabbli;
- għadd/tieqa tal-konsumatur imqabbad;
- limitatur tar-rata distribwita/globali;
- cache kondiviża fejn is-semantika tan-negozju tippermetti dan;
- ittardja mill-ġdid u DLQ;
- idempotenza/deduplikazzjoni;
- Autoscaling ibbażat fuq l-età tal-kju, mhux konkorrenza diretta VIES illimitata.

## 11. Is-saħħa u l-osservabbiltà

- Il-ħajja m'għandhiex tiddependi fuq il-VIES.
- `availability()` għandu jkun rari, cached dijanjostiku / Poll u cache huwa kemxejn.
- Kejjel latency p50/p95/p99 / Kejjel latency p50/p95/p99.
- Għadd skond it-tip ta 'riżultat u `errorCode`/ Għadd skond ir-riżultat u l-kodiċi ta' żball.
- Kejjel il-hit tal-cache, ipprova mill-ġdid, tagħbija żejda, età tal-kju u DLQ.
- Kejjel hits tal-cache, tentattivi mill-ġdid, tagħbija żejda, età tal-kju, u daqs DLQ.
- Maskra VAT/isem/indirizz fiz-zkuk.

## 12. Kontra l-mudelli / Soluzzjonijiet li għandhom jiġu evitati

- klijent għal kull talba / klijent ġdid għal kull talba;
- futures illimitati jew is-sinkronizzazzjoni ta' min iċempel
- trattament ta' limitatur per-pod bħala globali / jara ta' limitatur lokali bħala globali;
- jerġa' jipprova kull żball immedjatament
- konverżjoni `Unavailable` għal`Invalid`;
- it-trattament tad-dejta tal-konsultazzjoni fil-cache bħala prova ġdida;
- sejħa VIES ħajjin minn kontrolli tal-ħajja jew testijiet tal-unità default.

## 13. Lista ta' kontroll tal-produzzjoni

- [ ] Iċ-ċiklu tal-ħajja u l-għeluq ta' Singleton huma kkonfigurati.
- [ ] L-erba' varjanti `ViesResponse` kollha huma ttrattati b'mod espliċitu.
- [ ] API jirritorna kodiċi stabbli flimkien ma' messaġġi HU/EN.
- [ ] Il-limiti tas-sinkronizzazzjoni, l-async, id-dħul u n-netwerk huma limitati.
- [ ] It-traffiku b'ħafna nodi juża limitatur globali/imqassam.
- [ ] Il-freskezza tal-cache u l-politika ta' prova ta' konsultazzjoni huma approvati.
- [ ] Jeżistu pprova mill-ġdid, tentattivi massimi, idempotenza, u DLQ.
- [ ] Jeżistu metriċi, twissijiet, u logs masked.
- [ ] Il-ħajja hija indipendenti mill-VIES.
- [ ] It-testijiet tal-unità, tal-integrazzjoni lokali, tal-konkorrenza, tat-tagħbija, tat-tixrib, u tal-falliment huma definiti.
