# English (en) — GitHub publication
> [Language selector](../../LANGUAGES.md) · English source and root `LICENSE`/`NOTICE` govern.

Before publication confirm rights, scan directory and full history for secrets, remove customer/requester data, tokens, `.env` and machine files, verify Apache-2.0/notices, and run `./mvnw --batch-mode --no-transfer-progress clean verify`. Create an empty public `vies-client` repository (do not regenerate README/license), review staged files, then `git init -b main`, commit, and only after replacing `OWNER` run `gh repo create OWNER/vies-client --public --source . --remote origin --push`.

Enable Issues, Discussions, private vulnerability reporting, Dependabot, secret scanning/push protection and branch rules requiring PR, approval, resolved conversations and CI/CodeQL/dependency checks; disable force-push/deletion. Add relevant Java/VIES topics and labels. Configure a verified donate URL in `.github/FUNDING.yml`; donations buy no SLA. After the first push update `pom.xml` URL/SCM/developers, badges, advisory URL and release coordinates. Release signed `v1.0.0` only after green checks.
