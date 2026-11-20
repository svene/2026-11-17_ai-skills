---
name: playwright
description: Add Playwright browser tests to a Triptych Quarkus project: e2e-playwright/, a TypeScript @playwright/test project that builds and starts the app (packaged jar or quarkus:dev) and drives Chromium against it. Use when the Setup guide hands off to it, or the user asks for Playwright, end-to-end or browser tests, a new e2e spec, or a Playwright conformance check.
---

# Playwright end-to-end tests

Gives a Triptych project browser tests that run against the real app. Unit and `@QuarkusTest` tests stay where they are; the Playwright suite owns everything only a browser can see: what htmx swaps, which request fires, what the user reads.

Arguments: `$ARGUMENTS`

## The technique

**`e2e-playwright/`** is its own npm project at the project root, next to the Maven modules, run with `npm test`. Maven does not run it: `./mvnw verify` stays fast and needs no browser. (`frontend-maven-plugin` could run it in `integration-test`; set that up only when the user asks.) The templates in `${CLAUDE_SKILL_DIR}/templates/e2e-playwright/` are the pattern: copy them, never rewrite them from memory.

| File | Role |
|---|---|
| `package.json` | `@playwright/test` pinned exactly, `test`, `test:headed`, `install:browsers` (Chromium only) |
| `server.ts` | `E2E_SERVER` → `mode`, `baseURL`, `readyURL`: one place for ports and the readiness path |
| `playwright.config.ts` | `webServer` per mode, `retries: 0`, `actionTimeout: 3_000`, screenshot and trace on failure; `appFlags` for `-D` flags |
| `global-setup.ts` | dev mode only: absorbs a reused dev server's live reload before the first test |
| `tests/*.spec.ts` | one spec per page or flow |

**The server**, chosen by `E2E_SERVER`:
- **`jar`** (default): `./mvnw package -DskipTests`, then `java -jar app-server/target/quarkus-app/quarkus-run.jar` on port 8089, never reused. Every run gets a fresh app and fresh seed data, and a dev server on 8080 is left alone.
- **`dev`**: `./mvnw install -DskipTests`, then `quarkus:dev -pl app-server` on the dev port, reused outside CI. Use it when the app needs Dev Services (a database or broker with no prod connection): the packaged jar runs the prod profile and cannot start without one. A project like that gets `dev` as its default in `server.ts`.

**Specs** follow [references/writing-specs.md](references/writing-specs.md): role-based locators, web-first assertions, data that survives a rerun, and the htmx patterns.

## Inputs

| Input | Default |
|---|---|
| project directory | the current directory |

When the arguments contain a `from: setup-guide; …` line, take `dir=` from it, and don't ask for anything it gives. `fresh: true` there means the fresh branch. That caller has already confirmed, so skip every **Confirm** step below, and leave all commits to it.

**Never commit.** Outside the Setup guide, the changes are left for the user to review.

## Steps

1. **Check the precondition.** The project must be a Triptych project: `core`, at least one `inbound-*` module and `app-server`. Otherwise, stop and tell the user to apply the triptych skill first. Nothing is changed. (`run.sh` refuses such a project too.) The suite needs a system Node with `npm`; without one, stop and say so.
2. **Detect the mode.** Done when the project matches exactly one row:

   | Shape | Mode |
   |---|---|
   | only triptych's `feature.example` slice, no Playwright setup | **fresh**: step 3 |
   | other feature slices, no Playwright setup | **existing project**: follow [references/existing-project.md](references/existing-project.md) |
   | a Playwright setup outside `e2e-playwright/` (another folder, a JS config) | **existing project**, stage "align": [references/existing-project.md](references/existing-project.md) |
   | `e2e-playwright/` present, the user wants a spec for a page or flow | **add a spec**: follow [references/writing-specs.md](references/writing-specs.md) |
   | `e2e-playwright/` present, otherwise | **conformance check**: follow [references/conformance-check.md](references/conformance-check.md) |

3. **Confirm** (fresh). Show one line and wait for a yes: `Playwright in <dir>: e2e-playwright/ (Chromium, @playwright/test 1.65.1), packaged jar on 8089 (E2E_SERVER=dev for quarkus:dev), example spec[ + guestbook spec]`. The guestbook spec is part of it when `index.html` bootstraps `/uiroute/ExamplePage` (the htmx-hono example).
4. **Apply** (fresh). Run
   ```sh
   ${CLAUDE_SKILL_DIR}/scripts/run.sh <dir>
   ```
   then, in `<dir>/e2e-playwright`: `npm ci`, `npm run install:browsers`, `npm test`. The first `npm test` builds the whole project before the browser starts. Done when all four exit 0. On a failure, show the error and stop.
5. **Report** what was added:
   - `e2e-playwright/` and its files (the table above), and the README link
   - the specs: `example.spec.ts` (`GET /api/example`) and, with htmx-hono, `guestbook.spec.ts`
   - how to run it: `npm test`, `npm run test:headed`, `E2E_SERVER=dev npm test`

   Mention that the specs go with `feature.example` when it is deleted, and that a new page gets its spec through this skill (add a spec).

## Scripts

- `scripts/run.sh <dir> [--existing --path <p>] [--dev-port <n>]`: writes `e2e-playwright/` and the README link. Without `--existing`, it refuses anything but a fresh Triptych project and writes the example specs. With it, it writes `smoke.spec.ts` for `--path`, which is also the readiness URL. `--dev-port` defaults to `quarkus.http.port` (`%dev.` first) in `app-server`'s `application.properties`, else 8080. It refuses a project that already has a Playwright setup. It never commits.
