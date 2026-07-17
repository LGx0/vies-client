# Română (ro) — Integrare

> [Toate limbile](../../LANGUAGES.md) · Traducere informativă. În caz de diferență prevalează sursa canonică tehnică sau juridică în limba engleză. Numai `LICENSE` și `NOTICE` din rădăcină sunt texte juridice oficiale; traducerea nu le înlocuiește.

## 1. Ciclul de viață

Utilizați un singur obiect `ViesClient` într-o aplicație sau o instanță de lucru.
Nu creați un client pe cerere HTTP: veți pierde pool-ul de conexiuni, memoria cache,
fuziunea unui singur zbor și limitele locale.

Utilizați un `ViesClient` per aplicație/proces de lucru. Nu creați un client per
cerere HTTP; procedând astfel, elimină gruparea conexiunilor, memoria cache, zborul unic și local
limite.

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

O nouă operațiune nu poate fi începută după `close()`. Oprirea întrerupe sistemul intern activ
operațiuni și returnează `CLIENT_CLOSED` la cererile comune în proces.
Metodele de sincronizare și asincrone numite direct ulterior sunt ambele sincrone
Se aruncă o excepție `IllegalStateException`.

După `close()`, nu este acceptată nicio lucrare nouă. Oprirea întrerupe activitatea internă activă
și completează solicitările partajate în timpul zborului cu `CLIENT_CLOSED`.
Noile apeluri API sincronizate și asincrone făcute ulterior ambele aruncă `IllegalStateException`
în mod sincron.

## 2. API sincron

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

Numărul de apelanți API sincron este, de asemenea, limitat (`maxPendingSyncRequests`). La limită
cererea validă de mai sus primește imediat rezultatul `CLIENT_OVERLOADED`.

Apelanții sincroni sunt, de asemenea, limitați de admitere de `maxPendingSyncRequests`.

## 3. API asincron

```java
client.checkAsync("PL0000000000")
        .thenAccept(response -> {
            // Handle all four response variants / Kezeld mind a négy eredményt.
        });
```

API-ul asincron rulează pe un fir virtual cu executorul implicit; propriul executor
se aplică regulile sale de programare. Număr de taxă identic + cererile de anchetă sunt una
se conectează la un viitor comun comun. Apelantul primește o copie dovedită:
anularea unui singur consumator nu întrerupe cererea comună a celorlalţi.

Cu executorul implicit, API-ul asincron rulează pe fire virtuale; un executor personalizat
folosește propria politică de programare. Apeluri identice cu TVA/solicitant se alătură unuia intern
viitor comun. Fiecare apelant primește o copie sigură pentru anulare.

Nu stocați milioane de futures în memorie. Consumatorul de coadă persistentă este întotdeauna limitat
menține fereastra activă.

Nu păstrați milioane de futures. Un consumator de coadă durabilă trebuie să păstreze o limită
fereastra de procesare.

## 4. Contract API HTTP

Mapare recomandată:

| ViesResponse                     | HTTP |            Reîncercați | Notă / Notă                             |
| -------------------------------- | ---: | ---------------------: | --------------------------------------- |
| `Valid`                          |  200 |                     nu | rezultat domeniul                       |
| `Invalid`                        |  200 |                     nu | rezultatul domeniului, nu o eroare HTTP |
| `MalformedInput`                 |  400 |                     nu | apelantul trebuie să repare intrarea    |
| `Unavailable(CLIENT_OVERLOADED)` |  429 |              întârziat | contrapresiune locală                   |
| alte `Unavailable`               |  503 | de `error.retryable()` | nici o decizie de valabilitate          |

Exemplu de eroare JSON / Exemplu de eroare JSON:

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

Text utilizator `messageHu`/`messageEn`. Pentru logică, metrică și logica client
utilizați întotdeauna valoarea stabilă `errorCode`.

`messageHu`/`messageEn`sunt orientate către utilizator. Utilizați valori`errorCode`stabile pentru jurnalele,
metrici și logica clientului.

## 5. Spring Boot

### Configurare

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

### Controler

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

Biblioteca nu are dependențe Spring; codul de mai sus face parte din aplicația consumatoare.
Biblioteca nu are dependență de Spring; acest adaptor aparține aplicației consumatoare.

## 6. Server simplu JDK HTTP

Exemplul executabil complet este `examples/ViesDemoServer.java`.
Exemplu rulabil complet:`examples/ViesDemoServer.java`.

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java 8085
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Exemplul utilizează fire virtuale, stări HTTP reale și răspunsuri de eroare bilingve.
Exemplul folosește fire virtuale, stări HTTP semnificative și erori bilingve.

## 7. Solicitant și identificator de consultare

```java
var client = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
        .build();
```

