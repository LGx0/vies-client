# Čeština (cs) — Komponenty třetích stran

**Jazyky:** [English](../../../THIRD_PARTY_NOTICES.md) · [Čeština](THIRD_PARTY_NOTICES.md) · [Dokumentace](README.md)

> Informativní překlad; při rozporu platí anglický originál. Jediná závazná licence je [LICENSE](../../../LICENSE); překlad není licencí.

Právně směrodatné anglické soubory [LICENSE](../../../LICENSE) a [NOTICE](../../../NOTICE) zůstávají nezměněné; tento překlad je nenahrazuje.

## Distribuovaný runtime JAR

`vies-client-1.2.0.jar` neobsahuje ani nevyžaduje knihovny třetích stran; používá pouze JDK moduly `java.base` a `java.net.http`.

## Build a test

| Komponenta | Verze | Rozsah | Licence |
|---|---:|---|---|
| JUnit Jupiter | 6.1.2 | pouze test | EPL-2.0 |

Maven pluginy a tranzitivní test dependency nejsou součástí release JAR; Maven je řeší podle jejich metadat/licencí. Zdroj: <https://github.com/junit-team/junit-framework>. Každý PR přidávající dependency musí aktualizovat tento soubor a doložit kompatibilitu licence.
