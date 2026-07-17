# Deutsch (`de`) — Integration

`disableCache()` deaktiviert den gespeicherten/persistenten Cache. Gleichzeitige Aufrufe mit derselben USt-IdNr.-Requester-Kombination können jedoch eine einzige Single-Flight-Netzwerkanfrage teilen. Ein späterer Aufruf nach deren Abschluss führt eine neue VIES-Anfrage aus. Die `consultationNumber` ist optional und kann von VIES geliefert werden, ist aber nie garantiert; ihr rechtlicher Beweiswert hängt von den lokalen Vorschriften ab. Laden Sie `MY_EU_VAT_NUMBER` nur aus einer vertrauenswürdigen Secret-/Konfigurationsquelle.

[Sprachauswahl](../../LANGUAGES.md) · [English source](../../INTEGRATION.md)

> Bei technischen oder rechtlichen Abweichungen gilt das englische Original. `LICENSE` wird nicht übersetzt. Projektlizenz: Apache-2.0.

## Lebenszyklus und API

Erstellen Sie einen `ViesClient` als Singleton pro Prozess und schließen Sie ihn über `close()` beim Shutdown. Der Client ist threadsicher. Erstellen Sie nicht für jede Anfrage eine neue Instanz.

```java
try (var client = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
        .maxConcurrentRequests(32)
        .maxPendingSyncRequests(512)
        .maxPendingAsyncRequests(512)
        .admissionTimeout(Duration.ofSeconds(2))
        .retries(2)
        .build()) {
    ViesResponse sync = client.check("FRXX000000000");
    CompletableFuture<ViesResponse> async = client.checkAsync("PL0000000000");
}
```

Behandeln Sie alle vier Varianten: `Valid` und `Invalid` sind fachliche Antworten; `MalformedInput` ist nicht retrybar; `Unavailable` kann abhängig von `error().retryable()` später erneut versucht werden. Geben Sie an APIs stets `errorCode`, `messageHu`, `messageEn` und `retryable` zurück. Empfohlene HTTP-Codes: 200 für `Valid`/`Invalid`, 400 für `MalformedInput`, 429 für `CLIENT_OVERLOADED`, 503 für vorübergehende VIES-Fehler.

## Spring Boot

```java
@Bean(destroyMethod = "close")
ViesClient viesClient() { return ViesClient.builder().retries(1).build(); }
```

Ein Controller sollte die sealed-Varianten mit einem erschöpfenden `switch` abbilden. Loggen Sie keine unnötigen vollständigen Umsatzsteuer-IDs; nutzen Sie strukturierte Metriken für Latenz, Cache-Hit, Retry, Ablehnung und Upstream-Fehlercode.

## Requester und Nachweis

Mit `defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))` kann VIES bei gültigen Treffern optional eine `consultationNumber` liefern. Lesen Sie die eigene USt-IdNr. nur aus einer vertrauenswürdigen Secret-/Konfigurationsquelle. Die Nummer ist nie garantiert; speichern Sie sie zusammen mit Zeitstempel, angefragter ID und Ergebnis entsprechend Ihren Aufbewahrungs-, Datenschutz- und lokalen Nachweisregeln.

## Cache, Queue und mehrere Knoten

Implementieren Sie `ViesCache` für Redis oder einen anderen gemeinsamen Cache. Cache-Schlüssel müssen Umsatzsteuer-ID und Requester enthalten. Verwenden Sie TTL und vermeiden Sie dauerhafte Speicherung veralteter Status. Bei mehreren Pods sind Single-Flight und Semaphore nur JVM-lokal: Eine dauerhafte Queue, begrenzte Consumer, ein verteilter Rate-Limiter und gegebenenfalls DLQ sind deshalb erforderlich.

Retryen Sie nur vorübergehende Fehler, mit exponentiellem Backoff und Jitter. `Invalid` und `MalformedInput` gehören nicht in die Retry-Schleife. Begrenzen Sie Queue-Länge und vom Aufrufer gehaltene Futures.

## Produktionscheckliste

