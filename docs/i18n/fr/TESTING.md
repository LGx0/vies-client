# Français (`fr`) — Tests

[Sélecteur de langue](../../LANGUAGES.md) · [Original anglais](../../TESTING.md)

> La documentation anglaise prévaut. `LICENSE` n’est pas traduit. Licence du projet : Apache-2.0.

La suite déterministe actuelle contient **73 tests** : 44 unitaires et 29 tests locaux HTTP/intégration/concurrence.

Les tests unitaires isolent format, requester, modèle de réponse/erreur, disponibilité, JSON, cache TTL et builder. Les tests locaux HTTP/concurrence/lifecycle utilisent un `HttpServer`, des latches et des executors contrôlés ; ils n’appellent pas VIES.

```bash
./mvnw test
./mvnw clean verify
./mvnw -Dtest=VatFormatTest test
./mvnw -Dtest=ViesClientHttpTest test
```

La suite couvre retry, timeout, réponse mal formée, cache, single-flight, backpressure sync/async, libération de permis, executor rejeté, annulation et courses avec `close()`, notamment la cohérence leader/follower. Les tests concurrents doivent employer barrières/latches plutôt que `Thread.sleep(...)`. Toute race corrigée reçoit un test déterministe.

Un smoke test live est manuel et très limité. Les tests de charge visent uniquement un mock contrôlé, avec warm-up, concurrence fixe, débit et p50/p95/p99. Les tests soak suivent heap, threads, files et connexions ; les tests chaos injectent lenteur, timeout, JSON corrompu, cache indisponible, rejet et shutdown.

Le CI exécute `./mvnw clean verify` sur JDK 21, archive rapports et artefacts, lance analyse statique et scans de secrets/dépendances, puis bloque la release au moindre échec. Les performances ne sont comparables qu’avec le même profil.

## Catalogue complet et règle de régression

Unitaires : `VatFormatTest` 8 (normalisation, séparateurs, `GR`→`EL`, invalides, 28 codes), `ViesRequesterTest` 4, `ViesResponseMappingTest` 11 (GET/POST, placeholder, décisions/erreurs, objet/boolean/date stricts), `ViesErrorTest` 6, `ViesAvailabilityTest` 2, `MiniJsonTest` 4, `TtlCacheTest` 6, `ViesClientBuilderTest` 3.

| IDs | Cas local intégration/concurrence |
|---|---|
| I-01/I-02 | retry 503→succès ; cache failure→zéro HTTP |
| C-01–C-04 | single-flight, limite HTTP, backpressure async, permit après cancel |
| C-05–C-09 | close callback, admission timeout, cleanup chain, races cache-close async/sync |
| C-10–C-13 | executor custom, résultats close identiques, 100 followers, rejection cleanup |
| C-14–C-17 | single-flight mixte, cache recheck, write-close, unique chain |
| C-18–C-22 | queued cancel, callback bloquant, sens inverse, limite sync, `Error` fatal |

```bash
./mvnw --batch-mode --no-transfer-progress clean test
./mvnw --batch-mode --no-transfer-progress clean verify
./mvnw -Dtest=VatFormatTest test
./mvnw -Dtest=ViesClientHttpTest test
```

Suite standard offline et déterministe. Live smoke : 1–2 appels manuels, jamais CI/charge. Load : mock loopback, warm-up, concurrence fixe, heap/GC, débit, p50/p95/p99. Soak : plusieurs heures, heap borné. Chaos : lenteur, timeout, JSON corrompu, cache down, executor rejection, shutdown. Chaque bug corrigé reçoit un test qui échoue sans fix ; latches/barrières, pas de sleeps fixes comme oracle.

### Cas déterministes C-01 à C-22

| ID | Attente |
|---|---|
| C-01 | 200 appels async identiques → un HTTP |
| C-02 | HTTP actifs sous la limite |
| C-03 | dépassement pending → `CLIENT_OVERLOADED` |
| C-04 | cancel restitue le permis |
| C-05 | close depuis callback sans deadlock |
| C-06 | admission timeout borne l’attente |
| C-07 | chaîne identique voit cleanup avant completion |
| C-08/C-09 | race cache/close async puis sync → closed, zéro HTTP |
| C-10 | tâche custom active interrompue, executor ouvert |
| C-11 | leader/follower sync même résultat de fermeture |
| C-12 | 100 followers identiques sans 100 permis |
| C-13 | rejection nettoie permit/in-flight |
| C-14 | leader sync + follower async → un HTTP |
| C-15 | second cache check supprime HTTP stale-miss |
| C-16 | cache-write/close cohérent leader/follower |
| C-17 | chaîne unique restitue pending permit |
| C-18 | tâche custom queued réellement annulée |
| C-19 | callback utilisateur bloquant hors lifecycle lock |
| C-20 | leader async + follower sync → un HTTP |
| C-21 | limite sync fournit backpressure bornée |
| C-22 | `Error` fatal atteint future et uncaught handler |

