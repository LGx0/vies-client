# Italiano (`it`) — Test

[Selettore lingua](../../LANGUAGES.md) · [Originale inglese](../../TESTING.md)

> Prevale la documentazione inglese. `LICENSE` non è tradotto.

La suite deterministica attuale contiene **73 test**: 44 unit test e 29 test locali HTTP/integrazione/concorrenza.

Unit test: formati, requester, risultati/errori, availability, JSON, cache TTL, builder. Test locali HTTP/concorrenza/lifecycle: `HttpServer`, latch ed executor controllati, senza chiamate VIES.

```bash
./mvnw test
./mvnw clean verify
./mvnw -Dtest=VatFormatTest test
./mvnw -Dtest=ViesClientHttpTest test
```

La suite verifica retry, timeout, malformed response, cache, single-flight, backpressure sync/async, rilascio permit, executor rejection, cancellazione e race `close()`, inclusa coerenza leader/follower. Usare barriere/latch, non `Thread.sleep(...)`; ogni race corretta richiede test deterministico.

Smoke live manuale e minimo. Load solo contro mock controllato con warm-up, concorrenza fissa, throughput, p50/p95/p99. Soak con heap limitato e monitoraggio di thread/code/connessioni. Chaos con lentezza, timeout, JSON rotto, cache guasta, rejection e shutdown. CI: `./mvnw clean verify` su JDK 21, report/artefatti, analisi e scansioni; release bloccata in caso di errore.

## Catalogo completo

Unit: VatFormat 8 (normalize, separatori, GR→EL, invalidi, 28 codici), Requester 4, Mapping 11 (GET/POST, placeholder, decisioni/errori, object/boolean/date strict), Error 6, Availability 2, MiniJson 4, TtlCache 6, Builder 3.

| IDs | Caso locale |
|---|---|
| I-01/I-02 | retry 503→success; cache error→0 HTTP |
| C-01–C-04 | single-flight, HTTP cap, async backpressure, cancel permit |
| C-05–C-09 | callback close, admission timeout, chain cleanup, cache-close |
| C-10–C-13 | custom executor, close uguale, 100 follower, rejection cleanup |
| C-14–C-17 | mixed flight, cache recheck, write-close, unique chain |
| C-18–C-22 | queued cancel, blocked callback, reverse, sync cap, fatal Error |

```bash
./mvnw --batch-mode --no-transfer-progress clean test
./mvnw --batch-mode --no-transfer-progress clean verify
./mvnw -Dtest=VatFormatTest test
./mvnw -Dtest=ViesClientHttpTest test
```

Suite offline/deterministica. Live 1–2 call manuali, mai CI/load. Load mock loopback con warm-up, concurrency fissa, heap/GC, throughput, p50/p95/p99. Soak ore con heap limitato; chaos lentezza/timeout/JSON/cache/rejection/shutdown. Ogni fix ha test che fallisce senza fix; latch/barrier, non sleep-oracle.

### Casi C-01…C-22

| ID | Attesa |
|---|---|
| C-01 | 200 async uguali → un HTTP |
| C-02 | HTTP attivi sotto limite |
| C-03 | overload → `CLIENT_OVERLOADED` |
| C-04 | cancel libera permit |
| C-05 | close callback senza deadlock |
| C-06 | admission timeout limita attesa |
| C-07 | chain vede cleanup |
| C-08/C-09 | cache-close async/sync → closed, 0 HTTP |
| C-10 | custom attivo interrotto, executor aperto |
| C-11 | leader/follower sync uguali |
| C-12 | 100 follower senza permit extra |
| C-13 | rejection pulisce stato |
| C-14 | sync→async un HTTP |
| C-15 | cache recheck evita HTTP |
| C-16 | cache-write close coerente |
| C-17 | unique chain libera permit |
| C-18 | custom queued cancellato |
| C-19 | callback bloccante fuori lock |
| C-20 | async→sync un HTTP |
| C-21 | sync pending backpressure |
| C-22 | fatal Error a future/handler |

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
