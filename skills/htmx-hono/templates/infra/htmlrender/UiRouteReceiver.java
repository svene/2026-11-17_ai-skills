package @PACKAGE@.cross.htmlrender;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * The one GET endpoint for components: {@code /uiroute/{name}} answers {@code {route, vm}} for every
 * {@link UiRouteName}, the view model built by the feature's {@link UiRouteSource}. Mutations live in the features'
 * {@code *ActionResource} classes, a separate concept.
 * <p>
 * The table is built at startup; a route served twice or not at all fails the boot (and AppServerBootTest).
 */
@Path("/uiroute")
public class UiRouteReceiver {

    @Inject
    Instance<UiRouteSource> sources;

    private final Map<UiRouteName, Function<MultivaluedMap<String, String>, Object>> table = new EnumMap<>(UiRouteName.class);

    void buildTable(@Observes StartupEvent event) {
        buildTable(sources);
    }

    /** Separate from the startup observer, so a unit test can hand in stub sources. */
    void buildTable(Iterable<? extends UiRouteSource> sources) {
        table.clear();
        for (UiRouteSource source : sources) {
            source.routes().forEach((route, vm) -> {
                if (table.putIfAbsent(route, vm) != null) {
                    throw new IllegalStateException("UiRouteName." + route + " is served by more than one UiRouteSource");
                }
            });
        }
        Set<UiRouteName> missing = EnumSet.allOf(UiRouteName.class);
        missing.removeAll(table.keySet());
        if (!missing.isEmpty()) {
            throw new IllegalStateException("UiRouteName " + missing + " not served by any UiRouteSource");
        }
    }

    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public UiResponse uiroute(@PathParam("name") String name, @Context UriInfo uriInfo) {
        return respond(name, uriInfo.getQueryParameters());
    }

    UiResponse respond(String name, MultivaluedMap<String, String> query) {
        UiRouteName route;
        try {
            route = UiRouteName.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Unknown uiroute: " + name);
        }
        return new UiResponse(route, table.get(route).apply(query));
    }
}
