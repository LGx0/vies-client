# Project instructions for coding agents

- Keep the public API, source level and bytecode baseline on Java 21. Do not raise
  `maven.compiler.release` without an explicit compatibility decision and a coordinated
  documentation/CI update. JDK 25 LTS is the preferred development and analysis runtime.
- Treat Maven as the build authority. Run `java -version`, `javac -version` and
  `mvn -version` with the same selected JDK installation, then verify with
  `mvn --batch-mode --no-transfer-progress clean verify`. CI and release validation must
  continue to include JDK 21; normal development and CodeQL should also run on JDK 25.
- Never diagnose source failures from stale files under `target/`. VS Code/JDT may have
  written Eclipse error stubs there; use a clean Maven build before drawing conclusions.
- Tests compile and run on the classpath so their `jdk.httpserver` fixture remains a
  test-only concern. Do not add `jdk.httpserver` to the published `module-info.java`.
- Keep VAT fixtures synthetic (for example all-zero format-valid values) and internally
  consistent. Static-analysis cleanup must not change a test input without updating its
  matching expected national/full VAT values and echoed response fields.
