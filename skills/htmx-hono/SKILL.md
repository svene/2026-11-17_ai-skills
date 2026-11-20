---
name: htmx-hono
description: Add an htmx 4 + Hono-templates-in-the-browser frontend to a Triptych Quarkus project, where Quarkus answers {route, vm} JSON envelopes and Java view models, route names and mutation URLs are generated into TypeScript so a mismatch breaks mvn verify. Use when the Setup guide hands off to it, or the user asks for an htmx or Hono UI, the inbound-http-json-vm module, a new page or component route, or an htmx + Hono conformance check.
---

# htmx + Hono in the browser

Gives a Triptych project an HTML UI without server-side rendering. Quarkus answers JSON **envelopes** `{route, vm}`. A small htmx 4 extension in the browser runs the Hono `html` template named by `route` on `vm` and hands the HTML to htmx's normal swap. Java is the source of truth: typescript-generator turns the view models, the route and event names, and the mutation URLs into TypeScript, and `tsc --noEmit` runs inside `./mvnw verify`. A renamed field or a misspelled route is a red build, not a blank page.

Arguments: `$ARGUMENTS`

## The technique

Everything lives in one module, **`inbound-http-json-vm`** (JSON view models for our own UI; `inbound-http-jsonapi` stays for machine clients). The templates in `${CLAUDE_SKILL_DIR}/templates/` are the pattern: copy and rename them, never rewrite them from memory.

**Reads: component routes.** One endpoint, `GET /uiroute/{name}` (`cross.htmlrender.UiRouteReceiver`), answers `UiResponse(route, vm)` for every value of the `UiRouteName` enum. Each feature's `*UiReceiver` implements `UiRouteSource` and maps its routes to view-model builders. A route served twice or not at all fails the boot.

**Writes: action resources.** Mutations live in a feature's `*ActionResource`: a JAX-RS resource with a literal `@Path`, whose methods return `Response` (blocking; `Uni<T>` and `void` are unsupported).
- Success: **204** plus `HX-Trigger: <UiEventName>`. The affected components listen with `hx-trigger="${eventName("…")} from:body"` and re-fetch their route.
- Invalid input: **400** with the form's own envelope, `{route: <FormRoute>, vm: {…submitted values…, errors: {field: message}}}`. One template renders both states, and the form re-renders into the same target. A form VM always carries `errors` (empty on GET); in TS it is refined to `Partial<Record<keyof XxxVM, string>>`. The guestbook checks inline; a real feature's form uses `parse()` from the validation skill.
- Navigation: `HX-Redirect`. Header names are in `HtmxHeaders`.

**View models** are records named `*VM`, next to their Receiver in `feature.<x>`.

**The browser side** (`cross.htmlrender`):

| File | Role |
|---|---|
| `hx-hono.ts` | the esbuild entry and the `hono` extension: sets `Accept: application/json, text/html;q=0.9`, renders envelopes in `htmx_after_request`, never swaps a 5xx (`config.noSwap`) and shows `#error-banner` instead |
| `routes.ts` | `routes = {…} satisfies Record<UiRouteName, RouteDefinition>`, plus `nameUrl`, `nameIdUrl`, `eventName` |
| `render.ts` | the only place that calls `String(...)` on a template result |
| `route-types.ts` | `HtmlResult`, `RouteDefinition` |
| `api.ts` | `basePath` (change it for a non-default `quarkus.http.root-path`) and one URL-only client per action resource: `hx-post="${exampleApi.sign()}"` |
| `generated/types/` | `vm-types.d.ts` and `web-api.ts`, written by typescript-generator in `process-classes`; git-ignored |

Each component's template (`example-page.ts`) sits in its `feature.<x>` package, next to its VM. Templates use `hono/html`, no JSX.

**Styling: Bulma 1, vendored** like htmx at a versioned path, `META-INF/resources/css/bulma/1.0.4/bulma.min.css`, and linked from `index.html`. No npm package, no Sass, no CDN. Templates use Bulma's classes (`title`, `box`, `columns`, `field`/`control`/`label`/`input`, `button is-primary`, `help is-danger` for a form error); `#error-banner` is a `notification is-danger`. Bulma 1 follows the OS dark mode; `data-theme="light"` on `<html>` opts out. To upgrade, vendor the new `bulma.min.css` under its own version directory and change the `<link>`.

