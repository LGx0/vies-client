# Suomi (fi) — Integration

`disableCache()` poistaa tallennetun/pysyvän välimuistin käytöstä, mutta samanaikaiset kutsut samalla ALV-tunnus + requester -parilla voivat jakaa yhden single-flight-verkkopyynnön. Sen valmistumisen jälkeen tehty myöhempi kutsu lähettää uuden VIES-pyynnön. `consultationNumber` on valinnainen ja VIES voi palauttaa sen, mutta sitä ei koskaan taata; sen oikeudellinen todistusarvo riippuu paikallisista säännöistä. Lataa `MY_EU_VAT_NUMBER` vain luotetusta salaisuus-/konfiguraatiolähteestä.

> [Kielivalitsin](../../LANGUAGES.md) · Tämä lokalisointi parantaa saavutettavuutta. Jos se poikkeaa kanonisesta englanninkielisestä teknisestä tai oikeudellisesta lähteestä, englanninkielinen lähde määrää. Juuren `LICENSE` ja`NOTICE` ovat oikeudellisesti määrääviä.

## 1. Elinkaari / Elinkaari

Käytä yhtä `ViesClient`-objektia sovelluksessa tai työntekijäinstanssissa.
Älä luo asiakasta HTTP-pyyntöä kohti: menetät yhteyspoolin, välimuistin,
yhden lennon yhdistäminen ja paikalliset rajat.
Käytä yhtä `ViesClient`:tä hakemusta/työntekijäprosessia kohti. Älä luo asiakasta per
HTTP-pyyntö; tämä hylkää yhteyden yhdistämisen, välimuistin, yhden lennon ja paikallisen
rajoja.

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

Uutta toimintoa ei voi aloittaa `close()`:n jälkeen. Sammutus keskeyttää aktiivisen sisäisen
toiminnot ja palauttaa `CLIENT_CLOSED` yleisiin käynnissä oleviin pyyntöihin.
Heti jälkeenpäin kutsutut synkronointi- ja asynkronointimenetelmät ovat molemmat synkronisia `IllegalStateException`-poikkeus heitetään.
`close()`:n jälkeen uusia töitä ei hyväksytä. Sammutus keskeyttää aktiivisen sisäisen työn
ja suorittaa jaetut lennonaikaiset pyynnöt `CLIENT_CLOSED`:n kanssa.
Myöhemmin tehdyt uudet synkronointi- ja asynkronointisovellusliittymäkutsut heittävät molemmat `IllegalStateException`:n
synkronisesti.

## 2. Synchronous API / Synchronous API

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

Synkronisten API-soittajien määrä on myös rajoitettu (`maxPendingSyncRequests`). Rajalla
yllä oleva kelvollinen pyyntö saa välittömästi `CLIENT_OVERLOADED`-tuloksen.
Synkroniset soittajat ovat myös sisäänpääsyn rajoittamia `maxPendingSyncRequests`:llä.

## 3. Asynchronous API / Asynchronous API

```java
client.checkAsync("PL0000000000")
        .thenAccept(response -> {
            // Handle all four response variants / Kezeld mind a négy eredményt.
        });
```

Async-sovellusliittymä toimii virtuaalisessa säikeessä oletussuorittimen kanssa; oma toimeenpanija
sen aikataulusääntöjä sovelletaan. Sama veronumero + tiedustelupyynnöt ovat yksi
ne muodostavat yhteyden sisäiseen yhteiseen tulevaisuuteen. Soittaja saa peruuttamattoman kopion:
yhden kuluttajan peruuttaminen ei keskeytä muiden yhteistä pyyntöä.
Oletussuorittajalla async-sovellusliittymä toimii virtuaalisissa säikeissä; mukautettu toimeenpanija
käyttää omaa aikataulupolitiikkaansa. Identtiset ALV/pyynnön esittäjäpuhelut yhdistyvät yhdeksi sisäiseksi
yhteinen tulevaisuus. Jokainen soittaja saa peruutusturvallisen kopion.
Älä tallenna miljoonia futuureja muistiin. Jatkuva jonokuluttaja on aina rajoitettu
pitää ikkuna aktiivisena.
Älä pidä miljoonia futuureja. Kestävän jonon kuluttajan on pidettävä rajallinen
käsittelyikkuna.

