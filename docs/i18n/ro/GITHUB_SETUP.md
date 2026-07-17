# Română (ro) — Publicarea pe GitHub

> [Toate limbile](../../LANGUAGES.md) · Traducere informativă. În caz de diferență prevalează sursa canonică tehnică sau juridică în limba engleză. Numai `LICENSE` și `NOTICE` din rădăcină sunt texte juridice oficiale; traducerea nu le înlocuiește.

## 1. Verificări obligatorii înainte de publicare

- Confirmați că aveți dreptul de a publica sursa, documentația și numele.
- Scanați întregul director și mai târziu întregul istoric Git pentru secrete.
- Eliminați datele clienților, numărul fiscal al solicitantului real, simbolul, fișierul `.env` și IDE
  fișier specific mașinii.
- Verificați Apache-2.0`LICENSE`,`NOTICE`și fișierul de notificare a terților.
- Run:`./mvnw --batch-mode --no-transfer-progress clean verify`.

- Confirmați dreptul de a publica tot codul, documentația și denumirea proiectului.
- Scanați directorul și eventualul istoric Git pentru secrete.
- Eliminați datele clienților, numerele de TVA ale solicitantului real, jetoanele,`.env` și fișierele mașinii.
- Verificați notificările Apache-2.0 și ale terților.
- Rulați verificarea completă Maven.

## 2. Creați depozitul

Proiectul include deja un README, licență și fișier `.gitignore`, de unde GitHub
nu generați altele noi atunci când creați web. Nume sugerat:`vies-client`, vizibilitate:
`Public`, ramură implicită:`main`.

Proiectul conține deja fișiere README, licență și ignorare; nu generează
duplicate pe GitHub. Nume sugerat:`vies-client`, vizibilitate:`Public`, implicit
filiala:`main`.

Comandă care poate fi folosită după confirmarea proprietarului exact:

```bash
git init -b main
git add --all
git commit -m "Initial open-source release"
gh repo create OWNER/vies-client --public --source . --remote origin --push
```

Nu rulați comanda fără să înlocuiți `OWNER` și să verificați diferența completă în etape.
Nu-l rulați înainte de a înlocui `OWNER` și de a revizui diferența completă în etape.

## 3. Setări pentru depozit

Recomandat:

- Probleme: activat;
- Discuții: activat pentru întrebări de utilizare;
- Wikis: dezactivat dacă nu este întreținut activ;
- Proiecte: optional;
- Păstrați acest depozit: opțional după prima lansare stabilă;
- Ștergeți automat ramurile capului: activat;
- Raportarea vulnerabilităților private: activată;
- Alerte Dependabot și actualizări de securitate: activate;
- Scanare secretă și protecție prin împingere: activată acolo unde GitHub le oferă.

## 4. Protecția ramurii sau ruleset

O sucursală `main`:

- solicitarea de tragere este necesară;
- cel putin o aprobare / cel putin o aprobare;
- demiterea avizului învechit după noi comiteri;
- verificări necesare:`CI`,`CodeQL`,`Dependency review`după cum sunt disponibile;
- necesara rezolvarea conversatiei;
- forțare împingere și ștergere dezactivate;
- bypass admin folosit doar pentru urgente;
- comite-uri/etichete semnate recomandate pentru lansări.

Înainte de prima apăsare, numele verificării necesare nu există încă; rulați fluxul de lucru o dată
kat, majd állítsd fie un reguli. / Numele de verificare obligatorie apar după primul flux de lucru
alerga; configurați ulterior setul de reguli.

## 5. Etichete și subiecte

Etichete sugerate:

```text
bug, enhancement, documentation, localization, security, dependencies,
performance, concurrency, breaking-change, good-first-issue, help-wanted, triage
```

Descrierea depozitului sugerată:

```text
Fast Java 21 VIES VAT checker and EU VAT number validator with virtual threads, bounded concurrency, cache, and bilingual errors.
```

Subiecte sugerate:

```text
java, java21, java-library, vies, vat, vat-number, vat-checker, vat-validator,
vat-validation, vat-number-validation, eu-vat, tax-id, tax-id-checker,
tax-id-validation, rest-client, virtual-threads, single-flight, jpms,
zero-dependency
```

## 6. Butonul Donează/Sponsorizează

GitHub afișează butonul **Sponsor** bazat pe `.github/FUNDING.yml`. Doar cel
introduceți o adresă URL financiară verificată de întreținător. Exemplu:

```yaml
buy_me_a_coffee: lgx0
```

O cheie dedicată poate fi utilizată și cu un furnizor acceptat, de exemplu `github`,`ko_fi`
sau `custom`. O donație nu constituie un SLA sau un drept de prioritate; vezi `SUPPORT.md`.

GitHub afișează butonul Sponsor de la `.github/FUNDING.yml`. Configurați doar a
destinația finanțării verificată de întreținător. Donațiile nu achiziționează un SLA sau
drepturi de guvernare.

## 7. Actualizări după prima apăsare

După ce cunoașteți adresa URL GitHub reală, actualizați:

- `pom.xml`:`url`,`scm`,`developers`;

- README: URL-uri insigna CI și CodeQL;
- `SECURITY.md`: URL de consultanță privată, dacă este necesar;
- documentația de lansare și coordonatele Maven Central;
- `.github/FUNDING.yml`:`https://buymeacoffee.com/lgx0`.

## 8. Prima lansare

Creați o etichetă `v1.0.0` semnată numai după CI/CodeQL verde. Testele fluxului de lucru de lansare,
construiți JAR-urile binare/sursele/Javadoc și suma de control SHA-256, apoi GitHub
Creează o eliberare. Detalii: [RELEASING.md](RELEASING.md).
