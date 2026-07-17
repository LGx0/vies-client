# Português (pt) — TESTING

> [Todos os idiomas](../../LANGUAGES.md) · Tradução informativa. Em caso de divergência, prevalece a fonte técnica ou jurídica canónica em inglês. Apenas `LICENSE` e `NOTICE` na raiz têm valor jurídico; esta tradução não os substitui.

## O que chamamos de teste de unidade? / O que é um teste de unidade?

Sim: o teste unitário verifica uma única classe ou regra isoladamente, externamente
sem rede, banco de dados e VIES ao vivo. Rápido, determinístico e em todas as construções
pode correr.

Sim: um teste unitário verifica uma classe ou regra isoladamente, sem rede externa,
banco de dados ou VIES ao vivo. É rápido, determinístico e adequado para cada construção.

Este módulo também requer **integração local e testes de competição**
também porque os comportamentos de tempo limite, nova tentativa, voo único, cancelamento e`close()`são mais
isso é evidente pela cooperação do componente. Eles usam um servidor simulado de loopback, não
ligue para o serviço da UE.

Este módulo também precisa de **integração local e testes de simultaneidade**, porque o tempo limite,
nova tentativa, voo único, cancelamento e desligamento envolvem vários componentes. Esses
os testes usam um servidor simulado de loopback e nunca chamam o serviço público da UE.

## Comandos rápidos / Comandos rápidos

Pacote de teste determinístico completo / Conjunto determinístico completo:

```bash
./mvnw test
```

Somente testes unitários:

```bash
./mvnw -Dtest=VatFormatTest,ViesRequesterTest,ViesResponseMappingTest,ViesErrorTest,ViesAvailabilityTest,ViesClientBuilderTest,MiniJsonTest,TtlCacheTest test
```

Somente testes locais de HTTP/simultaneidade/Somente HTTP local e simultaneidade:

```bash
./mvnw -Dtest=ViesClientHttpTest test
```

Verificação limpa com geração JAR/Javadoc/Verificação limpa com artefatos:

```bash
./mvnw clean verify
./mvnw package
```

Um teste específico/Um método de teste:

```bash
./mvnw -Dtest=ViesClientHttpTest#cancellationDoesNotLeakAsyncCapacity test
```

## Cobertura atual / Cobertura atual

O pacote determinístico atual contém **73 testes**:

- **44 testes unitários** em oito turmas;
- **29 testes locais de HTTP/integração/simultaneidade**;
- zero chamadas de rede externas obrigatórias.

O conjunto determinístico contém **73 testes**: 44 testes unitários, 29 testes locais
Testes HTTP/integração/simultaneidade e zero chamadas externas obrigatórias.

## Catálogo de testes unitários / Catálogo de testes unitários

###`VatFormatTest`— 8 testes

| ID       | Gol húngaro                                                      | Objetivo inglês                                          |
| -------- | ---------------------------------------------------------------- | -------------------------------------------------------- |
| U-FMT-01 | Normalização do número fiscal total                              | Normalizar o identificador completo do IVA               |
| U-FMT-02 | Remover espaço/ponto final/hífen, colocar maiúscula              | Separadores de tiras e maiúsculas                        |
| U-FMT-03 | `GR`→ Mapeamento`EL`                                             | Mapear Grécia`GR` para VIES`EL`                          |
| U-FMT-04 | Rejeitar nulo, branco, país desconhecido e comprimento incorreto | Rejeitar comprimento nulo/em branco/desconhecido/ruim    |
| U-FMT-05 | Formulários representativos do país                              | Formatos representativos dos países                      |
| U-FMT-06 | API de código+número de país separado, prefixo anexado           | Emparelhar API com prefixo anexado                       |
| U-FMT-07 | Códigos de países suportados                                     | Conjunto de países suportados                            |
| U-FMT-08 | Todos os 28 países apoiados têm pelo menos uma forma de          | Pelo menos uma forma para todos os 28 códigos suportados |

###`ViesRequesterTest`— 4 testes

| ID       | Gol húngaro                                               | Objetivo inglês                              |
| -------- | --------------------------------------------------------- | -------------------------------------------- |
| U-REQ-01 | Preencher a partir do próprio requerente do número fiscal | Criar solicitante do IVA integral            |
| U-REQ-02 | Canonização de requerente grego                           | Canonizar solicitante grego                  |
| U-REQ-03 | Construtor emparelhado com prefixo anexado                | Construtor de par com prefixo correspondente |
| U-REQ-04 | Rejeição imediata do solicitante falho                    | Falha rápida em solicitante inválido         |

###`ViesResponseMappingTest`— 11 testes

