# Dansk (da) — THIRD_PARTY_NOTICES

> [Alle sprog](../../LANGUAGES.md) · Informativ oversættelse. Ved afvigelser er den kanoniske engelske tekniske eller juridiske kilde gældende. Kun `LICENSE` og `NOTICE` i roden er juridisk autoritative; oversættelsen erstatter dem ikke.

## Distributed runtime JAR / Distributed runtime JAR

`vies-client-1.0.0.jar`inkluderer eller kræver ingen eksterne runtime-biblioteker.
Den bruger kun modulerne`java.base`og`java.net.http`i JDK.

Den distribuerede JAR indeholder og kræver ikke noget tredjeparts runtime-bibliotek. Det kun
bruger JDK-modulerne`java.base`og`java.net.http`.

## Byg og test afhængighed / Byg og test afhængighed

| Komponent / Komponent | Version / Version | Brug / Omfang       | Licens / Licens |
| --------------------- | ----------------: | ------------------- | --------------- |
| JUnit Jupiter         |             6.1.2 | kun test / kun test | EPL-2.0         |

Maven build-plugins og transitive testafhængigheder er ikke inkluderet i den frigivne JAR
til; de downloades af Maven i henhold til deres egne artefaktmetadata og licenser.

Maven-plugins og transitive testafhængigheder er ikke bundtet i udgivelses-JAR;
Maven løser dem under deres egne artefakt-metadata og licenser.

Kilde / Kilde: <https://github.com/junit-team/junit-framework>

Hvis en ny afhængighed tilføjes til projektet, kontrolleres denne fil og licensens kompatibilitet
den skal opdateres i samme pull-anmodning.

Enhver pull-anmodning, der tilføjer en afhængighed, skal opdatere denne fil og dokumentlicens
kompatibilitet.
