# Português (pt) — CONTRIBUTING

> [Todos os idiomas](../../LANGUAGES.md) · Tradução informativa. Em caso de divergência, prevalece a fonte técnica ou jurídica canónica em inglês. Apenas `LICENSE` e `NOTICE` na raiz têm valor jurídico; esta tradução não os substitui.

Obrigado por melhorar o projeto`vies-client`. O objetivo é previsível,
cliente Java VIES livre de dependências e seguro, mesmo sob carga pesada.

Obrigado por melhorar`vies-client`. O projeto pretende permanecer previsível,
livre de dependências em tempo de execução e seguro sob alta simultaneidade.

## Antes de começar / Antes de começar

- Para correção de erros, abra um problema ou consulte um existente.
- Vamos discutir primeiro as principais alterações de API, licença ou arquitetura em um problema.
- Não reportar erro de segurança em questão pública; consulte [SEGURANÇA.md](SECURITY.md).
- Abra ou faça referência a um problema para correção de bugs.
- Discuta as principais alterações de API, licenciamento ou arquitetura antes da implementação.
- Nunca divulgue uma vulnerabilidade em um assunto público; siga [SECURITY.md](SECURITY.md).

## Ambiente de desenvolvimento

Requer: JDK 21+, Maven 3.9+, Git. Controlar:

```bash
java -version
javac -version
./mvnw -version
```

Verificação local completa:

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
```

Instalação detalhada: [docs/INSTALLATION.md](INSTALLATION.md).

## Processo de desenvolvimento/fluxo de trabalho de desenvolvimento

1. Bifurque o repositório e crie uma ramificação de curta duração a partir da ramificação`main`.
2. Faça uma pequena mudança focada em um objetivo.
3. Adicione um teste de regressão determinístico para cada correção de bug.
4. Execute o comando`./mvnw clean verify`completo.
5. Abra uma solicitação pull e preencha todas as partes relevantes do modelo.

6. Bifurque o repositório e crie uma ramificação de curta duração a partir de`main`.
7. Mantenha as mudanças pequenas e focadas.
8. Adicione um teste de regressão determinístico para cada correção de bug.
9. Execute a verificação completa do Maven.
10. Abra uma solicitação pull e preencha o modelo fornecido.

Nomes de filiais sugeridos:

```text
fix/close-race
feat/cache-adapter-hook
docs/redis-example
```

## Regras de codificação

- Nível de linguagem Java 21; a API pública deve permanecer pequena e com segurança de tipo.
- O módulo de tempo de execução deve permanecer sem dependências externas, a menos que seja tomada uma decisão separada.
- Incerteza técnica`Unavailable`, nunca pode ser convertida em resultado`Invalid`.
- Todos os estados compartilhados devem ser thread-safe e com memória limitada.
- Javadoc/comentários em inglês e húngaro são necessários para API pública e lógica simultânea complexa.
- Não insira número fiscal completo, razão social, endereço ou número fiscal do próprio solicitante como dados de teste.
- Use Java 21 e mantenha a API pública pequena e com segurança de tipo.
- Mantenha o módulo de tempo de execução livre de dependências, a menos que seja explicitamente acordado de outra forma.
- Nunca converta`Unavailable`em`Invalid`.
- O estado compartilhado deve ser thread-safe e limitado pela memória.
- Documentar API pública e lógica de simultaneidade não óbvia em inglês e húngaro.
- Não comprometa ou registre dados privados de IVA, empresa, endereço ou solicitante.

## Regras de teste/Regras de teste

- O teste unitário não deve usar uma rede pública.
- Os testes HTTP e de competição só podem chamar um servidor simulado de loopback.
- São proibidos testes de carga, estresse ou CI contra VIES públicos.
- Situação de corrida deverá ser controlada por trava/barreira;`Thread.sleep`fixo não pode ser um oráculo de correção.
- O teste deverá falhar sem a correção.
- Os testes unitários não devem utilizar a rede pública.
- Os testes HTTP e de simultaneidade só podem chamar um servidor simulado de loopback.
- Nunca execute testes de carga, estresse ou CI necessários em VIES públicos.
- Conduzir corridas com trincos/barreiras; sonos fixos não são oráculos de correção.
- Um teste de regressão deve falhar sem a correção correspondente.

Catálogo de testes: [docs/TESTING.md](TESTING.md).

## Requisitos de solicitação pull / Requisitos de solicitação pull

O PR está pronto para revisão se:

- a construção e todos os testes foram bem-sucedidos;
- o comportamento público é documentado;
- nenhum vício novo e injustificado;
- a quebra de compatibilidade é destacada separadamente;
- o ajuste de potência inclui medição local reproduzível;
- o autor tem o direito de enviar o código.

Um PR está pronto quando a construção for verde, o comportamento público estiver documentado, novos
as dependências são justificadas, as alterações significativas são explícitas, as reivindicações de desempenho são
reproduzível, cabendo ao autor o direito de submeter o trabalho.

## Mudanças assistidas por IA / Mudanças assistidas por IA

A IA pode ser usada, mas o remetente assume total responsabilidade pelo resultado. Cada linha
deve ser verificado, testado e revisado para licenciamento. IA significativa-
indicar contribuição no PR; não forneça dados confidenciais ou protegidos por terceiros
modelo e não envie código gerado não verificado.

Ferramentas de IA são permitidas, mas o contribuidor permanece totalmente responsável. Revise e teste
cada linha, verificar a proveniência e o licenciamento, divulgar assistência substancial de IA em
o PR e nunca envie material gerado confidencial ou não revisado.

## Licença / Licença

O projeto está licenciado sob Apache-2.0. Consentimento enviando intencionalmente uma solicitação pull
é submetido incondicionalmente sob a Seção 5 da Licença Apache 2.0,
a menos que o remetente indique especificamente o contrário.

O projeto usa Apache-2.0. Ao enviar intencionalmente uma contribuição, você envia
sob a Seção 5 da Licença Apache 2.0 sem termos adicionais, a menos que você
declarar explicitamente o contrário.

## Comportamento/Conduta

Todos os participantes estão sujeitos a [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).
Todos os participantes devem seguir [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).
