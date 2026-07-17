# Italiano (`it`) — Registro modifiche

[Selettore lingua](../../LANGUAGES.md) · [Originale inglese](../../../CHANGELOG.md)

> Traduzione informativa; prevale inglese. `LICENSE` non tradotto. SemVer `MAJOR.MINOR.PATCH`.

## [Unreleased]

## [1.2.0] - 2026-07-17
### Aggiunto
- File community GitHub, automazione CI/security e governance open source.
- Licenza Apache-2.0 e metadata Maven.

## [1.0.0] - 2026-07-17
### Aggiunto
- Client REST VIES Java 21 senza runtime dependencies.
- API sync/async con virtual threads e admission sync/async/rete limitata.
- Single-flight JVM, cache TTL limitata, estensione `ViesCache`.
- Validazione rigorosa, risultati sealed, errori HU/EN.
- Retry esponenziale+jitter; test deterministici unit, HTTP, concurrency, cancellation, shutdown.
- Documenti installazione, integrazione, tecnica e test.

## Comunità, sicurezza e licenza complete

Apache-2.0 consente uso privato/commerciale, modifica e distribuzione conservando licenza, copyright, modifiche e `NOTICE`. Include patent grant/termination, nessuna garanzia, responsabilità o marchio. Solo `LICENSE` e `NOTICE` inglesi sono vincolanti. JUnit Jupiter 6.1.2 è EPL-2.0 test-only, non runtime. Nuova dependency documenta nome, versione, scope, fonte, licenza, bundling e compatibilità.

Contributi secondo Apache section 5 se l’autore ha diritto. Controllare Git history, segreti, dati fiscali/personali, codice datore/cliente, screenshot, asset. Progetto indipendente, non approvato da Commissione/VIES. Donazione volontaria non compra supporto, priorità, SLA, security exception, IP o governance.

Comunità aperta e senza molestie. Criticare codice/idee, rispettare esperienza/identità, proteggere dati. Vietati minacce, discriminazione, doxxing, attenzione indesiderata, spam, inganno, disclosure irresponsabile, falsa authorship. Report privato con contesto; moderazione, rimozione dati, warning/suspension/ban proporzionati, no retaliation, conflitti dichiarati.

Vulnerabilità non in issue pubblico. Security Advisory con versione, JDK/OS, config redatta, reproduction, expected/actual, impact, workaround, PoC sintetico. Target non SLA: 7 giorni acknowledgement, 14 assessment. Scope injection/SSRF, TLS, leak/log, cache poisoning/deserialization, resource leak, deadlock, limit bypass, false-decision race. Upstream down correttamente Unavailable non è client vulnerability. Dopo fix regression, advisory, changelog, affected, upgrade; no bug bounty garantito.

Supporto: versione, ambiente, build, reproduction e machine code senza dati. Bug template, domande Discussions, security advisory. Best effort, non consulenza fiscale/legale/contabile. Changelog usa Added, Changed, Fixed, Security, Deprecated, Removed; Unreleased raccoglie lavoro. Release notes includono migration, limits, checksum, rollback.
