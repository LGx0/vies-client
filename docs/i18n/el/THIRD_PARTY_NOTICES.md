# Ελληνικά (el) — Στοιχεία τρίτων

**Languages:** [English](../../../THIRD_PARTY_NOTICES.md) · [Ελληνικά](THIRD_PARTY_NOTICES.md) · [Όλες οι γλώσσες](../../LANGUAGES.md)

> Ενημερωτική μετάφραση. Σε περίπτωση διαφοράς υπερισχύει το αγγλικό τεχνικό και νομικό πρωτότυπο. Μόνο τα ριζικά LICENSE και NOTICE είναι νομικά έγκυρα· η μετάφραση δεν αποτελεί άδεια. [LICENSE](../../../LICENSE), [NOTICE](../../../NOTICE).

vies-client-1.0.0.jar contains/requires no third-party runtime library; only JDK java.base and java.net.http.

| Component | Version | Scope | License |
|---|---:|---|---|
| JUnit Jupiter | 6.1.2 | test only | EPL-2.0 |

Maven plugins/transitive tests are not bundled; Maven resolves own metadata/licenses. Source: <https://github.com/junit-team/junit-framework>. Any new dependency PR updates this file and license compatibility.

Σε αλλαγή dependency, το pull request ελέγχει προέλευση, άδεια, scope και ότι το component δεν περιλαμβάνεται στο runtime JAR.
