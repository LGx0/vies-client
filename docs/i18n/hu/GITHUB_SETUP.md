# Magyar (hu) — GitHub közzététel
> [Nyelvválasztó](../../LANGUAGES.md) · Az angol eredeti és a gyökér `LICENSE`/`NOTICE` irányadó.

Publikálás előtt igazold a jogokat, scaneld a könyvtárat és teljes historyt titkokra, távolítsd el az ügyfél/requester adatot, tokent, `.env`-et és gépfájlt, ellenőrizd az Apache-2.0/notices tartalmat, futtasd `./mvnw --batch-mode --no-transfer-progress clean verify`. Hozz létre üres public `vies-client` repót (ne generálj új README/licencet), nézd át a staged diffet, majd `git init -b main`, commit, és csak `OWNER` cseréje után `gh repo create OWNER/vies-client --public --source . --remote origin --push`.

Kapcsold be Issues, Discussions, private vulnerability reporting, Dependabot, secret scanning/push protection; ruleset: PR, approval, resolved conversation, CI/CodeQL/dependency check, force-push/deletion tiltás. Állíts Java/VIES topicokat és címkéket. Ellenőrzött donate URL a `.github/FUNDING.yml`-ben; nem vásárol SLA-t. Első push után frissítsd `pom.xml` URL/SCM/developer, badge, advisory URL és release koordinátákat. Aláírt `v1.2.0` csak zöld ellenőrzésből.
