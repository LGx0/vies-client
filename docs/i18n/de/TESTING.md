# Deutsch (`de`) — Tests

[Sprachauswahl](../../LANGUAGES.md) · [English source](../../TESTING.md)

> Bei Abweichungen gilt die englische Testdokumentation. `LICENSE` wird nicht übersetzt. Projektlizenz: Apache-2.0.

Die aktuelle deterministische Suite umfasst **73 Tests**: 44 Unit-Tests und 29 lokale HTTP-/Integrations-/Konkurrenztests.

## Testarten und Befehle

Unit-Tests prüfen eine Klasse ohne echtes Netzwerk. Lokale Integrations-, Konkurrenz- und Lebenszyklustests verwenden einen kontrollierten `HttpServer` und deterministische Latches/Executor. Die Standardsuite kontaktiert VIES nicht.

```bash
./mvnw test
./mvnw clean verify
./mvnw -Dtest=VatFormatTest test
./mvnw -Dtest=ViesClientHttpTest test
```

## Abdeckung

Die Suite deckt EU-Länderformate und Normalisierung, Requester, Resultat-/Fehlerabbildung, Availability, JSON-Parser, TTL-Cache und Builder-Validierung ab. HTTP-Tests prüfen Retry, Timeout, malformed response, Cache, Single-Flight, synchrone/async Backpressure, Permit-Freigabe, abgelehnte Executor, Cancellation und `close()`-Races einschließlich Leader/Follower-Konsistenz.

Für Konkurrenztests sind `Thread.sleep(...)`-Annahmen zu vermeiden. Verwenden Sie Barriers, Latches und blockierende Test-Doubles, damit Fehler reproduzierbar sind. Jeder behobene Race erhält einen Regressionstest.

## Zusätzliche Tests

- Live smoke: höchstens wenige manuelle Aufrufe; nie Teil des normalen CI.
- Load: nur lokaler Mock/WireMock, Warm-up, feste Parallelität, Durchsatz und p50/p95/p99 dokumentieren.
- Soak: mehrere Stunden mit begrenztem Heap; Threads, Heap, Queue und offene Verbindungen beobachten.
- Chaos: Timeouts, langsame Antworten, beschädigtes JSON, Cache-Ausfall, Executor-Rejection und Shutdown injizieren.

## CI-Empfehlung

Führen Sie `./mvnw clean verify` auf JDK 21 aus, archivieren Sie Surefire-Berichte und das erzeugte JAR und blockieren Sie Releases bei Fehlern. Ergänzen Sie statische Analyse, Dependency-/Secret-Scan und reproduzierbaren Package-Check. Leistungswerte dürfen nur bei identischem Profil verglichen werden.

## Vollständiger Katalog und Regressionsregeln

Unit-Gruppen: `VatFormatTest` 8 (Normalisierung, Separatoren, `GR`→`EL`, Fehler, alle 28 Codes); `ViesRequesterTest` 4; `ViesResponseMappingTest` 11 (GET/POST, Placeholder, Invalid/Unavailable/Input, Nichtobjekt, Boolean- und Datumstrenge); `ViesErrorTest` 6; `ViesAvailabilityTest` 2; `MiniJsonTest` 4; `TtlCacheTest` 6; `ViesClientBuilderTest` 3.

| IDs | Lokaler Integration-/Concurrency-Fall |
|---|---|
| I-01/I-02 | 503-Retry→Erfolg; Cachefehler→kein HTTP |
| C-01–C-04 | Single-Flight, HTTP-Limit, Async-Backpressure, Cancel-Permit |
| C-05–C-09 | Callback-Close, Admissiontimeout, Chain-Cleanup, Async/Sync Cache-Close |
| C-10–C-13 | Custom Executor, gleiche Close-Ergebnisse, 100 Followers, Rejection-Cleanup |
| C-14–C-17 | gemischtes Single-Flight, Cache-Neuprüfung, Write-Close, Unique-Chain |
| C-18–C-22 | queued Cancel, blockierender Callback, reverse Single-Flight, Sync-Limit, fatal `Error` |

