---
name: validation
description: Apply "parse, don't validate" input validation to a Triptych Quarkus project, so untrusted input becomes a domain value only through parse() and invalid input is a 400 (HTTP) or a logged skip (Kafka). Use when the Setup guide hands off to it, or the user asks for input validation, "parse, don't validate", replacing @Valid, poison-pill or dead-letter handling for a Kafka consumer, or a validation conformance check.
---

# Validation: parse, don't validate

Teaches the **"parse, don't validate"** technique to a Triptych project and keeps it there. Untrusted input from an inbound adapter becomes a domain value only through `parse(...)`. That call returns either the value or the constraint violations. An ArchUnit rule in `app-server` turns a direct constructor call in an inbound module into a red `./mvnw verify`.

Arguments: `$ARGUMENTS`

## The technique

A **domain value** `Xxx` in `core` is a record whose components carry Bean Validation constraints. It implements a sealed `ParsedXxx`:

```java
public record Example(@NotBlank String name, @Positive int count) implements ParsedExample { … }

public sealed interface ParsedExample permits Example, ParsedExample.Invalid {
    record Invalid(Set<ConstraintViolation<Example>> violations) implements ParsedExample {}
}
```

- `parse(...)` validates the arguments against the canonical constructor's constraints and returns the value or `Invalid`. There is no `Valid` wrapper: the value itself is the success case.
- The compact constructor runs the same check and throws `IllegalArgumentException`. Use it for **trusted data only**: tests, data the application produced itself, in-process events.
- Both go through `cross.validation.ConstructorValidation` (`declaredConstructor`, `validateParameters`), on a constructor cached in a static field.
- Callers `switch` exhaustively over the result. A new case is a compile error, not a forgotten branch.
- **When the input is text** (a form field, a JSON string), add a private, never-invoked constructor with the text parameters and their own constraints (`@Pattern(…, message = "must be a number")`), plus a `parse(String, String)` that validates against it first. Then a non-number becomes a violation with a field name, not a 400 from Jackson. One limitation: when the text check fails, only the text violations are reported. A blank name and a count of `0` report only the name.

The **template** for all of this is `${CLAUDE_SKILL_DIR}/templates/core/Example.java`, with `ParsedExample.java` and `ExampleTest.java` next to it. Copy it and rename it. Don't rewrite it from memory.

**Why not …**

- **`@Valid` on the Receiver parameter:** it validates a DTO, not the domain value. The Handler can then still be called with an unchecked value, and the violation is an exception mapped somewhere else, not a branch the adapter has to handle.
- **Deserializing straight into the domain type:** it couples the wire format to the domain and turns every bad field into a deserialization error without a field name. The DTO stays the wire format.
- **Relying on the validator that comes in transitively** (e.g. through `quarkus-rest`): `core` must not depend on an adapter's choices. `core` declares `quarkus-hibernate-validator` itself.

**The rule.** `app-server/src/test/java/<package>/devsupport/ValidationArchitectureTest` holds `inbound_adapters_construct_domain_values_only_via_parse`. No class in an `inbound-*` module (found by its path) may call a constructor of a type that implements a `Parsed*` interface. The test is separate from triptych's `ArchitectureTest`, and the project owns it once copied. `archunit.properties` sets `archRule.failOnEmptyShould=true`.

**Logging.** Use plain log calls (no audit abstraction):

| Line | Level | Where |
|---|---|---|
| `<OP>_RECEIVED <input>` | INFO | the Receiver, before `parse()` |
| `<OP>_INVALID <violations>` | WARN | the Receiver's invalid branch (Kafka: add topic/partition/offset when the consumer has them) |
| `<OP>_PROCESSING <value>` | INFO | the Handler |

## Boundaries

Only the invalid branch differs between kinds of boundary. Read the reference for the boundary at hand:

