import {html} from "hono/html";

/** The value an `html` tagged-template component produces. Turned into a string only in render.ts. */
export type HtmlResult = ReturnType<typeof html>;

/** One component route: the URL that fetches its `{route, vm}` envelope, and the template that renders `vm`. */
export type RouteDefinition = {
	url: (...args: any[]) => string;
	render: (vm: any) => HtmlResult;
};
