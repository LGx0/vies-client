# Español (`es`) — Publicación en GitHub

[Selector de idioma](../../LANGUAGES.md) · [Original inglés](../../GITHUB_SETUP.md)

> Prevalece el inglés; `LICENSE` queda sin traducir y vinculante.

Antes: confirme derechos, escanee directorio/historial Git, elimine datos cliente/requester, tokens, `.env` y archivos de máquina, revise Apache-2.0 `LICENSE`/`NOTICE` y ejecute `./mvnw --batch-mode --no-transfer-progress clean verify`.

Cree repo público `vies-client`, rama `main`, sin duplicados generados. Tras sustituir OWNER y revisar staged diff: `git init -b main`, add/commit, `gh repo create OWNER/vies-client --public --source . --remote origin --push`.

Active Issues, Discussions, PR+approval, checks `CI`/`CodeQL`/dependencies, resolución, sin force-push/delete, private vulnerability, Dependabot y secret scanning. Sponsor usa `.github/FUNDING.yml` y URL verificada (Buy Me a Coffee), sin SLA/gobernanza.

Después actualice `pom.xml`, badges, Security, releases y funding. Solo con CI/CodeQL verdes cree `v1.2.0` firmado y publique JAR, sources, Javadocs y SHA-256.

No genere README/license/gitignore duplicados. Antes de add: status, secret scan, excluir target/IDE/.env/archive/token/customer. Confirme owner, Public y main.

Active Issues, Discussions, delete merged branches, private vulnerability, Dependabot, secret/push protection. Wiki off sin mantenimiento. CodeQL/dependency con permisos mínimos.

Ruleset: PR, approval, dismiss stale, resolve, CI/CodeQL/dependency; no force/delete, admin bypass emergency. Checks tras primer workflow. Labels bug, enhancement, docs, localization, security, dependencies, performance, concurrency, breaking, good-first, help, triage. Topics java/java21/vies/vat/eu/rest/virtual/single-flight/jpms/zero-dependency. Templates sin data real.

Funding solo Buy Me a Coffee verificado, sin SLA. Después del push POM/SCM/developer, badges, advisory y docs con owner real. Actions pinned, permisos limitados, no secrets en forks/logs; environment approval para publish. Configure notifications, backup repository/packages/releases y protección signing keys. Pruebe templates como usuario externo.

El primer push se revisa como release: historia y staged diff sin archivos machine, authorship confirmado y build verde. Tras la primera ejecución, configure required checks antes de aceptar PR externos. Documente quién administra secrets, rulesets, packages, releases y vulnerability reports, con dos factores y mínima cantidad de owners.

La descripción explica cliente Java 21 VIES no oficial. Añada website de documentación y social preview sin logotipos que sugieran aprobación UE. Preserve repositorio puede activarse después del primer release estable. Los nombres de workflows deben ser estables para no romper rulesets.

Pruebe un fork externo: template, permisos, CI sin secrets y review flow. Configure branch deletion automática pero conserve tags. Las releases adjuntan checksum y provenance. Un backup periódico de Git refs, issues críticos, release assets y configuración permite recuperación ante error de cuenta o plataforma.

Revise también LICENSE/NOTICE visibles en la raíz, language selector, enlaces relativos y badges sin owner placeholder. Active la pestaña Security y confirme que el formulario privado funciona antes de anunciar el proyecto. La URL de donación se prueba en sesión no autenticada y nunca se duplica en fuentes no controladas.
