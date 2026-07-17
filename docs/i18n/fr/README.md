# Français (`fr`) — vies-client — vérificateur de numéro de TVA

`disableCache()` désactive le cache stocké/persistant, mais des appels simultanés avec la même paire TVA + requester peuvent partager une seule requête réseau single-flight. Un appel ultérieur, après sa fin, effectue une nouvelle requête VIES. `consultationNumber` est facultatif et peut être renvoyé par VIES, mais n’est jamais garanti ; sa valeur probante juridique dépend des règles locales. Chargez `MY_EU_VAT_NUMBER` uniquement depuis une source de secrets/configuration de confiance.

**Termes de recherche:** vérification de numéro de TVA, validateur de numéro de TVA, validation TVA UE, vérification d’identifiant fiscal, client Java VIES; `VAT checker`, `VAT number validator`, `EU VAT validation`, `tax ID checker`.

Ce projet n’est pas un calculateur fiscal général, mais un client de validation des identifiants TVA de l’UE via VIES.

[Sélecteur de langue](../../LANGUAGES.md) · [Original anglais](../../../README.md)

> Cette traduction facilite l’accès au projet. En cas de divergence technique ou juridique, l’original anglais prévaut. Le fichier `LICENSE` n’est pas traduit : seul son texte anglais fait foi.

`vies-client` est un client Java 21+ thread-safe, sans dépendance d’exécution, pour l’API REST VIES de la Commission européenne. Il s’intègre à Spring Boot, Quarkus, Micronaut ou au JDK seul via `java.net.http.HttpClient`.

## Documentation

- [Installation](INSTALLATION.md) · [Intégration](INTEGRATION.md) · [Conception technique](TECHNICAL.md)
- [Tests](TESTING.md) · [Open source et licence](OPEN_SOURCE.md) · [Publication](RELEASING.md)

## Démarrage rapide

```bash
./mvnw clean verify
./mvnw install
```

```xml
<dependency><groupId>vies.client</groupId><artifactId>vies-client</artifactId><version>1.2.0</version></dependency>
```

```java
try (var vies = ViesClient.builder().retries(1).build()) {
    ViesResponse response = vies.check("FRXX000000000");
}
```

Créez une instance partagée par application et fermez-la à l’arrêt. `check(...)` est synchrone ; `checkAsync(...)` renvoie `CompletableFuture<ViesResponse>`. Les résultats sont `Valid`, `Invalid`, `Unavailable` ou `MalformedInput`. Les erreurs exposent un code stable, des messages hongrois/anglais et `retryable`.

Pour des millions de travaux, ajoutez une file persistante partitionnée, un cache partagé, un rate limiter distribué et des workers horizontaux. Le single-flight, les limites de concurrence, la backpressure, les timeouts, les retries avec jitter et les threads virtuels protègent un processus, mais n’augmentent pas la capacité de VIES.

Les contributions et signalements sont bienvenus. Le lien Sponsor/café du README principal permet un soutien volontaire, sans droit particulier ni modification de la licence Apache-2.0.

## Build, JPMS et exemple complet

Coordonnées Maven : `vies.client:vies-client:1.2.0`. Le module JPMS `vies.client` exporte `vies.client`, requiert `java.net.http` et encapsule `vies.client.internal`. En module : `module my.api { requires vies.client; }`; rien à déclarer en classpath.

```java
try (var vies = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
        .retries(1).build()) {
    switch (vies.check("DE 000 000 000")) {
        case ViesResponse.Valid v -> System.out.println(v.traderName().orElse("-"));
        case ViesResponse.Invalid i -> System.out.println("Non valide");
        case ViesResponse.Unavailable u -> System.out.println(u.errorCode());
        case ViesResponse.MalformedInput m -> System.out.println(m.reason());
    }
}
```

Le requester est votre propre numéro TVA UE ; VIES peut fournir un `consultationNumber` facultatif, jamais garanti, dont la valeur probante dépend des règles locales. Cycle : normalisation (`GR`→`EL`), cache, single-flight JVM VAT/requester, admission pending/réseau, `HttpClient` réutilisé avec timeout/retry, validation stricte boolean/date d’audit, cache uniquement `Valid`.

| Résultat | HTTP | Retry | Cache | Sens |
|---|---:|---|---|---|
| `Valid` | 200 | non | oui | VIES confirme valide |
| `Invalid` | 200 | non | non | VIES ne confirme pas |
| `Unavailable` | 503/429 | selon code | non | aucune décision |
| `MalformedInput` | 400 | non | non | corriger l’entrée |

## Configuration du builder

| Réglage | Défaut | Fonction |
|---|---:|---|
| `baseUrl(String)` | VIES officiel | mock/endpoint contrôlé |
| `connectTimeout(Duration)` | 5 s | connexion TCP/TLS |
| `requestTimeout(Duration)` | 8 s | requête complète |
| `admissionTimeout(Duration)` | 2 s | attente place réseau |
| `defaultRequester(...)` | aucun | consultation ID |
| `retries(int)` | 0 | 0–5, erreurs temporaires |
| `retryDelay(Duration)` | 400 ms | backoff exponentiel+jitter |
| `maxConcurrentRequests(int)` | 32 | HTTP réels simultanés |
| `maxPendingSyncRequests(int)` | 512 | mémoire sync |
| `maxPendingAsyncRequests(int)` | 512 | mémoire async |
| `cacheTtl(Duration)` | 24 h | TTL des Valid |
| `cacheMaxEntries(int)` | 10 000 | limite cache local |
| `cache(ViesCache)` | intégré | Redis/externe |
| `disableCache()` | — | Pas de cache stocké ; les appels simultanés identiques peuvent partager un single-flight |
| `userAgent(String)` | identifiant module | identification EU |
| `executor(ExecutorService)` | virtual threads | cycle externe à l’appelant |

