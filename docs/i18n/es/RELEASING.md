# Español (`es`) — Publicaciones

[Selector de idioma](../../LANGUAGES.md) · [Original inglés](../../RELEASING.md)

> Manda el original inglés. `LICENSE` no se traduce y es vinculante.

Publique desde un commit limpio y verificado con JDK 21, Maven y accesos seguros. Use SemVer: MAJOR incompatible, MINOR función compatible, PATCH corrección compatible.

```bash
./mvnw clean verify
./mvnw package
jar --describe-module --file target/vies-client-1.2.0.jar
```

Revise versión del POM, changelog, ejemplos, enlaces de idiomas, Javadocs/fuentes, licencia/seguridad, ausencia de secretos y repetición de tests concurrentes. VIES live solo como smoke mínimo.

Cree tag firmado/anotado `vX.Y.Z` y GitHub Release. Notas: novedades, API/config, fixes, migración, límites y checksums. Adjunte JAR, sources y Javadocs del mismo CI. Maven Central requiere metadatos, fuentes/Javadocs, firma y secretos; GitHub Packages requiere configuración del consumidor. Después pruebe en proyecto vacío, verifique índices y documentación. Nunca sobrescriba silenciosamente: publique un PATCH.

Checklist: Git limpio, Apache/NOTICE/third-party, scans, links, no secrets, concurrency repetida. Binary/sources/Javadoc/SHA del mismo CI; tags/artifacts immutable. Notes incluyen breaking, config, security, limits, upgrade/rollback. Central requiere namespace/POM/signing; Packages config client. Pruebe classpath y JPMS vacío. Release solo CI/CodeQL verde y live smoke mínimo.

Semantic Versioning distingue PATCH compatible, MINOR funcionalidad compatible y MAJOR cambio incompatible. Antes de tag, actualice POM, changelog, ejemplos, traducciones y matriz de soporte. Cree tag anotado/firmado `vX.Y.Z`; nunca reutilice una versión publicada.

Después, verifique descarga, checksum, module descriptor y dependencia desde un proyecto limpio. Monitorice advisories e issues y conserve rollback. Si un artefacto es defectuoso, márquelo y publique nueva versión, no lo reemplace silenciosamente. Credentials de firma/publicación son secrets CI con mínimo acceso y rotación.
