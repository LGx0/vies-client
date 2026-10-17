# Biztonsági felmérés / Security review — 2026-10-17

## Hatókör / Scope

A Java-kliens teljes production forrása, JSON-olvasója, TTL-cache-e, szinkron és
aszinkron életciklusa, loopback demója és GitHub kiadási folyamata. A vizsgálat
forrásellenőrzést, szintetikus loopback reprodukciót és regressziós teszteket használ.
Nem indít terhelést az EU VIES ellen, és nem tanúsítás vagy behatolási teszt.

This review covers production Java sources, the JSON parser, TTL cache, sync/async
lifecycle, loopback demo, and GitHub release workflow. Evidence consists of source
inspection, synthetic loopback reproductions, and regression tests. It does not
load-test public VIES or constitute certification or penetration testing.

## Javított megállapítások / Fixed findings in 1.2.1

| Prioritás / Priority | Veszély / Risk | Javítás és bizonyíték / Fix and evidence |
|---|---|---|
| P1 | JDK 21: fejléc után elakadó body tartósan foglalhat hálózati slotot / A stalled post-header body can indefinitely hold network capacity | Teljes body-határidő és HTTP cancel; VAT és availability teszt / Full-body deadline and HTTP cancellation; VAT and availability tests |
| P2 | Kiadáskor wrapper/POM futott a tag megbízhatóságának igazolása előtt / Release ran wrapper/POM before establishing tag trust | Aláírás és main-ősviszony ellenőrzése minden repository-kód előtt / Signature and main ancestry checked before any repository code |
| P2 | Ír requester `+` szóközzé válhat / Irish requester `+` may decode as space | Query-érték URL-kódolása és dekódolásos regresszió / URL-encode query value; decoding regression |
| P2 | Szabálytalan JSON Unicode escape elfogadása / Invalid JSON Unicode escapes accepted | Négy ASCII hex számjegy, helyes surrogate-kezelés / Four ASCII hex digits; surrogate validation |
| P2 | Hibás availability séma kiesésként vagy üres térképként jelent meg / Malformed status schema appeared as outage or empty map | Kötelező boolean és listaelem-típusok, duplikációellenőrzés / Required boolean, entry types, duplicate checks |
| P3 | Túlcsorduló hálózati timeout félrevezető hibát adhat / Oversized timeout can produce misleading transport errors | Builder-validáció long nanoszekundumra / Validate signed-long nanoseconds in builder |

A prioritás a javítás előtti hatást jelöli. A táblázat sorai az 1.2.1-ben javítottak.
Priorities describe pre-fix impact. All rows above are fixed in 1.2.1.

## Ellenőrzés / Verification

- 81 determinisztikus teszt: 47 unit, 34 helyi HTTP/integráció/konkurencia.
- Ugyanaz a clean Maven verify JDK 21-en és JDK 25-ön; Java 21 bytecode (major 65).
- Nincs új runtime függőség, teszt-only `jdk.httpserver` nem kerül a modulba.
- A hibák előzetes reprodukciója, majd javítás utáni pozitív/negatív regressziók.
- A kiadási workflow aláírt annotált taget, pontos verziót és main-ősviszonyt kér;
  read-only build után külön védett job publikál és attestál.

The suite has 81 deterministic tests: 47 unit and 34 local HTTP/integration/
concurrency tests. Clean Maven verification runs on JDK 21 and 25 while emitting
Java 21 bytecode. No runtime dependency was added. Release checks require a verified
annotated tag, matching version, and main ancestry; the read-only build is followed
by a separate protected publishing/attestation job. CI results remain visible on
the associated pull request and release run.

## Üzemeltetési kockázatok / Operational risks

| Határ / Boundary | Kötelező alkalmazásoldali védelem / Application responsibility |
|---|---|
| Nyilvános API / Public API | Hitelesítés, tenant-jogosultság, bemeneti limit és felhasználónkénti rate limit / Authentication, tenant authorization, input and per-user rate limits |
| Több JVM / Multiple JVMs | Globális limiter és tartós bounded queue; a kliens limitei példányonkéntiek / Global limiter and durable bounded queue; client limits are per instance |
| Azonos async követők / Same-key async followers | A leader limit nem korlátozza az összes visszaadott future/callback számát; tarts korlátos hívói ablakot / Leader capacity does not cap every returned future/callback; bound the caller window |
| Külső cache / External cache | TLS/auth/ACL, megbízható írók, rövid get/put timeout. A SHA-256 kulcs nem titkosítás vagy értékaláírás / TLS/auth/ACL, trusted writers, short get/put timeouts. A SHA-256 key is not encryption or value authentication |
| Cache-hit / Cached decision | Az eredeti requestDate/consultationNumber marad; üzleti frissességet TTL-lel vagy disableCache-sel szabályozz / Original timestamp/consultation ID remains; control freshness with TTL or disableCache |
| Timeout / Deadline | HTTP-próbánkénti; cache, admission és retry várakozás hozzáadódik / Per HTTP attempt; cache, admission, and retry waits add to operation time |
| `baseUrl` | Csak megbízható konfigurációból. A kliens HTTPS hostot nem allowlistel / Trusted configuration only; custom HTTPS hosts are not allowlisted |
| Külső szöveg / External text | JSON-szerializálás és HTML-escape; adószám, név, cím maszkolása a logban / JSON serialization, HTML escaping, and redacted VAT/name/address logging |
| Megszakítás / Cancellation | Egy hívó future.cancel-je nem törli a többi követő közös kérését; teljes leállításhoz close / A caller cancellation does not cancel shared work; close shuts down the client |
| Demó / Demo | Csak loopback; éles szolgáltatás elé külön hitelesített TLS API szükséges / Loopback only; production needs an authenticated TLS API |

A szerkezetileg helyes cache-érték nem bizonyít VIES-eredetet: az adapter és a cache
írási jogosultsága része a bizalmi határnak. A VIES `Unavailable` eredményét soha ne
könyveld `Invalid` üzleti döntésként. E felmérés nem garantál ismeretlen hibáktól
való mentességet vagy milliós throughputot; ilyen állításhoz külön mért load/soak
eredmény kell saját mock/staging környezetben.

A structurally valid cache entry does not prove VIES origin: cache writers and the
adapter are trusted components. Never turn `Unavailable` into an `Invalid` decision.
This review does not prove absence of undiscovered bugs or million-request
throughput; those claims require measured mock/staging load and soak evidence.
