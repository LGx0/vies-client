# Português (pt) — INTEGRATION

> [Todos os idiomas](../../LANGUAGES.md) · Tradução informativa. Em caso de divergência, prevalece a fonte técnica ou jurídica canónica em inglês. Apenas `LICENSE` e `NOTICE` na raiz têm valor jurídico; esta tradução não os substitui.

## 1. Ciclo de vida / Ciclo de vida

Use um único objeto`ViesClient`em um aplicativo ou instância de trabalho.
Não crie um cliente por solicitação HTTP: você perderia o pool de conexões, o cache,
fusão de voo único e limites locais.

Use um`ViesClient` por aplicativo/processo de trabalho. Não crie um cliente por
Solicitação HTTP; fazer isso descarta o pool de conexões, o cache, o single-flight e o local
limites.

```java
var client = ViesClient.builder()
        .connectTimeout(Duration.ofSeconds(5))
        .requestTimeout(Duration.ofSeconds(8))
        .admissionTimeout(Duration.ofSeconds(2))
        .maxConcurrentRequests(32)
        .maxPendingSyncRequests(512)
        .maxPendingAsyncRequests(512)
        .retries(1)
        .build();

// Application shutdown / Alkalmazásleállítás
client.close();
```

Uma nova operação não pode ser iniciada após`close()`. O desligamento interrompe o interno ativo
operações e retorna`CLIENT_CLOSED` para solicitações comuns em processo.
Os métodos sync e async chamados diretamente depois são ambos síncronos
Uma exceção`IllegalStateException`é lançada.

Após`close()`nenhum novo trabalho será aceito. O desligamento interrompe o trabalho interno ativo
e conclui solicitações compartilhadas em trânsito com`CLIENT_CLOSED`.
Novas chamadas de API sincronizadas e assíncronas feitas posteriormente geram`IllegalStateException`
sincronicamente.

## 2. API Síncrona / API Síncrona

```java
ViesResponse response = client.check("DE 000 000 000");

switch (response) {
    case ViesResponse.Valid valid ->
        System.out.println("VALID: " + valid.vatNumber());
    case ViesResponse.Invalid invalid ->
        System.out.println("INVALID: " + invalid.vatNumber());
    case ViesResponse.Unavailable unavailable -> {
        var error = unavailable.error().orElseThrow();
        System.out.println(error.messageHu());
        System.out.println(error.messageEn());
    }
    case ViesResponse.MalformedInput malformed -> {
        var error = malformed.error().orElseThrow();
        System.out.println(error.messageHu());
        System.out.println(error.messageEn());
    }
}
```

O número de chamadores de API síncronos também é limitado (`maxPendingSyncRequests`). No limite
a solicitação válida acima obtém imediatamente o resultado`CLIENT_OVERLOADED`.

Os chamadores síncronos também são limitados por admissão por`maxPendingSyncRequests`.

## 3. API assíncrona / API assíncrona

```java
client.checkAsync("PL0000000000")
        .thenAccept(response -> {
            // Handle all four response variants / Kezeld mind a négy eredményt.
        });
```

A API assíncrona é executada em um thread virtual com o executor padrão; próprio executor
suas regras de agendamento se aplicam. Número fiscal idêntico + pedidos de consulta são um
eles se conectam a um futuro interno compartilhado. O chamador recebe uma cópia à prova de cancelamento:
o cancelamento de um único consumidor não interrompe o pedido conjunto dos demais.

Com o executor padrão, a API assíncrona é executada em threads virtuais; um executor personalizado
usa sua própria política de agendamento. Chamadas idênticas de IVA/solicitante juntam-se a uma chamada interna
futuro compartilhado. Cada chamador recebe uma cópia segura para cancelamento.

Não armazene milhões de futuros na memória. O consumidor da fila persistente é sempre limitado
mantenha a janela ativa.

Não retenha milhões de futuros. Um consumidor de fila durável deve manter um limite
janela de processamento.

## 4. Contrato API HTTP/contrato API HTTP

Mapeamento recomendado:

| ViesResposta                     | http |        Tente novamente | Nota / Nota                              |
| -------------------------------- | ---: | ---------------------: | ---------------------------------------- |
| `Valid`                          |  200 |                    não | resultado do domínio                     |
| `Invalid`                        |  200 |                    não | resultado de domínio, não uma falha HTTP |
| `MalformedInput`                 |  400 |                    não | chamador deve corrigir a entrada         |
| `Unavailable(CLIENT_OVERLOADED)` |  429 |               atrasado | contrapressão local                      |
| outro`Unavailable`               |  503 | por`error.retryable()` | nenhuma decisão de validade              |

