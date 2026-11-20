# Add a page or component

A page is a component route like any other; `ExamplePage` is one that composes two more. Copy `feature.example` (in `inbound-http-json-vm`, plus its Handler in `core`) and rename. `<x>` is the feature and `<X>` its class prefix; one feature can own several routes.

## Steps

1. **Confirm.** Pick the feature with the user, then show one line and wait for a yes: `<X> in feature.<x>: routes <X>Page, <X>…; actions <METHOD> /<x>/… → 204 + HX-Trigger <X>…|HX-Redirect|400; test <X>UiTest`.
2. **Handler.** In `core`, the feature's Handler gets the reads and writes the page needs. A feature that does not exist yet starts with the triptych skill's feature-slice procedure.
3. **Names.** Add each route to `cross.htmlrender.UiRouteName` and each event to `UiEventName`. Prefix both with `<X>`.
4. **View models.** One `*VM` record per route in `inbound-http-json-vm`'s `feature.<x>`. A page VM composes its components' VMs. A form VM carries the entered values and `Map<String, String> errors`, plus an `empty()` factory.
5. **Reads.** A `<X>UiReceiver` (`@ApplicationScoped`, `implements UiRouteSource`) maps each new route to a function of the query parameters that builds its VM. For one entity, read `query.getFirst("id")`; the template's URL is `nameIdUrl(...)`.
6. **Writes.** A `<X>ActionResource` with a literal `@Path("/<x>")`. Each method is annotated with the HTTP verb its `hx-*` attribute will use, and returns `Response`:
   - success: `Response.noContent()` with `HtmxHeaders.HX_TRIGGER` set to a `UiEventName`, or `HX_REDIRECT` to navigate
   - invalid input: 400 with `new UiResponse(UiRouteName.<X>Form, <the form VM with the values and errors>)`
7. **Generate.** Run `./mvnw -q -pl inbound-http-json-vm -am process-classes`. It writes the VM types and the `<X>ActionResourceClient` into `cross/htmlrender/generated/types/`. Expect `tsc` errors here until step 9 is done.
8. **Templates.** One `<x>-<component>.ts` per route next to the VMs, each exporting `<RouteName> = (vm: <RouteName>VM): HtmlResult => html\`…\``. Templates follow these rules:
   - Component URLs come from `nameUrl` / `nameIdUrl`, event names from `eventName`, mutation URLs from the API client (`hx-post="${<x>Api.save()}"`). A string literal URL slips past the type check.
   - A component that shows changed data re-fetches itself: `hx-get="${nameUrl("<X>…")}" hx-trigger="${eventName("<X>…")} from:body" hx-swap="outerHTML"`.
   - A form's errors are typed `Partial<Record<keyof <X>FormVM, string>>`, and the 400 re-renders the form into the same target (`hx-target`, `hx-swap="outerHTML"`).
9. **Wire.**
   - `routes.ts`: one `<RouteName>: {url: () => nameUrl("<RouteName>"), render: <RouteName>}` entry per route.
   - `api.ts`: `export const <x>Api = new <X>ActionResourceClient(urlOnly);`
   - Navigation: a link in an existing template, `hx-get="${nameUrl("<X>Page")}" hx-target="#app" hx-push-url="true"`, or make the page the start page in `index.html`'s `<main id="app" hx-get=…>`.
10. **Test.** `<X>UiTest` in `app-server`, modelled on `ExampleUiTest`, asserts:
    - each route answers its envelope (`route` and the VM's fields)
    - each action answers 204 with its event (or the redirect)
    - each invalid input answers 400 with the form envelope and its `errors`
11. **Verify.** Run `./mvnw verify`. Done when it is green, including `ArchitectureTest`, `tsc` and `<X>UiTest`. Report the routes, the actions with their answers, the events and the test. Browser tests belong to the Playwright skill.
