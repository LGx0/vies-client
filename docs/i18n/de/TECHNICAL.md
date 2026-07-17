# Deutsch (`de`) — Technisches Modell

[Sprachauswahl](../../LANGUAGES.md) · [English source](../../TECHNICAL.md)

> Das englische Original ist bei Abweichungen maßgeblich. `LICENSE` bleibt unverändert und rechtsverbindlich. Projektlizenz: Apache-2.0.

## Aufbau und Ergebnismodell

Das JPMS-Modul `vies.client` exportiert ausschließlich `vies.client`; interne Parser- und Cache-Details bleiben gekapselt. Es gibt keine Laufzeitabhängigkeiten außerhalb des JDK. `ViesResponse` ist sealed: `Valid`, `Invalid`, `Unavailable`, `MalformedInput`. `ViesError` enthält stabilen Code, `messageHu`, `messageEn` und `retryable`.

## Anfragepfad

1. Umsatzsteuer-ID normalisieren und länderspezifisches Format lokal prüfen.
2. Cache lesen; ein gültiger Eintrag beendet die Anfrage.
3. Gleiche VAT/Requester-Schlüssel innerhalb einer JVM per Single-Flight zusammenführen.
4. Pending- und Netzwerk-Semaphoren anwenden; bei Überlast strukturiert ablehnen.
5. REST-Aufruf mit wiederverwendetem JDK `HttpClient`, Timeouts und begrenztem Retry.
6. JSON streng validieren; fehlende/ungültige Felder ergeben `MALFORMED_RESPONSE`.
7. Nur vertrauenswürdige `Valid`-Ergebnisse mit TTL cachen; Zustand vor Completion bereinigen.

## Parallelität, Retry und Cache

Async-Arbeit verwendet standardmäßig virtuelle Threads. `maxPendingAsyncRequests` begrenzt eindeutige async Leader; gleiche Followers benötigen keinen weiteren Platz. `maxPendingSyncRequests` begrenzt wartende synchrone Aufrufer, `maxConcurrentRequests` tatsächliche HTTP-Aufrufe. Permits und In-flight-Zustand werden auf allen Erfolgs-, Fehler-, Ablehnungs- und Shutdown-Pfaden freigegeben.

Nur als vorübergehend klassifizierte Fehler werden wiederholt. Backoff und Jitter verhindern synchronisierte Retry-Wellen. Cache-Lesefehler werden als `CACHE_ERROR` sichtbar und dürfen keinen Stampede auslösen; Schreibfehler dürfen ein bereits gültiges Ergebnis nicht überschreiben.

## Shutdown und Skalierung

`close()` ist konkurrierend sicher: Neue Arbeit wird abgelehnt, laufende Ergebnisse werden konsistent mit `CLIENT_CLOSED` beendet, eigene Tasks unterbrochen und Ressourcen freigegeben. Externe Executor bleiben Eigentum des Aufrufers.

Bei großem Volumen lautet die Topologie: API → dauerhafte partitionierte Queue → begrenzte Worker → gemeinsamer Cache + verteilter Limiter → VIES. Lokale Semaphore schützen nur einen Prozess. VIES und Mitgliedstaat-Systeme bleiben die harte, variable Kapazitätsgrenze.

## Beobachtbarkeit, Performance und Sicherheit

Messen Sie Dauer, Ergebnisvariante, Fehlercode, Retry-Zahl, Cache-Hit, In-flight- und Ablehnungszahlen. Verwenden Sie keine hochkardinalen VAT-Werte als Metriklabels. Lokale Benchmarks sind keine VIES-SLA. Geheimnisse gehören nicht in Logs; vollständige IDs sind personenbezogene/geschäftliche Daten. TLS validieren, Abhängigkeiten/Build reproduzierbar halten und Eingaben vor URL-Erzeugung normalisieren.

## Detaillierte Invarianten und bekannte Grenzen

| Typ | Entscheidung | Retry | Cache |
|---|---|---|---|
| `Valid` | bestätigt | nein | ja |
| `Invalid` | nicht bestätigt | nein | nein |
| `Unavailable` | keine Entscheidung | codeabhängig | nein |
| `MalformedInput` | ungültige Eingabe | nein | nein |

