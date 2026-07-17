# Hrvatski (hr) — Doprinos projektu

> [Svi jezici](../../LANGUAGES.md) · Informativni prijevod. U slučaju razlike mjerodavan je kanonski engleski tehnički ili pravni izvor. Samo su korijenski `LICENSE` i `NOTICE` pravno mjerodavni; prijevod ih ne zamjenjuje.

Hvala vam što ste poboljšali projekt `vies-client`. Cilj je predvidljiv,
bez ovisnosti i siguran Java VIES klijent čak i pod velikim opterećenjem.

Hvala što poboljšavate `vies-client`. Cilj projekta je ostati predvidljiv,
bez ovisnosti tijekom izvođenja i siguran pod visokom konkurentnošću.

## Prije početka

- Za ispravak pogreške otvorite problem ili pogledajte postojeći.
- Raspravljajmo o velikim promjenama API-ja, licence ili arhitekture prvo u izdanju.
- Ne prijavljujte sigurnosnu pogrešku u javnom izdanju; vidi [SIGURNOST.md](SECURITY.md).
- Otvorite ili referencirajte problem za ispravke grešaka.
- Razgovarajte o velikim promjenama API-ja, licenciranja ili arhitekture prije implementacije.
- Nikada ne otkrivajte ranjivost u javnoj temi; slijedite [SIGURNOST.md](SECURITY.md).

## Razvojno okruženje

Zahtijeva: JDK 21+, Maven 3.9+, Git. Kontrolirati:

```bash
java -version
javac -version
./mvnw -version
```

Puna lokalna potvrda:

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
```

Detaljna instalacija: [docs/INSTALLATION.md](INSTALLATION.md).

## Proces razvoja

1. Račvajte repo, zatim kreirajte kratkotrajnu granu iz grane `main`.
2. Napravite malu promjenu usmjerenu na cilj.
3. Dodajte deterministički regresijski test za svaki ispravak pogreške.
4. Izvedite punu naredbu `./mvnw clean verify`.
5. Otvorite zahtjev za povlačenjem i ispunite sve relevantne dijelove predloška.

6. Račvajte repozitorij i kreirajte kratkotrajnu granu iz `main`.
7. Neka promjene budu male i fokusirane.
8. Dodajte deterministički regresijski test za svaki ispravak pogreške.
9. Pokrenite punu Maven provjeru.
10. Otvorite zahtjev za povlačenjem i ispunite ponuđeni predložak.

Predloženi nazivi grana:

```text
fix/close-race
feat/cache-adapter-hook
docs/redis-example
```

## Pravila kodiranja

- razina jezika Java 21; javni API bi trebao ostati malen i siguran za tip.
- Izvršni modul trebao bi ostati bez vanjskih ovisnosti, osim ako se ne donese posebna odluka.
- Tehnička nesigurnost `Unavailable`, nikada se ne može pretvoriti u rezultat `Invalid`.
- Sva dijeljena stanja trebaju biti niti sigurno i ograničena memorija.
- Javadoc/komentari na engleskom i mađarskom su potrebni za javni API i složenu konkurentnu logiku.
- Nemojte prijavljivati ​​puni porezni broj, naziv tvrtke, adresu ili vlastiti porezni broj podnositelja zahtjeva kao testne podatke.
- Koristite Javu 21 i održavajte javni API malim i sigurnim za upisivanje.
- Zadržite modul za vrijeme izvođenja bez ovisnosti osim ako nije izričito drugačije dogovoreno.
- Nikada ne pretvarajte `Unavailable` u `Invalid`.
- Dijeljeno stanje mora biti niti sigurno i ograničeno na memoriju.
- Dokumentirajte javni API i logiku neočite paralelnosti na engleskom i mađarskom jeziku.
- Nemojte predavati niti bilježiti privatne podatke o PDV-u, tvrtki, adresi ili podnositelju zahtjeva.

## Pravila testiranja

- Jedinični test ne bi trebao koristiti javnu mrežu.
- HTTP i natjecateljski testovi mogu pozvati samo lažni poslužitelj petlje.
- Zabranjeni su testovi opterećenja, stresa ili CI u odnosu na javni VIES.
- Situaciju na utrci treba kontrolirati zasunom/preprekom; fiksni `Thread.sleep` ne može biti proročište ispravnosti.
- Test bi trebao biti neuspješan bez popravka.
- Jedinični testovi ne smiju koristiti javnu mrežu.
- HTTP i testovi konkurentnosti mogu pozvati samo lažni poslužitelj povratne petlje.
- Nikada nemojte pokretati testove opterećenja, stresa ili potrebne CI testove protiv javnih VIES.
- Vozite utrke sa zasunima/pregradama; fiksna spavanja nisu proročanstva ispravnosti.
- Regresijski test mora pasti bez odgovarajućeg popravka.

Katalog testova: [docs/TESTING.md](TESTING.md).

## Zahtjevi za pull request

PR je spreman za pregled ako:

- izrada i svi testovi su uspješni;
- javno ponašanje je dokumentirano;
- nema nove, neopravdane ovisnosti;
- prekid kompatibilnosti je posebno označen;
- podešavanje snage uključuje ponovljivo lokalno mjerenje;
- autor ima pravo dostaviti šifru.

PR je spreman kada je gradnja zelena, javno ponašanje dokumentirano, novo
ovisnosti su opravdane, prijelomne promjene su eksplicitne, tvrdnje o izvedbi jesu
reproduktivan, a autor ima pravo predaje rada.

## Promjene uz pomoć umjetne inteligencije

AI se može koristiti, ali podnositelj preuzima punu odgovornost za rezultat. Svaki redak
moraju biti provjereni, testirani i pregledani za licenciranje. Značajna AI-
navesti doprinos u PR-u; ne daju povjerljive podatke ili podatke zaštićene trećim stranama
model i nemojte slati neprovjereni generirani kod.

AI alati su dopušteni, ali suradnik ostaje u potpunosti odgovoran. Pregledajte i testirajte
svaki redak, provjeriti porijeklo i licenciranje, otkriti značajnu pomoć umjetne inteligencije u
PR-u i nikada ne šaljite povjerljive ili nepregledane generirane materijale.

## Licenca

Projekt je licenciran pod Apache-2.0. Pristanak namjernim slanjem zahtjeva za povlačenjem
podnosi se bezuvjetno prema odjeljku 5 licence Apache 2.0,
osim ako pošiljatelj posebno ne naznači drugačije.

Projekt koristi Apache-2.0. Namjernim slanjem doprinosa, podnosite
prema odjeljku 5 licence Apache 2.0 bez dodatnih uvjeta osim ako vi
izričito navesti drugačije.

## Ponašanje

Svi sudionici podliježu [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).
Svi sudionici moraju slijediti [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).
