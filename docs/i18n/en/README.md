# VIES VAT checker — Java EU tax ID validation client

[All EU languages](../../LANGUAGES.md) · [Complete quick start](../../QUICK_START.md) · [Releases](https://github.com/LGx0/vies-client/releases)

A Java 21+ client for the European Commission's VIES REST API. It has no runtime
dependencies, provides synchronous and asynchronous checks, coalesces identical
in-flight requests, and includes bounded admission and an optional TTL cache.
Development and security analysis use JDK 25 while retaining Java 21 compatibility.

This independent project is not an official product of the European Commission,
the EU, or any tax authority. It checks EU VAT registration through VIES; it is not
a general tax calculator.

## Start here

The library is distributed through GitHub Releases and source builds, **not Maven
Central**. Follow the [quick start](../../QUICK_START.md) for checksum/provenance
verification, local Maven installation, Gradle setup, an offline sanity check,
and a complete runnable example.

Use one shared client per application. Handle all four results separately:
`Valid`, `Invalid`, `Unavailable`, and `MalformedInput`. An unavailable result is
not an invalid VAT number. Optional names, addresses, and consultation IDs may be
absent; cache hits retain the original consultation timestamp and identifier.

## Documentation

- [Installation](INSTALLATION.md)
- [Integration, Spring Boot, Redis and HTTP contracts](../../INTEGRATION.md)
- [Technical design and concurrency limits](../../TECHNICAL.md)
- [Testing: 81 deterministic cases](TESTING.md)
- [Security policy](../../../SECURITY.md) and [review/risk assessment](../../SECURITY_REVIEW.md)
- [Release and provenance verification](../../RELEASING.md)
- [License and open-source governance](../../OPEN_SOURCE.md)
- [Changelog](../../../CHANGELOG.md)
- [Complete CLI example](../../../examples/CheckVat.java)

For large deployments, use a durable bounded queue, shared cache and global
limiter. VIES upstream availability and throttling determine achievable capacity;
virtual threads do not remove upstream limits.

Apache-2.0. Retain the LICENSE and NOTICE files when redistributing.
Support is optional: [Buy Me a Coffee](https://buymeacoffee.com/lgx0).
