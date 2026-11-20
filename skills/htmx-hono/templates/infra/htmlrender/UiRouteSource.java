package @PACKAGE@.cross.htmlrender;

import jakarta.ws.rs.core.MultivaluedMap;

import java.util.Map;
import java.util.function.Function;

/**
 * Implemented by each feature's {@code *UiReceiver}: the routes it serves, each with the function that builds the
 * view model from the request's query parameters. {@link UiRouteReceiver} collects them into one table.
 */
public interface UiRouteSource {

    Map<UiRouteName, Function<MultivaluedMap<String, String>, Object>> routes();
}
