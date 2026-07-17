# Français (`fr`) — Intégration

`disableCache()` désactive le cache stocké/persistant, mais des appels simultanés avec la même paire TVA + requester peuvent partager une seule requête réseau single-flight. Un appel ultérieur, après sa fin, effectue une nouvelle requête VIES. `consultationNumber` est facultatif et peut être renvoyé par VIES, mais n’est jamais garanti ; sa valeur probante juridique dépend des règles locales. Chargez `MY_EU_VAT_NUMBER` uniquement depuis une source de secrets/configuration de confiance.

[Sélecteur de langue](../../LANGUAGES.md) · [Original anglais](../../INTEGRATION.md)

> L’original anglais prévaut ; `LICENSE` n’est pas traduit. Licence du projet : Apache-2.0.

Créez un `ViesClient` singleton par processus et appelez `close()` à l’arrêt. Le client est thread-safe ; ne créez pas une instance par requête.

```java
var client = ViesClient.builder()
    .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
    .maxConcurrentRequests(32).maxPendingSyncRequests(512)
    .maxPendingAsyncRequests(512).admissionTimeout(Duration.ofSeconds(2))
    .retries(2).build();
```

Traitez `Valid`, `Invalid`, `Unavailable` et `MalformedInput`. `Invalid` et `MalformedInput` ne doivent pas être retentés ; pour `Unavailable`, respectez `error().retryable()`. Contrat HTTP conseillé : 200 pour les réponses métier, 400 pour entrée invalide, 429 pour `CLIENT_OVERLOADED`, 503 pour panne temporaire. Retournez `errorCode`, `messageHu`, `messageEn`, `retryable`.

Spring Boot : exposez le client avec `@Bean(destroyMethod = "close")` et utilisez un `switch` exhaustif dans le contrôleur. Pour un serveur JDK, réutilisez le même client dans tous les handlers.

`defaultRequester(...)` permet à VIES de fournir éventuellement `consultationNumber`, sans garantie. S’il est présent, conservez ce numéro, la date, l’identifiant demandé et le résultat selon vos règles locales d’audit, de preuve et de protection des données.

Implémentez `ViesCache` pour Redis si plusieurs pods partagent les résultats ; la clé inclut VAT et requester et doit avoir une TTL. Les single-flight et sémaphores étant locaux à la JVM, ajoutez une file durable, des consommateurs bornés, un rate limiter distribué et une DLQ. Limitez aussi le nombre de futures retenues par l’appelant.

En production : testez l’arrêt propre, configurez timeouts et limites, surveillez latence/cache/retry/rejets, évitez les VAT complètes dans les logs et ne chargez jamais le service VIES public.

## Contrat complet et exploitation

```java
ViesResponse sync = client.check("FRXX000000000");
CompletableFuture<ViesResponse> async = client.checkAsync("FRXX000000000");
```

| Réponse | HTTP | Retry | Interprétation |
|---|---:|---|---|
| `Valid`/`Invalid` | 200 | non | décision métier |
| `MalformedInput` | 400 | non | entrée à corriger |
| `Unavailable(CLIENT_OVERLOADED)` | 429 | différé | backpressure locale |
| `Unavailable` retryable | 503 | différé | aucune décision |

Spring : `@Bean(destroyMethod="close")`, propriétés pour limites/timeouts, controller exhaustif retournant `errorCode`, `messageHu`, `messageEn`, `retryable`. JDK : client partagé dans `HttpServer`, voir la démo. Annuler un follower ne détruit pas le travail single-flight partagé ; un cache externe bloquant s’exécute dans le worker borné.

Redis `ViesCache` : clé VAT+requester, TTL, sérialisation sûre/versionnée, timeouts courts. Lecture en panne → `CACHE_ERROR`; écriture en panne ne remplace pas le résultat valide. Queue/DLQ : seulement `Unavailable` retryable, nombre borné, backoff+jitter ; acquitter `Invalid`/`MalformedInput`. Pour N pods, `N × maxConcurrentRequests` exige un limiteur distribué par endpoint EU/État membre.

Health : liveness processus, readiness queue/cache, `check-status` parcimonieux, jamais une validation VAT par probe. Mesures : latence, résultat, code, retry, hit, pending/in-flight/reject, sans VAT comme label. Anti-patterns : client/requête, files/futures illimités, boucle retry immédiate, cache sans TTL, `Unavailable`→`Invalid`, charge publique, PII logs, oubli de `close()`. Checklist : shutdown, capacité, audit/privacy, alertes, DLQ, failover cache/limiteur, upgrade/rollback.

## Référence Spring Boot

