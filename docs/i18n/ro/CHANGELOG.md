# Română (ro) — Jurnal de modificări

> [Toate limbile](../../LANGUAGES.md) · Traducere informativă. În caz de diferență prevalează sursa canonică tehnică sau juridică în limba engleză. Numai `LICENSE` și `NOTICE` din rădăcină sunt texte juridice oficiale; traducerea nu le înlocuiește.

În acest fișier sunt documentate modificări semnificative. Proiectul este semantic
urmează versiunea:`MAJOR.MINOR.PATCH`.

Toate modificările notabile sunt documentate aici. Proiectul urmează versiunea semantică:
`MAJOR.MINOR.PATCH`.

## [Nelansat]

## [1.2.0] - 2026-07-17

### Adăugat

- Fișiere de sănătate ale comunității GitHub, automatizare CI/securitate și guvernare open-source.
- Licență pentru proiecte Apache License 2.0 și metadate Maven.
- Pachete de documentație în toate cele 24 de limbi oficiale ale UE.
- GitHub Sponsor/Buy Me a Coffee Integrarea și termenii de descoperire multilingv.

### Schimbat

- Lanțul de instrumente de testare actualizat la JUnit Jupiter 6.1.2 și la pluginurile Maven stabile actuale.
- Finalizarea opririi folosește acum fire virtuale, prevenind amplificarea firelor native
  când mai multe operațiuni sunt închise simultan.

## [1.0.0] - 2026-07-17

### Adăugat

- Java 21, client VIES REST cu dependență zero de rulare.
- API-uri sincrone și asincrone cu valori implicite pentru fire virtuale.
- Sincronizare limitată, asincronizare și admitere în rețea de ieșire.
- Coalisarea cererilor de zbor unic JVM-local.
- Cache TTL limitat și punct de extensie extern `ViesCache`.
- Validare strictă a răspunsului și ierarhie sigilată a rezultatelor.
- Erori structurate bilingve stabile maghiară/engleză.
- Reîncercați cu backoff exponențial și jitter.
- Teste de unități deterministe, HTTP, concurență, anulare și oprire.
- Documentație de instalare, integrare, tehnică și testare.
