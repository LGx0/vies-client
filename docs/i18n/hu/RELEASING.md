# Magyar (hu) — Kiadás

> [Nyelvválasztó](../../LANGUAGES.md) · Eltérésnél az angol technikai/jogi eredeti irányadó; csak a gyökér `LICENSE` hiteles.

Szemantikus verziózás: PATCH kompatibilis javítás, MINOR kompatibilis funkció, MAJOR publikus API- vagy szemantikatörés. Előfeltétel: tiszta, review-zott `main`, zöld Actions, JDK 21/Maven 3.9+, release jog, friss `CHANGELOG.md`.

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
jar --describe-module --file target/vies-client-1.0.0.jar
jdeps --print-module-deps target/vies-client-1.0.0.jar
```

Vizsgáld át a teljes Git historyt titkokért és személyes adatokért; aktualizáld a `LICENSE`, `NOTICE`, `SECURITY.md`, lokalizáció és API dokumentációt; ellenőrizd a binary/sources/Javadoc JAR-t; az élő VIES/load teszt maradjon opt-in; minden publikus változás legyen changelogban.

Állíts verziót a `pom.xml`-ben, commitold a verziót/changelogot, készíts lehetőleg aláírt annotált taget: `git tag -s v1.0.0 -m "v1.0.0"`, majd `git push origin main --follow-tags`. A workflow újra ellenőrizzen, csatolja a három JAR-t és SHA-256 összegeket. Hibás vagy nem review-zott commitból ne adj ki.

Maven Centralhoz saját reverse-DNS `groupId`, projekt/SCM/developer metaadat, Portal hitelesítés, aláírás és publikáló konfiguráció kell. Tokent/privát kulcsot ne commitolj; least-privilege secretet használj. Kiadás után ellenőrizd a letöltést/checksumot, nyiss `[Unreleased]` részt, security fixhez Advisoryt, és frissíts minden dokumentált verziót/lokalizációt.
