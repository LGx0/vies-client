# Latviešu (lv) — Contributing
> [Valodu izvēle](../../LANGUAGES.md) · Noteicošais ir angļu tehniskais/juridiskais oriģināls un saknes `LICENSE`/`NOTICE`.

Atver/atsaucies uz issue; lielas API/licences/arhitektūras izmaiņas vispirms apspried; security ziņo privāti. JDK 21+, Maven 3.9+, Git; `./mvnw --batch-mode --no-transfer-progress clean verify`. Fork, īss branch no `main`, fokusētas izmaiņas un deterministisks regression tests katram labojumam. API mazs/type-safe un runtime dependency-free; `Unavailable` nekad nav `Invalid`; kopīgs stāvoklis thread-safe un bounded. Testi neizsauc publisku VIES, race izmanto latch/barrier. Dokumentē publisko uzvedību, breaking changes, pamato dependencies/performance. AI drīkst izmantot, bet autors pārbauda/testē/licencē visu un norāda būtisku izmantošanu. Contributions Apache-2.0 §5; ievēro `CODE_OF_CONDUCT.md`.

