# Eesti (et) — Väljalaskmine

> [Keelevalik](../../LANGUAGES.md) · Ingliskeelne tehniline/õiguslik allikas on määrav; ainult juure `LICENSE` on siduv.

SemVer: PATCH ühilduv parandus, MINOR ühilduv funktsioon, MAJOR murdev API/semantika. Eeldused: puhas ülevaadatud `main`, roheline Actions, JDK 21/Maven 3.9+, õigused ja värske `CHANGELOG.md`.

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
jar --describe-module --file target/vies-client-1.0.0.jar
jdeps --print-module-deps target/vies-client-1.0.0.jar
```

Skaneeri kogu Git history saladuste/isikuandmete suhtes; kontrolli `LICENSE`, `NOTICE`, `SECURITY.md`, lokaliseeringuid, API dokumentatsiooni ja kolme JAR-i; live/load jäävad valikuliseks; dokumenteeri avalikud muudatused.

Uuenda `pom.xml`, commiti versioon/changelog, loo soovitatavalt allkirjastatud annoteeritud tag `git tag -s v1.0.0 -m "v1.0.0"`, seejärel `git push origin main --follow-tags`. Workflow kordab verify’d ja lisab binary/sources/Javadoc JAR-id ning SHA-256. Ära avalda ebaõnnestunud või ülevaatamata commit’i.

Maven Central vajab omatud reverse-DNS `groupId`-i, metaandmeid, Portal-krediiti, allkirjastamist ja publish-seadistust. Ära commiti tokeneid/võtmeid. Pärast väljalaset kontrolli allalaadimisi/checksum’e, ava `[Unreleased]`, tee vajadusel Security Advisory ning uuenda versioonid ja tõlked.
