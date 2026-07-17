# Hrvatski (hr) — vies-client

`disableCache()` isključuje spremljenu/trajnu predmemoriju, ali istodobni pozivi s istim parom PDV broj + requester mogu dijeliti jedan single-flight mrežni zahtjev. Kasniji poziv nakon njegova završetka izvršava novi VIES zahtjev. `consultationNumber` je neobavezan i VIES ga može vratiti, ali nikada nije zajamčen; njegova pravna dokazna vrijednost ovisi o lokalnim pravilima. Učitavajte `MY_EU_VAT_NUMBER` samo iz pouzdanog izvora tajni/konfiguracije.

> [Svi jezici](../../LANGUAGES.md) · Informativni prijevod. U slučaju razlike mjerodavan je kanonski engleski tehnički ili pravni izvor. Samo su korijenski `LICENSE` i `NOTICE` pravno mjerodavni; prijevod ih ne zamjenjuje.

[![License: Apache-2.0](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](../../../LICENSE)
[![Java 21+](https://img.shields.io/badge/Java-21%2B-orange.svg)](../../../pom.xml)
[![Buy Me a Coffee](https://img.shields.io/badge/Buy_Me_a_Coffee-support-ffdd00?logo=buymeacoffee&logoColor=000)](https://buymeacoffee.com/lgx0)

🌍 **[Svi službeni jezici Europske unije](../../LANGUAGES.md)**

`vies-client` je samostalan Java 21+ klijent za REST API sustava VIES
(VAT Information Exchange System) Europske komisije. Nema vanjskih ovisnosti u
vrijeme izvođenja i koristi samo JDK HTTP klijent (`java.net.http`). Može se
uključiti u Spring Boot, Quarkus, Micronaut, obični JDK `HttpServer` ili bilo koju
drugu Java aplikaciju.

**Pojmovi za pretraživanje:** VIES provjera, provjera PDV broja, validator PDV
identifikacijskog broja, provjera EU PDV-a i provjera poreznog identifikacijskog
broja. Ovo nije opći porezni kalkulator; služi isključivo za provjeru EU PDV
brojeva putem sustava VIES.

Ovo je neovisan projekt otvorenog koda. Nije službeni proizvod Europske komisije,
Europske unije ni poreznog tijela države članice te ga te institucije ne
podržavaju niti certificiraju.

- **Zahtjev:** Java **21+** bajt kod/API; cijeli paket je verificiran na JDK 21.
- **Službena VIES dokumentacija:** <https://ec.europa.eu/taxation_customs/vies/#/technical-information>
- **Krajnja točka za provjeru:** `https://ec.europa.eu/taxation_customs/vies/rest-api/ms/{countryCode}/vat/{vatNumber}`
- **Dostupnost država članica:** `https://ec.europa.eu/taxation_customs/vies/rest-api/check-status`

## Dokumentacija

- [Instalacija](INSTALLATION.md)
- [Integracija](INTEGRATION.md)
- [Tehnička dokumentacija](TECHNICAL.md)
- [Testiranje](TESTING.md)
- [Otvoreni kod i licenca](OPEN_SOURCE.md)
- [Izdavanje](RELEASING.md)
- [Objava na GitHubu](GITHUB_SETUP.md)
- [Doprinos projektu](CONTRIBUTING.md)
- [Sigurnosna pravila](SECURITY.md)
- [Kodeks ponašanja](CODE_OF_CONDUCT.md)
- [Podrška i donacije](SUPPORT.md)
- [Obavijesti o komponentama trećih strana](THIRD_PARTY_NOTICES.md)
- [Dnevnik promjena](CHANGELOG.md)

## Izgradnja i povezivanje

```bash
./mvnw install    # testovi + target/vies-client-1.2.0.jar (+ -sources.jar, -javadoc.jar)
               # i instalacija u lokalni Maven repozitorij
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

**Pravi JPMS modul.** Jar se ponaša kao imenovani modul (`jar --describe-module`):

```
vies.client@1.2.0
exports vies.client
requires java.net.http
contains vies.client.internal   ← neizvezeni zatvoreni interni paket
```

Povežite se iz modularne aplikacije poput ove:

```java
module my.api.server {
    requires vies.client;
}
```

Iz Classpatha (u nemodulariziranom, "tradicionalnom" projektu) radi na isti način —
`module-info` jednostavno ne igra u ovom slučaju. Čak i bez Maven/Gradle
može se koristiti: izvorno stablo pod `src/main/java` može se kopirati u vaš vlastiti projekt
(ostavite `module-info.java` ako vaš projekt nije modulariziran).

## Brzi primjer

```java
import vies.client.*;

try (var vies = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER"))) // pouzdani secret/config
        .retries(1)
        .build()) {

    // Prihvaća crtice, razmake i mala slova; preslikava GR u EL.
    switch (vies.check("DE 000 000 000")) {
        case ViesResponse.Valid v ->
            System.out.println("Valjan: " + v.traderName().orElse("(naziv nije javan)")
                    + " — broj konzultacije: " + v.consultationNumber().orElse("-"));
        case ViesResponse.Invalid i ->
            System.out.println("Nevažeći PDV broj: " + i.vatNumber());
        case ViesResponse.Unavailable u ->
            System.out.println("VIES trenutačno nije dostupan (" + u.errorCode() + "), pokušajte poslije");
        case ViesResponse.MalformedInput m ->
            System.out.println("Neispravan unos: " + m.reason());
    }
}
```

`switch` je iscrpan: kompajler jamči da ste obradili sva četiri izlaza
(zapečaćeno sučelje + podudaranje uzorka).

### Zašto je `defaultRequester` važan?

Kao requester navedite vlastiti PDV broj organizacije iz pouzdanog izvora tajni/konfiguracije. Za valjan rezultat VIES može vratiti neobavezni `consultationNumber`, ali on nikada nije zajamčen. Njegova dokazna vrijednost ovisi o lokalnim pravilima; spremite i vrijeme, provjereni PDV broj te rezultat prema pravilima revizije i zaštite podataka.

## Veza s Spring Boot API poslužiteljem

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

Klijent je nepromjenjiv i siguran za niti — aplikacija stvara jednu instancu
životni ciklus (singleton bean) i zatvoriti pri gašenju (`close`).

Uzorak bez okvira: [`examples/ViesDemoServer.java`](../../../examples/ViesDemoServer.java)
(obični JDK `HttpServer`, na virtualnim nitima):

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java     # port 8085
curl "http://localhost:8085/vat-check?number=HU00000000"
```

## Asinkrona upotreba

```java
CompletableFuture<ViesResponse> future = vies.checkAsync("PL0000000000");
```

Prema zadanim postavkama, asinkroni pozivi izvode se na **virtualnim nitima** (Project Loom) — puno
niti u paralelnoj provjeri nema otpada platforma-nit. Vlastiti izvršitelj a
može se navesti u builder-u (`executor(...)`— ne zatvara ga klijent).

Do 512 **individualnih radnji asinkronog voditelja** iz baze asinkronih klijenata
primljeno po instanci klijenta. Jedno čitanje predmemorije kratko vrijeme zauzima prostor;
neispravan unos i isti jednokratni pratilac ne zauzimaju odvojeni prostor. U slučaju preopterećenja, novi pojedinačni zahtjev odmah
Dobivate rezultat `Unavailable(..., "CLIENT_OVERLOADED")`. Ovo je unutar klijenta
ograničava rad; ulazni red čekanja i budućnosti koje je također pohranio pozivatelj
mora biti ograničen.

## Višemilijunska tvornica upita

Za milijune korisnika, nemojte pokretati milijune budućnosti u jednoj JVM, i nemojte
dopustite neograničen izravan promet na VIES. Predložena struktura:

> Klijent može biti komponenta obrade milijunskog reda čekanja, ali stvarna
> Prijenos VIES-a ograničen je promjenjivim, nezajamčenim ograničenjima sustava EU-a i zemalja članica
> biti odlučan. Virtualne niti smanjuju troškove čekanja; uzvodno
> kapacitet nije povećan.

1. Dolazne provjere prima trajni, podijeljeni red poruka.
2. Nekoliko vodoravno postavljenih radnika konzumira uže u ograničenim obrocima.
3. Jedan singleton `ViesClient` radi s virtualnim nitima po radniku.
4. Radnici koriste uobičajenu Redis predmemoriju preko `ViesCache` sučelja.
5. Globalni/distribuirani limitator brzine štiti krajnje VIES krajnje točke EU-a i država članica.

Unutar JVM-a, klijent spaja iste, dolazeći u isto vrijeme
porezni broj/parovi upita (single-flight), tako da ga ne uzrokuje promašaj predmemorije
"stampedo zahtjeva".`maxConcurrentRequests` je ograničen stvarnom mrežom
zahtjeva, a `maxPendingAsyncRequests` i `maxPendingSyncRequests` u memoriji
daje trenutni povratni pritisak. Svi su lokalni za JVM. Zbirni promet nekoliko radnika
mora se regulirati zajedničkim, raspodijeljenim graničnikom. Trajni ulazni red čekanja i
potrošački skup i dalje bi trebao biti ograničen aplikacijom.

Primjer radnika s teškim teretom:

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

Rezultat `Unavailable`— uključujući vrijednost `CLIENT_OVERLOADED`— postojan je
morate pokušati ponovno s odgodom u redu čekanja.`MalformedInput` i`Invalid` ne treba ponovno pokušavati.

## Put zahtjeva/životni ciklus zahtjeva

1. **Normaliziraj / Normaliziraj:** provjerite kod zemlje i format bez mreže.
2. **Predmemorija:** Trenutačni povratak valjanih, još uvijek živih rezultata.
3. **Single-flight:** identični parovi porezne šifre+upita spojeni su unutar jednog JVM-a.
4. **Pristup:** asinkrona i mrežna ograničenja štite memoriju i VIES.
5. **HTTP:** ponovno korištena JDK `HttpClient` veza s kratkim vremenskim ograničenjem.
6. **Potvrda / Provjera odgovora:** nepotpuna booleova vrijednost ili datum →`MALFORMED_RESPONSE`.
7. **Pisanje u predmemoriju:** samo autentični `Valid` rezultat je u predmemoriju.

## Dvojezični odgovori na pogreške

Kod strojne pogreške uvijek je stabilan i neovisan o jeziku.`error()` je mađarski i engleski
daje korisniku tekst i odluku o ponovnom pokušaju:

```java
var response = vies.check("HU00000000");
response.error().ifPresent(error -> {
    log.warn("{} | {} | retry={}", error.messageHu(), error.messageEn(), error.retryable());
});
```

| Rezultat         |        HTTP | Pokušaj ponovno | Predmemorija | Značenje                                                       |
| ---------------- | ----------: | --------------: | -----------: | -------------------------------------------------------------- |
| `Valid`          |         200 |              ne |        da/da | VIES potvrđen kao važeći / VIES potvrđeno važeći               |
| `Invalid`        |         200 |              ne |           ne | VIES nije potvrdio da je valjan / VIES nije potvrdio valjanost |
| `Unavailable`    | 503 ili 429 | uglavnom/obično |           ne | Nije donesena odluka o valjanosti                              |
| `MalformedInput` |         400 |              ne |           ne | Unos se mora ispraviti / Unos se mora ispraviti                |

## Konfiguracija (builder)

| Postavka                          | Osnovna vrijednost     | Što znači                                                                                       |
| --------------------------------- | ---------------------- | ----------------------------------------------------------------------------------------------- |
| `baseUrl(String)`                 | službeni VIES REST URL | Preusmjeravanje u lažnom testu                                                                  |
| `connectTimeout(Duration)`        | 5 s                    | Istek TCP/TLS veze                                                                              |
| `requestTimeout(Duration)`        | 8 s                    | Ukupno vrijeme čekanja zahtjeva (VIES je interaktivan, neka bude kratak)                        |
| `admissionTimeout(Duration)`      | 2 s                    | Ovo je koliko dugo čeka slobodan mrežni prostor                                                 |
| `defaultRequester(ViesRequester)` | nema                   | Vlastiti porezni broj zajednice → savjetovanje ID                                               |
| `retries(int)`                    | 0                      | Automatski ponovni pokušaj pri prolaznim pogreškama (0-5, eksponencijalni odmak + podrhtavanje) |
| `retryDelay(Duration)`            | 400 ms                 | Eksponencijalni backoff default                                                                 |
| `maxConcurrentRequests(int)`      | 32                     | Gornja granica istodobnih stvarnih VIES mrežnih zahtjeva                                        |
| `maxPendingSyncRequests(int)`     | 512                    | Ograničenje memorije za istodobne sinkronizirane pozivatelje; iznad njega `CLIENT_OVERLOADED`   |
| `maxPendingAsyncRequests(int)`    | 512                    | Ograničenje memorije za aktivne/na čekanju asinkrone operacije; iznad njega `CLIENT_OVERLOADED` |
| `cacheTtl(Duration)`              | 24 sata                | Vrijeme predmemorije za valjane pogotke                                                         |
| `cacheMaxEntries(int)`            | 10 000                 | Ograničenje veličine ugrađene predmemorije                                                      |
| `cache(ViesCache)`                | ugrađen                | Vlastiti cache-backend (npr. Redis)                                                             |
| `disableCache()`                  | —                      | Nema spremljene predmemorije; istodobni jednaki pozivi mogu dijeliti single-flight               |
| `userAgent(String)`               | modul-id               | Trebali biste se identificirati u EU                                                            |
| `executor(ExecutorService)`       | virtualna nit/zadatak  | Moj asinkroni izvršitelj; pozivatelj je odgovoran za njegov životni ciklus                      |

### Vlastita predmemorija (npr. Redis)

```java
class RedisViesCache implements ViesCache {
    public Optional<ViesResponse.Valid> get(String key) { /* ... */ }
    public void put(String key, ViesResponse.Valid value, Duration ttl) { /* ... */ }
}

var vies = ViesClient.builder().cache(new RedisViesCache()).build();
```

Samo se rezultat `Valid` sprema u predmemoriju — nevažeći broj i prolazna pogreška nikad.
Za Redis adapter koristite kratko vrijeme čekanja predmemorije, imenski prostor s verzijama i vlastiti
metrika pogreške. Pogreška čitanja predmemorije `CACHE_ERROR` nastaje kada Redis ne radi
ne započinjite nekontrolirani stampedo VIES zahtjeva.

`consultationNumber` i`requestDate` vraćeni iz predmemorije su **originalni**
dokumentira konzultacije; pogodak predmemorije nije nova VIES provjera. Za novi certifikat
provjerite `fromCache`, koristite kratki TTL ili `disableCache()`.

## Semantika koju vrijedi znati

1. **`Unavailable`≠`Invalid`.** Sustavi pozadine države članice redovito
   ispadaju (`MS_UNAVAILABLE`,`MS_MAX_CONCURRENT_REQ`...). U ovom slučaju, broj a
   **nije ocijenjeno od strane VIES-a** — komercijalno je zabranjeno smatrati nevažećim.
2. **Prethodno filtriranje formata nije VIES autentifikacija.** Modul filtrira registrirane
   pogrešan unos (`MalformedInput`) prije odlaska na mrežu, ali valjanost
   njegov izvor je uvijek odgovor VIES-a.
3. **Ponovni pokušaj također povećava opterećenje.** Pokušajte samo privremenu pogrešku lokalno nekoliko puta
   opet; u velikoj operaciji primarni je mehanizam odgođenog ponovnog pokušaja trajnog reda čekanja.
4. **Grčka `EL`, Sjeverna Irska `XI`.** Modul obrađuje oba,`GR`
   automatski preslikava unos u `EL`.

## Testovi

```bash
./mvnw test    # jedinični + lokalni HTTP testovi konkurentnosti, ponovnog pokušaja, backpressurea i životnog ciklusa
```

---

## Engleski brzi početak

Java 21+ klijent bez ovisnosti za VIES PDV broj Europske komisije
validacija REST API. Gradite sa `./mvnw package`, zatim:

```java
try (var vies = ViesClient.createDefault()) {
    if (vies.check("DE000000000") instanceof ViesResponse.Valid v) {
        System.out.println(v.traderName().orElse("?"));
    }
}
```

Rezultati su zapečaćena hijerarhija (`Valid`/`Invalid`/`Unavailable`/
`MalformedInput`) dizajniran za iscrpno usklađivanje uzoraka`switch`. Valjano
rezultati se pohranjuju u predmemoriju 24 sata; prolazni VIES prekidi prijavljeni su kao
`Unavailable`, nikada kao`Invalid`. Službena API dokumentacija:
<https://ec.europa.eu/taxation_customs/vies/#/technical-information>.

### Operacija velikih razmjera

Ovaj klijent može biti jedna komponenta cjevovoda za obradu od milijun stavki, ali
stvarna propusnost VIES-a ograničena je promjenjivim, nezajamčenim EU-om i državom članicom
granice. Virtualne niti smanjuju trošak blokiranja; ne povećavaju se uzvodno
kapacitet. Koristite izdržljivi particionirani red čekanja, ograničene potrošače, zajednički Redis
predmemorija i distribuirani limitator brzine za sve radnike. Lokalni jednolet i
semafori štite samo jedan JVM. Nikada ne tretirajte `Unavailable` kao `Invalid`; koristiti
`response.error()` za stabilne kodove, mogućnost ponovnog pokušaja i poruke na mađarskom/engleskom.

## Otvoreni kod

Projekt se može koristiti, mijenjati i
može se distribuirati. Licenca dopušta komercijalnu i ekspresnu upotrebu
daje patentnu licencu; moraju se pročitati uvjeti licence, atribucije i modifikacije
čuvati. Prije doprinosa pročitajte [CONTRIBUTING.md](CONTRIBUTING.md)i
[CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)datoteke.

Ovaj projekt je licenciran pod [Apache licencom 2.0](../../../LICENSE), dopuštenom
licenca s izričitim priznanjem patenta. Pogledajte [CONTRIBUTING.md](CONTRIBUTING.md),
[SECURITY.md](SECURITY.md), i detaljne [otvorene bilješke](OPEN_SOURCE.md).

## Podrška i donacije

Podrška zajednice pruža se na temelju najboljeg truda, putem pitanja i rasprava na GitHubu.
Uvijek prijavite sigurnosne pogreške na privatnom kanalu. Održavanje projekta je značajno
uključuje troškove razvoja i infrastrukture; ako je vama, programeru, uštedjelo vrijeme
[možete ga pozvati na kavu](https://buymeacoffee.com/lgx0).

Podrška zajednice je najbolji pokušaj kroz probleme i rasprave na GitHubu. Sigurnost
izvještaji moraju ostati privatni. Ako vam je projekt uštedio vrijeme, možete
[kupite programeru kavu](https://buymeacoffee.com/lgx0).
