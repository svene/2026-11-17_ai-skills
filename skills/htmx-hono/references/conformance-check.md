# htmx + Hono conformance check

For a project that already has `inbound-http-json-vm`. Find every deviation, fix only what the user approves, and never commit. The templates in `${CLAUDE_SKILL_DIR}/templates/` are the reference for every comparison below.

## 1. Build

Run `./mvnw verify`. A red `tsc` or `ExampleUiTest`/`*UiTest` is a finding in itself; read its errors before walking the checklist. Done when the build result is recorded.

## 2. Collect findings

Walk the checklist over `inbound-http-json-vm`, `index.html`, the module pom and `.gitignore`. Every row is either a finding or confirmed clean. Several rows are searches; each finding names its file and line.

| Check | Severity |
|---|---|
| `routes` in `routes.ts` lacks `satisfies Record<UiRouteName, RouteDefinition>` | major |
| a hand-written VM type in TS: a `type`/`interface` for a view model outside `generated/types/` | major |
| a literal URL in a mutation attribute: an `hx-post`, `hx-put`, `hx-patch` or `hx-delete` value that is not a call on an API client from `api.ts` (`${<x>Api.…()}`) | major |
| an `hx-*` verb that differs from the HTTP annotation of the Java method its URL comes from (`hx-post` on a `@PUT` method). Match each mutation attribute to its `*ActionResource` method through the client call | major |
| a `*ActionResource` method returning anything but `Response` (`void`, `Uni<…>`, an entity) | major |
| an action that answers success with a body to swap instead of 204 + `HX-Trigger` (or `HX-Redirect`), or invalid input with anything but a 400 form envelope | minor |
| a literal URL in `hx-get`, or a literal event name in `hx-trigger`, instead of `nameUrl`/`nameIdUrl`/`eventName` | minor |
| `String(` on a template result outside `render.ts` | minor |
| `hx-hono.ts` no longer sets `Accept` in `htmx_config_request` | major |
| 5xx missing from `htmx.config.noSwap`, or no `#error-banner` in `index.html` | major |
| `index.html` loads the bundle before htmx, or either `<script>` is `defer`red, `async` or a module | major |
| a `cross.devreload` bean without `@IfBuildProfile("dev")` | major |
| `tsc --noEmit` (the `typecheck` execution) missing from the module pom, or bound to a phase after `verify` | major |
| `generated/`, `node/` or `node_modules/` not git-ignored, or committed | minor |
| typescript-generator driven by gmavenplus or another script instead of its two plugin executions: a deviation from the technique, recorded in the report | minor |
| the README's `## Architecture` section lacks the link in `${CLAUDE_SKILL_DIR}/templates/fragments/readme-htmx-hono.md`, or points at another commit | minor |

Done when every checklist row is marked either finding or clean.

## 3. Report and fix

Show the findings grouped by severity, each with a file reference and the fix. The fix is the template's version of that piece; for a mismatched verb, the Java annotation is the source of truth unless the user says otherwise. Ask which to fix. Apply the approved fixes, then run `./mvnw verify` again. Done when the approved fixes are applied and the build is green.