```java
@Configuration
class ViesConfig {
 @Bean(destroyMethod = "close")
 ViesClient viesClient() { return ViesClient.builder().retries(1).build(); }
}
@RestController
class VatController {
 private final ViesClient vies;
 VatController(ViesClient vies) { this.vies = vies; }
 @GetMapping("/api/vat/{number}")
 ResponseEntity<?> check(@PathVariable String number) {
  return switch (vies.check(number)) {
   case ViesResponse.Valid v -> ResponseEntity.ok(Map.of("valid", true));
   case ViesResponse.Invalid i -> ResponseEntity.ok(Map.of("valid", false));
   case ViesResponse.MalformedInput m -> ResponseEntity.badRequest().body(m.error());
   case ViesResponse.Unavailable u -> ResponseEntity.status(
    "CLIENT_OVERLOADED".equals(u.errorCode()) ? 429 : 503).body(u.error());
  };
 }
}
```

Smoke JDK : `./mvnw -q package`, `java -cp target/classes examples/ViesDemoServer.java`, puis `curl "http://localhost:8085/vat-check?number=FRXX000000000"`.

## Redis, file, audit et shutdown détaillés

Le cache distribué ne stocke que `ViesResponse.Valid`. Le document sérialisé contient version de schéma, VAT normalisée, clé requester, date d’audit, champs trader optionnels et consultation. Il accepte des champs futurs inconnus mais rejette les mauvais types. Évitez la sérialisation native Java pour un contenu partagé non fiable. Observez latence et erreurs Redis et décidez explicitement si sa panne retire la readiness.

Une tâche de file contient identifiant, VAT, requester optionnel, numéro d’essai, heure minimale et correlation ID. Le consumer doit être idempotent. Un résultat final et l’ack sont rendus atomiques par transaction, outbox ou équivalent. Un `Unavailable` retryable reçoit un prochain horaire avec backoff exponentiel et jitter. Après le maximum, DLQ ; une entrée invalide produit immédiatement une erreur permanente.

Lorsqu’il est présent, `consultationNumber` peut aider à documenter la consultation, mais sa valeur probante dépend des règles locales et il ne définit pas à lui seul un archivage légal. L’intégrateur fixe rétention, immutabilité, accès et suppression. Les noms/adresses sont optionnels et les placeholders deviennent absents ; ne construisez pas une règle critique supposant leur présence.

Un `ExecutorService` fourni appartient à l’application. Le client annule ses handles actifs/queued à `close()` mais ne ferme pas l’executor. Arrêt recommandé : stopper ingress, suspendre consumers, attendre une grace period bornée, fermer le client, puis les ressources applicatives. `CLIENT_CLOSED` n’est pas une décision VAT. Plusieurs appels close sont sérialisés et les callbacks ne tiennent pas le lifecycle lock.

Mesures : compteurs par résultat/code, histogrammes total/cache/HTTP, gauges pending/in-flight, retry/reject, queue age et DLQ. Aucun identifiant fiscal comme label. Les logs gardent correlation et code, les payloads debug sont redacted et temporaires. Alertes distinctes pour timeout, overload, cache failure, backlog et hausse anormale des Invalid.

La capacité globale est `pods × limite locale` avant limiteur. Dix pods à 32 pourraient produire 320 appels. Placez donc un rate limiter partagé, éventuellement par État membre. Le scaling se fonde sur âge de queue, budget global et santé upstream, pas seulement CPU. Testez failover cache/limiter, idempotence, replay DLQ et rollback avant le trafic réel.

## Exemple de mapping d’erreur et requester

```java
ViesResponse result = client.check("FRXX000000000");
result.error().ifPresent(e -> audit(e.code(), e.retryable(), e.messageFr(), e.messageEn()));
```

La bibliothèque expose réellement `messageHu()` et `messageEn()` ; une couche française peut traduire depuis le code stable, sans utiliser le texte comme clé. Le requester par défaut s’applique à toutes les demandes ; une surcharge explicite doit rester cohérente dans la clé de cache et le single-flight. Une VAT requester invalide échoue localement avant réseau. La consultation peut être absente même sur `Valid`, donc le stockage utilise Optional/null explicite.

Dans une API publique, validez taille et encodage avant d’appeler le client, appliquez authentification/autorisation et rate limit par tenant. Les limites internes protègent VIES et la JVM, pas votre endpoint contre abus. Corrélez requête API, job de queue et audit sans exposer la VAT dans l’URL des logs. Définissez une politique de timeout du controller compatible avec celle du client : un reverse proxy plus court peut abandonner la réponse tandis que le travail partagé continue.

Pour un traitement transactionnel, séparez état `PENDING`, `VALID`, `INVALID`, `RETRYABLE_ERROR`, `PERMANENT_ERROR` et `DEAD_LETTER`. Ne remplacez jamais une décision précédente par un nouvel `Unavailable` sans conserver l’historique et l’horodatage. Un contrôle périodique utilise deduplication et priorité, pas des millions de timers/futures dans la JVM.
