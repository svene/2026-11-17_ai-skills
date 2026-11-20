import {defineConfig} from "@playwright/test";

// Runs the packaged app (mvn verify built it) on its own port; the browser drives htmx + the hono extension.
export default defineConfig({
	testDir: "tests",
	workers: 1,
	use: {baseURL: "http://localhost:8089"},
	webServer: {
		command: "java -Dquarkus.http.port=8089 -jar ../app-server/target/quarkus-app/quarkus-run.jar",
		url: "http://localhost:8089/",
		reuseExistingServer: false,
		timeout: 60_000,
	},
});
