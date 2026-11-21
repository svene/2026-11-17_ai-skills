# Provenance

- **The dev-mode setup**: `webServer` building before `quarkus:dev`, `reuseExistingServer: !CI`, the `-D` flags against background jobs, `retries: 0`, `actionTimeout: 3_000`, and the live-reload warm-up in `global-setup.ts`. From [`e2e-playwright/`](https://github.com/svene/2026-07-26_quarkus-hexagon-demo-mm-per-tech/tree/b2f849396eb60a3aae38918c7fab3033c93bb1d9/e2e-playwright) in svene/2026-07-26_quarkus-hexagon-demo-mm-per-tech at `b2f8493`.
- **The jar mode** (package, then start a fresh app per run with freshly seeded data) and the htmx spec patterns (a `goto` helper that waits for the load-triggered render, `/uiroute/{name}` routes). From [`playwright/`](https://github.com/svene/2026-09-03_hypermedia-quarkus-browser-hono/tree/5b8da7f0d12eb125fd07c5138453c7b896d09a5a/playwright) and [`development.md`](https://github.com/svene/2026-09-03_hypermedia-quarkus-browser-hono/blob/5b8da7f0d12eb125fd07c5138453c7b896d09a5a/development.md#end-to-end-tests) in svene/2026-09-03_hypermedia-quarkus-browser-hono at `5b8da7f`. The README link points at that `development.md` section.

The changes made when copying:

- **Two modes in one config:** `E2E_SERVER` chooses jar or dev. `server.ts` holds the ports and the readiness URL for both the config and `global-setup.ts`.
- **Jar port 8089** instead of 8080, so a running dev server is left alone; the module is always `app-server`.
- **`./mvnw -B`** instead of `mvn`. The hono source's separate `npm run build` is dropped, since htmx-hono's bundle builds inside `mvn package`.
- **No retries anywhere** (the hono source retried twice on CI), screenshot and trace kept on failure, Chromium only, no HTML reporter.
- **Exact versions** for `@playwright/test` and `@types/node` (both sources use `^` ranges).
- **Specs:** `guestbook.spec.ts` for htmx-hono's guestbook (Playwright requires htmx-hono), with paired `waitForResponse`, anchored route matches and the 5xx path through `page.route`. Neither source has them. The sources' own specs are not copied.

Decided in [Decide the Playwright skill](https://github.com/svene/2026-11-17_ai-skills/issues/10), as amended there.
