# Português (pt) — THIRD_PARTY_NOTICES

> [Todos os idiomas](../../LANGUAGES.md) · Tradução informativa. Em caso de divergência, prevalece a fonte técnica ou jurídica canónica em inglês. Apenas `LICENSE` e `NOTICE` na raiz têm valor jurídico; esta tradução não os substitui.

## JAR de tempo de execução distribuído / JAR de tempo de execução distribuído

`vies-client-1.2.0.jar`não inclui nem requer nenhuma biblioteca de tempo de execução externa.
Ele usa apenas os módulos`java.base`e`java.net.http`do JDK.

O JAR distribuído não contém e não requer nenhuma biblioteca de tempo de execução de terceiros. É apenas
usa os módulos JDK`java.base`e`java.net.http`.

## Dependência de construção e teste / Dependência de construção e teste

| Componente / Componente | Versão / Versão | Uso / Escopo                | Licença / Licença |
| ----------------------- | --------------: | --------------------------- | ----------------- |
| JUnit Júpiter           |           6.1.2 | apenas teste / apenas teste | EPL-2.0           |

Plug-ins de construção Maven e dependências de teste transitivas não estão incluídos no JAR lançado
em; eles são baixados pelo Maven de acordo com seus próprios metadados e licenças de artefato.

Plug-ins Maven e dependências de teste transitivas não são incluídos no JAR de lançamento;
O Maven os resolve sob seus próprios metadados e licenças de artefato.

Fonte/Fonte: <https://github.com/junit-team/junit-framework>

Se uma nova dependência for adicionada ao projeto, este arquivo e a verificação de compatibilidade da licença
ele deve ser atualizado na mesma solicitação pull.

Qualquer solicitação pull adicionando uma dependência deve atualizar este arquivo e documento de licença
compatibilidade.
