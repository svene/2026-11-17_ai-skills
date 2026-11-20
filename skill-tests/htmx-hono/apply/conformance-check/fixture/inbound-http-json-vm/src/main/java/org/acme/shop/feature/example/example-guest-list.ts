import {html} from "hono/html";
import {ExampleGuestListVM} from "../../cross/htmlrender/generated/types/vm-types";
import {HtmlResult} from "../../cross/htmlrender/route-types";
import {eventName, nameUrl} from "../../cross/htmlrender/routes";
import {exampleApi} from "../../cross/htmlrender/api";

/** The ExampleGuestList route. Re-fetches itself whenever a guest has signed (HX-Trigger of the sign action). */
export const ExampleGuestList = (vm: ExampleGuestListVM): HtmlResult => html`
	<ul
		hx-get="${nameUrl("ExampleGuestList")}"
		hx-trigger="${eventName("ExampleGuestSigned")} from:body"
		hx-swap="outerHTML"
	>
		${vm.names.map((name) => html`<li>${name}</li>`)}
		<li><button hx-put="${exampleApi.clear()}">Clear</button></li>
	</ul>
`;
