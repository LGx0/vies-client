# Slovenčina (sk) — Open-source projekt

**Jazyky:** [English](../../OPEN_SOURCE.md) · [Čeština](../cs/OPEN_SOURCE.md) · [Polski](../pl/OPEN_SOURCE.md) · [Slovenčina](OPEN_SOURCE.md) · [Slovenščina](../sl/OPEN_SOURCE.md) · [Hrvatski](../hr/OPEN_SOURCE.md) · [Română](../ro/OPEN_SOURCE.md) · [Български](../bg/OPEN_SOURCE.md) · [Ελληνικά](../el/OPEN_SOURCE.md)

> Informatívny preklad. Rozhoduje anglický originál; jedinou záväznou licenciou je [LICENSE](../../../LICENSE). Tento text nie je licenciou.

Jediná licencia projektu je **Apache License 2.0** (`SPDX: Apache-2.0`): povoľuje súkromné, komerčné a open-source použitie, zmenu a distribúciu; vyžaduje zachovanie licencie/attribution, označenie zmien a poskytuje explicitný patentový grant. MIT je kratšia, no nemá rovnako podrobné patentové podmienky. Nejde o dual license.

Pred publikovaním vlastník overí práva k celému kódu/dokumentácii, práva zamestnávateľa/klienta, absenciu kopírovaného kódu, secretov a osobných/zákazníckych dát a licencie third-party materiálu.

Projekt je nezávislý, nie oficiálny ani podporený produkt EK, EÚ či daňového orgánu. Contributions upravuje Apache-2.0 §5 a [CONTRIBUTING.md](../../../CONTRIBUTING.md); samostatná CLA teraz nie je. Text nie je individuálna právna rada.

## Podrobné pravidlá správy projektu

Projekt používa jedinú licenciu Apache-2.0. Preklady licencie nie sú právne záväzné; rozhodujú koreňový anglický `LICENSE` a príslušný `NOTICE`. Pred publikovaním alebo prijatím contribution treba overiť pôvod kódu, práva autora, možné práva zamestnávateľa či klienta, neprítomnosť secretov a osobných údajov a kompatibilitu third-party materiálov. Projekt je nezávislý a nie je produktom ani odporúčaním Európskej komisie, EÚ alebo daňového orgánu.

Zmena začína issue alebo diskusiou, najmä ak mení public API, licenciu, concurrency model alebo architektúru. Contributor vytvorí krátky branch z `main`, zachová malý scope, pridá deterministic regression test a spustí `./mvnw --batch-mode --no-transfer-progress clean verify`. Unit test nepoužíva verejnú sieť; HTTP/concurrency test iba loopback mock. Load a stress proti public VIES sú zakázané. Race riadi latch/barrier, nie pevný sleep.

Public API zostáva malé, type-safe a Java 21. Runtime modul neprijíma novú dependency bez výslovného rozhodnutia. `Unavailable` sa nikdy nemení na `Invalid`, shared state je thread-safe a memory-bounded a zložitá concurrency logika má anglický aj maďarský komentár. AI nástroje sú povolené, ale autor zodpovedá za každý riadok, testy, provenance a licenciu, významnú pomoc uvedie v PR a neposiela dôverné dáta.

Security problém nepatrí do public issue. Použite GitHub Security Advisory s verziou, prostredím, reprodukciou, dopadom a minimálnym PoC bez skutočných daňových či osobných dát. Cieľom je potvrdenie do siedmich dní a prvé posúdenie do štrnástich dní; nejde o zmluvné SLA. Relevantné sú injection, TLS/endpoint bypass, data leak, cache poisoning, zneužitie requester dát, unbounded resource growth, shutdown deadlock a chybné `Invalid` pri technickej chybe.

V GitHub repository zapnite Issues, Discussions, private vulnerability reporting, Dependabot, secret scanning a push protection. `main` chráni PR, aspoň jedno approval, zrušenie stale approval, resolved conversations a kontroly CI, CodeQL a dependency review. Force push a deletion sú zakázané. Release používa podľa možnosti signed annotated tag, obsahuje binary, sources, Javadoc JAR a SHA-256. Tokeny a GPG keys sú len v Actions secrets s least privilege.

Community podpora je best effort a nie je daňové, právne ani účtovné poradenstvo. Dary cez Buy Me a Coffee sú dobrovoľné a nekupujú prioritu, SLA, security exception ani governance právo. Účastníci majú byť vecní a rešpektujúci, chrániť súkromné informácie a kritizovať kód, nie človeka. Obťažovanie, diskriminácia, doxxing, klamanie, krádež autorstva a nezodpovedné vulnerability disclosure môžu viesť k odstráneniu obsahu alebo obmedzeniu účasti.
