# Español (`es`) — Contribuir

[Selector de idioma](../../LANGUAGES.md) · [Original inglés](../../../CONTRIBUTING.md)

> Prevalece el inglés y el `LICENSE` original.

Referencie issue para bugs; discuta previamente cambios grandes de API/licencia/arquitectura. Seguridad solo por [SECURITY.md](SECURITY.md). Requisitos: JDK 21+, Maven 3.9+, Git. Fork, rama breve desde `main`, cambio enfocado, regresión determinista, `./mvnw --batch-mode --no-transfer-progress clean verify` y PR completa.

Java 21, API pequeña/tipada, sin dependencia runtime no acordada, nunca `Unavailable`→`Invalid`, estado compartido thread-safe/acotado, API y concurrencia documentadas EN/HU, sin datos VAT/empresa/dirección/requester privados. Tests sin red pública, loopback, nunca load a VIES, races con latches/barriers.

PR verde, comportamiento documentado, dependencies justificadas, breaking changes visibles, rendimiento reproducible y derechos confirmados. AI permitida bajo responsabilidad total: revisar/testear todo, comprobar procedencia/licencia, declarar ayuda sustancial, no compartir confidenciales. Contribuciones según Apache-2.0 section 5 y [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

Sincronice fork, branch corto desde main, commits enfocados. Verifique Java/Maven y batch clean verify. API pequeña, immutable/typed; dependencia runtime tras discusión. Unavailable nunca Invalid. Shared state thread-safe/bounded/cleanup. API y concurrency con Javadoc EN/HU de invariants/ownership.

Tests offline/loopback, races con latches/barriers, regression falla sin fix. Performance con JDK, warm-up, concurrency, payload, cache, p50/p95/p99. No stress público.

PR explica motivación, before/after, API/config, tests, security/privacy, compatibility, benchmark. Breaking claro; dependency actualiza notices; feature actualiza docs/localizations. AI: autor responsable, provenance/license, disclosure, no confidential.

Título/issue/checklist; no target/IDE/data. Cambio concurrente explica locks, ownership, cleanup, memory. Reviewer verifica success, timeout, interrupt, cancel, rejection, close; cache key/TTL/schema/errors; retry class/max/backoff/jitter. Draft hasta listo. Feedback concreto, resolve tras cambio.

Compatibilidad source/binary: renames pueden major; método abstracto cache rompe adapters; nuevos error codes estables. Docs comandos ejecutables, links, versions y authority inglesa. Localización conserva code/API y no traduce license legalmente.

## Revisión técnica completa

El reviewer analiza éxito, invalid, input, timeout, network, malformed, cancel, interrupt, rejection y close. En concurrency verifica orden de locks, ownership de permits/tasks, cleanup antes completion y memoria bounded. En cache revisa key con requester, TTL, serialización segura, read/write failure y stampede. En retry revisa clasificación, máximo, crecimiento y jitter.

Cambios de API añaden Javadoc bilingüe, ejemplos y compatibilidad JPMS. Cambios internos no exponen package `internal`. Dependencias runtime nuevas requieren issue y justificación de seguridad/licencia/tamaño; plugins de build también se fijan y documentan. No se aceptan optimizaciones sin medición o que conviertan errores técnicos en decisión.

La descripción del PR incluye cómo reproducir el bug antes, qué test lo captura y por qué la solución no introduce deadlock/leak. Para docs, se ejecutan comandos y link checker. Para traducciones, se comparan secciones/tablas/code blocks con canonical y se cumplen umbrales de contenido sin padding repetido.

Los commits pueden ser rebaseados/squashed según política. Firmar es recomendado para releases. Después de merge, el contributor ayuda a responder regression relacionada, pero no asume SLA. Toda interacción sigue Code of Conduct y Security.

Antes de solicitar review, actualice branch con main y resuelva conflictos sin eliminar cambios ajenos. La checklist confirma tests, docs, changelog y notices. Si el PR toca hot path, incluya medición antes/después y explique trade-offs de CPU, memoria y complejidad. Si toca shutdown/cache/retry, enumere los casos de fallo cubiertos.

No mezcle refactor masivo con bug urgente. Mantenga nombres públicos y códigos estables. Use excepciones solo para fallos de programación/fatales; los resultados esperables siguen la jerarquía. Comentarios bilingües deben explicar por qué existe una barrera o cleanup order, para que una persona nueva pueda mantenerlo.
