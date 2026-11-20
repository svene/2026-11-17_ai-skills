import {expect, test} from "@playwright/test";

// The example slice of triptych: GET /api/example?name=… answers a greeting. Delete this spec with feature.example.

test("the example endpoint greets by name", async ({page}) => {
	const response = await page.goto("/api/example?name=Ada");
	expect(response?.ok()).toBe(true);
	await expect(page.locator("body")).toContainText("Hello, Ada");
});
