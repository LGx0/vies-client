# Čeština (cs) — Příručka vydání

**Jazyky:** [English](../../RELEASING.md) · [Čeština](RELEASING.md) · [Polski](../pl/RELEASING.md) · [Slovenčina](../sk/RELEASING.md) · [Slovenščina](../sl/RELEASING.md) · [Hrvatski](../hr/RELEASING.md) · [Română](../ro/RELEASING.md) · [Български](../bg/RELEASING.md) · [Ελληνικά](../el/RELEASING.md)

> Informativní překlad; platí anglický technický/právní originál. [LICENSE](../../../LICENSE) je jediný závazný licenční text.

## 1. Předpoklady a verze

Potřebujete čistou větev `main`, zelené GitHub Actions, oprávnění pro tag/release, JDK 21, Maven 3.9+ a aktualizovaný `CHANGELOG.md`. Semantic Versioning: `PATCH` kompatibilní oprava, `MINOR` kompatibilní funkce, `MAJOR` porušení veřejného API/sémantiky.

## 2. Kontrola před vydáním

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
jar --describe-module --file target/vies-client-1.0.0.jar
jdeps --print-module-deps target/vies-client-1.0.0.jar
```

Ověřte celý Git history bez tajemství/osobních dat, aktuální `LICENSE`, `NOTICE`, `SECURITY.md` a dokumentaci, vznik sources/Javadoc JAR, žádný povinný live/load test v CI a changelog všech veřejných změn.

## 3. GitHub Release

1. Nastavte verzi v `pom.xml` a commitněte ji s changelogem.
2. Vytvořte pokud možno podepsaný anotovaný tag: `git tag -s v1.0.0 -m "v1.0.0"`.
3. Push: `git push origin main --follow-tags`.
4. `release.yml` znovu testuje a přiloží binary, sources a Javadoc JAR.

Nevydávejte nezkontrolovaný nebo failing commit.

## 4. Maven Central / GitHub Packages a kroky poté

Pro Maven Central je navíc třeba vlastněný reverse-DNS `groupId`, úplná metadata `url`/`scm`/`developers`/distribution management, registrace a token Central Portal, GPG podpis a kompatibilní publishing konfigurace. Tokeny a privátní klíče necommitujte; používejte GitHub Actions secrets a least privilege.

Po vydání ověřte downloady a SHA-256, založte novou sekci `[Unreleased]`, při bezpečnostní opravě publikujte GitHub Security Advisory a aktualizujte dokumentované dependency verze.

## Podrobná pravidla správy projektu

Projekt používá jedinou licenci Apache-2.0. Překlady licence nejsou právně závazné; rozhoduje kořenový anglický `LICENSE` a příslušný `NOTICE`. Před zveřejněním nebo přijetím příspěvku je nutné ověřit původ kódu, práva autora, případná práva zaměstnavatele či klienta, nepřítomnost tajemství a osobních údajů a kompatibilitu materiálů třetích stran. Projekt je nezávislý a není produktem ani doporučením Evropské komise, Evropské unie nebo daňové správy.

Změna začíná issue nebo diskusí, zejména pokud mění veřejné API, licenci, concurrency model či architekturu. Contributor vytvoří krátkodobou větev z `main`, udrží změnu malou, přidá deterministický regresní test a spustí `./mvnw --batch-mode --no-transfer-progress clean verify`. Unit test nesmí používat veřejnou síť; HTTP a concurrency test používá pouze loopback mock. Load a stress test proti veřejnému VIES je zakázán. Race se synchronizuje latch/barrier, nikoli pevným spánkem.

Veřejné API zůstává malé, type-safe a Java 21. Runtime modul nepřijímá novou dependency bez výslovného rozhodnutí. `Unavailable` se nikdy nemění na `Invalid`, sdílený stav je thread-safe a memory-bounded a složitá concurrency logika má anglický i maďarský komentář. AI nástroje jsou povoleny, ale autor odpovídá za každý řádek, testy, původ a licenci, významnou pomoc uvede v PR a neposílá důvěrná data.

Bezpečnostní problém nepatří do veřejného issue. Použijte GitHub Security Advisory s verzí, prostředím, reprodukcí, dopadem a minimálním PoC bez skutečných daňových či osobních dat. Cílem je potvrzení do sedmi dnů a první posouzení do čtrnácti dnů; nejde o smluvní SLA. Relevantní jsou injection, TLS/endpoint bypass, únik dat, cache poisoning, zneužití requester údajů, neomezený růst zdrojů, shutdown deadlock a chybné `Invalid` při technické chybě.

V GitHub repozitáři zapněte Issues, Discussions, private vulnerability reporting, Dependabot, secret scanning a push protection. `main` chrání PR, alespoň jedno schválení, zrušení zastaralého schválení, vyřešené konverzace a kontroly CI, CodeQL a dependency review. Force push a smazání větve jsou zakázány. Release používá pokud možno podepsaný anotovaný tag, obsahuje binary, sources, Javadoc JAR a SHA-256. Tokeny a GPG klíče jsou jen v Actions secrets s minimálními právy.

Komunitní podpora je best effort a není daňovým, právním ani účetním poradenstvím. Dary přes Buy Me a Coffee jsou dobrovolné a nekupují prioritu, SLA, bezpečnostní výjimku ani governance právo. Všichni účastníci musí být věcní a respektující, chránit soukromé informace a kritizovat kód, ne člověka. Obtěžování, diskriminace, doxxing, klamání, krádež autorství a nezodpovědné zveřejnění zranitelnosti mohou vést k odstranění obsahu nebo omezení účasti.
