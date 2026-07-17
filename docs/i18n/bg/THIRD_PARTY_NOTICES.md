# Български (bg) — Компоненти на трети страни

**Languages:** [English](../../../THIRD_PARTY_NOTICES.md) · [Български](THIRD_PARTY_NOTICES.md) · [Всички езици](../../LANGUAGES.md)

> Информационен превод. При различие водещ е английският технически и правен оригинал. Само кореновите LICENSE и NOTICE са правно меродавни; преводът не е лиценз. [LICENSE](../../../LICENSE), [NOTICE](../../../NOTICE).

vies-client-1.0.0.jar contains/requires no third-party runtime library; only JDK java.base and java.net.http.

| Component | Version | Scope | License |
|---|---:|---|---|
| JUnit Jupiter | 6.1.2 | test only | EPL-2.0 |

Maven plugins/transitive tests are not bundled; Maven resolves own metadata/licenses. Source: <https://github.com/junit-team/junit-framework>. Any new dependency PR updates this file and license compatibility.

При промяна на зависимост pull request трябва да провери произхода, лиценза, обхвата и че компонентът не влиза в runtime JAR.
