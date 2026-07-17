# Italiano (`it`) — Rilascio

[Selettore lingua](../../LANGUAGES.md) · [Originale inglese](../../RELEASING.md)

> Prevale l’inglese. `LICENSE` resta non tradotto e vincolante.

Rilasciare da commit pulito e verificato con JDK 21, Maven e accessi sicuri. SemVer: MAJOR incompatibile, MINOR funzione compatibile, PATCH correzione compatibile.

```bash
./mvnw clean verify
./mvnw package
jar --describe-module --file target/vies-client-1.2.0.jar
```

Controllare versione POM, changelog, esempi, link lingue, Javadocs/sources, licenza/security, assenza segreti e test concorrenti ripetuti. VIES live solo smoke minimo.

Creare tag firmato/annotato `vX.Y.Z` e GitHub Release. Note: novità, API/config, fix, migrazione, limiti, checksum. Allegare JAR, sources e Javadocs dello stesso CI. Maven Central richiede metadata, sources/Javadocs, firma e secrets; GitHub Packages configurazione consumer. Dopo il rilascio testare progetto vuoto, indici e documenti. Mai sovrascrivere artefatti: pubblicare PATCH.

## Procedura completa di sviluppo e pubblicazione

Verificare `java -version`, `javac -version`, `./mvnw -version` su JDK 21. Workflow: fork aggiornato, branch breve da `main`, modifica focalizzata, regression deterministica, `./mvnw --batch-mode --no-transfer-progress clean verify`. Non committare `target/`, IDE, `.env`, token, path macchina, dati reali o asset senza diritto. Maven produce binary/sources/Javadoc; `jar --describe-module` verifica JPMS. Gradle usa versione fissa e `mavenLocal()` solo sviluppo.

API pubblica piccola, immutable, type-safe, zero runtime dependency. Unavailable mai Invalid. Shared state thread-safe, memory-bounded, cleanup su success/timeout/interrupt/cancel/rejection/parse/close. API e concurrency complessa hanno Javadoc/commenti EN/HU su invarianti e ownership. Performance claim indica JDK, hardware, warm-up, payload, cache, concurrency, p50/p95/p99 e non usa VIES pubblico.

PR descrive motivazione, prima/dopo, API/config, test, security/privacy, compatibilità, benchmark, breaking. Dependency aggiorna third-party/licenza; feature aggiorna tutte docs/localizzazioni. Autore AI verifica ogni riga, origine, licenza, API, dichiara assistenza, non invia confidential. Review controlla lock order, ownership, cleanup, cache key/TTL/schema/error, retry class/max/backoff/jitter e shutdown.

GitHub Public `vies-client`, `main`, senza duplicati README/license/gitignore. Prima push: staged diff e secret scan. Attivare Issues, Discussions, private vulnerability, Dependabot, secret/push protection, CodeQL, Dependency Review. Ruleset richiede PR, approvazione, conversations e check verdi, vieta force/delete, limita bypass. Actions pinned, permissions minime, niente secret ai fork.

Labels bug, enhancement, documentation, localization, security, dependencies, performance, concurrency, breaking-change, good-first-issue, help-wanted, triage. Topics java21, vies, vat, eu, rest-client, virtual-threads, single-flight, jpms, zero-dependency. Funding solo URL verificato senza diritti. Aggiornare POM URL/SCM/developer, badge, advisory e docs con owner.

Release SemVer con Git pulito, changelog, Apache/NOTICE/third-party, scans, test ripetuti, link/traduzioni. Tag `vX.Y.Z` firmato; binary, sources, Javadoc, SHA-256 stesso CI e immutabili. Central richiede namespace/POM/firme/secrets; Packages config consumer. Provare classpath/JPMS vuoti. Difetto: nuova patch e rollback, mai overwrite.

Rete: DNS, HTTPS CONNECT, proxy auth e truststore; secrets in vault, TLS mai disabilitato. Kubernetes configura egress, CA, non-root, CPU/heap/connections e grace. Prima suite offline/demo, live smoke minimo. IDE SDK/language/Maven JDK a 21 e niente path locali. Archiviare report/checksum del JAR distribuito.
