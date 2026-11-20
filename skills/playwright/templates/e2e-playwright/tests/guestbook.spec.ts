import {expect, Page, test} from "@playwright/test";

// The guestbook of the htmx-hono example: envelopes rendered in the browser, the 400 form flow, the 204 + HX-Trigger
// re-fetch, and the 5xx banner. Delete this spec with feature.example.

// Component routes live under /uiroute/{name}; anchored, so ExampleGuestList would not also match ExampleGuestListItem.
const uiroute = (name: string) => new RegExp(`/uiroute/${name}(\\?|$)`);

// index.html is an empty shell: the page arrives with the load-triggered GET /uiroute/ExamplePage.
async function gotoGuestbook(page: Page) {
	await page.goto("/");
	await expect(page.getByRole("heading", {name: "Guestbook"})).toBeVisible();
}

test("the page is rendered from the ExamplePage envelope", async ({page}) => {
	await gotoGuestbook(page);
	await expect(page.getByRole("button", {name: "Sign"})).toBeVisible();
	await expect(page.locator("#error-banner")).toBeHidden();
});

test("a blank name re-renders the form with its error", async ({page}) => {
	await gotoGuestbook(page);
	await page.getByRole("button", {name: "Sign"}).click();
	await expect(page.getByText("must not be blank")).toBeVisible();
	await expect(page.locator("#error-banner")).toBeHidden();
});

test("signing re-fetches the list and an empty form", async ({page}) => {
	const name = `Ada ${Date.now()}`; // the guestbook outlives a test: unique per run
	await gotoGuestbook(page);
	await page.getByLabel("Name").fill(name);
	await Promise.all([
		page.waitForResponse((r) => uiroute("ExampleGuestList").test(r.url())),
		page.getByRole("button", {name: "Sign"}).click(),
	]);
	await expect(page.getByRole("listitem").filter({hasText: name})).toBeVisible();
	await expect(page.getByLabel("Name")).toHaveValue("");
});

test("a 5xx shows the banner and swaps nothing", async ({page}) => {
	await gotoGuestbook(page);
	await page.route(uiroute("ExampleGuestList"), (route) =>
		route.fulfill({status: 500, contentType: "text/html", body: "<h1>Internal Server Error</h1>"}));
	await page.getByLabel("Name").fill("Bob");
	// Wait for the failed re-fetch itself: asserting that nothing was swapped is only meaningful after it.
	await Promise.all([
		page.waitForResponse((r) => uiroute("ExampleGuestList").test(r.url()) && r.status() === 500),
		page.getByRole("button", {name: "Sign"}).click(),
	]);
	await expect(page.locator("#error-banner")).toBeVisible();
	await expect(page.getByRole("heading", {name: "Internal Server Error"})).toHaveCount(0);
	await expect(page.getByRole("heading", {name: "Guestbook"})).toBeVisible();
});
