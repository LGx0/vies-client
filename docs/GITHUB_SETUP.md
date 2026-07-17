# GitHub közzétételi útmutató / GitHub publication guide

## 1. Közzététel előtti kötelező ellenőrzés / Mandatory pre-publication checks

- Erősítsd meg, hogy a forrás, dokumentáció és név közzétételi joga nálad van.
- Vizsgáld át a teljes könyvtárat és később a teljes Git historyt titkokra.
- Távolíts el ügyféladatot, valós requester-adószámot, tokent, `.env` fájlt és IDE-
  gépspecifikus állományt.
- Ellenőrizd az Apache-2.0 `LICENSE`, `NOTICE` és harmadik fél notice fájlját.
- Futtasd: `./mvnw --batch-mode --no-transfer-progress clean verify`.

- Confirm the right to publish all code, documentation, and project naming.
- Scan the directory and eventual Git history for secrets.
- Remove customer data, real requester VAT numbers, tokens, `.env`, and machine files.
- Verify Apache-2.0 and third-party notices.
- Run the full Maven verification.

## 2. Repó létrehozása / Create the repository

A projekt már tartalmaz README-t, licencet és `.gitignore` fájlt, ezért a GitHub
webes létrehozáskor ne generálj újakat. Javasolt név: `vies-client`, láthatóság:
`Public`, alapértelmezett branch: `main`.

The project already contains README, license, and ignore files; do not generate
duplicates on GitHub. Suggested name: `vies-client`, visibility: `Public`, default
branch: `main`.

A pontos owner megerősítése után használható parancs:

```bash
git init -b main
git add --all
git commit -m "Initial open-source release"
gh repo create OWNER/vies-client --public --source . --remote origin --push
```

Ne futtasd a parancsot `OWNER` cseréje és a teljes staged diff ellenőrzése nélkül.
Do not run it before replacing `OWNER` and reviewing the complete staged diff.

## 3. Repository settings

Ajánlott / Recommended:

- Issues: enabled;
- Discussions: enabled for usage questions;
- Wikis: disabled unless actively maintained;
- Projects: optional;
- Preserve this repository: optional after the first stable release;
- Automatically delete head branches: enabled;
- Private vulnerability reporting: enabled;
- Dependabot alerts and security updates: enabled;
- Secret scanning and push protection: enabled where GitHub offers them.

## 4. Branch protection vagy ruleset / Branch protection or ruleset

A `main` branchre:

- pull request required;
- legalább egy jóváhagyás / at least one approval;
- stale approval dismissal after new commits;
- required checks: `JDK 21 verification`, `JDK 25 verification`,
  `Java security analysis`, and `dependency-review`;
- conversation resolution required;
- force push and deletion disabled;
- admin bypass used only for emergencies;
- signed commits/tags recommended for releases.
- a `v*` tag ruleset csak kijelölt maintainernek engedjen létrehozást/módosítást,
  és követeljen GitHub által ellenőrzött aláírt annotált taget;
- hozz létre védett `release` environmentet kötelező maintainer approval-val.

Az első push előtt a required check neve még nem létezik; futtasd egyszer a workflow-
kat, majd állítsd be a rulesetet. / Required-check names appear after the first workflow
run; configure the ruleset afterward.

## 5. Címkék és témák / Labels and topics

Javasolt címkék:

```text
bug, enhancement, documentation, localization, security, dependencies,
performance, concurrency, breaking-change, good-first-issue, help-wanted, triage
```

Javasolt repository-leírás / Suggested repository description:

```text
Fast Java 21 VIES VAT checker and EU VAT number validator with virtual threads, bounded concurrency, cache, and bilingual errors.
```

Javasolt topicok:

```text
java, java21, java-library, vies, vat, vat-number, vat-checker, vat-validator,
vat-validation, vat-number-validation, eu-vat, tax-id, tax-id-checker,
tax-id-validation, rest-client, virtual-threads, single-flight, jpms,
zero-dependency
```

## 6. Donate/Sponsor gomb

A GitHub a `.github/FUNDING.yml` alapján jeleníti meg a **Sponsor** gombot. Csak a
maintainer által ellenőrzött pénzügyi URL-t add meg. Példa:

```yaml
buy_me_a_coffee: lgx0
```

Támogatott szolgáltatónál használható dedikált kulcs is, például `github`, `ko_fi`
vagy `custom`. Az adomány nem jelent SLA-t vagy prioritási jogot; lásd `SUPPORT.md`.

GitHub displays the Sponsor button from `.github/FUNDING.yml`. Configure only a
funding destination verified by the maintainer. Donations do not purchase an SLA or
governance rights.

## 7. Első push utáni frissítések / Updates after the first push

A tényleges GitHub URL ismeretében frissítsd:

- `pom.xml`: `url`, `scm`, `developers`;
- README: CI és CodeQL badge URL-ek;
- `SECURITY.md`: private advisory URL, ha szükséges;
- release dokumentáció és Maven Central koordináták;
- `.github/FUNDING.yml`: `https://buymeacoffee.com/lgx0`.

## 8. Első kiadás / First release

Csak zöld CI/CodeQL után készíts aláírt `v1.2.0` taget. A release workflow tesztel,
elkészíti a binary/sources/Javadoc JAR-okat és SHA-256 checksumot, majd GitHub
Release-t hoz létre. Részletek: [RELEASING.md](RELEASING.md).

All third-party Actions are pinned to full commit SHAs. The workflow rejects an
unsigned tag, a tag/version mismatch, and a commit outside `main`; repository code
runs read-only, while the isolated publisher receives minimal write permission and
creates GitHub artifact attestations.