| ID       | Gol húngaro                                                        | Objetivo inglês                                            |
| -------- | ------------------------------------------------------------------ | ---------------------------------------------------------- |
| U-MAP-01 | `Valid`estilo GET com todos os campos                              | Mapa estilo GET Resposta válida                            |
| U-MAP-02 | POST-stílusú`Valid`, espaço reservado`---`                         | Mapa estilo POST Válido e espaços reservados               |
| U-MAP-03 | Hiteles`Invalid`                                                   | Mapa oficial Inválido                                      |
| U-MAP-04 | Átmeneti hiba →`Unavailable`                                       | Mapear erro transitório para Indisponível                  |
| U-MAP-05 | Entrada →`MalformedInput`                                          | Erro de entrada do mapa VIES                               |
| U-MAP-06 | Rejeitar JSON não-objeto                                           | Rejeitar JSON não-objeto                                   |
| U-MAP-07 | Booleano ausente não pode ser`Invalid`                             | Booleano ausente nunca se torna inválido                   |
| U-MAP-08 | A Boolean string cannot become `Invalid`                           | Validade não booleana rejeitada                            |
| U-MAP-09 | Não encontramos a hora local para uma data de auditoria ausente    | Nunca invente um carimbo de data/hora de auditoria ausente |
| U-MAP-10 | Rejeitar data de auditoria incorreta                               | Rejeitar carimbo de data/hora de auditoria inválido        |
| U-MAP-11 | Conversão correta do carimbo de data/hora de deslocamento para UTC | Analisar carimbo de data e hora de deslocamento para UTC   |

###`ViesErrorTest`— 6 testes

| ID       | Gol húngaro                                                       | Objetivo inglês                                            |
| -------- | ----------------------------------------------------------------- | ---------------------------------------------------------- |
| U-ERR-01 | Mensagem de rede em húngaro+inglês                                | Erro de rede bilíngue                                      |
| U-ERR-02 | Input errors are not retried                                      | O erro de entrada é permanente                             |
| U-ERR-03 | HTTP 408/429/5xx nova tentativa de besorolása                     | Classificação de novas tentativas HTTP                     |
| U-ERR-04 | `Valid` and `Invalid` are not errors                              | Decisions expose no error                                  |
| U-ERR-05 | Todos os códigos públicos HU+EN e valor de nova tentativa estável | Todo código público tem HU+EN e política de nova tentativa |
| U-ERR-06 | Retenha o código desconhecido sem tentar novamente                | Preservar código desconhecido sem novas tentativas         |

###`ViesAvailabilityTest`— 2 testes

- cópia de proteção e imutabilidade do mapa de entrada;
- rejeição imediata do mapa nulo dos Estados-Membros.

Cópia defensiva de snapshot imutável e validação de construtor de mapa nulo.

###`MiniJsonTest`— 4 testes

- documento VIES típico e escape Unicode;
- objeto/lista/escalar/número/nulo aninhado;
- JSON escape-ek;
- rejeição de entradas incorretas, truncadas e finais.

VIES JSON típico, valores aninhados/escalares, escapes e entrada malformada com falha fechada.

###`TtlCacheTest`— 6 testes

- acertar antes do TTL, errar depois;
- limite exato de TTL;
- ignorar TTL não positivo;
- limite de tamanho configurado;
- deslocamento preferencial de um elemento expirado;
- 32 gravações/leituras simultâneas de threads virtuais.

Comportamento TTL, limite de expiração exato, controle de tamanho, amostragem expirada primeiro e
pressão simultânea de 32 threads virtuais.

###`ViesClientBuilderTest`— 3 testes

- configuração completa de alta carga pode ser construída;
- rejeitar URL/limite/nova tentativa errados;
- rejeição de Duração zero, negativa e overflow.

Configuração válida de alta carga, além de URL, limite, nova tentativa, duração e estouro
validação.

## HTTP local, simultaneidade e ciclo de vida / Integração local e simultaneidade

`ViesClientHttpTest`— 29 testes em uma porta de loopback livre aleatória:

