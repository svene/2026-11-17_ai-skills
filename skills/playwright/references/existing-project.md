# Existing project

A Triptych project with feature slices of its own gets `e2e-playwright/` with a smoke spec first, then a spec per page. Each stage ends on a green `npm test` and is reported before the next one starts.

## Stages

1. **Gather.** Done when each item has a value:
   - **The smoke path:** a GET that answers 2xx once the app is up, with no side effects (the start page, a list page, a JSON list). It is also the readiness URL.
   - **The dev port:** `run.sh` reads `quarkus.http.port` (`%dev.` first) from `app-server`'s `application.properties`. Pass `--dev-port` when the port is set elsewhere (another profile file, an environment variable).
   - **The server mode:** `dev` when the app needs Dev Services (a datasource, Kafka, MongoDB without a prod connection configured), `jar` otherwise.
   - **Background jobs:** schedulers or simulators that change data on their own, and the property that switches each off.
2. **Confirm** (skip `from: setup-guide`): `Playwright in <dir>: e2e-playwright/ (Chromium, @playwright/test 1.65.1), <jar on 8089 | quarkus:dev on <port>>, smoke spec GET <path>`.
3. **Apply.** Run
   ```sh
   ${CLAUDE_SKILL_DIR}/scripts/run.sh <dir> --existing --path <path> [--dev-port <n>]
   ```
   For `dev`, change the default in `server.ts` (`process.env.E2E_SERVER ?? "dev"`). Add the background-job flags to `appFlags` in `playwright.config.ts`, one comment each. Then, in `e2e-playwright/`: `npm ci`, `npm run install:browsers`, `npm test`. Done when all exit 0.
4. **One page at a time.** Pick the page with the user, then follow [writing-specs.md](writing-specs.md). Report the stage, then offer the next page.

Done when every page the user picked has a green spec.

## Align an existing Playwright setup

`run.sh` refuses a project with a `playwright.config.*` anywhere, so an existing suite is never overwritten. Align it by hand, in place, keeping its specs:

- Move it to `e2e-playwright/` only when the user agrees; the folder name is a convention, the rest is the technique.
- Bring the config to the template's: `server.ts`, both modes, `retries: 0`, `actionTimeout: 3_000`, screenshot and trace on failure, Chromium. Keep its own `-D` flags and timeouts, with their comments.
- Convert a JS config or specs to TypeScript only when the user asks.
- Add the README link from `${CLAUDE_SKILL_DIR}/templates/fragments/readme-playwright.md` to `## Architecture`.

Then run the conformance check ([conformance-check.md](conformance-check.md)). Done when it reports no major finding and `npm test` is green.
