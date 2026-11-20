import {render} from "./render";

declare const htmx: any;

/**
 * The `hono` htmx 4 extension, the esbuild entry: renders the Hono `html` templates in the browser.
 *
 * `/uiroute/*` endpoints (and an action resource's 400) answer a JSON envelope `{route, vm}` instead of an HTML
 * fragment. `htmx_after_request` (htmx 4's place to rewrite the body before the swap) runs the template for `route`
 * on `vm` and replaces `ctx.text` with the HTML, so the normal htmx swap proceeds unchanged.
 *
 * Gotchas:
 * - htmx 4 extensions are global: these hooks run for every request, whatever `hx-ext` says. Hence the checks below.
 * - Hooks are synchronous (htmx never awaits them): every template must be in this bundle. Lazy-loaded chunks would
 *   need a `ctx.fetch` override instead.
 * - This bundle must load right after htmx.js, and neither may be `defer`red: `registerExtension` runs at parse
 *   time and must happen before htmx processes the page.
 */
htmx.registerExtension("hono", {
	init: () => {
		// A 5xx body is an error page, not a fragment: never swap it, show the banner instead.
		htmx.config.noSwap = [...htmx.config.noSwap, "5xx"];
	},

	// htmx 4 asks for text/html; the envelopes are JSON. HTML stays acceptable for error pages and redirects.
	htmx_config_request: (_elt: Element, detail: any) => {
		detail.ctx.request.headers["Accept"] = "application/json, text/html;q=0.9";
	},

	htmx_after_request: (elt: Element, detail: any) => {
		const ctx = detail.ctx;
		const status: number = ctx.response?.status ?? 0;
		if (status >= 500) {
			failed(elt, `Server error (${status}). Please try again.`);
			return;
		}
		succeeded(elt);
		if (!ctx.text) return;
		const contentType: string = ctx.response?.headers?.get?.("content-type") ?? "";
		if (!contentType.includes("application/json")) return;
		const envelope = JSON.parse(ctx.text);
		if (typeof envelope?.route !== "string") return; // JSON, but not an envelope: leave it alone
		ctx.text = render(envelope.route, envelope.vm);
	},

	// The server is unreachable, or a hook threw (e.g. a template on an unexpected view model).
	htmx_error: (elt: Element) => {
		failed(elt, "Something went wrong. Please try again.");
	},
});

/*
 * `#error-banner` (index.html) is shown while the last request of some element still in the page failed. Tracked per
 * element, not per request: one event can start several requests (e.g. HX-Trigger re-fetches), in any order, and one
 * succeeding must not hide another's failure. An element swapped out (e.g. re-rendered after a retry) no longer counts.
 */
const failedElements = new Set<Element>();

function failed(elt: Element, message: string): void {
	failedElements.add(elt);
	const banner = document.getElementById("error-banner");
	if (!banner) return;
	banner.textContent = message;
	banner.hidden = false;
}

function succeeded(elt: Element): void {
	failedElements.delete(elt);
	for (const e of failedElements) if (!e.isConnected) failedElements.delete(e);
	const banner = document.getElementById("error-banner");
	if (banner && failedElements.size === 0) banner.hidden = true;
}
