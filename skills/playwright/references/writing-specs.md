# Writing specs

One spec per page or flow in `e2e-playwright/tests/`, named after it (`orders.spec.ts`). `tests/guestbook.spec.ts` (with htmx-hono) and `tests/example.spec.ts` are the worked examples.

## Add a spec

1. **Pick the page or flow** with the user, and read its feature slice: the Receiver behind each URL, the data it shows, what a write changes.
2. **Write the spec** following the rules below. Seed data the app does not create on start comes through the app's own API or UI in the test itself.
3. **Run it** in `e2e-playwright/`: `npm test -- tests/<name>.spec.ts`, then the whole suite with `npm test`. Done when both are green, and the spec goes red when the behaviour it checks is broken (break it once by hand, then restore it).

## Rules

- **Locators by role:** `getByRole`, `getByLabel`, `getByText`, as the user perceives the page. A CSS or id selector is for elements without a role, like `#error-banner`.
- **Web-first assertions:** `await expect(locator).toBeVisible()`, `toHaveText`, `toHaveCount` retry until they pass. An effect that arrives later than the next render (a Kafka consumer, a scheduled job, an outbound call) gets `expect.poll(() => …, {timeout: 15_000})`, and the test timeout stays above that wait.
- **Data that survives a rerun:** the jar mode starts with fresh seed data, but tests within one run share it, and a reused dev server keeps it across runs. A test that creates data names it uniquely (`` `Ada ${Date.now()}` ``) and asserts only on its own rows. Seed data that tests rely on is deterministic (a fixed seed, fixed rows).
- **Background jobs off:** anything that changes data on its own (schedulers, simulators, auto-replenishment) is switched off with `-D` flags in `appFlags` in `playwright.config.ts`, each with a comment saying why.
- **No retries, no sleeps:** `retries` stays 0 and `waitForTimeout` stays out. A flaky test waits for the wrong thing; find the event it should wait for.

## htmx

An htmx page answers an action with requests the test cannot see in the DOM until they land:

- **Pair the wait with the action:** `await Promise.all([page.waitForResponse(match), button.click()])`. A `waitForResponse` started after the click can miss a fast response.
- **Anchored route matches:** `new RegExp(`/uiroute/${name}(\\?|$)`)`, so `ExampleGuestList` does not also match `ExampleGuestListItem`.
- **Wait for the load-triggered fragment:** `index.html` is a shell; `goto("/")` returns before the page arrives. A `goto<Page>` helper waits for a heading or element of the page itself.
- **Assert absence after the request:** "nothing was swapped" is meaningful only once the response has arrived. Wait for it (with `r.status() === 500` for a failure path), then assert `toHaveCount(0)`.
- **Failure paths with `page.route`:** `route.fulfill({status: 500, …})` on the component route tests the 5xx banner without breaking the app.
