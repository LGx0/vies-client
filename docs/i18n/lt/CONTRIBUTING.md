# Lietuvių (lt) — Contributing
> [Kalbų pasirinkimas](../../LANGUAGES.md) · Pirmenybę turi angliškas techninis/teisinis originalas ir šakniniai `LICENSE`/`NOTICE`.

Atidarykite/nurodykite issue; didelius API/licencijos/architektūros pokyčius aptarkite; security praneškite privačiai. JDK 21+, Maven 3.9+, Git; `./mvnw --batch-mode --no-transfer-progress clean verify`. Fork, trumpa šaka nuo `main`, fokusuotas pakeitimas ir deterministinis regression testas kiekvienam taisymui. API mažas/type-safe ir runtime dependency-free; `Unavailable` niekada ne `Invalid`; bendras state thread-safe ir bounded. Testai nekviečia viešo VIES, race valdomas latch/barrier. Dokumentuokite elgesį/breaking changes, pagrįskite dependencies/performance. AI leidžiamas, tačiau autorius viską peržiūri/testuoja/licencijuoja ir atskleidžia reikšmingą naudojimą. Contributions Apache-2.0 §5; laikykitės `CODE_OF_CONDUCT.md`.

