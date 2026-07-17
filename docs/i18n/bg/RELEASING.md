# Български (bg) — Издания

**Languages:** [English](../../RELEASING.md) · [Български](RELEASING.md) · [Всички езици](../../LANGUAGES.md)

> Информационен превод. При различие водещ е английският технически и правен оригинал. Само кореновите LICENSE и NOTICE са правно меродавни; преводът не е лиценз. [LICENSE](../../../LICENSE), [NOTICE](../../../NOTICE).

Изисквания: clean main, green Actions, release permissions, JDK 21, Maven 3.9+, current CHANGELOG. SemVer: PATCH fix, MINOR compatible feature, MAJOR breaking API/semantics.

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
jar --describe-module --file target/vies-client-1.0.0.jar
jdeps --print-module-deps target/vies-client-1.0.0.jar
```

Проверете Git history без secrets/data, актуални LICENSE/NOTICE/SECURITY/docs, sources/Javadoc JAR, no mandatory live/load CI, API changelog. Set pom.xml, commit, signed tag: git tag -s v1.0.0 -m v1.0.0; push origin main --follow-tags. release.yml retests and attaches JARs.

Maven Central допълнително изисква owned reverse-DNS groupId, url/scm/developers/distribution metadata, Portal token, GPG и publishing config. Secrets само в Actions. След release: downloads/SHA-256, new Unreleased, Security Advisory, dependency version docs.

## Допълнителни правила за управление и сигурност

Единственият лиценз е Apache-2.0. Само английските `LICENSE` и `NOTICE` са правно обвързващи. Преди publish се проверяват авторските права, евентуална собственост на работодател или клиент, произходът и лицензът на third-party материалите и липсата на secrets, лични и клиентски данни. Проектът е независим и не е официален продукт или препоръка на Европейската комисия, ЕС или данъчен орган.

Промяна на public API, лиценз или архитектура първо се обсъжда в issue. Contributor работи в кратък branch от `main`, прави малка промяна, добавя deterministic regression test и изпълнява пълния Maven verify. Unit test не използва публична мрежа, HTTP/concurrency test използва loopback mock, а load срещу public VIES е забранен. Shared state остава thread-safe и memory-bounded; `Unavailable` никога не става `Invalid`.

Уязвимост се докладва частно чрез GitHub Security Advisory с версия, среда, възпроизвеждане, impact и минимален PoC без реални данни. Целите са потвърждение до седем дни и първа оценка до четиринадесет дни, не договорно SLA. Важни са injection, TLS bypass, data leak, cache poisoning, requester misuse, unbounded resources, shutdown deadlock и грешно `Invalid`.

За `main` се изискват PR, approval, resolved conversations, CI, CodeQL и dependency review; force push и deletion са забранени. Release използва signed annotated tag и включва binary, sources, Javadoc JAR и SHA-256. Secrets и GPG keys се пазят само в Actions. Даренията са доброволни и не купуват priority, SLA или governance. Участниците трябва да са уважителни; тормоз, дискриминация, doxxing, измама, кражба на авторство и безотговорно disclosure водят до модерация.
