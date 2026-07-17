# Gaeilge (ga) — Integration

Díchumasaíonn `disableCache()` an taisce stóráilte/mharthanach, ach féadfaidh glaonna comhuaineacha leis an bpéire céanna CBL + requester aon iarratas líonra single-flight amháin a roinnt. Déanann glao níos déanaí, tar éis a chríochnaithe, iarratas nua VIES. Tá `consultationNumber` roghnach agus féadfaidh VIES é a thabhairt ar ais, ach ní ráthaítear riamh é; braitheann a luach fianaiseach dlíthiúil ar rialacha áitiúla. Ná lódáil `MY_EU_VAT_NUMBER` ach ó fhoinse rúin/cumraíochta iontaofa.

> [Roghnóir teanga](../../LANGUAGES.md) · Cuirtear an logánú seo ar fáil ar mhaithe le hinrochtaineacht. Má bhíonn difríocht ann, is í an fhoinse chanónach theicniúil nó dhlíthiúil Bhéarla atá i réim. Fanann `LICENSE` agus`NOTICE` na fréimhe ceangailteach.

## 1. Saolré / Lifecycle

Úsáid réad `ViesClient` amháin i bhfeidhmchlár nó i gcás oibrí.
Ná cruthaigh cliant de réir iarratais HTTP: chaillfeá an comhthiomsú naisc, an taisce,
cumasc aon-eitilte agus teorainneacha áitiúla.
Úsáid `ViesClient` amháin in aghaidh an phróisis iarratais/oibrí. Ná cruthaigh cliant per
Iarratas HTTP; Má dhéantar é sin, caitheann sé comhthiomsú nasc, taisce, eitilt aonair agus áitiúil
teorainneacha.

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

Ní féidir oibríocht nua a thosú tar éis `close()`. Cuireann múchadh isteach ar an inmheánach gníomhach
oibríochtaí, agus cuireann sé `CLIENT_CLOSED` ar ais chuig iarratais choitianta atá ar bun.
Tá na modhanna sioncronaithe agus asyncrónacha a dtugtar díreach ina dhiaidh sin araon sioncrónach
Caitear eisceacht `IllegalStateException`.
Tar éis `close()`, ní ghlactar le haon obair nua. Cuireann múchadh isteach ar obair ghníomhach inmheánach
agus comhlánaíonn sé iarratais eitilte roinnte le `CLIENT_CLOSED`.
Caitheann glaonna API sioncronaithe agus sioncronaithe nua a dhéantar ina dhiaidh sin `IllegalStateException` go sioncronach.

## 2. API sioncronach / API Synchronous

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

Tá líon na nglaoiteoirí API sioncronacha teoranta freisin (`maxPendingSyncRequests`). Ar an teorainn
Faigheann iarratas bailí thuas toradh `CLIENT_OVERLOADED` láithreach.
Tá glaoiteoirí sioncrónacha teorannaithe ag `maxPendingSyncRequests`.

## 3. API Asincrónach / API Asincrónach

```java
client.checkAsync("PL0000000000")
        .thenAccept(response -> {
            // Handle all four response variants / Kezeld mind a négy eredményt.
        });
```

Ritheann an API async ar snáithe fíorúil leis an seiceadóir réamhshocraithe; seiceadóir féin
tá feidhm ag a rialacha sceidealaithe. Is ionann uimhir chánach + iarratais fiosrúcháin amháin
nascann siad le todhchaí roinnte inmheánach. Faigheann an glaoiteoir cóip cealaithe:
ní chuireann cealú tomhaltóra aonair isteach ar iarratas comhpháirteach na dtomhaltóirí eile.
Leis an seiceadóir réamhshocraithe, ritheann an API async ar snáitheanna fíorúla; seiceadóir saincheaptha
úsáideann sé a bheartas sceidealaithe féin. Tagann glaonna comhionanna CBL/iarratais isteach i gceann inmheánach
todhchaí roinnte. Faigheann gach glaoiteoir cóip chealaithe-sábháilte.
Ná stóráil na milliúin todhchaí i gcuimhne. Tá an tomhaltóir scuaine leanúnach teoranta i gcónaí
coinnigh an fhuinneog gníomhach.
Ná coinnigh na milliúin todhchaí. Caithfidh tomhaltóir scuaine marthanach teorainn a choinneáil
fuinneog próiseála.

