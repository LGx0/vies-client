# Svenska (sv) — THIRD_PARTY_NOTICES

> [Alla språk](../../LANGUAGES.md) · Informativ översättning. Vid avvikelser gäller den kanoniska engelska tekniska eller juridiska källan. Endast `LICENSE` och `NOTICE` i roten är juridiskt auktoritativa; översättningen ersätter dem inte.

## Distributed runtime JAR / Distributed runtime JAR

`vies-client-1.2.0.jar`inkluderar eller kräver inga externa runtime-bibliotek.
Den använder endast modulerna`java.base`och`java.net.http`i JDK.

Den distribuerade JAR innehåller och kräver inget runtime-bibliotek från tredje part. Det bara
använder JDK-modulerna`java.base`och`java.net.http`.

## Bygga och testa beroende / Bygga och testa beroende

| Komponent / Komponent | Version / Version | Användning / Omfattning   | Licens / Licens |
| --------------------- | ----------------: | ------------------------- | --------------- |
| JUnit Jupiter         |             6.1.2 | endast test / endast test | EPL-2.0         |

Maven build-plugins och transitiva testberoenden ingår inte i den släppta JAR
till; de laddas ner av Maven enligt deras egna artefaktmetadata och licenser.

Maven-plugin-program och transitiva testberoenden är inte inkluderade i release-JAR;
Maven löser dem under sina egna artefaktmetadata och licenser.

Källa / Källa: <https://github.com/junit-team/junit-framework>

Om ett nytt beroende läggs till i projektet kontrolleras den här filen och licensens kompatibilitet
den måste uppdateras i samma pull-begäran.

Alla pull-begäranden som lägger till ett beroende måste uppdatera denna fil och dokumentlicens
kompatibilitet.
