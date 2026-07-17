# Suomi (fi) — Julkaiseminen

> [Kielivalitsin](../../LANGUAGES.md) · Englanninkielinen tekninen/oikeudellinen lähde määrää; vain juuren `LICENSE` on sitova.

SemVer: PATCH yhteensopiva korjaus, MINOR yhteensopiva ominaisuus, MAJOR rikkova API/semantiikka. Edellytykset: puhdas katselmoitu `main`, vihreä Actions, JDK 21/Maven 3.9+, oikeudet ja päivitetty `CHANGELOG.md`.

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
jar --describe-module --file target/vies-client-1.2.0.jar
jdeps --print-module-deps target/vies-client-1.2.0.jar
```

Skannaa koko historia salaisuuksista/henkilötiedoista; tarkista `LICENSE`, `NOTICE`, `SECURITY.md`, lokalisoinnit, API-dokumentaatio ja kaikki kolme JARia; live/load pysyy valinnaisena; dokumentoi julkiset muutokset.

Päivitä `pom.xml`, commit/changelog, tee mieluiten allekirjoitettu annotoitu tagi `git tag -s v1.2.0 -m "v1.2.0"`, ja `git push origin main --follow-tags`. Workflow ajaa verify uudelleen ja liittää binary/sources/Javadoc JARit sekä SHA-256:t. Älä julkaise epäonnistunutta tai katselmoimatonta committia.

Maven Central vaatii omistetun reverse-DNS `groupId`:n, metadataa, Portal-tunnukset, allekirjoituksen ja publish-konfiguraation. Älä commitoi tokenia/avainta. Julkaisun jälkeen tarkista lataus/checksum, avaa `[Unreleased]`, tee tarvittaessa Security Advisory ja päivitä versiot sekä lokalisoinnit.