| Boundary | Invalid input | Reference |
|---|---|---|
| HTTP JSON API (`inbound-http-jsonapi`) | 400 with `[{"field": …, "message": …}]` | [boundaries/http-jsonapi.md](references/boundaries/http-jsonapi.md) |
| HTML/htmx (`inbound-http-json-vm`) | 400 with `{route, vm: {…, errors: {field: message}}}`, re-rendered into the same target | [boundaries/http-json-vm.md](references/boundaries/http-json-vm.md) |
| Kafka (`inbound-kafka`) | ask per consumer: WARN and skip, or dead-letter | [boundaries/kafka.md](references/boundaries/kafka.md) |
| In-process events | none: trusted data, throwing constructor | [boundaries/in-process-events.md](references/boundaries/in-process-events.md) |
| anything else | | [boundaries/new-boundary.md](references/boundaries/new-boundary.md) |

400 (not 422) is the status for invalid input on every HTTP boundary.

## Inputs

| Input | Default |
|---|---|
| project directory | the current directory |
| root package | the package of `app-server`'s `AppServerBootTest` |

When the arguments contain a `from: setup-guide; …` line, take `dir=` and `package=` from it, and don't ask for anything it gives. `fresh: true` there means the fresh branch. That caller has already confirmed, so skip every **Confirm** step below, and leave all commits to it.

**Never commit.** Outside the Setup guide, the changes are left for the user to review.

## Steps

1. **Check the precondition.** The project must be a Triptych project: `core`, at least one `inbound-*` module and `app-server`. Otherwise, stop and tell the user to apply the triptych skill first. Nothing is changed. (All scripts refuse such a project too.)
2. **Detect the mode.** Done when the project matches exactly one row:

   | Shape | Mode |
   |---|---|
   | only the `feature.example` slice (triptych's, plus Kafka's consumer and htmx-hono's guestbook if present), no `ConstructorValidation` | **fresh**: step 3 |
   | other feature slices, no `ConstructorValidation` | **existing project**: follow [references/existing-project.md](references/existing-project.md) |
   | `ConstructorValidation` present | **conformance check**: follow [references/conformance-check.md](references/conformance-check.md) |

3. **Confirm** (fresh). Show one line and wait for a yes: `Validation in <dir>: Example + ParsedExample, POST /api/example → 201/400[, Kafka consumer on "example"][, guestbook form via ExampleGuest.parse], ValidationArchitectureTest, package <p>`. Include the Kafka part only if `inbound-kafka/` exists, and the guestbook part only if `inbound-http-json-vm/` exists (htmx-hono).
4. **Apply** (fresh). Run
   ```sh
   ${CLAUDE_SKILL_DIR}/scripts/run.sh <dir> [--package <p>]
   ```
   then `./mvnw verify` in `<dir>`. With `inbound-kafka`, the tests need Docker (Dev Services starts the broker). Done when both commands exit 0. On a failure, show the error and stop.
5. **Report** what was added:
   - `Example` and `ParsedExample` in `core`
   - `POST /api/example`: 201 `{"message": "3 x widget"}`, or 400 with the violations
   - the Kafka consumer if there is one: invalid → WARN and skip; null or undeserializable → `example-dlq`
   - the guestbook if there is one: `ExampleGuest` + `ParsedExampleGuest` in `core`, and `POST /action/example/sign` parses the name instead of checking it inline (still 400 with the form's `errors`)
   - the tests and the rule
   - the README link

   Mention that `feature.example` is the pattern to copy for a real feature.

## Scripts

- `scripts/run.sh <dir> [--package <p>]`: the fresh mode only. It refuses any other shape. It runs `add-infrastructure.sh`, then rewrites `feature.example` from `${CLAUDE_SKILL_DIR}/templates/`, including Kafka's consumer and htmx-hono's guestbook when their modules exist.
- `scripts/add-infrastructure.sh <dir> [--package <p>]`: `ConstructorValidation`, `quarkus-hibernate-validator` in `core`, `ValidationArchitectureTest` and `archunit.properties`, and the README link. It skips every piece that is already there and touches no endpoint. It is used by all three modes.
- `templates/fragments/kafka-application.properties`: the channel configuration of a consumer with a dead-letter queue (`@PACKAGE@` is the root package).

Neither script commits.
