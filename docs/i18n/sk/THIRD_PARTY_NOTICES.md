# Slovenčina (sk) — Komponenty tretích strán

**Jazyky:** [English](../../../THIRD_PARTY_NOTICES.md) · [Slovenčina](THIRD_PARTY_NOTICES.md) · [Dokumentácia](README.md)

> Informatívny preklad; rozhoduje anglický originál. Jediná záväzná licencia je [LICENSE](../../../LICENSE); preklad nie je licenciou.

Právne smerodajné anglické súbory [LICENSE](../../../LICENSE) a [NOTICE](../../../NOTICE) zostávajú nezmenené; tento preklad ich nenahrádza.

## Distribuovaný runtime JAR

`vies-client-1.2.0.jar` neobsahuje ani nevyžaduje third-party runtime library; iba JDK `java.base`, `java.net.http`.

## Závislosti pre build a testy

| Komponent | Verzia | Scope | Licencia |
|---|---:|---|---|
| JUnit Jupiter | 6.1.2 | len test | EPL-2.0 |

Maven plugins/transitive tests nie sú bundled; Maven ich rieši podľa ich metadata/licenses. Zdroj: <https://github.com/junit-team/junit-framework>. Nová dependency musí aktualizovať tento súbor a license compatibility.
