package @PACKAGE@.cross.htmlrender;

/**
 * The JSON envelope every {@code /uiroute/*} endpoint returns, and every 400 of an action resource:
 * {@code {"route":"ExamplePage","vm":{…}}}.
 * <p>
 * The route name travels with the data, so the browser-side {@code hono} htmx extension (hx-hono.ts) knows which
 * template to run on {@code vm} without an extra header or per-element attribute. No HTML is rendered on the server.
 */
public record UiResponse(UiRouteName route, Object vm) {
}
