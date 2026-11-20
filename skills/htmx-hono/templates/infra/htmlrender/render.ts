import {html} from "hono/html";
import {routes} from "./routes";
import {RouteDefinition} from "./route-types";

/**
 * Runs the template of `route` on `vm` and returns the HTML as a primitive string.
 *
 * Templates return `HtmlResult` (a boxed `HtmlEscapedString`, possibly a Promise); this is the one place it is
 * stringified. `String(...)` runs Hono's stringify phase and yields the string the extension hands to htmx.
 */
export function render(route: string, vm: unknown): string {
	const definition = (routes as Record<string, RouteDefinition>)[route];
	return String(definition
		? definition.render(vm)
		: html`<div class="route-not-found">Route '${route}' not found</div>`);
}
