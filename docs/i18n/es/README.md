# Español (`es`) — vies-client — comprobador de número de IVA

`disableCache()` desactiva la caché almacenada/persistente, pero las llamadas simultáneas con la misma pareja IVA + requester pueden compartir una única petición de red single-flight. Una llamada posterior, una vez finalizada, realiza una nueva petición a VIES. `consultationNumber` es opcional y VIES puede devolverlo, pero nunca está garantizado; su valor probatorio jurídico depende de las normas locales. Cargue `MY_EU_VAT_NUMBER` únicamente desde una fuente de secretos/configuración de confianza.

**Términos de búsqueda:** comprobador de número de IVA, validador de NIF-IVA, validación de IVA de la UE, comprobador de identificador fiscal, cliente Java VIES; `VAT checker`, `VAT number validator`, `EU VAT validation`, `tax ID checker`.

No es una calculadora fiscal general, sino un cliente para validar identificadores de IVA de la UE mediante VIES.

[Selector de idioma](../../LANGUAGES.md) · [Original inglés](../../../README.md)

> Esta traducción mejora la accesibilidad. Ante cualquier diferencia técnica o jurídica prevalece el original inglés. `LICENSE` no se traduce; solo su texto inglés tiene validez jurídica.

`vies-client` es un cliente Java 21+ seguro para múltiples hilos y sin dependencias de ejecución para la API REST VIES de la Comisión Europea. Funciona con Spring Boot, Quarkus, Micronaut o solo JDK mediante `java.net.http.HttpClient`.

- [Instalación](INSTALLATION.md) · [Integración](INTEGRATION.md) · [Diseño técnico](TECHNICAL.md)
- [Pruebas](TESTING.md) · [Código abierto](OPEN_SOURCE.md) · [Publicaciones](RELEASING.md)

```bash
./mvnw clean verify
./mvnw install
```

```java
try (var vies = ViesClient.builder().retries(1).build()) {
    ViesResponse result = vies.check("ESX0000000X");
}
```

Use una instancia compartida por aplicación y ciérrela durante el apagado. `check(...)` es síncrono y `checkAsync(...)` devuelve `CompletableFuture<ViesResponse>`. Las respuestas son `Valid`, `Invalid`, `Unavailable` o `MalformedInput`; los errores incluyen código estable, mensajes húngaro/inglés y `retryable`.

Para millones de trabajos, añada una cola persistente particionada, caché compartida, limitador distribuido y workers escalados horizontalmente. Single-flight, backpressure, límites, timeouts, reintentos con jitter e hilos virtuales protegen una JVM, pero no amplían la capacidad de VIES. Las contribuciones y donaciones voluntarias mediante el enlace Sponsor/café son bienvenidas; no otorgan derechos especiales ni modifican Apache-2.0.

## Build, JPMS y semántica completa

Maven: `vies.client:vies-client:1.2.0`. El módulo JPMS `vies.client` exporta `vies.client`, requiere `java.net.http`, encapsula internals y se declara `module my.api { requires vies.client; }`; classpath funciona sin declaración.

```java
try (var vies = ViesClient.builder().defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER"))).retries(1).build()) {
  switch (vies.check("DE 000 000 000")) {
    case ViesResponse.Valid v -> System.out.println(v.traderName().orElse("-"));
    case ViesResponse.Invalid i -> System.out.println("No válido");
    case ViesResponse.Unavailable u -> System.out.println(u.errorCode());
    case ViesResponse.MalformedInput m -> System.out.println(m.reason());
  }
}
```

El requester propio permite `consultationNumber`. Flujo: normalizar (`GR`→`EL`), caché, single-flight VAT/requester, admisión pending/red, HTTP reutilizado con timeout/retry, validación estricta boolean/fecha, caché solo `Valid`.

| Resultado | HTTP | Retry | Caché | Significado |
|---|---:|---|---|---|
| `Valid` | 200 | no | sí | confirmado |
| `Invalid` | 200 | no | no | no confirmado |
| `Unavailable` | 503/429 | por código | no | sin decisión |
| `MalformedInput` | 400 | no | no | corregir entrada |

| Opción builder | Defecto | Función |
|---|---:|---|
| `baseUrl` | VIES | mock controlado |
| `connectTimeout` / `requestTimeout` / `admissionTimeout` | 5 s / 8 s / 2 s | límites temporales |
| `defaultRequester` | ninguno | consultation ID |
| `retries` / `retryDelay` | 0 / 400 ms | 0–5; backoff+jitter |
| `maxConcurrentRequests` | 32 | HTTP reales |
| `maxPendingSyncRequests` / `maxPendingAsyncRequests` | 512 / 512 | memoria acotada |
| `cacheTtl` / `cacheMaxEntries` | 24 h / 10 000 | caché local |
| `cache` / `disableCache` | integrado / — | Redis o sin caché |
| `userAgent` / `executor` | módulo / virtual threads | identidad / ejecución |

Caché/single-flight JVM-locales; `Unavailable` nunca `Invalid`. Millones requieren queue durable, workers/batches limitados, Redis, limiter global, DLQ y retries controlados; no load público. Errores `code`, `messageHu`, `messageEn`, `retryable`; no PII en logs. `./mvnw clean verify`, demo JDK, Apache-2.0, seguridad privada y Sponsor sin SLA.

