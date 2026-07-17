# Gaeilge (ga) — Installation

> [Roghnóir teanga](../../LANGUAGES.md) · Cuirtear an logánú seo ar fáil ar mhaithe le hinrochtaineacht. Má bhíonn difríocht ann, is í an fhoinse chanónach theicniúil nó dhlíthiúil Bhéarla atá i réim. Fanann `LICENSE` agus`NOTICE` na fréimhe ceangailteach.

Díríonn an modúl seo ar Java 21 agus ní úsáideann sé ach an JDK ag am rite. Níl aon ghá
chuig an Earrach nó chuig leabharlann sheachtrach JSON/HTTP.
Díríonn an modúl seo ar Java 21 agus ní úsáideann sé ach an JDK ag am rite. Earraigh agus seachtrach
Níl leabharlanna JSON/HTTP ag teastáil.

## 1. Réamhriachtanais / Réamhriachtanais

- JDK 21 nó níos nuaí / JDK 21 nó níos nuaí
- Maven 3.9+ le haghaidh tógála foinse / Maven 3.9+ le haghaidh tógála foinse
- Rochtain HTTPS lasmuigh ar an seoladh `ec.europa.eu:443`/ rochtain amach HTTPS
  Seiceáil / Fíoraigh:

```bash
java -version
javac -version
./mvnw -version
```

Ba cheart go ndíreodh na trí ordú go léir ar an timpeallacht chéanna JDK 21+. Má tá Maven difriúil
Priontáil leagan Java, ceartaigh luach `JAVA_HOME` ar dtús.
Ba cheart go n-úsáidfeadh na trí ordú go léir an suiteáil JDK 21+ céanna. Má thuairiscíonn Maven a
leagan éagsúla de Java, socraigh `JAVA_HOME` ar dtús.

## 2. Cumraigh JDK 21 / Cumraigh JDK 21

## macOS + Homebrew

```bash
brew install openjdk@21
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

Chun socrú buan a fháil, athraigh an dá líne `export` go`~/.zprofile` agus `~/.zshrc` chuig comhad. Ar Intel Mac, de ghnáth is é `/usr/local` an réimír Homebrew .
Chun socrú leanúnach a dhéanamh, cuir an dá onnmhairiú chuig `~/.zprofile` agus `~/.zshrc`.
Is gnách an réimír Homebrew `/usr/local` ar Intel Macs.

## Linux

Suiteáil an pacáiste OpenJDK 21 de do dháileadh, ansin mar shampla:

```bash
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
```

Suiteáil pacáiste OpenJDK 21 do dháileadh agus pointe `JAVA_HOME` chuige.

## Windows PowerShell

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

Le haghaidh socruithe buana, bain úsáid as comhéadan Athróga Comhshaoil ​​Windows.
Úsáid Athróga Timpeallachta Windows le haghaidh cumraíochta marthanach.

## 3. Tógáil agus suiteáil áitiúil / Tógáil agus suiteáil áitiúil

Ó fhréamh an tionscadail:

```bash
./mvnw clean verify
./mvnw install
```

-`verify`: tástálacha aistriúcháin, aonaid agus comhtháthú áitiúil, cruthú JARanna.

- `install`: céanna, ansin suiteáil chuig stór áitiúil`~/.m2/repository`.
- `verify`: tiomsaíonn, ritheann tástálacha comhtháthú aonaid/áitiúla, agus cruthaíonn sé na JARanna.
- `install`: cuireann sé na déantáin isteach i stór áitiúil Maven freisin.
  Déantáin ghinte:

```text
target/vies-client-1.0.0.jar
target/vies-client-1.0.0-sources.jar
target/vies-client-1.0.0-javadoc.jar
```

## 4. Ceangal Maven / spleáchas Maven

Má ritheadh ​​`./mvnw install`/ Tar éis`./mvnw install` a rith roimhe seo:

```xml
<dependency>
    <groupId>vies.client</groupId>
    <artifactId>vies-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

Is déantán áitiúil é an modúl faoi láthair. Foilsíonn an eagraíocht i dtimpeallacht foirne/CI
Pacáistí Nexus, Artifactory nó GitHub, agus ansin é a úsáid as sin.
Tá an déantán áitiúil faoi láthair. Maidir le foirne agus CI, foilsigh chuig do Nexus é,
Artifactory, nó stór Pacáistí GitHub.

## 5. Ceangal gradle / Gradle dependency

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

## 6. Úsáid gan Maven/Gradle / Úsáid gan uirlis tógála

Feidhmchlár Classpath / Feidhmchlár Classpath:

