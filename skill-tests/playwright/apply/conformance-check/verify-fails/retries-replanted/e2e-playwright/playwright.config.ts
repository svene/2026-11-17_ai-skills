import path from "node:path";
import {defineConfig, devices} from "@playwright/test";
import {JAR_PORT, baseURL, mode, readyURL} from "./server";

// -D flags for the app under test, e.g. to switch off scheduled jobs that would change data mid-test.
const appFlags = "";

const root = path.join(__dirname, "..");

const webServer =
	mode === "jar"
		? {
				command:
					`./mvnw -B -q package -DskipTests && ` +
					`java -Dquarkus.http.port=${JAR_PORT} ${appFlags} -jar app-server/target/quarkus-app/quarkus-run.jar`,
				reuseExistingServer: false,
			}
		: {
				command:
					`./mvnw -B -q install -DskipTests && ` +
					`./mvnw -B quarkus:dev -pl app-server -Ddebug=false -Dquarkus.console.enabled=false ` +
					`-Dquarkus.analytics.disabled=true ${appFlags}`,
				reuseExistingServer: !process.env.CI,
			};

export default defineConfig({
	testDir: "tests",
	// A reused dev server live-reloads on its first request; global-setup absorbs that before the tests start.
	globalSetup: mode === "dev" ? "./global-setup.ts" : undefined,
	forbidOnly: !!process.env.CI,
	// A flaky test is a bug in the test or the app: fix it rather than retry it.
	retries: process.env.CI ? 2 : 0,
	use: {
		baseURL,
		// An action that cannot happen within 3 s points at a wrong locator, not a slow app.
		actionTimeout: 3_000,
		screenshot: "only-on-failure",
		trace: "retain-on-failure",
	},
	projects: [{name: "chromium", use: {...devices["Desktop Chrome"]}}],
	webServer: {
		...webServer,
		url: readyURL,
		cwd: root,
		timeout: 240_000,
		stderr: "pipe",
	},
});
