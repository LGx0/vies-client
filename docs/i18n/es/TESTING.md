# Español (`es`) — Pruebas

[Selector de idioma](../../LANGUAGES.md) · [Original inglés](../../TESTING.md)

> Prevalece la documentación inglesa. `LICENSE` no se traduce. Licencia del proyecto: Apache-2.0.

Los unit tests cubren formatos, requester, resultados/errores, availability, JSON, TTL cache y builder. Los tests locales de HTTP, concurrencia y ciclo de vida usan `HttpServer`, latches y executors controlados; no llaman a VIES.

```bash
./mvnw test
./mvnw clean verify
./mvnw -Dtest=VatFormatTest test
./mvnw -Dtest=ViesClientHttpTest test
```

Se comprueban retry, timeout, malformed response, caché, single-flight, backpressure sync/async, liberación de permisos, executor rechazado, cancelación y carreras de `close()`, incluida consistencia leader/follower. Use barreras/latches, no `Thread.sleep(...)`; toda carrera corregida necesita regresión determinista.

Smoke live: manual y mínimo. Carga: solo mock local, con warm-up, concurrencia fija, throughput y p50/p95/p99. Soak: heap limitado y observación de threads/colas/conexiones. Chaos: lentitud, timeout, JSON roto, fallo de caché, rechazo y shutdown. CI debe ejecutar `./mvnw clean verify` en JDK 21, archivar reportes/artefactos, hacer análisis y scans, y bloquear releases ante fallos.

## Catálogo completo y regresión

Unit: `VatFormatTest` 8 (normalización, separadores, `GR`→`EL`, inválidos, 28 códigos), `ViesRequesterTest` 4, `ViesResponseMappingTest` 11 (GET/POST, placeholder, decisiones/errores, objeto/boolean/fecha estrictos), `ViesErrorTest` 6, `ViesAvailabilityTest` 2, `MiniJsonTest` 4, `TtlCacheTest` 6, `ViesClientBuilderTest` 3.

| IDs | Caso local |
|---|---|
| I-01/I-02 | 503 retry→éxito; cache failure→0 HTTP |
| C-01–C-04 | single-flight, HTTP cap, async backpressure, cancel permit |
| C-05–C-09 | close callback, admission timeout, chain cleanup, cache-close async/sync |
| C-10–C-13 | custom executor, igualdad close, 100 followers, rejection cleanup |
| C-14–C-17 | mixed single-flight, cache recheck, write-close, unique chain |
| C-18–C-22 | queued cancel, callback bloqueado, reverse, sync cap, fatal `Error` |

```bash
./mvnw --batch-mode --no-transfer-progress clean test
./mvnw --batch-mode --no-transfer-progress clean verify
./mvnw -Dtest=VatFormatTest test
./mvnw -Dtest=ViesClientHttpTest test
```

Suite offline/determinista. Live smoke 1–2 llamadas manuales, nunca CI/load. Load solo mock loopback con warm-up, concurrency fija, heap/GC, throughput, p50/p95/p99. Soak horas con heap limitado; chaos inyecta lentitud, timeout, JSON roto, cache down, rejection y shutdown. Todo fix requiere test que falle sin él; latches/barriers, no sleeps como oráculo.

### Casos C-01…C-22

| ID | Expectativa |
|---|---|
| C-01 | 200 async iguales → un HTTP |
| C-02 | HTTP activos bajo límite |
| C-03 | overload → `CLIENT_OVERLOADED` |
| C-04 | cancel libera permit |
| C-05 | close callback sin deadlock |
| C-06 | admission timeout acota espera |
| C-07 | chain ve cleanup |
| C-08/C-09 | cache-close async/sync → closed, 0 HTTP |
| C-10 | custom activo interrumpido, executor abierto |
| C-11 | leader/follower sync iguales |
| C-12 | 100 followers sin permisos extra |
| C-13 | rejection limpia estado |
| C-14 | sync→async un HTTP |
| C-15 | cache recheck evita HTTP |
| C-16 | cache-write close coherente |
| C-17 | unique chain libera permit |
| C-18 | custom queued cancelado |
| C-19 | callback bloqueante fuera de lock |
| C-20 | async→sync un HTTP |
| C-21 | sync pending backpressure |
| C-22 | fatal Error llega a future/handler |

## Unit y concurrencia determinista

Format 8 cubre APIs, case, separators, prefix, null/blank/unknown, representativos y 28; GR→EL. Requester 4 y fail-fast. Mapping 11: GET/POST, Valid, placeholder, Invalid, transient/input, non-object, missing/string boolean, missing/bad/offset date. Nunca ambiguous→Invalid/hora inventada.

