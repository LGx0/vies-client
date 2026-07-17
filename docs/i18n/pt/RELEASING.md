# Português (pt) — RELEASING

> [Todos os idiomas](../../LANGUAGES.md) · Tradução informativa. Em caso de divergência, prevalece a fonte técnica ou jurídica canónica em inglês. Apenas `LICENSE` e `NOTICE` na raiz têm valor jurídico; esta tradução não os substitui.

## 1. Pré-requisitos/Pré-requisitos

- limpar branch`main`e ações verdes do GitHub;
- autoridade apropriada do GitHub para criar a tag e liberá-la;
- JDK 21 e Maven 3.9+;
- versão de lançamento e data registrada no arquivo`CHANGELOG.md`.

## 2. Versionamento

O projeto usa versionamento semântico:

- `PATCH`: correção de bug compatível;
- `MINOR`: nova função compatível;
- `MAJOR`: quebra de API pública ou semântica.

O projeto usa Versionamento Semântico: patch para correções compatíveis, menor para correções compatíveis
recursos e principais para quebrar API ou alterações semânticas.

## 3. Verificação de pré-lançamento

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
jar --describe-module --file target/vies-client-1.0.0.jar
jdeps --print-module-deps target/vies-client-1.0.0.jar
```

Verifique também:

- sem segredos ou dados pessoais em todo o histórico do Git;
- `LICENSE`,`NOTICE`,`SECURITY.md`e documentação estão atuais;
- Javadoc e JAR de fontes criados;
- não há VIES obrigatório ao vivo ou teste de carga no CI;
- as alterações na API pública são incluídas no changelog.

## 4. Lançamento do GitHub

1. Defina a versão no arquivo`pom.xml`.
2. Confirme o changelog e a versão.
3. Crie uma tag anotada assinada:`git tag -s v1.0.0 -m "v1.0.0"`.
4. Envie commit e tag:`git push origin main --follow-tags`.
5. O fluxo de trabalho`release.yml`executa novamente os testes e, em seguida, anexa o binário,
   fontes e arquivos Javadoc JAR para GitHub Release.

Use tags anotadas assinadas quando possível. Nunca crie uma release a partir de uma versão não revisada ou
falhando no commit.

## 5. Pacotes Maven Central ou GitHub

A versão atual é local e está pronta para distribuição do GitHub Release. Mais para Maven Central
necessário:

- um DNS reverso próprio e verificável`groupId`;
- projeto`url`,`scm`,`developers`e metadados de gerenciamento de distribuição;
- Cadastro e token do Portal Central;
- Assinatura GPG e configuração de publicação compatível com Central.

A compilação atual está pronta para instalação local e versões do GitHub. Maven Central
a publicação requer adicionalmente um ID de grupo de DNS reverso de propriedade, projeto completo/SCM
metadados, credenciais do Portal Central, assinatura de artefatos e configuração de publicação.

Não coloque um token ou chave GPG privada no repositório. As ações do GitHub são secretas e mínimas
autorização de uso. / Nunca confirme tokens ou chaves de assinatura privadas. Utilize o GitHub
Segredos de ações e privilégios mínimos.

## 6. Etapas pós-lançamento / Pós-lançamento

- verifique downloads de lançamento e valores SHA-256;
- iniciar uma nova sessão`[Unreleased]`;
- publicar um comunicado de segurança do GitHub para um patch de segurança;
- atualize a versão documentada da dependência.
