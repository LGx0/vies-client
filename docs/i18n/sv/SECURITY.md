# Svenska (sv) — SECURITY

> [Alla språk](../../LANGUAGES.md) · Informativ översättning. Vid avvikelser gäller den kanoniska engelska tekniska eller juridiska källan. Endast `LICENSE` och `NOTICE` i roten är juridiskt auktoritativa; översättningen ersätter dem inte.

## Stödda versioner / Stödda versioner

| Version / Version          | Säkerhetsfixar / Säkerhetsfixar                     |
| -------------------------- | --------------------------------------------------- |
| senaste`1.x`/ senaste`1.x` | ja / ja                                             |
| äldre utgåvor              | endast med ett separat beslut / från fall till fall |

När projektet startar, uppdatera alltid till den senaste versionen. Stödet
matrisen kan ändras i senare större versioner.

Under den inledande projektfasen, uppdatera till den senaste versionen. Denna policy kan
ändras när ytterligare större versioner finns.

## Säkerhetsmeddelande / Rapportering av en sårbarhet

Öppna inte ett offentligt problem eller lägg inte upp ett utnyttjande innan det är åtgärdat.

1. Använd gränssnittet **Säkerhet → Råd → Rapportera en sårbarhet** för GitHub-repo.
2. Specificera påverkad version, miljö, reproduktion och potentiell påverkan.
3. Om möjligt, ge ett minimalt proof-of-concept utan riktiga person- eller skatteuppgifter.
4. Ange om det finns en känd lösning eller en föreslagen lösning.

Öppna inte ett offentligt problem eller avslöja ett utnyttjande innan en samordnad åtgärd.

1. Använd **Säkerhet → Råd → Rapportera en sårbarhet** i GitHub-förvaret.
2. Inkludera berörda versioner, miljö, reproduktionssteg och potentiell påverkan.
3. Ge ett minimalt bevis på konceptet utan riktiga person- eller skatteuppgifter när det är möjligt.
4. Inkludera kända lösningar eller en föreslagen korrigering, om tillgänglig.

## Svarstid / Svarsmål

- Bekräftelse på ankomst: inom 7 dagar om möjligt.
- Första bedömning och nästa steg: inom 14 dagar om möjligt.
- Fixa och publicera: baserat på svårighetsgrad och komplexitet, samordnat.

- Bekräftelse: mål inom 7 dagar.
- Inledande bedömning och nästa steg: mål inom 14 dagar.
- Fix och avslöjande: samordnas efter svårighetsgrad och komplexitet.

Dessa är mål, inte avtalsenliga SLA. / Dessa är mål, inte kontraktuella SLA.

## Omfattning

Särskilt relevant: URI/inputinjektion, TLS eller kringgående av endpoint, känslig data
läckage, cacheförgiftning, obehörig begärandedata, obegränsat minne/trådar/
anslutningstillväxt, dödläge avstängning, felaktigt`Invalid`-beslut vid tekniskt fel.

Högvärdiga rapporter inkluderar injektion, TLS/slutpunktsbypass, exponering för känslig data,
cacheförgiftning, missbruk av begärandedata, obegränsad resurstillväxt, dödläge för avstängning,
eller felaktigt returnerar`Invalid`för ett tekniskt fel.

Bortfallet, strypningen eller datakvaliteten för uppströms VIES i sig är inte en
klientbiblioteks sårbarhet. / Uppströms tillgänglighet, strypning eller medlemsland
enbart datakvalitet är inte en sårbarhet i det här biblioteket.
