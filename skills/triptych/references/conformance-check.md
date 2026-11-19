# Triptych conformance check

For a project that already uses the Triptych architecture. Find every deviation, fix only what the user approves, and never commit.

## 1. Make the rules present

If `app-server` lacks any of the three devsupport classes or `archunit.properties`, run `${CLAUDE_SKILL_DIR}/scripts/add-rules.sh <dir> --package <root>`. Add the missing pom entries from `${CLAUDE_SKILL_DIR}/templates/fragments/` (ArchUnit and its version, plus the jandex plugin). Existing rule files stay as they are: the project owns them. Compare them with the templates and report any rule the project has weakened or removed.

Done when `ArchitectureTest` exists and runs: surefire reports both test methods.

## 2. Collect findings

Run `./mvnw verify`, then walk the checklist. Every item is either a finding or confirmed clean.

| Check | Severity |
|---|---|
| `ArchitectureTest` failures, one finding per violation | major |
| `@Disabled` on an architecture test | major |
| a Handler injecting or calling another Handler | major |
| a module that does not fit `core` / `inbound-<tech>` / `outbound-<tech>` / `app-server`, or one technology spread over two modules | major |
| `app-server` with main sources | major |
| an `*API` or `*Port` interface, or a class whose suffix contradicts its role or module | minor |
| an adapter module that is missing jandex, so its beans are not discovered | major |
| the README has no Architecture section, or its two links are missing or not commit permalinks | minor |

The current README links are in `${CLAUDE_SKILL_DIR}/templates/fragments/readme-architecture.md`.

Done when every checklist row is marked either finding or clean.

## 3. Report and fix

Show the findings grouped by severity, each with a file reference and the fix. Ask which to fix. Apply the approved fixes, then run `./mvnw verify` again. Done when the approved fixes are applied and the build is green.
