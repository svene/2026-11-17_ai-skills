package @PACKAGE@.cross.htmlrender;

/**
 * Every event an action resource sends in {@code HX-Trigger}, and templates listen for in {@code hx-trigger}.
 * typescript-generator turns it into the {@code UiEventName} union, so templates name events through
 * {@code eventName(...)} in routes.ts and a renamed event breaks {@code tsc --noEmit}.
 */
public enum UiEventName {
    ExampleGuestSigned,
}