## 4. HTTP API -sopimus / HTTP API -sopimus

Suositeltu kartoitus:
| ViesResponse | HTTP | Yritä uudelleen | Huomautus / Huomautus |
|---|---:|---:|---|
|`Valid`| 200 | ei | verkkotunnuksen tulos |
|`Invalid`| 200 | ei | verkkotunnuksen tulos, ei HTTP-virhe |
|`MalformedInput`| 400 | ei | soittajan on korjattava syöte |
|`Unavailable(CLIENT_OVERLOADED)`| 429 | myöhässä | paikallinen vastapaine |
| muut `Unavailable`| 503 | kirjoittanut`error.retryable()`| ei pätevää päätöstä |
Esimerkkivirhe JSON / Esimerkkivirhe JSON:

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

Käyttäjäteksti `messageHu`/`messageEn`. Loki-, metri- ja asiakaslogiikkaa varten
käytä aina vakaata arvoa `errorCode`.
`messageHu`/`messageEn`ovat käyttäjälle suunnattuja. Käytä lokeissa vakaita`errorCode`-arvoja,
mittareita ja asiakaslogiikkaa.

## 5. Spring Boot

## Kokoonpano

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

## Ohjain

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

Kirjastolla ei ole kevätriippuvuutta; yllä oleva koodi on osa kuluttavaa sovellusta.
Kirjastolla ei ole kevätriippuvuutta; tämä sovitin kuuluu kuluttavalle sovellukselle.

## 6. Tavallinen JDK HTTP-palvelin

Täysi suoritettava esimerkki on `examples/ViesDemoServer.java`.
Täysi ajettava esimerkki:`examples/ViesDemoServer.java`.

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java 8085
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Esimerkki käyttää virtuaalisia säikeitä, todellisia HTTP-tiloja ja kaksikielisiä virhevastauksia.
Esimerkki käyttää virtuaalisia säikeitä, merkityksellisiä HTTP-tiloja ja kaksikielisiä virheitä.

## 7. Pyytäjän ja konsultoinnin tunnus

```java
var client = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
        .build();
```

Requester on organisaation oma ALV-tunnus ja sen tulee tulla luotetusta salaisuus-/konfiguraatiolähteestä. Kelvollisen tarkistuksen yhteydessä VIES voi palauttaa valinnaisen `requestIdentifier`/`consultationNumber`-arvon, mutta sitä ei taata ja sen todistusarvo riippuu paikallisista säännöistä.

Välimuistiosuma sisältää alkuperäisen konsultaation tunnuksen/päivämäärän, ei uutta tarkistusta. `disableCache()` poistaa tallennetun välimuistin käytöstä, mutta samanaikaiset identtiset ALV+requester-kutsut voivat silti jakaa yhden single-flight-verkkopyynnön; valmistumisen jälkeen myöhempi kutsu lähettää uuden VIES-pyynnön.

## 8. Redis-välimuistisovitin

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

Vaatimukset / Vaatimukset:

- säikeen turvallinen toteutus;
- lyhyt yhteys/komennon aikakatkaisu;
- versioidut avaimen nimiavaruudet;
- mittarit ja terveyshälytykset;
- ei rajatonta sisäistä uudelleenyritystä;
- `requestDate`:n, valinnaisten ja`fromCache`:n oikea sarjoittaminen.
  Lukupoikkeus aiheuttaa tuloksen `CACHE_ERROR` eikä laukaise VIES-varaa.
  Kirjoituspoikkeus ei poista jo vastaanotettua autenttista `Valid`-tulosta.
  Lukupoikkeus palauttaa `CACHE_ERROR`:n ilman VIES-varamuutosta. Kirjoituspoikkeus
  ei poista arvovaltaista `Valid`-tulosta.

## 9. Yritä uudelleen, jono ja DLQ / Yritä uudelleen, jono ja DLQ

Laajamittaisessa käsittelyssä:

