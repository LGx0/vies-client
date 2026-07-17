# Lietuvių (lt) — Leidimai

> [Kalbų pasirinkimas](../../LANGUAGES.md) · Pirmenybę turi angliškas techninis/teisinis šaltinis; tik šakninis `LICENSE` privalomas.

SemVer: PATCH suderinamas taisymas, MINOR suderinama funkcija, MAJOR laužo API/semantiką. Sąlygos: švarus peržiūrėtas `main`, žalias Actions, JDK 21/Maven 3.9+, teisės ir atnaujintas `CHANGELOG.md`.

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
jar --describe-module --file target/vies-client-1.0.0.jar
jdeps --print-module-deps target/vies-client-1.0.0.jar
```

Patikrinkite visą Git history dėl paslapčių/asmens duomenų; atnaujinkite `LICENSE`, `NOTICE`, `SECURITY.md`, lokalizacijas, API dokumentaciją ir tris JAR; live/load lieka pasirenkami; vieši pokyčiai įrašomi changelog.

Atnaujinkite `pom.xml`, commit versiją/changelog, geriausia pasirašytas annotated tag `git tag -s v1.0.0 -m "v1.0.0"`, tada `git push origin main --follow-tags`. Workflow pakartoja verify, prideda binary/sources/Javadoc JAR ir SHA-256. Neleiskite neperžiūrėto ar nesėkmingo commit.

Maven Central reikia nuosavo reverse-DNS `groupId`, metaduomenų, Portal kredencialų, pasirašymo ir publish konfigūracijos. Necommitinkite tokenų/raktų. Po leidimo tikrinkite downloads/checksum, atverkite `[Unreleased]`, saugumo taisymui Advisory ir atnaujinkite versijas/lokalizacijas.