## 4. Conradh API HTTP / conradh API HTTP

Mapáil mholta:
| ViesResponse | HTTP | Bain triail eile as | Megjegyzés / Nóta |
|---|---:|---:|---|
|`Valid`| 200 | níl | toradh fearainn |
|`Invalid`| 200 | níl | toradh fearainn, ní teip HTTP |
|`MalformedInput`| 400 | níl | ní mór don ghlaoiteoir ionchur a shocrú |
|`Unavailable(CLIENT_OVERLOADED)`| 429 | moill | cúlbhrú áitiúil |
| eile `Unavailable`| 503 | ag`error.retryable()`| gan aon chinneadh bailíochta |
Earráid shamplach JSON / Earráid shamplach JSON:

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

Téacs úsáideora `messageHu`/`messageEn`. Le haghaidh loga, méadrach agus loighic cliant
bain úsáid as an luach cobhsaí `errorCode` i gcónaí.
Tá `messageHu`/`messageEn`os comhair úsáideoirí. Úsáid luachanna cobhsaí`errorCode` le haghaidh logaí,
méadracht, agus loighic cliant.

## 5. Tosaithe an Earraigh

## Cumraíocht

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

## Rialaitheoir

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

Níl aon spleáchais Earraigh ag an leabharlann; tá an cód thuas mar chuid den fheidhmchlár íditheach.
Níl aon spleáchais Earraigh ag an leabharlann; baineann an t-oiriúnóir seo leis an aip íditheach.

## 6. Gnáthfhreastalaí JDK HTTP

Is é an sampla inrite iomlán ná `examples/ViesDemoServer.java`.
Sampla iomlán inrite:`examples/ViesDemoServer.java`.

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java 8085
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Úsáideann an sampla snáitheanna fíorúla, stádas HTTP fíor, agus freagraí earráide dátheangacha.
Úsáideann an sampla snáitheanna fíorúla, stádas HTTP brí, agus earráidí dátheangacha.

## 7. Aitheantas an iarrthóra agus an chomhairliúcháin

```java
var client = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
        .build();
```

Is é an requester uimhir CBL na heagraíochta féin agus ní mór di teacht ó fhoinse rúin/cumraíochta iontaofa. Le seiceáil bhailí féadfaidh VIES `requestIdentifier`/`consultationNumber` roghnach a thabhairt ar ais, ach ní ráthaítear é agus braitheann a luach fianaiseach ar rialacha áitiúla.

Tá aitheantas/dáta an chomhairliúcháin bhunaidh, ní seiceáil nua, i mbuille taisce. Díchumasaíonn `disableCache()` an taisce stóráilte, ach féadfaidh glaonna comhuaineacha comhionanna CBL+requester aon iarratas single-flight amháin a roinnt fós; déanann glao níos déanaí iarratas nua VIES tar éis a chríochnaithe.

## 8. Adapter taisce Redis

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

Riachtanais/Riachtanais:

- cur chun feidhme slán sábháilte;
- Teorainn ama gearr nasctha/orduithe;
- ainmspás eochrach versioned;
- méadracht agus foláirimh sláinte;
- gan atriail inmheánach gan teorainn;
- sraithiú ceart `requestDate`, roghnach, agus`fromCache`.
  Léamh eisceacht is cúis le toradh `CACHE_ERROR` agus ní spreagann sé aisíocaíocht VIES.
  Ní scriosann eisceacht scríofa an toradh barántúil `Valid` a fuarthas cheana.
  Tugann eisceacht léite ar ais `CACHE_ERROR` gan ais-ais-tarchur VIES. Eisceacht scríofa
  nach scriosann toradh údarásach `Valid`.

## 9. Atriail, scuaine és DLQ / Retry, scuaine, agus DLQ

I bpróiseáil ardscála:

