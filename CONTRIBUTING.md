# Közreműködés / Contributing

Köszönjük, hogy javítanád a `vies-client` projektet. A cél egy kiszámítható,
függőségmentes és nagy terhelés mellett is biztonságos Java VIES-kliens.

Thank you for improving `vies-client`. The project aims to remain predictable,
dependency-free at runtime, and safe under high concurrency.

## Mielőtt elkezded / Before starting

- Hibajavításhoz nyiss issue-t vagy hivatkozz egy meglévőre.
- Nagy API-, licenc- vagy architektúraváltást előbb beszéljünk meg issue-ban.
- Biztonsági hibát ne publikus issue-ban jelents; lásd [SECURITY.md](SECURITY.md).
- Open or reference an issue for bug fixes.
- Discuss major API, licensing, or architecture changes before implementation.
- Never disclose a vulnerability in a public issue; follow [SECURITY.md](SECURITY.md).

## Fejlesztői környezet / Development environment

Szükséges: JDK 21+, Maven 3.9+, Git. Ellenőrzés:

```bash
java -version
javac -version
mvn -version
```

Teljes helyi ellenőrzés / Full local verification:

```bash
mvn --batch-mode --no-transfer-progress clean verify
```

Részletes telepítés: [docs/INSTALLATION.md](docs/INSTALLATION.md).

## Fejlesztési folyamat / Development workflow

1. Forkold a repót, majd hozz létre rövid életű branchet a `main` ágról.
2. Készíts kicsi, egy célra fókuszáló módosítást.
3. Minden hibajavításhoz adj determinisztikus regressziós tesztet.
4. Futtasd a teljes `mvn clean verify` parancsot.
5. Nyiss pull requestet, és töltsd ki a sablon minden releváns részét.

1. Fork the repository and create a short-lived branch from `main`.
2. Keep changes small and focused.
3. Add a deterministic regression test for every bug fix.
4. Run the full Maven verification.
5. Open a pull request and complete the provided template.

Ajánlott branchnevek / Suggested branch names:

```text
fix/close-race
feat/cache-adapter-hook
docs/redis-example
```

## Kódszabályok / Coding rules

- Java 21 nyelvi szint; a publikus API maradjon kicsi és típusbiztos.
- A futásidejű modul maradjon külső függőség nélkül, hacsak külön döntés nem születik.
- `Unavailable` technikai bizonytalanság, soha nem alakítható `Invalid` eredménnyé.
- Minden megosztott állapot legyen szálbiztos és memóriában korlátos.
- Publikus API-hoz és összetett konkurens logikához angol és magyar Javadoc/komment kell.
- Ne logolj teljes adószámot, cégnevet, címet vagy saját requester-adószámot tesztadatként.
- Use Java 21 and keep the public API small and type-safe.
- Keep the runtime module dependency-free unless explicitly agreed otherwise.
- Never convert `Unavailable` into `Invalid`.
- Shared state must be thread-safe and memory-bounded.
- Document public API and non-obvious concurrency logic in English and Hungarian.
- Do not commit or log private VAT, company, address, or requester data.

## Tesztszabályok / Testing rules

- Unit teszt ne használjon publikus hálózatot.
- HTTP és konkurenciateszt kizárólag loopback mock szervert hívhat.
- A publikus VIES ellen load-, stressz- vagy CI-teszt tilos.
- Versenyhelyzetet latch/barrier vezéreljen; fix `Thread.sleep` nem lehet helyességi orákulum.
- A tesztnek a javítás nélkül el kellene buknia.
- Unit tests must not use the public network.
- HTTP and concurrency tests may only call a loopback mock server.
- Never run load, stress, or required CI tests against public VIES.
- Drive races with latches/barriers; fixed sleeps are not correctness oracles.
- A regression test must fail without the corresponding fix.

Tesztkatalógus: [docs/TESTING.md](docs/TESTING.md).

## Pull request követelmények / Pull-request requirements

A PR akkor kész review-ra, ha:

- a build és minden teszt sikeres;
- a publikus viselkedés dokumentált;
- nincs új, indokolatlan függőség;
- a kompatibilitástörés külön ki van emelve;
- a teljesítményállításhoz reprodukálható helyi mérés tartozik;
- a szerző jogosult a kódot beküldeni.

A PR is ready when the build is green, public behavior is documented, new
dependencies are justified, breaking changes are explicit, performance claims are
reproducible, and the author has the right to submit the work.

## AI-val támogatott módosítás / AI-assisted changes

AI használható, de a beküldő teljes felelősséget vállal az eredményért. Minden sort
ellenőrizni, tesztelni és licencelhetőség szempontjából átnézni kell. Jelentős AI-
közreműködést jelezz a PR-ben; bizalmas vagy harmadik fél által védett adatot ne adj
modellnek és ne küldj be ellenőrizetlen generált kódot.

AI tools are allowed, but the contributor remains fully responsible. Review and test
every line, verify provenance and licensing, disclose substantial AI assistance in
the PR, and never submit confidential or unreviewed generated material.

## Licenc / License

A projekt Apache-2.0 licencű. A pull request szándékos elküldésével a hozzájárulás
az Apache License 2.0 5. szakasza szerint, külön feltétel nélkül kerül beküldésre,
hacsak a beküldő ezt kifejezetten másként nem jelöli.

The project uses Apache-2.0. By intentionally submitting a contribution, you submit
it under Section 5 of the Apache License 2.0 without additional terms unless you
explicitly state otherwise.

## Magatartás / Conduct

Minden résztvevőre a [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) vonatkozik.
All participants must follow [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).
