# Italiano (`it`) — vies-client — verificatore di partita IVA

**Termini di ricerca:** verifica partita IVA, validatore partita IVA, validazione IVA UE, verifica identificativo fiscale, client Java VIES; `VAT checker`, `VAT number validator`, `EU VAT validation`, `tax ID checker`.

Non è un calcolatore fiscale generico, ma un client per validare gli identificativi IVA dell’UE tramite VIES.

[Selettore lingua](../../LANGUAGES.md) · [Originale inglese](../../../README.md)

> Questa traduzione facilita l’accesso. In caso di divergenza tecnica o legale prevale l’originale inglese. `LICENSE` non viene tradotto: solo il testo inglese ha valore legale.

`vies-client` è un client Java 21+ thread-safe e senza dipendenze runtime per l’API REST VIES della Commissione europea. Funziona con Spring Boot, Quarkus, Micronaut o JDK puro tramite `java.net.http.HttpClient`.

- [Installazione](INSTALLATION.md) · [Integrazione](INTEGRATION.md) · [Modello tecnico](TECHNICAL.md)
- [Test](TESTING.md) · [Open source](OPEN_SOURCE.md) · [Rilascio](RELEASING.md)

```bash
./mvnw clean verify
./mvnw install
```

```java
try (var vies = ViesClient.builder().retries(1).build()) {
    ViesResponse result = vies.check("IT00000000000");
}
```

Usare un’istanza condivisa per applicazione e chiuderla allo shutdown. `check(...)` è sincrono, `checkAsync(...)` restituisce `CompletableFuture<ViesResponse>`. Risultati: `Valid`, `Invalid`, `Unavailable`, `MalformedInput`; gli errori hanno codice stabile, messaggi ungherese/inglese e `retryable`.

Per milioni di attività servono inoltre coda persistente partizionata, cache condivisa, rate limiter distribuito e worker orizzontali. Single-flight, backpressure, limiti, timeout, retry con jitter e thread virtuali proteggono una JVM ma non aumentano la capacità VIES. Contributi e donazioni volontarie tramite Sponsor/caffè sono benvenuti e non attribuiscono diritti né cambiano Apache-2.0.

## Build, JPMS e semantica completa

Maven `vies.client:vies-client:1.0.0`; modulo JPMS `vies.client`, export `vies.client`, require `java.net.http`, internals chiusi. Usare `module my.api { requires vies.client; }` o classpath.

```java
try (var vies = ViesClient.builder().defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER"))).retries(1).build()) {
  switch (vies.check("DE000000000")) {
    case ViesResponse.Valid v -> System.out.println(v.traderName().orElse("-"));
    case ViesResponse.Invalid i -> System.out.println("Non valido");
    case ViesResponse.Unavailable u -> System.out.println(u.errorCode());
    case ViesResponse.MalformedInput m -> System.out.println(m.reason());
  }
}
```

Il requester è la propria partita IVA UE, letta da `MY_EU_VAT_NUMBER` tramite un
segreto o una configurazione attendibile. VIES **può** restituire `consultationNumber`,
ma il campo è facoltativo, mai garantito e il suo valore probatorio legale dipende
dalle norme locali. Percorso: normalize (`GR`→`EL`), cache, single-flight VAT/requester,
admission pending/rete, HTTP riusato con timeout/retry, validazione rigorosa boolean/data, cache solo Valid.

| Esito | HTTP | Retry | Cache | Significato |
|---|---:|---|---|---|
| `Valid` | 200 | no | sì | confermato |
| `Invalid` | 200 | no | no | non confermato |
| `Unavailable` | 503/429 | per codice | no | nessuna decisione |
| `MalformedInput` | 400 | no | no | correggere input |

| Builder | Default | Scopo |
|---|---:|---|
| `baseUrl` | VIES | mock controllato |
| `connectTimeout`/`requestTimeout`/`admissionTimeout` | 5s/8s/2s | tempi |
| `defaultRequester` | nessuno | consultation ID |
| `retries`/`retryDelay` | 0/400ms | 0–5, backoff+jitter |
| `maxConcurrentRequests` | 32 | HTTP reali |
| `maxPendingSyncRequests`/`maxPendingAsyncRequests` | 512/512 | memoria |
| `cacheTtl`/`cacheMaxEntries` | 24h/10.000 | cache locale |
| `cache`/`disableCache` | built-in/— | Redis/nessuna cache persistita; richieste parallele identiche possono condividere un single-flight |
| `userAgent`/`executor` | modulo/virtual threads | identità/esecuzione |

Cache/single-flight JVM-locali; mai `Unavailable` come `Invalid`. Milioni: queue durevole, worker/batch limitati, Redis, limiter globale, DLQ, retry controllati; nessun load pubblico. Errori code/HU/EN/retryable, no PII log. `./mvnw clean verify`, demo JDK, Apache-2.0, security privata, Sponsor senza SLA.

## Riferimento canonico completo

Il modulo richiede Java 21, non ha dipendenze runtime esterne e funziona su classpath o come JPMS `vies.client`. L’applicazione crea una sola istanza thread-safe, configura timeout e limiti nel builder e chiama `close()` allo shutdown. Il requester è la propria partita IVA UE e VIES può restituire un `consultationNumber` opzionale e non garantito; nome, indirizzo e consultation restano opzionali. Il suo valore probatorio legale dipende dalle norme locali. La gerarchia sealed distingue `Valid`, `Invalid`, `Unavailable`, `MalformedInput`. Un’incertezza tecnica non diventa mai `Invalid`.

