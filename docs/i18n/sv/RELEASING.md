# Svenska (sv) — RELEASING

> [Alla språk](../../LANGUAGES.md) · Informativ översättning. Vid avvikelser gäller den kanoniska engelska tekniska eller juridiska källan. Endast `LICENSE` och `NOTICE` i roten är juridiskt auktoritativa; översättningen ersätter dem inte.

## 1. Förutsättningar / Förutsättningar

- Rengör`main`-grenen och gröna GitHub-åtgärder;
- lämplig GitHub-behörighet för att skapa taggen och släppa den;
- JDK 21 och Maven 3.9+;
- releaseversion och datum registrerat i filen`CHANGELOG.md`.

## 2. Versionering

Projektet använder semantisk versionering:

- `PATCH`: kompatibel buggfix;
- `MINOR`: kompatibel ny funktion;
- `MAJOR`: bryter offentlig API eller semantik.

Projektet använder semantisk versionering: patch för kompatibla korrigeringar, mindre för kompatibla
funktioner och stora för att bryta API eller semantiska förändringar.

## 3. Förhandsverifiering

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
jar --describe-module --file target/vies-client-1.0.0.jar
jdeps --print-module-deps target/vies-client-1.0.0.jar
```

Kontrollera även:

- inga hemligheter eller personlig information i hela Git-historiken;
- `LICENSE`,`NOTICE`,`SECURITY.md`och dokumentationen är aktuella;
- Javadoc och källor JAR skapade;
- det finns inget obligatoriskt live VIES eller belastningstest i CI;
- ändringar av det offentliga API:et ingår i ändringsloggen.

## 4. GitHub Release

1. Ställ in versionen i filen`pom.xml`.
2. Ange ändringsloggen och versionen.
3. Skapa en signerad kommenterad tagg:`git tag -s v1.0.0 -m "v1.0.0"`.
4. Push commit och tagga:`git push origin main --follow-tags`. 5.`release.yml`-arbetsflödet kör om testerna och bifogar sedan den binära,
   källor och Javadoc JAR-filer för GitHub Release.

Använd signerade kommenterade taggar när det är möjligt. Skapa aldrig en release från en ogranskad eller
misslyckas begå.

## 5. Maven Central eller GitHub-paket

Den nuvarande versionen är lokal och redo för GitHub Release-distribution. Mer för Maven Central
nödvändig:

- en verifierbar, egen omvänd DNS`groupId`;
- Projekt`url`,`scm`,`developers`och metadata för distributionshantering;
- Central portalregistrering och token;
- GPG-signatur och centralkompatibel publiceringskonfiguration.

Den nuvarande versionen är redo för lokal installation och GitHub-versioner. Maven Central
publicering kräver dessutom ett ägt omvänd DNS-grupp-ID, komplett projekt/SCM
metadata, Central Portal-uppgifter, artefaktsignering och publiceringskonfiguration.

Lägg inte en token eller privat GPG-nyckel i repot. GitHub-åtgärder är hemliga och minimala
använda behörighet. / Begå aldrig tokens eller privata signeringsnycklar. Använd GitHub
Handlingshemligheter och minsta privilegium.

## 6. Steg efter release / Post-release

- kontrollera nedladdningar och SHA-256-värden;
- starta en ny`[Unreleased]`-session;
- publicera en GitHub Security Advisory för en säkerhetskorrigering;
- uppdatera den dokumenterade beroendeversionen.
