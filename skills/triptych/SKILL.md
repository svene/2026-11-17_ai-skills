---
name: triptych
description: Apply the Triptych architecture (hexagonal; feature/cross slices; Handler/SPI/Receiver/Service) to a multi-module Quarkus project. Use when the Setup guide hands off to it, or the user asks for Triptych or hexagonal architecture, a new inbound/outbound Technology module, a new feature slice, or a Triptych conformance check.
---

# Triptych architecture

Applies the **Triptych architecture**, the author's variant of hexagonal architecture, to an existing multi-module Quarkus project, and keeps it there: ArchUnit rules in `app-server` turn every violation into a red `./mvnw verify`.

Arguments: `$ARGUMENTS`

## The architecture

**Modules.** One concern per Maven module:

| Module | Holds | Depends on |
|---|---|---|
| `core` | the hexagon: domain, `*Handler`s, `*SPI`s | no adapter module |
| `inbound-<tech>` | `*Receiver`s for one inbound technology (e.g. `inbound-kafka`) | `core` |
| `outbound-<tech>` | `*Service`s for one outbound technology (e.g. `outbound-postgres`) | `core` |
| `app-server` | assembly only: no main sources; the boot test, the adapters' `@QuarkusTest`s, the ArchUnit rules | `core` and every adapter module |

A Technology module is added when a technology is added, never when a feature is added.

**Packages.** Every module shares one package tree under the root package:

- `feature.<x>`: one slice per feature. Slices never depend on each other.
- `cross.<y>`: cross-cutting concerns (audit log, shared value types). Every feature may use them.

The same package therefore spans several modules. Quarkus logs a *split package* warning for this at build time. It is expected; leave it.

**Roles.** The class name's suffix gives its role, and the role fixes its module:

| Role | Suffix | Module |
|---|---|---|
| use case (inbound port) | `*Handler`, a concrete CDI bean | `core` |
| outbound port | `*SPI`, a public interface | `core` |
| inbound adapter | `*Receiver` | `inbound-*` |
| outbound adapter | `*Service`, implements an `*SPI` | `outbound-*` |

A Receiver calls a Handler. A Handler calls SPIs and `cross` code, never another Handler. Inbound code never touches an SPI. There are no `*API` interfaces: the Handler is the inbound port.

Coming from the single-module variant, rename Primary Adapter → `*Receiver`, `*Port` → drop it and use the Handler, Secondary Port → `*SPI`. `*Handler` and `*Service` keep their names.

**Rules.** `app-server/src/test/java/<package>/devsupport/` holds `TriptychArchitecture`, `TriptychArchitectureConfig` and `ArchitectureTest`, owned by the project once copied. They check that feature slices are independent, that SPIs are public interfaces, that every role class sits under `feature` or `cross`, and that no `inbound-*` class depends on an SPI. `archunit.properties` sets `archRule.failOnEmptyShould=true`, so a scan that finds nothing fails too. The module graph enforces the rest: `core` cannot see adapters, and adapters cannot see each other.

**Tests.** Where a test goes depends on what it checks:

| Code | Test | Module |
|---|---|---|
| domain, Handlers | plain JUnit + AssertJ | `core` |
| Receivers and Services | `@QuarkusTest` against the booted app (RestAssured for HTTP, Dev Services for Kafka or a database) | `app-server` |
| logic in an adapter module beyond translating a call (a view model's calculation, error classification, a startup check) | plain JUnit + AssertJ, no Quarkus boot | that adapter module |

Do not unit-test a Receiver or Service by calling its methods. What can break in one lives in the framework: paths, the JSON shape, status codes, headers, CDI wiring. Only the booted app shows that, and only `app-server` boots, because the SPI implementations and the configuration are there. When a Receiver grows real logic, move it into a class of its own and unit-test that class in the module, adding `org.junit.jupiter:junit-jupiter` and `org.assertj:assertj-core` (test scope; the Quarkus BOM manages the versions) to the module's pom.

## Inputs

| Input | Default |
|---|---|
| project directory | the current directory |
| root package | the package of `app-server`'s `AppServerBootTest`, or else the common prefix of the main sources |

When the arguments contain a `from: setup-guide; …` line, take `dir=` and `package=` from it. With `module=<name>` there, add that Technology module (feature `example`); otherwise `fresh: true` means the project is fresh, so go straight to the fresh branch. That caller has already confirmed, so skip every **Confirm** step below, and leave all commits to it.

## Steps

1. **Detect the project's shape.** Read the parent `pom.xml` and the module directories. Done when the project matches exactly one branch:

   | Shape | Branch |
   |---|---|
   | parent lists only `app-server`, no `core/`, no `app-server/src/main/java` | **fresh**: step 2 |
   | single-module (no `<modules>`) | stop: tell the user the skill needs a multi-module project and that the bare-project skill shows the shape. Nothing is changed. |
   | multi-module, without `core` + role-suffixed classes | **migrate**: follow [references/migrate.md](references/migrate.md) |
   | multi-module with `core`, `*Handler`s and adapter modules | **conformance check**: follow [references/conformance-check.md](references/conformance-check.md) |

   Explicit operations on a Triptych project skip detection:
   - add a Technology module: follow [references/technology-module.md](references/technology-module.md).
   - add a feature slice: see **Feature slices** below.

2. **Confirm** (fresh). Show one line, `Triptych in <dir>: core + inbound-http-jsonapi, example feature, ArchUnit rules, package <p>`, and wait for a yes.
3. **Apply** (fresh). Run
   ```sh
   ${CLAUDE_SKILL_DIR}/scripts/run.sh <dir> [--package <p>]
   ```
   then `./mvnw verify` in `<dir>`. Done when both exit 0. On a failure, show the error and stop.
4. **Report** what was added: the modules, the `feature.example` slice (`ExampleReceiver` → `ExampleHandler`, `GET /api/example?name=…`), the rules, and the README section. Mention that `feature.example` exists only as a pattern to copy and can be deleted once a real feature exists.

## Feature slices

A new feature `<x>` gets the package `<root>.feature.<x>` in every module that has code for it:

- `core`: the `<X>Handler` plus the domain types, and an `<Name>SPI` per outbound need.
- each `inbound-*` module that drives the feature: a `<Name>Receiver` injecting the Handler.
- each `outbound-*` module that serves an SPI: a `<Name>Service` implementing it.

Code shared by two features moves to `cross.<y>`; neither feature imports the other. Done when `./mvnw verify` is green, which includes `ArchitectureTest`.

## Scripts

- `scripts/run.sh <dir> [--package <p>]`: the fresh branch only. It refuses any other shape.
- `scripts/add-rules.sh <dir> --package <p>`: copies the rule classes and `archunit.properties` into `app-server`, skipping files that exist. Used by migrate and conformance check. It touches no pom.
- The pom entries the rules need are in `${CLAUDE_SKILL_DIR}/templates/fragments/`: `parent-properties.xml` (versions), `parent-plugins.xml` (jandex, so CDI beans in module JARs are discovered), the first entry of `parent-dependency-management.xml` and the ArchUnit entry of `app-server-test-dependencies.xml`.

Neither script commits.
