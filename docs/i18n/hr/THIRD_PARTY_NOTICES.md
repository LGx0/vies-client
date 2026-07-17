# Hrvatski (hr) — Komponente trećih strana

> [Svi jezici](../../LANGUAGES.md) · Informativni prijevod. U slučaju razlike mjerodavan je kanonski engleski tehnički ili pravni izvor. Samo su korijenski `LICENSE` i `NOTICE` pravno mjerodavni; prijevod ih ne zamjenjuje.

## Distribuirani runtime JAR

`vies-client-1.2.0.jar` ne uključuje niti zahtijeva vanjske biblioteke za vrijeme izvođenja.
Koristi samo `java.base` i `java.net.http` module JDK-a.

Distribuirani JAR sadrži i ne zahtijeva biblioteku vremena izvođenja treće strane. Samo to
koristi JDK module `java.base` i `java.net.http`.

## Ovisnosti za build i testiranje

| Komponenta / Komponenta | Verzija / Verzija | Upotreba/opseg        | Licenca / Licenca |
| ----------------------- | ----------------: | --------------------- | ----------------- |
| JUnit Jupiter           |             6.1.2 | samo test / samo test | EPL-2.0           |

Mavenovi dodaci za izgradnju i tranzitivne testne ovisnosti nisu uključeni u objavljeni JAR
u; Maven ih preuzima u skladu s vlastitim metapodacima i licencama za artefakt.

Dodaci za Maven i tranzitivne testne ovisnosti nisu uključeni u JAR izdanja;
Maven ih rješava pod vlastitim metapodacima artefakta i licencama.

Izvor / Izvor: <https://github.com/junit-team/junit-framework>

Ako se projektu doda nova ovisnost, provjerava se kompatibilnost ove datoteke i licence
mora se ažurirati u istom zahtjevu za povlačenjem.

Svaki zahtjev za povlačenje koji dodaje ovisnost mora ažurirati ovu datoteku i licencu za dokument
kompatibilnost.
