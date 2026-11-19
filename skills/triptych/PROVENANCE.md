# Provenance

- **ArchUnit rules** (`TriptychArchitecture`, `TriptychArchitectureConfig`, `ArchitectureTest`, `archunit.properties`), the **module shape** (`core`, `inbound-*`, `outbound-*`, `app-server`), the jandex plugin and the adapter patterns in `references/modules/`: from [svene/2026-07-26_quarkus-hexagon-demo-mm-per-tech](https://github.com/svene/2026-07-26_quarkus-hexagon-demo-mm-per-tech/tree/b2f849396eb60a3aae38918c7fab3033c93bb1d9) at `b2f8493`. The changes made when copying:
  - the two SPI rules allow an empty match
  - `@RecordBuilder` is replaced by hand-written withers
  - the JAR filter matches the root package path
  - only the Triptych rules of `ArchitectureTest` are kept
  - the custom `dead-letter-or-fail-stop` Kafka failure strategy is replaced by the standard `dead-letter-queue`
- **Naming table** for the single-module variant: from [svene/2026-07-03_quarkus_triptych-architecture-demo-sm](https://github.com/svene/2026-07-03_quarkus_triptych-architecture-demo-sm/blob/bab55362b7d2d8cbbae539e7e1e8f9ff6e0075af/readme.md#L7-L11) at `bab5536`.
- Decided in [Decide the triptych architecture skill](https://github.com/svene/2026-11-17_ai-skills/issues/11). Amended by the decisions in issues #12 to #15.
