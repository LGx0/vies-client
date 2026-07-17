# Dansk (da) — RELEASING

> [Alle sprog](../../LANGUAGES.md) · Informativ oversættelse. Ved afvigelser er den kanoniske engelske tekniske eller juridiske kilde gældende. Kun `LICENSE` og `NOTICE` i roden er juridisk autoritative; oversættelsen erstatter dem ikke.

## 1. Forudsætninger / Forudsætninger

- ren`main`-gren og grønne GitHub-handlinger;
- passende GitHub-autoritet til at oprette tagget og frigive;
- JDK 21 og Maven 3.9+;
- udgivelsesversion og dato registreret i filen`CHANGELOG.md`.

## 2. Versionering

Projektet bruger semantisk versionering:

- `PATCH`: kompatibel fejlrettelse;
- `MINOR`: kompatibel ny funktion;
- `MAJOR`: bryde offentlig API eller semantik.

Projektet bruger semantisk versionering: patch til kompatible rettelser, mindre for kompatible
funktioner og vigtige til at bryde API eller semantiske ændringer.

## 3. Bekræftelse før udgivelse

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
jar --describe-module --file target/vies-client-1.0.0.jar
jdeps --print-module-deps target/vies-client-1.0.0.jar
```

Tjek også:

- ingen hemmeligheder eller personlige data i hele Git-historien;
- `LICENSE`,`NOTICE`,`SECURITY.md`og dokumentation er aktuelle;
- Javadoc og kilder JAR oprettet;
- der er ingen obligatorisk live VIES eller belastningstest i CI;
- ændringer til den offentlige API er inkluderet i ændringsloggen.

## 4. GitHub-udgivelse

1. Indstil versionen i filen`pom.xml`.
2. Overfør ændringsloggen og versionen.
3. Opret et signeret annoteret tag:`git tag -s v1.0.0 -m "v1.0.0"`.
4. Push commit og tag:`git push origin main --follow-tags`. 5.`release.yml`-arbejdsgangen kører testene igen og vedhæfter derefter den binære,
   kilder og Javadoc JAR-filer til GitHub Release.

Brug signerede annoterede tags, når det er muligt. Opret aldrig en udgivelse fra en uanmeldt eller
svigtende forpligtelse.

## 5. Maven Central eller GitHub-pakker

Den nuværende build er lokal og klar til GitHub Release-distribution. Mere til Maven Central
nødvendig:

- en verificerbar, egen omvendt DNS`groupId`;
- projekt`url`,`scm`,`developers`og distributionsstyringsmetadata;
- Central portalregistrering og token;
- GPG-signatur og central-kompatibel udgivelseskonfiguration.

Den nuværende build er klar til lokal installation og GitHub-udgivelser. Maven Central
udgivelse kræver desuden et ejet omvendt DNS-gruppe-id, komplet projekt/SCM
metadata, Central Portal-legitimationsoplysninger, artefaktsignering og udgivelseskonfiguration.

Læg ikke en token eller en privat GPG-nøgle i repoen. GitHub-handlinger er hemmelige og minimale
bruge autorisation. / Forpligt aldrig tokens eller private signeringsnøgler. Brug GitHub
Handlingshemmeligheder og mindste privilegier.

## 6. Trin efter udgivelse / Post-udgivelse

- tjek udgivelsesdownloads og SHA-256-værdier;
- start en ny`[Unreleased]`session;
- udgiv en GitHub Security Advisory for en sikkerhedspatch;
- Opdater den dokumenterede afhængighedsversion.
