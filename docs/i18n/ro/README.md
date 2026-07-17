# Română (ro) — vies-client

> [Toate limbile](../../LANGUAGES.md) · Traducere informativă. În caz de diferență prevalează sursa canonică tehnică sau juridică în limba engleză. Numai `LICENSE` și `NOTICE` din rădăcină sunt texte juridice oficiale; traducerea nu le înlocuiește.

[![License: Apache-2.0](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](../../../LICENSE)
[![Java 21+](https://img.shields.io/badge/Java-21%2B-orange.svg)](../../../pom.xml)
[![Buy Me a Coffee](https://img.shields.io/badge/Buy_Me_a_Coffee-support-ffdd00?logo=buymeacoffee&logoColor=000)](https://buymeacoffee.com/lgx0)

🌍 **[Toate limbile oficiale ale Uniunii Europene](../../LANGUAGES.md)**

`vies-client` este un client Java 21+ autonom pentru API-ul REST VIES
(VAT Information Exchange System) al Comisiei Europene. Nu are dependențe externe
la rulare și folosește numai clientul HTTP din JDK (`java.net.http`). Poate fi
integrat în Spring Boot, Quarkus, Micronaut, într-un `HttpServer` JDK simplu sau în
orice altă aplicație Java.

**Termeni de căutare:** verificare VIES, verificare cod TVA, validator de număr TVA,
validare TVA UE și verificare identificator fiscal. Acesta nu este un calculator
fiscal general; scopul său este exclusiv validarea numerelor de TVA din UE prin VIES.

Acesta este un proiect open-source independent. Nu este un produs oficial al
Comisiei Europene, al Uniunii Europene sau al vreunei autorități fiscale naționale
și nu este aprobat ori certificat de aceste instituții.

- **Cerință:** Java **21+** bytecode/API; întregul pachet este verificat pe JDK 21.
- **Documentație oficială VIES:** <https://ec.europa.eu/taxation_customs/vies/#/technical-information>
- **Endpoint de validare:** `https://ec.europa.eu/taxation_customs/vies/rest-api/ms/{countryCode}/vat/{vatNumber}`
- **Disponibilitatea statelor membre:** `https://ec.europa.eu/taxation_customs/vies/rest-api/check-status`

## Documentație

- [Instalare](INSTALLATION.md)
- [Integrare](INTEGRATION.md)
- [Documentație tehnică](TECHNICAL.md)
- [Testare](TESTING.md)
- [Proiect open-source și licență](OPEN_SOURCE.md)
- [Publicarea versiunilor](RELEASING.md)
- [Contribuții](CONTRIBUTING.md)
- [Politica de securitate](SECURITY.md)
- [Codul de conduită](CODE_OF_CONDUCT.md)
- [Suport și donații](SUPPORT.md)
- [Notificări privind componentele terțe](THIRD_PARTY_NOTICES.md)
- [Jurnal de modificări](CHANGELOG.md)

## Construire și conectare

```bash
./mvnw install    # teste + target/vies-client-1.2.0.jar (+ -sources.jar, -javadoc.jar)
               # și instalare în depozitul Maven local
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

**Modul JPMS real.** Jarul se comportă ca un modul numit (`jar --describe-module`):

```
vies.client@1.2.0
exports vies.client
requires java.net.http
contains vies.client.internal   ← pachet intern neexportat
```

Conectați-vă de la o aplicație modularizată ca aceasta:

```java
module my.api.server {
    requires vies.client;
}
```

De la Classpath (într-un proiect nemodularizat, „tradițional”) funcționează în același mod -
`module-info` pur și simplu nu joacă în acest caz. Chiar și fără Maven/Gradle
poate fi folosit: arborele sursă de sub `src/main/java` poate fi copiat în propriul proiect
(Lăsați `module-info.java` dacă proiectul dvs. nu este modularizat).

## Exemplu rapid

```java
import vies.client.*;

try (var vies = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER"))) // secret/configurație de încredere
        .retries(1)
        .build()) {

    // Acceptă cratime, spații și litere mici; mapează GR la EL.
    switch (vies.check("DE 000 000 000")) {
        case ViesResponse.Valid v ->
            System.out.println("Valid: " + v.traderName().orElse("(denumire nepublică)")
                    + " — număr de consultare: " + v.consultationNumber().orElse("-"));
        case ViesResponse.Invalid i ->
            System.out.println("Cod TVA nevalid: " + i.vatNumber());
        case ViesResponse.Unavailable u ->
            System.out.println("VIES nu este disponibil momentan (" + u.errorCode() + "), reîncercați mai târziu");
        case ViesResponse.MalformedInput m ->
            System.out.println("Date de intrare incorecte: " + m.reason());
    }
}
```

`switch` este exhaustiv: compilatorul garantează că ați gestionat toate cele patru rezultate
(interfață sigilată + potrivire model).

### De ce este important `defaultRequester`?

`defaultRequester` trebuie să fie propriul cod de TVA UE al organizației, citit dintr-un
secret sau o configurație de încredere. Pentru un răspuns valid, VIES **poate**
returna `consultationNumber`; câmpul este opțional și nu este niciodată garantat.
Valoarea sa probatorie juridică depinde de regulile locale; păstrați-l împreună cu
data și datele de intrare ale interogării.

## Conexiune la serverul API Spring Boot

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

Clientul este imuabil și sigur pentru fire — aplicația creează o singură instanță
ciclu de viață (singleton bean) și se închide la oprire (`close`).

Model fără cadru: [`examples/ViesDemoServer.java`](../../../examples/ViesDemoServer.java)
(JDK simplu `HttpServer`, pe fire virtuale):

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java     # port 8085
curl "http://localhost:8085/vat-check?number=HU00000000"
```

## Utilizare asincronă

```java
CompletableFuture<ViesResponse> future = vies.checkAsync("PL0000000000");
```

În mod implicit, apelurile asincrone rulează pe ** fire virtuale ** (Project Loom) - o mulțime
nu există nicio risipă de fir platformă în verificarea paralelă. Executor propriu a
poate fi specificat în builder (`executor(...)`— nu este închis de client).

Până la 512 **acțiuni individuale de lider asincron** din baza de clienți asincron
primite per instanță client. O singură citire cache consumă spațiu pentru o perioadă scurtă de timp;
intrarea defectuoasă și același urmăritor pentru un singur zbor nu consumă spațiu separat. În caz de supraîncărcare, solicitarea individuală nouă imediat
Obțineți un rezultat pentru `Unavailable(..., "CLIENT_OVERLOADED")`. Acesta este in-client
restricționează munca; coada de intrare și viitoarele stocate de apelant
trebuie să fie limitată.

## Uzină de interogări de mai multe milioane

Pentru milioane de utilizatori, nu porniți milioane de futures într-un singur JVM și nu
permite trafic direct nelimitat către VIES. Structura propusa:

> Clientul poate fi o componentă de procesare a unui milion de coadă de lucru, dar real
> Transmisia VIES este limitată de limitele în schimbare, negarantate ale sistemelor UE și ale statelor membre
> fi determinat. Firele virtuale reduc costul de așteptare; cele din amonte
> capacitatea nu este crescută.

1. Verificările primite sunt primite de o coadă de mesaje persistentă, partiționată.
2. Câțiva lucrători la scară orizontală consumă linia în porțiuni limitate.
3. Un singleton `ViesClient` funcționează cu fire virtuale per lucrător.
4. Lucrătorii folosesc un cache Redis comun prin interfața `ViesCache`.
5. Limitatorul global/distribuit de rată protejează punctele finale VIES ale UE și ale statelor membre.

În cadrul unui JVM, clientul fuzionează același, ajungând în același timp
perechi număr de taxă/interogare (single-flight), deci o pierdere de cache nu o cauzează
„cererea de fugă”.`maxConcurrentRequests` este limitat de rețeaua reală
cereri și `maxPendingAsyncRequests` și `maxPendingSyncRequests` în memorie
oferă contrapresiune instantanee. Toate sunt JVM-locale. Trafic agregat al mai multor muncitori
trebuie reglat cu un limitator comun, distribuit. Coada de intrare persistentă și
grupul de consumatori ar trebui să fie limitat de aplicație.

Exemplu de muncitor cu sarcini grele:

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

Rezultatul `Unavailable`— inclusiv valoarea `CLIENT_OVERLOADED`— este persistent
trebuie să reîncercați cu o întârziere în coadă.`MalformedInput` și`Invalid` nu trebuie reîncercate.

## Calea unei cereri/ciclului de viață al cererii

1. **Normalizare/Normalizare:** verificați codul țării și formatați fără rețea.
2. **Cache:** Revenirea imediată a rezultatelor valide, încă vii.
3. **Single-flight:** perechi identice de cod fiscal+interogare sunt îmbinate într-un singur JVM.
4. **Admitere:** limitele asincrone și de rețea protejează memoria și VIES.
5. **HTTP:** conexiune JDK `HttpClient` reutilizată cu un timeout scurt.
6. **Verificare validare / răspuns:** boolean incomplet sau dată →`MALFORMED_RESPONSE`.
7. **Scrie în cache:** numai rezultatul `Valid` autentic este stocat în cache.

## Răspunsuri de eroare bilingve

Codul de eroare al mașinii este întotdeauna stabil și independent de limbă.`error()` este maghiară și engleză
oferă text utilizatorului și o decizie de reîncercare:

```java
var response = vies.check("HU00000000");
response.error().ifPresent(error -> {
    log.warn("{} | {} | retry={}", error.messageHu(), error.messageEn(), error.retryable());
});
```

| Rezultat         |        HTTP |           Reîncercați | Cache | Înțeles                                                  |
| ---------------- | ----------: | --------------------: | ----: | -------------------------------------------------------- |
| `Valid`          |         200 |                    nu | da/da | VIES confirmat ca valabil / VIES confirmat valid         |
| `Invalid`        |         200 |                    nu |    nu | VIES nu a confirmat ca valid / VIES nu a confirmat valid |
| `Unavailable`    | 503 sau 429 | majoritatea/de obicei |    nu | Nu a fost luată nicio decizie de valabilitate            |
| `MalformedInput` |         400 |                    nu |    nu | Intrarea trebuie corectată / Intrarea trebuie corectată  |

## Configurație (generator)

| Setare                            | Valoarea de bază      | Ce înseamnă                                                                                         |
| --------------------------------- | --------------------- | --------------------------------------------------------------------------------------------------- |
| `baseUrl(String)`                 | URL oficial VIES REST | Redirecționare în test simulat                                                                      |
| `connectTimeout(Duration)`        | 5 s                   | Timeout conexiune TCP/TLS                                                                           |
| `requestTimeout(Duration)`        | 8 s                   | Timp de expirare total al cererii (VIES este interactiv, păstrați-l scurt)                          |
| `admissionTimeout(Duration)`      | 2 s                   | Acesta este cât timp așteaptă spațiul liber în rețea                                                |
| `defaultRequester(ViesRequester)` | nu exista             | Număr fiscal comunitar propriu → ID consultație                                                     |
| `retries(int)`                    | 0                     | Reîncercare automată pentru erori tranzitorii (0-5, backoff exponențial + jitter)                   |
| `retryDelay(Duration)`            | 400 ms                | Retragere exponențială implicită                                                                    |
| `maxConcurrentRequests(int)`      | 32                    | Limita superioară a cererilor concurente ale rețelei VIES reale                                     |
| `maxPendingSyncRequests(int)`     | 512                   | Limită de memorie pentru apelanții sincronizați simultan; deasupra lui `CLIENT_OVERLOADED`          |
| `maxPendingAsyncRequests(int)`    | 512                   | Limită de memorie pentru operațiuni asincrone active/în așteptare; deasupra lui `CLIENT_OVERLOADED` |
| `cacheTtl(Duration)`              | 24 de ore             | Timp de cache pentru accesări valide                                                                |
| `cacheMaxEntries(int)`            | 10.000                | Limită de dimensiune a memoriei cache a memoriei încorporate                                        |
| `cache(ViesCache)`                | construit în          | Cache-backend propriu (de exemplu, Redis)                                                           |
| `disableCache()`                  | —                     | Fără cache stocat; apelurile paralele identice pot partaja o singură cerere single-flight            |
| `userAgent(String)`               | modul-id              | Ar trebui să vă identificați în UE                                                                  |
| `executor(ExecutorService)`       | fir virtual/sarcină   | Executorul meu asincron; apelantul este responsabil pentru ciclul său de viață                      |

### Cache propriu (de ex. Redis)

```java
class RedisViesCache implements ViesCache {
    public Optional<ViesResponse.Valid> get(String key) { /* ... */ }
    public void put(String key, ViesResponse.Valid value, Duration ttl) { /* ... */ }
}

var vies = ViesClient.builder().cache(new RedisViesCache()).build();
```

Numai rezultatul `Valid` este stocat în cache — număr nevalid și eroare tranzitorie niciodată.
Pentru adaptorul Redis, utilizați cache-timeout scurt, spațiu de nume versiuneat și propriu
metrica de eroare. Eroare de citire a memoriei cache `CACHE_ERROR` rezultă atunci când Redis este oprit
nu începe o fugă necontrolată de solicitare VIES.

`consultationNumber` și `requestDate` returnate din cache aparțin verificării
**inițiale**; accesarea cache-ului nu este o nouă verificare VIES. `disableCache()`
dezactivează numai cache-ul stocat: apelurile simultane identice VAT+requester pot
partaja o singură cerere de rețea single-flight, iar un apel ulterior după finalizare
face o cerere nouă. `consultationNumber` rămâne opțional.

## Semantică care merită cunoscută

1. **`Unavailable`≠`Invalid`.** Sistemele de fundal ale statelor membre în mod regulat
   sunt abandonate (`MS_UNAVAILABLE`,`MS_MAX_CONCURRENT_REQ`...). În acest caz, numărul a
   **nu este judecat de VIES** - interzis din punct de vedere comercial pentru a fi considerat invalid.
2. **Prefiltrarea formatului nu este autentificare VIES.** Modulul filtrează cele înregistrate
   introducere greșită (`MalformedInput`) înainte de a merge la rețea, dar valabilitatea
   sursa sa este întotdeauna răspunsul VIES.
3. **Reîncercarea crește și sarcina.** Încercați doar de câteva ori o eroare temporară local
   din nou; într-o operațiune mare, mecanismul de reîncercare întârziată al cozii permanente este cel primar.
4. **Grecia `EL`, Irlanda de Nord `XI`.** Modulul se ocupă de ambele,`GR`
   mapează automat intrarea la `EL`.

## Teste

```bash
./mvnw test    # teste unitare + HTTP local, concurență, reîncercare, backpressure și ciclu de viață
```

---

## Pornire rapidă în engleză

Client Java 21+ cu dependență zero pentru numărul de TVA VIES al Comisiei UE
validare REST API. Construiți cu `./mvnw package`, apoi:

```java
try (var vies = ViesClient.createDefault()) {
    if (vies.check("DE000000000") instanceof ViesResponse.Valid v) {
        System.out.println(v.traderName().orElse("?"));
    }
}
```

Rezultatele sunt o ierarhie sigilată (`Valid`/`Invalid`/`Unavailable`/
`MalformedInput`) concepute pentru potrivirea completă a modelelor`switch`. Valabil
rezultatele sunt stocate în cache în memorie timp de 24 de ore; întreruperile tranzitorii VIES sunt raportate ca
`Unavailable`, niciodată ca`Invalid`. Documentație oficială API:
<https://ec.europa.eu/taxation_customs/vies/#/technical-information>.

### Operare la scară mare

Acest client poate fi o componentă a unei conducte de procesare de un milion de articole, dar
Debitul real VIES este limitat de variabile, negarantate UE și statul membru
limite. Firele virtuale reduc costurile de blocare; nu cresc în amonte
capacitate. Utilizați o coadă partiționată durabilă, consumatori delimitați, un Redis partajat
cache și un limitator de rată distribuit pentru toți lucrătorii. Zbor unic local și
semaforele protejează doar un JVM. Nu tratați niciodată `Unavailable` ca `Invalid`; utilizare
`response.error()` pentru coduri stabile, reîncercare și mesaje în limba maghiară/engleză.

## Cod sursă deschis

Proiectul poate fi folosit, modificat și
pot fi distribuite. Licența permite utilizarea comercială și expresă
acordă o licență de brevet; trebuie cititi termenii de licenta, atribuire si modificare
a păzi. Înainte de a contribui, citiți [CONTRIBUTING.md](CONTRIBUTING.md)și
[CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)fișiere.

Acest proiect este licențiat sub [Licența Apache 2.0](../../../LICENSE), un permisiv
licență cu acordare explicită de brevet. A se vedea [CONTRIBUTING.md](CONTRIBUTING.md),
[SECURITY.md](SECURITY.md), și detaliile [note open-source](OPEN_SOURCE.md).

## Sprijin și donații

Asistența comunității este oferită pe baza celor mai bune eforturi, prin probleme și discuții GitHub.
Raportați întotdeauna erorile de securitate pe un canal privat. Întreținerea proiectului este semnificativă
implică costuri de dezvoltare și infrastructură; dacă te-a salvat pe tine, dezvoltatorul, timp
[îl poți invita la o cafea](https://buymeacoffee.com/lgx0).

Asistența comunității este cel mai bun efort prin problemele și discuțiile GitHub. Securitate
rapoartele trebuie să rămână private. Dacă proiectul v-a economisit timp, puteți
[cumpărați dezvoltatorului o cafea](https://buymeacoffee.com/lgx0).
