# Français (`fr`) — Politique de sécurité

[Sélecteur de langue](../../LANGUAGES.md) · [Original anglais](../../../SECURITY.md)

> L’anglais prévaut ; `LICENSE` n’est pas traduit. Licence du projet : Apache-2.0.

La dernière version `1.x` reçoit les correctifs ; versions antérieures au cas par cas. Mettez toujours à jour durant la phase initiale.

Ne créez pas d’issue publique et ne divulguez pas d’exploit avant correction coordonnée. Utilisez **Security → Advisories → Report a vulnerability** et indiquez version, environnement, reproduction, impact, contournement/correctif et, si possible, PoC minimal sans données réelles.

Objectifs sans SLA : accusé sous 7 jours, première évaluation/étape sous 14 jours, correction/divulgation selon sévérité et complexité. Sont concernés injection, contournement TLS/endpoint, fuite, cache poisoning, abus requester, croissance non bornée, deadlock shutdown ou `Invalid` erroné après panne. Disponibilité/throttling/qualité VIES seuls ne sont pas des vulnérabilités du client.

Une alerte fournit version/commit, JDK/OS, configuration sans secrets, reproduction, attendu/réel, impact et workaround. PoC avec VAT/requester synthétiques et mock local ; aucune donnée client/fiscale/auth. Communication chiffrée possible après advisory privé.

Les 7/14 jours sont des objectifs non contractuels. Évaluez gravité, exploitabilité, versions et disclosure coordonnée. Pas de publication avant fix. Scope : SSRF/endpoint, TLS, logs, désérialisation cache, resource leaks, races donnant fausse décision, bypass de limites. Une panne upstream correctement `Unavailable` n’est pas une vulnérabilité.

Après fix : régression, changelog, advisory, versions et upgrade. Détails éventuellement retenus jusqu’au patch. Reconnaissance possible, aucun bug bounty garanti.

Les pièces jointes doivent être minimales et vérifiables par checksum. Ne demandez pas au maintainer d’ouvrir un binaire opaque ou d’exécuter un script destructif. Une reproduction Docker/Java utilise des données synthétiques et décrit les permissions nécessaires. Si un secret a été exposé, révoquez-le immédiatement au lieu d’attendre la correction logicielle.
