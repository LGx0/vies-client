# Français (`fr`) — Conception technique

[Sélecteur de langue](../../LANGUAGES.md) · [Original anglais](../../TECHNICAL.md)

> En cas de divergence, l’original anglais prévaut. `LICENSE` reste inchangé et fait foi. Licence du projet : Apache-2.0.

Le module JPMS `vies.client` exporte `vies.client` et encapsule les composants internes. Il n’a aucune dépendance d’exécution hors JDK. `ViesResponse` est scellé : `Valid`, `Invalid`, `Unavailable`, `MalformedInput`. `ViesError` fournit code stable, textes hongrois/anglais et décision de retry.

## Cycle d’une requête

1. Normalisation et validation locale du format national.
2. Lecture du cache.
3. Regroupement single-flight par couple VAT/requester dans une JVM.
4. Admission par limites pending et sémaphore réseau.
5. HTTP REST via un `HttpClient` réutilisé, timeout et retry borné.
6. Validation stricte du JSON ; champs absents ou invalides → `MALFORMED_RESPONSE`.
7. Cache TTL des seuls résultats `Valid` fiables et nettoyage avant completion.

Les opérations async utilisent par défaut des threads virtuels. Les leaders uniques consomment un permis pending ; leurs followers n’en consomment pas. Les appels sync, async et HTTP ont des limites distinctes. Tous les chemins libèrent permis et état in-flight, y compris rejet, annulation, erreur fatale et shutdown.

Seules les erreurs temporaires sont retentées avec backoff et jitter. Une erreur de lecture du cache produit `CACHE_ERROR` sans stampede ; une erreur d’écriture n’efface pas une réponse valide. `close()` bloque les nouvelles admissions, termine leaders/followers de façon cohérente, interrompt les tâches possédées et ne ferme pas un executor externe.

À grande échelle : API → file persistante → workers bornés → cache partagé + limiteur distribué → VIES. Mesurez durée, résultat, code d’erreur, retry, cache et rejet sans utiliser les VAT comme labels. TLS, minimisation des logs, validation des entrées et scans du build font partie du modèle de sécurité. Les benchmarks locaux ne constituent pas un SLA VIES.

## Invariants détaillés et limites connues

| Type | Décision | Retry | Cache |
|---|---|---|---|
| `Valid` | confirmée | non | oui |
| `Invalid` | non confirmée | non | non |
| `Unavailable` | aucune | selon code | non |
| `MalformedInput` | entrée invalide | non | non |

`Valid` porte VAT, nom/adresse/consultation optionnels, date d’audit et origine cache ; `---` devient vide. Les codes inconnus sont conservés, sans retry par défaut. Après un premier miss, une nouvelle vérification leadership/cache évite un HTTP si un pair vient d’écrire. `ConcurrentHashMap` stocke in-flight ; sémaphores séparés bornent sync pending, leaders async et HTTP. Cleanup précède `complete` afin que les callbacks chaînés voient la capacité. Un `Error` fatal complète exceptionnellement puis est relancé.

Retry : timeouts/busy/MS/network et HTTP 408/429/5xx classifiés ; input/close/surcharge locale jamais aveuglément. Backoff exponentiel+jitter ; overflow de Duration validé. Le TTL cache local est borné mais limite approximative/éventuelle sous concurrence, sans garantie LRU. Read failure empêche stampede ; write failure ne change pas un Valid ; seul `Valid` est stocké.

`close()` sérialise les fermeurs, stoppe admission, donne `CLIENT_CLOSED` identique aux leaders/followers, interrompt tasks propres actives/queued et laisse l’executor externe ouvert. Callbacks hors lifecycle lock.

Médianes loopback JDK 21, pas SLA : cache hit 8,91 M/s, rejet format 9,02 M/s, HTTP séquentiel 4 044/s. Mesurez warm-up/p50/p95/p99 dans votre environnement. Production HTTPS, segments normalisés, aucun secret/PII logs, cache sensible, réponses non fiables validées strictement.

## Codes d’erreur publics

