# Polski (pl) — Komponenty stron trzecich

**Języki:** [English](../../../THIRD_PARTY_NOTICES.md) · [Polski](THIRD_PARTY_NOTICES.md) · [Dokumentacja](README.md)

> Tłumaczenie informacyjne; obowiązuje angielski oryginał. Jedyną wiążącą licencją jest [LICENSE](../../../LICENSE); tłumaczenie nie jest licencją.

Prawnie rozstrzygające angielskie pliki [LICENSE](../../../LICENSE) i [NOTICE](../../../NOTICE) pozostają niezmienione; to tłumaczenie ich nie zastępuje.

## Runtime JAR

`vies-client-1.0.0.jar` nie zawiera ani nie wymaga bibliotek stron trzecich; używa tylko JDK `java.base` i `java.net.http`.

## Build/test

| Komponent | Wersja | Zakres | Licencja |
|---|---:|---|---|
| JUnit Jupiter | 6.1.2 | tylko test | EPL-2.0 |

Pluginy Maven i tranzytywne test dependencies nie są bundlowane; Maven pobiera je z własnymi metadanymi/licencjami. Źródło: <https://github.com/junit-team/junit-framework>. PR dodający dependency musi zaktualizować plik i zgodność licencji.
