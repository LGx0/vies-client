# Polski (pl) — GitHub

**Języki:** [English](../../GITHUB_SETUP.md) · [Polski](GITHUB_SETUP.md) · [Dokumentacja](README.md)

> Tłumaczenie informacyjne; obowiązuje angielski oryginał. Jedyną wiążącą licencją jest [LICENSE](../../../LICENSE); tłumaczenie nie jest licencją.

Przed publikacją potwierdź prawa, zeskanuj katalog/Git history, usuń customer/requester data, tokeny, `.env` i machine files, sprawdź LICENSE/NOTICE/third-party, wykonaj `./mvnw --batch-mode --no-transfer-progress clean verify`.

Utwórz public `vies-client` bez nowego README/licence/gitignore. Po zmianie OWNER i review staged diff:

```bash
git init -b main
git add --all
git commit -m "Initial open-source release"
gh repo create OWNER/vies-client --public --source . --remote origin --push
```

Włącz Issues, Discussions, private vulnerability, Dependabot, secret scanning/push protection, auto-delete. Na `main`: PR, approval, stale dismissal, CI/CodeQL/dependency review, resolved conversations, bez force push/delete. Checks po pierwszym workflow.

Labels: `bug, enhancement, documentation, localization, security, dependencies, performance, concurrency, breaking-change, good-first-issue, help-wanted, triage`. Topics: `java, java21, vies, vat, vat-validation, eu, rest-client, virtual-threads, single-flight, jpms, zero-dependency`.

Sponsor przez `.github/FUNDING.yml`, np. `custom: [https://buymeacoffee.com/ACCOUNT]`; tylko zweryfikowany URL, bez SLA/governance. Po push popraw pom metadata, badges, advisory, release/Central i funding. Signed `v1.2.0` dopiero po green CI/CodeQL.

## Szczegółowe zasady zarządzania projektem

Projekt używa jednej licencji Apache-2.0. Tłumaczenia licencji nie są prawnie wiążące; rozstrzygają główny angielski `LICENSE` oraz odpowiedni `NOTICE`. Przed publikacją lub przyjęciem wkładu należy sprawdzić pochodzenie kodu, prawa autora, możliwe prawa pracodawcy lub klienta, brak sekretów i danych osobowych oraz zgodność licencji materiałów stron trzecich. Projekt jest niezależny i nie stanowi produktu ani rekomendacji Komisji Europejskiej, Unii Europejskiej czy administracji podatkowej.

Zmiana rozpoczyna się od issue lub dyskusji, szczególnie gdy dotyczy publicznego API, licencji, modelu concurrency albo architektury. Contributor tworzy krótki branch z `main`, utrzymuje mały zakres, dodaje deterministyczny regression test i wykonuje `./mvnw --batch-mode --no-transfer-progress clean verify`. Unit test nie korzysta z publicznej sieci; test HTTP/concurrency używa wyłącznie loopback mocka. Load i stress przeciw publicznemu VIES są zabronione. Race synchronizuje latch/barrier, nie stały sleep.

Publiczne API pozostaje małe, type-safe i zgodne z Java 21. Moduł runtime nie otrzymuje nowej dependency bez wyraźnej decyzji. `Unavailable` nigdy nie staje się `Invalid`, shared state jest thread-safe i memory-bounded, a złożona logika concurrency ma komentarze angielskie i węgierskie. Narzędzia AI są dozwolone, lecz autor odpowiada za każdą linię, testy, pochodzenie i licencję, ujawnia istotną pomoc w PR i nie przekazuje poufnych danych.

Problemu bezpieczeństwa nie zgłasza się publicznym issue. Należy użyć GitHub Security Advisory, podając wersję, środowisko, reprodukcję, wpływ i minimalny PoC bez prawdziwych danych podatkowych lub osobowych. Cel to potwierdzenie w siedem dni i pierwsza ocena w czternaście dni; nie jest to umowne SLA. Istotne są injection, TLS/endpoint bypass, data leak, cache poisoning, nadużycie requester data, nieograniczony wzrost zasobów, shutdown deadlock i błędne `Invalid` przy awarii technicznej.

W repozytorium GitHub włącz Issues, Discussions, private vulnerability reporting, Dependabot, secret scanning oraz push protection. `main` chronią PR, co najmniej jedno approval, dismissal starego approval, resolved conversations oraz CI, CodeQL i dependency review. Force push i usunięcie brancha są zabronione. Release używa najlepiej podpisanego annotated tag, zawiera binary, sources, Javadoc JAR i SHA-256. Tokeny i klucze GPG są wyłącznie w Actions secrets z least privilege.

Wsparcie społeczności jest best effort i nie stanowi porady podatkowej, prawnej ani księgowej. Darowizny przez Buy Me a Coffee są dobrowolne i nie kupują priorytetu, SLA, wyjątku bezpieczeństwa ani prawa governance. Uczestnicy są rzeczowi i szanują innych, chronią prywatne informacje i krytykują kod, nie człowieka. Nękanie, dyskryminacja, doxxing, oszustwo, przywłaszczenie autorstwa oraz nieodpowiedzialne disclosure mogą skutkować usunięciem treści lub ograniczeniem udziału.
