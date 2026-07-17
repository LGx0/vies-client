# Español (`es`) — Política de seguridad

[Selector de idioma](../../LANGUAGES.md) · [Original inglés](../../../SECURITY.md)

> Prevalece el inglés; `LICENSE` no se traduce. Licencia del proyecto: Apache-2.0.

La última `1.x` recibe fixes; versiones antiguas caso por caso. No abra issue público ni divulgue exploit antes del arreglo coordinado. Use **Security → Advisories → Report a vulnerability** e incluya versión, entorno, reproducción, impacto, workaround/fix y PoC sin datos reales.

Objetivos, no SLA: acuse en 7 días, evaluación/siguiente paso en 14, corrección/divulgación según severidad. Alcance: injection, bypass TLS/endpoint, fuga, cache poisoning, abuso requester, crecimiento ilimitado, deadlock de cierre o `Invalid` incorrecto por fallo técnico. Disponibilidad/throttling/calidad upstream no son por sí solos vulnerabilidad.

Reporte con version/commit, JDK/OS, config redacted, reproduction, expected/actual, impact/workaround. PoC synthetic y mock; no customer/tax/auth data. Cifrado después de advisory privado.

7/14 son targets no contractuales. Coordine severity, exploitability, affected y disclosure; nada público antes de fix. Scope SSRF/endpoint, TLS, sensitive logs, cache deserialization, resource leaks, false-decision races, limit bypass. Upstream correctamente Unavailable no es client vulnerability.

Después: regression, changelog, advisory, versions, upgrade. Attachments mínimos/checksum, no binary opaque/destructive script. Revoke exposed secrets inmediatamente. Reconocimiento posible, bug bounty no garantizado.

Los maintainers mantienen el reporte privado, reproducen en mock y preparan patch con tests. Si el problema afecta integridad de decisión, la severidad considera que un falso Invalid/Valid puede causar daño fiscal. También se evalúan disponibilidad, confidencialidad, exploit remoto, necesidad de credenciales y alcance multi-tenant.

El release de seguridad se firma, publica checksum y explica mitigación. Los usuarios deben actualizar a última `1.x`; versiones antiguas se corrigen caso por caso. La coordinación puede involucrar proveedor upstream sin revelar datos del reporter. No se promete compatibilidad con configuraciones que deshabiliten TLS o expongan baseUrl a usuarios.
