import {routes} from "../../cross/htmlrender/routes";

// Planted: a route name that does not exist. tsc --noEmit in mvn verify must reject it.
export const planted: keyof typeof routes = "ItemPag";
