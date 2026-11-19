---
name: bare-project
description: Create a bare multi-module Quarkus project (parent + app-server, no Techniques). Use when the Setup guide hands off to it, or the user asks for a bare project by name.
---

# Bare project

Creates a new **multi-module** Quarkus project: a parent pom plus one `app-server` module with a boot test, a Maven wrapper, `.gitignore`, `.sdkmanrc`, a README and one initial git commit. It applies no Technique and takes no extensions: technologies arrive later as Technology modules through the triptych skill.

Arguments: `$ARGUMENTS`

## Inputs

| Input | Default |
|---|---|
| group ID | required |
| artifact ID | required; also the directory name |
| package | `<groupId>.<artifactId with non-alphanumerics removed, lower-cased>` |
| directory | `./<artifactId>` |
| Java | 25 (21 is also supported) |

Quarkus is pinned in the script; a `QUARKUS_VERSION` environment variable overrides it.

When the arguments contain a `from: setup-guide; …` line, take `groupId=`, `artifactId=`, `package=` and `dir=` from it.

## Steps

1. **Collect inputs.** Ask only for what the arguments leave missing (group ID and artifact ID), showing the derived package and directory so the user can override them. Done when every input has a value.
2. **Check the directory.** If it exists and is not empty, stop and tell the user; nothing is written.
3. **Confirm.** Show one line (`<groupId>:<artifactId>, package <package>, in <dir>, Java <n>`) and wait for a yes. Skip this when called `from: setup-guide`: the guide has already confirmed.
4. **Create.** Run
   ```sh
   ${CLAUDE_SKILL_DIR}/scripts/run.sh <dir> --group-id <g> --artifact-id <a> --package <p> --java <n>
   ```
   then `./mvnw verify` in `<dir>`. Done when both exit 0. On a failure, show the error and stop.
5. **Report** the absolute project path, the build commands from its README, and that it is a fresh project, ready for the triptych skill.
