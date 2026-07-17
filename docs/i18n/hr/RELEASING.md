# Hrvatski (hr) — Izdavanje

> [Svi jezici](../../LANGUAGES.md) · Informativni prijevod. U slučaju razlike mjerodavan je kanonski engleski tehnički ili pravni izvor. Samo su korijenski `LICENSE` i `NOTICE` pravno mjerodavni; prijevod ih ne zamjenjuje.

## 1. Preduvjeti

- čista `main` grana i zelene GitHub akcije;
- odgovarajuće GitHub ovlaštenje za izradu oznake i izdanje;
- JDK 21 i Maven 3.9+;
- verzija izdanja i datum zabilježeni u datoteci `CHANGELOG.md`.

## 2. Verzija

Projekt koristi semantičku verziju:

- `PATCH`: kompatibilan ispravak bugova;

- `MINOR`: kompatibilna nova funkcija;
- `MAJOR`: razbijanje javnog API-ja ili semantike.

Projekt koristi semantičko određivanje verzija: zakrpa za kompatibilne popravke, manja za kompatibilne
značajke i glavne za razbijanje API-ja ili semantičke promjene.

## 3. Provjera prije objave

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
jar --describe-module --file target/vies-client-1.0.0.jar
jdeps --print-module-deps target/vies-client-1.0.0.jar
```

Također provjerite:

- nema tajni ili osobnih podataka u cijeloj Git povijesti;
- `LICENSE`,`NOTICE`,`SECURITY.md`i dokumentacija su aktualni;
- Javadoc i izvorni JAR stvoreni;
- nema obveznog VIES-a uživo ili testa opterećenja u CI-ju;
- promjene javnog API-ja uključene su u dnevnik promjena.

## 4. GitHub izdanje

1. Postavite verziju u datoteci `pom.xml`.
2. Utvrdite dnevnik promjena i verziju.
3. Napravite potpisanu označenu oznaku:`git tag -s v1.0.0 -m "v1.0.0"`.
4. Push commit i tag:`git push origin main --follow-tags`.
5. Tijek rada `release.yml` ponovno pokreće testove i zatim prilaže binarnu datoteku,
   izvori i Javadoc JAR datoteke za GitHub izdanje.

Koristite potpisane označene oznake kada je to moguće. Nikada ne stvarajte izdanje od nepregledanog ili
neuspješno počiniti.

## 5. Paketi Maven Central ili GitHub

Trenutna verzija je lokalna i spremna za distribuciju GitHub Release. Više za Maven Central
potrebno:

- provjerljivi, vlastiti obrnuti DNS `groupId`;
- projekt `url`,`scm`,`developers`i metapodaci upravljanja distribucijom;
- Registracija i token na središnjem portalu;
- GPG potpis i središnja kompatibilna konfiguracija objavljivanja.

Trenutna verzija je spremna za lokalnu instalaciju i GitHub izdanja. Maven Central
objava dodatno zahtijeva ID obrnute DNS grupe u vlasništvu, kompletan projekt/SCM
metapodaci, vjerodajnice središnjeg portala, potpisivanje artefakta i konfiguracija objavljivanja.

Ne stavljajte token ili privatni GPG ključ u spremište. GitHub radnje su tajne i minimalne
koristiti ovlaštenje. / Nikada nemojte predavati tokene ili privatne ključeve za potpisivanje. Koristite GitHub
Tajne radnji i najmanje privilegije.

## 6. Koraci nakon objave

- provjerite preuzimanja izdanja i SHA-256 vrijednosti;
- započeti novu `[Unreleased]` sesiju;
- objavi GitHub sigurnosno savjetovanje za sigurnosnu zakrpu;
- ažuriranje dokumentirane verzije ovisnosti.
