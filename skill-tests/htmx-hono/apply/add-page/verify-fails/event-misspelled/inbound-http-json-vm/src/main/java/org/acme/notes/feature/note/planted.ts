import {eventName} from "../../cross/htmlrender/routes";

// Planted: an event name the Java UiEventName enum does not have. tsc --noEmit in mvn verify must reject it.
export const planted = eventName("NoteAddedd");