- Genau ein Client pro Worker; sauberer Shutdown getestet.
- Connect-/Request-/Admission-Timeouts festgelegt.
- Parallelitäts- und Pending-Limits passend zur Umgebung.
- Gemeinsamer Cache und globaler Limiter bei mehreren Knoten.
- Metriken, Alerts, Audit-Aufbewahrung und Datenschutz definiert.
- Keine Live-VIES-Lasttests; Lasttests nur gegen kontrollierte Mocks.

## Vollständiger Vertrag und Betriebsdetails

```java
ViesResponse sync = client.check("DE000000000");
CompletableFuture<ViesResponse> async = client.checkAsync("DE000000000");
```

| Antwort | HTTP | Retry | Interpretation |
|---|---:|---|---|
| `Valid`/`Invalid` | 200 | nein | fachliche Entscheidung |
| `MalformedInput` | 400 | nein | Eingabe korrigieren |
| `Unavailable(CLIENT_OVERLOADED)` | 429 | verzögert | lokale Backpressure |
| retrybares `Unavailable` | 503 | verzögert | keine Entscheidung |

Spring registriert `@Bean(destroyMethod="close")`, liest Limits/Timeouts aus Properties und mappt sealed Varianten samt `errorCode`, `messageHu`, `messageEn`, `retryable`. Plain JDK verwendet einen gemeinsamen Client im `HttpServer`; siehe Demo. Cancellation eines Followers darf die gemeinsame Single-Flight-Arbeit nicht zerstören; blockierender externer Cache läuft im begrenzten Worker.

Ein Redis-`ViesCache` nutzt VAT+Requester-Schlüssel, TTL, versionierte sichere Serialisierung und kurze Timeouts. Read-Fehler bleiben `CACHE_ERROR`; Write-Fehler überschreiben kein gültiges Resultat. Queue/DLQ: nur retrybares `Unavailable` verzögert mit begrenzten Versuchen, Backoff und Jitter; `Invalid`/`MalformedInput` quittieren. Bei N Pods ist `N × maxConcurrentRequests` nur ein theoretisches lokales Maximum—ein verteilter Rate Limiter muss EU-/Mitgliedstaat-Endpunkte schützen.

Health: Prozess-Liveness, Queue-/Cache-Readiness und sparsames `check-status`, keine VAT-Prüfung je Probe. Metriken: Latenz, Resultat, Errorcode, Retry, Cachehit, Pending/In-flight/Reject ohne VAT-Label. Anti-Patterns: Client pro Request, unbeschränkte Queue/Futures, sofortige Retryschleife, Cache ohne TTL, `Unavailable`→`Invalid`, öffentliche Lasttests, PII-Logs, vergessenes `close()`. Produktion benötigt Shutdown-Test, Kapazitätsrechnung, Audit/Privacy, Alerts, DLQ, Cache-/Limiter-Failover und Rollbackplan.

## Spring-Boot-Referenz

```java
@Configuration
class ViesConfig {
 @Bean(destroyMethod = "close")
 ViesClient viesClient() { return ViesClient.builder().retries(1).build(); }
}
@RestController
class VatController {
 private final ViesClient vies;
 VatController(ViesClient vies) { this.vies = vies; }
 @GetMapping("/api/vat/{number}")
 ResponseEntity<?> check(@PathVariable String number) {
  return switch (vies.check(number)) {
   case ViesResponse.Valid v -> ResponseEntity.ok(Map.of("valid", true));
   case ViesResponse.Invalid i -> ResponseEntity.ok(Map.of("valid", false));
   case ViesResponse.MalformedInput m -> ResponseEntity.badRequest().body(m.error());
   case ViesResponse.Unavailable u -> ResponseEntity.status(
    "CLIENT_OVERLOADED".equals(u.errorCode()) ? 429 : 503).body(u.error());
  };
 }
}
```

Plain-JDK-Smoke: `./mvnw -q package`, `java -cp target/classes examples/ViesDemoServer.java`, dann `curl "http://localhost:8085/vat-check?number=DE000000000"`.

## Vollständiger Redis-, Queue- und Auditablauf

