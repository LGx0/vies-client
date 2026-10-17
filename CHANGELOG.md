# Változásnapló / Changelog

A jelentős változásokat ebben a fájlban dokumentáljuk. A projekt szemantikus
verziózást követ: `MAJOR.MINOR.PATCH`.

All notable changes are documented here. The project follows Semantic Versioning:
`MAJOR.MINOR.PATCH`.

## [Unreleased]

## [1.2.1] - 2026-10-17

### Fixed / Javítva

- Enforce the request timeout through the full response body and cancel the HTTP
  exchange on timeout/interruption. On JDK 21 a peer could previously stall after
  headers and hold a network slot indefinitely. A teljes válaszletöltés határidős;
  az elakadt válasz többé nem foglalhat tartósan hálózati kapacitást.
- Verify tag signature and protected-main ancestry before executing the repository's
  Maven wrapper/POM during release. A kiadási forrás megbízhatóságát minden
  repository-vezérelt kódfuttatás előtt igazoljuk.
- Preserve literal `+` in Irish requester VAT numbers by encoding the query value.
  Az ír lekérdezői adószám `+` jele URL-kódolással változatlan marad.
- Reject signed or non-ASCII Unicode escapes in JSON; only four ASCII hex digits
  are accepted. A JSON Unicode escape kizárólag négy ASCII hex számjegy lehet.
- Reject malformed availability schemas and duplicate known country entries instead
  of reporting a false outage or silently overwriting status. A hibás állapotválasz
  explicit hibát ad; az opcionális, hiányos országlista továbbra is támogatott.
- Reject overflowing connect/request timeouts at configuration time with bilingual
  validation errors. A túl nagy hálózati timeout már a beállításkor hibát jelez.
- Add eight regression tests: the suite now has 81 tests (47 unit, 34 local HTTP/
  concurrency). Nyolc új regressziós teszt egészíti ki a meglévő csomagot.

### Documentation / Dokumentáció

- Add a bilingual risk assessment, verified installation from GitHub Releases,
  complete English quick start, and clearer cache/deadline/integration boundaries.
  Kétnyelvű kockázatelemzés, letölthető kiadásból telepítés és pontosított bekötési útmutató.

## [1.2.0] - 2026-07-17

### Added

- GitHub community health files, CI/security automation, and open-source governance.
- Apache License 2.0 project licensing and Maven metadata.
- Documentation packages in all 24 official EU languages.
- GitHub Sponsor/Buy Me a Coffee integration and multilingual discovery terms.

### Changed

- Test toolchain updated to JUnit Jupiter 6.1.2 and current stable Maven plugins.
- Shutdown completion now uses virtual threads, preventing native-thread amplification
  when many operations are closed simultaneously.
- Security hardening now bounds raw input, UTF-8 response bytes, JSON depth/value
  count/numbers/fields, validates VAT echo and cache values, hashes external cache
  keys, disables redirects, restricts insecure endpoints, and sanitizes public errors.
- Async admission no longer invokes inline custom executors under the lifecycle lock;
  fatal-error cleanup and close races have deterministic regression coverage.
- Release automation now uses SHA-pinned Actions, signed-tag/version/main validation,
  read-only builds, isolated publication, provenance attestation and reproducible JARs.
- The deterministic suite now contains 73 tests, including 29 local HTTP/concurrency tests.

## [1.0.0] - 2026-07-17

### Added

- Java 21, zero-runtime-dependency VIES REST client.
- Synchronous and asynchronous APIs with virtual-thread defaults.
- Bounded sync, async, and outbound-network admission.
- JVM-local single-flight request coalescing.
- Bounded TTL cache and external `ViesCache` extension point.
- Strict response validation and sealed result hierarchy.
- Stable bilingual Hungarian/English structured errors.
- Retry with exponential backoff and jitter.
- Deterministic unit, HTTP, concurrency, cancellation, and shutdown tests.
- Installation, integration, technical, and testing documentation.
