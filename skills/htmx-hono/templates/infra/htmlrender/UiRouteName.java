package @PACKAGE@.cross.htmlrender;

/**
 * Every component route, i.e. every {@code /uiroute/{name}} the browser can fetch and every template routes.ts maps.
 * <p>
 * The source of truth for both sides: typescript-generator turns it into the {@code UiRouteName} union, and routes.ts
 * declares {@code satisfies Record<UiRouteName, RouteDefinition>}, so a missing or misspelled template breaks
 * {@code tsc --noEmit}; {@link UiRouteReceiver} fails the boot when a value has no {@link UiRouteSource}.
 */
public enum UiRouteName {
    ExamplePage,
    ExampleGuestList,
    ExampleSignForm,
}
