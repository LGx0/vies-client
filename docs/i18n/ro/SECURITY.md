# Română (ro) — Politica de securitate

> [Toate limbile](../../LANGUAGES.md) · Traducere informativă. În caz de diferență prevalează sursa canonică tehnică sau juridică în limba engleză. Numai `LICENSE` și `NOTICE` din rădăcină sunt texte juridice oficiale; traducerea nu le înlocuiește.

## Versiuni acceptate

| Versiune / Versiune                        | Remedieri de securitate / Remedieri de securitate |
| ------------------------------------------ | ------------------------------------------------- |
| cel mai recent `1.x`/ cel mai recent `1.x` | da / da                                           |
| versiuni mai vechi                         | numai cu o decizie separată / caz cu caz          |

Când proiectul începe, actualizați întotdeauna la cea mai recentă versiune. Sprijinul
matricea se poate modifica în versiunile majore ulterioare.

În timpul fazei inițiale a proiectului, actualizați la cea mai recentă versiune. Această politică poate
modificați atunci când există versiuni majore suplimentare.

## Anunț de vulnerabilitate

Nu deschideți o problemă publică și nu publicați un exploit înainte de a fi remediat.

1. Utilizați interfața **Securitate → Avizele → Raportați o vulnerabilitate** a depozitului GitHub.
2. Specificați versiunea afectată, mediul, reproducerea și impactul potențial.
3. Dacă este posibil, oferiți o dovadă minimă de concept fără date personale sau fiscale reale.
4. Indicați dacă există o soluție cunoscută sau o remediere sugerată.

Nu deschideți o problemă publică și nu dezvăluiți un exploit înainte de o remediere coordonată.

1. Utilizați **Securitate → Avizele → Raportați o vulnerabilitate** în depozitul GitHub.
2. Includeți versiunile afectate, mediul, pașii de reproducere și impactul potențial.
3. Oferiți o dovadă minimă a conceptului fără date personale sau fiscale reale, atunci când este posibil.
4. Includeți soluții alternative cunoscute sau o remediere propusă, dacă este disponibilă.

## Timp de răspuns

- Confirmarea sosirii: în termen de 7 zile dacă este posibil.
- Prima evaluare și următorul pas: în 14 zile dacă este posibil.
- Fixați și publicați: pe baza severității și complexității, coordonat.

- Confirmare: țintă în 7 zile.
- Evaluare inițială și următorul pas: țintă în 14 zile.
- Remediere și dezvăluire: coordonate în funcție de severitate și complexitate.

Acestea sunt ținte, nu SLA-uri contractuale. / Acestea sunt ținte, nu SLA-uri contractuale.

## Domeniul de aplicare

Deosebit de relevante: URI/injectare de intrare, TLS sau eludarea punctului final, date sensibile
scurgeri, otrăvire a memoriei cache, date neautorizate ale solicitantului, memorie/thread-uri nelimitate/
creșterea conexiunii, blocaj de închidere, decizie incorectă `Invalid` în caz de eroare tehnică.

Rapoartele de mare valoare includ injectarea, TLS/bypass-ul punctului final, expunerea la date sensibile,
otrăvirea memoriei cache, utilizarea greșită a datelor solicitantului, creșterea nelimitată a resurselor, blocarea închiderii,
sau returnarea incorectă a `Invalid` pentru o defecțiune tehnică.

Renunțarea, limitarea sau calitatea datelor VIES din amonte în sine nu este a
vulnerabilitatea bibliotecii client. / Disponibilitate în amonte, limitare sau stat membru
Doar calitatea datelor nu este o vulnerabilitate în această bibliotecă.