```bash
javac -cp /path/to/vies-client-1.0.0.jar MyApplication.java
java -cp "/path/to/vies-client-1.0.0.jar:." MyApplication
```

Faoi Windows is é an deighilteoir rangchosán ná `;`, faoi Unix/macOS `:`.
Úsáideann Windows `;` mar an deighilteoir rangchosán; Úsáideann Unix/macOS `:`.

## 7. Úsáid modúl JPMS / úsáid modúl JPMS

I gcomhad `module-info.java` den fheidhmchlár:

```java
module my.application {
    requires vies.client;
}
```

Tiomsaigh agus rith:

```bash
javac --module-path vies-client-1.0.0.jar -d out src/module-info.java src/my/application/Main.java
java --module-path "vies-client-1.0.0.jar:out" -m my.application/my.application.Main
```

## 8. Cód Windsurf / USA

Socraíonn `.vscode/settings.json` sa tionscadal an síneadh Java go JDK 21.
Tá cosán Homebrew ann go sonrach don mheaisín: ar Intel Mac, Linux nó Windows
athscríobh chuig d'eolaire JDK 21 féin é. Tar éis modhnú, reáchtáil an
Ordú `Developer: Reload Window`.
Tugann an `.vscode/settings.json` san áireamh an síneadh Java chuig JDK 21
Tá cosán Homebrew sonrach don mheaisín; ionad é ar Intel macOS, Linux, nó Windows.
Rith `Developer: Reload Window` tar éis socruithe JDK a athrú.

## 9. Líonra agus seachfhreastalaí / Líonra agus seachfhreastalaí

Críochphointe riachtanach:

```text
https://ec.europa.eu/taxation_customs/vies/rest-api
```

Níl eochair API ag teastáil ón gcliant. Le haghaidh seachfhreastalaí corparáideach, JDK `HttpClient` ní mór socrú seachfhreastalaí a shonrú sa timpeallacht rite. Seachfhreastalaí briseadh tráchta TLS
Sa chás seo, ní mór deimhniú CA na cuideachta a shuiteáil i stór iontaobhais an JDK a úsáidtear.
Níl eochair API ag teastáil. Ní mór seachfhreastalaí corparáideach agus muinín CA a chumrú sa
am rite JDK/comhshaol.

## 10. Seiceáil suiteála tapa / Seiceáil deataigh suiteála

```bash
./mvnw -q test
java -cp target/classes examples/ViesDemoServer.java
```

I teirminéal eile:

```bash
curl "http://localhost:8085/vat-check?number=DE000000000"
```

Braitheann an tástáil deataigh bheo ar an líonra; Níor cheart go mbeadh sé éigeantach in CI. Na tástálacha gnáth
úsáideann siad freastalaí bréige áitiúil.
Braitheann an seiceáil deataigh beo ar an líonra agus níor cheart go geata CI. Gnáththástálacha
úsáid a bhaint as seachfhreastalaí áitiúil.

## 11. Earráidí coitianta / Fabhtcheartú

| Earráid / Fadhb                    | Réiteach / Deisigh                                                                                                                                              |
| ---------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `switch expressions ... Java 14`   | Ba cheart go mbeadh an tionscadal SDK agus an leibhéal teanga 21 / Socraigh SDK agus leibhéal teanga go 21                                                      |
| `release version 21 not supported` | Ritheann Maven leis an sean JDK; shocrú`JAVA_HOME`/ úsáideann Maven sean JDK; seasta`JAVA_HOME`                                                                 |
| `UnsupportedClassVersionError`     | Ba cheart go mbeadh an JVM reatha 21+ / Rith le JVM 21+                                                                                                         |
| `NETWORK_ERROR`                    | Seiceáil DNS / seachfhreastalaí / balla dóiteáin / socrú TLS / Seiceáil DNS, seachfhreastalaí, balla dóiteáin, agus TLS                                         |
| `TIMEOUT`                          | Ná cuir leis an teorainn ama ar feadh tréimhse éiginnte; an líonra agus sláinte VIES a fhiosrú / sláinte líonra/VIES a fhiosrú sula n-ardaítear amanna ama      |
| `CLIENT_OVERLOADED`                | Atriail mhoillithe, dul isteach teorantach, oibrithe móide teorannóir domhanda / Atriail mhoillithe, dul isteach teorantach, oibrithe móide teorannóir domhanda |
| `CACHE_ERROR`                      | Seiceáil Teorainn ama/sláinte/méadracht Redis; ná seachaint é le glao díreach VIES / Seiceáil sláinte Redis; ná seachaint é le stampede VIES                    |
