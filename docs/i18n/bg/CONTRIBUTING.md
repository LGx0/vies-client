# Български (bg) — Приноси

**Languages:** [English](../../../CONTRIBUTING.md) · [Български](CONTRIBUTING.md) · [Всички езици](../../LANGUAGES.md)

> Информационен превод. При различие водещ е английският технически и правен оригинал. Само кореновите LICENSE и NOTICE са правно меродавни; преводът не е лиценз. [LICENSE](../../../LICENSE), [NOTICE](../../../NOTICE).

Open/reference issue; major API/license/architecture първо discussion; security следва SECURITY. JDK 21+, Maven 3.9+, Git. Fork → short branch from main → focused change → deterministic regression test → ./mvnw clean verify → complete PR.

Java 21, small type-safe API, no runtime deps without agreement, Unavailable never Invalid, shared state thread-safe/bounded. Public API/complex concurrency comments EN/HU. No private VAT/company/address/requester.

Unit no public network; HTTP/concurrency only loopback; no load public VIES; latch/barrier. PR green/documented, dependencies justified, breaking/performance explicit. AI allowed, contributor reviews/tests every line and provenance/license, discloses substantial use, no confidential/unreviewed material. Apache-2.0 §5 и CODE_OF_CONDUCT.

## Допълнителни правила за управление и сигурност

Единственият лиценз е Apache-2.0. Само английските `LICENSE` и `NOTICE` са правно обвързващи. Преди publish се проверяват авторските права, евентуална собственост на работодател или клиент, произходът и лицензът на third-party материалите и липсата на secrets, лични и клиентски данни. Проектът е независим и не е официален продукт или препоръка на Европейската комисия, ЕС или данъчен орган.

Промяна на public API, лиценз или архитектура първо се обсъжда в issue. Contributor работи в кратък branch от `main`, прави малка промяна, добавя deterministic regression test и изпълнява пълния Maven verify. Unit test не използва публична мрежа, HTTP/concurrency test използва loopback mock, а load срещу public VIES е забранен. Shared state остава thread-safe и memory-bounded; `Unavailable` никога не става `Invalid`.

Уязвимост се докладва частно чрез GitHub Security Advisory с версия, среда, възпроизвеждане, impact и минимален PoC без реални данни. Целите са потвърждение до седем дни и първа оценка до четиринадесет дни, не договорно SLA. Важни са injection, TLS bypass, data leak, cache poisoning, requester misuse, unbounded resources, shutdown deadlock и грешно `Invalid`.

За `main` се изискват PR, approval, resolved conversations, CI, CodeQL и dependency review; force push и deletion са забранени. Release използва signed annotated tag и включва binary, sources, Javadoc JAR и SHA-256. Secrets и GPG keys се пазят само в Actions. Даренията са доброволни и не купуват priority, SLA или governance. Участниците трябва да са уважителни; тормоз, дискриминация, doxxing, измама, кражба на авторство и безотговорно disclosure водят до модерация.

### Критерии за приемане

Промяната се приема само ако всички permits и `inFlight` записи се освобождават при rejection, cancellation, interruption и cache exception. Leader и followers трябва да получат еднакъв резултат при `close()`, а user callback не може да задържа lifecycle lock. Тестът използва latch, blocking cache и контролиран executor, проверява точния брой HTTP заявки, възстановяването на pending capacity и липсата на останали threads или sockets. Pull request описва public behavior, compatibility, security impact и възпроизводима performance методика. Авторът потвърждава правото да предостави кода и проверява всеки AI-generated ред, provenance и лиценз. Нов dependency изисква актуализация на third-party notices и отделно обосноваване защо zero-runtime-dependency целта се променя.

Преди review contributor проверява документацията, changelog, публичните Javadoc коментари и всички локализирани връзки. Breaking change се отбелязва изрично и изисква подходяща major версия.
