# Validation conformance check

For a project that already has `ConstructorValidation`. Find every deviation, fix only what the user approves,
and never commit.

## 1. Make the infrastructure present

Run `${CLAUDE_SKILL_DIR}/scripts/add-infrastructure.sh <dir> [--package <p>]`. It adds only the missing pieces:
`quarkus-hibernate-validator` in `core`, `ValidationArchitectureTest`, `archunit.properties` and the README link.
Existing files stay as they are, because the project owns them. Compare `ConstructorValidation` and
`ValidationArchitectureTest` with the templates, and report any rule the project has weakened or removed.

Done when `ValidationArchitectureTest` exists and runs.

## 2. Collect findings

Run `./mvnw verify`, then walk the checklist. Every item is either a finding or confirmed clean.

| Check | Severity |
|---|---|
| `ValidationArchitectureTest` failures: an inbound class calls a domain value's constructor. One finding per call. | major |
| `@Disabled` on `ValidationArchitectureTest` | major |
| `@Valid`, or constraint annotations, on a Receiver parameter or a DTO in an `inbound-*` module | major |
| a Receiver that passes request fields to a Handler without `parse()` | major |
| a `switch` over a `Parsed*` result with a `default` branch (it hides a new case) | minor |
| a domain value that implements `Parsed*` but has no `parse()`, or whose `parse()` throws | major |
| a domain value that validates in `parse()` with its own checks instead of `ConstructorValidation` | minor |
| a Receiver without `<OP>_RECEIVED`, or an invalid branch without `<OP>_INVALID` at WARN | minor |
| a Kafka consumer that retries invalid messages, has no dead-letter topic, or lacks `fail-on-deserialization-failure=false` | major |
| a Kafka consumer whose `@Retry` does not abort on `UnprocessableMessageException` | minor |
| an HTTP invalid branch with a status other than 400 | minor |
| `core` without its own `quarkus-hibernate-validator` dependency | minor |
| the README's `## Architecture` section lacks the validation.md link, or it is not the permalink in `${CLAUDE_SKILL_DIR}/templates/fragments/readme-validation.md` | minor |

The rule only sees direct constructor calls in inbound modules. For a reflection-based construction or a
mapper library, search for it too.

Done when every checklist row is marked either finding or clean.

## 3. Report and fix

Show the findings grouped by severity, each with a file reference and the fix (the stage steps in
[existing-project.md](existing-project.md) for a Receiver). Ask which to fix. Apply the approved fixes, then run
`./mvnw verify` again. Done when the approved fixes are applied and the build is green.
