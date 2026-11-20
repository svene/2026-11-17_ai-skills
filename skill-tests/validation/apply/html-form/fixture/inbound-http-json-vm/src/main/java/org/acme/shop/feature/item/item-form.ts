import {html} from "hono/html";
import {ItemFormVM} from "../../cross/htmlrender/generated/types/vm-types";
import {HtmlResult} from "../../cross/htmlrender/route-types";
import {eventName, nameUrl} from "../../cross/htmlrender/routes";
import {itemApi} from "../../cross/htmlrender/api";

type Errors = Partial<Record<keyof ItemFormVM, string>>;

/** The ItemForm route: 204 + ItemAdded re-fetches an empty form, a 400 re-renders it with the errors. */
export const ItemForm = (vm: ItemFormVM): HtmlResult => {
	const errors: Errors = vm.errors;
	return html`
		<div hx-get="${nameUrl("ItemForm")}" hx-trigger="${eventName("ItemAdded")} from:body" hx-swap="outerHTML">
			<form hx-post="${itemApi.add()}" hx-target="closest div" hx-swap="outerHTML">
				<label>Name <input type="text" name="name" value="${vm.name}"></label>
				${errors.name ? html`<p class="error">${errors.name}</p>` : ""}
				<label>Quantity <input type="text" name="quantity" value="${vm.quantity}"></label>
				${errors.quantity ? html`<p class="error">${errors.quantity}</p>` : ""}
				<button type="submit">Add</button>
			</form>
		</div>
	`;
};
