# Italiano (`it`) — Installazione

[Selettore lingua](../../LANGUAGES.md) · [Originale inglese](../../INSTALLATION.md)

> In caso di differenza prevale l’inglese. `LICENSE` resta non tradotto e vincolante.

Richiede JDK 21+, Maven 3.9+ e in produzione HTTPS verso `ec.europa.eu:443`. I test locali non richiedono Internet.

```bash
java -version
./mvnw -version
git clone <repository-url>
cd vies-client
./mvnw clean verify
./mvnw install
```

macOS: `brew install openjdk@21`; Linux: pacchetto JDK 21 della distribuzione; Windows: impostare `JAVA_HOME` e aggiungere `%JAVA_HOME%\bin` a `Path`. Impostare anche SDK e Maven JDK dell’IDE a 21.

In `target/` vengono prodotti JAR, sources e Javadocs. Maven: `vies.client:vies-client:1.2.0`; Gradle: `implementation("vies.client:vies-client:1.2.0")`. JPMS: `requires vies.client;`; su classpath non serve. Senza build tool usare `--module-path`/`-cp`; copiando i sorgenti in progetto non modulare omettere `module-info.java`.

Gestire proxy/certificati nell’infrastruttura senza credenziali nel repository. Verifica:

```bash
./mvnw -q clean test
./mvnw -q package
jar --describe-module --file target/vies-client-1.2.0.jar
```

Le chiamate live devono essere smoke manuali minimi. In caso di errore controllare JDK, proxy/TLS, indisponibilità VIES/Stato membro e limiti locali.

## Piattaforma, build, IDE e rete completi

macOS: persistere Homebrew e `JAVA_HOME="$(/usr/libexec/java_home -v 21)"`; Linux: `sudo apt install openjdk-21-jdk maven`; Windows: `JAVA_HOME`/`Path`, nuovo terminale. Verificare java/javac/mvn.

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
./mvnw install
ls -lh target/vies-client-1.2.0*.jar
javac --release 21 -cp vies-client-1.2.0.jar MyApp.java
java -cp .:vies-client-1.2.0.jar MyApp # Windows ;
```

JAR senza runtime terzi; JUnit/`jdk.httpserver` solo test. IDE SDK/language/Maven JDK e impostazioni Java a 21, reload, niente path locali committati. HTTPS verso `https://ec.europa.eu/taxation_customs/vies/`, proxy/truststore centrali, mai disabilitare TLS. Container JDK/JRE21, CA, non-root e limiti.

| Problema | Fix |
|---|---|
| Java 14 / release 21 | SDK 21 e `JAVA_HOME` Maven |
| `UnsupportedClassVersionError` | runtime 21+ |
| `NETWORK_ERROR` | DNS/proxy/firewall/TLS |
| `TIMEOUT` | indagare, non timeout infinito |
| `CLIENT_OVERLOADED` | retry ritardato, ingress/worker/limiter |
| `CACHE_ERROR` | Redis health/timeout, evitare stampede |

## Procedura completa di sviluppo e pubblicazione

Verificare `java -version`, `javac -version`, `./mvnw -version` su JDK 21. Workflow: fork aggiornato, branch breve da `main`, modifica focalizzata, regression deterministica, `./mvnw --batch-mode --no-transfer-progress clean verify`. Non committare `target/`, IDE, `.env`, token, path macchina, dati reali o asset senza diritto. Maven produce binary/sources/Javadoc; `jar --describe-module` verifica JPMS. Gradle usa versione fissa e `mavenLocal()` solo sviluppo.

API pubblica piccola, immutable, type-safe, zero runtime dependency. Unavailable mai Invalid. Shared state thread-safe, memory-bounded, cleanup su success/timeout/interrupt/cancel/rejection/parse/close. API e concurrency complessa hanno Javadoc/commenti EN/HU su invarianti e ownership. Performance claim indica JDK, hardware, warm-up, payload, cache, concurrency, p50/p95/p99 e non usa VIES pubblico.

PR descrive motivazione, prima/dopo, API/config, test, security/privacy, compatibilità, benchmark, breaking. Dependency aggiorna third-party/licenza; feature aggiorna tutte docs/localizzazioni. Autore AI verifica ogni riga, origine, licenza, API, dichiara assistenza, non invia confidential. Review controlla lock order, ownership, cleanup, cache key/TTL/schema/error, retry class/max/backoff/jitter e shutdown.

GitHub Public `vies-client`, `main`, senza duplicati README/license/gitignore. Prima push: staged diff e secret scan. Attivare Issues, Discussions, private vulnerability, Dependabot, secret/push protection, CodeQL, Dependency Review. Ruleset richiede PR, approvazione, conversations e check verdi, vieta force/delete, limita bypass. Actions pinned, permissions minime, niente secret ai fork.

Labels bug, enhancement, documentation, localization, security, dependencies, performance, concurrency, breaking-change, good-first-issue, help-wanted, triage. Topics java21, vies, vat, eu, rest-client, virtual-threads, single-flight, jpms, zero-dependency. Funding solo URL verificato senza diritti. Aggiornare POM URL/SCM/developer, badge, advisory e docs con owner.

Release SemVer con Git pulito, changelog, Apache/NOTICE/third-party, scans, test ripetuti, link/traduzioni. Tag `vX.Y.Z` firmato; binary, sources, Javadoc, SHA-256 stesso CI e immutabili. Central richiede namespace/POM/firme/secrets; Packages config consumer. Provare classpath/JPMS vuoti. Difetto: nuova patch e rollback, mai overwrite.

Rete: DNS, HTTPS CONNECT, proxy auth e truststore; secrets in vault, TLS mai disabilitato. Kubernetes configura egress, CA, non-root, CPU/heap/connections e grace. Prima suite offline/demo, live smoke minimo. IDE SDK/language/Maven JDK a 21 e niente path locali. Archiviare report/checksum del JAR distribuito.

Troubleshooting conserva machine code: `NETWORK_ERROR` controlla DNS/proxy/firewall/TLS; `TIMEOUT` distingue connect/request/admission; `CLIENT_OVERLOADED` richiede backpressure, non loop; `CACHE_ERROR` controlla Redis senza bypass. `UnsupportedClassVersionError` significa runtime vecchio. Dopo cambio JDK eseguire `./mvnw clean` per evitare classi miste.

Documentare sempre versione JDK, Maven, sistema operativo e checksum dell’artefatto installato.
