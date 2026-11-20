import {readyURL} from "./server";

// Dev mode only (E2E_SERVER=dev). A fresh dev server needs no warm-up: the webServer command builds first. A reused
// one recompiles changed sources, but only live-reloads on the next HTTP request; without this warm-up the first tests
// would send that request and fail with ERR_EMPTY_RESPONSE.
const WARM_UP_TIMEOUT_MS = 90_000;
// Quarkus scans for changes at most every 2 s, so OKs within a shorter window may all come before the scan that
// triggers the reload.
const OK_STREAK_MS = 2_500;
const POLL_INTERVAL_MS = 250;

export default async function globalSetup() {
	console.log(`Warming up ${readyURL} (absorbs a reused dev server's live reload)...`);
	const deadline = Date.now() + WARM_UP_TIMEOUT_MS;
	let streakStart: number | undefined;
	while (streakStart === undefined || Date.now() - streakStart < OK_STREAK_MS) {
		if (Date.now() > deadline) {
			throw new Error(`${readyURL} did not answer OK for ${OK_STREAK_MS} ms within ${WARM_UP_TIMEOUT_MS} ms`);
		}
		let ok = false;
		try {
			ok = (await fetch(readyURL)).ok;
		} catch {
			// The reload dropped the connection: the streak starts over.
		}
		streakStart = ok ? (streakStart ?? Date.now()) : undefined;
		await new Promise((resolve) => setTimeout(resolve, POLL_INTERVAL_MS));
	}
	console.log("Dev server is warm.\n");
}
