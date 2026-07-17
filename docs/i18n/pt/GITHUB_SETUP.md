# Português (pt) — GITHUB_SETUP

> [Todos os idiomas](../../LANGUAGES.md) · Tradução informativa. Em caso de divergência, prevalece a fonte técnica ou jurídica canónica em inglês. Apenas `LICENSE` e `NOTICE` na raiz têm valor jurídico; esta tradução não os substitui.

## 1. Verificações obrigatórias de pré-publicação

- Confirme que você tem o direito de publicar a fonte, a documentação e o nome.
- Verifique todo o diretório e, posteriormente, todo o histórico do Git em busca de segredos.
- Remover dados do cliente, número fiscal do solicitante real, token, arquivo`.env`e IDE
  arquivo específico da máquina.
- Verifique o Apache-2.0`LICENSE`,`NOTICE`e o arquivo de aviso de terceiros.
- Execute:`./mvnw --batch-mode --no-transfer-progress clean verify`.

- Confirme o direito de publicar todo o código, documentação e nomenclatura do projeto.
- Verifique o diretório e o eventual histórico do Git em busca de segredos.
- Remova dados do cliente, números de IVA reais do solicitante, tokens,`.env`e arquivos de máquina.
- Verifique os avisos do Apache-2.0 e de terceiros.
- Execute a verificação completa do Maven.

## 2. Crie o repositório / Crie o repositório

O projeto já inclui README, licença e arquivo`.gitignore`, daí o GitHub
não gere novos ao criar web. Nome sugerido:`vies-client`, visibilidade:
`Public`, ramificação padrão:`main`.

O projeto já contém arquivos README, licença e arquivos ignorados; não gere
duplicatas no GitHub. Nome sugerido:`vies-client`, visibilidade:`Public`, padrão
filial:`main`.

Comando que pode ser usado após confirmar o proprietário exato:

```bash
git init -b main
git add --all
git commit -m "Initial open-source release"
gh repo create OWNER/vies-client --public --source . --remote origin --push
```

Não execute o comando sem substituir`OWNER`e verificar a comparação completa.
Não execute-o antes de substituir`OWNER`e revisar a comparação completa.

## 3. Configurações do repositório

Recomendado:

- Problemas: habilitado;
- Discussões: habilitadas para dúvidas de uso;
- Wikis: desativados, a menos que sejam mantidos ativamente;
- Projetos: opcionais;
- Preservar este repositório: opcional após a primeira versão estável;
- Excluir automaticamente ramos principais: habilitado;
- Relatórios de vulnerabilidade privada: habilitado;
- Alertas do Dependabot e atualizações de segurança: habilitados;
- Verificação secreta e proteção push: habilitadas onde o GitHub as oferece.

## 4. Conjunto de regras vago de proteção de ramificação / Proteção de ramificação ou conjunto de regras

Uma ramificação`main`:

- solicitação pull necessária;
- pelo menos uma aprovação/pelo menos uma aprovação;
- dispensa de aprovação obsoleta após novos commits;
- verificações necessárias:`CI`,`CodeQL`,`Dependency review`conforme disponível;
- resolução de conversa necessária;
- forçar push e exclusão desativados;
- bypass administrativo usado apenas para emergências;
- commits/tags assinados recomendados para lançamentos.

Antes do primeiro push, o nome da verificação necessária ainda não existe; execute o fluxo de trabalho uma vez
kat, pode ser um conjunto de regras. /Os nomes de verificação obrigatória aparecem após o primeiro fluxo de trabalho
correr; configure o conjunto de regras posteriormente.

## 5. Rótulos e tópicos

Etiquetas sugeridas:

```text
bug, enhancement, documentation, localization, security, dependencies,
performance, concurrency, breaking-change, good-first-issue, help-wanted, triage
```

Descrição sugerida do repositório:

```text
Fast Java 21 VIES VAT checker and EU VAT number validator with virtual threads, bounded concurrency, cache, and bilingual errors.
```

Tópicos sugeridos:

```text
java, java21, java-library, vies, vat, vat-number, vat-checker, vat-validator,
vat-validation, vat-number-validation, eu-vat, tax-id, tax-id-checker,
tax-id-validation, rest-client, virtual-threads, single-flight, jpms,
zero-dependency
```

## 6. Botão Doar/Patrocinar

GitHub exibe o botão **Patrocinador** com base em`.github/FUNDING.yml`. Apenas o
insira um URL financeiro verificado pelo mantenedor. Exemplo:

```yaml
buy_me_a_coffee: lgx0
```

Uma chave dedicada também pode ser usada com um provedor compatível, por exemplo`github`,`ko_fi`
ou`custom`. Uma doação não constitui um SLA ou um direito de prioridade; veja`SUPPORT.md`.

GitHub exibe o botão Patrocinador de`.github/FUNDING.yml`. Configurar apenas um
destino de financiamento verificado pelo mantenedor. As doações não adquirem um SLA ou
direitos de governança.

## 7. Atualizações após o primeiro push / Atualizações após o primeiro push

Depois de saber o URL real do GitHub, atualize:

- `pom.xml`:`url`,`scm`,`developers`;
- README: URLs de crachás CI e CodeQL;
- `SECURITY.md`: URL de aconselhamento privado, se necessário;
- liberar documentação e coordenadas do Maven Central;
- `.github/FUNDING.yml`:`https://buymeacoffee.com/lgx0`.

## 8. Primeiro lançamento / Primeiro lançamento

Crie uma tag`v1.2.0`assinada somente após CI/CodeQL verde. Os testes de fluxo de trabalho de lançamento,
construa os JARs binários/sources/Javadoc e a soma de verificação SHA-256 e, em seguida, GitHub
Cria uma versão. Detalhes: [RELEASING.md](RELEASING.md).