1. jatka työtä ennen käsittelyä;
2. käytä idempotenssiavainta;
3. soittaa VIES:lle pienellä paikallisella uudelleenyritysten määrällä;
4. jos `error.retryable()`, ajoita viivästetty uudelleenyritys eksponentiaalisella viiveellä;
5. lopeta määritetyn maksimiarvon jälkeen ja siirry DLQ/manuaaliseen tarkasteluun;
6. Älä koskaan yritä uudelleen `Invalid` tai`MalformedInput` muuttumattomana.
   Älä säilytä kestävää uudelleenyritystä muistissa ketjulla `CompletableFuture`.
   Älä käytä kestäviä uudelleenyrityksiä muistissa olevina `CompletableFuture`-ketjuina.

## 10. Több node/pod / Useita solmuja tai tyynyjä

Jokaisella replikalla on oma yhteysallas, välimuisti, yhden lennon pöytä ja semafori
get `maxConcurrentRequests(32)` on siksi **ei globaali 32**.
Jokaisella replikalla on oma yhteysallas, välimuisti, yhden lennon taulukko ja
semaforit. Siksi `maxConcurrentRequests(32)` ei ole **32:n maailmanlaajuinen raja**.
Vaaditut komponentit suuressa mittakaavassa / Vaaditaan mittakaavassa:

- kestävä osioitu jono;
- rajoitettu kuluttajamäärä/ikkuna;
- hajautettu / yleinen nopeuden rajoitin;
- jaettu välimuisti, jos liiketoiminnan semantiikka sen sallii;
- viivästynyt uudelleenyritys ja DLQ;
- idempotenssi/deduplikaatio;
- automaattinen skaalaus perustuu jonon ikään, ei suoraa rajoittamatonta VIES-samanaikaisuutta.

## 11. Terveys ja havaittavuus

- Elävyys ei saa riippua VIES:stä.
- `availability()`:n tulisi olla harvinainen, välimuistissa oleva diagnostiikka / kysely ja tallenna se säästeliäästi.
- Mittaa p50/p95/p99 latenssi / Mittaa p50/p95/p99 latenssi.
- Laske tulostyypin mukaan ja `errorCode`/ Laske tuloksen ja virhekoodin mukaan.
- Mittaa välimuistin osuma, yritä uudelleen, ylikuormitus, jonon ikä ja DLQ.
- Mittaa välimuistin osumia, uudelleenyrityksiä, ylikuormituksia, jonon ikää ja DLQ-kokoa.
- Peitä ALV/nimi/osoite lokeissa.

## 12. Anti-kuviot / Vältettävät ratkaisut

- asiakas per pyyntö / uusi asiakas per pyyntö;
- rajoittamattomat futuurit tai synkronoidut soittajat
- Pod-rajoittimen käsitteleminen globaalina / paikallisen rajoittimen näkeminen globaalina;
- yrittää jokaista virhettä välittömästi uudelleen
- `Unavailable`:n muuntaminen`Invalid`:ksi;
- välimuistissa olevien kuulemistietojen käsitteleminen tuoreena todisteena;
- Live VIES:n kutsuminen elävyyden tarkistuksista tai oletusyksiköiden testeistä.

## 13. Tuotannon tarkistuslista

- [ ] Singletonin elinkaari ja sammutus on määritetty.
- [ ] Kaikki neljä `ViesResponse`-versiota käsitellään erikseen.
- [ ] API palauttaa vakaan koodin sekä HU/EN-viestit.
- [ ] Synkronointi-, asynkronointi-, sisääntulo- ja verkkorajoitukset ovat rajoitettuja.
- [ ] Usean solmun liikenteessä käytetään globaalia/hajautettua rajoitinta.
- [ ] Välimuistin tuoreus ja konsultointivarma käytäntö on hyväksytty.
- [ ] Viivästynyt uudelleenyritys, yritysten enimmäismäärä, idempotenssi ja DLQ ovat olemassa.
- [ ] Mittareita, hälytyksiä ja peitettyjä lokeja on olemassa.
- [ ] Elävyys on riippumaton VIES:stä.
- [ ] Yksikkö-, paikallinen integraatio-, samanaikaisuus-, kuormitus-, pito- ja vikatestit määritellään.
