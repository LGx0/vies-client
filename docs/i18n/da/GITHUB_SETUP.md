# Dansk (da) — GITHUB_SETUP

> [Alle sprog](../../LANGUAGES.md) · Informativ oversættelse. Ved afvigelser er den kanoniske engelske tekniske eller juridiske kilde gældende. Kun `LICENSE` og `NOTICE` i roden er juridisk autoritative; oversættelsen erstatter dem ikke.

## 1. Obligatorisk kontrol før offentliggørelse

- Bekræft, at du har ret til at offentliggøre kilde, dokumentation og navn.
- Scan hele biblioteket og senere hele Git-historien for hemmeligheder.
- Fjern kundedata, reelt anmoder-skattenummer, token,`.env`-fil og IDE
  maskinspecifik fil.
- Tjek Apache-2.0`LICENSE`,`NOTICE`og tredjeparts meddelelsesfil.
- Kørsel:`./mvnw --batch-mode --no-transfer-progress clean verify`.

- Bekræft retten til at offentliggøre al kode, dokumentation og projektnavngivning.
- Scan biblioteket og eventuel Git-historik for hemmeligheder.
- Fjern kundedata, reelle anmoder-momsnumre, tokens,`.env`og maskinfiler.
- Bekræft Apache-2.0 og tredjepartsmeddelelser.
- Kør den fulde Maven-verifikation.

## 2. Opret lageret / Opret lageret

Projektet inkluderer allerede en README, licens og fil`.gitignore`, derfor GitHub
generer ikke nye, når du opretter web. Foreslået navn:`vies-client`, synlighed:
`Public`, standardgren:`main`.

Projektet indeholder allerede README-, licens- og ignoreringsfiler; ikke generere
dubletter på GitHub. Foreslået navn:`vies-client`, synlighed:`Public`, standard
filial:`main`.

Kommando, der kan bruges efter bekræftelse af den nøjagtige ejer:

```bash
git init -b main
git add --all
git commit -m "Initial open-source release"
gh repo create OWNER/vies-client --public --source . --remote origin --push
```

Kør ikke kommandoen uden at erstatte`OWNER`og kontrollere den fulde trinvise diff.
Kør det ikke, før du udskifter`OWNER`og har gennemgået den komplette trinvise diff.

## 3. Lagringsindstillinger

Anbefalet:

- Problemer: aktiveret;
- Diskussioner: aktiveret til brugsspørgsmål;
- Wikier: deaktiveret, medmindre de aktivt vedligeholdes;
- Projekter: valgfrit;
- Bevar dette lager: valgfrit efter den første stabile udgivelse;
- Slet automatisk hovedgrene: aktiveret;
- Privat sårbarhedsrapportering: aktiveret;
- Dependabot-advarsler og sikkerhedsopdateringer: aktiveret;
- Hemmelig scanning og push-beskyttelse: aktiveret, hvor GitHub tilbyder dem.

## 4. Branch protection or ruleset / Branch protection or ruleset

En`main`filial:

- pull anmodning påkrævet;
- mindst én godkendelse / mindst én godkendelse;
- gammel godkendelse afskedigelse efter nye tilsagn;
- påkrævede kontroller:`CI`,`CodeQL`,`Dependency review`som tilgængelig;
- samtaleopløsning påkrævet;
- Tving push og sletning deaktiveret;
- admin bypass bruges kun til nødsituationer;
- underskrevne commits/tags anbefales til udgivelser.

Før det første tryk eksisterer navnet på den nødvendige kontrol endnu ikke; køre arbejdsgangen én gang
kat, majd állítsd være et regelsæt. / Nødvendige kontrolnavne vises efter den første arbejdsgang
løbe; konfigurere regelsættet efterfølgende.

## 5. Etiketter og emner

Foreslåede tags:

```text
bug, enhancement, documentation, localization, security, dependencies,
performance, concurrency, breaking-change, good-first-issue, help-wanted, triage
```

Foreslået lagerbeskrivelse:

```text
Fast Java 21 VIES VAT checker and EU VAT number validator with virtual threads, bounded concurrency, cache, and bilingual errors.
```

Foreslåede emner:

```text
java, java21, java-library, vies, vat, vat-number, vat-checker, vat-validator,
vat-validation, vat-number-validation, eu-vat, tax-id, tax-id-checker,
tax-id-validation, rest-client, virtual-threads, single-flight, jpms,
zero-dependency
```

## 6. Doner/sponsor-knap

GitHub viser knappen **Sponsor** baseret på`.github/FUNDING.yml`. Kun den
indtaste en vedligeholder-verificeret finansiel URL. Eksempel:

```yaml
buy_me_a_coffee: lgx0
```

En dedikeret nøgle kan også bruges med en understøttet udbyder, for eksempel`github`,`ko_fi`
eller`custom`. En donation udgør ikke en SLA eller en fortrinsret; se`SUPPORT.md`.

GitHub viser Sponsor-knappen fra`.github/FUNDING.yml`. Konfigurer kun en
finansieringsdestination verificeret af vedligeholderen. Donationer køber ikke en SLA eller
ledelsesrettigheder.

## 7. Opdateringer efter første tryk / Opdateringer efter første tryk

Når du kender den faktiske GitHub-URL, skal du opdatere:

- `pom.xml`:`url`,`scm`,`developers`;
- README: CI- og CodeQL-badge-URL'er;
- `SECURITY.md`: privat rådgivnings-URL, hvis nødvendigt;
- frigivelsesdokumentation og Maven Central-koordinater;
- `.github/FUNDING.yml`:`https://buymeacoffee.com/lgx0`.

## 8. Første udgivelse / Første udgivelse

Opret kun et signeret`v1.0.0`-tag efter grøn CI/CodeQL. Udgivelsens workflowtest,
byg binære/kilder/Javadoc JAR'er og SHA-256 kontrolsum, derefter GitHub
Opretter en udgivelse. Detaljer: [RELEASING.md](RELEASING.md).
