# Existing project

A Triptych project with feature slices of its own gets the technique in stages. Each stage ends on a green `./mvnw verify` and is reported before the next one starts. Converting server-rendered pages (Qute, `inbound-http-html`) is out of scope. Their pages stay as they are, and new pages use this technique.

## Stages

1. **Module and infrastructure.** Confirm (skip `from: setup-guide`): `htmx + Hono in <dir>: module inbound-http-json-vm, guestbook example at / (ExamplePage), ExampleUiTest, package <p>`. Then run
   ```sh
   ${CLAUDE_SKILL_DIR}/scripts/run.sh <dir> [--package <p>] --existing
   ```
   then `./mvnw verify`. The guestbook in `feature.example` is the working pattern; it can be deleted after the first real page (stage 2): its classes, its three routes and its event in the enums, its `routes.ts` entries, `exampleApi`, and `ExampleUiTest`. Point `index.html`'s start page at the real page when doing so.

   `run.sh` refuses when the project differs from what it expects. Each refusal has its own fix:
   - **`inbound-http-json-vm` exists already:** run the conformance check ([conformance-check.md](conformance-check.md)) instead.
   - **The example's class names are taken:** create the module by hand. Follow the triptych skill's "add a Technology module" procedure, with `${CLAUDE_SKILL_DIR}/templates/inbound-http-json-vm/pom.xml` as the module pom. Copy `templates/infra/` into `cross.htmlrender`, `cross.devreload` and `META-INF/resources` (htmx at `js/htmx/4.0.0/htmx.js`, Bulma at `css/bulma/1.0.4/bulma.min.css`). Add `templates/fragments/gitignore` and the README link. Replace every `@…@` placeholder. Then go straight to stage 2. `UiRouteName` and `UiEventName` need at least one value each, and `routes.ts` one entry per route, so the first page brings them.
   - **A non-default `quarkus.http.root-path`:** set `basePath` in `api.ts`, and the start page, script and stylesheet URLs in `index.html`.
   - **The project already has a CSS framework:** keep it. Drop the Bulma `<link>` and `css/bulma/`, and restyle the guestbook's templates, or delete them with the example.
2. **One page at a time.** Pick the page with the user, then follow [add-page.md](add-page.md). It reuses the feature's existing Handler. Writes that a JSON API Receiver already performs get their own `*ActionResource`; the API Receiver stays for its machine clients. Report the stage, then offer the next page.

Done when every page the user picked is green.
