package @PACKAGE@.cross.devreload;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.vertx.web.Route;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dev only: the server-sent-event stream {@code /js/dev-reload.js} listens on. {@link #broadcastReload()} tells every
 * open page to reload. Absent outside the dev profile, where dev-reload.js gets a 404 and stops.
 */
@IfBuildProfile("dev")
@ApplicationScoped
public class DevReloadSSE {

	private final Set<HttpServerResponse> clients = ConcurrentHashMap.newKeySet();

	@Route(path = "/dev-reload", methods = Route.HttpMethod.GET)
	void stream(RoutingContext ctx) {
		HttpServerResponse response = ctx.response();
		response.putHeader("Content-Type", "text/event-stream");
		response.putHeader("Cache-Control", "no-cache");
		response.setChunked(true);
		clients.add(response);
		response.closeHandler(v -> clients.remove(response));
		response.write("event: connected\ndata: ok\n\n");
	}

	public void broadcastReload() {
		clients.forEach(response -> {
			try {
				response.write("event: reload\ndata: now\n\n");
			} catch (RuntimeException e) {
				clients.remove(response);
			}
		});
	}
}
