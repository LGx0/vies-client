# Svenska (sv) — GITHUB_SETUP

> [Alla språk](../../LANGUAGES.md) · Informativ översättning. Vid avvikelser gäller den kanoniska engelska tekniska eller juridiska källan. Endast `LICENSE` och `NOTICE` i roten är juridiskt auktoritativa; översättningen ersätter dem inte.

## 1. Obligatoriska kontroller före publicering

- Bekräfta att du har rätt att publicera källa, dokumentation och namn.
- Skanna hela katalogen och senare hela Git-historiken efter hemligheter.
- Ta bort kunddata, verkligt skattenummer för begäranden, token,`.env`-fil och IDE
  maskinspecifik fil.
- Kontrollera Apache-2.0`LICENSE`,`NOTICE`och tredje parts meddelandefil.
- Kör:`./mvnw --batch-mode --no-transfer-progress clean verify`.

- Bekräfta rätten att publicera all kod, dokumentation och projektnamn.
- Skanna katalogen och eventuell Git-historik efter hemligheter.
- Ta bort kunddata, riktiga begärande momsnummer, tokens,`.env`och maskinfiler.
- Verifiera Apache-2.0 och meddelanden från tredje part.
- Kör hela Maven-verifieringen.

## 2. Skapa arkivet / Skapa arkivet

Projektet innehåller redan en README, licens och fil`.gitignore`, därav GitHub
generera inte nya när du skapar webb. Föreslaget namn:`vies-client`, synlighet:
`Public`, standardgren:`main`.

Projektet innehåller redan README-, licens- och ignoreringsfiler; genererar inte
dubbletter på GitHub. Föreslaget namn:`vies-client`, synlighet:`Public`, standard
filial:`main`.

Kommando som kan användas efter att ha bekräftat den exakta ägaren:

```bash
git init -b main
git add --all
git commit -m "Initial open-source release"
gh repo create OWNER/vies-client --public --source . --remote origin --push
```

Kör inte kommandot utan att ersätta`OWNER`och kontrollera den fullständiga diff.
Kör den inte innan du byter ut`OWNER`och granskar hela stegdiff.

## 3. Lagringsinställningar

Rekommenderad:

- Problem: aktiverat;
- Diskussioner: aktiverat för användningsfrågor;
- Wikis: inaktiverad om de inte underhålls aktivt;
- Projekt: frivilligt;
- Bevara detta förråd: valfritt efter den första stabila utgåvan;
- Ta bort huvudgrenar automatiskt: aktiverat;
- Privat sårbarhetsrapportering: aktiverad;
- Dependabot-varningar och säkerhetsuppdateringar: aktiverat;
- Hemlig skanning och push-skydd: aktiverat där GitHub erbjuder dem.

## 4. Grenskydd or regeluppsättning / Grenskydd eller regeluppsättning

En`main`-gren:

- pull begäran krävs;
- minst ett godkännande / minst ett godkännande;
- inaktuellt godkännande uppsägning efter nya åtaganden;
- obligatoriska kontroller:`CI`,`CodeQL`,`Dependency review`som tillgängliga;
- Konversationsupplösning krävs;
- tvinga push och radering inaktiverad;
- admin bypass används endast för nödsituationer;
- Undertecknade åtaganden/taggar rekommenderas för releaser.

Före den första tryckningen finns namnet på den nödvändiga kontrollen ännu inte; kör arbetsflödet en gång
kat, majd állítsd be a rulesetet. / Obligatoriska kontrollnamn visas efter det första arbetsflödet
sikt; konfigurera regeluppsättningen efteråt.

## 5. Etiketter och ämnen

Föreslagna taggar:

```text
bug, enhancement, documentation, localization, security, dependencies,
performance, concurrency, breaking-change, good-first-issue, help-wanted, triage
```

Föreslagen förvarsbeskrivning:

```text
Fast Java 21 VIES VAT checker and EU VAT number validator with virtual threads, bounded concurrency, cache, and bilingual errors.
```

Föreslagna ämnen:

```text
java, java21, java-library, vies, vat, vat-number, vat-checker, vat-validator,
vat-validation, vat-number-validation, eu-vat, tax-id, tax-id-checker,
tax-id-validation, rest-client, virtual-threads, single-flight, jpms,
zero-dependency
```

## 6. Donera/sponsor-knapp

GitHub visar knappen **Sponsor** baserat på`.github/FUNDING.yml`. Endast den
ange en förvaltarverifierad ekonomisk URL. Exempel:

```yaml
buy_me_a_coffee: lgx0
```

En dedikerad nyckel kan också användas med en leverantör som stöds, till exempel`github`,`ko_fi`
eller`custom`. En donation utgör inte en SLA eller en företrädesrätt; se`SUPPORT.md`.

GitHub visar sponsorknappen från`.github/FUNDING.yml`. Konfigurera endast en
finansieringsdestination verifierad av underhållaren. Donationer köper inte en SLA eller
styrande rättigheter.

## 7. Uppdateringar efter första trycket / Uppdateringar efter första trycket

När du känner till den faktiska GitHub URL, uppdatera:

- `pom.xml`:`url`,`scm`,`developers`;
- README: URL-adresser för CI- och CodeQL-märket;
- `SECURITY.md`: privat rådgivande URL, om nödvändigt;
- släppdokumentation och Maven Central-koordinater;
- `.github/FUNDING.yml`:`https://buymeacoffee.com/lgx0`.

## 8. Första utgåvan / Första utgåvan

Skapa en signerad`v1.2.0`-tagg först efter grön CI/CodeQL. Utgivningens arbetsflödestester,
bygg binary/sources/Javadoc JARs och SHA-256 checksumma, sedan GitHub
Skapar en release. Detaljer: [RELEASING.md](RELEASING.md).
