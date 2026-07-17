# Változásnapló / Changelog

A jelentős változásokat ebben a fájlban dokumentáljuk. A projekt szemantikus
verziózást követ: `MAJOR.MINOR.PATCH`.

All notable changes are documented here. The project follows Semantic Versioning:
`MAJOR.MINOR.PATCH`.

## [Unreleased]

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
