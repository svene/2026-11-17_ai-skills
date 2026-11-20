import {html} from "hono/html";
import {ExampleGuestListVM} from "../../cross/htmlrender/generated/types/vm-types";
import {HtmlResult} from "../../cross/htmlrender/route-types";
import {eventName, nameUrl} from "../../cross/htmlrender/routes";

/** The ExampleGuestList route. Re-fetches itself whenever a guest has signed (HX-Trigger of the sign action). */
export const ExampleGuestList = (vm: ExampleGuestListVM): HtmlResult => html`
	<div
		class="box content"
		hx-get="${nameUrl("ExampleGuestList")}"
		hx-trigger="${eventName("ExampleGuestSigned")} from:body"
		hx-swap="outerHTML"
	>
		${vm.names.length
			? html`<ul>${vm.names.map((name) => html`<li>${name}</li>`)}</ul>`
			: html`<p class="has-text-grey">No guests yet.</p>`}
	</div>
`;
