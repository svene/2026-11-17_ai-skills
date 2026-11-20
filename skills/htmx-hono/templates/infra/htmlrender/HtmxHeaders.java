package @PACKAGE@.cross.htmlrender;

/** The htmx response headers an action resource sets. */
public final class HtmxHeaders {

    /** Fires the named event (a {@link UiEventName}) on the requesting element; it bubbles to {@code body}. */
    public static final String HX_TRIGGER = "HX-Trigger";

    /** Makes the browser navigate to the given URL. */
    public static final String HX_REDIRECT = "HX-Redirect";

    private HtmxHeaders() {
    }
}
