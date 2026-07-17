# Latviešu (lv) — Laidieni

> [Valodu izvēle](../../LANGUAGES.md) · Noteicošais ir angļu tehniskais/juridiskais avots; tikai saknes `LICENSE` ir saistošs.

SemVer: PATCH saderīgs labojums, MINOR saderīga funkcija, MAJOR lauž API/semantiku. Priekšnoteikumi: tīrs pārskatīts `main`, zaļš Actions, JDK 21/Maven 3.9+, tiesības un atjaunots `CHANGELOG.md`.

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
jar --describe-module --file target/vies-client-1.2.0.jar
jdeps --print-module-deps target/vies-client-1.2.0.jar
```

Pārbaudi visu Git history noslēpumiem/personas datiem; aktualizē `LICENSE`, `NOTICE`, `SECURITY.md`, lokalizācijas, API dokumentāciju un trīs JAR; live/load paliek izvēles; publiskās izmaiņas ir changelog.

Atjauno `pom.xml`, commit versiju/changelog, vēlams parakstīts anotēts tags `git tag -s v1.2.0 -m "v1.2.0"`, tad `git push origin main --follow-tags`. Workflow atkārto verify un pievieno binary/sources/Javadoc JAR ar SHA-256. Neizlaid nepārskatītu vai neveiksmīgu commit.

Maven Central vajag savu reverse-DNS `groupId`, metadatus, Portal akreditāciju, parakstīšanu un publish konfigurāciju. Necommitē tokenus/atslēgas. Pēc laidiena pārbaudi download/checksum, atver `[Unreleased]`, security fixam Advisory un atjauno versijas/lokalizācijas.
