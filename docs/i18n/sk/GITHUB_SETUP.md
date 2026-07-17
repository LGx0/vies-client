# Slovenčina (sk) — GitHub

**Jazyky:** [English](../../GITHUB_SETUP.md) · [Slovenčina](GITHUB_SETUP.md) · [Dokumentácia](README.md)

> Informatívny preklad; rozhoduje anglický originál. Jediná záväzná licencia je [LICENSE](../../../LICENSE); preklad nie je licenciou.

Pred publish potvrďte práva, scan directory/Git history, odstráňte customer/requester dáta, tokeny, `.env`, machine files, overte LICENSE/NOTICE/third-party a spusťte `./mvnw --batch-mode --no-transfer-progress clean verify`.

Vytvorte public `vies-client` bez nového README/license/gitignore. Po OWNER a staged review:

```bash
git init -b main
git add --all
git commit -m "Initial open-source release"
gh repo create OWNER/vies-client --public --source . --remote origin --push
```

Zapnite Issues, Discussions, private vulnerability, Dependabot, secret scanning/push protection, auto-delete. `main`: PR, approval, stale dismissal, CI/CodeQL/dependency review, resolved conversations, no force/delete. Checks po prvom workflow.

Labels/topics podľa anglického originálu. Sponsor cez `.github/FUNDING.yml`, napr. `custom: [https://buymeacoffee.com/ACCOUNT]`; verified URL, no SLA/governance. Po push aktualizujte pom metadata, badges, advisory, release/Central, funding. Signed `v1.2.0` až po green CI/CodeQL.

## Podrobné pravidlá správy projektu

Projekt používa jedinú licenciu Apache-2.0. Preklady licencie nie sú právne záväzné; rozhodujú koreňový anglický `LICENSE` a príslušný `NOTICE`. Pred publikovaním alebo prijatím contribution treba overiť pôvod kódu, práva autora, možné práva zamestnávateľa či klienta, neprítomnosť secretov a osobných údajov a kompatibilitu third-party materiálov. Projekt je nezávislý a nie je produktom ani odporúčaním Európskej komisie, EÚ alebo daňového orgánu.

Zmena začína issue alebo diskusiou, najmä ak mení public API, licenciu, concurrency model alebo architektúru. Contributor vytvorí krátky branch z `main`, zachová malý scope, pridá deterministic regression test a spustí `./mvnw --batch-mode --no-transfer-progress clean verify`. Unit test nepoužíva verejnú sieť; HTTP/concurrency test iba loopback mock. Load a stress proti public VIES sú zakázané. Race riadi latch/barrier, nie pevný sleep.

Public API zostáva malé, type-safe a Java 21. Runtime modul neprijíma novú dependency bez výslovného rozhodnutia. `Unavailable` sa nikdy nemení na `Invalid`, shared state je thread-safe a memory-bounded a zložitá concurrency logika má anglický aj maďarský komentár. AI nástroje sú povolené, ale autor zodpovedá za každý riadok, testy, provenance a licenciu, významnú pomoc uvedie v PR a neposiela dôverné dáta.

Security problém nepatrí do public issue. Použite GitHub Security Advisory s verziou, prostredím, reprodukciou, dopadom a minimálnym PoC bez skutočných daňových či osobných dát. Cieľom je potvrdenie do siedmich dní a prvé posúdenie do štrnástich dní; nejde o zmluvné SLA. Relevantné sú injection, TLS/endpoint bypass, data leak, cache poisoning, zneužitie requester dát, unbounded resource growth, shutdown deadlock a chybné `Invalid` pri technickej chybe.

V GitHub repository zapnite Issues, Discussions, private vulnerability reporting, Dependabot, secret scanning a push protection. `main` chráni PR, aspoň jedno approval, zrušenie stale approval, resolved conversations a kontroly CI, CodeQL a dependency review. Force push a deletion sú zakázané. Release používa podľa možnosti signed annotated tag, obsahuje binary, sources, Javadoc JAR a SHA-256. Tokeny a GPG keys sú len v Actions secrets s least privilege.

Community podpora je best effort a nie je daňové, právne ani účtovné poradenstvo. Dary cez Buy Me a Coffee sú dobrovoľné a nekupujú prioritu, SLA, security exception ani governance právo. Účastníci majú byť vecní a rešpektujúci, chrániť súkromné informácie a kritizovať kód, nie človeka. Obťažovanie, diskriminácia, doxxing, klamanie, krádež autorstva a nezodpovedné vulnerability disclosure môžu viesť k odstráneniu obsahu alebo obmedzeniu účasti.