Exemplo de erro JSON / Exemplo de erro JSON:

```json
{
  "status": "UNAVAILABLE",
  "vatNumber": "HU00000000",
  "errorCode": "MS_UNAVAILABLE",
  "messageHu": "A tagállami adóhatóság rendszere átmenetileg nem érhető el.",
  "messageEn": "The member state's tax system is temporarily unavailable.",
  "retryable": true
}
```

Texto do usuário`messageHu`/`messageEn`. Para log, métrica e lógica do cliente
sempre use o valor estável`errorCode`.

`messageHu`/`messageEn`são voltados para o usuário. Use valores`errorCode`estáveis ​​para registros,
métricas e lógica do cliente.

## 5. Bota de mola

### Configuração

```java
@Configuration
class ViesConfiguration {
    @Bean(destroyMethod = "close")
    ViesClient viesClient(ViesCache cache) {
        return ViesClient.builder()
                .cache(cache)
                .connectTimeout(Duration.ofSeconds(5))
                .requestTimeout(Duration.ofSeconds(8))
                .admissionTimeout(Duration.ofSeconds(2))
                .maxConcurrentRequests(32)
                .maxPendingSyncRequests(512)
                .maxPendingAsyncRequests(512)
                .retries(1)
                .build();
    }
}
```

### Controlador

```java
@RestController
@RequestMapping("/api/v1/vat")
class VatController {
    private final ViesClient vies;

    VatController(ViesClient vies) {
        this.vies = vies;
    }

    @GetMapping("/{vatNumber}")
    ResponseEntity<?> check(@PathVariable String vatNumber) {
        return switch (vies.check(vatNumber)) {
            case ViesResponse.Valid v -> ResponseEntity.ok(Map.of(
                    "status", "VALID",
                    "vatNumber", v.vatNumber(),
                    "name", v.traderName().orElse(""),
                    "address", v.traderAddress().orElse(""),
                    "requestDate", v.requestDate().toString(),
                    "fromCache", v.fromCache()));
            case ViesResponse.Invalid i -> ResponseEntity.ok(Map.of(
                    "status", "INVALID",
                    "vatNumber", i.vatNumber(),
                    "requestDate", i.requestDate().toString()));
            case ViesResponse.MalformedInput m -> problem(400, m);
            case ViesResponse.Unavailable u -> problem(
                    "CLIENT_OVERLOADED".equals(u.errorCode()) ? 429 : 503, u);
        };
    }

    private static ResponseEntity<?> problem(int status, ViesResponse response) {
        var error = response.error().orElseThrow();
        return ResponseEntity.status(status).body(Map.of(
                "errorCode", error.code(),
                "retryable", error.retryable(),
                "messageHu", error.messageHu(),
                "messageEn", error.messageEn()));
    }
}
```

A biblioteca não possui dependências do Spring; o código acima faz parte do aplicativo de consumo.
A biblioteca não tem dependência do Spring; este adaptador pertence ao aplicativo consumidor.

## 6. Servidor HTTP JDK simples

O exemplo executável completo é`examples/ViesDemoServer.java`.
Exemplo executável completo:`examples/ViesDemoServer.java`.

```bash
./mvnw -q package
java -cp target/classes examples/ViesDemoServer.java 8085
curl "http://localhost:8085/vat-check?number=DE000000000"
```

O exemplo usa threads virtuais, status HTTP reais e respostas de erro bilíngues.
O exemplo usa threads virtuais, status HTTP significativos e erros bilíngues.

## 7. Solicitante e ID da consulta

```java
var client = ViesClient.builder()
        .defaultRequester(ViesRequester.of(System.getenv("MY_EU_VAT_NUMBER")))
        .build();
```

O solicitante deve ser o número de IVA da própria organização, carregado por
`MY_EU_VAT_NUMBER` a partir de um segredo ou configuração confiável. Para uma
resposta válida, o VIES **pode** retornar `requestIdentifier`/`consultationNumber`,
mas o campo é opcional, nunca garantido e o valor probatório depende das regras locais.

Num acerto de cache, o identificador e `requestDate` pertencem à verificação original.
`disableCache()` desativa apenas o cache persistido: chamadas simultâneas idênticas
de VAT+requester podem partilhar uma solicitação de rede single-flight; uma chamada
posterior à conclusão faz uma nova solicitação.

## 8. Adaptador de cache Redis

