# English (en) — Third-party components
> [Language selector](../../LANGUAGES.md) · Root English `LICENSE`, `NOTICE`, and `THIRD_PARTY_NOTICES.md` are legally authoritative; this localization is informational.

The runtime JAR bundles and requires no third-party library; it uses JDK `java.base` and `java.net.http`. JUnit Jupiter 6.1.2 (EPL-2.0) is test-only. Maven plugins and transitive test dependencies are not bundled and are resolved under their own metadata/licenses. Source: <https://github.com/junit-team/junit-framework>. Any dependency-adding PR must update notices and document license compatibility.