`Valid` trägt VAT, Optionals für Name/Adresse/Consultation, Auditzeit und Cacheherkunft; `---` wird leer. Unbekannte Fehlercodes bleiben erhalten und sind standardmäßig nicht retrybar. Nach erstem Cache-Miss erfolgt eine Leadership-/Cache-Neuprüfung. In-flight-Futures liegen in `ConcurrentHashMap`; getrennte Semaphore begrenzen Sync-Pending, Async-Leader und HTTP. Cleanup geschieht vor `complete`, damit Callback-Ketten Kapazität sehen. Fatal `Error` wird exceptional signalisiert und erneut geworfen.

Retrybar sind klassifizierte Timeouts, Busy/MS-Ausfall, Netzwerk sowie HTTP 408/429/5xx; Input, Close und lokale Überlast werden nicht intern blind wiederholt. Backoff wächst exponentiell mit Jitter; Duration-Überlauf wird validiert. Der lokale TTL-Cache ist begrenzt, aber unter Konkurrenz nur näherungsweise/eventuell und keine LRU-Garantie. Cache-Read-Fehler verhindern Stampede; Write-Fehler ändern gültige Antwort nicht; nur `Valid` wird gespeichert.

`close()` serialisiert konkurrierende Schließer, stoppt Admission, beendet Leader/Follower gleich mit `CLIENT_CLOSED`, unterbricht eigene aktive/queued Tasks und lässt externe Executor offen. Callbacks laufen außerhalb des Lifecycle-Locks.

Lokale JDK-21-Loopback-Mediane (keine VIES-SLA): Cachehit 8,91 Mio/s, Formatabweisung 9,02 Mio/s, sequenzielles HTTP 4.044/s. Reale Werte hängen von JVM, Hardware, Netz und Upstream ab; Warm-up und p50/p95/p99 dokumentieren. Produktion nur HTTPS, normalisierte URL-Segmente, keine Secrets/PII in Logs, Cache als sensible Daten behandeln, Antworten strikt als untrusted validieren.

## Öffentliche Fehlercodes

| Codefamilie | Bedeutung und Behandlung |
|---|---|
| `SERVICE_UNAVAILABLE`, `MS_UNAVAILABLE`, `SERVER_BUSY`, `TIMEOUT` | temporärer Upstream; begrenzt retrybar |
| `GLOBAL_MAX_CONCURRENT_REQ`, `MS_MAX_CONCURRENT_REQ`, HTTP `408`/`429`/`5xx` | Throttling/Server; verzögert retrybar |
| `NETWORK_ERROR` | DNS/TCP/TLS; Backoff |
| `CLIENT_OVERLOADED` | lokale Admission voll; verzögert neu einreihen |
| `CLIENT_CLOSED`, `INTERRUPTED` | Lifecycle/Abbruch, keine Entscheidung |
| `INVALID_INPUT`, `INVALID_VAT_FORMAT`, `INVALID_REQUESTER_INFO` | permanente Eingabe |
| `IP_BLOCKED`, `VAT_BLOCKED` | VIES-Policy, manuell prüfen |
| `MALFORMED_RESPONSE` | Schema/Datum/Boolean ungültig |
| `CACHE_ERROR` | Cacheausfall, keinen Stampede erzeugen |
| `INTERNAL_ERROR`, unbekannt | keine Entscheidung, standardmäßig kein Retry |

## Paketgrenzen, Datenfluss und Invarianten

Die öffentliche Paketoberfläche enthält Client, Builder, Requester, Responsehierarchie, Error, Availability und Cache-Erweiterungspunkt. Parser, Mini-JSON und lokaler TTL-Cache bleiben im nicht exportierten internen Paket. Dadurch kann die Implementierung ohne unnötige binäre API-Verpflichtung weiterentwickelt werden. Das Release benötigt nur `java.base` und `java.net.http`; Testwerkzeuge sind nicht im Runtime-JAR.

Jede Anfrage erhält nach Normalisierung einen stabilen Schlüssel aus Ziel-VAT und Requester. Ein Cachetreffer wird sofort kopiert und mit Herkunft markiert. Bei einem Miss konkurrieren Aufrufer um einen In-flight-Eintrag. Genau ein Leader liest nach Leadership nochmals den Cache und führt gegebenenfalls HTTP aus; Followers warten auf dasselbe Shared Result, erhalten aber eine eigene Future-Sicht. Diese zweite Prüfung schließt das Fenster, in dem ein anderer Prozess oder Thread zwischen erster Prüfung und Leadership in den Cache geschrieben hat.