```bash
./mvnw --batch-mode --no-transfer-progress clean test
./mvnw --batch-mode --no-transfer-progress clean verify
./mvnw -Dtest=VatFormatTest test
./mvnw -Dtest=ViesClientHttpTest test
```

Standardsuite offline/deterministisch. Live-Smoke nur 1–2 manuelle Calls, niemals CI/Load. Load nur Loopback-Mock mit Warm-up, fixer Parallelität, Heap/GC, Throughput, p50/p95/p99. Soak mehrere Stunden mit begrenztem Heap; Chaos injiziert Langsamkeit, Timeout, kaputtes JSON, Cacheausfall, Executor-Rejection, Shutdown. Jeder Fix braucht einen ohne Fix fehlschlagenden Test; Latches/Barriers statt fester Sleeps.

### Einzelne deterministische Fälle C-01 bis C-22

| ID | Erwartung |
|---|---|
| C-01 | 200 gleiche async Aufrufer erzeugen genau einen HTTP-Call |
| C-02 | aktive HTTP-Calls überschreiten das konfigurierte Limit nicht |
| C-03 | Async-Pending-Überlast liefert sofort `CLIENT_OVERLOADED` |
| C-04 | Future-Cancel gibt Permit zurück |
| C-05 | `close()` aus Callback deadlockt nicht |
| C-06 | Admissiontimeout begrenzt das Warten |
| C-07 | verkettete gleiche Async-Calls sehen vor Completion bereinigten Zustand |
| C-08 | Async Cache/Close-Race liefert `CLIENT_CLOSED`, kein HTTP |
| C-09 | dasselbe synchron |
| C-10 | aktive Custom-Executor-Arbeit wird unterbrochen, Executor bleibt offen |
| C-11 | Sync Leader/Follower erhalten dasselbe Close-Ergebnis |
| C-12 | 100 gleiche Followers brauchen kein zusätzliches Pending-Permit |
| C-13 | Executor-Rejection räumt Permit und In-flight auf |
| C-14 | Sync Leader + Async Follower ist ein HTTP-Call |
| C-15 | zweite Cacheprüfung verhindert HTTP nach stale Miss |
| C-16 | Cache-Write/Close gibt Leader/Follower dasselbe Resultat |
| C-17 | verkettete unterschiedliche Async-Calls erhalten Permit zurück |
| C-18 | queued Custom-Executor-Task wird tatsächlich abgebrochen |
| C-19 | blockierender User-Callback blockiert Lifecycle-Lock nicht |
| C-20 | Async Leader + Sync Follower ist ein HTTP-Call |
| C-21 | Sync-Pending-Limit bietet begrenzte Backpressure |
| C-22 | fataler Async-`Error` erreicht Future und Uncaught-Handler |

## Unit-Testdetails nach Komponente

Die Formatgruppe prüft beide öffentlichen Eingabeformen, Groß-/Kleinschreibung, erlaubte Separatoren, Präfixkonflikte, unbekannte Codes, leere Werte und mindestens eine Form jedes unterstützten VIES-Ländercodes. Griechenland wird bewusst von Eingabe `GR` auf VIES-Code `EL` kanonisiert. Requester-Tests stellen dieselben Regeln für die eigene VAT sicher und verifizieren fail-fast ohne Netzwerk.

Mappingtests verwenden kleine JSON-Fixtures für GET- und POST-artige Antworten. Sie prüfen vollständig befülltes `Valid`, VIES-Platzhalter, bestätigtes `Invalid`, temporäre und Eingabefehler sowie Root-/Typverletzungen. Besonders wichtig: fehlendes oder als String geliefertes Boolean darf nicht zu `Invalid` werden; fehlende oder falsche Auditzeit darf nicht durch die lokale Uhr ersetzt werden. Offsetzeiten werden korrekt nach UTC konvertiert.

