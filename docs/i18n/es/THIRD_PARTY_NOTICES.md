# Español (`es`) — Componentes de terceros

[Selector de idioma](../../LANGUAGES.md) · [Original inglés](../../../THIRD_PARTY_NOTICES.md)

> Traducción informativa. Los originales ingleses `LICENSE` y `NOTICE` son los únicos jurídicamente vinculantes.

El JAR distribuido no contiene ni necesita librerías runtime de terceros; solo módulos JDK `java.base` y `java.net.http`. La dependencia de test es JUnit Jupiter 6.1.2, licencia EPL-2.0, solo tests. Plugins Maven y transitivos no se empaquetan; Maven los resuelve con sus metadatos/licencias. Fuente: <https://github.com/junit-team/junit-framework>. Todo PR con dependencia debe actualizar este archivo y documentar compatibilidad.

EPL solo test separado; JDK según distribución. Plugins/transitivos pueden cambiar y CI inspecciona tree. Solo LICENSE/NOTICE ingleses vinculantes. Nueva componente documenta nombre, versión, scope, source, license, bundling y compatibility.
