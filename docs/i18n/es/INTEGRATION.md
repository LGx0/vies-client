# Español (`es`) — Integración

`disableCache()` desactiva la caché almacenada/persistente, pero las llamadas simultáneas con la misma pareja IVA + requester pueden compartir una única petición de red single-flight. Una llamada posterior, una vez finalizada, realiza una nueva petición a VIES. `consultationNumber` es opcional y VIES puede devolverlo, pero nunca está garantizado; su valor probatorio jurídico depende de las normas locales. Cargue `MY_EU_VAT_NUMBER` únicamente desde una fuente de secretos/configuración de confianza.

[Selector de idioma](../../LANGUAGES.md) · [Original inglés](../../INTEGRATION.md)

> Prevalece el original inglés; `LICENSE` no se traduce. Licencia del proyecto: Apache-2.0.

Cree un `ViesClient` singleton por proceso y ejecute `close()` al apagar. Es thread-safe; no cree uno por petición.

```java
var client = ViesClient.builder()
 .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
 .maxConcurrentRequests(32).maxPendingSyncRequests(512)
 .maxPendingAsyncRequests(512).admissionTimeout(Duration.ofSeconds(2))
 .retries(2).build();
```

Gestione exhaustivamente `Valid`, `Invalid`, `Unavailable` y `MalformedInput`. No reintente `Invalid` ni `MalformedInput`; respete `error().retryable()` para `Unavailable`. Mapeo HTTP recomendado: 200, 400, 429 para `CLIENT_OVERLOADED` y 503 para fallos temporales. Devuelva `errorCode`, `messageHu`, `messageEn`, `retryable`.

En Spring Boot registre `@Bean(destroyMethod = "close")`; en JDK puro reutilice el mismo cliente en los handlers. `defaultRequester(...)` permite recibir `consultationNumber`; archive número, fecha, VAT consultado y resultado según auditoría y privacidad.

Para varios pods implemente `ViesCache` con Redis, TTL y clave VAT+requester. Single-flight y semáforos son locales: use cola duradera, consumers acotados, rate limiter distribuido y DLQ. Limite también futuros retenidos por el llamador. Observe latencia, caché, reintentos y rechazos, no registre VAT completas y no haga carga contra VIES público.

## Contrato completo y operación

`check` devuelve `ViesResponse`; `checkAsync`, `CompletableFuture`. Cancelar follower no destruye shared work; caché bloqueante corre en worker limitado.

| Respuesta | HTTP | Retry | Interpretación |
|---|---:|---|---|
| `Valid`/`Invalid` | 200 | no | decisión |
| `MalformedInput` | 400 | no | corregir |
| `CLIENT_OVERLOADED` | 429 | diferido | backpressure |
| retryable `Unavailable` | 503 | diferido | sin decisión |

Spring: `@Bean(destroyMethod="close")`, properties y controller con código/mensajes/retryable. JDK: cliente común en `HttpServer`. Redis: VAT+requester, TTL, esquema versionado seguro, timeout corto; read error→`CACHE_ERROR`, write error conserva Valid. Queue/DLQ solo retryable, intentos acotados, backoff+jitter; ack Invalid/Malformed. N pods requieren limiter distribuido.

Health: liveness, readiness queue/cache, `check-status` moderado, no VAT por probe. Métricas sin VAT: latencia, resultado, código, retry, hit, pending/in-flight/reject. Evite cliente/request, recursos ilimitados, retry loop, no TTL, Unavailable→Invalid, load público, PII logs y olvidar close. Producción: shutdown, capacidad, audit/privacy, alerts, DLQ, failover, rollback.

## Referencia Spring Boot

```java
@Configuration class ViesConfig {
 @Bean(destroyMethod="close") ViesClient client(){ return ViesClient.builder().retries(1).build(); }
}
@RestController class VatController {
 private final ViesClient vies;
 VatController(ViesClient vies){this.vies=vies;}
 @GetMapping("/api/vat/{number}") ResponseEntity<?> check(@PathVariable String number){
  return switch(vies.check(number)){
   case ViesResponse.Valid v -> ResponseEntity.ok(Map.of("valid",true));
   case ViesResponse.Invalid i -> ResponseEntity.ok(Map.of("valid",false));
   case ViesResponse.MalformedInput m -> ResponseEntity.badRequest().body(m.error());
   case ViesResponse.Unavailable u -> ResponseEntity.status("CLIENT_OVERLOADED".equals(u.errorCode())?429:503).body(u.error());
  }; }
}
```

Smoke JDK: `./mvnw -q package`, `java -cp target/classes examples/ViesDemoServer.java`, `curl "http://localhost:8085/vat-check?number=ESX0000000X"`.

## Redis, queue, auditoría y shutdown

