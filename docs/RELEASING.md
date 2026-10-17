# Kiadási útmutató / Release guide

## 1. Előfeltételek / Prerequisites

- tiszta `main` branch és zöld GitHub Actions;
- megfelelő GitHub jogosultság a tag és release létrehozásához;
- JDK 21 és JDK 25, checksum-rögzített Maven Wrapper;
- a kiadási verzió és dátum rögzítve a `CHANGELOG.md` fájlban.

## 2. Verziózás / Versioning

A projekt szemantikus verziózást használ:

- `PATCH`: kompatibilis hibajavítás;
- `MINOR`: kompatibilis új funkció;
- `MAJOR`: publikus API vagy szemantika törése.

The project uses Semantic Versioning: patch for compatible fixes, minor for compatible
features, and major for breaking API or semantic changes.

## 3. Kiadás előtti ellenőrzés / Pre-release verification

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
jar --describe-module --file target/vies-client-1.2.1.jar
jdeps --print-module-deps target/vies-client-1.2.1.jar
```

Ellenőrizd még:

- nincs titok vagy személyes adat a teljes Git historyban;
- a `LICENSE`, `NOTICE`, `SECURITY.md` és dokumentáció aktuális;
- a Javadoc és sources JAR létrejött;
- nincs kötelező élő VIES- vagy load teszt a CI-ban;
- a publikus API változása szerepel a changelogban.

## 4. GitHub Release

1. Állítsd be a verziót a `pom.xml` fájlban.
2. Commitold a changelogot és a verziót.
3. Hozz létre kötelezően aláírt annotált taget: `git tag -s v1.2.0 -m "v1.2.0"`.
4. Pushold a commitot és a taget: `git push origin main --follow-tags`.
5. A `release.yml` workflow újrafuttatja a teszteket, majd csatolja a binary,
   sources és Javadoc JAR fájlokat a GitHub Release-hez.

Signed annotated tags are mandatory. The workflow requires `v${pom.version}`, a
GitHub-verified tag signature, and a commit reachable from `main`. It builds with
read-only permissions, then an isolated protected-environment job attests and
publishes the artifacts. Never release an unreviewed or failing commit.

A tag aláírását és main-ősviszonyát a wrapper/POM futtatása előtt ellenőrzi a
workflow. A Maven-verzió csak ezután olvasható ki; kizárólag `MAJOR.MINOR.PATCH`
alakú, a taggel pontosan egyező kiadás engedélyezett. A kötelező PR-ellenőrzések
és a védett release-környezet kapuját tartsd meg.

The workflow verifies tag signature and main ancestry before executing the wrapper
or POM. Maven version resolution happens only afterward and must yield a plain
`MAJOR.MINOR.PATCH` matching the tag. Retain required PR checks and the protected
release-environment gate. Use a new version for every release; never replace a
published tag or its binaries.

## 5. Maven Central vagy GitHub Packages

A jelenlegi build lokális és GitHub Release terjesztésre kész. Maven Centralhoz még
szükséges:

- egy igazolható, saját reverse-DNS `groupId`;
- projekt `url`, `scm`, `developers` és distribution-management metaadat;
- Central Portal regisztráció és token;
- GPG-aláírás és Central-kompatibilis publikáló konfiguráció.

The current build is ready for local installation and GitHub Releases. Maven Central
publication additionally requires an owned reverse-DNS group ID, complete project/SCM
metadata, Central Portal credentials, artifact signing, and publishing configuration.

Ne tegyél tokent vagy privát GPG-kulcsot a repóba. GitHub Actions secretet és minimális
jogosultságot használj. / Never commit tokens or private signing keys. Use GitHub
Actions secrets and least privilege.

## 6. Kiadás utáni lépések / Post-release

- ellenőrizd a release letöltéseit és SHA-256 értékeit;
- ellenőrizd a GitHub artifact attestationt;
- indíts új `[Unreleased]` szekciót;
- biztonsági javításnál publikálj GitHub Security Advisory-t;
- frissítsd a dokumentált dependency verziót.

Consumer verification / Fogyasztói ellenőrzés:

```bash
sha256sum -c SHA256SUMS
gh attestation verify vies-client-1.2.1.jar --repo LGx0/vies-client
gh attestation verify vies-client-1.2.1-sources.jar --repo LGx0/vies-client
gh attestation verify vies-client-1.2.1-javadoc.jar --repo LGx0/vies-client
```

The checksum detects transfer/storage corruption; the GitHub CLI command verifies
the signed build provenance against this repository. / A checksum az átviteli vagy
tárolási hibát, az attestation pedig a repóhoz kötött aláírt build-eredetet ellenőrzi.
