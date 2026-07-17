# Български (bg) — Публикуване в GitHub

**Languages:** [English](../../GITHUB_SETUP.md) · [Български](GITHUB_SETUP.md) · [Всички езици](../../LANGUAGES.md)

> Информационен превод. При различие водещ е английският технически и правен оригинал. Само кореновите LICENSE и NOTICE са правно меродавни; преводът не е лиценз. [LICENSE](../../../LICENSE), [NOTICE](../../../NOTICE).

Before publish: rights, scan directory/Git history, remove customer/requester data, tokens, .env, machine files; verify LICENSE/NOTICE/third-party; run ./mvnw --batch-mode --no-transfer-progress clean verify.

Create public vies-client without duplicate README/license/gitignore. Replace OWNER and review staged diff:

```bash
git init -b main
git add --all
git commit -m "Initial open-source release"
gh repo create OWNER/vies-client --public --source . --remote origin --push
```

Enable Issues, Discussions, private vulnerability, Dependabot, secret scanning/push protection, auto-delete. main: PR, approval, stale dismissal, CI/CodeQL/dependency review, resolved conversations, no force/delete; required checks after first workflow.

Labels: bug, enhancement, documentation, localization, security, dependencies, performance, concurrency, breaking-change, good-first-issue, help-wanted, triage. Topics: java, java21, vies, vat, vat-validation, eu, rest-client, virtual-threads, single-flight, jpms, zero-dependency.

Sponsor via .github/FUNDING.yml, e.g. custom: [https://buymeacoffee.com/ACCOUNT]; verified URL, no SLA/governance. After push update pom metadata, badges, advisory, release/Central, funding. Signed v1.0.0 only after green CI/CodeQL.

## Допълнителни правила за управление и сигурност

Единственият лиценз е Apache-2.0. Само английските `LICENSE` и `NOTICE` са правно обвързващи. Преди publish се проверяват авторските права, евентуална собственост на работодател или клиент, произходът и лицензът на third-party материалите и липсата на secrets, лични и клиентски данни. Проектът е независим и не е официален продукт или препоръка на Европейската комисия, ЕС или данъчен орган.

Промяна на public API, лиценз или архитектура първо се обсъжда в issue. Contributor работи в кратък branch от `main`, прави малка промяна, добавя deterministic regression test и изпълнява пълния Maven verify. Unit test не използва публична мрежа, HTTP/concurrency test използва loopback mock, а load срещу public VIES е забранен. Shared state остава thread-safe и memory-bounded; `Unavailable` никога не става `Invalid`.

Уязвимост се докладва частно чрез GitHub Security Advisory с версия, среда, възпроизвеждане, impact и минимален PoC без реални данни. Целите са потвърждение до седем дни и първа оценка до четиринадесет дни, не договорно SLA. Важни са injection, TLS bypass, data leak, cache poisoning, requester misuse, unbounded resources, shutdown deadlock и грешно `Invalid`.

За `main` се изискват PR, approval, resolved conversations, CI, CodeQL и dependency review; force push и deletion са забранени. Release използва signed annotated tag и включва binary, sources, Javadoc JAR и SHA-256. Secrets и GPG keys се пазят само в Actions. Даренията са доброволни и не купуват priority, SLA или governance. Участниците трябва да са уважителни; тормоз, дискриминация, doxxing, измама, кражба на авторство и безотговорно disclosure водят до модерация.
