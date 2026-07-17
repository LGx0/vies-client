# Deutsch (`de`) — Veröffentlichung

[Sprachauswahl](../../LANGUAGES.md) · [English source](../../RELEASING.md)

> Bei Abweichungen gilt das englische Original. `LICENSE` bleibt unübersetzt und allein rechtsverbindlich.

## Voraussetzungen und Versionierung

Releases werden aus einem sauberen, überprüften Commit erstellt. JDK 21, Maven, GitHub-Zugriff und gegebenenfalls signierte Publishing-Zugangsdaten müssen verfügbar sein. Verwenden Sie Semantic Versioning: MAJOR für inkompatible API-Änderung, MINOR für kompatible Funktion, PATCH für kompatible Korrektur.

## Pre-Release

```bash
./mvnw clean verify
./mvnw package
jar --describe-module --file target/vies-client-1.0.0.jar
```

Prüfen Sie Version in `pom.xml`, Changelog, README-Beispiele, alle Sprachlinks, Javadocs, Quellen-JAR, Lizenz-/Security-Dateien und dass keine Secrets oder Build-Artefakte eingecheckt sind. Führen Sie deterministische Konkurrenztests wiederholt aus. Live-VIES nur als minimalen Smoke-Test nutzen.

## GitHub Release

Erstellen Sie einen signierten/annotierten Tag `vX.Y.Z`, pushen Sie ihn und erstellen Sie einen GitHub Release. Release Notes enthalten Highlights, API-/Konfigurationsänderungen, Fixes, Upgrade-Hinweise, bekannte Einschränkungen und Prüfsummen. Veröffentlichen Sie mindestens Haupt-JAR, Sources und Javadocs aus demselben CI-Lauf.

## Paket-Repository und Nachbereitung

Für Maven Central sind eindeutige Koordinaten, POM-Metadaten, Source/Javadoc-Artefakte, Signatur und zentrale Zugangsdaten nötig. GitHub Packages ist eine Alternative, erfordert aber Repository-Konfiguration beim Verbraucher. Zugangsdaten nur als CI-Secrets verwenden.

Nach Veröffentlichung Installation in einem leeren Beispielprojekt prüfen, GitHub Release und Paketindex kontrollieren, Dokumentation auf die neue Version setzen und Sicherheits-/Rollback-Kanal beobachten. Ein fehlerhafter Release wird nicht still überschrieben: neue Patch-Version veröffentlichen und betroffenen Release kennzeichnen.

Die Freigabecheckliste umfasst außerdem sauberen Git-Status, vollständige Apache-2.0-/NOTICE-Dateien, aktualisierte Third-Party-Hinweise, grüne Security-Scans, korrekte Sprachlinks und keine Secrets. Binary-, Sources-, Javadoc-JAR und SHA-256 müssen aus demselben CI-Lauf stammen. Tags und veröffentlichte Artefakte sind unveränderlich; bei einem Fehler folgt eine neue SemVer-Version. Release Notes nennen Breaking Changes, neue Optionen, behobene Concurrency-/Securityprobleme, bekannte Grenzen und Upgrade-/Rollbackschritte.

Maven Central erfordert verifizierte Namespace-Koordinaten, vollständige POM-Metadaten, Signaturen und sichere CI-Credentials. GitHub Packages ist einfacher, verlangt jedoch Repository-/Authentisierungskonfiguration beim Nutzer. Vor dem endgültigen Publish wird die Installation in einem leeren Java-21-Projekt sowohl über Classpath als auch JPMS geprüft. Ein Release erfolgt nur nach grünem CI und CodeQL; ein Live-VIES-Smoke bleibt minimal.
