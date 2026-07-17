# Čeština (cs) — Přehled změn

**Jazyky:** [English](../../../CHANGELOG.md) · [Čeština](CHANGELOG.md) · [Dokumentace](README.md)

> Informativní překlad; při rozporu platí anglický originál. Jediná závazná licence je [LICENSE](../../../LICENSE); překlad není licencí.

Významné změny jsou zde dokumentovány podle `MAJOR.MINOR.PATCH`.

## [Unreleased]

## [1.2.0] - 2026-07-17

### Přidáno

- GitHub community health soubory, CI/security automatizace a open-source governance.
- Licencování Apache License 2.0 a Maven metadata.

## [1.0.0] - 2026-07-17

### Přidáno

- Java 21 VIES REST klient bez runtime dependencies; sync/async API s virtual threads.
- Omezený sync/async/network admission, JVM single-flight, bounded TTL cache a `ViesCache`.
- Přísná validace, sealed výsledky, stabilní HU/EN chyby, exponential backoff+jitter.
- Deterministické unit/HTTP/concurrency/cancel/shutdown testy a úplná dokumentace.
