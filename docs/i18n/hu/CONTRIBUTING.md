# Magyar (hu) — Közreműködés
> [Nyelvválasztó](../../LANGUAGES.md) · Az angol eredeti és a gyökér `LICENSE` irányadó.

Nyiss/hivatkozz issue-t; nagy API/licenc/architektúraváltást előbb beszélj meg; securityt privátan jelents. JDK 21+, Maven 3.9+, Git; futtasd: `./mvnw --batch-mode --no-transfer-progress clean verify`. Fork, rövid branch a `main`-ről, fókuszált módosítás, minden javításhoz determinisztikus regresszió és kitöltött PR-sablon.

Maradjon kicsi/típusbiztos és dependency-free a publikus API; `Unavailable` soha nem `Invalid`; megosztott állapot korlátos; publikus/konkurens logika HU+EN dokumentált; privát adó-/cégadat nem commitolható. Teszt nem hív publikus VIES-t, race latch/barrierrel vezérelt. PR csak zöld builddel, dokumentációval, indokolt függőséggel, jelzett breaking change-dzsel és reprodukálható teljesítménnyel kész. AI megengedett, de minden sorért a beküldő felel; jelentős használatot jelezni kell. Beküldés Apache-2.0 §5 szerint; magatartás: `CODE_OF_CONDUCT.md`.
