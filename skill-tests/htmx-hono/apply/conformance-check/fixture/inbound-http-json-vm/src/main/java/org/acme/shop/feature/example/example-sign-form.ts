import {html} from "hono/html";
import {ExampleSignFormVM} from "../../cross/htmlrender/generated/types/vm-types";
import {HtmlResult} from "../../cross/htmlrender/route-types";
import {eventName, nameUrl} from "../../cross/htmlrender/routes";

type Errors = Partial<Record<keyof ExampleSignFormVM, string>>;

/**
 * The ExampleSignForm route. Submitting POSTs to ExampleActionResource.sign:
 * - 204 + HX-Trigger ExampleGuestSigned: nothing is swapped; the wrapper re-fetches an empty form on the event.
 * - 400 + {route: ExampleSignForm, vm}: the wrapper is replaced by this template rendered with the errors.
 */
export const ExampleSignForm = (vm: ExampleSignFormVM): HtmlResult => {
	const errors: Errors = vm.errors;
	return html`
		<div
			hx-get="${nameUrl("ExampleSignForm")}"
			hx-trigger="${eventName("ExampleGuestSigned")} from:body"
			hx-swap="outerHTML"
		>
			<form hx-post="/example/guests" hx-target="closest div" hx-swap="outerHTML">
				<label>Name <input type="text" name="name" value="${vm.name}"></label>
				<button type="submit">Sign</button>
				${errors.name ? html`<p class="error">${errors.name}</p>` : ""}
			</form>
		</div>
	`;
};
