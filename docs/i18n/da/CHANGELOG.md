# Dansk (da) — CHANGELOG

> [Alle sprog](../../LANGUAGES.md) · Informativ oversættelse. Ved afvigelser er den kanoniske engelske tekniske eller juridiske kilde gældende. Kun `LICENSE` og `NOTICE` i roden er juridisk autoritative; oversættelsen erstatter dem ikke.

Væsentlige ændringer er dokumenteret i denne fil. Projektet er semantisk
følger versionering:`MAJOR.MINOR.PATCH`.

Alle bemærkelsesværdige ændringer er dokumenteret her. Projektet følger Semantisk versionering:
`MAJOR.MINOR.PATCH`.

## [Ikke udgivet]

## [1.2.0] - 2026-07-17

### Tilføjet

- GitHub-samfundssundhedsfiler, CI/sikkerhedsautomatisering og open source-styring.
- Apache License 2.0-projektlicenser og Maven-metadata.
- Dokumentationspakker på alle 24 officielle EU-sprog.
- GitHub Sponsor/Buy Me a Coffee integration og flersprogede opdagelsesbetingelser.

### Ændret

- Testværktøjskæde opdateret til JUnit Jupiter 6.1.2 og nuværende stabile Maven-plugins.
- Afslutning af nedlukning bruger nu virtuelle tråde, hvilket forhindrer native-thread-forstærkning
  når mange operationer lukkes samtidigt.

## [1.0.0] - 2026-07-17

### Tilføjet

- Java 21, nul-runtime-afhængig VIES REST-klient.
- Synkrone og asynkrone API'er med standardindstillinger for virtuel tråd.
- Afgrænset synkronisering, asynkron og udgående netværksadgang.
- JVM-lokal single-flight anmodning koalescing.
- Afgrænset TTL-cache og eksternt`ViesCache`-udvidelsespunkt.
- Streng responsvalidering og forseglet resultathierarki.
- Stabile tosprogede ungarsk/engelsk strukturerede fejl.
- Prøv igen med eksponentiel backoff og jitter.
- Test af deterministisk enhed, HTTP, samtidighed, annullering og nedlukning.
- Installations-, integrations-, teknisk- og testdokumentation.
