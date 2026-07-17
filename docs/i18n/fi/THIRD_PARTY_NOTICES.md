# Suomi (fi) — Kolmannen osapuolen osat
> [Kielivalitsin](../../LANGUAGES.md) · Englanninkielinen tekninen ja oikeudellinen alkuperä sekä juuren `LICENSE`/`NOTICE` ovat määrääviä.

Runtime JAR ei sisällä eikä tarvitse kolmannen osapuolen kirjastoja; käytössä ovat JDK-moduulit `java.base` ja `java.net.http`. JUnit Jupiter 6.1.2 (EPL-2.0) on vain testeissä. Maven-pluginien ja transitiivisten testiriippuvuuksien koodia ei pakata julkaisuun, vaan ne ratkaistaan omilla metadata-/lisenssiehdoillaan. Lähde: <https://github.com/junit-team/junit-framework>. Riippuvuutta lisäävän PR:n on päivitettävä notices ja dokumentoitava lisenssiyhteensopivuus.