Error-Tests verifizieren für jeden öffentlichen Maschinen-Code nichtleere HU-/EN-Texte und eine stabile Retryklassifikation. HTTP 408, 429 und 5xx werden getrennt von permanenten Eingabe-/Policyfehlern geprüft. Unbekannte Codes bleiben sichtbar und starten keinen Retry-Sturm. Availability-, MiniJson-, TTL-Cache- und Buildertests decken strikte Typen, Ablauf, Größenbegrenzung, ungültige URLs, negative/überlaufende Durations und Grenzwerte ab.

## Deterministische Concurrency-Technik

Der lokale `HttpServer` bindet nur Loopback und erlaubt kontrollierte Statuscodes, Bodies und Blockaden. Latches markieren genau, wann Cache, Executor oder Handler betreten wurde. Der Test löst dann Close, Cancellation oder den zweiten Aufrufer aus und gibt die Blockade explizit frei. Ein fester Sleep ist kein Korrektheitsbeweis; er darf höchstens ein oberes Timeout für das Testframework ergänzen. Executor-Finished-Latches bestätigen, dass tatsächliche Tasks endeten und nicht nur Ergebnis-Futures abgeschlossen wurden.

Leader/Follower-Tests zählen HTTP-Aufrufe und vergleichen komplette Responsevarianten einschließlich Errorcode. Gemischte Sync/Async-Richtungen werden separat geprüft. Pending-Tests unterscheiden gleiche Followers von unterschiedlichen Leadern. Rejectiontests benutzen einen kontrolliert ablehnenden Executor und prüfen anschließend eine erfolgreiche Anfrage, um Permit- und Map-Leaks sichtbar zu machen. Fatal-Error-Tests prüfen sowohl exceptional Future als auch Uncaught-Handler.

## CI-, Load-, Soak- und Chaosplan

CI verwendet eine frische JDK-21-Umgebung, `./mvnw --batch-mode --no-transfer-progress clean verify`, Surefire-Berichte, Sources/Javadocs und Modulbeschreibung. Zusätzliche Jobs können Dependency Review, CodeQL, Secret Scan und Lizenzprüfung ausführen. Die Standardsuite hat keinen öffentlichen Netzwerkzugriff und muss wiederholt stabil bleiben. Flaky Tests werden nicht einfach erneut ausgeführt, sondern durch deterministische Synchronisation repariert.

Ein Loadtest nutzt nur einen lokalen oder dedizierten Mock. Dokumentiert werden Warm-up, Dauer, eindeutige/gleiche Schlüssel, Payload, Cachezustand, Parallelität, Heap, GC, Durchsatz, p50/p95/p99 und Fehlerverteilung. Ein Soaktest läuft mehrere Stunden mit begrenztem Heap und beobachtet In-flight-Map, Permits, Threads, Verbindungen und Queuealter. Chaosfälle injizieren langsames DNS/HTTP, Timeout, 429/5xx, beschädigtes JSON, Redis-Ausfall, Executor-Rejection, Interrupt und gleichzeitiges Shutdown.

Live-Smoke ist optional, manuell und auf sehr wenige Calls begrenzt. Er prüft nur aktuelle Konnektivität, nicht deterministische Fachrichtigkeit. VIES darf weder durch CI noch durch Benchmark oder Stresstest belastet werden. Für jede behobene Regression wird zuerst ein reproduzierender Test verlangt, der ohne Fix fehlschlägt und mit Fix dauerhaft grün ist.

Vor einem Release wird zusätzlich das erzeugte JAR in einem leeren Classpath- und einem leeren JPMS-Beispiel kompiliert, `jar --describe-module` geprüft und der Demo-Server gestartet. Dokumentationslinks, Codefences, Lizenzhinweise und Sprachwähler werden automatisiert validiert. Testergebnisse und Checksums stammen aus demselben CI-Lauf wie das veröffentlichte Artefakt; ein lokaler grüner Test ersetzt nicht die geschützte Release-Pipeline.

Auch negative Testdaten bleiben synthetisch und enthalten weder echte Unternehmen noch persönliche oder steuerliche Informationen. Temporäre Server und Executor werden nach jedem Test zuverlässig geschlossen.
