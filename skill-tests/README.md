# Skill tests

Unit-test-like checks for the skills in `../skills/`, run on demand only (no CI). A test builds a throwaway project, applies a skill, then lets the project's own tests judge the result. Terms (Skill test, Fixture project) are defined in `../GLOSSARY.md`.

## Running

```sh
bin/run                                        # every fresh test (no LLM, free)
bin/run --all-apply                            # every fresh and every apply test (costs tokens)
bin/run <skill> [fresh|apply] [<case>]         # one skill / kind / case
bin/run <skill> apply <case> --manual          # prepare the project, print steps for a human run
bin/run <skill> verify <project-dir> [<case>]  # judge a human's run exactly like an automated one
```

Options: `--model <alias>` overrides the model, `--keep` keeps the run dir even on a pass.

Each case prints one line, `PASS|FAIL|SKIP <skill> <kind>/<case> total_cost_usd=… num_turns=…`, and the exit code is non-zero if any case failed. Every run happens in a fresh `mktemp -d` outside the repo. It is deleted on a pass and kept on a failure or with `--keep`, and holds `log.txt`, the `claude` transcript (`claude.jsonl`) and the final reply (`result.txt`).

## Two kinds of test

- **fresh** (no LLM): the `setup` chain runs the skills' bundled scripts on a new project, e.g. Bare project → triptych → the skill under test. Then the project is verified.
- **apply** (LLM): `setup` builds a Fixture project, `claude -p` runs `prompt.md` against it, then it is verified.

## Layout

```
skill-tests/
  bin/run
  versions.env                  # QUARKUS_VERSION override for a whole run
  <skill>/
    fresh/<case>/  setup  [fixture/] [config] [verify/] [verify-fails/]
    apply/<case>/  setup  [fixture/] [config] [verify/] [verify-fails/]  prompt.md  [expect-result]
  selftest/                     # the harness's own tests (see below)
```

| File | Meaning |
|---|---|
| `setup` | One step per line: `<skill>[/<script>] [args…]`. Runs `skills/<skill>/scripts/<script>` (default `run.sh`) as `<script> <project-dir> [args…]`. `#` starts a comment. With no `setup`, the project starts empty. |
| `fixture/` | Copied over the project after `setup`, e.g. a REST resource that makes a Fixture project "existing". The project is then committed. |
| `config` | Shell variables: `MODEL` (default `sonnet`), `MAX_BUDGET_USD` (`0.50`), `MAX_TURNS` (`30`), `REQUIRES` (space-separated commands; `docker` must also be running). If a requirement is missing, the case is skipped. |
| `verify/` | Copied into the project. Then `./mvnw verify` (or `mvn`) runs if there is a `pom.xml`, each `e2e*/` folder with a `package.json` runs `npm test` (after `npx playwright install chromium`), and `verify/check.sh` runs in the project dir if present. Everything must pass. |
| `verify-fails/<name>/` | A planted violation. After verify passes, each one is copied in on its own, and verification must then **fail**. An optional `expect` file holds regexes the failing log must match, so the failure has the right cause. |
| `prompt.md` | The headless prompt. A human pastes the same prompt in `--manual` mode. |
| `expect-result` | Regexes (one per line, case-insensitive) that Claude's final reply must match, for cases judged on what the model reports (e.g. a Technique Conformance Check). |

## Script contract for skills

A skill that has a deterministic part ships it as `skills/<skill>/scripts/run.sh <project-dir> [args…]` (other script names via `<skill>/<script>`). The Bare project script creates `<project-dir>`; every other script changes the project in it. A script must need nothing beyond bash, a JDK and the tools the project itself uses.

## How apply runs `claude`

- `claude -p --plugin-dir <repo> --model … --max-budget-usd … --max-turns … --permission-mode acceptEdits --output-format stream-json`.
- With `ANTHROPIC_API_KEY` set, it adds `--bare`. Otherwise (OAuth) it adds `--setting-sources project --strict-mcp-config`, so your installed `seswdevskills@skills-dir` copy is not loaded alongside the repo's.
- The allow-list is `Read Edit Write Glob Grep Skill Bash(mvn:*) Bash(./mvnw:*) Bash(npm:*) Bash(npx:*) Bash(git:*) Bash(ls:*)` plus the plugin's own scripts (`Bash(<repo>/skills/*)`). Permissions are never bypassed.
- The case fails on an error, a budget or turn cap, or **any permission denial**: a missing tool is a signal about the skill.
- The LLM never writes the tests that judge it: `verify/` is copied in after the run.

## Self-test

`selftest/run [--all-apply]` runs `bin/run` against a test-only plugin (`selftest/plugin`, skill `harness-probe`). Its cases cover the setup chain, the fixture overlay, `check.sh`, `verify-fails` with `expect`, a `REQUIRES` skip, and one Haiku apply run with `expect-result` (about $0.003).
