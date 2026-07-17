# Español (`es`) — Diseño técnico

[Selector de idioma](../../LANGUAGES.md) · [Original inglés](../../TECHNICAL.md)

> En caso de diferencia manda el inglés. `LICENSE` permanece inalterado y vinculante. Licencia del proyecto: Apache-2.0.

El módulo JPMS `vies.client` exporta solo `vies.client`, encapsula internos y no tiene dependencias runtime fuera del JDK. `ViesResponse` es sealed: `Valid`, `Invalid`, `Unavailable`, `MalformedInput`; `ViesError` ofrece código estable, textos HU/EN y retry.

Flujo: normalizar y validar formato; leer caché; agrupar claves VAT/requester mediante single-flight; aplicar admisión pending y semáforo HTTP; llamar con `HttpClient` reutilizado, timeout y retry limitado; validar JSON estrictamente; cachear con TTL solo `Valid` fiable y limpiar estado antes de completar.

Async usa hilos virtuales por defecto. Solo leaders únicos consumen permiso pending; followers iguales no. Sync, async y red tienen límites independientes y liberan permisos en éxito, rechazo, cancelación, error y cierre. Solo fallos temporales se reintentan con backoff+jitter. Error de lectura de caché devuelve `CACHE_ERROR` sin stampede; error de escritura no borra la respuesta válida.

`close()` bloquea admisiones nuevas, completa leader/followers coherentemente, interrumpe tareas propias y no cierra executors externos. A escala: API → cola persistente → workers acotados → caché compartida + limitador distribuido → VIES. Mida latencia, resultado, error, retry, hit y rechazo sin usar VAT como etiquetas. Proteja TLS y logs; benchmarks locales no son SLA de VIES.

## Invariantes y límites

| Tipo | Decisión | Retry | Caché |
|---|---|---|---|
| `Valid` | confirmada | no | sí |
| `Invalid` | no confirmada | no | no |
| `Unavailable` | ninguna | por código | no |
| `MalformedInput` | entrada mala | no | no |

`Valid` incluye VAT, opcionales, timestamp y cache-origin; `---` vacío. Código desconocido se preserva sin retry por defecto. Tras miss se revalida leadership/cache. `ConcurrentHashMap` in-flight; semáforos sync pending, async leaders, HTTP. Cleanup antes de `complete`; fatal `Error` exceptional y relanzado.

Retry para timeout/busy/MS/network y HTTP 408/429/5xx, no input/close/overload local. Backoff exponencial+jitter y overflow validado. TTL cache limitado pero aproximado/eventual y no LRU; read failure evita stampede, write failure no cambia Valid, solo Valid se guarda. `close` serializa, igual `CLIENT_CLOSED`, interrumpe tasks propios, deja executor externo, callbacks fuera del lock.

Loopback JDK 21, no SLA: hit 8,91 M/s; formato 9,02 M/s; HTTP 4.044/s. Medir warm-up/p50/p95/p99. Solo HTTPS, URL normalizada, no secrets/PII, caché sensible, respuestas untrusted estrictas.

## Códigos públicos

| Familia | Tratamiento |
|---|---|
| `SERVICE_UNAVAILABLE`, `MS_UNAVAILABLE`, `SERVER_BUSY`, `TIMEOUT` | upstream temporal, retry limitado |
| `GLOBAL_MAX_CONCURRENT_REQ`, `MS_MAX_CONCURRENT_REQ`, HTTP `408`/`429`/`5xx` | throttling/server, retry diferido |
| `NETWORK_ERROR` | DNS/TCP/TLS, backoff |
| `CLIENT_OVERLOADED` | admisión local llena, requeue |
| `CLIENT_CLOSED`, `INTERRUPTED` | lifecycle/cancelación, sin decisión |
| `INVALID_INPUT`, `INVALID_VAT_FORMAT`, `INVALID_REQUESTER_INFO` | entrada permanente |
| `IP_BLOCKED`, `VAT_BLOCKED` | política VIES, revisión manual |
| `MALFORMED_RESPONSE` | schema/fecha/boolean inválido |
| `CACHE_ERROR` | caché caída, sin stampede |
| `INTERNAL_ERROR`, desconocido | sin decisión, no retry default |

## Módulos, flujo e invariantes

API exporta client/builder, requester, results, errors, availability y cache hook; parser/TTL internos. Runtime solo base/http. Key VAT+requester. Hit copia marcada. Miss: un leader relee cache y quizá HTTP; followers shared con vista propia. Segundo read cierra race.

