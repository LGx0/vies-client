# English (en) — Contributing
> [Language selector](../../LANGUAGES.md) · English source and root `LICENSE` govern.

Open/reference an issue; discuss major API/license/architecture changes first; report security privately. Use JDK 21+, Maven 3.9+, Git and run `./mvnw --batch-mode --no-transfer-progress clean verify`. Fork, branch from `main`, keep changes focused, add a deterministic regression for every fix, and complete the PR template.

Keep public API small/type-safe and runtime dependency-free; never turn `Unavailable` into `Invalid`; bound shared state; document public/concurrent logic in English and Hungarian; never commit private VAT/business data. Unit/HTTP tests use no public VIES; races use latches/barriers. PRs need green build, docs, justified dependencies, explicit breaking changes and reproducible performance claims. AI is allowed, but contributors must review/test/license every line, disclose substantial use and never provide confidential data. Contributions are submitted under Apache-2.0 §5 and participants follow `CODE_OF_CONDUCT.md`.
