# Hrvatski (hr) — Objavljivanje na GitHubu

> [Svi jezici](../../LANGUAGES.md) · Informativni prijevod. U slučaju razlike mjerodavan je kanonski engleski tehnički ili pravni izvor. Samo su korijenski `LICENSE` i `NOTICE` pravno mjerodavni; prijevod ih ne zamjenjuje.

## 1. Obavezne provjere prije objavljivanja

- Potvrdite da imate pravo objaviti izvor, dokumentaciju i ime.
- Skenirajte cijeli direktorij i kasnije cijelu Git povijest u potrazi za tajnama.
- Uklonite korisničke podatke, pravi porezni broj podnositelja zahtjeva, token,`.env` datoteku i IDE
  datoteka specifična za stroj.
- Provjerite Apache-2.0`LICENSE`,`NOTICE`i datoteku obavijesti treće strane.
- Trčanje:`./mvnw --batch-mode --no-transfer-progress clean verify`.

- Potvrdite pravo na objavu cjelokupnog koda, dokumentacije i naziva projekta.
- Skenirajte imenik i eventualnu Git povijest u potrazi za tajnama.
- Uklonite korisničke podatke, stvarne PDV brojeve podnositelja zahtjeva, tokene,`.env` i strojne datoteke.
- Provjerite Apache-2.0 i obavijesti trećih strana.
- Pokrenite punu Maven provjeru.

## 2. Stvorite repozitorij

Projekt već uključuje README, licencu i datoteku `.gitignore`, dakle GitHub
nemojte generirati nove prilikom izrade weba. Preporučeno ime:`vies-client`, vidljivost:
`Public`, zadana grana:`main`.

Projekt već sadrži datoteke README, licence i zanemarivanja; ne stvaraju
duplikata na GitHubu. Predloženi naziv:`vies-client`, vidljivost:`Public`, zadano
grana:`main`.

Naredba koja se može koristiti nakon potvrde točnog vlasnika:

```bash
git init -b main
git add --all
git commit -m "Initial open-source release"
gh repo create OWNER/vies-client --public --source . --remote origin --push
```

Ne izvodite naredbu bez zamjene `OWNER` i provjere pune stupnjeve diff.
Ne pokrećite ga prije nego što zamijenite `OWNER` i pregledate kompletnu diff.

## 3. Postavke spremišta

Preporučeno:

- Problemi: omogućeno;
- Rasprave: omogućeno za pitanja o korištenju;
- Wikiji: onemogućeni osim ako se aktivno ne održavaju;
- Projekti: izborno;
- Sačuvaj ovo spremište: izborno nakon prvog stabilnog izdanja;
- Automatsko brisanje glavnih grana: omogućeno;
- Privatno izvješćivanje o ranjivostima: omogućeno;
- Dependabot upozorenja i sigurnosna ažuriranja: omogućeno;
- Tajno skeniranje i push zaštita: omogućeno tamo gdje ih GitHub nudi.

## 4. Zaštita grane ili ruleset

Ogranak `main`:

- potreban zahtjev za povlačenjem;
- najmanje jedno odobrenje / najmanje jedno odobrenje;
- odbacivanje starog odobrenja nakon novih obveza;
- potrebne provjere:`CI`,`CodeQL`,`Dependency review` prema mogućnostima;
- potrebno razrješenje razgovora;
- onemogućeno prisilno guranje i brisanje;
- administratorska premosnica koja se koristi samo u hitnim slučajevima;
- potpisane obveze/oznake preporučene za izdanja.

Prije prvog pritiska, naziv tražene provjere još ne postoji; jednom pokrenite tijek rada
kat, majd állítsd biti pravilasetet. / Imena obavezne provjere pojavljuju se nakon prvog tijeka rada
trčanje; nakon toga konfigurirajte skup pravila.

## 5. Oznake i teme

Predložene oznake:

```text
bug, enhancement, documentation, localization, security, dependencies,
performance, concurrency, breaking-change, good-first-issue, help-wanted, triage
```

Predloženi opis spremišta:

```text
Fast Java 21 VIES VAT checker and EU VAT number validator with virtual threads, bounded concurrency, cache, and bilingual errors.
```

Predložene teme:

```text
java, java21, java-library, vies, vat, vat-number, vat-checker, vat-validator,
vat-validation, vat-number-validation, eu-vat, tax-id, tax-id-checker,
tax-id-validation, rest-client, virtual-threads, single-flight, jpms,
zero-dependency
```

## 6. Gumb Doniraj/Sponzoriraj

GitHub prikazuje gumb **Sponzor** na temelju `.github/FUNDING.yml`. Samo
unesite financijski URL koji je potvrdio održavatelj. Primjer:

```yaml
buy_me_a_coffee: lgx0
```

Namjenski ključ također se može koristiti s podržanim pružateljem, na primjer `github`,`ko_fi`
ili `custom`. Donacija ne predstavlja SLA niti pravo prvenstva; vidi `SUPPORT.md`.

GitHub prikazuje gumb Sponzor iz `.github/FUNDING.yml`. Konfigurirajte samo a
odredište financiranja koje je potvrdio održavatelj. Donacije ne kupuju SLA ili
prava upravljanja.

## 7. Ažuriranja nakon prvog pritiska

Kada saznate stvarni GitHub URL, ažurirajte:

- `pom.xml`:`url`,`scm`,`developers`;

- PROČITAJ ME: URL-ovi znački CI i CodeQL;
- `SECURITY.md`: privatni savjetodavni URL, ako je potrebno;
- dokumentacija izdanja i koordinate Maven Centrala;
- `.github/FUNDING.yml`:`https://buymeacoffee.com/lgx0`.

## 8. Prvo izdanje

Stvorite potpisanu oznaku `v1.0.0` tek nakon zelenog CI/CodeQL. Testovi tijeka rada izdanja,
izgraditi binarne/izvore/Javadoc JAR-ove i SHA-256 kontrolni zbroj, zatim GitHub
Stvara izdanje. Detalji: [OSLOBAĐANJE.md](RELEASING.md).
