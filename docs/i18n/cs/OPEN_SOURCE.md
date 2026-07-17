# Čeština (cs) — Open-source projekt

**Jazyky:** [English](../../OPEN_SOURCE.md) · [Čeština](OPEN_SOURCE.md) · [Polski](../pl/OPEN_SOURCE.md) · [Slovenčina](../sk/OPEN_SOURCE.md) · [Slovenščina](../sl/OPEN_SOURCE.md) · [Hrvatski](../hr/OPEN_SOURCE.md) · [Română](../ro/OPEN_SOURCE.md) · [Български](../bg/OPEN_SOURCE.md) · [Ελληνικά](../el/OPEN_SOURCE.md)

> Informativní překlad. Při rozdílu je rozhodující anglický právní/technický originál. Právně závaznou licencí je výhradně nezměněný [LICENSE](../../../LICENSE); tento překlad není licencí.

## Volba licence

Projekt používá jako jedinou licenci **Apache License 2.0** (`SPDX: Apache-2.0`). Jde o permisivní licenci pro soukromé, komerční i open-source použití, úpravy a distribuci; vyžaduje zachování licence/attribution, označení změněných souborů a obsahuje výslovný patentový grant.

MIT je kratší a permisivní, ale nemá stejně podrobné explicitní patentové podmínky. Pro znovupoužitelnou Java knihovnu poskytuje Apache-2.0 jasnější firemní rámec. Projekt není dual-licensed; uživatel si mezi MIT a Apache nevybírá.

## Kontrola práv před zveřejněním

Vlastník musí potvrdit právo licencovat veškerý kód a dokumentaci, že nejde o majetek zaměstnavatele/klienta, nejsou přítomny nelegálně kopírované části, tajemství, osobní či zákaznická data a materiály třetích stran mají dokumentovaný původ/licenci.

## Nezávislost a příspěvky

Jde o nezávislou knihovnu, nikoli oficiální, schválený ani přidružený produkt Evropské komise, EU či daňové správy. „VIES“ pouze popisuje kompatibilitu.

Příspěvky se řídí oddílem 5 Apache-2.0 a [CONTRIBUTING.md](../../../CONTRIBUTING.md). Samostatná CLA nyní není; případná budoucí CLA musí být předem dokumentována a platit jen na nové příspěvky.

Tento text je provozní shrnutí, nikoli individuální právní poradenství. U zaměstnaneckých práv, patentů a regulovaného použití vyhledejte kvalifikovaného právníka.

## Podrobná pravidla správy projektu

Projekt používá jedinou licenci Apache-2.0. Překlady licence nejsou právně závazné; rozhoduje kořenový anglický `LICENSE` a příslušný `NOTICE`. Před zveřejněním nebo přijetím příspěvku je nutné ověřit původ kódu, práva autora, případná práva zaměstnavatele či klienta, nepřítomnost tajemství a osobních údajů a kompatibilitu materiálů třetích stran. Projekt je nezávislý a není produktem ani doporučením Evropské komise, Evropské unie nebo daňové správy.

Změna začíná issue nebo diskusí, zejména pokud mění veřejné API, licenci, concurrency model či architekturu. Contributor vytvoří krátkodobou větev z `main`, udrží změnu malou, přidá deterministický regresní test a spustí `./mvnw --batch-mode --no-transfer-progress clean verify`. Unit test nesmí používat veřejnou síť; HTTP a concurrency test používá pouze loopback mock. Load a stress test proti veřejnému VIES je zakázán. Race se synchronizuje latch/barrier, nikoli pevným spánkem.

Veřejné API zůstává malé, type-safe a Java 21. Runtime modul nepřijímá novou dependency bez výslovného rozhodnutí. `Unavailable` se nikdy nemění na `Invalid`, sdílený stav je thread-safe a memory-bounded a složitá concurrency logika má anglický i maďarský komentář. AI nástroje jsou povoleny, ale autor odpovídá za každý řádek, testy, původ a licenci, významnou pomoc uvede v PR a neposílá důvěrná data.

Bezpečnostní problém nepatří do veřejného issue. Použijte GitHub Security Advisory s verzí, prostředím, reprodukcí, dopadem a minimálním PoC bez skutečných daňových či osobních dat. Cílem je potvrzení do sedmi dnů a první posouzení do čtrnácti dnů; nejde o smluvní SLA. Relevantní jsou injection, TLS/endpoint bypass, únik dat, cache poisoning, zneužití requester údajů, neomezený růst zdrojů, shutdown deadlock a chybné `Invalid` při technické chybě.

V GitHub repozitáři zapněte Issues, Discussions, private vulnerability reporting, Dependabot, secret scanning a push protection. `main` chrání PR, alespoň jedno schválení, zrušení zastaralého schválení, vyřešené konverzace a kontroly CI, CodeQL a dependency review. Force push a smazání větve jsou zakázány. Release používá pokud možno podepsaný anotovaný tag, obsahuje binary, sources, Javadoc JAR a SHA-256. Tokeny a GPG klíče jsou jen v Actions secrets s minimálními právy.

Komunitní podpora je best effort a není daňovým, právním ani účetním poradenstvím. Dary přes Buy Me a Coffee jsou dobrovolné a nekupují prioritu, SLA, bezpečnostní výjimku ani governance právo. Všichni účastníci musí být věcní a respektující, chránit soukromé informace a kritizovat kód, ne člověka. Obtěžování, diskriminace, doxxing, klamání, krádež autorství a nezodpovědné zveřejnění zranitelnosti mohou vést k odstranění obsahu nebo omezení účasti.
