# Gaeilge (ga) — Eisiúint

> [Roghnóir teanga](../../LANGUAGES.md) · Is é foinse Bhéarla atá i réim; níl ach `LICENSE` na fréimhe ceangailteach.

SemVer: PATCH deisiú comhoiriúnach, MINOR gné chomhoiriúnach, MAJOR API/seimeantaic bhriste. Réamhriachtanais: `main` glan athbhreithnithe, Actions glas, JDK 21/Maven 3.9+, cearta agus `CHANGELOG.md` nuashonraithe.

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
jar --describe-module --file target/vies-client-1.0.0.jar
jdeps --print-module-deps target/vies-client-1.0.0.jar
```

Scan Git history do secrets/sonraí pearsanta; seiceáil `LICENSE`, `NOTICE`, `SECURITY.md`, logánuithe, API docs agus na trí JAR; coinnigh live/load roghnach; cuir athruithe poiblí sa changelog.

Nuashonraigh `pom.xml`, commit leagan/changelog, cruthaigh tag sínithe anótáilte más féidir `git tag -s v1.0.0 -m "v1.0.0"`, ansin `git push origin main --follow-tags`. Athrithfidh workflow verify agus ceanglóidh binary/sources/Javadoc JAR + SHA-256. Ná heisigh commit teipthe nó neamh-athbhreithnithe.

Éilíonn Maven Central `groupId` reverse-DNS faoi úinéireacht, metadata, Portal credentials, síniú agus publish config. Ná commit token/key. Tar éis eisiúna seiceáil download/checksum, oscail `[Unreleased]`, Security Advisory más gá agus nuashonraigh leaganacha/logánuithe.
