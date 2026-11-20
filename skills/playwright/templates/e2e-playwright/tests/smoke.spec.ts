import {expect, test} from "@playwright/test";

// The first test: the app is up and answers @SMOKE_PATH@. Next to it go one spec per page or flow.

test("@SMOKE_PATH@ answers", async ({page}) => {
	const response = await page.goto("@SMOKE_PATH@");
	expect(response?.ok()).toBe(true);
});
