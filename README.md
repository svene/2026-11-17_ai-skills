# seswdevskills

My own Claude Code skills for software development. The repo is one Claude Code plugin, `seswdevskills`, published through a one-entry local marketplace, `svenemarket`. The skills target Quarkus multi-module projects. The **Setup guide** starts a new project: it runs the **Bare project** skill, then applies the **Technique** skills (triptych architecture, validation, htmx + Hono, Playwright). Terms are defined in [GLOSSARY.md](GLOSSARY.md).

## Layout

- `.claude-plugin/plugin.json`: the plugin manifest. It has no `version`; the commit SHA is the version.
- `.claude-plugin/marketplace.json`: the `svenemarket` marketplace, with one entry (`source: "./"`).
- `skills/<name>/SKILL.md`: one self-contained folder per skill. Bundled scripts and templates are referenced via `${CLAUDE_SKILL_DIR}`.
- `skill-tests/`: the skill tests (see below).

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