Solicitantul trebuie să fie propriul cod de TVA UE al organizației, încărcat prin
`MY_EU_VAT_NUMBER` dintr-un secret sau o configurație de încredere. Pentru un răspuns
valid, VIES **poate** returna `requestIdentifier`/`consultationNumber`, dar câmpul
este opțional, nu este garantat, iar valoarea probatorie depinde de regulile locale.

La o accesare a cache-ului, identificatorul și `requestDate` aparțin verificării inițiale.
`disableCache()` dezactivează numai cache-ul stocat: apelurile simultane identice
VAT+requester pot partaja o cerere de rețea single-flight; un apel ulterior după
finalizare face o cerere nouă.

## 8. Adaptor pentru cache Redis

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

Cerințe / Cerințe:

- implementare thread-safe;
- timeout scurt pentru conexiune/comandă;
- spatiu de nume chei versionate;
- metrici și alerte de sănătate;
- fără reîncercare internă nelimitată;
- serializarea corectă a `requestDate`, opționale și `fromCache`.

Excepția de citire determină rezultatul `CACHE_ERROR` și nu declanșează fallback VIES.
O excepție de scriere nu șterge rezultatul `Valid` autentic deja primit.

O excepție de citire returnează `CACHE_ERROR` fără o alternativă VIES. O excepție de scriere
nu șterge un rezultat `Valid` autorizat.

## 9. Reîncercați, coadă și DLQ

În procesarea la scară mare:

1. persistă jobul înainte de procesare;
2. folosiți o cheie de idempotenta;
3. apelați VIES cu un număr mic de reîncercări locale;
4. dacă `error.retryable()`, programați reîncercarea întârziată cu întârziere exponențială;
5. opriți după un maxim configurat și treceți la DLQ/revizuire manuală;
6. nu reîncercați niciodată `Invalid` sau `MalformedInput` neschimbat.

Nu păstrați reîncercarea durabilă în memorie cu lanțul `CompletableFuture`.
Nu implementați reîncercări durabile ca lanțuri `CompletableFuture` în memorie.

## 10. Mai multe noduri sau poduri

Fiecare replică are propriul pool de conexiuni, cache, tabel cu un singur zbor și semafor
obţine. Prin urmare,`maxConcurrentRequests(32)` este **nu este global 32**.

Fiecare replica are propriul pool de conexiuni, cache, tabel cu un singur zbor și
semafoare. Prin urmare,`maxConcurrentRequests(32)`**nu este o limită globală de 32**.

Componente necesare la scară mare / Necesare la scară:

- coadă partiționată durabilă;
- număr de consumatori delimitat/fereastră;
- limitator de rată distribuit/global;
- cache partajat acolo unde semantica afacerii o permite;
- reîncercare întârziată și DLQ;
- idempotenta/deduplicare;
- scalare automată bazată pe vârsta cozii, nu concurență directă nelimitată VIES.

## 11. Sănătatea și observabilitatea

- Vietatea nu trebuie să depindă de VIES.
- `availability()` ar trebui să fie un diagnostic / sondaj rar, stocat în cache și să îl memoreze cu moderație.
- Merd a p50/p95/p99 latency-t/Measure p50/p95/p99 latency.
- Numărați după tipul de rezultat și `errorCode`/ Numărați după rezultat și codul de eroare.
- Măsurați accesul în cache, reîncercați, supraîncărcare, vârsta cozii și DLQ.
- Măsurați accesările în cache, reîncercările, supraîncărcările, vârsta cozii și dimensiunea DLQ.
- Mascați TVA/numele/adresa în jurnale.

## 12. Anti-modeluri

- client la cerere / client nou la cerere;
- viitor nelimitat sau sincronizare apelanți
- tratarea unui limitator per-pod ca global / vedea un limitator local ca global;
- reîncercați imediat fiecare eroare
- conversia `Unavailable` în `Invalid`;
- tratarea datelor de consultare stocate în cache ca pe o nouă dovadă;
- apelarea VIES live din controale de viabilitate sau teste unitare implicite.

## 13. Lista de verificare a producției

- [ ] Ciclul de viață și oprirea Singleton sunt configurate.
- [ ] Toate cele patru variante `ViesResponse` sunt tratate explicit.
- [ ] API returnează cod stabil plus mesaje HU/EN.
- [ ] Limitele de sincronizare, asincronizare, intrare și rețea sunt limitate.
- [ ] Traficul cu mai multe noduri folosește un limitator global/distribuit.
- [ ] Politica privind prospețimea memoriei cache și consultările sunt aprobate.
- [ ] Există reîncercare întârziată, încercări maxime, idempotenta și DLQ.
- [ ] Există valori, alerte și jurnalele mascate.
- [ ] Vietatea este independentă de VIES.
- [ ] Sunt definite teste de unitate, integrare locală, concurență, încărcare, absorbție și eșec.
