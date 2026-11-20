# Playwright conformance check

For a project that already has `e2e-playwright/`. Find every deviation, fix only what the user approves, and never commit. The templates in `${CLAUDE_SKILL_DIR}/templates/e2e-playwright/` are the reference for every comparison below.

## 1. Run

In `e2e-playwright/`: `npm ci`, `npm run install:browsers`, `npm test`. A red test is a finding in itself; read its error and trace before walking the checklist. Done when the run result is recorded.

## 2. Collect findings

Walk the checklist over `e2e-playwright/`, the README and `.gitignore`. Every row is either a finding or confirmed clean; each finding names its file and line.

| Check | Severity |
|---|---|
| htmx-hono's example is present (`index.html` bootstraps `/uiroute/ExamplePage`) but `tests/guestbook.spec.ts` is missing: copy it from the templates | major |
| `retries` above 0, or a `waitForTimeout` in a spec | major |
| `webServer` missing, or the jar mode reuses an existing server | major |
| ports or the readiness URL written outside `server.ts` | minor |
| `@playwright/test` not pinned to an exact version | minor |
| `actionTimeout`, `screenshot: "only-on-failure"` or `trace: "retain-on-failure"` missing | minor |
| a `-D` flag in `appFlags` without a comment saying why | minor |
| a spec locating by CSS or XPath where a role, label or text would do | minor |
| an htmx action whose `waitForResponse` starts after the action instead of in `Promise.all` with it, or an unanchored `/uiroute/` match | minor |
| a spec asserting on data another test or a rerun can change, without a unique run ID | minor |
| `node_modules/`, `test-results/`, `playwright-report/` not git-ignored, or committed | minor |
| the README's `## Architecture` section lacks the link in `${CLAUDE_SKILL_DIR}/templates/fragments/readme-playwright.md`, or points at another commit | minor |

Done when every checklist row is marked either finding or clean.

## 3. Report and fix

Show the findings grouped by severity, each with a file reference and the fix. The fix is the template's version of that piece. Ask which to fix. Apply the approved fixes, then run `npm test` again. Done when the approved fixes are applied and the suite is green.
