# Slovenščina (sl) — Komponente tretjih oseb

**Jeziki:** [English](../../../THIRD_PARTY_NOTICES.md) · [Slovenščina](THIRD_PARTY_NOTICES.md) · [Dokumentacija](README.md)

> Informativni prevod. Angleški [LICENSE](../../../LICENSE) in [NOTICE](../../../NOTICE) sta pravno veljavna in ostaneta nespremenjena; ta prevod ni licenca ali notice.

`vies-client-1.2.0.jar` nima third-party runtime knjižnic; uporablja le JDK `java.base` in `java.net.http`.

| Komponenta | Verzija | Obseg | Licenca |
|---|---:|---|---|
| JUnit Jupiter | 6.1.2 | samo test | EPL-2.0 |

Maven plugins/transitive tests niso v release JAR. Vir: <https://github.com/junit-team/junit-framework>. Nov dependency zahteva posodobitev tega seznama in pregled licence.

Pri vsaki spremembi različice JUnit ali Maven vtičnika mora pull request preveriti izvor, licenco, obseg uporabe in dejstvo, da komponenta ni vključena v runtime JAR.
