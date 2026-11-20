import {expect, test} from "@playwright/test";

// The guestbook page of the htmx-hono example: envelopes rendered in the browser, the 400 form flow, the 204 +
// HX-Trigger re-fetch, and the 5xx banner.

test("the page is rendered from the ExamplePage envelope", async ({page}) => {
	await page.goto("/");
	await expect(page.getByRole("heading", {name: "Guestbook"})).toBeVisible();
	await expect(page.getByRole("button", {name: "Sign"})).toBeVisible();
	await expect(page.locator("#error-banner")).toBeHidden();
});

test("a blank name re-renders the form with its error", async ({page}) => {
	await page.goto("/");
	await page.getByRole("button", {name: "Sign"}).click();
	await expect(page.getByText("must not be blank")).toBeVisible();
	await expect(page.locator("#error-banner")).toBeHidden();
});

test("signing re-fetches the list and an empty form", async ({page}) => {
	const name = `Ada ${Date.now()}`; // the server's guestbook outlives a test (--repeat-each)
	await page.goto("/");
	await page.getByLabel("Name").fill(name);
	await page.getByRole("button", {name: "Sign"}).click();
	await expect(page.getByRole("listitem").filter({hasText: name})).toBeVisible();
	await expect(page.getByLabel("Name")).toHaveValue("");
});

test("a 5xx shows the banner and swaps nothing", async ({page}) => {
	await page.goto("/");
	await expect(page.getByRole("button", {name: "Sign"})).toBeVisible();
	await page.route("**/uiroute/ExampleGuestList", (route) =>
		route.fulfill({status: 500, contentType: "text/html", body: "<h1>Internal Server Error</h1>"}));
	await page.getByLabel("Name").fill("Bob");
	await page.getByRole("button", {name: "Sign"}).click();
	await expect(page.locator("#error-banner")).toBeVisible();
	await expect(page.getByRole("heading", {name: "Internal Server Error"})).toHaveCount(0);
	await expect(page.getByRole("heading", {name: "Guestbook"})).toBeVisible();
});
