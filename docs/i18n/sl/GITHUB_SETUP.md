# Slovenščina (sl) — Objavljanje na GitHubu

**Jeziki:** [English](../../GITHUB_SETUP.md) · [Slovenščina](GITHUB_SETUP.md) · [Drugi prevodi](../cs/GITHUB_SETUP.md)

> Informativni prevod; velja angleški izvirnik in [LICENSE](../../../LICENSE).

Pred objavo potrdite pravice do kode/dokumentacije/imena, preglejte mapo in Git history za secrets, odstranite customer/requester podatke, tokene, `.env` in machine files, preverite LICENSE/NOTICE/third-party ter zaženite `./mvnw --batch-mode --no-transfer-progress clean verify`.

Na GitHubu ustvarite public `vies-client` brez generiranja novega README/licence/gitignore. Po zamenjavi `OWNER` in pregledu staged diff:

```bash
git init -b main
git add --all
git commit -m "Initial open-source release"
gh repo create OWNER/vies-client --public --source . --remote origin --push
```

Omogočite Issues, Discussions, private vulnerability reporting, Dependabot, secret scanning/push protection in auto-delete branches. Za `main` zahtevajte PR, approval, dismissal stale approvals, CI/CodeQL/dependency review, resolved conversations; prepovejte force push/delete. Required checks nastavite po prvem workflow runu.

Labels: `bug, enhancement, documentation, localization, security, dependencies, performance, concurrency, breaking-change, good-first-issue, help-wanted, triage`. Topics: `java, java21, vies, vat, vat-validation, eu, rest-client, virtual-threads, single-flight, jpms, zero-dependency`.

Sponsor gumb nastavi `.github/FUNDING.yml`, npr. `custom: [https://buymeacoffee.com/ACCOUNT]`; le preverjeni URL, donacija ne kupi SLA/governance.

Po prvem pushu posodobite `pom.xml` url/scm/developers, README badges, advisory URL, release/Central koordinate in funding URL. Prvi signed `v1.0.0` šele po green CI/CodeQL; workflow ustvari binary/sources/Javadoc JAR in SHA-256.

## Podrobna pravila upravljanja projekta

Projekt uporablja eno samo licenco Apache-2.0. Prevodi licence niso pravno zavezujoči; odločata korenski angleški `LICENSE` in ustrezni `NOTICE`. Pred objavo ali sprejemom prispevka je treba preveriti izvor kode, pravice avtorja, morebitne pravice delodajalca ali naročnika, odsotnost skrivnosti in osebnih podatkov ter združljivost gradiva tretjih oseb. Projekt je neodvisen in ni izdelek ali priporočilo Evropske komisije, EU ali davčnega organa.

Sprememba se začne z issue ali razpravo, posebej kadar spreminja javni API, licenco, concurrency model ali arhitekturo. Contributor ustvari kratek branch iz `main`, ohrani majhen obseg, doda deterministični regression test in zažene `./mvnw --batch-mode --no-transfer-progress clean verify`. Unit test ne uporablja javnega omrežja; HTTP/concurrency test le loopback mock. Load in stress proti javnemu VIES sta prepovedana. Race usmerja latch/barrier, ne fiksni sleep.

Javni API ostane majhen, type-safe in Java 21. Runtime modul ne dobi nove dependency brez izrecne odločitve. `Unavailable` se nikoli ne spremeni v `Invalid`, skupno stanje je thread-safe in memory-bounded, zapletena concurrency logika pa ima angleške in madžarske komentarje. Orodja AI so dovoljena, vendar avtor odgovarja za vsako vrstico, teste, izvor in licenco, bistveno pomoč navede v PR in ne pošilja zaupnih podatkov.

Varnostni problem ne spada v javni issue. Uporabite GitHub Security Advisory z različico, okoljem, reprodukcijo, vplivom in minimalnim PoC brez pravih davčnih ali osebnih podatkov. Cilj je potrditev v sedmih dneh in prva ocena v štirinajstih dneh; to ni pogodbeni SLA. Pomembni so injection, TLS/endpoint bypass, data leak, cache poisoning, zloraba requester podatkov, neomejena rast virov, shutdown deadlock in napačen `Invalid` ob tehnični napaki.

V GitHub repozitoriju omogočite Issues, Discussions, private vulnerability reporting, Dependabot, secret scanning in push protection. `main` varujejo PR, vsaj ena odobritev, preklic stale approval, resolved conversations ter CI, CodeQL in dependency review. Force push in brisanje sta prepovedana. Release naj uporablja signed annotated tag ter vsebuje binary, sources, Javadoc JAR in SHA-256. Tokeni in GPG ključi so le v Actions secrets z najmanjšimi pravicami.

Podpora skupnosti je best effort in ni davčno, pravno ali računovodsko svetovanje. Donacije prek Buy Me a Coffee so prostovoljne in ne kupijo prioritete, SLA, varnostne izjeme ali governance pravice. Udeleženci morajo biti stvarni in spoštljivi, varovati zasebne informacije ter kritizirati kodo, ne človeka. Nadlegovanje, diskriminacija, doxxing, zavajanje, prisvajanje avtorstva in neodgovorno razkritje ranljivosti lahko povzročijo odstranitev vsebine ali omejitev sodelovanja.
