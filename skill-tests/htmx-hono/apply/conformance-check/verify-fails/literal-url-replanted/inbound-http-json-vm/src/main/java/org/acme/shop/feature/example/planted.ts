import {html} from "hono/html";

// Planted: a literal mutation URL. It type-checks, so only check.sh catches it.
export const planted = html`<form hx-post="/example/guests"></form>`;