Error 6 valida HU/EN/retry, HTTP408/429/5xx/unknown. Availability2, MiniJson4, TTL6, Builder3: types, expiry, pressure, URL, limits, overflow.

Loopback HttpServer. Latches/barriers bloquean cache/executor/handler; second/cancel/close y release. Sleep no oracle; finished latch prueba task. Leader/follower cuenta HTTP/full response. Ambas direcciones. Rejection/cancel seguido de success detecta leak. Followers vs leaders. Fatal Error future+handler.

CI JDK21 batch verify, Surefire/JAR/sources/Javadocs/module, CodeQL/dependency/secret/license. Offline; flaky se repara.

Load mock: warm-up,duration,keys,payload,cache,concurrency,heap/GC,throughput,p50/p95/p99,error. Soak horas map/permits/threads/connections/queue. Chaos DNS/HTTP slow, timeout,429/5xx,bad JSON,Redis down,rejection,interrupt,shutdown. Live 1–2 manual, nunca stress.

Release compile classpath/JPMS, describe-module, demo, links/fences/selectors/licenses. Cada bug test falla sin fix. Expected compara type,VAT,optionals,audit,origin,code. Retry exact count/no permanent.

Cache clock controlado y eventually bounded, no LRU. Builder zero/negative/max, URL, huge Duration. Availability fixture local. Matriz JDK21+nuevos con artefacto fijo. Benchmark threshold previo y perfil igual.

## Matriz de casos e interpretación

I-01 configura dos 503 y éxito y exige tres calls. I-02 hace fallar cache read y exige `CACHE_ERROR` y cero HTTP. C-01 usa 200 followers iguales y un call; C-02 mide máximo activo. C-03/C-21 separan límites async/sync. C-04 verifica cancel permit. C-05 close en callback y C-19 callback bloqueante prueban ausencia de deadlock.

C-06 mide admission con límite superior. C-07 y C-17 encadenan misma/diferente key para asegurar cleanup antes completion. C-08/C-09 bloquean cache read y cierran async/sync. C-10/C-18 cubren task custom active/queued sin cerrar executor. C-11/C-16 comparan leader/follower durante close/write.

C-12 demuestra que followers no gastan slots; C-13 rejection cleanup. C-14/C-20 mixed directions comparten request. C-15 recheck cache evita HTTP. C-22 verifica fatal Error doble canal. Todas usan timeouts defensivos pero la sincronización real es latch/barrier.

Unit test names y IDs se mantienen estables para diagnóstico, pero una refactorización puede reorganizar métodos sin reducir escenarios. Un fallo CI publica Surefire y logs redacted. No se soluciona aumentando sleeps o deshabilitando race tests.

Para load, use distribución de keys realista: muchos duplicados muestran single-flight, keys únicas prueban admission, cache hits prueban hot path. Reporte hardware, JDK flags, warm-up y confidence. No compare números obtenidos con diferentes concurrency/cache. El objetivo de soak es ausencia de crecimiento continuo, no throughput máximo.

Chaos también prueba cierre del mock, conexiones reset, body grande/malformado, date offset, Redis lento y executor que rechaza. Tras cada fault, una solicitud sana comprueba recuperación. Los recursos del test se cierran en `@AfterEach`; no quedan threads/ports.

## Criterios de aceptación

Una suite verde significa 73 casos actuales sin failure/error/skip inesperado y build de binary/sources/Javadoc. El número puede crecer; la cobertura de escenarios importa más que conservar exactamente 73. Ningún test depende de orden global, hora real, red pública o VAT cambiante. Fixtures sintéticos tienen nombres claros y no parecen credenciales reales.

Para cambios de parser, añada body mínimo de éxito y cada forma inválida relevante. Para concurrency, pruebe interleaving exacto antes y después de la línea cambiada. Para shutdown, mida que termina dentro de una cota y que el executor externo sigue usable. Para cache, pruebe read/write exception por separado.

Un test de rendimiento no reemplaza assertions de corrección. Antes de medir se verifica que responses y request count son correctos. Use varias iteraciones, mediana y distribución; descarte warm-up de compilación. El benchmark local debe etiquetarse claramente como loopback, no VIES.

Cuando CI falla solo en una plataforma, conserve logs, JDK/vendor, timing y thread dump. No aumente timeouts sin entender la sincronización. Un race reproducido una vez se convierte en latch determinista. Un leak se confirma con capacidad recuperada o estado observable, no solo ausencia de excepción.
