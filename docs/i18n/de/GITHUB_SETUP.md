# Deutsch (`de`) — GitHub-Veröffentlichung

[Sprachauswahl](../../LANGUAGES.md) · [English source](../../GITHUB_SETUP.md)

> Bei Abweichungen gilt Englisch; `LICENSE` bleibt unübersetzt und bindend.

Vorher Rechte an Code/Dokumentation/Name bestätigen, Verzeichnis und Git-Historie auf Secrets prüfen, Kunden-/Requester-Daten, Tokens, `.env` und Maschinenfiles entfernen, Apache-2.0 `LICENSE`/`NOTICE` kontrollieren und `./mvnw --batch-mode --no-transfer-progress clean verify` ausführen.

Erstellen Sie das öffentliche Repository `vies-client` mit `main`, ohne GitHub-generierte README/License/ignore-Duplikate. Erst nach OWNER-Ersatz und Prüfung des staged diff: `git init -b main`, `git add --all`, Commit, dann `gh repo create OWNER/vies-client --public --source . --remote origin --push`.

Aktivieren: Issues, Discussions, branch/ruleset mit PR+Approval+erforderlichen `CI`/`CodeQL`/Dependency-Checks, Conversation Resolution, kein Force-Push/Delete; private vulnerability reporting, Dependabot, secret scanning/push protection. Themen: Java 21, VIES, VAT, REST, virtual threads, JPMS.

Der Sponsor-Button kommt aus `.github/FUNDING.yml`, z. B. `custom: https://buymeacoffee.com/ACCOUNT`; nur verifizierten Link verwenden, Spende ohne SLA/Governance-Recht. Nach erstem Push `pom.xml`, Badge-URLs, Security-Link, Release-Doku und Funding-URL aktualisieren. Erst bei grünem CI/CodeQL signiertes `v1.0.0`; Workflow erzeugt JAR, Sources, Javadocs, SHA-256 und Release.

## Repository-Einstellungen im Detail

Beim Erstellen auf GitHub weder README, License noch `.gitignore` generieren, weil diese Dateien bereits lokal bestehen. Vor `git add --all` mit `git status --short`, Secret-Scanner und manueller Prüfung sicherstellen, dass `target/`, IDE-State, `.env`, Tokens, echte Requester-/Kundendaten und temporäre Archive fehlen. Der Owner, Projektname, öffentliche Sichtbarkeit und Default-Branch `main` müssen vor dem ersten Push bestätigt werden.

Aktivieren Sie Issues für reproduzierbare Fehler und Discussions für Nutzungsfragen. Wiki bleibt aus, solange es nicht gepflegt wird. Automatisches Löschen gemergter Head-Branches reduziert Altlasten. Private Vulnerability Reporting, Dependabot Alerts/Updates, Secret Scanning und Push Protection werden soweit im Account verfügbar eingeschaltet. CodeQL und Dependency Review laufen in GitHub Actions mit minimalen Berechtigungen.

Ein Ruleset für `main` verlangt Pull Request, mindestens eine Freigabe, erneute Freigabe nach neuen Commits, Auflösung aller Conversations und grüne Checks `CI`, `CodeQL` sowie Dependency Review. Force-Push und Branch-Löschung bleiben gesperrt. Admin-Bypass ist nur für dokumentierte Notfälle. Signierte Commits sind optional, signierte/annotierte Release-Tags empfohlen. Required-Check-Namen können erst nach dem ersten Workflowlauf ausgewählt werden.

Empfohlene Labels: `bug`, `enhancement`, `documentation`, `localization`, `security`, `dependencies`, `performance`, `concurrency`, `breaking-change`, `good-first-issue`, `help-wanted`, `triage`. Topics: `java`, `java21`, `vies`, `vat`, `vat-validation`, `eu`, `rest-client`, `virtual-threads`, `single-flight`, `jpms`, `zero-dependency`. Issue-/PR-Templates sollen keine echten VAT- oder Kundendaten verlangen.

Die Funding-Datei enthält nur die vom Maintainer verifizierte Buy-Me-a-Coffee-URL. Nach dem ersten Push werden POM-Felder `url`, `scm`, `developers`, Badge-Links, Advisory-Link und Release-Dokumentation mit der echten Owner-URL ersetzt. Der erste Tag wird erst nach erfolgreicher Repository-Security-Konfiguration und einem vollständigen Release-Dry-Run erstellt.

Zum Abschluss werden Repository-Beschreibung, Website, Topics, Social Preview und Kontaktwege geprüft. Ein Test-Fork bestätigt, dass externe Beiträge Templates, CI und geschützte Branchregeln korrekt durchlaufen. Backup- und Recovery-Verantwortung für Repository, Packages, Releases und Signing-Schlüssel wird dokumentiert.
