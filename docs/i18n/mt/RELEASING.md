# Malti (mt) — Rilaxx

> [Għażla tal-lingwa](../../LANGUAGES.md) · Is-sors Ingliż jipprevali; `LICENSE` fir-root biss huwa vinkolanti.

SemVer: PATCH fix kompatibbli, MINOR feature kompatibbli, MAJOR API/semantika breaking. Prerekwiżiti: `main` nadif u reviewed, Actions aħdar, JDK 21/Maven 3.9+, permessi u `CHANGELOG.md` aġġornat.

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
jar --describe-module --file target/vies-client-1.2.0.jar
jdeps --print-module-deps target/vies-client-1.2.0.jar
```

Skennja Git history għal secrets/data personali; ivverifika `LICENSE`, `NOTICE`, `SECURITY.md`, lokalizzazzjonijiet, API docs u t-tliet JARs; live/load jibqgħu optional; iddokumenta bidliet pubbliċi.

Aġġorna `pom.xml`, commit version/changelog, idealment signed annotated tag `git tag -s v1.2.0 -m "v1.2.0"`, imbagħad `git push origin main --follow-tags`. Workflow jerġa’ jagħmel verify u jżid binary/sources/Javadoc JAR + SHA-256. Tirrilaxxax commit fallut jew mhux reviewed.

Maven Central jeħtieġ reverse-DNS `groupId` proprjetà tiegħek, metadata, Portal credentials, signing u publish config. Tikkommettjax token/key. Wara r-rilaxx ivverifika download/checksum, iftaħ `[Unreleased]`, Security Advisory jekk meħtieġ u aġġorna versions/lokalizzazzjonijiet.
