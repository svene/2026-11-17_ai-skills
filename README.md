# seswdevskills

My own Claude Code skills for software development. The repo is one Claude Code plugin, `seswdevskills`, published through a one-entry local marketplace, `svenemarket`. The skills target Quarkus multi-module projects. The **Setup guide** starts a new project: it runs the **Bare project** skill, then applies the **Technique** skills (triptych architecture, htmx + Hono, validation, Playwright). Terms are defined in [GLOSSARY.md](GLOSSARY.md).

## Using the skills

Invoke a skill as `/seswdevskills:<skill>`, or simply ask for what you want ("start a new project", "add Playwright tests"), and Claude picks the matching skill. Each skill shows what it will do and waits for your yes before writing anything. When it has finished, `./mvnw verify` is green.

### Starting a new project

In an empty directory, run **`/seswdevskills:setup-guide`**. It asks every question at once: group ID, artifact ID, package, and yes or no for Kafka, htmx + Hono, validation and Playwright (Playwright only after a yes to htmx + Hono). It shows the plan once and, after your yes, runs the chain with one git commit per step:

```text
bare-project → triptych → Kafka module → htmx-hono → validation → playwright
               (always)    (each optional, in this order; playwright needs htmx-hono)
```

The order matters for the examples. Kafka and htmx + Hono work without validation, which adds complexity to the receivers, so you can choose them alone. Validation, when chosen, also converts the Kafka consumer and the htmx + Hono guestbook form to `parse()` when they exist. Playwright tests the htmx + Hono UI, so it requires htmx + Hono and writes the spec for its guestbook page. If a step fails, the guide stops, leaves that step uncommitted and names the skills still to run by hand. Anything you decline can be added later with its own skill.

You can also run the same chain by hand, one skill at a time, in the order above. Use `/seswdevskills:bare-project` first, then `/seswdevskills:triptych`, then the Technique skills. In that case you make the commits yourself.

### Adding to an existing project

Apply **triptych first**: every other Technique skill refuses a project that does not use the Triptych architecture yet. After that, add htmx-hono and validation in any order, and each only when you need it. Playwright requires htmx-hono. Each skill detects what the project already has and picks its mode:

| Skill | Fresh project (from bare-project / triptych) | Existing project | Already applied |
|---|---|---|---|
| `bare-project` | creates parent + `app-server` (no Techniques) | refuses a non-empty directory | n/a |
| `triptych` | adds `core`, `inbound-http-jsonapi`, an example feature and ArchUnit rules | migrates a multi-module project (single-module projects are not supported) | conformance check; on request also adds a Technology module (e.g. `inbound-kafka`) or a feature slice |
| `validation` | "parse, don't validate" helper, an example and an ArchUnit rule (plus a Kafka consumer if `inbound-kafka` exists, and the guestbook form converted to `parse()` if htmx-hono's module exists) | applies it to your features in stages | conformance check |
| `htmx-hono` | `inbound-http-json-vm` module with a guestbook example page | adds the module to your project in stages | conformance check; on request adds a page or component |
| `playwright` | `e2e-playwright/` with a guestbook spec (requires htmx-hono) | adds `e2e-playwright/` with a smoke spec (htmx-hono's module must exist), or aligns a Playwright setup you already have | conformance check; on request writes a spec for a page or flow |

A **conformance check** means running the skill on a project that already uses its Technique. It reports where the project deviates from the Technique.

Prerequisites:
- Java 25 (or 21).
- Docker, when the project uses Kafka, because the tests use a Dev Services broker.
- A system Node with `npm`, for Playwright only. htmx-hono's build downloads its own Node through Maven.

Each skill's `SKILL.md` describes the Technique in detail.

## Layout

- `.claude-plugin/plugin.json`: the plugin manifest. It has no `version`; the commit SHA is the version.
- `.claude-plugin/marketplace.json`: the `svenemarket` marketplace, with one entry (`source: "./"`).
- `skills/<name>/SKILL.md`: one self-contained folder per skill. Bundled scripts and templates are referenced via `${CLAUDE_SKILL_DIR}`.
- `skill-tests/`: the skill tests; [skill-tests/README.md](skill-tests/README.md) explains how to run them.

## Install (personal, one machine)

The repo is installed as a skills-dir plugin through two symlinks, so it loads in place. Edits, new skills and `git pull` take effect in the next session, or after `/reload-plugins`; no commit or reinstall is needed:

```sh
mkdir -p ~/src && ln -s "$PWD" ~/src/seswdevskills
ln -s ~/src/seswdevskills ~/.claude/skills/seswdevskills
```

`claude plugin list` then shows `seswdevskills@skills-dir`. Skills are invoked as `/seswdevskills:<skill>`; the bare name usually works too. If the clone moves, repoint `~/src/seswdevskills`.

Don't install it through the local `svenemarket` marketplace: `claude plugin install` copies the working tree into `~/.claude/plugins/cache/`, keyed by commit, so changes would need a commit plus `claude plugin update`.

## Testing a branch

`claude --plugin-dir <worktree>` loads a worktree's copy of the plugin in place of the installed one for that session.

## Other machines

As a fallback, add the GitHub repo as a marketplace: `claude plugin marketplace add svene/2026-11-17_ai-skills`. This uses a cached copy, so updates need `claude plugin update seswdevskills@svenemarket`.
