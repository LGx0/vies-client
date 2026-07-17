# Português (pt) — CHANGELOG

> [Todos os idiomas](../../LANGUAGES.md) · Tradução informativa. Em caso de divergência, prevalece a fonte técnica ou jurídica canónica em inglês. Apenas `LICENSE` e `NOTICE` na raiz têm valor jurídico; esta tradução não os substitui.

Mudanças significativas estão documentadas neste arquivo. O projeto é semântico
segue versionamento:`MAJOR.MINOR.PATCH`.

Todas as mudanças notáveis ​​estão documentadas aqui. O projeto segue Versionamento Semântico:
`MAJOR.MINOR.PATCH`.

## [Não lançado]

### Adicionado

- Arquivos de integridade da comunidade GitHub, automação de CI/segurança e governança de código aberto.
- Licenciamento de projetos Apache License 2.0 e metadados Maven.
- Pacotes de documentação nas 24 línguas oficiais da UE.
- Integração do GitHub Sponsor/Buy Me a Coffee e termos de descoberta multilíngues.

### Mudado

- Conjunto de ferramentas de teste atualizado para JUnit Jupiter 6.1.2 e plug-ins Maven estáveis ​​atuais.
- A conclusão do desligamento agora usa threads virtuais, evitando a amplificação de threads nativos
  quando muitas operações são fechadas simultaneamente.

## [1.0.0] - 17/07/2026

### Adicionado

- Java 21, cliente VIES REST com dependência de tempo de execução zero.
- APIs síncronas e assíncronas com padrões de thread virtual.
- Sincronização local, assíncrona e acesso à rede de saída.
- União de solicitação de voo único local da JVM.
- Cache TTL limitado e ponto de extensão`ViesCache`externo.
- Validação de resposta rigorosa e hierarquia de resultados selada.
- Erros estruturados bilíngues húngaro/inglês estáveis.
- Tente novamente com espera exponencial e jitter.
- Testes determinísticos de unidade, HTTP, simultaneidade, cancelamento e desligamento.
- Documentação de instalação, integração, técnica e testes.