## Détails unitaires et méthode concurrente

Les huit tests de format couvrent deux signatures publiques, casse, séparateurs, conflit de préfixe, null/vide, codes inconnus, formes représentatives et les 28 codes supportés ; `GR` est canonisé en `EL`. Les quatre tests requester appliquent les mêmes règles à la VAT propre et garantissent un fail-fast sans réseau.

Les onze tests de mapping utilisent JSON GET/POST, Valid complet, placeholders, Invalid autoritatif, erreurs transitoires/input, root non objet, boolean absent ou string, date absente/invalide et timestamp avec offset. L’invariant central interdit qu’une réponse ambiguë devienne `Invalid` et interdit d’inventer l’heure locale. Les six tests erreur vérifient texte HU/EN et retry de tous les codes, HTTP 408/429/5xx et inconnus. Availability, MiniJson, TTL cache et builder couvrent types stricts, expiration, pression concurrente, URL, limites et Duration overflow.

Le `HttpServer` écoute seulement loopback. Latches et barriers arrêtent précisément cache, executor ou handler ; le test déclenche second caller, cancel ou close puis libère le point. `Thread.sleep` n’est pas un oracle. Un latch de fin d’executor prouve que la tâche réelle est terminée. Les tests leaders/followers comptent HTTP et comparent la variante complète ; sync→async et async→sync sont distincts.

Rejection et cancellation sont suivies d’un appel réussi pour détecter permit/map leaks. Les followers identiques sont séparés des leaders uniques dans les tests de pending. Un fatal `Error` doit atteindre à la fois Future exceptionnel et uncaught handler. Les tests close couvrent cache read, cache write, callback bloquant, tâche active/queued et callers concurrents.

## CI et campagnes non standard

CI fraîche JDK 21 exécute `./mvnw --batch-mode --no-transfer-progress clean verify`, archive Surefire, JAR/sources/Javadocs et module description. CodeQL, dependency review, secret scan et licence complètent. Aucun réseau public. Un flaky test est rendu déterministe, pas masqué par retry.

Load uniquement contre mock : warm-up, durée, clés, payload, cache, concurrence, heap/GC, débit, p50/p95/p99 et erreurs documentés. Soak plusieurs heures avec heap borné observe map, permits, threads, connexions et queue. Chaos injecte DNS/HTTP lent, timeout, 429/5xx, JSON corrompu, Redis down, executor rejection, interrupt et shutdown. Live smoke manuel limité à quelques appels ; jamais de benchmark/stress VIES.

Avant release, compilez le JAR dans un projet classpath et JPMS vide, vérifiez `jar --describe-module`, démarrez la démo et validez liens, code fences, sélecteurs et licences. Chaque bug reçoit un test qui échoue sans correction et reste vert en exécutions répétées.

Les résultats attendus doivent comparer type scellé, VAT normalisée, optionnels, audit timestamp, origine cache et code d’erreur, pas seulement un boolean. Les tests de retry vérifient nombre exact de requêtes et absence de retry pour les erreurs permanentes. Les tests timeout utilisent un handler contrôlé et une borne généreuse indépendante de la vitesse de la machine.

La pression cache insère plusieurs clés concurrentes, avance une horloge contrôlée et vérifie expiration ainsi qu’une taille finalement bornée sans exiger un ordre LRU non promis. Les tests builder couvrent zéro, négatif, maximum, schéma URL et Duration énorme. Les tests availability valident un body complet et un body malformé sans joindre l’EU.

Une matrice de release peut compiler sur le JDK minimal 21 et tester des JDK plus récents, tout en produisant les artefacts avec une version déterminée. Les rapports conservent versions Maven/plugins et dependency tree. Une variation de benchmark ne bloque un release que si le profil et le seuil ont été définis avant mesure ; sinon elle déclenche investigation, pas une affirmation absolue.
