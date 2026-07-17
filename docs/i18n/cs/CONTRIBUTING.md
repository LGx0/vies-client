# Čeština (cs) — Přispívání

**Jazyky:** [English](../../../CONTRIBUTING.md) · [Čeština](CONTRIBUTING.md) · [Dokumentace](README.md)

> Informativní překlad; při rozporu platí anglický originál. Jediná závazná licence je [LICENSE](../../../LICENSE); překlad není licencí.

Před změnou otevřete/odkažte issue; velké API, licence či architekturu nejdříve prodiskutujte. Security hlaste dle [SECURITY.md](SECURITY.md).

Požadavky: JDK 21+, Maven 3.9+, Git. Fork → krátká větev z `main` → malá cílená změna → deterministický regresní test → `./mvnw --batch-mode --no-transfer-progress clean verify` → úplný PR.

Pravidla: Java 21, malé type-safe API, žádná nová runtime dependency bez dohody, `Unavailable` nikdy jako `Invalid`, sdílený stav thread-safe a memory-bounded. Veřejné API a složitá concurrency logika vyžadují EN/HU komentáře. Necpěte do logu soukromé VAT, firmy, adresy či requester data.

Unit test bez veřejné sítě; HTTP/concurrency jen loopback; nikdy load/stress na public VIES; races řiďte latch/barrier. PR musí být green, dokumentovaný, breaking changes označené a performance tvrzení reprodukovatelné.

AI je povolena, ale autor kontroluje/testuje každý řádek, původ a licenci, přizná významnou AI pomoc a neposílá confidential/neověřený obsah. Příspěvky se řídí Apache-2.0 §5 a [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

## Podrobná pravidla správy projektu

Projekt používá jedinou licenci Apache-2.0. Překlady licence nejsou právně závazné; rozhoduje kořenový anglický `LICENSE` a příslušný `NOTICE`. Před zveřejněním nebo přijetím příspěvku je nutné ověřit původ kódu, práva autora, případná práva zaměstnavatele či klienta, nepřítomnost tajemství a osobních údajů a kompatibilitu materiálů třetích stran. Projekt je nezávislý a není produktem ani doporučením Evropské komise, Evropské unie nebo daňové správy.

Změna začíná issue nebo diskusí, zejména pokud mění veřejné API, licenci, concurrency model či architekturu. Contributor vytvoří krátkodobou větev z `main`, udrží změnu malou, přidá deterministický regresní test a spustí `./mvnw --batch-mode --no-transfer-progress clean verify`. Unit test nesmí používat veřejnou síť; HTTP a concurrency test používá pouze loopback mock. Load a stress test proti veřejnému VIES je zakázán. Race se synchronizuje latch/barrier, nikoli pevným spánkem.

Veřejné API zůstává malé, type-safe a Java 21. Runtime modul nepřijímá novou dependency bez výslovného rozhodnutí. `Unavailable` se nikdy nemění na `Invalid`, sdílený stav je thread-safe a memory-bounded a složitá concurrency logika má anglický i maďarský komentář. AI nástroje jsou povoleny, ale autor odpovídá za každý řádek, testy, původ a licenci, významnou pomoc uvede v PR a neposílá důvěrná data.

Bezpečnostní problém nepatří do veřejného issue. Použijte GitHub Security Advisory s verzí, prostředím, reprodukcí, dopadem a minimálním PoC bez skutečných daňových či osobních dat. Cílem je potvrzení do sedmi dnů a první posouzení do čtrnácti dnů; nejde o smluvní SLA. Relevantní jsou injection, TLS/endpoint bypass, únik dat, cache poisoning, zneužití requester údajů, neomezený růst zdrojů, shutdown deadlock a chybné `Invalid` při technické chybě.

V GitHub repozitáři zapněte Issues, Discussions, private vulnerability reporting, Dependabot, secret scanning a push protection. `main` chrání PR, alespoň jedno schválení, zrušení zastaralého schválení, vyřešené konverzace a kontroly CI, CodeQL a dependency review. Force push a smazání větve jsou zakázány. Release používá pokud možno podepsaný anotovaný tag, obsahuje binary, sources, Javadoc JAR a SHA-256. Tokeny a GPG klíče jsou jen v Actions secrets s minimálními právy.

Komunitní podpora je best effort a není daňovým, právním ani účetním poradenstvím. Dary přes Buy Me a Coffee jsou dobrovolné a nekupují prioritu, SLA, bezpečnostní výjimku ani governance právo. Všichni účastníci musí být věcní a respektující, chránit soukromé informace a kritizovat kód, ne člověka. Obtěžování, diskriminace, doxxing, klamání, krádež autorství a nezodpovědné zveřejnění zranitelnosti mohou vést k odstranění obsahu nebo omezení účasti.
