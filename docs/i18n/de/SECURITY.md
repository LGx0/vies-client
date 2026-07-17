# Deutsch (`de`) — Sicherheitsrichtlinie

[Sprachauswahl](../../LANGUAGES.md) · [English source](../../../SECURITY.md)

> Bei Abweichungen gilt das englische Original; `LICENSE` wird nicht übersetzt. Projektlizenz: Apache-2.0.

## Unterstützte Versionen

Die neueste `1.x` erhält Sicherheitskorrekturen; ältere Releases nur fallweise. Aktualisieren Sie in der Anfangsphase stets auf die neueste Version.

## Schwachstelle melden

Kein öffentliches Issue und kein Exploit vor koordinierter Behebung. Nutzen Sie GitHub **Security → Advisories → Report a vulnerability**. Nennen Sie Version, Umgebung, Reproduktion, mögliche Auswirkung, Workaround/Fix und möglichst einen minimalen PoC ohne echte Personen- oder Steuerdaten.

Ziele, keine SLA: Eingangsbestätigung möglichst binnen 7 Tagen, erste Bewertung/Nächster Schritt binnen 14 Tagen, Fix/Offenlegung koordiniert nach Schwere und Aufwand.

Relevant sind Injection, TLS-/Endpoint-Umgehung, Datenabfluss, Cache Poisoning, Requester-Missbrauch, unbegrenztes Ressourcenwachstum, Shutdown-Deadlock oder fälschliches `Invalid` bei Technikfehlern. Reine VIES-Verfügbarkeit, Throttling oder Datenqualität sind keine Bibliotheksschwachstelle.

## Inhalt und Ablauf einer Meldung

Eine gute Meldung nennt betroffene Version/Commit, JDK und Betriebssystem, Konfiguration, minimale Reproduktion, erwartetes und tatsächliches Verhalten, mögliche Auswirkung und bekannte Workarounds. Ein Proof of Concept verwendet synthetische VAT-/Requester-Daten und einen lokalen Mock. Keine echten Kunden-, Steuer- oder Authentisierungsdaten hochladen. Verschlüsselte weitere Kommunikation kann nach der ersten privaten Advisory-Meldung vereinbart werden.

Maintainer bestätigen den Eingang nach Möglichkeit innerhalb von sieben Tagen und liefern innerhalb von vierzehn Tagen eine erste Einschätzung oder Rückfrage. Diese Ziele sind kein vertragliches SLA. Schweregrad, Exploitbarkeit, betroffene Versionen und koordinierter Veröffentlichungstermin werden gemeinsam bewertet. Reporter sollen ausreichend Zeit für Fix, Tests und Release lassen und vorab keine Issue-, Social-Media- oder Exploit-Veröffentlichung vornehmen.

Im Scope liegen auch SSRF-/Endpoint-Manipulation, fehlerhafte TLS-Validierung, sensible Logs, unsichere Cache-Deserialisierung, Permit-/Memory-Leaks, Race-bedingte falsche Entscheidungen und Umgehung von Limits. Reine Fehler oder Limits des offiziellen Upstreams werden an den zuständigen Betreiber gemeldet, sofern die Clientbibliothek sie korrekt als `Unavailable` darstellt.

Nach Korrektur werden Regressionstests, Changelog, Advisory, betroffene Versionen und Upgradehinweis veröffentlicht. Sicherheitsreleases können Details bis zur verfügbaren gepatchten Version zurückhalten. Gute, koordinierte Meldungen werden auf Wunsch anerkannt; ein Bug-Bounty-Programm oder finanzielle Vergütung wird nicht zugesagt.
