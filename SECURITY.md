# Biztonsági szabályzat / Security policy

## Támogatott verziók / Supported versions

| Verzió / Version | Biztonsági javítás / Security fixes |
|---|---|
| legfrissebb `1.x` / latest `1.x` | igen / yes |
| régebbi kiadások / older releases | csak külön döntéssel / case by case |

A projekt induló állapotában mindig a legfrissebb kiadásra frissíts. A támogatási
mátrix későbbi major verzióknál változhat.

During the initial project phase, update to the latest release. This policy may
change when additional major versions exist.

## Sérülékenység bejelentése / Reporting a vulnerability

Ne nyiss publikus issue-t, és ne tegyél közzé exploitot a javítás előtt.

1. A GitHub repó **Security → Advisories → Report a vulnerability** felületét használd.
2. Add meg az érintett verziót, környezetet, reprodukciót és a lehetséges hatást.
3. Ha lehet, adj minimális proof-of-conceptot valódi személyes vagy adózási adat nélkül.
4. Jelezd, ismert-e megkerülés vagy javasolt javítás.

Do not open a public issue or disclose an exploit before a coordinated fix.

1. Use **Security → Advisories → Report a vulnerability** in the GitHub repository.
2. Include affected versions, environment, reproduction steps, and potential impact.
3. Provide a minimal proof of concept without real personal or tax data when possible.
4. Include known workarounds or a proposed fix, if available.

## Válaszidő / Response targets

- Beérkezés visszaigazolása: lehetőség szerint 7 napon belül.
- Első értékelés és következő lépés: lehetőség szerint 14 napon belül.
- Javítás és közzététel: súlyosság és komplexitás alapján, koordináltan.

- Acknowledgement: target within 7 days.
- Initial assessment and next step: target within 14 days.
- Fix and disclosure: coordinated according to severity and complexity.

Ezek célértékek, nem szerződéses SLA-k. / These are targets, not contractual SLAs.

## Hatókör / Scope

Kiemelten releváns: URI/input injection, TLS vagy endpoint megkerülés, érzékeny adat
kiszivárgása, cache poisoning, jogosulatlan requester-adat, korlátlan memória/szál/
kapcsolat növekedés, shutdown deadlock, hibás `Invalid` döntés technikai hiba esetén.

High-value reports include injection, TLS/endpoint bypass, sensitive-data exposure,
cache poisoning, requester-data misuse, unbounded resource growth, shutdown deadlock,
or incorrectly returning `Invalid` for a technical failure.

Az upstream VIES kiesése, throttlingja vagy tagállami adatminősége önmagában nem a
klienskönyvtár sérülékenysége. / Upstream availability, throttling, or member-state
data quality alone is not a vulnerability in this library.

## Beépített védelmek / Built-in defenses

- A kliens nem követ redirectet, és sima HTTP-t csak pontos loopback tesztcímhez fogad el.
- A VIES-válasz 64 KiB-os; a JSON mélysége, elemszáma, számai és üzleti mezői korlátosak.
- Hibás UTF-8, duplikált kulcs, ellentmondó döntés vagy eltérő VAT echo
  `MALFORMED_RESPONSE`, soha nem `Invalid`.
- A megosztott cache kulcsa verziózott SHA-256 lenyomat; a visszakapott értéknek
  egyeznie kell a kért adószámmal és a mezőlimitekkel.
- A lokális sync, async, hálózati és cache limitek konfigurációsan is felső korlátosak.

- Redirects are disabled and plain HTTP is limited to exact loopback test addresses.
- Responses are capped at 64 KiB; JSON depth, value count, numbers and business
  fields are bounded.
- Invalid UTF-8, duplicate keys, contradictory decisions or mismatched VAT echoes
  become `MALFORMED_RESPONSE`, never `Invalid`.
- Shared-cache keys are versioned SHA-256 digests; cache values are VAT-bound and
  field-bounded.
- Sync, async, network and in-memory cache limits have hard configuration ceilings.

Operational controls remain mandatory: authenticate and rate-limit every public API,
keep custom HTTPS `baseUrl` configuration trusted, use TLS/auth/ACL/private networking
for Redis, mask personal/tax data in logs, and apply a distributed limiter across JVMs.

Üzemeltetési védelem továbbra is kötelező: publikus API hitelesítése és rate limitje,
megbízható egyedi HTTPS `baseUrl`, Redis TLS/auth/ACL/privát hálózat, maszkolt logok és
JVM-ek közötti elosztott limiter.
