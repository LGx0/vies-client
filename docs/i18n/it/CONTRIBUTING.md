# Italiano (`it`) — Contribuire

[Selettore lingua](../../LANGUAGES.md) · [Originale inglese](../../../CONTRIBUTING.md)

> Prevale l’inglese e il `LICENSE` originale.

Citare issue per bug; discutere prima grandi modifiche API/licenza/architettura. Sicurezza solo via [SECURITY.md](SECURITY.md). Requisiti: JDK 21+, Maven 3.9+, Git. Fork, branch breve da `main`, modifica focalizzata, regressione deterministica, `./mvnw --batch-mode --no-transfer-progress clean verify`, PR completa.

Java 21, API piccola/tipizzata, nessuna runtime dependency non concordata, mai `Unavailable`→`Invalid`, stato condiviso thread-safe/limitato, API/concorrenza documentate EN/HU, niente dati VAT/azienda/indirizzo/requester privati. Test senza rete pubblica, solo loopback, mai load VIES, race con latch/barrier.

PR verde, comportamento documentato, dependency motivate, breaking change evidenti, performance riproducibile, diritti confermati. AI consentita sotto piena responsabilità: revisionare/testare, verificare origine/licenza, dichiarare aiuto sostanziale, niente confidenziali. Contributi Apache-2.0 section 5 e [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

## Procedura completa di sviluppo e pubblicazione

Verificare `java -version`, `javac -version`, `./mvnw -version` su JDK 21. Workflow: fork aggiornato, branch breve da `main`, modifica focalizzata, regression deterministica, `./mvnw --batch-mode --no-transfer-progress clean verify`. Non committare `target/`, IDE, `.env`, token, path macchina, dati reali o asset senza diritto. Maven produce binary/sources/Javadoc; `jar --describe-module` verifica JPMS. Gradle usa versione fissa e `mavenLocal()` solo sviluppo.

API pubblica piccola, immutable, type-safe, zero runtime dependency. Unavailable mai Invalid. Shared state thread-safe, memory-bounded, cleanup su success/timeout/interrupt/cancel/rejection/parse/close. API e concurrency complessa hanno Javadoc/commenti EN/HU su invarianti e ownership. Performance claim indica JDK, hardware, warm-up, payload, cache, concurrency, p50/p95/p99 e non usa VIES pubblico.

PR descrive motivazione, prima/dopo, API/config, test, security/privacy, compatibilità, benchmark, breaking. Dependency aggiorna third-party/licenza; feature aggiorna tutte docs/localizzazioni. Autore AI verifica ogni riga, origine, licenza, API, dichiara assistenza, non invia confidential. Review controlla lock order, ownership, cleanup, cache key/TTL/schema/error, retry class/max/backoff/jitter e shutdown.

GitHub Public `vies-client`, `main`, senza duplicati README/license/gitignore. Prima push: staged diff e secret scan. Attivare Issues, Discussions, private vulnerability, Dependabot, secret/push protection, CodeQL, Dependency Review. Ruleset richiede PR, approvazione, conversations e check verdi, vieta force/delete, limita bypass. Actions pinned, permissions minime, niente secret ai fork.

Labels bug, enhancement, documentation, localization, security, dependencies, performance, concurrency, breaking-change, good-first-issue, help-wanted, triage. Topics java21, vies, vat, eu, rest-client, virtual-threads, single-flight, jpms, zero-dependency. Funding solo URL verificato senza diritti. Aggiornare POM URL/SCM/developer, badge, advisory e docs con owner.

Release SemVer con Git pulito, changelog, Apache/NOTICE/third-party, scans, test ripetuti, link/traduzioni. Tag `vX.Y.Z` firmato; binary, sources, Javadoc, SHA-256 stesso CI e immutabili. Central richiede namespace/POM/firme/secrets; Packages config consumer. Provare classpath/JPMS vuoti. Difetto: nuova patch e rollback, mai overwrite.

Rete: DNS, HTTPS CONNECT, proxy auth e truststore; secrets in vault, TLS mai disabilitato. Kubernetes configura egress, CA, non-root, CPU/heap/connections e grace. Prima suite offline/demo, live smoke minimo. IDE SDK/language/Maven JDK a 21 e niente path locali. Archiviare report/checksum del JAR distribuito.

Prima della review aggiornare il branch e risolvere conflitti senza eliminare lavoro altrui. Il PR resta draft finché test, documentazione, changelog e notices non sono completi. Il reviewer verifica success, timeout, interrupt, cancel, rejection e close; per cache key/TTL/schema/error; per retry classification/max/backoff/jitter. Un cambiamento hot path include misura prima/dopo e trade-off CPU/memoria/complessità.

Compatibilità source e binary è importante: rinominare tipo, record component, metodo o modulo può richiedere major. Aggiungere metodo astratto a `ViesCache` rompe adapter. Nuovi error code restano stabili. Localizzazioni conservano API/comandi, struttura canonica e autorità inglese, senza presentare traduzioni di licenza come vincolanti.
