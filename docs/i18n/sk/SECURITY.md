# Slovenčina (sk) — Bezpečnostná politika

**Jazyky:** [English](../../../SECURITY.md) · [Slovenčina](SECURITY.md) · [Dokumentácia](README.md)

> Informatívny preklad; rozhoduje anglický originál. Jediná záväzná licencia je [LICENSE](../../../LICENSE); preklad nie je licenciou.

Najnovší `1.x` dostáva security fixes, staršie case-by-case. Aktualizujte na posledný release.

Neotvárajte public issue ani exploit. Použite **Security → Advisories → Report a vulnerability** a uveďte verziu, prostredie, reprodukciu, dopad, minimálny PoC bez reálnych osobných/daňových dát a workaround.

Ciele, nie SLA: potvrdenie 7 dní, hodnotenie 14 dní, koordinovaná oprava podľa závažnosti. Scope: injection, TLS/endpoint bypass, data leak, cache poisoning, requester misuse, unbounded resources, shutdown deadlock, chybné `Invalid`. Upstream outage/throttling nie je sám vulnerability.

## Podrobné pravidlá správy projektu

Projekt používa jedinú licenciu Apache-2.0. Preklady licencie nie sú právne záväzné; rozhodujú koreňový anglický `LICENSE` a príslušný `NOTICE`. Pred publikovaním alebo prijatím contribution treba overiť pôvod kódu, práva autora, možné práva zamestnávateľa či klienta, neprítomnosť secretov a osobných údajov a kompatibilitu third-party materiálov. Projekt je nezávislý a nie je produktom ani odporúčaním Európskej komisie, EÚ alebo daňového orgánu.

Zmena začína issue alebo diskusiou, najmä ak mení public API, licenciu, concurrency model alebo architektúru. Contributor vytvorí krátky branch z `main`, zachová malý scope, pridá deterministic regression test a spustí `./mvnw --batch-mode --no-transfer-progress clean verify`. Unit test nepoužíva verejnú sieť; HTTP/concurrency test iba loopback mock. Load a stress proti public VIES sú zakázané. Race riadi latch/barrier, nie pevný sleep.

Public API zostáva malé, type-safe a Java 21. Runtime modul neprijíma novú dependency bez výslovného rozhodnutia. `Unavailable` sa nikdy nemení na `Invalid`, shared state je thread-safe a memory-bounded a zložitá concurrency logika má anglický aj maďarský komentár. AI nástroje sú povolené, ale autor zodpovedá za každý riadok, testy, provenance a licenciu, významnú pomoc uvedie v PR a neposiela dôverné dáta.

Security problém nepatrí do public issue. Použite GitHub Security Advisory s verziou, prostredím, reprodukciou, dopadom a minimálnym PoC bez skutočných daňových či osobných dát. Cieľom je potvrdenie do siedmich dní a prvé posúdenie do štrnástich dní; nejde o zmluvné SLA. Relevantné sú injection, TLS/endpoint bypass, data leak, cache poisoning, zneužitie requester dát, unbounded resource growth, shutdown deadlock a chybné `Invalid` pri technickej chybe.

V GitHub repository zapnite Issues, Discussions, private vulnerability reporting, Dependabot, secret scanning a push protection. `main` chráni PR, aspoň jedno approval, zrušenie stale approval, resolved conversations a kontroly CI, CodeQL a dependency review. Force push a deletion sú zakázané. Release používa podľa možnosti signed annotated tag, obsahuje binary, sources, Javadoc JAR a SHA-256. Tokeny a GPG keys sú len v Actions secrets s least privilege.

Community podpora je best effort a nie je daňové, právne ani účtovné poradenstvo. Dary cez Buy Me a Coffee sú dobrovoľné a nekupujú prioritu, SLA, security exception ani governance právo. Účastníci majú byť vecní a rešpektujúci, chrániť súkromné informácie a kritizovať kód, nie človeka. Obťažovanie, diskriminácia, doxxing, klamanie, krádež autorstva a nezodpovedné vulnerability disclosure môžu viesť k odstráneniu obsahu alebo obmedzeniu účasti.
