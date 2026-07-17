# Hrvatski (hr) — Dnevnik promjena

> [Svi jezici](../../LANGUAGES.md) · Informativni prijevod. U slučaju razlike mjerodavan je kanonski engleski tehnički ili pravni izvor. Samo su korijenski `LICENSE` i `NOTICE` pravno mjerodavni; prijevod ih ne zamjenjuje.

Značajne promjene dokumentirane su u ovoj datoteci. Projekt je semantički
slijedi verzije:`MAJOR.MINOR.PATCH`.

Ovdje su dokumentirane sve značajne promjene. Projekt slijedi semantičko verzioniranje:
`MAJOR.MINOR.PATCH`.

## [Neobjavljeno]

## [1.2.0] - 2026-07-17

### Dodano

- Zdravstvene datoteke GitHub zajednice, CI/sigurnosna automatizacija i upravljanje otvorenim kodom.
- Licenciranje projekta Apache License 2.0 i metapodaci Mavena.
- Paketi dokumentacije na sva 24 službena jezika EU.
- GitHub Sponsor/Buy Me a Coffee integracija i višejezični uvjeti otkrivanja.

### Promijenjeno

- Testni lanac alata ažuriran na JUnit Jupiter 6.1.2 i trenutne stabilne Maven dodatke.
- Završetak isključivanja sada koristi virtualne niti, sprječavajući pojačanje izvorne niti
  kada se mnoge operacije zatvaraju istovremeno.

## [1.0.0] - 2026-07-17

### Dodano

- Java 21, VIES REST klijent bez ovisnosti o vremenu izvođenja.
- Sinkroni i asinkroni API-ji sa zadanim postavkama virtualne niti.
- Ograničena sinkronizacija, asinkronacija i ulaz u izlaznu mrežu.
- JVM-lokalno spajanje jednokratnih zahtjeva.
- Ograničena TTL predmemorija i vanjska točka proširenja `ViesCache`.
- Stroga provjera valjanosti odgovora i zapečaćena hijerarhija rezultata.
- Stabilne dvojezične mađarsko/engleske strukturirane pogreške.
- Pokušajte ponovno s eksponencijalnim odmakom i podrhtavanjem.
- Testovi determinističke jedinice, HTTP-a, konkurentnosti, otkazivanja i isključivanja.
- Instalacijska, integracijska, tehnička i ispitna dokumentacija.
