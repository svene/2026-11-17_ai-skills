import {html} from "hono/html";
import {ExamplePageVM} from "../../cross/htmlrender/generated/types/vm-types";
import {HtmlResult} from "../../cross/htmlrender/route-types";
import {ExampleGuestList} from "./example-guest-list";
import {ExampleSignForm} from "./example-sign-form";

/** The ExamplePage route: the whole page, composed of the two components. */
export const ExamplePage = (vm: ExamplePageVM): HtmlResult => html`
	<section>
		<h1>Guestbook</h1>
		${ExampleSignForm(vm.form)}
		${ExampleGuestList(vm.guests)}
	</section>
`;
