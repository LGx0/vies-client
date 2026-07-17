# Deutsch (`de`) — Mitwirken

[Sprachauswahl](../../LANGUAGES.md) · [English source](../../../CONTRIBUTING.md)

> Bei Abweichungen gilt Englisch; `LICENSE` bleibt allein maßgeblich.

Danke für Beiträge zu einem vorhersagbaren, runtime-abhängigkeitsfreien und unter hoher Parallelität sicheren Client. Referenzieren Sie für Bugs ein Issue; besprechen Sie große API-, Lizenz- oder Architekturänderungen zuerst. Sicherheitsprobleme nur nach [SECURITY.md](SECURITY.md).

Voraussetzungen: JDK 21+, Maven 3.9+, Git. Fork, kurzer Branch von `main`, kleine fokussierte Änderung, deterministischer Regressionstest je Bug, `./mvnw --batch-mode --no-transfer-progress clean verify`, dann vollständig ausgefüllter PR.

Regeln: Java 21; kleine typsichere API; keine neue Runtime-Abhängigkeit ohne Beschluss; `Unavailable` nie zu `Invalid`; geteilter Zustand threadsicher und speicherbegrenzt; öffentliche API und komplexe Konkurrenzlogik auf Englisch/Ungarisch dokumentieren; keine privaten VAT-, Firmen-, Adress- oder Requester-Daten loggen/committen.

Tests nutzen kein öffentliches Netzwerk; HTTP-/Concurrency-Tests nur loopback. Kein Load-/Stress-/CI gegen VIES. Races durch Latches/Barriers, nicht feste Sleeps. PR: grüner Build, dokumentiertes Verhalten, begründete Dependencies, Breaking Changes sichtbar, Performance reproduzierbar, Einreichungsrechte bestätigt.

AI ist erlaubt, doch der Einreichende prüft/testet jede Zeile, Herkunft und Lizenz, nennt wesentliche AI-Hilfe und übermittelt keine vertraulichen Daten. Beiträge erfolgen gemäß Apache License 2.0 Abschnitt 5 ohne Zusatzbedingungen. Es gilt [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

## Lokaler Workflow und Qualitätsstandard

Synchronisieren Sie den Fork mit `main`, erstellen Sie einen kurzlebigen Branch wie `fix/close-race`, `feat/cache-adapter-hook` oder `docs/redis-example` und halten Sie Commits logisch nachvollziehbar. Änderungen sollen ein Problem lösen, nicht nebenbei öffentliche API, Formatting und Architektur großflächig umbauen. Vor dem PR werden `java -version`, `./mvnw -version` und `./mvnw --batch-mode --no-transfer-progress clean verify` ausgeführt.

Öffentliche API bleibt klein, immutable und typsicher. Neue Runtime-Abhängigkeiten widersprechen dem Zero-Dependency-Ziel und benötigen vorherige Diskussion. Technische Unsicherheit wird immer `Unavailable`, niemals `Invalid`. Geteilter Zustand muss threadsicher, speicherbegrenzt und auf jedem Fehler-/Close-Pfad bereinigt sein. Komplexe Concurrency erhält englische und ungarische Kommentare/Javadoc, die Invarianten und Ownership erklären, nicht nur die Syntax nacherzählen.

Unit-Tests verwenden kein öffentliches Netzwerk. HTTP-Tests binden Loopback. Races werden mit Latches, Barriers und kontrollierten Executor-/Cache-Doubles gesteuert; ein fester Sleep ist kein Oracle. Jede Fehlerkorrektur enthält eine Regression, die ohne Fix fehlschlägt. Performancebehauptungen brauchen reproduzierbares Profil mit JDK, Warm-up, Parallelität, Payload, Cachezustand und p50/p95/p99. Öffentliche VIES-Lasttests werden nicht akzeptiert.

Ein PR beschreibt Motivation, Verhalten vorher/nachher, API-/Konfigurationsänderung, Tests, Sicherheits-/Datenschutzauswirkung, Kompatibilität und gegebenenfalls Benchmark. Breaking Changes werden deutlich markiert. Neue Dependency aktualisiert Third-Party-Hinweise. Neue/änderte öffentliche Funktion aktualisiert README, technische, Integrations-, Installations- und Testdokumente sowie relevante Lokalisationen.

AI-generierter Inhalt wird wie menschlicher Code vollständig geprüft. Der Autor bestätigt, dass keine vertraulichen Prompts/Daten, urheberrechtlich unklare Kopien oder halluzinierten APIs übernommen wurden. Wesentliche Unterstützung wird transparent im PR genannt. Maintainer können kleinere Anpassungen verlangen, Tests ergänzen oder den Scope auf mehrere PRs aufteilen.

Dokumentationsbeiträge sind ebenso technisch prüfbar wie Code. Befehle müssen ausführbar, Links relativ und gültig, Versionsnummern konsistent und Sicherheits-/Lizenzaussagen mit den kanonischen englischen Dateien abgestimmt sein. Lokalisationen nennen Sprache und Code, verlinken den zentralen Sprachwähler und erklären, dass bei Abweichungen Englisch maßgeblich ist. `LICENSE` und `NOTICE` werden nicht als rechtlich bindende Übersetzung angeboten.

Reviews konzentrieren sich auf Korrektheit, Wartbarkeit, begrenzte Ressourcen, Rückwärtskompatibilität und verständliche Dokumentation. Persönliche Präferenzen werden von nachweisbaren Risiken getrennt. Feedback soll konkret und umsetzbar sein; Autorinnen und Autoren reagieren sachlich, aktualisieren Tests und markieren erledigte Punkte erst nach tatsächlicher Änderung. Nach Merge kann der Branch gelöscht werden, während Issue und Changelog die Entscheidung nachvollziehbar halten.
