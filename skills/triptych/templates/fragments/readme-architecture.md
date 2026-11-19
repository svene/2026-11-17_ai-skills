
## Architecture

Hexagonal architecture in the Triptych variant: one Maven module per direction and technology (`core`, `inbound-*`, `outbound-*`, `app-server`), packages cut into `feature.<x>` and `cross.<y>`, roles `*Handler`, `*SPI`, `*Receiver`, `*Service`. `ArchitectureTest` in `app-server` enforces the rules the module graph cannot.

- [Triptych concepts](https://github.com/svene/2026-07-26_quarkus-hexagon-demo-mm-per-tech/blob/b2f849396eb60a3aae38918c7fab3033c93bb1d9/concepts.md)
- [Hexagonal → Triptych naming](https://github.com/svene/2026-07-03_quarkus_triptych-architecture-demo-sm/blob/bab55362b7d2d8cbbae539e7e1e8f9ff6e0075af/readme.md#L7-L11)
