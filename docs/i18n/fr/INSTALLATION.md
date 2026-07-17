# Français (`fr`) — Installation

[Sélecteur de langue](../../LANGUAGES.md) · [Original anglais](../../INSTALLATION.md)

> L’original anglais prévaut en cas d’écart. `LICENSE` reste non traduit et seul fait foi. Licence du projet : Apache-2.0.

## Prérequis

- JDK 21 ou plus récent et Maven 3.9+.
- HTTPS sortant vers `ec.europa.eu:443` en production. La suite locale n’utilise pas Internet.

```bash
java -version
./mvnw -version
```

macOS : `brew install openjdk@21`, puis définissez `JAVA_HOME`. Sous Linux, installez le paquet JDK 21 de la distribution. Sous Windows PowerShell :

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

## Compiler et installer

```bash
git clone <repository-url>
cd vies-client
./mvnw clean verify
./mvnw install
```

Le répertoire `target/` contient le JAR, `-sources.jar` et `-javadoc.jar`. Pour Maven utilisez les coordonnées `vies.client:vies-client:1.0.0`; pour Gradle : `implementation("vies.client:vies-client:1.0.0")`.

Le module JPMS se déclare par `requires vies.client;`. En classpath aucune déclaration n’est nécessaire. Sans outil de build, utilisez le JAR avec `--module-path` ou `-cp`; si vous copiez les sources vers un projet non modulaire, omettez `module-info.java`.

Configurez le SDK du projet et le JDK Maven de l’IDE sur 21. Les proxys et certificats d’entreprise doivent être gérés par la JVM/l’infrastructure ; ne stockez jamais d’identifiants dans le dépôt.

```bash
./mvnw -q clean test
./mvnw -q package
jar --describe-module --file target/vies-client-1.0.0.jar
```

Un test live doit rester manuel et minimal. Les erreurs fréquentes viennent d’un mauvais JDK, d’un proxy/TLS bloqué, d’une indisponibilité VIES/État membre ou de limites locales trop agressives.

## Détails complets de plateforme, build et réseau

macOS : persistez le chemin Homebrew et `JAVA_HOME="$(/usr/libexec/java_home -v 21)"`. Linux : p. ex. `sudo apt install openjdk-21-jdk maven`, puis `JAVA_HOME`. Windows : rouvrez le terminal après modification. Vérifiez ensemble `java -version`, `javac -version`, `./mvnw -version`.

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
./mvnw install
ls -lh target/vies-client-1.0.0*.jar
javac --release 21 -cp vies-client-1.0.0.jar MyApp.java
java -cp .:vies-client-1.0.0.jar MyApp  # Windows : ;
```

Le JAR release n’a aucune dépendance runtime tierce ; JUnit et `jdk.httpserver` sont de test. IDE : SDK, language level, Maven JDK, `java.configuration.runtimes` et `java.jdt.ls.java.home` sur 21, puis rechargez Java/Maven. Ne commitez aucun chemin SDK machine.

HTTPS requis vers `https://ec.europa.eu/taxation_customs/vies/`. Configurez proxy/truststore centralement, ne désactivez jamais TLS. Conteneur : JRE/JDK 21, CA, utilisateur non-root, limites CPU/heap/connexions.

| Problème | Correction |
|---|---|
| `switch expressions ... Java 14` | SDK/language level 21 |
| `release version 21 not supported` | corriger Maven JDK/`JAVA_HOME` |
| `UnsupportedClassVersionError` | runtime 21+ |
| `NETWORK_ERROR` | DNS, proxy, pare-feu, TLS |
| `TIMEOUT` | examiner réseau/VIES, ne pas augmenter sans borne |
| `CLIENT_OVERLOADED` | retry différé, ingress borné, workers + limiteur global |
| `CACHE_ERROR` | santé/timeouts/métriques Redis, pas de stampede VIES |

## Installation reproductible et artefacts

`./mvnw clean verify` nettoie les anciennes sorties, compile avec release 21, exécute les tests unitaires et d’intégration loopback, puis construit le module. `./mvnw install` dépose JAR binaire, sources et Javadocs dans le repository Maven local. Le JDK utilisé par Maven doit être le même que celui affiché par le terminal. Le cache Maven du CI accélère le build mais ne remplace pas la vérification des artefacts et dépendances.

Avant publication, `./mvnw install` ou le JAR direct suffisent. Après publication, Maven Central ou GitHub Packages exigent leurs repositories et credentials. `mavenLocal()` convient au développement Gradle, pas à un build de production reproductible. Vérifiez le module avec `jar --describe-module` et compilez un petit projet classpath puis JPMS.

En entreprise, contrôlez séparément DNS, CONNECT HTTPS, authentification proxy et CA truststore. Les secrets proxy restent dans le gestionnaire de secrets, jamais dans Git ou une ligne de commande visible. Kubernetes doit définir egress, DNS, CA, non-root, CPU/heap, connexions et grace period. Commencez par la suite offline et le serveur local ; un live smoke se limite à quelques appels, jamais à un test de charge.

Après installation, archivez les rapports de tests et checksums du JAR utilisé. Une mise à jour se fait d’abord en staging avec rollback de la version précédente. Ne copiez pas `target/` dans Git et ne mélangez pas classes produites par plusieurs JDK.