Der `ViesCache`-Adapter soll nur bestätigte `ViesResponse.Valid`-Objekte speichern. Eine robuste Darstellung enthält Schema-Version, normalisierte VAT, Requester-Schlüssel, Auditzeit, optionale Händlerfelder und Consultation-ID. Das Format muss vor unbekannten Feldern tolerant, vor falschen Typen aber streng sein. Java-native Objektserialisierung ist für verteilte, untrusted Cacheinhalte ungeeignet. Setzen Sie einen kurzen Cachetimeout, überwachen Sie Fehlerquote und Latenz und planen Sie bewusst, ob ein Cacheausfall Readiness beeinflusst.

Die Queue-Nachricht benötigt mindestens Job-ID, VAT, optional Requester, Versuchszähler, frühesten Ausführungszeitpunkt und Korrelations-ID. Verarbeitung soll idempotent sein. Bei `Valid` oder `Invalid` wird das Ergebnis atomar mit dem Queue-Acknowledge persistiert oder über Outbox/vergleichbare Technik abgesichert. Bei retrybarem `Unavailable` wird der nächste Zeitpunkt mit exponentiellem Backoff und Jitter berechnet. Permanente Eingabefehler gehen direkt in die fachliche Fehlerausgabe, wiederholt fehlgeschlagene technische Jobs nach begrenzter Zahl in die DLQ.

Die Consultation-ID ist ein nützlicher Nachweis, aber nicht automatisch ein vollständiges gesetzliches Archiv. Das integrierende Unternehmen definiert Aufbewahrung, Unveränderlichkeit, Zugriff und Löschung. Speichern Sie außerdem normalisierte Anfrage, Requester, UTC-Auditzeit, Antworttyp und stabile Fehlercodes. Händlername und Adresse können fehlen oder als VIES-Platzhalter erscheinen; Geschäftslogik darf optionale Felder nicht als garantiert betrachten.

## Konfiguration, Cancellation und Shutdown

Ein externer `ExecutorService` bleibt Eigentum der Anwendung. Sie muss ihn nach dem Client und nach ihren eigenen abhängigen Tasks schließen. Der Client verfolgt seine eingereichten Handles, damit `close()` aktive und wartende Arbeiten abbrechen kann, ohne den fremden Executor selbst zu beenden. Ein vom Aufrufer gecancelter Future ist eine private Sicht auf das gemeinsame Resultat und darf Followers derselben Single-Flight-Anfrage nicht beschädigen.

Beim Deployment stoppt die Anwendung zuerst neue Ingress-Nachrichten, pausiert Consumer, wartet eine begrenzte Grace Period und schließt dann den Client. `CLIENT_CLOSED` ist keine VAT-Entscheidung. Konkurrierende `close()`-Aufrufer werden serialisiert; Callback-Code läuft außerhalb des Lifecycle-Locks. Diese Reihenfolge muss mit echten Container-Signalen und einem langsamen lokalen Mock getestet werden.

Für Observability eignen sich Counter nach Resultat/Errorcode, Histogramme für Gesamt-, Cache- und HTTP-Latenz, Gauges für Pending/In-flight und Counter für Retry/Reject. VAT, Firmenname, Adresse und Consultation-ID sind keine Labels. Logs enthalten Korrelation und Maschinen-Code; Debug-Payloads müssen redigiert und zeitlich begrenzt sein. Alerts sollten anhaltende Timeouts, Cachefehler, Overload, DLQ-Wachstum und ungewöhnliche Invalid-Raten unterscheiden.

## Multi-Node-Kapazitätsplanung

Die lokale Netzwerkgrenze multipliziert sich mit der Anzahl der Pods. Zehn Pods mit `maxConcurrentRequests(32)` könnten theoretisch 320 parallele Aufrufe erzeugen. Darum muss ein gemeinsamer Limiter vor der Bibliothek liegen und möglichst Mitgliedstaat-spezifische Budgets berücksichtigen. Autoscaling allein kann ein Upstream-Problem verschärfen. Scale-out soll anhand Queuealter, lokaler Auslastung und erlaubtem globalem Budget erfolgen, nicht nur anhand CPU.
