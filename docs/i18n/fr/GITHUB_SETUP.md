# Français (`fr`) — Publication GitHub

[Sélecteur de langue](../../LANGUAGES.md) · [Original anglais](../../GITHUB_SETUP.md)

> L’anglais prévaut ; `LICENSE` reste non traduit et contraignant.

Avant publication, confirmez les droits, scannez dossier/historique Git, supprimez données client/requester, tokens, `.env` et fichiers machine, vérifiez Apache-2.0 `LICENSE`/`NOTICE`, puis `./mvnw --batch-mode --no-transfer-progress clean verify`.

Créez le dépôt public `vies-client`, branche `main`, sans README/licence/ignore générés en double. Après remplacement de OWNER et revue du staged diff : `git init -b main`, ajout/commit, `gh repo create OWNER/vies-client --public --source . --remote origin --push`.

Activez Issues, Discussions, PR+approbation, checks `CI`/`CodeQL`/dependencies, résolution des conversations, interdiction force-push/suppression, private vulnerability reporting, Dependabot et secret scanning. Le bouton Sponsor vient de `.github/FUNDING.yml` avec une URL vérifiée (p. ex. Buy Me a Coffee), sans SLA ni droit de gouvernance.

Après le push, mettez à jour `pom.xml`, badges, Security, release et funding. Créez `v1.2.0` signé uniquement après CI/CodeQL verts ; publiez JAR, sources, Javadocs et SHA-256.

## Réglages détaillés

Ne générez pas README/licence/gitignore sur GitHub. Avant `git add --all`, contrôlez status, secrets et exclusion de `target/`, IDE, `.env`, archives, tokens et données client. Confirmez owner, nom, Public et `main`.

Activez Issues, Discussions, suppression des branches fusionnées, private vulnerability, Dependabot, secret scanning et push protection. Wiki reste désactivé sans mainteneur. CodeQL/Dependency Review utilisent permissions minimales.

Le ruleset exige PR, approbation, invalidation après commit, conversations résolues, checks CI/CodeQL/dependency ; force-push et suppression interdits. Bypass admin seulement urgence. Les check-names apparaissent après le premier workflow.

Labels : bug, enhancement, documentation, localization, security, dependencies, performance, concurrency, breaking-change, good-first-issue, help-wanted, triage. Topics : java, java21, vies, vat, eu, rest-client, virtual-threads, single-flight, jpms, zero-dependency. Templates sans vraies données fiscales.

Funding contient uniquement l’URL Buy Me a Coffee vérifiée, sans SLA/gouvernance. Après push, remplacez POM/SCM/developer, badges, advisory et docs par le vrai owner. Vérifiez description, topics, social preview, backups et clés de signature avant le tag.

Les workflows doivent épingler des versions d’actions fiables, limiter `permissions`, ne jamais imprimer de secret et séparer validation PR de publication tag. Les forks non fiables ne reçoivent pas les credentials de release. Environments GitHub peuvent exiger une approbation avant Maven Central ou Release. Conservez les checksums et provenance avec les artefacts.

Configurez les notifications pour advisories, Dependabot, échecs main et releases. Testez le template de bug, le lien Security et Discussions depuis un compte externe. Vérifiez que le bouton Sponsor pointe au compte du maintainer. Une sauvegarde périodique du repository et de la configuration réduit la dépendance à une seule interface.

Le premier push est relu comme une release : aucun fichier machine, historique propre, auteurs/licences confirmés, build vert. Après apparition des checks, appliquez le ruleset avant d’accepter des contributions. Documentez qui peut modifier secrets, rulesets, releases et packages, et révoquez les accès inutiles.
