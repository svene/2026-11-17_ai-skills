# Migrate a multi-module project to Triptych

For a multi-module Quarkus project that is not yet Triptych. Migration runs in **stages**. Each stage is confirmed before it starts (unless called `from: setup-guide`) and ends on a green `./mvnw verify`, so the user can commit between stages. Never commit yourself.

## 1. Survey

Map the existing code: which modules exist, which classes are entry points (REST resources, message consumers, schedulers), which are use cases, which talk to the outside (DB, HTTP clients, brokers), and which features they belong to. Pick the root package.

Done when every main class has a planned target: its module, its `feature.<x>` or `cross.<y>` package, and its role suffix. Show the plan as a table, grouped by stage.

## 2. Stage 1: the frame

1. Add the `core` module (copy `${CLAUDE_SKILL_DIR}/templates/core/pom.xml`, filling `@GROUP_ID@`, `@ARTIFACT_ID@` and `@VERSION@` from the parent). If the project has no `app-server`-style assembly module, decide with the user which existing module becomes it.
2. Add the pom entries from `${CLAUDE_SKILL_DIR}/templates/fragments/`: the versions, the jandex plugin, ArchUnit in dependency management and in `app-server`'s test dependencies, and `core` in the parent's modules and dependency management and in `app-server`'s dependencies.
3. Run `${CLAUDE_SKILL_DIR}/scripts/add-rules.sh <dir> --package <root>`.
4. Append `${CLAUDE_SKILL_DIR}/templates/fragments/readme-architecture.md` to `README.md`.
5. Run `./mvnw verify`. For each failing `ArchitectureTest` method, add JUnit's `@Disabled("TODO triptych migration: <rule> until stage <n>")` to it. Disable only the failing ones. (ArchUnit's `@ArchIgnore` does nothing here, because these are plain JUnit `@Test` methods.)

Done when `./mvnw verify` is green and every `@Disabled` names the stage that removes it.

## 3. Stage 2..n: one feature per stage

For each feature from the survey:

1. Move its use-case logic into `core` as `<root>.feature.<x>.<X>Handler`. Turn each call to the outside into an `<Name>SPI` interface in the same package.
2. Move its entry points into the matching `inbound-<tech>` module as `*Receiver`s that call the Handler. Create the module first if needed ([technology-module.md](technology-module.md)).
3. Move its outside calls into the matching `outbound-<tech>` module as `*Service`s implementing the SPIs.
4. Move code that two features share into `cross.<y>`.
5. Keep behaviour identical: existing tests move along with their classes and stay green. Move files with `git mv` so their history follows them.

Done when `./mvnw verify` is green. After the last stage, remove every `@Disabled("TODO triptych migration…")`, and `./mvnw verify` stays green.

## 4. Report

List the stages done, the modules created, and anything left `@Disabled` (with the reason, if the user stopped early).
