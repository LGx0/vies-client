# Italiano (`it`) — Modello tecnico

[Selettore lingua](../../LANGUAGES.md) · [Originale inglese](../../TECHNICAL.md)

> In caso di divergenza vale l’inglese. `LICENSE` resta invariato e vincolante.

Il modulo JPMS `vies.client` esporta solo `vies.client`, incapsula gli interni e non ha dipendenze runtime oltre JDK. `ViesResponse` è sealed: `Valid`, `Invalid`, `Unavailable`, `MalformedInput`; `ViesError` contiene codice stabile, testi HU/EN e retry.

Flusso: normalizzazione/validazione; lettura cache; single-flight per VAT/requester; admission pending e semaforo HTTP; `HttpClient` riusato con timeout/retry limitato; validazione JSON rigorosa; cache TTL solo dei `Valid` affidabili e cleanup prima della completion.

Async usa thread virtuali. Solo leader unici consumano il pending permit; follower uguali no. Sync, async e rete hanno limiti distinti e rilasciano risorse in successo, rifiuto, cancellazione, errore e chiusura. Solo errori temporanei sono ritentati con backoff+jitter. Errore cache read → `CACHE_ERROR` senza stampede; errore write non cancella un risultato valido.

`close()` blocca nuova admission, completa leader/follower coerentemente, interrompe task propri e non chiude executor esterni. Su larga scala: API → coda persistente → worker limitati → cache condivisa + limiter distribuito → VIES. Misurare durata, esito, errore, retry, hit e rifiuti senza VAT nelle label. Proteggere TLS/log; benchmark locali non sono SLA VIES.

## Invarianti e limiti

| Tipo | Decisione | Retry | Cache |
|---|---|---|---|
| Valid | confermata | no | sì |
| Invalid | non confermata | no | no |
| Unavailable | nessuna | codice | no |
| Malformed | input errato | no | no |

Valid contiene VAT, optional, timestamp e cache-origin; placeholder vuoto. Codici ignoti preservati senza retry default. Dopo miss si ricontrollano leadership/cache. ConcurrentHashMap in-flight, semafori sync/async/HTTP, cleanup prima complete; fatal Error exceptional e rilanciato.

Retry per timeout/busy/MS/network e HTTP 408/429/5xx, non input/close/local overload. Backoff+jitter, overflow validato. TTL cache limitata ma approssimativa/eventuale, non LRU; read failure evita stampede, write failure conserva Valid, solo Valid memorizzato. Close serializza, CLIENT_CLOSED uguale, interrompe task propri, executor esterno aperto, callback fuori lock.

Loopback JDK21 non SLA: hit 8,91M/s, format 9,02M/s, HTTP 4.044/s. Misurare warm-up/p50/p95/p99. HTTPS, URL normalizzata, niente secret/PII, cache sensibile, response untrusted rigorosa.

## Codici errore pubblici

| Famiglia | Trattamento |
|---|---|
| `SERVICE_UNAVAILABLE`, `MS_UNAVAILABLE`, `SERVER_BUSY`, `TIMEOUT` | upstream temporaneo, retry limitato |
| `GLOBAL_MAX_CONCURRENT_REQ`, `MS_MAX_CONCURRENT_REQ`, HTTP `408`/`429`/`5xx` | throttling/server, retry ritardato |
| `NETWORK_ERROR` | DNS/TCP/TLS, backoff |
| `CLIENT_OVERLOADED` | admission piena, requeue |
| `CLIENT_CLOSED`, `INTERRUPTED` | lifecycle/cancel, nessuna decisione |
| `INVALID_INPUT`, `INVALID_VAT_FORMAT`, `INVALID_REQUESTER_INFO` | input permanente |
| `IP_BLOCKED`, `VAT_BLOCKED` | policy VIES, review manuale |
| `MALFORMED_RESPONSE` | schema/data/boolean invalido |
| `CACHE_ERROR` | cache guasta, no stampede |
| `INTERNAL_ERROR`, ignoto | nessuna decisione, no retry default |

## Riferimento canonico completo

Il modulo richiede Java 21, non ha dipendenze runtime esterne e funziona su classpath o come JPMS `vies.client`. L’applicazione crea una sola istanza thread-safe, configura timeout e limiti nel builder e chiama `close()` allo shutdown. Il requester è la propria partita IVA UE e può produrre `consultationNumber`; nome, indirizzo e consultation restano opzionali. La gerarchia sealed distingue `Valid`, `Invalid`, `Unavailable`, `MalformedInput`. Un’incertezza tecnica non diventa mai `Invalid`.

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

Il single-flight vale solo nell’istanza e nella finestra inflight, non tra JVM o dopo cleanup con cache disabilitata. I limiti proteggono risorse ma non promettono fairness o throughput. Una implementazione custom di cache/executor deve essere thread-safe e bounded. L’host non espone `baseUrl` ai tenant per evitare SSRF.
