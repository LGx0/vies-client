# Magyar (hu) — Harmadik fél összetevői
> [Nyelvválasztó](../../LANGUAGES.md) · Jogilag a gyökér angol `LICENSE`, `NOTICE` és `THIRD_PARTY_NOTICES.md` irányadó; ez információs fordítás.

A runtime JAR nem csomagol és nem igényel harmadik fél könyvtárat; JDK `java.base` és `java.net.http` modulokat használ. A JUnit Jupiter 6.1.2 (EPL-2.0) csak tesztfüggőség. Maven pluginek és tranzitív tesztfüggőségek nem kerülnek a JAR-ba, saját metaadataik/licencük szerint oldódnak fel. Forrás: <https://github.com/junit-team/junit-framework>. Függőséget hozzáadó PR frissítse a notice-t és dokumentálja a licenckompatibilitást.
