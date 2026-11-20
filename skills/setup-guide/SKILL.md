---
name: setup-guide
description: Start a new Quarkus multi-module project: asks every question up front, then chains the Bare project skill, the triptych architecture and the chosen technologies and Techniques (Kafka, validation, htmx + Hono, Playwright), committing after each step. Use when the user wants to start, create or set up a new project.
---

# Setup guide

Starts a **new** project in one confirmed run: Bare project → triptych → the chosen Techniques, each applied by its own skill, each step ending on a green `./mvnw verify` and its own commit. Declining a Technique here never prevents adding it later with its skill.

Arguments: `$ARGUMENTS`

## Techniques

The fixed, ordered chain. Order matters: each step adapts to what earlier steps added (validation's example covers Kafka when `inbound-kafka` exists; Playwright writes the spec for the htmx + Hono example page). `skill-tests/bin/check-technique-list` fails when a skill in `skills/` is missing from this table, so a new Technique skill is added here.

| Step | Asked as | Skill | Extra `args` | Commit message |
|---|---|---|---|---|
| Bare project | always | `bare-project` | | (its own initial commit) |
| Triptych | always | `triptych` | | `Apply triptych architecture` |
| Kafka | "Kafka consumers (inbound-kafka)?" | `triptych` | `module=inbound-kafka`, plus `adapter=none` when validation is chosen too (its example consumer replaces triptych's) | `Add Kafka (inbound-kafka)` |
| Validation | "Input validation (parse, don't validate)?" | `validation` | | `Apply validation` |
| htmx + Hono | "HTML UI with htmx + Hono?" | `htmx-hono` | | `Add htmx + Hono frontend` |
| Playwright | "Playwright browser tests?" | `playwright` | | `Add Playwright browser tests` |

## Steps

1. **Target directory.** Take it from the arguments; else the current directory if it is empty (no entries at all, hidden ones included); else ask for one. Done when the directory is known and is missing or empty. A non-empty directory ends the run here, before any other question: the guide is for new projects only, and an existing project takes the Technique skills one by one (`/seswdevskills:triptych`, `validation`, `htmx-hono`, `playwright`).
2. **Ask everything.** Collect every answer before writing anything, taking what the arguments already give:
   - **artifact ID**: default the directory name
   - **group ID**: no default
   - **root package**: default `<groupId>.<artifactId without non-alphanumerics, lower-cased>`
   - **yes/no, no defaults**: each "Asked as" question of the table, in order

   Done when every input has an answer.
3. **Confirm once.** Show one screen and wait for a yes:
   ```
   New project <groupId>:<artifactId>, package <package>, in <absolute dir>
     1. Bare project            commit: Bare project <artifactId> (…)
     2. Triptych                commit: Apply triptych architecture
     3. Kafka                   commit: Add Kafka (inbound-kafka)
     …                          (only the chosen steps)
   Each step ends on a green ./mvnw verify and its own commit.
   ```
4. **Run the chain.** For each chosen step, in table order, invoke its skill through the Skill tool (`seswdevskills:<skill>`) with this `args` line, plus the step's extra `args` (`; key=value` each):
   ```
   from: setup-guide; fresh: true; groupId=<g>; artifactId=<a>; package=<p>; dir=<absolute dir>
   ```
   The skill skips its own confirmations and asks for nothing the line gives. A step is done when the skill has finished with `./mvnw verify` green in the directory (run it yourself if the skill did not; Playwright's step also needs its `npm test` green). Then commit everything with the step's message (`git add -A`, `git commit -m "<message>"`); the Bare project step commits by itself. Done when every chosen step has its commit.

   **Gate:** when a step fails, stop the chain there. Leave the failed step's changes uncommitted (`git reset --hard` undoes them) and go straight to the report.
5. **Report**: the absolute project path, `git log --oneline`, and the build commands from the README (plus `cd e2e-playwright && npm test` with Playwright, and that Kafka's tests need Docker). After a gate stop, report instead what is done (the commits), the failing step with its error, and the steps left, each as the skill to run by hand (`/seswdevskills:<skill>`) in table order.
