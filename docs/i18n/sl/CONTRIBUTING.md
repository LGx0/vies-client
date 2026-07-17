# Slovenščina (sl) — Prispevanje

**Jeziki:** [English](../../../CONTRIBUTING.md) · [Slovenščina](CONTRIBUTING.md) · [Drugi prevodi](../cs/CONTRIBUTING.md)

> Informativni prevod; velja angleški izvirnik in [LICENSE](../../../LICENSE).

Za bug odprite/referencirajte issue; velike API/licenčne/arhitekturne spremembe prej razpravljajte. Security sledi [SECURITY.md](SECURITY.md), ne public issue.

Potrebujete JDK 21+, Maven 3.9+, Git. Fork → kratek branch iz `main` → majhna fokusirana sprememba → determinističen regression test → `./mvnw --batch-mode --no-transfer-progress clean verify` → popoln PR.

Java 21, majhen type-safe API, brez runtime deps brez dogovora, `Unavailable` nikoli `Invalid`, shared state thread-safe in bounded. Javni API in kompleksna concurrency logika morata imeti EN/HU komentarje. Ne logirajte zasebnih VAT/podjetij/naslovov/requester podatkov.

Unit brez javne mreže; HTTP/concurrency le loopback; nikoli load/stress na public VIES; race z latch/barrier, ne fixed sleep. PR mora biti green, dokumentiran, dependencies utemeljene, breaking change označen, performance merljiv in avtor sme prispevati.

AI je dovoljen, vendar contributor pregleda/testira vsako vrstico, preveri izvor/licenco, razkrije bistveno AI pomoč in ne pošilja confidential ali nepreverjene kode.

Prispevek je po Apache-2.0 §5 brez dodatnih pogojev. Velja [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

## Podrobna pravila upravljanja projekta

Projekt uporablja eno samo licenco Apache-2.0. Prevodi licence niso pravno zavezujoči; odločata korenski angleški `LICENSE` in ustrezni `NOTICE`. Pred objavo ali sprejemom prispevka je treba preveriti izvor kode, pravice avtorja, morebitne pravice delodajalca ali naročnika, odsotnost skrivnosti in osebnih podatkov ter združljivost gradiva tretjih oseb. Projekt je neodvisen in ni izdelek ali priporočilo Evropske komisije, EU ali davčnega organa.

Sprememba se začne z issue ali razpravo, posebej kadar spreminja javni API, licenco, concurrency model ali arhitekturo. Contributor ustvari kratek branch iz `main`, ohrani majhen obseg, doda deterministični regression test in zažene `./mvnw --batch-mode --no-transfer-progress clean verify`. Unit test ne uporablja javnega omrežja; HTTP/concurrency test le loopback mock. Load in stress proti javnemu VIES sta prepovedana. Race usmerja latch/barrier, ne fiksni sleep.

Javni API ostane majhen, type-safe in Java 21. Runtime modul ne dobi nove dependency brez izrecne odločitve. `Unavailable` se nikoli ne spremeni v `Invalid`, skupno stanje je thread-safe in memory-bounded, zapletena concurrency logika pa ima angleške in madžarske komentarje. Orodja AI so dovoljena, vendar avtor odgovarja za vsako vrstico, teste, izvor in licenco, bistveno pomoč navede v PR in ne pošilja zaupnih podatkov.

Varnostni problem ne spada v javni issue. Uporabite GitHub Security Advisory z različico, okoljem, reprodukcijo, vplivom in minimalnim PoC brez pravih davčnih ali osebnih podatkov. Cilj je potrditev v sedmih dneh in prva ocena v štirinajstih dneh; to ni pogodbeni SLA. Pomembni so injection, TLS/endpoint bypass, data leak, cache poisoning, zloraba requester podatkov, neomejena rast virov, shutdown deadlock in napačen `Invalid` ob tehnični napaki.

V GitHub repozitoriju omogočite Issues, Discussions, private vulnerability reporting, Dependabot, secret scanning in push protection. `main` varujejo PR, vsaj ena odobritev, preklic stale approval, resolved conversations ter CI, CodeQL in dependency review. Force push in brisanje sta prepovedana. Release naj uporablja signed annotated tag ter vsebuje binary, sources, Javadoc JAR in SHA-256. Tokeni in GPG ključi so le v Actions secrets z najmanjšimi pravicami.

Podpora skupnosti je best effort in ni davčno, pravno ali računovodsko svetovanje. Donacije prek Buy Me a Coffee so prostovoljne in ne kupijo prioritete, SLA, varnostne izjeme ali governance pravice. Udeleženci morajo biti stvarni in spoštljivi, varovati zasebne informacije ter kritizirati kodo, ne človeka. Nadlegovanje, diskriminacija, doxxing, zavajanje, prisvajanje avtorstva in neodgovorno razkritje ranljivosti lahko povzročijo odstranitev vsebine ali omejitev sodelovanja.