Cache et single-flight sont locaux à la JVM. `Unavailable` n’est jamais `Invalid`. À l’échelle de millions : file durable, lots/workers bornés, Redis, limiteur global, DLQ et retries contrôlés ; jamais de charge sur VIES public. Les erreurs ont `code`, `messageHu`, `messageEn`, `retryable`; ne loguez ni VAT complet, nom, adresse ou requester. Build `./mvnw clean verify`, démo `examples/ViesDemoServer.java`, Apache-2.0, sécurité privée, don volontaire sans SLA.

## Déploiement Spring, cache partagé et production

Dans Spring Boot, créez exactement un bean singleton et liez sa fermeture au contexte. Le controller traite exhaustivement les quatre variantes. `Valid` et `Invalid` sont des décisions métier HTTP 200, `MalformedInput` devient 400, `CLIENT_OVERLOADED` 429 et une indisponibilité temporaire 503. Le code machine reste stable ; les deux messages sont destinés aux interfaces et au support, pas à la logique métier.

```java
@Bean(destroyMethod = "close")
ViesClient viesClient() {
 return ViesClient.builder()
   .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
   .connectTimeout(Duration.ofSeconds(5))
   .requestTimeout(Duration.ofSeconds(8))
   .admissionTimeout(Duration.ofSeconds(2))
   .maxConcurrentRequests(32)
   .maxPendingSyncRequests(512)
   .maxPendingAsyncRequests(512)
   .cacheTtl(Duration.ofHours(24))
   .retries(2).retryDelay(Duration.ofMillis(400)).build();
}
```

Un cache Redis implémente `ViesCache`, avec clé VAT+requester, timeout court, TTL et sérialisation versionnée. Une lecture en panne reste `CACHE_ERROR` afin d’éviter qu’une panne Redis déclenche un stampede vers VIES. Une écriture en panne ne détruit pas la réponse `Valid` déjà obtenue. Le cache est une optimisation ; la preuve d’audit durable doit enregistrer séparément VAT normalisée, requester, heure UTC, résultat et éventuel `consultationNumber`.

Pour un batch massif, les workers lisent des lots bornés d’une file durable. Seuls `Unavailable` retryables et `CLIENT_OVERLOADED` sont replanifiés avec backoff, jitter et nombre maximal. Les échecs persistants vont en DLQ ; `Invalid` et `MalformedInput` sont acquittés. Un limiteur distribué borne la somme des pods, car les sémaphores du client sont locales à chaque JVM. Autoscaler sans budget global peut aggraver une panne upstream.

## Limites, données et exploitation

VIES fédère des systèmes nationaux dont latence, disponibilité, champs et quotas varient. Les virtual threads réduisent le coût d’attente, pas le trafic permis par l’upstream. L’application doit borner ingress, queue, consumers, futures conservées et stockage d’audit. Une health probe ne doit pas valider une VAT à chaque passage ; liveness processus et readiness queue/cache sont locales, `check-status` est appelé avec parcimonie.

VAT, nom, adresse, requester et consultation peuvent être sensibles. Ne les utilisez jamais comme labels métriques ni logs ordinaires. Définissez accès, chiffrement, rétention et effacement. Le client ne fournit ni avis fiscal/juridique/comptable ni affiliation officielle à la Commission européenne.

Avant production, un staging avec données synthétiques simule hits/misses cache, 429/5xx, timeout, cache down, backlog, cancellation et shutdown simultané. Les critères exigent aucune fausse réponse `Invalid`, mémoire bornée, limites respectées, audit traçable et replay DLQ. Les versions suivent SemVer ; gardez rollback, notes de release, checksums et dernière `1.x` supportée.

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
./mvnw package
jar --describe-module --file target/vies-client-1.2.0.jar
java -cp target/classes examples/ViesDemoServer.java
```

La disponibilité membre peut être consultée avec l’API dédiée, sans confondre ce statut indicatif avec une décision sur une VAT. Les erreurs `SERVICE_UNAVAILABLE`, `MS_UNAVAILABLE`, `SERVER_BUSY`, `TIMEOUT`, `NETWORK_ERROR` et certains HTTP sont temporairement retryables ; input, requester, blocked, closed et malformed demandent correction ou intervention. Le host doit toujours utiliser `error().retryable()` plutôt qu’une liste divergente.

Le cache désactivé force chaque nouvelle opération à VIES, sauf regroupement single-flight simultané. Avec cache activé, seuls les `Valid` confirmés sont conservés et leur TTL ne garantit pas que l’entreprise restera valide pendant toute la durée. Les options gigantesques ou négatives sont rejetées, y compris Durations susceptibles de dépasser nanos/millis. Fermez le client une seule fois au shutdown ; après fermeture, toute nouvelle demande reçoit une réponse structurée, pas une exception d’executor non contrôlée.

Chaque paramètre de production doit être versionné, documenté et vérifié en staging ; les valeurs par défaut ne constituent pas une promesse universelle de capacité.
