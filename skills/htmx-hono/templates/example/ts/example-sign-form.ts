import {html} from "hono/html";
import {ExampleSignFormVM} from "../../cross/htmlrender/generated/types/vm-types";
import {HtmlResult} from "../../cross/htmlrender/route-types";
import {eventName, nameUrl} from "../../cross/htmlrender/routes";
import {exampleApi} from "../../cross/htmlrender/api";

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
			class="box"
			hx-get="${nameUrl("ExampleSignForm")}"
			hx-trigger="${eventName("ExampleGuestSigned")} from:body"
			hx-swap="outerHTML"
		>
			<form hx-post="${exampleApi.sign()}" hx-target="closest div.box" hx-swap="outerHTML">
				<div class="field">
					<label class="label" for="example-sign-name">Name</label>
					<div class="control">
						<input id="example-sign-name" class="input ${errors.name ? "is-danger" : ""}" type="text" name="name"
							value="${vm.name}">
					</div>
					${errors.name ? html`<p class="help is-danger">${errors.name}</p>` : ""}
				</div>
				<div class="field">
					<div class="control">
						<button type="submit" class="button is-primary">Sign</button>
					</div>
				</div>
			</form>
		</div>
	`;
};
