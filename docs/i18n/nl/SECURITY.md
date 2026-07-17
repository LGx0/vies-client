# Nederlands (nl) — SECURITY

> [Alle talen](../../LANGUAGES.md) · Informatieve vertaling. Bij verschillen is de canonieke Engelse technische of juridische bron leidend. Alleen `LICENSE` en `NOTICE` in de hoofdmap zijn juridisch gezaghebbend; deze vertaling vervangt ze niet.

## Ondersteunde versies / Ondersteunde versies

| Versie / Versie              | Beveiligingsoplossingen / Beveiligingsoplossingen |
| ---------------------------- | ------------------------------------------------- |
| nieuwste`1.x`/ nieuwste`1.x` | ja / ja                                           |
| oudere uitgaven              | alleen bij afzonderlijk besluit / geval per geval |

Wanneer het project start, update dan altijd naar de nieuwste release. De steun
matrix kan in latere hoofdversies veranderen.

Update tijdens de eerste projectfase naar de nieuwste release. Dit beleid mag
veranderen wanneer er aanvullende hoofdversies bestaan.

## Kwetsbaarheidsaankondiging / Een kwetsbaarheid melden

Open geen openbaar probleem en plaats geen exploit voordat het probleem is opgelost.

1. Gebruik de interface **Beveiliging → Advies → Een kwetsbaarheid melden** van de GitHub-opslagplaats.
2. Specificeer de getroffen versie, omgeving, reproductie en potentiële impact.
3. Geef indien mogelijk een minimale proof-of-concept zonder echte persoonlijke of belastinggegevens.
4. Geef aan of er een bekende oplossing of een voorgestelde oplossing is.

Open geen openbaar probleem en maak geen exploit bekend voordat er een gecoördineerde oplossing is gevonden.

1. Gebruik **Beveiliging → Advies → Een kwetsbaarheid melden** in de GitHub-repository.
2. Vermeld de getroffen versies, omgeving, reproductiestappen en potentiële impact.
3. Zorg indien mogelijk voor een minimale proof of concept zonder echte persoonlijke of belastinggegevens.
4. Voeg bekende oplossingen of een voorgestelde oplossing toe, indien beschikbaar.

## Reactietijd / Reactiedoelen

- Aankomstbevestiging: indien mogelijk binnen 7 dagen.
- Eerste beoordeling en volgende stap: indien mogelijk binnen 14 dagen.
- Fix en publiceer: op basis van ernst en complexiteit, gecoördineerd.

- Bevestiging: doel binnen 7 dagen.
- Eerste beoordeling en volgende stap: doelstelling binnen 14 dagen.
- Fix en openbaarmaking: gecoördineerd op basis van ernst en complexiteit.

Dit zijn doelstellingen, geen contractuele SLA's. / Dit zijn doelstellingen, geen contractuele SLA's.

## Domein

Bijzonder relevant: URI/invoerinjectie, TLS of eindpuntomzeiling, gevoelige gegevens
lekkage, cachevergiftiging, ongeautoriseerde aanvragergegevens, onbeperkt geheugen/threads/
verbindingsgroei, impasse bij afsluiting, onjuiste`Invalid`-beslissing in geval van technische fout.

Hoogwaardige rapporten omvatten injectie, TLS/endpoint bypass, blootstelling aan gevoelige gegevens,
cachevergiftiging, misbruik van gegevens van de aanvrager, grenzeloze groei van hulpbronnen, impasse bij afsluiten,
of het ten onrechte retourneren van de`Invalid`vanwege een technisch defect.

De uitval, beperking of gegevenskwaliteit van de stroomopwaartse VIES is op zichzelf geen probleem
kwetsbaarheid van de clientbibliotheek. / Upstream-beschikbaarheid, beperking of lidstaat
datakwaliteit alleen is geen kwetsbaarheid in deze bibliotheek.
