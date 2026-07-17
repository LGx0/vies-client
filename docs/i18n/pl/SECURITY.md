# Polski (pl) — Polityka bezpieczeństwa

**Języki:** [English](../../../SECURITY.md) · [Polski](SECURITY.md) · [Dokumentacja](README.md)

> Tłumaczenie informacyjne; obowiązuje angielski oryginał. Jedyną wiążącą licencją jest [LICENSE](../../../LICENSE); tłumaczenie nie jest licencją.

Najnowsza wersja `1.x` otrzymuje poprawki bezpieczeństwa; starsze case-by-case. Aktualizuj do ostatniego wydania.

Nie publikuj issue ani exploita. Użyj **Security → Advisories → Report a vulnerability** i podaj wersję, środowisko, reprodukcję, wpływ, minimalny PoC bez realnych danych osobowych/podatkowych oraz obejście.

Cele, nie SLA: potwierdzenie 7 dni, ocena 14 dni, koordynowana naprawa wg wagi. Zakres: injection, TLS/endpoint bypass, data leak, cache poisoning, requester misuse, unbounded resources, shutdown deadlock i błędne `Invalid` po awarii. Sam outage/throttling VIES nie jest podatnością biblioteki.

## Szczegółowe zasady zarządzania projektem

Projekt używa jednej licencji Apache-2.0. Tłumaczenia licencji nie są prawnie wiążące; rozstrzygają główny angielski `LICENSE` oraz odpowiedni `NOTICE`. Przed publikacją lub przyjęciem wkładu należy sprawdzić pochodzenie kodu, prawa autora, możliwe prawa pracodawcy lub klienta, brak sekretów i danych osobowych oraz zgodność licencji materiałów stron trzecich. Projekt jest niezależny i nie stanowi produktu ani rekomendacji Komisji Europejskiej, Unii Europejskiej czy administracji podatkowej.

Zmiana rozpoczyna się od issue lub dyskusji, szczególnie gdy dotyczy publicznego API, licencji, modelu concurrency albo architektury. Contributor tworzy krótki branch z `main`, utrzymuje mały zakres, dodaje deterministyczny regression test i wykonuje `./mvnw --batch-mode --no-transfer-progress clean verify`. Unit test nie korzysta z publicznej sieci; test HTTP/concurrency używa wyłącznie loopback mocka. Load i stress przeciw publicznemu VIES są zabronione. Race synchronizuje latch/barrier, nie stały sleep.

Publiczne API pozostaje małe, type-safe i zgodne z Java 21. Moduł runtime nie otrzymuje nowej dependency bez wyraźnej decyzji. `Unavailable` nigdy nie staje się `Invalid`, shared state jest thread-safe i memory-bounded, a złożona logika concurrency ma komentarze angielskie i węgierskie. Narzędzia AI są dozwolone, lecz autor odpowiada za każdą linię, testy, pochodzenie i licencję, ujawnia istotną pomoc w PR i nie przekazuje poufnych danych.

Problemu bezpieczeństwa nie zgłasza się publicznym issue. Należy użyć GitHub Security Advisory, podając wersję, środowisko, reprodukcję, wpływ i minimalny PoC bez prawdziwych danych podatkowych lub osobowych. Cel to potwierdzenie w siedem dni i pierwsza ocena w czternaście dni; nie jest to umowne SLA. Istotne są injection, TLS/endpoint bypass, data leak, cache poisoning, nadużycie requester data, nieograniczony wzrost zasobów, shutdown deadlock i błędne `Invalid` przy awarii technicznej.

W repozytorium GitHub włącz Issues, Discussions, private vulnerability reporting, Dependabot, secret scanning oraz push protection. `main` chronią PR, co najmniej jedno approval, dismissal starego approval, resolved conversations oraz CI, CodeQL i dependency review. Force push i usunięcie brancha są zabronione. Release używa najlepiej podpisanego annotated tag, zawiera binary, sources, Javadoc JAR i SHA-256. Tokeny i klucze GPG są wyłącznie w Actions secrets z least privilege.

Wsparcie społeczności jest best effort i nie stanowi porady podatkowej, prawnej ani księgowej. Darowizny przez Buy Me a Coffee są dobrowolne i nie kupują priorytetu, SLA, wyjątku bezpieczeństwa ani prawa governance. Uczestnicy są rzeczowi i szanują innych, chronią prywatne informacje i krytykują kod, nie człowieka. Nękanie, dyskryminacja, doxxing, oszustwo, przywłaszczenie autorstwa oraz nieodpowiedzialne disclosure mogą skutkować usunięciem treści lub ograniczeniem udziału.
