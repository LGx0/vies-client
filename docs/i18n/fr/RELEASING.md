# Français (`fr`) — Publication

[Sélecteur de langue](../../LANGUAGES.md) · [Original anglais](../../RELEASING.md)

> L’original anglais prévaut. `LICENSE` demeure non traduit et seul fait foi.

Publiez depuis un commit propre et vérifié avec JDK 21, Maven et les accès nécessaires. Suivez SemVer : MAJOR incompatible, MINOR compatible avec fonctionnalité, PATCH correction compatible.

```bash
./mvnw clean verify
./mvnw package
jar --describe-module --file target/vies-client-1.2.0.jar
```

Avant release, contrôlez version du `pom.xml`, changelog, exemples, liens de langues, Javadocs, sources, licence/sécurité, absence de secrets et répétition des tests concurrents. Un appel VIES live ne peut être qu’un smoke minimal.

Créez un tag annoté/signé `vX.Y.Z`, poussez-le et produisez un GitHub Release. Les notes décrivent nouveautés, changements API/configuration, corrections, migration et limites. Publiez JAR, sources, Javadocs et sommes de contrôle du même run CI.

Maven Central exige coordonnées, métadonnées POM, sources/Javadocs, signatures et secrets de publication ; GitHub Packages nécessite une configuration côté consommateur. Après publication, testez dans un projet vierge, vérifiez les index, mettez la documentation à jour et surveillez sécurité/rollback. Ne remplacez jamais silencieusement un artefact : publiez un PATCH correctif.

La checklist vérifie Git propre, Apache-2.0/NOTICE/tiers, scans verts, liens de langues, absence de secrets et répétition des races. Binary, sources, Javadocs et SHA-256 proviennent du même run CI. Tags et artefacts sont immuables. Les notes listent breaking changes, options, corrections concurrence/sécurité, limites, migration et rollback.

Maven Central exige namespace, POM complet, signatures et secrets CI ; GitHub Packages exige configuration client. Avant publish final, installez depuis un projet Java 21 vierge en classpath et JPMS. Release seulement après CI/CodeQL verts et smoke live minimal.
