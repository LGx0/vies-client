# Português (pt) — INSTALLATION

> [Todos os idiomas](../../LANGUAGES.md) · Tradução informativa. Em caso de divergência, prevalece a fonte técnica ou jurídica canónica em inglês. Apenas `LICENSE` e `NOTICE` na raiz têm valor jurídico; esta tradução não os substitui.

Este módulo é direcionado ao Java 21 e usa apenas o JDK em tempo de execução. Não há necessidade
para Spring ou uma biblioteca JSON/HTTP externa.

Este módulo é direcionado ao Java 21 e usa apenas o JDK em tempo de execução. Primavera e externo
Bibliotecas JSON/HTTP não são necessárias.

## 1. Pré-requisitos/Pré-requisitos

- JDK 21 ou mais recente / JDK 21 ou mais recente
- Maven 3.9+ para compilações de origem / Maven 3.9+ para compilações de origem
- Acesso HTTPS de saída para o endereço`ec.europa.eu:443`/ acesso HTTPS de saída

Verifique/Verifique:

```bash
java -version
javac -version
./mvnw -version
```

Todos os três comandos devem apontar para o mesmo ambiente JDK 21+. Se Maven for diferente
Imprima a versão Java, primeiro corrija o valor de`JAVA_HOME`.

Todos os três comandos devem usar a mesma instalação do JDK 21+. Se Maven relatar um
versão Java diferente, corrija`JAVA_HOME` primeiro.

## 2. Configurar JDK 21 / Configurar JDK 21

### macOS + Homebrew

```bash
brew install openjdk@21
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

Para uma configuração permanente, altere as duas linhas`export` para`~/.zprofile`e`~/.zshrc`
para um arquivo. Em um Mac Intel, o prefixo Homebrew geralmente é`/usr/local`.

Para uma configuração persistente, adicione ambas as exportações a`~/.zprofile`e`~/.zshrc`.
O prefixo Homebrew geralmente é`/usr/local`em Macs Intel.

###Linux

Instale o pacote OpenJDK 21 da sua distribuição e, por exemplo:

```bash
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
```

Instale o pacote OpenJDK 21 da sua distribuição e aponte`JAVA_HOME` para ele.

### Windows PowerShell

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

Para configurações permanentes, use a interface Variáveis ​​de ambiente do Windows.
Use variáveis ​​de ambiente do Windows para uma configuração persistente.

## 3. Compilação e instalação local / Compilação e instalação local

Da raiz do projeto:

```bash
./mvnw clean verify
./mvnw install
```

- `verify`: tradução, testes unitários e de integração local, criação de JARs.
- `install`: mesmo e instale no repositório`~/.m2/repository`local.
- `verify`: compila, executa testes de integração unitários/locais e cria os JARs.
- `install`: também instala os artefatos no repositório Maven local.

Artefatos gerados:

```text
target/vies-client-1.0.0.jar
target/vies-client-1.0.0-sources.jar
target/vies-client-1.0.0-javadoc.jar
```

## 4. Conexão Maven/Dependência Maven

Se`./mvnw install`/ Depois de executar`./mvnw install`foi executado antes:

```xml
<dependency>
    <groupId>vies.client</groupId>
    <artifactId>vies-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

O módulo é atualmente um artefato local. A organização publica em um ambiente de equipe/CI
Pacotes Nexus, Artifactory ou GitHub e use-os a partir daí.

O artefato é atualmente local. Para equipes e CI, publique-o em seu Nexus,
Artifactory ou repositório de pacotes GitHub.

## 5. Conexão Gradle/Dependência Gradle

```kotlin
repositories {
    mavenLocal()
}

dependencies {
    implementation("vies.client:vies-client:1.0.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
```

## 6. Use sem Maven/Gradle / Use sem uma ferramenta de construção

Aplicativo Classpath / Aplicativo Classpath:

```bash
javac -cp /path/to/vies-client-1.0.0.jar MyApplication.java
java -cp "/path/to/vies-client-1.0.0.jar:." MyApplication
```

No Windows, o separador do caminho de classe é`;`, no Unix/macOS é`:`.
O Windows usa`;`como separador de caminho de classe; Unix/macOS usa`:`.

## 7. Uso do módulo JPMS / uso do módulo JPMS

No arquivo`module-info.java`do aplicativo:

```java
module my.application {
    requires vies.client;
}
```

Compile e execute:

```bash
javac --module-path vies-client-1.0.0.jar -d out src/module-info.java src/my/application/Main.java
java --module-path "vies-client-1.0.0.jar:out" -m my.application/my.application.Main
```

## 8. Windsurf / Código dos EUA

`.vscode/settings.json`no projeto define a extensão Java para JDK 21.
O caminho do Homebrew é específico da máquina: em Intel Mac, Linux ou Windows
reescreva-o em seu próprio diretório JDK 21. Após a modificação, execute o
Comando`Developer: Reload Window`.

O`.vscode/settings.json`incluído aponta a extensão Java para JDK 21. Seu
O caminho do Homebrew é específico da máquina; substitua-o no Intel macOS, Linux ou Windows.
Execute`Developer: Reload Window`após alterar as configurações do JDK.

## 9. Rede e proxy / Rede e proxy

Ponto de extremidade necessário:

```text
https://ec.europa.eu/taxation_customs/vies/rest-api
```

O cliente não requer uma chave de API. Para proxy corporativo, JDK`HttpClient`
a configuração de proxy deve ser especificada no ambiente de tempo de execução. Proxy interrompendo o tráfego TLS
Neste caso, o certificado CA da empresa deve ser instalado no armazenamento confiável do JDK utilizado.

Nenhuma chave de API é necessária. O proxy corporativo e a confiança da CA devem ser configurados no
tempo de execução JDK/ambiente.

## 10. Verificação rápida da instalação / Verificação de fumaça da instalação

```bash
./mvnw -q test
java -cp target/classes examples/ViesDemoServer.java
```

Em outro terminal:

```bash
curl "http://localhost:8085/vat-check?number=DE000000000"
```

O teste de fumaça ativa depende da rede; Não deveria ser obrigatório em CI. Os testes normais
eles usam um servidor simulado local.

A verificação de fumaça ativa depende da rede e não deve bloquear o CI. Testes normais
use um servidor simulado local.

## 11. Erros comuns/solução de problemas

| Erro/Problema                      | Solução / Correção                                                                                                                                                                |
| ---------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `switch expressions ... Java 14`   | O SDK do projeto e o nível de idioma devem ser 21 / Defina o SDK e o nível de idioma como 21                                                                                      |
| `release version 21 not supported` | Maven roda com o antigo JDK; corrigir`JAVA_HOME`/Maven usa um JDK antigo;`JAVA_HOME`fixo                                                                                          |
| `UnsupportedClassVersionError`     | A JVM em execução deve ser 21+/Executar com JVM 21+                                                                                                                               |
| `NETWORK_ERROR`                    | Verifique a configuração de DNS/proxy/firewall/TLS / Verifique DNS, proxy, firewall e TLS                                                                                         |
| `TIMEOUT`                          | Não aumente o tempo limite indefinidamente; investigue a rede e a integridade do VIES / Investigue a integridade da rede/VIES antes de aumentar os tempos limite                  |
| `CLIENT_OVERLOADED`                | Nova tentativa atrasada, entrada limitada, trabalhadores mais limitador global / Nova tentativa atrasada, entrada limitada, trabalhadores mais limitador global                   |
| `CACHE_ERROR`                      | Verifique o tempo limite/saúde/métricas do Redis; não ignore isso com uma chamada direta em massa do VIES / verifique a integridade do Redis; não o ignore com uma debandada VIES |
