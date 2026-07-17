# Español (`es`) — Instalación

[Selector de idioma](../../LANGUAGES.md) · [Original inglés](../../INSTALLATION.md)

> Si hay diferencias prevalece el inglés. `LICENSE` permanece sin traducir y es el único texto vinculante. Licencia del proyecto: Apache-2.0.

Necesita JDK 21+, Maven 3.9+ y, en producción, HTTPS saliente a `ec.europa.eu:443`. Los tests locales no requieren Internet.

```bash
java -version
./mvnw -version
git clone <repository-url>
cd vies-client
./mvnw clean verify
./mvnw install
```

En macOS instale `brew install openjdk@21`; en Linux use el paquete JDK 21 de su distribución; en Windows configure `JAVA_HOME` y añada `%JAVA_HOME%\bin` a `Path`. Configure también el SDK y Maven JDK del IDE en 21.

Los artefactos se generan en `target/`: JAR principal, fuentes y Javadocs. Maven usa `vies.client:vies-client:1.0.0`; Gradle usa `implementation("vies.client:vies-client:1.0.0")`. En JPMS declare `requires vies.client;`; en classpath no hace falta. Sin build tool use `--module-path`/`-cp`; al copiar fuentes a un proyecto no modular omita `module-info.java`.

Gestione proxy y certificados en JVM/infraestructura, nunca con credenciales en Git. Verificación:

```bash
./mvnw -q clean test
./mvnw -q package
jar --describe-module --file target/vies-client-1.0.0.jar
```

Reserve las llamadas reales para un smoke manual mínimo. Revise JDK incorrecto, proxy/TLS, indisponibilidad de VIES/Estado miembro y límites locales si falla.

## Plataforma, build, IDE y red completos

macOS: persista Homebrew y `JAVA_HOME="$(/usr/libexec/java_home -v 21)"`. Linux: `sudo apt install openjdk-21-jdk`; Windows: configure `JAVA_HOME`/`Path` y reabra terminal. Compruebe `java`, `javac` y `./mvnw`.

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
./mvnw install
ls -lh target/vies-client-1.0.0*.jar
javac --release 21 -cp vies-client-1.0.0.jar MyApp.java
java -cp .:vies-client-1.0.0.jar MyApp # Windows: ;
```

JAR sin runtime de terceros; JUnit/`jdk.httpserver` solo test. IDE: SDK, language level, Maven JDK, `java.configuration.runtimes` y `java.jdt.ls.java.home` a 21; recargar y no commitear rutas locales. HTTPS a `https://ec.europa.eu/taxation_customs/vies/`; proxy/truststore central, nunca TLS off. Contenedor con JDK/JRE 21, CA, non-root y límites.

| Problema | Solución |
|---|---|
| Java 14 / release 21 | SDK 21, `JAVA_HOME` Maven |
| `UnsupportedClassVersionError` | runtime 21+ |
| `NETWORK_ERROR` | DNS/proxy/firewall/TLS |
| `TIMEOUT` | investigar, no límite infinito |
| `CLIENT_OVERLOADED` | retry diferido, ingress/workers/limiter |
| `CACHE_ERROR` | Redis health/timeouts, evitar stampede |

## Instalación reproducible

`./mvnw clean verify` limpia, compila release 21, ejecuta unit/integration loopback y construye módulo. `./mvnw install` guarda binary, sources, Javadocs. Confirme JDK Maven. Cache Maven acelera CI pero no sustituye verificación.

Antes de publicar use install/JAR; después Maven Central/GitHub Packages. `mavenLocal()` solo desarrollo. Compruebe describe-module y apps classpath/JPMS. No target ni paths IDE en Git.

En red corporativa pruebe DNS, HTTPS CONNECT, proxy auth y truststore. Secrets en vault, TLS nunca off. Kubernetes: egress, DNS, CA, non-root, CPU/heap/connections, grace. Primero offline/demo; live smoke mínimo, nunca load.

Archive reports/checksum. Actualice staging con rollback. Live solo conectividad. Timeout se investiga antes de subir; overload reduce ingress o escala dentro del limiter.

## Maven, Gradle, JPMS e IDE

La dependencia Maven utiliza group `vies.client`, artifact `vies-client` y versión fija. Gradle usa `implementation("vies.client:vies-client:1.0.0")`. En módulos declare `requires vies.client`; en classpath no elimine el JAR de runtime. Sin build tool, compile con `javac --release 21 -cp ...` y use `:` o `;` según sistema.

VS Code/Windsurf e IntelliJ deben usar JDK 21 tanto para project SDK como Maven importer/language server. Después de cambiarlo, limpie workspace y recargue Maven. No commit de `.vscode` machine-specific, `.idea`, credentials o paths absolutos.

El paquete genera `vies-client-1.0.0.jar`, `-sources.jar` y `-javadoc.jar`. `jar --describe-module` confirma exports/requires. El JAR runtime no incluye JUnit ni plugins. Verifique `THIRD_PARTY_NOTICES.md` y checksum antes de distribuir.

Troubleshooting debe conservar el error original y machine code. `NETWORK_ERROR` exige DNS/proxy/firewall/TLS; `TIMEOUT` distingue connect/request/admission; `CLIENT_OVERLOADED` pide backpressure, no loop inmediato; `CACHE_ERROR` investiga Redis sin bypass masivo. `UnsupportedClassVersionError` indica JVM vieja, no bug VIES.

En CI use una imagen limpia JDK 21, modo batch/no-transfer-progress y archive Surefire más los tres JAR. Fije versiones de plugins y revise el dependency tree. El módulo de test necesita `jdk.httpserver`, pero el consumidor no. Después de cambiar JDK, elimine compilados viejos con `./mvnw clean` para evitar bytecode mezclado. El smoke local debe cerrar el proceso y liberar el puerto antes de repetir.
