# Română (ro) — Contribuții

> [Toate limbile](../../LANGUAGES.md) · Traducere informativă. În caz de diferență prevalează sursa canonică tehnică sau juridică în limba engleză. Numai `LICENSE` și `NOTICE` din rădăcină sunt texte juridice oficiale; traducerea nu le înlocuiește.

Vă mulțumim pentru îmbunătățirea proiectului `vies-client`. Scopul este previzibil,
Client Java VIES fără dependențe și securizat chiar și sub sarcină grea.

Vă mulțumim pentru îmbunătățirea `vies-client`. Proiectul își propune să rămână previzibil,
fără dependență în timpul rulării și sigur în condiții de concurență ridicată.

## Înainte de a începe

- Pentru corectarea erorilor, deschideți o problemă sau consultați una existentă.
- Să discutăm mai întâi despre schimbările majore ale API-ului, licenței sau arhitecturii într-o problemă.
- Nu raportați o eroare de securitate într-o problemă publică; vezi [SECURITY.md](SECURITY.md).
- Deschideți sau faceți referire la o problemă pentru remedieri de erori.
- Discutați modificările majore ale API-ului, licențelor sau arhitecturii înainte de implementare.
- Nu dezvălui niciodată o vulnerabilitate într-o problemă publică; urmați [SECURITY.md](SECURITY.md).

## Mediu de dezvoltare

Necesită: JDK 21+, Maven 3.9+, Git. Controla:

```bash
java -version
javac -version
./mvnw -version
```

Verificare locală completă:

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
```

Instalare detaliată: [docs/INSTALLATION.md](INSTALLATION.md).

## Proces de dezvoltare

1. Bifurcați repo-ul, apoi creați o ramură de scurtă durată din ramura `main`.
2. Faceți o schimbare mică, concentrată pe obiective.
3. Adăugați un test de regresie deterministă pentru fiecare remediere a erorilor.
4. Rulați comanda completă `./mvnw clean verify`.
5. Deschideți o cerere de extragere și completați toate părțile relevante ale șablonului.

6. Bifurcați depozitul și creați o ramură de scurtă durată din `main`.
7. Păstrați schimbările mici și concentrate.
8. Adăugați un test de regresie deterministă pentru fiecare remediere a erorilor.
9. Rulați verificarea completă Maven.
10. Deschideți o cerere de extragere și completați șablonul furnizat.

Nume de ramuri sugerate:

```text
fix/close-race
feat/cache-adapter-hook
docs/redis-example
```

## Reguli de codificare

- Nivel de limbaj Java 21; API-ul public ar trebui să rămână mic și sigur de tip.
- Modulul de rulare ar trebui să rămână fără dependențe externe, cu excepția cazului în care se ia o decizie separată.
- `Unavailable` incertitudine tehnică, nu poate fi niciodată convertită în rezultatul`Invalid`.
- Toate stările partajate ar trebui să fie sigure pentru fire și cu memorie limitată.
- Javadoc/comentariile în limba engleză și maghiară sunt necesare pentru API-ul public și logica concurență complexă.
- Nu vă autentificați numărul fiscal complet, numele companiei, adresa sau numărul fiscal propriu al solicitantului ca date de testare.
- Folosiți Java 21 și păstrați API-ul public mic și sigur de tip.
- Păstrați modulul de rulare fără dependențe, cu excepția cazului în care sa convenit în mod explicit altfel.
- Nu convertiți niciodată `Unavailable` în `Invalid`.
- Starea partajată trebuie să fie sigură pentru fire și limitată de memorie.
- Documentați API-ul public și logica concurenței neevidente în engleză și maghiară.
- Nu angajați și nu înregistrați datele private privind TVA-ul, compania, adresa sau solicitantul.

## Reguli de testare

- Testul unitar nu ar trebui să utilizeze o rețea publică.
- Testele HTTP și de competiție pot apela doar un server simulat de loopback.
- Testele de sarcină, stres sau CI împotriva VIES publice sunt interzise.
- Situația cursei trebuie controlată prin zăvor/barieră; fix `Thread.sleep` nu poate fi un oracol al corectitudinii.
- Testul ar trebui să eșueze fără remediere.
- Testele unitare nu trebuie să utilizeze rețeaua publică.
- Testele HTTP și de concurență pot apela doar un server simulat de loopback.
- Nu executați niciodată teste de sarcină, stres sau CI necesare împotriva VIES publice.
- Conduceți curse cu zăvoare/bariere; somnurile fixe nu sunt oracole de corectitudine.
- Un test de regresie trebuie să eșueze fără remedierea corespunzătoare.

Catalog de teste: [docs/TESTING.md](TESTING.md).

## Cerințe pentru pull request

PR-ul este gata de revizuire dacă:

- build-ul și toate testele au succes;
- comportamentul public este documentat;
- nicio dependență nouă, nejustificată;
- ruptura de compatibilitate este evidentiata separat;
- reglarea puterii include măsurare locală reproductibilă;
- autorul are dreptul de a transmite codul.

Un PR este gata atunci când construcția este verde, comportamentul public este documentat, nou
dependențele sunt justificate, modificările rupturi sunt explicite, pretențiile de performanță sunt
reproductibil, iar autorul are dreptul de a trimite lucrarea.

## Modificări asistate de AI

AI poate fi folosit, dar cel care trimite este responsabilitatea totală pentru rezultat. Fiecare linie
trebuie verificate, testate și revizuite pentru licență. AI semnificativă-
indicați contribuția în PR; nu furnizați date confidențiale sau protejate de terți
model și nu trimiteți codul generat neverificat.

Instrumentele AI sunt permise, dar contribuitorul rămâne pe deplin responsabil. Revizuire și testare
fiecare linie, verifică proveniența și acordarea licenței, dezvăluie asistență substanțială AI în
PR și nu trimiteți niciodată materiale confidențiale sau nerevizuite.

## Licență

Proiectul este licențiat sub Apache-2.0. Consimțământ prin trimiterea intenționată a unei cereri de extragere
este transmisă necondiționat în conformitate cu Secțiunea 5 a Licenței Apache 2.0,
cu excepția cazului în care expeditorul indică altfel.

Proiectul folosește Apache-2.0. Prin trimiterea intenționată a unei contribuții, trimiteți
în conformitate cu Secțiunea 5 din Licența Apache 2.0, fără termeni suplimentari, cu excepția cazului în care dvs
declară explicit contrariul.

## Comportament

Toți participanții sunt supuși [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).
Toți participanții trebuie să urmeze [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).
