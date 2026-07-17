# Deutsch (`de`) — vies-client — Umsatzsteuer-ID-Prüfer

`disableCache()` deaktiviert den gespeicherten/persistenten Cache. Gleichzeitige Aufrufe mit derselben USt-IdNr.-Requester-Kombination können jedoch eine einzige Single-Flight-Netzwerkanfrage teilen. Ein späterer Aufruf nach deren Abschluss führt eine neue VIES-Anfrage aus. Die `consultationNumber` ist optional und kann von VIES geliefert werden, ist aber nie garantiert; ihr rechtlicher Beweiswert hängt von den lokalen Vorschriften ab. Laden Sie `MY_EU_VAT_NUMBER` nur aus einer vertrauenswürdigen Secret-/Konfigurationsquelle.

**Suchbegriffe:** Umsatzsteuer-ID prüfen, USt-IdNr. validieren, EU-Umsatzsteuer-Validierung, Steuer-ID-Prüfung, VIES Java-Client; `VAT checker`, `VAT number validator`, `EU VAT validation`, `tax ID checker`.

Dies ist kein allgemeiner Steuerrechner, sondern ein Client zur Prüfung von EU-Umsatzsteuer-Identifikationsnummern über VIES.

[Sprachauswahl](../../LANGUAGES.md) · [English source](../../../README.md)

> Diese Übersetzung dient der Zugänglichkeit. Bei technischen oder rechtlichen Abweichungen ist die englische Originaldokumentation maßgeblich. Die Datei `LICENSE` wird nicht übersetzt; ausschließlich ihr englischer Text ist rechtsverbindlich.

`vies-client` ist ein threadsicherer Java-21+-Client ohne Laufzeitabhängigkeiten für die REST-API des VAT Information Exchange System (VIES) der Europäischen Kommission. Er funktioniert mit Spring Boot, Quarkus, Micronaut und reinem JDK-Code und verwendet `java.net.http.HttpClient`.

## Dokumentation

- [Installation](INSTALLATION.md)
- [Integration](INTEGRATION.md)
- [Technisches Modell](TECHNICAL.md)
- [Tests](TESTING.md)
- [Open Source und Lizenz](OPEN_SOURCE.md)
- [Veröffentlichung](RELEASING.md)

## Schnellstart

```bash
./mvnw clean verify
./mvnw install
```

```xml
<dependency>
  <groupId>vies.client</groupId>
  <artifactId>vies-client</artifactId>
  <version>1.2.0</version>
</dependency>
```

```java
try (var vies = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
        .retries(1)
        .build()) {
    ViesResponse response = vies.check("DE000000000");
}
```

Erstellen Sie pro Anwendung einen gemeinsamen `ViesClient` und schließen Sie ihn beim Herunterfahren. `check(...)` ist synchron, `checkAsync(...)` liefert ein `CompletableFuture<ViesResponse>`. Ergebnisse sind `Valid`, `Invalid`, `Unavailable` oder `MalformedInput`. Fehler enthalten einen stabilen Code, ungarischen und englischen Text sowie `retryable`.

## Betrieb bei hoher Last

Der Client bietet lokale Single-Flight-Zusammenführung, Cache, begrenzte Netzwerkparallelität, synchrone/async Backpressure, Timeouts, Retry mit Jitter und kontrolliertes Herunterfahren. Für Millionen Aufträge braucht die Anwendung zusätzlich eine dauerhafte partitionierte Queue, einen gemeinsamen Cache (z. B. Redis), einen verteilten Rate-Limiter und horizontal skalierte Worker. Virtuelle Threads senken Wartekosten, erhöhen aber nicht die Kapazität von VIES.

## Unterstützung

Fehlerberichte und Beiträge sind willkommen. Wenn Ihnen das Projekt Zeit spart, können Sie die Entwicklung über den im Haupt-README angegebenen Sponsor-/Kaffee-Link unterstützen. Unterstützung gewährt keine Sonderrechte und ändert die Apache-2.0-Lizenz nicht.

## Build, JPMS und vollständiges Beispiel

Das Artefakt hat die Maven-Koordinaten `vies.client:vies-client:1.2.0`. Das JPMS-Modul `vies.client` exportiert `vies.client`, benötigt `java.net.http` und kapselt `vies.client.internal`. Im Modulprojekt: `module my.api { requires vies.client; }`; im Classpath ist keine Deklaration nötig.

```java
try (var vies = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
        .retries(1).build()) {
    switch (vies.check("DE 000 000 000")) {
        case ViesResponse.Valid v -> System.out.println(v.traderName().orElse("-"));
        case ViesResponse.Invalid i -> System.out.println("Nicht gültig");
        case ViesResponse.Unavailable u -> System.out.println(u.errorCode());
        case ViesResponse.MalformedInput m -> System.out.println(m.reason());
    }
}
```