**Gotchas** (kept as comments in `hx-hono.ts` and `index.html`):
- htmx 4 extensions are global: the hooks run for every request, whatever `hx-ext` says.
- Hooks are synchronous: every template must be in the bundle. Lazy chunks would need a `ctx.fetch` override.
- `index.html` loads the vendored htmx (`js/htmx/<version>/htmx.js`), then the bundle. Neither is `defer`red, because `registerExtension` must run before htmx processes the page.
- The `hx-*` verb is a plain attribute: nothing ties it to the Java method's annotation at compile time. The conformance check and an e2e test catch a mismatch.

**Build.** `frontend-maven-plugin` installs a pinned Node into the module's `node/`, runs `npm ci` (`initialize`), then after codegen `tsc --noEmit` and the esbuild bundle (`process-classes`). The bundle lands in `target/classes/META-INF/resources/js/hono/`, so it ships inside the module jar.

**Dev loop.** `./mvnw quarkus:dev -pl app-server -am`, and in `inbound-http-json-vm`: `npm run watch`. `cross.devreload` (`JsBundleWatcher`, `DevReloadSSE`, dev profile only) reloads the page when the bundle changes.

## Inputs

| Input | Default |
|---|---|
| project directory | the current directory |
| root package | the package of `app-server`'s `AppServerBootTest` |

When the arguments contain a `from: setup-guide; …` line, take `dir=` and `package=` from it, and don't ask for anything it gives. `fresh: true` there means the fresh branch. That caller has already confirmed, so skip every **Confirm** step below, and leave all commits to it.

**Never commit.** Outside the Setup guide, the changes are left for the user to review.

## Steps

1. **Check the precondition.** The project must be a Triptych project: `core`, at least one `inbound-*` module and `app-server` with ArchUnit. Otherwise, stop and tell the user to apply the triptych skill first. Nothing is changed. (`run.sh` refuses such a project too.)
2. **Detect the mode.** Done when the project matches exactly one row:

   | Shape | Mode |
   |---|---|
   | only triptych's `feature.example` slice, no `inbound-http-json-vm` | **fresh**: step 3 |
   | other feature slices, no `inbound-http-json-vm` | **existing project**: follow [references/existing-project.md](references/existing-project.md) |
   | `inbound-http-json-vm` present, the user wants a page or component | **add a page**: follow [references/add-page.md](references/add-page.md) |
   | `inbound-http-json-vm` present, otherwise | **conformance check**: follow [references/conformance-check.md](references/conformance-check.md) |

3. **Confirm** (fresh). Show one line and wait for a yes: `htmx + Hono in <dir>: module inbound-http-json-vm, guestbook example at / (ExamplePage, POST /example/guests → 204/400), ExampleUiTest, package <p>`.
4. **Apply** (fresh). Run
   ```sh
   ${CLAUDE_SKILL_DIR}/scripts/run.sh <dir> [--package <p>]
   ```
   then `./mvnw verify` in `<dir>`. The first build downloads Node and the npm packages. Done when both commands exit 0. On a failure, show the error and stop.
5. **Report** what was added:
   - the module `inbound-http-json-vm`, wired into the parent and `app-server`
   - Bulma 1.0.4, vendored under `META-INF/resources/css/bulma/1.0.4/`
   - the guestbook at `/`: a list and a sign form, `POST /example/guests` answering 204 + `HX-Trigger` or the 400 form envelope
   - `ExampleUiTest` in `app-server`, `UiRouteReceiverUnitTest` in the module (the route table's startup checks), the `.gitignore` entries, the README link
   - the dev loop (step 4's two commands)

   Mention that `feature.example` is the pattern to copy for a real page ([references/add-page.md](references/add-page.md)), and that browser tests belong to the Playwright skill.

## Scripts

- `scripts/run.sh <dir> [--package <p>] [--existing]`: creates the module and adds the infrastructure and the guestbook example. Without `--existing`, it refuses anything but a fresh Triptych project; with it, any Triptych project without the module. It refuses a project that already has `inbound-http-json-vm` or the example's classes. It never commits.
