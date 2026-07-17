# Hrvatski (hr) — Sigurnosna pravila

> [Svi jezici](../../LANGUAGES.md) · Informativni prijevod. U slučaju razlike mjerodavan je kanonski engleski tehnički ili pravni izvor. Samo su korijenski `LICENSE` i `NOTICE` pravno mjerodavni; prijevod ih ne zamjenjuje.

## Podržane verzije

| Verzija / Verzija                | Sigurnosni popravci / Sigurnosni popravci     |
| -------------------------------- | --------------------------------------------- |
| najnoviji `1.x`/ najnoviji `1.x` | da / da                                       |
| starija izdanja                  | samo posebnom odlukom / od slučaja do slučaja |

Kad projekt počinje, uvijek ažurirajte na najnovije izdanje. Podrška
matrica se može promijeniti u kasnijim glavnim verzijama.

Tijekom početne faze projekta ažurirajte na najnovije izdanje. Ova politika može
promijeniti kada postoje dodatne glavne verzije.

## Najava ranjivosti

Nemojte otvarati javni problem ili objavljivati ​​exploit prije nego što se riješi.

1. Koristite **Sigurnost → Savjeti → Prijavi ranjivost** sučelje GitHub repoa.
2. Navedite zahvaćenu verziju, okruženje, reprodukciju i potencijalni utjecaj.
3. Ako je moguće, pružite minimalni dokaz koncepta bez stvarnih osobnih ili poreznih podataka.
4. Navedite postoji li poznato zaobilazno rješenje ili predloženi popravak.

Ne otvarajte javni problem niti otkrivajte exploit prije koordiniranog popravka.

1. Koristite **Sigurnost → Savjeti → Prijavite ranjivost** u GitHub repozitoriju.
2. Uključite zahvaćene verzije, okruženje, korake reprodukcije i potencijalni utjecaj.
3. Pružite minimalan dokaz koncepta bez stvarnih osobnih ili poreznih podataka kada je to moguće.
4. Uključite poznata rješenja ili predloženi popravak, ako je dostupno.

## Vrijeme odgovora

- Potvrda dolaska: unutar 7 dana ako je moguće.
- Prva procjena i sljedeći korak: unutar 14 dana ako je moguće.
- Popravi i objavi: na temelju ozbiljnosti i složenosti, koordinirano.

- Potvrda: cilj u roku od 7 dana.
- Početna procjena i sljedeći korak: cilj unutar 14 dana.
- Popravak i otkrivanje: usklađeno prema ozbiljnosti i složenosti.

Ovo su ciljevi, a ne ugovorni SLA-ovi. / Ovo su ciljevi, a ne ugovorni SLA-ovi.

## Opseg

Posebno relevantno: URI/injekcija unosa, TLS ili zaobilaženje krajnje točke, osjetljivi podaci
curenje, trovanje predmemorije, podaci neovlaštenog zahtjevatelja, neograničena memorija/niti/
rast veze, zastoj pri isključivanju, netočna odluka `Invalid` u slučaju tehničke pogreške.

Izvješća visoke vrijednosti uključuju ubacivanje, TLS/endpoint bypass, izloženost osjetljivim podacima,
trovanje predmemorije, zlouporaba podataka zahtjevatelja, neograničeni rast resursa, zastoj pri isključivanju,
ili neispravno vraćanje `Invalid` zbog tehničkog kvara.

Ispadanje, prigušivanje ili kvaliteta podataka uzvodnog VIES-a samo po sebi nije
ranjivost knjižnice klijenta. / Dostupnost uzvodno, ograničenje ili država članica
sama kvaliteta podataka nije ranjivost u ovoj knjižnici.
