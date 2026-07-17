# Čeština (cs) — GitHub

**Jazyky:** [English](../../GITHUB_SETUP.md) · [Čeština](GITHUB_SETUP.md) · [Dokumentace](README.md)

> Informativní překlad; při rozporu platí anglický originál. Jediná závazná licence je [LICENSE](../../../LICENSE); překlad není licencí.

Před zveřejněním potvrďte práva, prohledejte adresář i Git history na secrets, odstraňte zákaznické/requester údaje, tokeny, `.env` a machine files, ověřte LICENSE/NOTICE/third-party a spusťte `./mvnw --batch-mode --no-transfer-progress clean verify`.

Vytvořte public repo `vies-client` bez nového README/licence/gitignore. Po nahrazení OWNER a kontrole staged diff:

```bash
git init -b main
git add --all
git commit -m "Initial open-source release"
gh repo create OWNER/vies-client --public --source . --remote origin --push
```

Zapněte Issues, Discussions, private vulnerability reporting, Dependabot, secret scanning/push protection a auto-delete branches. Pro `main`: PR, approval, stale dismissal, CI/CodeQL/dependency review, resolved conversations; zakažte force push/delete. Required checks nastavte po prvním workflow runu.

Labels: `bug, enhancement, documentation, localization, security, dependencies, performance, concurrency, breaking-change, good-first-issue, help-wanted, triage`. Topics: `java, java21, vies, vat, vat-validation, eu, rest-client, virtual-threads, single-flight, jpms, zero-dependency`.

Sponsor tlačítko řídí `.github/FUNDING.yml`, např. `custom: [https://buymeacoffee.com/ACCOUNT]`; jen ověřený URL, bez SLA/governance. Po push aktualizujte pom.xml metadata, badges, advisory, release/Central a funding. První signed `v1.0.0` až po green CI/CodeQL.

## Podrobná pravidla správy projektu

Projekt používá jedinou licenci Apache-2.0. Překlady licence nejsou právně závazné; rozhoduje kořenový anglický `LICENSE` a příslušný `NOTICE`. Před zveřejněním nebo přijetím příspěvku je nutné ověřit původ kódu, práva autora, případná práva zaměstnavatele či klienta, nepřítomnost tajemství a osobních údajů a kompatibilitu materiálů třetích stran. Projekt je nezávislý a není produktem ani doporučením Evropské komise, Evropské unie nebo daňové správy.

Změna začíná issue nebo diskusí, zejména pokud mění veřejné API, licenci, concurrency model či architekturu. Contributor vytvoří krátkodobou větev z `main`, udrží změnu malou, přidá deterministický regresní test a spustí `./mvnw --batch-mode --no-transfer-progress clean verify`. Unit test nesmí používat veřejnou síť; HTTP a concurrency test používá pouze loopback mock. Load a stress test proti veřejnému VIES je zakázán. Race se synchronizuje latch/barrier, nikoli pevným spánkem.

Veřejné API zůstává malé, type-safe a Java 21. Runtime modul nepřijímá novou dependency bez výslovného rozhodnutí. `Unavailable` se nikdy nemění na `Invalid`, sdílený stav je thread-safe a memory-bounded a složitá concurrency logika má anglický i maďarský komentář. AI nástroje jsou povoleny, ale autor odpovídá za každý řádek, testy, původ a licenci, významnou pomoc uvede v PR a neposílá důvěrná data.

Bezpečnostní problém nepatří do veřejného issue. Použijte GitHub Security Advisory s verzí, prostředím, reprodukcí, dopadem a minimálním PoC bez skutečných daňových či osobních dat. Cílem je potvrzení do sedmi dnů a první posouzení do čtrnácti dnů; nejde o smluvní SLA. Relevantní jsou injection, TLS/endpoint bypass, únik dat, cache poisoning, zneužití requester údajů, neomezený růst zdrojů, shutdown deadlock a chybné `Invalid` při technické chybě.

V GitHub repozitáři zapněte Issues, Discussions, private vulnerability reporting, Dependabot, secret scanning a push protection. `main` chrání PR, alespoň jedno schválení, zrušení zastaralého schválení, vyřešené konverzace a kontroly CI, CodeQL a dependency review. Force push a smazání větve jsou zakázány. Release používá pokud možno podepsaný anotovaný tag, obsahuje binary, sources, Javadoc JAR a SHA-256. Tokeny a GPG klíče jsou jen v Actions secrets s minimálními právy.

Komunitní podpora je best effort a není daňovým, právním ani účetním poradenstvím. Dary přes Buy Me a Coffee jsou dobrovolné a nekupují prioritu, SLA, bezpečnostní výjimku ani governance právo. Všichni účastníci musí být věcní a respektující, chránit soukromé informace a kritizovat kód, ne člověka. Obtěžování, diskriminace, doxxing, klamání, krádež autorství a nezodpovědné zveřejnění zranitelnosti mohou vést k odstranění obsahu nebo omezení účasti.
