# Português (pt) — SECURITY

> [Todos os idiomas](../../LANGUAGES.md) · Tradução informativa. Em caso de divergência, prevalece a fonte técnica ou jurídica canónica em inglês. Apenas `LICENSE` e `NOTICE` na raiz têm valor jurídico; esta tradução não os substitui.

## Versões suportadas/Versões suportadas

| Versão / Versão          | Correções de segurança / Correções de segurança |
| ------------------------ | ----------------------------------------------- |
| último`1.x`/ último`1.x` | sim / sim                                       |
| lançamentos mais antigos | apenas com decisão separada/caso a caso         |

Quando o projeto estiver iniciando, sempre atualize para a versão mais recente. O suporte
a matriz pode mudar em versões principais posteriores.

Durante a fase inicial do projeto, atualize para a versão mais recente. Esta política poderá
mudar quando existirem versões principais adicionais.

## Anúncio de vulnerabilidade / Relatando uma vulnerabilidade

Não abra um problema público nem publique uma exploração antes que ela seja corrigida.

1. Use a interface **Segurança → Avisos → Relatar uma vulnerabilidade** do repositório GitHub.
2. Especifique a versão afetada, o ambiente, a reprodução e o impacto potencial.
3. Se possível, forneça uma prova de conceito mínima, sem dados pessoais ou fiscais reais.
4. Indique se há uma solução alternativa conhecida ou uma correção sugerida.

Não abra um problema público nem divulgue uma exploração antes de uma correção coordenada.

1. Use **Segurança → Avisos → Relatar uma vulnerabilidade** no repositório GitHub.
2. Inclua versões afetadas, ambiente, etapas de reprodução e impacto potencial.
3. Fornecer uma prova de conceito mínima, sem dados pessoais ou fiscais reais, quando possível.
4. Inclua soluções alternativas conhecidas ou uma correção proposta, se disponível.

## Tempo de resposta/metas de resposta

- Confirmação de chegada: dentro de 7 dias se possível.
- Primeira avaliação e próximo passo: dentro de 14 dias, se possível.
- Corrigir e publicar: com base na gravidade e complexidade, coordenado.

- Reconhecimento: meta em até 7 dias.
- Avaliação inicial e próximo passo: meta em 14 dias.
- Correção e divulgação: coordenadas de acordo com gravidade e complexidade.

Estas são metas, não SLAs contratuais. / Estas são metas, não SLAs contratuais.

## Escopo

Particularmente relevante: injeção de URI/entrada, TLS ou evasão de endpoint, dados confidenciais
vazamento, envenenamento de cache, dados de solicitantes não autorizados, memória/threads/
crescimento da conexão, impasse de desligamento, decisão`Invalid`incorreta em caso de erro técnico.

Relatórios de alto valor incluem injeção, desvio de TLS/endpoint, exposição de dados confidenciais,
envenenamento de cache, uso indevido de dados do solicitante, crescimento ilimitado de recursos, impasse de desligamento,
ou retornar`Invalid`incorretamente devido a uma falha técnica.

O abandono, a limitação ou a qualidade dos dados do VIES upstream em si não é um
vulnerabilidade da biblioteca cliente. / Disponibilidade upstream, limitação ou estado membro
a qualidade dos dados por si só não é uma vulnerabilidade nesta biblioteca.