| ID | Caso de teste |
| ---- | --------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------- |
| I-01 | 503 tentativas duas vezes, depois sucesso / duas 503 tentativas e depois sucesso |
| C-01 | 200 chamadores assíncronos da mesma chave → exatamente 1 solicitação HTTP / 200 chamadores assíncronos da mesma chave → uma solicitação |
| C-02 | As solicitações HTTP ativas não excedem o limite de 4 / as chamadas ativas permanecem dentro de 4 |
| C-03 | Assíncrono pendente acima do limite imediato`CLIENT_OVERLOADED` |
| C-04 | Cancelamento futuro não vaza, permitet, capacidade é restaurada |
| C-05 | `close()`retorno de chamada assíncrono sem deadlock |
| C-06 | `admissionTimeout`limita o enfileiramento |
| I-02 | Erro de leitura de Redis/cache →`CACHE_ERROR`, chamada VIES nula |
| C-07 | Licença e voo único | são liberados antes de chamadas assíncronas consecutivas |
| C-08 | Cache assíncrono/corrida fechada →`CLIENT_CLOSED`, nulla HTTP |
| C-09 | Sincronizar cache/fechar corrida →`CLIENT_CLOSED`, nulla HTTP |
| C-10 | O trabalho do executor personalizado é interrompido, mas o executor não fecha |
| C-11 | O líder e o seguidor de sincronização obtêm o mesmo resultado`CLIENT_CLOSED` |
| C-12 | 100 seguidores assíncronos idênticos não consomem 100 slots pendentes |
| C-13 | Após a rejeição do executor, a autorização e o estatuto de voo são restaurados |
| C-14 | Líder de sincronização + seguidor assíncrono → uma solicitação HTTP |
| C-15 | Segunda verificação de cache fecha corrida obsoleta, HTTP nulo |
| C-16 | No fechamento durante a gravação do cache, o resultado da sincronização líder/seguidor é o mesmo |
| C-17 | A licença pendente | também é liberado antes de uma chamada assíncrona encadeada com chaves diferentes |
| C-18 | Uma tarefa de executor personalizado enfileirada é cancelada ao fechar |
| C-19 | Bloquear o retorno de chamada do usuário não bloqueia o fechamento/bloqueio do ciclo de vida |
| C-20 | Líder assíncrono + seguidor de sincronização → uma solicitação HTTP |
| C-21 | `maxPendingSyncRequests`oferece contrapressão limitada instantânea |
| C-22 | Assíncrono fatal continua`Error`futuro excepcional e manipulador não capturado |

## O que não testamos por padrão? / O que não está no pacote padrão?

### Teste de fumaça VIES ao vivo / Teste de fumaça VIES ao vivo

O serviço ativo é variável e com taxa limitada, portanto não pode haver uma condição de IC obrigatória.
O teste de fumaça manual é no máximo um`availability()`e um teste conhecido, não secreto ou
consultar o número fiscal obtido de uma variável de ambiente, simultaneidade = 1 e tentativas = 0
contexto.

O VIES ao vivo é variável e tem taxa limitada, portanto não deve bloquear o CI normal. Uma adesão
teste de fumaça deve realizar no máximo uma verificação de disponibilidade e uma validação, com
simultaneidade=1 e novas tentativas=0. Nunca comprometa ou registre números de IVA de solicitantes privados.

### Teste de carga / Teste de carga

Sempre execute uma simulação local ou seu próprio serviço de teste, nunca o público
contra o VIES. As necessidades absolutas são informativas; correção, limite, p95/p99 e códigos de erro
os portões.

Sempre direcione uma simulação local ou serviço de teste próprio, nunca VIES público. Absoluto
solicitações/segundo é informativo; correção, limites, p95/p99 e semântica de erro
são os portões.

Casos recomendados:

- muitas chaves distintas;
- 100k chamador de tecla de atalho kis kulcshalmazon / debandada de tecla de atalho;
- sobrecarga e recuperação acima do limite;
- 200/429/503/timeout/resposta mista malformada;
- pressão de cache em torno do máximo configurado.

### Teste de imersão e caos / Teste de imersão e caos

Carga fixa de 30 a 60 minutos, heap limitado, JFR, fechamento/reinício repetido, latência
pico, redefinição de conexão, falha de cache e cancelamento. A pilha, threads ativos,
número de em voo/pendentes e platô de tomadas; não deve haver impasses ou vazamentos de permissão.

Execute de 30 a 60 minutos com heap fixo, JFR, ciclo de vida repetido, picos de latência,
redefinições de conexão, falhas de cache e cancelamento. Heap/threads/em trânsito/soquetes
deve estabilizar sem impasse ou permitir vazamentos.

Exemplo JFR / exemplo JFR:

```bash
JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=target/vies-soak.jfr,settings=profile" ./mvnw test
```

## SOMENTE recommendation / SOMENTE recomendado

Pipeline mínimo:

```bash
./mvnw --batch-mode clean verify
```

Matriz recomendada: pelo menos JDK 21 LTS; JDKs adicionais só podem ser definidos então
suportado se o mesmo conjunto realmente for executado neles.

Matriz recomendada: pelo menos JDK 21 LTS. Solicite suporte adicional ao JDK somente depois
executando o mesmo conjunto nessas versões.

## Regra de regressão / Regra de regressão

Cada bug corrigido deve ter um teste determinístico que é a correção
teria falhado antes. A trava/barreira deve ser a prioridade para o teste de competição
sincronização;`sleep`fixo pode ser apenas uma breve espera na votação, não um oráculo de correção.

Todo bug corrigido deve receber um teste determinístico que falhou antes da correção.
Prefira travas/barreiras para testes de simultaneidade; sonos fixos podem ser pesquisas curtas
apenas recuo, nunca o oráculo da correção.
