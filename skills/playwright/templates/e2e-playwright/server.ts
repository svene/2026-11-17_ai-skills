// The server the suite runs against, chosen by E2E_SERVER:
// - jar (default): build, then start the packaged app fresh on its own port, so every run sees freshly seeded data
//   and a dev server on the usual port is left alone.
// - dev: start quarkus:dev, or reuse a running one outside CI. Needed when the app relies on Dev Services.
const modes = ["jar", "dev"] as const;
export type Mode = (typeof modes)[number];

const requested = process.env.E2E_SERVER ?? "jar";
if (!(modes as readonly string[]).includes(requested)) {
	throw new Error(`E2E_SERVER=${requested}: expected one of ${modes.join(", ")}`);
}
export const mode = requested as Mode;

export const JAR_PORT = 8089;
export const DEV_PORT = @DEV_PORT@; // quarkus.http.port of the dev profile

export const baseURL = `http://localhost:${mode === "jar" ? JAR_PORT : DEV_PORT}`;
// Answers 2xx once the app is up: the webServer and the dev warm-up wait for it.
export const readyURL = `${baseURL}@READY_PATH@`;
