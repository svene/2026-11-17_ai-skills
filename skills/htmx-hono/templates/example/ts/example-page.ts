import {html} from "hono/html";
import {ExamplePageVM} from "../../cross/htmlrender/generated/types/vm-types";
import {HtmlResult} from "../../cross/htmlrender/route-types";
import {ExampleGuestList} from "./example-guest-list";
import {ExampleSignForm} from "./example-sign-form";

/** The ExamplePage route: the whole page, composed of the two components. */
export const ExamplePage = (vm: ExamplePageVM): HtmlResult => html`
	<h1 class="title">Guestbook</h1>
	<div class="columns">
		<div class="column is-half">${ExampleSignForm(vm.form)}</div>
		<div class="column is-half">${ExampleGuestList(vm.guests)}</div>
	</div>
`;
