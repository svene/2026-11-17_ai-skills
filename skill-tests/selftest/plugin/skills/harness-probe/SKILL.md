---
name: harness-probe
description: Test-only skill for the skill test harness self-test. Use when asked to set the greeting of a harness-probe project.
---

# harness-probe

A harness-probe project has a `greeting.txt` file.

To set its greeting, run this from the project directory:

```sh
${CLAUDE_SKILL_DIR}/scripts/run.sh . --name <name>
```

Then reply with one line: `greeting set to <name>`.
