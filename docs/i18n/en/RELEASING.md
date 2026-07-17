# English (en) — Releasing

> [Language selector](../../LANGUAGES.md) · The English technical/legal source governs differences. Only the root `LICENSE` is authoritative.

Use Semantic Versioning: PATCH for compatible fixes, MINOR for compatible features, MAJOR for breaking public API or semantics.

Prerequisites: clean reviewed `main`, green GitHub Actions, JDK 21/Maven 3.9+, release permissions, and version/date in `CHANGELOG.md`.

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
jar --describe-module --file target/vies-client-1.2.0.jar
jdeps --print-module-deps target/vies-client-1.2.0.jar
```

Before release, scan the complete Git history for secrets/personal data; verify `LICENSE`, `NOTICE`, `SECURITY.md`, translations and API docs; ensure binary/sources/Javadoc JARs exist; keep live VIES/load tests optional; document public changes and migration impact.

Set the version in `pom.xml`, commit version/changelog, create a signed annotated tag (`git tag -s v1.2.0 -m "v1.2.0"`), then `git push origin main --follow-tags`. The release workflow must rerun verification and attach binary, sources and Javadoc JARs, ideally with SHA-256 checksums. Never release an unreviewed or failing commit.

Maven Central additionally requires an owned reverse-DNS `groupId`, project/SCM/developer metadata, Central Portal credentials, artifact signing and publishing setup. Never commit tokens or private signing keys; use least-privilege GitHub secrets.

After release, verify downloads/checksums, open a new `[Unreleased]` changelog section, publish a Security Advisory for security fixes, and update documented dependency versions and localized pages.
