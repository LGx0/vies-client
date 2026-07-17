# Slovenščina (sl) — Izdajanje

**Jeziki:** [English](../../RELEASING.md) · [Slovenščina](RELEASING.md) · [Drugi prevodi](../cs/RELEASING.md)

> Informativni prevod; velja angleški izvirnik in [LICENSE](../../../LICENSE).

Zahteve: čist `main`, green Actions, release pravice, JDK 21, Maven 3.9+, aktualen `CHANGELOG.md`. SemVer: PATCH popravek, MINOR združljiva funkcija, MAJOR breaking API/semantics.

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
jar --describe-module --file target/vies-client-1.0.0.jar
jdeps --print-module-deps target/vies-client-1.0.0.jar
```

Preverite history brez secretov/podatkov, LICENSE/NOTICE/SECURITY/docs, sources/Javadoc JAR, brez required live/load CI in changelog API.

Nastavite `pom.xml`, commit, signed tag `git tag -s v1.0.0 -m "v1.0.0"`, nato `git push origin main --follow-tags`; `release.yml` ponovno testira in priloži JAR-e. Maven Central dodatno zahteva owned reverse-DNS `groupId`, metadata, Portal token, GPG in publishing config. Skrivnosti le v Actions secrets.

Po izdaji preverite downloads/SHA-256, odprite `[Unreleased]`, po potrebi Security Advisory in posodobite dependency verzije.

## Podrobna pravila upravljanja projekta

Projekt uporablja eno samo licenco Apache-2.0. Prevodi licence niso pravno zavezujoči; odločata korenski angleški `LICENSE` in ustrezni `NOTICE`. Pred objavo ali sprejemom prispevka je treba preveriti izvor kode, pravice avtorja, morebitne pravice delodajalca ali naročnika, odsotnost skrivnosti in osebnih podatkov ter združljivost gradiva tretjih oseb. Projekt je neodvisen in ni izdelek ali priporočilo Evropske komisije, EU ali davčnega organa.

Sprememba se začne z issue ali razpravo, posebej kadar spreminja javni API, licenco, concurrency model ali arhitekturo. Contributor ustvari kratek branch iz `main`, ohrani majhen obseg, doda deterministični regression test in zažene `./mvnw --batch-mode --no-transfer-progress clean verify`. Unit test ne uporablja javnega omrežja; HTTP/concurrency test le loopback mock. Load in stress proti javnemu VIES sta prepovedana. Race usmerja latch/barrier, ne fiksni sleep.

Javni API ostane majhen, type-safe in Java 21. Runtime modul ne dobi nove dependency brez izrecne odločitve. `Unavailable` se nikoli ne spremeni v `Invalid`, skupno stanje je thread-safe in memory-bounded, zapletena concurrency logika pa ima angleške in madžarske komentarje. Orodja AI so dovoljena, vendar avtor odgovarja za vsako vrstico, teste, izvor in licenco, bistveno pomoč navede v PR in ne pošilja zaupnih podatkov.

Varnostni problem ne spada v javni issue. Uporabite GitHub Security Advisory z različico, okoljem, reprodukcijo, vplivom in minimalnim PoC brez pravih davčnih ali osebnih podatkov. Cilj je potrditev v sedmih dneh in prva ocena v štirinajstih dneh; to ni pogodbeni SLA. Pomembni so injection, TLS/endpoint bypass, data leak, cache poisoning, zloraba requester podatkov, neomejena rast virov, shutdown deadlock in napačen `Invalid` ob tehnični napaki.

V GitHub repozitoriju omogočite Issues, Discussions, private vulnerability reporting, Dependabot, secret scanning in push protection. `main` varujejo PR, vsaj ena odobritev, preklic stale approval, resolved conversations ter CI, CodeQL in dependency review. Force push in brisanje sta prepovedana. Release naj uporablja signed annotated tag ter vsebuje binary, sources, Javadoc JAR in SHA-256. Tokeni in GPG ključi so le v Actions secrets z najmanjšimi pravicami.

Podpora skupnosti je best effort in ni davčno, pravno ali računovodsko svetovanje. Donacije prek Buy Me a Coffee so prostovoljne in ne kupijo prioritete, SLA, varnostne izjeme ali governance pravice. Udeleženci morajo biti stvarni in spoštljivi, varovati zasebne informacije ter kritizirati kodo, ne človeka. Nadlegovanje, diskriminacija, doxxing, zavajanje, prisvajanje avtorstva in neodgovorno razkritje ranljivosti lahko povzročijo odstranitev vsebine ali omejitev sodelovanja.