1. leanúint leis an bpost roimh phróiseáil;
2. bain úsáid as eochair neamhshuime;
3. glaoch ar VIES le comhaireamh beag atriail áitiúil;
4. más `error.retryable()`, moill ar athiarracht sceidealta le moill easpónantúil;
5. stad tar éis uasmhéid cumraithe agus bogadh chuig DLQ/athbhreithniú láimhe;
6. Ná bain triail eile as `Invalid` nó`MalformedInput` gan athrú.
   Ná coinnigh athiarracht marthanach i gcuimhne le slabhra `CompletableFuture`.
   Ná cuir trialacha buana i bhfeidhm mar shlabhraí `CompletableFuture` i gcuimhne.

## 10. Több nód/pod / Nóid iolracha nó pods

Tá a linn ceangail féin, taisce, tábla aon-eitilte agus semaphore ag gach macasamhail
get `maxConcurrentRequests(32)` mar sin **ní domhanda 32**.
Tá a chomhthiomsú nasc féin, taisce, tábla aon-eitilte, agus
semaphores. Mar sin **ní teorainn dhomhanda é `maxConcurrentRequests(32)` de 32**.
Comhpháirteanna riachtanacha ar scála mór / Riachtanach ar scála:

- scuaine deighilte buan;
- comhaireamh teoranta tomhaltóra/fuinneog;
- teorannóir dáilte/ráta domhanda;
- taisce roinnte ina gceadaíonn séimeantaic ghnó é;
- moill ar atriail agus DLQ;
- neamhláithreacht/dídiúlú;
- uathscálú bunaithe ar aois scuaine, ní comhairgeadra VIES neamhtheoranta díreach.

## 11. Sláinte és observability

- Ní fhéadfaidh beogacht a bheith ag brath ar VIES.
- Ba cheart go mbeadh `availability()` ina dhiagnóiseach/Vótaíocht annamh, i dtaisce agus é a chur i dtaisce go coigilteach.
- Tomhais latency p50/p95/p99 / Beart p50/p95/p99 latency.
- Comhair de réir cineáil toraidh agus `errorCode`/ Comhaireamh de réir toraidh agus cód earráide.
- Tomhais an taisce a bhuail, bain triail eile as, ró-ualach, aois scuaine agus DLQ.
- Amais taisce, atriail, ró-ualaí, aois scuaine agus méid DLQ a thomhas.
- Cuir CBL/ainm/seoladh isteach sna logaí.

## 12. Frith-patrúin / Réitigh le seachaint

- cliant in aghaidh an iarratais / cliant nua in aghaidh an iarratais;
- todhchaí neamhtheoranta nó glaoiteoirí sioncronaithe
- caitheamh le teorannóir per-pod mar shriantóir domhanda / sriantóir áitiúil a fheiceáil mar shriantóir domhanda;
- gach earráid a thriail láithreach
- `Unavailable` a thiontú go`Invalid`;
- sonraí comhairliúcháin i dtaisce a láimhseáil mar chruthúnas úr;
- VIES beo a ghlaoch ó sheiceálacha beocht nó ó thástálacha aonaid réamhshocraithe.

## 13. Seicliosta táirgeachta

- [ ] Tá saolré agus múchadh Singleton cumraithe.
- [ ] Láimhseáiltear na ceithre leagan `ViesResponse` go sainráite.
- [ ] Seolann API cód cobhsaí mar aon le teachtaireachtaí HU/EN.
- [ ] Tá teorainneacha sioncronaithe, sioncronaithe, dul isteach agus líonra.
- [ ] Úsáideann trácht ilnód teorannóir domhanda/dáilte.
- [ ] Formheastar an beartas maidir le húire taisce agus cosaint ar chomhairliúchán.
- [ ] Atriail mhoillithe, tá uasiarrachtaí, díspeagadh, agus DLQ ann.
- [ ] Tá méadracht, foláirimh agus logaí folaithe ann.
- [ ] Tá beocht neamhspleách ar VIES.
- [ ] Sainmhínítear tástálacha aonaid, comhtháthú áitiúil, comhairgeadra, ualach, maos agus teip.
