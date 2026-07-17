# Deutsch (`de`) — Drittanbieter-Komponenten

[Sprachauswahl](../../LANGUAGES.md) · [English source](../../../THIRD_PARTY_NOTICES.md)

> Informative Übersetzung. Ausschließlich die englischen Originale `LICENSE` und `NOTICE` sind rechtsverbindlich.

Das ausgelieferte JAR enthält und benötigt keine Drittanbieter-Runtimebibliothek; es nutzt nur JDK `java.base` und `java.net.http`. Testabhängigkeit: JUnit Jupiter 6.1.2, EPL-2.0, nur Tests. Maven-Plugins und transitive Testabhängigkeiten werden nicht gebündelt und unter eigenen Metadaten/Lizenzen geladen. Quelle: <https://github.com/junit-team/junit-framework>. Jeder PR mit neuer Dependency muss diese Datei aktualisieren und Lizenzkompatibilität dokumentieren.

EPL-2.0 betrifft die separat aufgelöste Testbibliothek und wird nicht in `vies-client-1.2.0.jar` eingebettet. Das JDK wird nach den Bedingungen der jeweils verwendeten Distribution bereitgestellt. Buildplugins und transitive Testartefakte können sich durch Maven-Auflösung ändern; Release-CI soll den effektiven Dependency Tree prüfen. Nur die englischen Originale `LICENSE` und `NOTICE` sind rechtlich maßgeblich. Neue Komponenten benötigen Name, Version, Scope, Quelle, Lizenz, Bundling-Status und dokumentierte Kompatibilitätsentscheidung.
