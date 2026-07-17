# Português (pt) — vies-client

> [Todos os idiomas](../../LANGUAGES.md) · Tradução informativa. Em caso de divergência, prevalece a fonte técnica ou jurídica canónica em inglês. Apenas `LICENSE` e `NOTICE` na raiz têm valor jurídico; esta tradução não os substitui.

[![Licença: Apache-2.0](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](../../../LICENSE)
[![Java 21+](https://img.shields.io/badge/Java-21%2B-orange.svg)](../../../pom.xml)
[![Compre-me um café](https://img.shields.io/badge/Buy_Me_a_Coffee-support-ffdd00?logo=buymeacoffee&logoColor=000)](https://buymeacoffee.com/lgx0)

🌍 **[Todos os idiomas oficiais da UE](../../LANGUAGES.md)**

Um cliente Java independente, com **dependência zero de tempo de execução** para a Comissão Europeia VIES
(Sistema de troca de informações de IVA) para a API REST de um verificador de número fiscal. Qualquer Java
pode ser conectado a um servidor API ou programa nas seguintes linguagens: Spring Boot, Quarkus, Micronaut,
ou até mesmo JDK`HttpServer`simples — o módulo usa apenas o JDK (`java.net.http`),
não há árvore de dependência transitiva.

**Em outros nomes de pesquisa/palavras-chave de pesquisa:** Verificador VIES, verificador de IVA, número de IVA
verificador, validador de número de IVA, validação de IVA da UE, verificador de identificação fiscal da UE, número de imposto
validação, verificação do número fiscal, verificação do número fiscal comunitário, validação do número IVA.
Esta não é uma calculadora fiscal geral: apenas para verificação VIES de números fiscais da UE
Esta não é uma calculadora fiscal geral; valida os números de IVA da UE através do VIES.

Projeto independente de código aberto; não a Comissão Europeia, a UE ou os estados membros
é um produto oficial das autoridades fiscais e não é endossado ou certificado por elas.

- **Requisito:** Java **21+** bytecode/API; todo o pacote é verificado no JDK 21.
- **Documentação oficial do VIES:** <https://ec.europa.eu/taxation_customs/vies/#/technical-information>
- **Extremidade chamada:**`https://ec.europa.eu/taxation_customs/vies/rest-api/ms/{countryCode}/vat/{vatNumber}`
- **Contato do Estado-Membro:**`https://ec.europa.eu/taxation_customs/vies/rest-api/check-status`

## Documentação

- [Instalação](INSTALLATION.md)
- [Integração](INTEGRATION.md)
- [Projeto técnico](TECHNICAL.md)
- [Testes unitários, de integração e de concorrência](TESTING.md)
- [Código aberto e decisão de licença](OPEN_SOURCE.md)
- [Guia de lançamento](RELEASING.md)
- [Publicação no GitHub](GITHUB_SETUP.md)
- [Como contribuir](CONTRIBUTING.md)
- [Política de segurança](SECURITY.md)
- [Código de Conduta](CODE_OF_CONDUCT.md)
- [Suporte e doações](SUPPORT.md)
- [Avisos de terceiros](THIRD_PARTY_NOTICES.md)
- [Registro de alterações](CHANGELOG.md)

## Construir e conectar

```bash
./mvnw install    # tests + target/vies-client-1.2.0.jar (+ -sources.jar, -javadoc.jar)
               # install into the local Maven repository
```

**Maven:**

```xml
<dependency>
    <groupId>vies.client</groupId>
    <artifactId>vies-client</artifactId>
    <version>1.2.0</version>
</dependency>
```

**Gradle:**

```kotlin
implementation("vies.client:vies-client:1.2.0")
```

**Módulo JPMS real.** O jar se comporta como um módulo nomeado (`jar --describe-module`):

```
vies.client@1.2.0
exports vies.client
requires java.net.http
contains vies.client.internal   ← non-exported internal package
```

Conecte-se a partir de um aplicativo modularizado como este:

```java
module my.api.server {
    requires vies.client;
}
```

No Classpath (em um projeto "tradicional" não modularizado), funciona da mesma maneira -
`module-info`simplesmente não funciona neste caso. Mesmo sem Maven/Gradle
pode ser usado: a árvore de origem em`src/main/java` pode ser copiada para seu próprio projeto
(deixe`module-info.java`se seu projeto não for modularizado).

## Exemplo rápido

```java
import vies.client.*;

try (var vies = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER"))) // segredo/configuração confiável
        .retries(1)
        .build()) {

    // Accepts hyphens, spaces, and lowercase; maps GR to EL.
    switch (vies.check("DE 000 000 000")) {
        case ViesResponse.Valid v ->
            System.out.println("Valid: " + v.traderName().orElse("(name not public)")
                    + " — consultation number: " + v.consultationNumber().orElse("-"));
        case ViesResponse.Invalid i ->
            System.out.println("Invalid VAT number: " + i.vatNumber());
        case ViesResponse.Unavailable u ->
            System.out.println("VIES unavailable (" + u.errorCode() + "), retry later");
        case ViesResponse.MalformedInput m ->
            System.out.println("Malformed input: " + m.reason());
    }
}
```

`switch`é exaustivo: o compilador garante que você administrou todas as quatro saídas
(interface selada + correspondência de padrões).

### Por que`defaultRequester`é importante?

`defaultRequester` deve ser o número de IVA da própria organização, obtido de um
segredo ou configuração confiável. Para uma resposta válida, o VIES **pode**
retornar `consultationNumber`; o campo é opcional e nunca garantido. O seu valor
como prova jurídica depende das regras locais; guarde-o com a data e os dados da consulta.

## Conexão com o servidor Spring Boot API

```java
@Configuration
class ViesConfig {
    @Bean(destroyMethod = "close")
    ViesClient viesClient() {
        return ViesClient.builder()
                .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
                .retries(1)
                .build();
    }
}

@RestController
@RequestMapping("/api/vat")
class VatController {
    private final ViesClient vies;

    VatController(ViesClient vies) { this.vies = vies; }

    @GetMapping("/{number}")
    ResponseEntity<?> check(@PathVariable String number) {
        return switch (vies.check(number)) {
            case ViesResponse.Valid v -> ResponseEntity.ok(Map.of(
                    "valid", true,
                    "name", v.traderName().orElse(""),
                    "address", v.traderAddress().orElse(""),
                    "consultationNumber", v.consultationNumber().orElse("")));
            case ViesResponse.Invalid i -> ResponseEntity.ok(Map.of("valid", false));
            case ViesResponse.Unavailable u -> {
                var error = u.error().orElseThrow();
                yield ResponseEntity.status("CLIENT_OVERLOADED".equals(error.code()) ? 429 : 503)
                        .body(Map.of("errorCode", error.code(), "retryable", error.retryable(),
                                "messageHu", error.messageHu(), "messageEn", error.messageEn()));
            }
            case ViesResponse.MalformedInput m -> {
                var error = m.error().orElseThrow();
                yield ResponseEntity.badRequest().body(Map.of(
                        "errorCode", error.code(), "retryable", error.retryable(),
                        "messageHu", error.messageHu(), "messageEn", error.messageEn()));
            }
        };
    }
}
```

O cliente é imutável e seguro para threads — o aplicativo cria uma única instância
ciclo de vida (bean singleton) e fechamento no desligamento (`close`).

Padrão sem moldura: [`examples/ViesDemoServer.java`](../../../examples/ViesDemoServer.java)
(JDK`HttpServer`simples, em threads virtuais):

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java     # port 8085
curl "http://localhost:8085/vat-check?number=HU00000000"
```

## Uso assíncrono

```java
CompletableFuture<ViesResponse> future = vies.checkAsync("PL0000000000");
```

Por padrão, as chamadas assíncronas são executadas em **threads virtuais** (Project Loom) — muitas vezes
também não há desperdício de thread de plataforma na verificação paralela. Próprio executor a
pode ser especificado no construtor (`executor(...)`— não é fechado pelo cliente).

Até 512 **ações individuais de líderes assíncronos** da base de clientes assíncronos
recebido por instância do cliente. Uma única leitura de cache consome espaço por um curto período;
a entrada defeituosa e o mesmo seguidor de voo único não consomem espaço separado. Em caso de sobrecarga, a nova solicitação individual imediatamente
Você obtém um resultado de`Unavailable(..., "CLIENT_OVERLOADED")`. Isso é no cliente
restringe o trabalho; a fila de entrada e os futuros armazenados pelo chamador também
deve ser limitado.

## Planta de consulta multimilionária

Para milhões de usuários, não inicie milhões de futuros em uma JVM e não
permitir tráfego direto ilimitado para VIES. A estrutura proposta:

> O cliente pode ser um componente de processamento de uma fila de um milhão de trabalhos, mas o verdadeiro
> A transmissão do VIES é limitada pelos limites variáveis ​​e não garantidos dos sistemas da UE e dos estados membros
> ser determinado. Threads virtuais reduzem o custo de espera; a montante
> a capacidade não é aumentada.

1. As verificações recebidas são recebidas por uma fila de mensagens particionada e persistente.
2. Vários trabalhadores escalados horizontalmente consomem a linha em porções limitadas.
3. Um singleton`ViesClient`funciona com threads virtuais por trabalhador.
4. Os trabalhadores usam um cache Redis comum por meio da interface`ViesCache`.
5. O limitador de taxa global/distribuída protege os terminais VIES da UE e dos estados membros.

Dentro de uma JVM o cliente mescla o mesmo, chegando ao mesmo tempo
pares de número de imposto/consulta (voo único), portanto, uma falha no cache não causa isso
"solicitar debandada".`maxConcurrentRequests`é limitado pela rede real
solicitações e`maxPendingAsyncRequests`e`maxPendingSyncRequests`na memória
dá contrapressão instantânea. Todos são locais da JVM. Tráfego agregado de vários trabalhadores
deve ser regulado com um limitador comum e distribuído. A fila de entrada persistente e o
o conjunto de consumidores ainda deve ser limitado pela aplicação.

Exemplo de trabalhador de carga pesada:

```java
var vies = ViesClient.builder()
        .cache(redisViesCache)
        .cacheTtl(Duration.ofHours(24))
        .maxConcurrentRequests(32)
        .maxPendingSyncRequests(512)
        .maxPendingAsyncRequests(512)
        .admissionTimeout(Duration.ofSeconds(2))
        .retries(2)
        .retryDelay(Duration.ofMillis(250))
        .build();
```

O resultado`Unavailable`— incluindo o valor`CLIENT_OVERLOADED`— é persistente
você precisa tentar novamente com um atraso na fila.`MalformedInput`e`Invalid`não devem ser tentados novamente.

## Caminho de uma solicitação/Ciclo de vida da solicitação

1. **Normalizar / Normalizar:** verifique o código do país e o formato sem rede.
2. **Cache:** Retorno imediato de resultados válidos e ainda ativos.
3. **Voo único:** pares de código de imposto+consulta idênticos são mesclados em uma JVM.
4. **Admissão:** limites assíncronos e de rede protegem a memória e o VIES.
5. **HTTP:** conexão JDK`HttpClient`reutilizada com tempo limite curto.
6. **Verificação de validação/resposta:** booleano incompleto ou data →`MALFORMED_RESPONSE`.
7. **Escrita em cache:** apenas o resultado`Valid`autêntico é armazenado em cache.

## Respostas de erro bilíngues/Respostas de erro bilíngues

O código de erro da máquina é sempre estável e independente do idioma.`error()`é húngaro e inglês
fornece texto ao usuário e uma decisão de nova tentativa:

```java
var response = vies.check("HU00000000");
response.error().ifPresent(error -> {
    log.warn("{} | {} | retry={}", error.messageHu(), error.messageEn(), error.retryable());
});
```

| Resultado        |       http |           Tente novamente |   Cache | Significado                                                     |
| ---------------- | ---------: | ------------------------: | ------: | --------------------------------------------------------------- |
| `Valid`          |        200 |                       não | sim/sim | VIES confirmado como válido / VIES confirmado como válido       |
| `Invalid`        |        200 |                       não |     não | VIES não confirmou como válido / VIES não confirmou como válido |
| `Unavailable`    | 503 ou 429 | principalmente/geralmente |     não | Nenhuma decisão de validade foi tomada                          |
| `MalformedInput` |        400 |                       não |     não | A entrada deve ser corrigida / A entrada deve ser corrigida     |

## Configuração (construtor)

| Configuração                      | Valor base               | O que faz                                                                                    |
| --------------------------------- | ------------------------ | -------------------------------------------------------------------------------------------- |
| `baseUrl(String)`                 | URL REST oficial do VIES | Redirecionamento em teste simulado                                                           |
| `connectTimeout(Duration)`        | 5s                       | Tempo limite de conexão TCP/TLS                                                              |
| `requestTimeout(Duration)`        | 8s                       | Tempo limite total da solicitação (VIES é interativo, seja curto)                            |
| `admissionTimeout(Duration)`      | 2s                       | É quanto tempo ele espera por espaço livre na rede                                           |
| `defaultRequester(ViesRequester)` | não há                   | Número fiscal comunitário próprio → ID de consulta                                           |
| `retries(int)`                    | 0                        | Nova tentativa automática em erros transitórios (0-5, backoff exponencial + jitter)          |
| `retryDelay(Duration)`            | 400ms                    | Padrão de espera exponencial                                                                 |
| `maxConcurrentRequests(int)`      | 32                       | Limite superior de pedidos simultâneos reais da rede VIES                                    |
| `maxPendingSyncRequests(int)`     | 512                      | Limite de memória para chamadas sincronizadas simultâneas; acima dele`CLIENT_OVERLOADED`     |
| `maxPendingAsyncRequests(int)`    | 512                      | Limite de memória para operações assíncronas ativas/pendentes; acima dele`CLIENT_OVERLOADED` |
| `cacheTtl(Duration)`              | 24 horas                 | Tempo de cache para acessos válidos                                                          |
| `cacheMaxEntries(int)`            | 10.000                   | Limite de tamanho do cache de memória integrado                                              |
| `cache(ViesCache)`                | construído em            | Back-end de cache próprio (por exemplo, Redis)                                               |
| `disableCache()`                  | —                        | Sem cache persistido; chamadas paralelas idênticas podem partilhar uma solicitação single-flight |
| `userAgent(String)`               | id do módulo             | Você deve se identificar na UE                                                               |
| `executor(ExecutorService)`       | thread/tarefa virtual    | Meu executor assíncrono; o chamador é responsável pelo seu ciclo de vida                     |

### Cache próprio (por exemplo, Redis)

```java
class RedisViesCache implements ViesCache {
    public Optional<ViesResponse.Valid> get(String key) { /* ... */ }
    public void put(String key, ViesResponse.Valid value, Duration ttl) { /* ... */ }
}

var vies = ViesClient.builder().cache(new RedisViesCache()).build();
```

Apenas o resultado`Valid`é armazenado em cache – número inválido e erro transitório nunca.
Para adaptador Redis, use tempo limite de cache curto, namespace versionado e próprio
métrica de erro. Erro de leitura de cache`CACHE_ERROR`resulta quando o Redis está inativo
não inicie uma debandada descontrolada de solicitações VIES.

O `consultationNumber` e o `requestDate` retornados do cache pertencem à verificação
**original**; um acerto no cache não é uma nova verificação VIES. `disableCache()`
desativa apenas o cache persistido: chamadas simultâneas idênticas de VAT+requester
podem partilhar uma solicitação single-flight, enquanto uma chamada posterior à
conclusão faz uma nova solicitação. `consultationNumber` continua opcional.

## Semântica que vale a pena conhecer

1. **`Unavailable`≠`Invalid`.** Sistemas de antecedentes dos Estados-Membros regularmente
   são descartados (`MS_UNAVAILABLE`,`MS_MAX_CONCURRENT_REQ`...). Neste caso, o número a
   **não julgado pelo VIES** — comercialmente proibido de ser considerado inválido.
2. **A pré-filtragem de formato não é autenticação VIES.** O módulo filtra os registrados
   entrada errada (`MalformedInput`) antes de ir para a rede, mas a validade
   sua fonte é sempre a resposta do VIES.
3. **A nova tentativa também aumenta a carga.** Tente apenas um erro temporário localmente algumas vezes
   de novo; em uma operação grande, o mecanismo de nova tentativa atrasada da fila durável é o principal.
4. **Grécia`EL`, Irlanda do Norte`XI`.** O módulo lida com ambos,`GR`
   mapeia automaticamente a entrada para`EL`.

## Testes

```bash
./mvnw test    # testes unitários e testes HTTP locais de concorrência, repetição, contrapressão e ciclo de vida
```

---

## Guia de início rápido em inglês

Cliente Java 21+ de dependência zero para o número de IVA VIES da Comissão Europeia
API REST de validação. Construa com`./mvnw package`, então:

```java
try (var vies = ViesClient.createDefault()) {
    if (vies.check("DE000000000") instanceof ViesResponse.Valid v) {
        System.out.println(v.traderName().orElse("?"));
    }
}
```

Os resultados são uma hierarquia selada (`Valid`/`Invalid`/`Unavailable`/
`MalformedInput`) projetado para correspondência exaustiva de padrões`switch`. Válido
os resultados são armazenados em cache na memória por 24 horas; interrupções transitórias do VIES são relatadas como
`Unavailable`, nunca como`Invalid`. Documentação oficial da API:
<https://ec.europa.eu/taxation_customs/vies/#/technical-information>.

### Operação em alta escala

Esse cliente pode ser um componente de um pipeline de processamento de um milhão de itens, mas
o rendimento real do VIES é limitado por valores variáveis ​​e não garantidos da UE e dos estados-membros
limites. Threads virtuais reduzem o custo de bloqueio; eles não aumentam a montante
capacidade. Use uma fila particionada durável, consumidores limitados, um Redis compartilhado
cache e um limitador de taxa distribuído por todos os trabalhadores. Voo único local e
semáforos protegem apenas uma JVM. Nunca trate`Unavailable`como`Invalid`; usar
`response.error()` para códigos estáveis, capacidade de repetição e mensagens em húngaro/inglês.

## Código-fonte aberto / Código-fonte aberto

O projeto pode ser usado, modificado e
pode ser distribuído. A licença permite o uso comercial e expresso
concede uma licença de patente; os termos de licença, atribuição e modificação devem ser lidos
para proteger. Antes de contribuir, leia [CONTRIBUTING.md](CONTRIBUTING.md)e
[Arquivos CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

Este projeto está licenciado sob a [Licença Apache 2.0](../../../LICENSE), uma licença permissiva
licença com concessão explícita de patente. Consulte [CONTRIBUTING.md](CONTRIBUTING.md),
[SECURITY.md](SECURITY.md)e as [notas de código aberto](OPEN_SOURCE.md).

## Apoio e doações

O suporte da comunidade é fornecido com base no melhor esforço, por meio de questões e discussões do GitHub.
Sempre relate erros de segurança em um canal privado. A manutenção do projeto é significativa
envolve custos de desenvolvimento e infraestrutura; se isso economizou tempo para você, o desenvolvedor
[você pode convidá-lo para um café](https://buymeacoffee.com/lgx0).

O suporte da comunidade é o melhor esforço por meio de questões e discussões do GitHub. Segurança
os relatórios devem permanecer privados. Se o projeto economizou seu tempo, você pode
[compre um café para o desenvolvedor](https://buymeacoffee.com/lgx0).
