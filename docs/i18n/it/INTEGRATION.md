# Italiano (`it`) — Integrazione

[Selettore lingua](../../LANGUAGES.md) · [Originale inglese](../../INTEGRATION.md)

> Prevale l’originale inglese; `LICENSE` non viene tradotto.

Creare un singleton `ViesClient` per processo e chiamare `close()` allo shutdown. È thread-safe; non crearne uno per richiesta.

```java
var client = ViesClient.builder()
 .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
 .maxConcurrentRequests(32).maxPendingSyncRequests(512)
 .maxPendingAsyncRequests(512).admissionTimeout(Duration.ofSeconds(2))
 .retries(2).build();
```

Gestire `Valid`, `Invalid`, `Unavailable`, `MalformedInput`. Non ritentare `Invalid`/`MalformedInput`; per `Unavailable` rispettare `error().retryable()`. HTTP consigliato: 200, 400, 429 per `CLIENT_OVERLOADED`, 503 per guasti temporanei; restituire `errorCode`, `messageHu`, `messageEn`, `retryable`.

In Spring Boot registrare `@Bean(destroyMethod = "close")`; nel JDK riusare il client tra handler. Caricare `MY_EU_VAT_NUMBER` da un segreto o una configurazione attendibile. VIES può restituire un `consultationNumber`, ma è facoltativo, mai garantito e il suo valore probatorio legale dipende dalle norme locali; conservarlo con data, VAT e risultato secondo audit/privacy.

Con più pod implementare `ViesCache` su Redis con TTL e chiave VAT+requester. Single-flight e semafori sono locali: usare coda durevole, consumer limitati, rate limiter distribuito e DLQ. Limitare future trattenuti. Monitorare latenza, cache, retry e rifiuti; non loggare VAT complete e non fare load test sul VIES pubblico.

## Contratto e operatività completi

`check` restituisce `ViesResponse`, `checkAsync` un `CompletableFuture`; cancellation follower non distrugge shared work, cache bloccante gira nel worker limitato.

| Risposta | HTTP | Retry | Interpretazione |
|---|---:|---|---|
| Valid/Invalid | 200 | no | decisione |
| Malformed | 400 | no | correggere |
| Overloaded | 429 | ritardato | backpressure |
| Unavailable retryable | 503 | ritardato | nessuna decisione |

Spring: bean close, properties e controller strutturato. JDK: client comune `HttpServer`. Redis: VAT+requester, TTL, schema versionato sicuro, timeout breve; read→CACHE_ERROR, write non cancella Valid. Queue/DLQ solo retryable, tentativi limitati, backoff+jitter; ack Invalid/Malformed. N pod richiedono limiter distribuito.

Health liveness, readiness queue/cache, check-status parsimonioso; metriche latency/result/code/retry/hit/pending/inflight/reject senza VAT. Evitare client/request, risorse illimitate, retry loop, no TTL, Unavailable→Invalid, load pubblico, PII log, close dimenticato. Production: shutdown, capacità, audit/privacy, alert, DLQ, failover, rollback.

## Riferimento Spring Boot

```java
@Configuration class ViesConfig {
 @Bean(destroyMethod="close") ViesClient client(){ return ViesClient.builder().retries(1).build(); }
}
@RestController class VatController {
 private final ViesClient vies;
 VatController(ViesClient vies){this.vies=vies;}
 @GetMapping("/api/vat/{number}") ResponseEntity<?> check(@PathVariable String number){
  return switch(vies.check(number)){
   case ViesResponse.Valid v -> ResponseEntity.ok(Map.of("valid",true));
   case ViesResponse.Invalid i -> ResponseEntity.ok(Map.of("valid",false));
   case ViesResponse.MalformedInput m -> ResponseEntity.badRequest().body(m.error());
   case ViesResponse.Unavailable u -> ResponseEntity.status("CLIENT_OVERLOADED".equals(u.errorCode())?429:503).body(u.error());
  }; }
}
```

Smoke JDK: `./mvnw -q package`, `java -cp target/classes examples/ViesDemoServer.java`, `curl "http://localhost:8085/vat-check?number=IT00000000000"`.

## Riferimento canonico completo

Il modulo richiede Java 21, non ha dipendenze runtime esterne e funziona su classpath o come JPMS `vies.client`. L’applicazione crea una sola istanza thread-safe, configura timeout e limiti nel builder e chiama `close()` allo shutdown. Il requester è la propria partita IVA UE e VIES può restituire un `consultationNumber` opzionale e non garantito; nome, indirizzo e consultation restano opzionali. La gerarchia sealed distingue `Valid`, `Invalid`, `Unavailable`, `MalformedInput`. Un’incertezza tecnica non diventa mai `Invalid`.

Il percorso normalizza paese/numero, converte `GR` in `EL`, valida senza rete, legge cache, applica single-flight su VAT/requester, acquisisce admission e limite HTTP, riusa `HttpClient`, valida JSON/boolean/data e salva soltanto `Valid`. Un secondo cache check dopo leadership chiude la race stale-miss. Follower identici condividono il lavoro ma hanno Future separate e non consumano nuovi pending permit. `disableCache()` disabilita solo la cache memorizzata: chiamate simultanee identiche possono condividere una richiesta single-flight; dopo il completamento, una chiamata successiva esegue una nuova richiesta.

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

L’API host applica autenticazione, quota tenant, limite input e redazione prima del client. I timeout di proxy/controller sono coordinati col request timeout. Gli stati persistiti distinguono pending, valid, invalid, retryable, permanent e dead-letter; un nuovo errore tecnico non cancella una decisione storica.
