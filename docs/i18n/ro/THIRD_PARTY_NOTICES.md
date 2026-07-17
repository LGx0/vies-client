# Română (ro) — Componente terțe

> [Toate limbile](../../LANGUAGES.md) · Traducere informativă. În caz de diferență prevalează sursa canonică tehnică sau juridică în limba engleză. Numai `LICENSE` și `NOTICE` din rădăcină sunt texte juridice oficiale; traducerea nu le înlocuiește.

## JAR de execuție distribuită

`vies-client-1.0.0.jar` nu include și nu necesită biblioteci de rulare externe.
Utilizează numai modulele `java.base` și `java.net.http` ale JDK.

JAR-ul distribuit conține și nu necesită nicio bibliotecă de execuție terță parte. Doar asta
folosește modulele JDK `java.base` și `java.net.http`.

## Creați și testați dependența

| Componentă / Componentă | Versiune / Versiune | Utilizare / Domeniu   | Licență / Licență |
| ----------------------- | ------------------: | --------------------- | ----------------- |
| JUnit Jupiter           |               6.1.2 | doar test / doar test | EPL-2.0           |

Pluginurile de compilare Maven și dependențele de testare tranzitivă nu sunt incluse în JAR-ul lansat
în; sunt descărcate de Maven conform propriilor metadate și licențe ale artefactelor.

Pluginurile Maven și dependențele de testare tranzitivă nu sunt incluse în versiunea JAR;
Maven le rezolvă în baza propriilor metadate și licențe ale artefactelor.

Sursă / Sursă: <https://github.com/junit-team/junit-framework>

Dacă la proiect este adăugată o nouă dependență, acest fișier și licența verifică compatibilitatea
trebuie actualizat în aceeași cerere de extragere.

Orice cerere de extragere care adaugă o dependență trebuie să actualizeze acest fișier și licența documentului
compatibilitate.