`defaultRequester` ist die eigene EU-USt-IdNr.; VIES kann optional eine `consultationNumber` liefern. Sie ist nicht garantiert, und ihr Nachweiswert richtet sich nach lokalen Vorschriften. Der sealed `switch` ist erschöpfend.

## Request-Lebenszyklus und Semantik

Normalisierung (`GR`→`EL`) → Cache → JVM-Single-Flight pro VAT/Requester → Pending-/Netzwerkadmission → wiederverwendeter `HttpClient` mit Timeout/Retry → strikte Boolean-/Auditdatumvalidierung → Cache nur für `Valid`.

| Ergebnis | HTTP | Retry | Cache | Bedeutung |
|---|---:|---|---|---|
| `Valid` | 200 | nein | ja | VIES bestätigte gültig |
| `Invalid` | 200 | nein | nein | VIES bestätigte nicht gültig |
| `Unavailable` | 503/429 | codeabhängig | nein | keine Entscheidung |
| `MalformedInput` | 400 | nein | nein | Eingabe korrigieren |

## Builder-Konfiguration

| Einstellung | Standard | Zweck |
|---|---:|---|
| `baseUrl(String)` | offizielles VIES | Test-Mock/kontrollierter Endpoint |
| `connectTimeout(Duration)` | 5 s | TCP/TLS-Verbindung |
| `requestTimeout(Duration)` | 8 s | komplette Anfrage |
| `admissionTimeout(Duration)` | 2 s | Warten auf Netzwerkplatz |
| `defaultRequester(...)` | keiner | Consultation-ID |
| `retries(int)` | 0 | 0–5, temporäre Fehler |
| `retryDelay(Duration)` | 400 ms | Exponentialbackoff + Jitter |
| `maxConcurrentRequests(int)` | 32 | echte HTTP-Aufrufe |
| `maxPendingSyncRequests(int)` | 512 | Sync-Speichergrenze |
| `maxPendingAsyncRequests(int)` | 512 | Async-Speichergrenze |
| `cacheTtl(Duration)` | 24 h | TTL gültiger Treffer |
| `cacheMaxEntries(int)` | 10.000 | lokales Cachelimit |
| `cache(ViesCache)` | eingebaut | externer Cache/Redis |
| `disableCache()` | — | Kein gespeicherter Cache; gleiche gleichzeitige Aufrufe können Single-Flight teilen |
| `userAgent(String)` | Modulkennung | EU-Identifikation |
| `executor(ExecutorService)` | Virtual Threads | externer Lifecycle beim Aufrufer |

Cache/Single-Flight sind JVM-lokal. `Unavailable` ist nie `Invalid`. Millionen Jobs benötigen dauerhafte Queue, begrenzte Batches/Worker, Redis, globalen Limiter, DLQ und kontrollierte Retries; kein Lasttest gegen öffentliches VIES. Fehler liefern `code`, `messageHu`, `messageEn`, `retryable`; VAT-, Namen-, Adress- und Requester-Daten nicht loggen. Build: `./mvnw clean verify`; Demo: `examples/ViesDemoServer.java`. Apache-2.0, private Security-Meldung, freiwilliger Sponsor ohne SLA.

## Spring Boot, eigener Cache und Betriebsbeispiel

In Spring Boot wird genau ein Client als Singleton-Bean angelegt. `destroyMethod = "close"` verbindet den Lebenszyklus der Bibliothek mit dem Application Context. Ein Controller soll alle vier Varianten mit einem erschöpfenden `switch` abbilden. `Valid` und `Invalid` sind fachliche HTTP-200-Antworten; eine ungültige Eingabe wird HTTP 400; lokale Überlast HTTP 429; ein vorübergehender technischer Ausfall HTTP 503. Der Maschinen-Code bleibt sprachneutral, während `messageHu` und `messageEn` für Benutzeroberfläche oder Support verfügbar sind.

```java
@Bean(destroyMethod = "close")
ViesClient viesClient() {
    return ViesClient.builder()
            .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
            .connectTimeout(Duration.ofSeconds(5))
            .requestTimeout(Duration.ofSeconds(8))
            .admissionTimeout(Duration.ofSeconds(2))
            .maxConcurrentRequests(32)
            .maxPendingSyncRequests(512)
            .maxPendingAsyncRequests(512)
            .cacheTtl(Duration.ofHours(24))
            .retries(2)
            .retryDelay(Duration.ofMillis(400))
            .build();
}
```