```java
final class RedisViesCache implements ViesCache {
    private final RedisClient redis; // application-specific adapter

    RedisViesCache(RedisClient redis) {
        this.redis = redis;
    }

    @Override
    public Optional<ViesResponse.Valid> get(String key) {
        // Use a short, bounded Redis timeout / Használj rövid Redis timeoutot.
        return redis.get("vies:v1:" + key).map(this::decode);
    }

    @Override
    public void put(String key, ViesResponse.Valid value, Duration ttl) {
        redis.set("vies:v1:" + key, encode(value), ttl);
    }

    // encode/decode are application-specific and must preserve every record field.
}
```

Requisitos/Requisitos:

- implementação thread-safe;
- tempo limite curto de conexão/comando;
- namespace de chave versionada;
- métricas e alertas de saúde;
- nenhuma nova tentativa interna ilimitada;
- serialização correta de`requestDate`, opcionais e`fromCache`.

A exceção de leitura causa o resultado`CACHE_ERROR`e não aciona o fallback do VIES.
Uma exceção de gravação não exclui o resultado`Valid`autêntico já recebido.

Uma exceção de leitura retorna`CACHE_ERROR`sem um substituto VIES. Uma exceção de gravação
não apaga um resultado`Valid`oficial.

## 9. Tentar novamente, fila é DLQ / Tentar novamente, fila e DLQ

No processamento em alta escala:

1. persistir o trabalho antes do processamento;
2. utilizar uma chave de idempotência;
3. ligue para VIES com pequena contagem de novas tentativas locais;
4. se`error.retryable()`, agendar nova tentativa atrasada com atraso exponencial;
5. parar após um máximo configurado e passar para DLQ/revisão manual;
6. Nunca tente novamente`Invalid`ou`MalformedInput`inalterado.

Não mantenha tentativas duráveis ​​na memória com a cadeia`CompletableFuture`.
Não implemente novas tentativas duráveis ​​como cadeias`CompletableFuture`na memória.

## 10. Nó/pod Multiple / Vários nós ou pods

Cada réplica tem seu próprio conjunto de conexões, cache, tabela de voo único e semáforo
pegar.`maxConcurrentRequests(32)`, portanto, **não é global 32**.

Cada réplica tem seu próprio pool de conexões, cache, tabela de voo único e
semáforos. Portanto,`maxConcurrentRequests(32)`**não é um limite global de 32**.

Componentes necessários em grande escala/Requeridos em escala:

- fila particionada durável;
- contagem/janela limitada de consumidores;
- limitador de taxa distribuída/global;
- cache compartilhado onde a semântica de negócios permitir;
- nova tentativa atrasada e DLQ;
- idempotência/desduplicação;
- escalonamento automático com base na idade da fila, não na simultaneidade direta e ilimitada do VIES.

## 11. Saúde é observabilidade

- A vitalidade não deve depender do VIES.
- `availability()`deve ser um diagnóstico / pesquisa raro em cache e armazená-lo em cache com moderação.
- Mérd a latência p50/p95/p99-t / Medir a latência p50/p95/p99.
- Contagem por tipo de resultado e`errorCode`/ Contagem por resultado e código de erro.
- Meça o acerto do cache, novas tentativas, sobrecarga, idade da fila e DLQ.
- Meça ocorrências de cache, novas tentativas, sobrecargas, idade da fila e tamanho DLQ.
- Mascarar IVA/nome/endereço nos logs.

## 12. Antipadrões / Soluções a serem evitadas

- cliente por solicitação/novo cliente por solicitação;
- futuros ilimitados ou sincronizadores de chamadas
- tratar um limitador por pod como global/ver um limitador local como global;
- tentar novamente todos os erros imediatamente
- conversão de`Unavailable`em`Invalid`;
- tratar os dados de consulta armazenados em cache como uma nova prova;
- chamar VIES ao vivo a partir de verificações de atividade ou testes de unidade padrão.

## 13. Lista de verificação de produção

- [ ] O ciclo de vida e o desligamento do Singleton estão configurados.
- [] Todas as quatro variantes`ViesResponse`são tratadas explicitamente.
- [ ] API retorna código estável mais mensagens HU/EN.
- [] Os limites de sincronização, assíncrono, entrada e rede são limitados.
- [] O tráfego de vários nós usa um limitador global/distribuído.
- [] A atualização do cache e a política à prova de consulta foram aprovadas.
- [] Existem novas tentativas atrasadas, máximo de tentativas, idempotência e DLQ.
- [] Existem métricas, alertas e logs mascarados.
- [ ] A atividade é independente do VIES.
- [ ] São definidos testes de unidade, integração local, simultaneidade, carga, absorção e falha.
