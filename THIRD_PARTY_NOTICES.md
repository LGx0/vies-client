# Harmadik fél összetevői / Third-party components

## Terjesztett futásidejű JAR / Distributed runtime JAR

A `vies-client-1.0.0.jar` nem tartalmaz és nem igényel külső futásidejű könyvtárat.
Csak a JDK `java.base` és `java.net.http` moduljait használja.

The distributed JAR contains and requires no third-party runtime library. It only
uses the JDK modules `java.base` and `java.net.http`.

## Build- és tesztfüggőség / Build and test dependency

| Összetevő / Component | Verzió / Version | Használat / Scope | Licenc / License |
|---|---:|---|---|
| JUnit Jupiter | 6.1.2 | csak teszt / test only | EPL-2.0 |
| JUnit Platform | 6.1.2 | tranzitív teszt / transitive test | EPL-2.0 |
| OpenTest4J | 1.3.0 | tranzitív teszt / transitive test | Apache-2.0 |
| API Guardian | 1.1.2 | tranzitív teszt / transitive test | Apache-2.0 |
| JSpecify | 1.0.0 | tranzitív teszt / transitive test | Apache-2.0 |
| Apache Maven Wrapper scripts | 3.3.4 | forrás/build indító / source build launcher | Apache-2.0 |

A Maven build pluginjei és tranzitív tesztfüggőségei nem kerülnek be a kiadott JAR-
ba; azokat a Maven a saját artifact-metaadataik és licenceik szerint tölti le.

Maven plugins and transitive test dependencies are not bundled into the release JAR;
Maven resolves them under their own artifact metadata and licenses.

Források / Sources: <https://github.com/junit-team/junit-framework>,
<https://github.com/ota4j-team/opentest4j>,
<https://github.com/apiguardian-team/apiguardian>,
<https://github.com/jspecify/jspecify>

Maven Wrapper source: <https://github.com/apache/maven-wrapper>. Its required ASF
attribution is preserved in the root `NOTICE` file. The downloaded Maven 3.9.16
binary is not committed or bundled; `distributionSha256Sum` verifies it before use.

Ha új függőség kerül a projektbe, ezt a fájlt és a licenc-kompatibilitási ellenőrzést
ugyanabban a pull requestben frissíteni kell.

Any pull request adding a dependency must update this file and document license
compatibility.
