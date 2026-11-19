# Provenance

- **Parent + `app-server` shape**, the BOM import, the compiler/surefire configuration and the `quarkus-maven-plugin` goals: from [svene/2026-07-26_quarkus-hexagon-demo-mm-per-tech](https://github.com/svene/2026-07-26_quarkus-hexagon-demo-mm-per-tech/tree/b2f849396eb60a3aae38918c7fab3033c93bb1d9) at `b2f8493`, stripped of every module except `app-server` and of ArchUnit, record-builder, the enforcer and jandex.
- **Maven wrapper** (`mvnw` only-script, wrapper 3.3.4, Maven 3.9.16), plugin versions (compiler 3.15.0, surefire 3.5.6), the `quarkus-junit` artifact name and the default Quarkus version (3.40.1.2): from the output of `quarkus-maven-plugin:3.36.3:create` on 2026-11-19.
- Decided in [Decide the Bare project skill](https://github.com/svene/2026-11-17_ai-skills/issues/16).