Ein eigener Cache implementiert `ViesCache`. Bei mehreren Pods empfiehlt sich Redis mit kurzen Timeouts, versionierter Serialisierung und einem Schlüssel, der sowohl Ziel-VAT als auch Requester berücksichtigt. Ein Lesefehler wird absichtlich als `CACHE_ERROR` sichtbar: ein stiller Bypass könnte bei einem Redis-Ausfall alle Worker gleichzeitig auf VIES loslassen. Ein Schreibfehler darf dagegen die bereits bestätigte `Valid`-Antwort nicht vernichten. Die TTL muss zur fachlichen Aktualitätsanforderung passen; ein Cacheeintrag ersetzt keinen archivierten Prüfungsbeleg.

```java
var response = vies.check("DE000000000");
response.error().ifPresent(error ->
    log.warn("VIES code={} retryable={} hu={} en={}",
        error.code(), error.retryable(), error.messageHu(), error.messageEn()));
```

Für Batchverarbeitung liest jeder Worker nur ein begrenztes Paket aus der dauerhaften Queue. `CLIENT_OVERLOADED` und retrybare `Unavailable`-Ergebnisse werden mit wachsender Verzögerung und Jitter neu eingeplant. Nach dem maximalen Versuch folgt die DLQ. `Invalid` und `MalformedInput` werden nicht wiederholt. Ein verteilter Token-Bucket oder ähnlicher Limiter begrenzt die Summe aller Pods; `maxConcurrentRequests` begrenzt nur eine einzelne JVM.

## Wichtige Grenzen und Datenschutz

VIES ist ein externes, föderiertes System. Antwortzeit, Verfügbarkeit, Feldinhalt und Mitgliedstaat-Limits können variieren. Virtuelle Threads erlauben viele wartende Aufgaben mit geringeren Plattformthread-Kosten, erzeugen aber keine zusätzliche Upstream-Kapazität. Die Anwendung muss Ingress, Queue, Consumerzahl, zurückgehaltene Futures und Auditablage selbst begrenzen. Health-Probes dürfen nicht bei jedem Takt eine echte VAT-Abfrage senden; Prozess, Queue und Cache werden lokal geprüft, der offizielle Statusendpunkt nur sparsam.

VAT-ID, Firmenname, Adresse, Requester und Consultation-ID können sensible Geschäfts- oder Personendaten sein. Keine vollständigen Werte als Metriklabel oder normales Logfeld verwenden. Zugriff, Verschlüsselung, Aufbewahrungsfrist und Löschung gehören in das Datenschutzkonzept des integrierenden Systems. Die Bibliothek ist keine Steuer-, Rechts- oder Buchhaltungsberatung und das Projekt ist weder von VIES noch von der Europäischen Kommission unterstützt.

## Entwicklerbefehle und weiterführende Dokumente

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
./mvnw package
jar --describe-module --file target/vies-client-1.2.0.jar
java -cp target/classes examples/ViesDemoServer.java
```

Die vollständigen Installations-, Integrations-, Architektur- und Testdetails stehen in den verlinkten Dokumenten. Beiträge müssen die deterministischen Tests, die englisch-ungarische öffentliche Javadoc-Regel, Apache-2.0 und den Verhaltenskodex einhalten. Schwachstellen niemals öffentlich vor einer koordinierten Korrektur melden.

Vor einem produktiven Rollout sollte ein Staginglauf mit realistischen, aber synthetischen Daten durchgeführt werden. Dabei werden Cachehit und Cacheausfall, wiederholte 429-/5xx-Antworten, langsame Mitgliedstaaten, Netzwerkunterbrechung, Queue-Rückstau und gleichzeitiges Shutdown simuliert. Akzeptanzkriterien umfassen keine falsche `Invalid`-Entscheidung, begrenzten Speicher, eingehaltene Parallelitätslimits, nachvollziehbare Auditdaten und einen funktionierenden DLQ-/Replay-Prozess. Kapazitätswerte werden pro Umgebung gemessen und nicht ungeprüft aus lokalen Benchmarks übernommen.

Updates folgen Semantic Versioning. Patch-Releases beheben kompatible Fehler, Minor-Releases ergänzen kompatible Funktionen und Major-Releases können Migration verlangen. Release Notes, Changelog, Prüfsummen und signierte Tags ermöglichen eine kontrollierte Aktualisierung. Nutzer sollen immer die neueste unterstützte `1.x` verwenden, Security Advisories beobachten und Rollback des vorherigen Artefakts vorbereiten. Optionalfelder bleiben optional; Integrationen dürfen Händlername, Adresse oder Consultation-ID nie voraussetzen.

Alle Produktionsparameter werden ausdrücklich dokumentiert, versioniert und im Staging überprüft. Ungeprüfte Standardwerte sind keine Kapazitätszusage für jede Umgebung.
