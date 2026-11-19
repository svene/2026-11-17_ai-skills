package @PACKAGE@.feature.example;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/** Inbound adapter of the example slice: translates an HTTP request into a call on {@link ExampleHandler}. */
@Path("/api/example")
public class ExampleReceiver {

    @Inject
    ExampleHandler exampleHandler;

    public record GreetingJson(String message) {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public GreetingJson greet(@QueryParam("name") String name) {
        return new GreetingJson(exampleHandler.greet(name));
    }
}
