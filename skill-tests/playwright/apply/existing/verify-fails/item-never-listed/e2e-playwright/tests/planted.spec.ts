import {expect, test} from "@playwright/test";

// Planted: expects an item nobody added. npm test must go red.
test("planted: an item nobody added is listed", async ({request}) => {
	const response = await request.get("/items");
	expect(await response.text()).toContain("never-added");
});