| Famille | Traitement |
|---|---|
| `SERVICE_UNAVAILABLE`, `MS_UNAVAILABLE`, `SERVER_BUSY`, `TIMEOUT` | upstream temporaire, retry borné |
| `GLOBAL_MAX_CONCURRENT_REQ`, `MS_MAX_CONCURRENT_REQ`, HTTP `408`/`429`/`5xx` | throttling/serveur, retry différé |
| `NETWORK_ERROR` | DNS/TCP/TLS, backoff |
| `CLIENT_OVERLOADED` | admission locale pleine, requeue différé |
| `CLIENT_CLOSED`, `INTERRUPTED` | lifecycle/annulation, aucune décision |
| `INVALID_INPUT`, `INVALID_VAT_FORMAT`, `INVALID_REQUESTER_INFO` | entrée permanente |
| `IP_BLOCKED`, `VAT_BLOCKED` | politique VIES, examen manuel |
| `MALFORMED_RESPONSE` | schéma/date/boolean invalide |
| `CACHE_ERROR` | cache en panne, aucun stampede |
| `INTERNAL_ERROR`, inconnu | aucune décision, pas de retry par défaut |

## Frontières de module et flux interne

L’API exportée comprend client/builder, requester, hiérarchie de résultats, erreurs, disponibilité et extension cache. Parser JSON et cache TTL restent dans le package interne non exporté, ce qui réduit les engagements binaires. Le JAR release ne requiert que `java.base` et `java.net.http`; JUnit et serveur local restent des outils de test.

Après normalisation, la clé stable combine VAT cible et requester. Un hit cache retourne immédiatement une copie marquée. Sur miss, les appels se disputent l’entrée in-flight ; un seul leader relit le cache puis effectue éventuellement HTTP, les followers attendent le même résultat via leur propre vue Future. La seconde lecture ferme la race où un autre thread ou nœud écrit entre premier miss et leadership.

Trois admissions sont séparées : pending sync, leaders async uniques et appels HTTP réels. Les followers identiques ne consomment pas un permis async supplémentaire. Chaque acquire a exactement un release sur succès, rejection, cancellation, interrupt, erreur parse, `Error` fatal ou close. In-flight et permits sont nettoyés avant `complete`, afin qu’un callback chaîné voie immédiatement la capacité libérée.

## HTTP, validation, retry et cache

Le `HttpClient` est réutilisé ; connect timeout et request timeout ont des rôles distincts. Les segments URL proviennent uniquement de valeurs normalisées. Les statuts non-2xx sont classifiés ; un 2xx reste non fiable : root objet, validity vrai boolean, date présente et parseable. Un boolean absent/string et une date invalide deviennent `MALFORMED_RESPONSE`, jamais `Invalid`. Les offsets sont convertis en UTC sans heure locale inventée.

Le retry est borné au nombre configuré, avec délai exponentiel et jitter. 408, 429, 5xx, timeout/busy/network peuvent être retryables ; input, closed et overload local ne bouclent pas. Les Durations positives sont validées contre overflow nanos/millis et l’interruption est respectée.

Le cache local est borné avec expiration, mais son éviction sous concurrence est approximative/eventuelle, pas une LRU stricte. Seuls les `Valid` sont stockés. Une lecture externe en panne donne `CACHE_ERROR` sans bypass massif ; une écriture en panne conserve le résultat. La map in-flight est bornée indirectement par pending, mais l’hôte doit aussi borner les Futures qu’il conserve.

## Shutdown, sécurité et performance

`close()` sérialise les callers, bloque l’admission, donne le même `CLIENT_CLOSED` aux leaders/followers, interrompt les tâches propres actives/queued et laisse l’executor externe ouvert. Les callbacks s’exécutent hors lifecycle lock. Les races close/cache/write et les deux directions sync/async sont des invariants testés.

Les nombres loopback montrent des plafonds locaux, pas un SLA EU. Comparez uniquement même JDK, hardware, payload, cache et concurrence ; documentez warm-up, p50/p95/p99, GC et erreurs. En production utilisez HTTPS, validation normale des certificats, URL contrôlée, logs minimisés, cache traité comme données sensibles, build scanné et releases signées avec checksums.

Le modèle ne fournit pas de limiteur distribué, de queue persistante, de scheduler de retry, de chiffrement de cache ni de stockage d’audit : ce sont des responsabilités explicites de l’hôte. Cette frontière évite des dépendances runtime et laisse le choix d’infrastructure. L’API d’extension cache est volontairement petite pour permettre Redis, base ou cache propriétaire sans imposer un client particulier.
