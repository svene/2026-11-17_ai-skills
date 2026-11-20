import {RouteDefinition} from "./route-types";
import {UiEventName, UiRouteName} from "./generated/types/vm-types";
import {basePath} from "./api";
import {ExamplePage} from "../../feature/example/example-page";
import {ExampleGuestList} from "../../feature/example/example-guest-list";
import {ExampleSignForm} from "../../feature/example/example-sign-form";

/** The URL of a component route without parameters. */
export const nameUrl = (name: UiRouteName): string =>
	`${basePath}uiroute/${name}`;

/** The URL of a component route for one entity. */
export const nameIdUrl = (name: UiRouteName, id: number | string): string =>
	`${basePath}uiroute/${name}?id=${encodeURIComponent(id)}`;

/** An event name for `hx-trigger`, checked against the Java UiEventName enum. */
export const eventName = (name: UiEventName): string => name;

/**
 * Every component route: the URL a template uses in `hx-get`, and the template that renders the route's view model.
 *
 * `satisfies Record<UiRouteName, RouteDefinition>` checks the keys against the Java UiRouteName enum (generated into
 * vm-types.d.ts), so a missing or misspelled route breaks `tsc --noEmit`.
 */
export const routes = {
	ExamplePage: {url: () => nameUrl("ExamplePage"), render: ExamplePage},
	ExampleGuestLst: {url: () => nameUrl("ExampleGuestList"), render: ExampleGuestList},
	ExampleSignForm: {url: () => nameUrl("ExampleSignForm"), render: ExampleSignForm},
} satisfies Record<UiRouteName, RouteDefinition>;
