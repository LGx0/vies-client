# Français (`fr`) — Contribuer

[Sélecteur de langue](../../LANGUAGES.md) · [Original anglais](../../../CONTRIBUTING.md)

> L’anglais prévaut ; seul `LICENSE` original fait foi.

Référencez une issue pour un bug ; discutez d’abord les changements majeurs API/licence/architecture. Vulnérabilités uniquement via [SECURITY.md](SECURITY.md). Requis : JDK 21+, Maven 3.9+, Git. Fork, branche courte depuis `main`, changement ciblé, régression déterministe, `./mvnw --batch-mode --no-transfer-progress clean verify`, puis PR complète.

Règles : Java 21, API petite et typée, aucune dépendance runtime sans accord, jamais convertir `Unavailable` en `Invalid`, état partagé thread-safe et borné, API/concurrence documentées anglais-hongrois, aucune donnée VAT/entreprise/adresse/requester privée. Tests sans réseau public, loopback seulement, aucune charge VIES, races avec latches/barrières.

PR : build vert, comportement documenté, dépendances justifiées, breaking changes visibles, performance reproductible, droits confirmés. L’AI est permise mais l’auteur vérifie/teste tout, contrôle provenance/licence, déclare l’aide importante et n’envoie aucune donnée confidentielle. Contributions sous Apache-2.0 section 5. Respectez [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

## Workflow et qualité

Synchronisez fork, créez une branche courte depuis `main` et des commits focalisés. Avant PR, vérifiez Java/Maven et lancez `./mvnw --batch-mode --no-transfer-progress clean verify`.

API publique petite, immutable et typée. Nouvelle dépendance runtime après discussion. `Unavailable` ne devient jamais `Invalid`. État partagé thread-safe, borné et nettoyé sur erreur/close. API et concurrence complexe reçoivent Javadoc/commentaires EN/HU expliquant invariants et ownership.

Tests sans réseau public, HTTP loopback, races par latches/barrières. Chaque fix a une régression échouant sans lui. Performance indique JDK, warm-up, concurrence, payload, cache, p50/p95/p99. Aucun stress VIES.

Le PR décrit motivation, avant/après, API/config, tests, sécurité/privacy, compatibilité, benchmark. Breaking explicite ; dependency met à jour tiers ; fonction publique met à jour README/docs/localisations. L’auteur AI vérifie origine/licence, divulgue aide et protège les secrets.

Documentation : commandes exécutables, liens relatifs, versions cohérentes, sécurité/licence alignées anglais. Traductions nomment langue/code, sélecteur, autorité anglaise ; `LICENSE`/`NOTICE` sans traduction juridique. Reviews concrètes, respectueuses et orientées correction.

## Pull request et review détaillés

Le titre est concis, la description référence l’issue et la checklist indique tests exécutés. Les commits ne contiennent pas `target/`, configuration IDE, données réelles ou changements générés sans source. Un changement de comportement public inclut exemple avant/après et note de compatibilité. Un changement de concurrence explique locks, ownership, ordre de cleanup et borne mémoire.

Le reviewer vérifie chemins succès, timeout, interrupt, cancellation, rejection et close. Pour cache, il examine clé, TTL, sérialisation, erreur read/write et stampede. Pour retry, classification, maximum, backoff et jitter. Pour une documentation, il exécute commandes importantes et vérifie liens/code fences. Le PR reste draft tant que ces éléments ne sont pas prêts.

Les auteurs répondent au raisonnement, pas à la personne. Une suggestion facultative est distinguée d’un blocker de correction/sécurité. Après modification, ne marquez resolved qu’une fois code et tests poussés. Maintainers peuvent demander de réduire scope, ajouter migration, benchmark ou test de régression. Un merge ne garantit pas une date de release.

La compatibilité source/binaire de l’API publique est prise au sérieux. Renommer type, record component, méthode ou module peut exiger major. Ajouter une méthode abstraite à `ViesCache` peut casser les adaptateurs ; préférez évolution compatible. Les nouveaux codes d’erreur restent stables et documentés dans toutes les couches.

Les contributions de localisation conservent code, commandes et noms API, utilisent une langue naturelle et suivent la structure canonique. Elles ne changent jamais la portée juridique de `LICENSE`/`NOTICE`. Lorsqu’un document anglais évolue, le PR indique les localisations à synchroniser ou ouvre des issues traçables.