Cache distribuido guarda solo Valid con schema, VAT, requester, fecha, opcionales y consultation. Tolere campos futuros, rechace tipos; no serialización Java nativa. Observe latencia/error y readiness.

Mensaje durable: job ID, VAT, requester, intento, not-before, correlation. Consumer idempotente persiste y ack con transacción/outbox. Retryable backoff+jitter; máximo a DLQ. Input permanente. Consultation ayuda pero retención/inmutabilidad/acceso/borrado es host. Nombre/dirección opcionales.

Executor externo pertenece al host. Client cancela handles pero no lo cierra. Shutdown: stop ingress, pause consumers, grace, close. CLIENT_CLOSED no decisión. Close serializado, callbacks fuera lock, follower cancel no cancela shared.

Métricas result/code/retry/reject, latencia total/cache/HTTP, pending/inflight, queue age/DLQ; no VAT. Logs correlation+code redacted. Alerts timeout, overload, cache, backlog, Invalid.

Global = pods×local; use limiter compartido por Estado. Scale por queue age/budget/health. Pruebe failover, idempotencia, replay, rollback.

Valide tamaño/encoding/auth/tenant quota antes. Limits internos no protegen API. Coordine proxy/controller timeout. No millones de timers/futures.

Estados PENDING, VALID, INVALID, RETRYABLE_ERROR, PERMANENT_ERROR, DEAD_LETTER. Un Unavailable nuevo no borra decisión histórica. Requester entra en key; override coherente, inválido fail-fast. Consultation Optional.

Spring properties y controller estructurado; JDK instancia común. Respuesta API con machine code, HU/EN/retryable, nunca stacktrace/payload.

## Ejemplo de cache y respuesta HTTP

Un adaptador Redis implementa lectura por key y escritura `Valid` con TTL. La key se forma a partir de valores ya normalizados, no de input raw. La serialización debe incluir versión y rechazar tipos inesperados. No guarde objetos Java arbitrarios. Un circuit breaker externo puede proteger Redis, pero su apertura debe producir un estado visible y métricas.

El controller devuelve un objeto estable: para decisiones, `valid`, datos opcionales y consulta; para errores, `errorCode`, `retryable`, `messageHu`, `messageEn`. HTTP 200 para Invalid evita confundir una decisión negativa con fallo técnico. HTTP 429 permite al cliente aplicar backoff; 503 indica que no existe decisión actual.

Los endpoints de entrada necesitan límite de longitud, normalización, auth y quota. Si una misma VAT se consulta en muchos tenants, el requester cambia la key y puede impedir deduplicación; esto es correcto porque la consultation depende del requester. No mezcle resultados de requesters distintos.

En multi-region, la cache compartida y el limiter deben decidir su ámbito. Un único limiter global reduce riesgo pero añade latencia/disponibilidad; budgets regionales coordinados pueden ser necesarios. La queue debe preservar orden solo cuando lo requiere el negocio. Deduplication ID evita que una redelivery escriba múltiples auditorías inconsistentes.

Durante shutdown, el orchestrator concede tiempo mayor que request timeout más cleanup. Si vence, los jobs no acknowledged vuelven a queue. Los consumers no deben ack antes de persistir resultado. El replay DLQ es una operación auditada, limitada y capaz de usar la versión nueva del parser sin perder el payload original redacted.

La observabilidad correlaciona API request, queue job, VIES attempt y audit record. Los dashboards muestran p50/p95/p99, retry distribution, cache hit, upstream status y saturación. Un aumento de Invalid puede ser real o un bug de mapping; alerts requieren inspección de samples sintéticos, nunca datos clientes públicos.

## Disponibilidad y fallos parciales

`checkAvailability()` puede alimentar un dashboard con el estado publicado de los Estados miembros, pero no debe bloquear automáticamente todas las validaciones por una muestra antigua. Cachee el status brevemente, marque su timestamp y combine con métricas reales. Si un país falla, otros pueden seguir; el limiter y circuit breaker externos pueden operar por country code.

Un fallo de cache read se trata distinto de write: read impide saber si existe respuesta compartida y por seguridad evita bypass masivo; write ocurre después de una decisión válida y no debe cambiarla. Registre ambos. Si Redis está degradado, pause o reduzca consumers según política, en vez de desactivar cache en todos los pods.

Los retries internos son pequeños y cubren fallos transitorios dentro de una solicitud. Los retries de minutos/horas pertenecen a queue. Evite multiplicar retries de HTTP client, service mesh, controller y queue sin un presupuesto común, porque tres capas de tres intentos pueden producir una explosión. Propague correlation y attempt count.

En APIs multi-tenant, la cuota por tenant evita que uno consuma pending slots. La respuesta `CLIENT_OVERLOADED` debe ser segura e incluir `Retry-After` calculado por la capa HTTP cuando sea posible. El client no promete fairness entre tenants; scheduling se implementa fuera.
