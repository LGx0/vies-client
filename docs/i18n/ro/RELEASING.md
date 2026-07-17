# Română (ro) — Publicarea versiunilor

> [Toate limbile](../../LANGUAGES.md) · Traducere informativă. În caz de diferență prevalează sursa canonică tehnică sau juridică în limba engleză. Numai `LICENSE` și `NOTICE` din rădăcină sunt texte juridice oficiale; traducerea nu le înlocuiește.

## 1. Cerințe preliminare

- curățați ramura `main` și acțiunile GitHub verzi;
- autoritatea GitHub corespunzătoare pentru a crea eticheta și a elibera;
- JDK 21 și Maven 3.9+;
- versiunea lansării și data înregistrate în fișierul `CHANGELOG.md`.

## 2. Versiune

Proiectul folosește versiunea semantică:

- `PATCH`: remediere a erorilor compatibile;

- `MINOR`: noua functie compatibila;
- `MAJOR`: ruperea API-ului public sau a semanticii.

Proiectul folosește Semantic Versioning: patch pentru corecții compatibile, minor pentru compatibil
caracteristici și majore pentru întreruperea modificărilor API sau semantice.

## 3. Verificare înainte de lansare

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
jar --describe-module --file target/vies-client-1.2.0.jar
jdeps --print-module-deps target/vies-client-1.2.0.jar
```

De asemenea, verificați:

- fără secrete sau date personale în întregul istoric Git;
- `LICENSE`,`NOTICE`,`SECURITY.md`și documentația sunt actuale;
- Javadoc și surse JAR create;
- nu există obligatoriu VIES live sau test de sarcină în CI;
- modificările aduse API-ului public sunt incluse în jurnalul de modificări.

## 4. Lansarea GitHub

1. Setați versiunea în fișierul `pom.xml`.
2. Commiteți jurnalul de modificări și versiunea.
3. Creați o etichetă adnotată semnată:`git tag -s v1.2.0 -m "v1.2.0"`.
4. Apăsați commit și etichetați:`git push origin main --follow-tags`.
5. Fluxul de lucru `release.yml` reluează testele și apoi atașează fișierul binar,
   surse și fișiere JAR Javadoc pentru lansarea GitHub.

Folosiți etichete adnotate semnate atunci când este posibil. Nu creați niciodată o versiune dintr-o versiune nerevizuită sau
nerespectarea comiterii.

## 5. Pachete Maven Central sau GitHub

Versiunea actuală este locală și gata pentru distribuția GitHub Release. Mai multe pentru Maven Central
necesar:

- un DNS invers propriu verificabil `groupId`;
- proiect `url`,`scm`,`developers`și metadate de distribuție-management;
- Înregistrarea și tokenul Portalului Central;
- Semnătură GPG și configurație de publicare compatibilă cu Central.

Versiunea actuală este gata pentru instalare locală și versiuni GitHub. Maven Central
Publicarea necesită în plus un ID de grup DNS invers deținut, proiect complet/SCM
metadate, acreditările Portalului Central, semnarea artefactelor și configurația publicării.

Nu puneți un token sau o cheie GPG privată în depozit. Acțiunile GitHub sunt secrete și minime
autorizatie de utilizare. / Nu comite niciodată jetoane sau chei private de semnare. Utilizați GitHub
Acțiuni secrete și cel mai mic privilegiu.

## 6. Pașii post-lansare

- verificați descărcările versiunii și valorile SHA-256;
- începe o nouă sesiune `[Unreleased]`;
- publicați un aviz de securitate GitHub pentru un patch de securitate;
- actualizați versiunea de dependență documentată.