Tres admissions: sync pending, unique async leaders, HTTP. Followers iguales no permit. Cada acquire libera en success, rejection, cancel, interrupt, parse, fatal Error, close. Cleanup antes complete; fatal Error exceptional+rethrow.

HttpClient reutilizado separa connect/request. URL normalizada. Non-2xx clasificado; 2xx untrusted: object, boolean real, date parseable. Missing/string boolean o bad date es MALFORMED, nunca Invalid; offset UTC, sin tiempo inventado.

Retry limitado exponential+jitter. 408/429/5xx, timeout/busy/network sí; input/closed/local overload no. Duration overflow validado e interrupt respetado.

TTL local bounded pero approximate/eventual, no LRU. Solo Valid. Read failure no bypass, write conserva. In-flight por pending; host limita futures. Virtual threads no reducen payload/queue/audit.

Close serializa, bloquea admission, igual CLIENT_CLOSED, interrumpe own tasks, external abierto, callback fuera lock. Races testeadas.

Loopback no SLA. Compare mismo perfil, warm-up, p50/p95/p99, GC/error. HTTPS/cert normal, URL controlada, logs mínimos, cache sensible, scans, signed/checksum.

No queue, distributed limiter, scheduler, encryption ni audit store: host. ViesCache pequeño mantiene zero-dependency. Sealed fuerza variantes; Error conserva unknown. Valid optional/audit/origin. Availability no decisión.

## Modelo de memoria, tiempo y errores

La map inflight contiene como máximo leaders distintos admitidos; followers comparten entry. Los semáforos son fair solo según implementación y no garantizan orden de negocio. Admission timeout evita waits indefinidos. Sync callers también tienen límite, por lo que un millón de threads externos no se convierte en un millón de entradas internas.

Todos los cálculos de Duration se validan al construir. El retry delay se multiplica con saturación segura y jitter. El audit timestamp proviene de VIES y se normaliza a UTC; el cache expiry usa reloj local solo para TTL, no como evidencia de consulta. Huge positive values se rechazan antes de `toNanos`/`toMillis` overflow.

El cleanup de async ocurre antes de completar shared Future. De lo contrario, un `thenCompose` podría ver aún ocupado el único pending permit o reusar inflight completado con cache disabled. Las pruebas cubren ambos defectos. Submitted task handles se rastrean para cancelar un executor custom; result futures por sí solas no bastan.

Close usa coordinación de lifecycle para que admission y cierre no crucen un cache read bloqueado. Callers de close concurrentes se unen al shutdown lógico. Leader y followers observan el mismo resultado, incluso si el cierre ocurre durante write cache. Los callbacks del usuario nunca se ejecutan sosteniendo el lock.

Errores fatales `Error` no se convierten silenciosamente en `Unavailable`: la Future termina exceptional y el uncaught handler también recibe el error. Runtime failures esperables se convierten en códigos estructurados. Interrupted restaura/respeta estado. Executor rejection libera permit y map, permitiendo una operación posterior.

Seguridad incluye validación de base URL HTTP(S), producción HTTPS, encoding de path, no redirect arbitrario, respuesta JSON limitada/estricta, redacción y dependencia cero. El host controla SSRF evitando exponer `baseUrl` a tenants. Redis y auditoría requieren auth, TLS, mínimo privilegio y retención.

## Garantías y no garantías

El single-flight garantiza una operación compartida por key dentro de una instancia durante la ventana inflight; no deduplica entre JVM ni después del cleanup cuando cache está disabled. Same-key followers no consumen pending adicional, pero conservan su propia Future y memoria de caller. El orden de completion entre keys distintas no está definido.

Los límites son de protección, no throughput recomendado. `maxConcurrentRequests` cuenta intentos HTTP activos; pending cuenta trabajo admitido. Admission timeout puede devolver overload aunque un slot se libere inmediatamente después. Esto es preferible a espera/memoria sin límite. La queue externa decide retry y prioridad.

El cache integrado no promete persistencia, distribución ni LRU exacta. Una entrada puede expirar o ser evicted y causar nueva consulta. La TTL se aplica a resultados válidos y no modifica el timestamp de auditoría. Los optional trader fields no se normalizan a valores inventados.

La seguridad de thread permite una instancia compartida. No significa que implementaciones custom de `ViesCache` o executor sean seguras; el integrador debe proporcionarlas thread-safe y con timeouts. Callbacks del usuario pueden bloquear sus propios threads, pero no deben ejecutarse bajo el lock interno.