`disableCache()` disabilita solo la cache memorizzata: chiamate simultanee identiche
VAT+requester possono condividere una richiesta di rete single-flight. Dopo il suo
completamento, una chiamata successiva esegue una nuova richiesta VIES.

Il percorso normalizza paese/numero, converte `GR` in `EL`, valida senza rete, legge cache, applica single-flight su VAT/requester, acquisisce admission e limite HTTP, riusa `HttpClient`, valida JSON/boolean/data e salva soltanto `Valid`. Un secondo cache check dopo leadership chiude la race stale-miss. Follower identici condividono il lavoro ma hanno Future separate e non consumano nuovi pending permit.

Default: connect timeout 5 secondi, request timeout 8, admission 2, 32 HTTP concorrenti, 512 sync pending, 512 async pending, TTL 24 ore, 10.000 entry, zero retry, delay 400 ms. `retries` ammette massimo cinque tentativi aggiuntivi con backoff esponenziale e jitter. Durate negative, zero vietati o overflow nanos/millis sono rifiutati. Un executor custom appartiene al chiamante.

`Valid` e `Invalid` sono decisioni dominio HTTP 200; `MalformedInput` 400; `CLIENT_OVERLOADED` 429; guasto temporaneo 503. L’errore espone code stabile, `messageHu`, `messageEn`, `retryable`. Temporanei: service/member unavailable, busy, timeout, network e HTTP 408/429/5xx secondo mapping. Input, requester, blocked, closed, malformed e unknown richiedono correzione o decisione esplicita.

La cache locale è bounded con TTL, eviction approssimativa/eventuale e nessuna LRU garantita. Redis implementa `ViesCache` con chiave VAT+requester, timeout breve, schema versionato e TLS. Un read failure produce `CACHE_ERROR` per evitare stampede; write failure non cancella Valid. La cache non sostituisce audit persistente con VAT, requester, UTC, esito e consultation opzionale.

Per milioni di job servono queue durevole, batch/consumer limitati, Redis condiviso, rate limiter distribuito, retry scheduler e DLQ. Solo Unavailable retryable viene ripianificato con tentativi bounded, backoff/jitter; Invalid/Malformed acknowledged. Il limite globale non è la somma incontrollata dei pod. Autoscaling considera queue age, budget upstream e salute, non solo CPU. VIES pubblico non riceve load/stress/CI.

Shutdown: fermare ingress, sospendere consumer, grace period, chiudere client e risorse host. Close concorrenti coordinati; leader/follower stesso `CLIENT_CLOSED`; task propri active/queued interrotti, executor esterno aperto. Cleanup permit/inflight prima di complete così callback concatenati vedono capacità. Fatal `Error` completa exceptional e raggiunge uncaught handler.

Sicurezza: HTTPS, certificati normali, base URL non controllabile dai tenant, input normalizzato, output upstream untrusted, log redatti, cache/audit protetti. VAT, società, indirizzo, requester, consultation sono sensibili e non label metriche. Health usa liveness, readiness queue/cache e status VIES parsimonioso, non VAT per probe.

La suite contiene 73 test deterministici: 44 unitari e 29 test loopback HTTP/concorrenza, inclusi single-flight, limiti, cancellazione, rejection, sync/async misto, race della cache, close, callback, executor personalizzato ed Error fatale con latch/barrier, non sleep-oracle. CI esegue `./mvnw --batch-mode --no-transfer-progress clean verify`, archivia Surefire, binary, sources, Javadoc e modulo. Il carico usa solo mock con warm-up, heap/GC, throughput, p50/p95/p99; soak/chaos simulano timeout, 429/5xx, JSON rotto, Redis down, interrupt e shutdown.

Spring Boot usa bean singleton `destroyMethod="close"`; controller restituisce 200/400/429/503 senza stacktrace. JDK puro condivide la stessa istanza. Metriche: result/code/retry/reject, latency total/cache/HTTP, pending/inflight, queue age, DLQ, mai VAT. Release SemVer, tag firmato, checksum e artefatti stesso CI. Apache-2.0; `LICENSE`/`NOTICE` inglesi vincolanti; donazione senza SLA/governance.

La map inflight contiene solo leader distinti ammessi. Admission timeout evita attese infinite. Cleanup async prima di completion impedisce a `thenCompose` di vedere permit ancora occupato o inflight già completato quando cache è off. Submitted task handles sono tracciati per cancellare executor custom. Close coordina cache read/write bloccanti e callback fuori lifecycle lock. L’host limita anche Future esterne, payload, queue e storage.

VIES è federato: latenza, campi, disponibilità e quota cambiano per Stato. Virtual thread riducono costo attesa, non capacità upstream. Staging simula cache down, 429/5xx, timeout, backlog, cancel e shutdown. Criteri: nessun falso Invalid, memoria bounded, limiti rispettati, audit tracciabile e replay DLQ. Campi trader/consultation non sono mai garantiti.

La disponibilità per Stato membro è consultabile con l’endpoint dedicato, ma resta un’indicazione temporanea e non una decisione sulla VAT. Le metriche e i log devono distinguere risposta dominio, errore upstream, overload locale e problema cache. Un aumento improvviso degli Invalid richiede verifica del mapping e campioni sintetici, non esposizione di dati cliente.

Per aggiornare in sicurezza conservare la versione precedente, leggere changelog e release notes, provare il JAR in staging classpath/JPMS e verificare checksum. Le configurazioni sono parte del deployment: default utili non sono una garanzia universale. Il progetto accetta issue e PR secondo le policy; security solo advisory privato e supporto community best effort.
