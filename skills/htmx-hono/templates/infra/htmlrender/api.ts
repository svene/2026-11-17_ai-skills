import {ExampleActionResourceClient, HttpClient} from "./generated/types/web-api";

/**
 * The base path of every URL the browser requests. A non-default `quarkus.http.root-path` means changing it here.
 */
export const basePath = "/";

/**
 * The mutation URLs, from the clients typescript-generator writes into generated/types/web-api.ts, one per
 * `*ActionResource`. The "client" sends nothing: `urlOnly` turns each call into its URL (generated URLs have no
 * leading slash, hence `basePath`), so a template writes `hx-post="${exampleApi.sign()}"` and a renamed Java method,
 * or a changed path parameter, breaks `tsc --noEmit`. Form fields are not in the signatures: htmx submits them.
 */
const urlOnly: HttpClient = {
	request: ({url, queryParams}) => {
		const query = new URLSearchParams();
		for (const [key, value] of Object.entries(queryParams ?? {})) {
			for (const v of Array.isArray(value) ? value : [value]) {
				if (v !== undefined && v !== null) query.append(key, String(v));
			}
		}
		const search = query.toString();
		return basePath + url + (search ? "?" + search : "");
	},
};

// One instance per *ActionResource.
export const exampleApi = new ExampleActionResourceClient(urlOnly);
