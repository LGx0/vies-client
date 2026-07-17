# Eesti (et) — Panustamine
> [Keelevalik](../../LANGUAGES.md) · Määravad ingliskeelne tehniline ja õiguslik originaal ning juure `LICENSE`/`NOTICE`.

Ava või viita issue; aruta suurt API/litsentsi/arhitektuuri muudatust enne; turve teata privaatselt. JDK 21+, Maven 3.9+, Git; käivita `./mvnw --batch-mode --no-transfer-progress clean verify`. Fork, lühike branch `main`-ist, fokuseeritud muudatus ja deterministlik regressioon iga bugi jaoks. Hoia API väike/tüübiturvaline ja runtime sõltuvusteta; `Unavailable` ei muutu `Invalid`-iks; jagatud olek on lõimekindel ja piiratud. Test ei kutsu avalikku VIES-i, race kasutab latch/barrier'it. Dokumenteeri avalik käitumine, breaking change, sõltuvus ja reprodutseeritav performance. AI on lubatud, kuid esitaja kontrollib/testib/litsentsib kõik ja avaldab olulise kasutuse. Contributions Apache-2.0 §5 järgi; järgi `CODE_OF_CONDUCT.md`.

