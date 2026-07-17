# Dansk (da) — SECURITY

> [Alle sprog](../../LANGUAGES.md) · Informativ oversættelse. Ved afvigelser er den kanoniske engelske tekniske eller juridiske kilde gældende. Kun `LICENSE` og `NOTICE` i roden er juridisk autoritative; oversættelsen erstatter dem ikke.

## Understøttede versioner / Understøttede versioner

| Version / Version          | Sikkerhedsrettelser / Sikkerhedsrettelser |
| -------------------------- | ----------------------------------------- |
| seneste`1.x`/ seneste`1.x` | ja / ja                                   |
| ældre udgivelser           | kun med særskilt afgørelse / sag for sag  |

Når projektet starter, skal du altid opdatere til den seneste udgivelse. Støtten
matrix kan ændre sig i senere større versioner.

Under den indledende projektfase skal du opdatere til den seneste udgivelse. Denne politik kan evt
ændres, når der findes flere større versioner.

## Meddelelse om sårbarhed / Rapportering af en sårbarhed

Åbn ikke et offentligt problem eller post en udnyttelse, før det er rettet.

1. Brug grænsefladen **Sikkerhed → Advisories → Rapporter en sårbarhed** i GitHub-reposen.
2. Angiv den berørte version, miljø, reproduktion og potentiel påvirkning.
3. Giv om muligt et minimalt proof-of-concept uden reelle personlige eller skattemæssige data.
4. Angiv, om der er en kendt løsning eller en foreslået rettelse.

Åbn ikke et offentligt problem eller afslør ikke en udnyttelse før en koordineret rettelse.

1. Brug **Sikkerhed → Advisories → Rapporter en sårbarhed** i GitHub-lageret.
2. Inkluder berørte versioner, miljø, reproduktionstrin og potentiel påvirkning.
3. Giv et minimalt proof of concept uden reelle personlige eller skattemæssige data, når det er muligt.
4. Medtag kendte løsninger eller en foreslået rettelse, hvis den er tilgængelig.

## Svartid / svarmål

- Bekræftelse af ankomst: inden for 7 dage, hvis det er muligt.
- Første vurdering og næste trin: inden for 14 dage, hvis det er muligt.
- Ret og publicer: baseret på sværhedsgrad og kompleksitet, koordineret.

- Bekræftelse: mål inden for 7 dage.
- Indledende vurdering og næste trin: mål inden for 14 dage.
- Rettelse og afsløring: koordineret efter sværhedsgrad og kompleksitet.

Disse er mål, ikke kontraktlige SLA'er. / Disse er mål, ikke kontraktlige SLA'er.

## Omfang

Særligt relevant: URI/input-injektion, TLS eller endepunktsomgåelse, følsomme data
lækage, cacheforgiftning, uautoriserede anmoderdata, ubegrænset hukommelse/tråde/
forbindelsesvækst, shutdown deadlock, forkert`Invalid`beslutning i tilfælde af teknisk fejl.

Rapporter af høj værdi omfatter injektion, TLS/slutpunktsbypass, eksponering af følsomme data,
cacheforgiftning, misbrug af anmoderdata, ubegrænset ressourcevækst, shutdown deadlock,
eller ukorrekt returnering af`Invalid`for en teknisk fejl.

Frafaldet, droslingen eller datakvaliteten af ​​upstream VIES i sig selv er ikke en
klientbibliotekssårbarhed. / Upstream tilgængelighed, drosling eller medlemsland
datakvalitet alene er ikke en sårbarhed i dette bibliotek.