Admission ist mehrstufig. Sync-Pending begrenzt Threads, die den synchronen Pfad betreten. Async-Pending begrenzt eindeutige Leader, nicht identische Followers. Die Netzwerksemaphore begrenzt tatsächliche HTTP-Aufrufe inklusive Retryversuchen. Jede erfolgreiche Acquire-Operation besitzt genau einen Release-Pfad. Rejection, Cancellation, Interrupt, Parsefehler, fataler Error und Close werden deterministisch aufgeräumt. Shared State wird vor `CompletableFuture.complete` entfernt, damit unmittelbar verkettete Arbeit keinen veralteten Slot sieht.

## HTTP- und Validierungsmodell

Der wiederverwendete JDK-Client nutzt Connect- und Per-Request-Timeout. Pfadsegmente entstehen nur aus validierten Länder-/VAT-Werten. Nicht-2xx-Antworten werden nach Status klassifiziert; 408, 429 und 5xx können retrybar sein. Ein 2xx-Body ist trotzdem untrusted: Root muss Objekt sein, Validity echtes Boolean, Requestdatum vorhanden und parsebar. Fehlende oder falsche Entscheidungsfelder dürfen niemals als `Invalid` interpretiert werden. Offset-Zeitstempel werden nach UTC normalisiert; es wird kein lokaler Ersatzzeitpunkt erfunden.

Retry läuft höchstens für konfigurierte zusätzliche Versuche. Die Verzögerung basiert auf `retryDelay`, wächst exponentiell und erhält Jitter, um synchronisierte Wellen zu vermeiden. Positive Durations werden beim Build auf darstellbaren Nanosekunden-/Millisekundenbereich geprüft. Interrupted Status wird respektiert. Lokale Overload- und Closed-Ergebnisse werden nicht in einer internen Schleife wiederholt; diese Scheduling-Entscheidung gehört zur Queue.

## Cache- und Speichermodell

Der eingebaute Cache verwendet Ablaufzeit und begrenzte Größe. Eviction ist absichtlich leichtgewichtig und unter hoher Konkurrenz näherungsweise, nicht strikte LRU. Die Maximalzahl ist eine operative Schutzgrenze mit eventualer Bereinigung, keine harte transaktionale Zusage in jedem Nanosekundenzeitpunkt. Nur `Valid` wird gespeichert, da ein negatives Ergebnis schneller veralten oder aus einer fachlich anderen Situation stammen kann. Externe Caches müssen dieselbe Semantik und sichere Serialisierung beibehalten.

Die In-flight-Map wächst nur mit gleichzeitig verschiedenen Schlüsseln, wird zusätzlich durch Pending-Limits begrenzt und auf jedem Abschlussweg bereinigt. Dennoch kann die Anwendung Millionen Future-Referenzen außerhalb der Bibliothek halten; dafür ist sie selbst verantwortlich. Virtuelle Threads reduzieren Stack-/Schedulingkosten wartender I/O-Arbeit, nicht Payload-, Queue- oder Audit-Speicher.

## Sicherheits- und Leistungsinterpretation

Benchmarkzahlen wurden gegen Loopback-Mocks nach Warm-up ermittelt. Sie zeigen lokale Obergrenzen bestimmter Pfade, nicht EU-Durchsatz. Produktionsprofile müssen echte Cachelatenz, TLS, Proxy, GC, Heap, CPU und Queuealter messen. p99 und Fehlerquote sind wichtiger als ein einzelner Mittelwert. Ein Performancevergleich ist nur bei identischer JDK-, Hardware-, Payload-, Cache- und Parallelitätskonfiguration sinnvoll.

Basis-URL, User-Agent und Requester sind Konfiguration, keine frei durch Benutzer kontrollierten Rohwerte. HTTPS-Zertifikate werden normal validiert. Sensible Daten werden nicht automatisch verschlüsselt oder archiviert; diese Verantwortung liegt beim Hostsystem. Dependency- und Secret-Scans, signierte Releases, Prüfsummen und reproduzierbare CI ergänzen die Laufzeitsicherheit.
