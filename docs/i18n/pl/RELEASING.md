# Polski (pl) — Wydawanie

**Języki:** [English](../../RELEASING.md) · [Čeština](../cs/RELEASING.md) · [Polski](RELEASING.md) · [Slovenčina](../sk/RELEASING.md) · [Slovenščina](../sl/RELEASING.md) · [Hrvatski](../hr/RELEASING.md) · [Română](../ro/RELEASING.md) · [Български](../bg/RELEASING.md) · [Ελληνικά](../el/RELEASING.md)

> Tłumaczenie informacyjne; obowiązuje angielski oryginał. Jedyny wiążący tekst licencji: [LICENSE](../../../LICENSE).

## Wymagania i wersje

Czysty `main`, zielone GitHub Actions, uprawnienia do tag/release, JDK 21, Maven 3.9+ oraz wersja/data w `CHANGELOG.md`. Semantic Versioning: `PATCH` kompatybilna poprawka, `MINOR` kompatybilna funkcja, `MAJOR` złamanie publicznego API/semantyki.

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
jar --describe-module --file target/vies-client-1.2.0.jar
jdeps --print-module-deps target/vies-client-1.2.0.jar
```

Sprawdź pełną historię bez sekretów/danych osobowych, aktualne `LICENSE`, `NOTICE`, `SECURITY.md` i dokumentację, sources/Javadoc JAR, brak obowiązkowego live/load w CI oraz changelog zmian API.

## GitHub Release

Ustaw wersję w `pom.xml`, commitnij wersję/changelog, utwórz najlepiej podpisany tag `git tag -s v1.2.0 -m "v1.2.0"`, potem `git push origin main --follow-tags`. `release.yml` ponownie testuje i dołącza binary/sources/Javadoc. Nie wydawaj nieprzejrzanego lub failing commita.

## Central/Packages i działania po wydaniu

Maven Central wymaga własnego reverse-DNS `groupId`, pełnych metadanych `url`/`scm`/`developers`/distribution management, konta/tokena Central Portal, podpisu GPG i konfiguracji publikacji. Nie commituj tokenów/kluczy; używaj Actions secrets i minimalnych uprawnień.

Po wydaniu zweryfikuj pobrania i SHA-256, otwórz `[Unreleased]`, dla poprawki security opublikuj GitHub Security Advisory i zaktualizuj wersje dependency w dokumentacji.

## Szczegółowe zasady zarządzania projektem

Projekt używa jednej licencji Apache-2.0. Tłumaczenia licencji nie są prawnie wiążące; rozstrzygają główny angielski `LICENSE` oraz odpowiedni `NOTICE`. Przed publikacją lub przyjęciem wkładu należy sprawdzić pochodzenie kodu, prawa autora, możliwe prawa pracodawcy lub klienta, brak sekretów i danych osobowych oraz zgodność licencji materiałów stron trzecich. Projekt jest niezależny i nie stanowi produktu ani rekomendacji Komisji Europejskiej, Unii Europejskiej czy administracji podatkowej.

Zmiana rozpoczyna się od issue lub dyskusji, szczególnie gdy dotyczy publicznego API, licencji, modelu concurrency albo architektury. Contributor tworzy krótki branch z `main`, utrzymuje mały zakres, dodaje deterministyczny regression test i wykonuje `./mvnw --batch-mode --no-transfer-progress clean verify`. Unit test nie korzysta z publicznej sieci; test HTTP/concurrency używa wyłącznie loopback mocka. Load i stress przeciw publicznemu VIES są zabronione. Race synchronizuje latch/barrier, nie stały sleep.

Publiczne API pozostaje małe, type-safe i zgodne z Java 21. Moduł runtime nie otrzymuje nowej dependency bez wyraźnej decyzji. `Unavailable` nigdy nie staje się `Invalid`, shared state jest thread-safe i memory-bounded, a złożona logika concurrency ma komentarze angielskie i węgierskie. Narzędzia AI są dozwolone, lecz autor odpowiada za każdą linię, testy, pochodzenie i licencję, ujawnia istotną pomoc w PR i nie przekazuje poufnych danych.

Problemu bezpieczeństwa nie zgłasza się publicznym issue. Należy użyć GitHub Security Advisory, podając wersję, środowisko, reprodukcję, wpływ i minimalny PoC bez prawdziwych danych podatkowych lub osobowych. Cel to potwierdzenie w siedem dni i pierwsza ocena w czternaście dni; nie jest to umowne SLA. Istotne są injection, TLS/endpoint bypass, data leak, cache poisoning, nadużycie requester data, nieograniczony wzrost zasobów, shutdown deadlock i błędne `Invalid` przy awarii technicznej.

W repozytorium GitHub włącz Issues, Discussions, private vulnerability reporting, Dependabot, secret scanning oraz push protection. `main` chronią PR, co najmniej jedno approval, dismissal starego approval, resolved conversations oraz CI, CodeQL i dependency review. Force push i usunięcie brancha są zabronione. Release używa najlepiej podpisanego annotated tag, zawiera binary, sources, Javadoc JAR i SHA-256. Tokeny i klucze GPG są wyłącznie w Actions secrets z least privilege.

Wsparcie społeczności jest best effort i nie stanowi porady podatkowej, prawnej ani księgowej. Darowizny przez Buy Me a Coffee są dobrowolne i nie kupują priorytetu, SLA, wyjątku bezpieczeństwa ani prawa governance. Uczestnicy są rzeczowi i szanują innych, chronią prywatne informacje i krytykują kod, nie człowieka. Nękanie, dyskryminacja, doxxing, oszustwo, przywłaszczenie autorstwa oraz nieodpowiedzialne disclosure mogą skutkować usunięciem treści lub ograniczeniem udziału.
