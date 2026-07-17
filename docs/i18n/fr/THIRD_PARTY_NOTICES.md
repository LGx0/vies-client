# Français (`fr`) — Composants tiers

[Sélecteur de langue](../../LANGUAGES.md) · [Original anglais](../../../THIRD_PARTY_NOTICES.md)

> Traduction informative. Seuls les originaux anglais `LICENSE` et `NOTICE` sont juridiquement contraignants.

Le JAR distribué ne contient et ne requiert aucune bibliothèque runtime tierce ; seulement JDK `java.base` et `java.net.http`. Dépendance test : JUnit Jupiter 6.1.2, EPL-2.0, tests uniquement. Plugins Maven et transitifs ne sont pas embarqués et gardent métadonnées/licences. Source : <https://github.com/junit-team/junit-framework>. Toute PR ajoutant une dépendance doit actualiser ce fichier et documenter la compatibilité.

EPL-2.0 concerne seulement le test séparé. Le JDK suit sa distribution. Plugins/transitifs peuvent évoluer ; le CI inspecte l’arbre effectif. Seuls `LICENSE` et `NOTICE` anglais sont contraignants. Toute composante documente nom, version, scope, source, licence, bundling et compatibilité.