## Producción, cache y procesamiento masivo

En Spring Boot use un único bean con `destroyMethod="close"`. El controller trata las cuatro variantes: Valid/Invalid son HTTP 200 de dominio, Malformed 400, Overloaded 429 y fallo temporal 503. La lógica usa code/retryable, no mensajes.

```java
@Bean(destroyMethod="close")
ViesClient client() {
 return ViesClient.builder().defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
  .connectTimeout(Duration.ofSeconds(5)).requestTimeout(Duration.ofSeconds(8))
  .admissionTimeout(Duration.ofSeconds(2)).maxConcurrentRequests(32)
  .maxPendingSyncRequests(512).maxPendingAsyncRequests(512)
  .cacheTtl(Duration.ofHours(24)).retries(2)
  .retryDelay(Duration.ofMillis(400)).build();
}
```

Redis implementa `ViesCache` con clave VAT+requester, TTL, timeout corto y schema versionado. Read failure sigue `CACHE_ERROR` para evitar que una caída lance todos los pods a VIES. Write failure conserva Valid. Cache es optimización; auditoría guarda VAT, requester, UTC, resultado y consultation opcional.

Workers leen batches limitados de queue durable. Solo Unavailable retryable/overload se reprograman con backoff, jitter y máximo; después DLQ. Invalid/Malformed se cierran. Limiter distribuido limita todos los pods. Autoscaling sin budget agrava throttling.

VIES federa sistemas nacionales con latencia, campos, disponibilidad y cuotas variables. Virtual threads reducen espera, no capacidad upstream. El host limita ingress, queue, consumers, futures y auditoría. Health usa liveness/readiness local y check-status moderado, no una VAT por probe.

VAT, empresa, dirección, requester y consultation son sensibles: nunca labels/logs normales. Defina acceso, cifrado, retención y borrado. No es consejo fiscal/legal ni afiliación UE.

Staging simula hit/down cache, 429/5xx, timeout, backlog, cancel y shutdown. Criterios: ningún falso Invalid, memoria acotada, límites, audit y replay DLQ. SemVer, checksums, notes y rollback. Campos trader/consultation opcionales.

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
./mvnw package
jar --describe-module --file target/vies-client-1.2.0.jar
java -cp target/classes examples/ViesDemoServer.java
```

Códigos temporales: service/member unavailable, busy, timeout, network, HTTP 408/429/5xx. Input, blocked, closed y malformed requieren intervención. Use mapping central. Cache disabled fuerza llamadas nuevas salvo single-flight simultáneo. Durations/limits inválidos u overflow se rechazan. Tras close, nueva operación recibe error estructurado.

## Contrato de uso y mantenimiento

El resultado `Valid` contiene VAT normalizada, fecha de consulta, origen cache y opcionales de nombre, dirección y consultation. Los placeholders upstream se convierten en vacíos. `Invalid` solo aparece cuando VIES devuelve una decisión auténtica; cualquier body ambiguo, boolean ausente, fecha incorrecta o problema técnico se mantiene separado. Esto evita que una caída sea interpretada como baja fiscal.

`checkAsync` usa virtual threads por defecto. Un executor propio se configura mediante builder y sigue siendo propiedad del host. Cancelar una future del caller no debe cancelar el resultado compartido de otros followers. `close()` interrumpe tasks propios, limpia inflight y devuelve `CLIENT_CLOSED` de forma coherente a leader/follower. El shutdown del contenedor debe parar ingress y consumers antes de cerrar.

La disponibilidad por Estado miembro puede consultarse con el endpoint específico, pero es indicativa y no reemplaza una validación. Las métricas recomendadas son latency, result, code, retry, cache hit, pending, inflight y reject; nunca VAT como label. Logs incluyen correlation ID y machine code, con payload redacted. Alerts diferencian cache down, VIES timeout, overload local, backlog y DLQ.

Para contribuir, use Java 21, mantenga zero runtime dependencies, añada regression determinista y actualice docs/localizaciones. Security se reporta por advisory privado. Releases contienen binary, sources, Javadocs y SHA-256 del mismo CI. La donación Buy Me a Coffee es voluntaria y no compra soporte, prioridad, SLA ni gobierno.

El módulo se puede usar en classpath o con `requires vies.client`. La API no instala Redis, queue, limiter, scheduler ni audit store: son responsabilidades explícitas del sistema integrador. Esta separación mantiene la librería simple y permite escoger infraestructura sin dependencias transitivas.

La compatibilidad mínima es Java 21 y el artefacto se compila con ese release. Las aplicaciones pueden ejecutarlo en JDK posteriores tras sus propias pruebas. No copie el package `internal` ni dependa de su implementación. El API público usa records, sealed interfaces y Optional para expresar decisiones y campos ausentes. Una actualización compatible conserva esos contratos; cambios incompatibles requieren versión major y guía de migración.

El proyecto acepta issues y pull requests bajo las políticas documentadas. Un bug report útil incluye versión, JDK, entorno, configuración redacted, pasos y código de error. No publique vulnerabilidades ni datos reales. El soporte comunitario es best effort. Revise siempre el resultado VIES según las normas aplicables en su país y conserve la evidencia que exija su proceso.
