# Svenska (sv) — CHANGELOG

> [Alla språk](../../LANGUAGES.md) · Informativ översättning. Vid avvikelser gäller den kanoniska engelska tekniska eller juridiska källan. Endast `LICENSE` och `NOTICE` i roten är juridiskt auktoritativa; översättningen ersätter dem inte.

Väsentliga förändringar dokumenteras i denna fil. Projektet är semantiskt
följer versionshantering:`MAJOR.MINOR.PATCH`.

Alla anmärkningsvärda förändringar dokumenteras här. Projektet följer Semantisk versionering:
`MAJOR.MINOR.PATCH`.

## [Ej släppt]

## [1.2.0] - 2026-07-17

### Tillagd

- GitHub-gemenskapshälsofiler, CI/säkerhetsautomatisering och öppen källkodsstyrning.
- Apache License 2.0-projektlicenser och Maven-metadata.
- Dokumentationspaket på alla 24 officiella EU-språk.
- GitHub Sponsor/Buy Me a Coffee-integrering och flerspråkiga upptäcktsvillkor.

### Ändrad

- Testverktygskedja uppdaterad till JUnit Jupiter 6.1.2 och nuvarande stabila Maven-plugins.
- Slutförande av avstängning använder nu virtuella trådar, vilket förhindrar inbyggd trådförstärkning
  när många verksamheter är stängda samtidigt.

## [1.0.0] - 2026-07-17

### Tillagd

- Java 21, noll körtidsberoende VIES REST-klient.
- Synkrona och asynkrona API:er med standardinställningar för virtuell tråd.
- Begränsad synkronisering, asynkron och utgående nätverksanslutning.
- JVM-lokal enkelflygningsförfrågan sammansmältning.
- Begränsad TTL-cache och extern`ViesCache`-förlängningspunkt.
- Strikt svarsvalidering och förseglad resultathierarki.
- Stabila tvåspråkiga ungerska/engelska strukturerade fel.
- Försök igen med exponentiell backoff och jitter.
- Test av deterministisk enhet, HTTP, samtidighet, annullering och avstängning.
- Installations-, integrations-, teknisk- och testdokumentation.
