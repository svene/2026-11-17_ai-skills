package @PACKAGE@.feature.example;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path.Node;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Inbound adapter of the example slice: translates HTTP requests into calls on {@link ExampleHandler}.
 * Untrusted input becomes an {@link Example} only through {@link Example#parse}; a violation is a 400 with
 * {@code [{"field": ..., "message": ...}]}, never an exception. No {@code @Valid} here: the domain value owns
 * its constraints.
 */
@Path("/api/example")
public class ExampleReceiver {

    private static final Logger LOG = Logger.getLogger(ExampleReceiver.class);

    @Inject
    ExampleHandler exampleHandler;

    public record GreetingJson(String message) {
    }

    /** The wire shape: everything is text, so a non-number count is reported as a violation, not a 400 from Jackson. */
    public record ExampleJson(String name, String count) {
    }

    public record AcceptedJson(String message) {
    }

    public record ViolationJson(String field, String message) {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public GreetingJson greet(@QueryParam("name") String name) {
        return new GreetingJson(exampleHandler.greet(name));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response accept(ExampleJson request) {
        LOG.infof("EXAMPLE_RECEIVED %s", request);
        if (request == null) {
            return badRequest(List.of(new ViolationJson("body", "must not be empty")));
        }
        return switch (Example.parse(request.name(), request.count())) {
            case Example example -> Response.status(Response.Status.CREATED)
                .entity(new AcceptedJson(exampleHandler.accept(example)))
                .build();
            case ParsedExample.Invalid invalid -> badRequest(toJson(invalid.violations()));
        };
    }

    private static Response badRequest(List<ViolationJson> violations) {
        LOG.warnf("EXAMPLE_INVALID %s", violations);
        return Response.status(Response.Status.BAD_REQUEST).entity(violations).build();
    }

    private static List<ViolationJson> toJson(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
            .map(v -> new ViolationJson(field(v), v.getMessage()))
            .sorted(Comparator.comparing(ViolationJson::field).thenComparing(ViolationJson::message))
            .toList();
    }

    /** The last node of the property path, e.g. "count" for "Example.count": the wire field of the same name. */
    private static String field(ConstraintViolation<?> violation) {
        String field = "";
        for (Node node : violation.getPropertyPath()) {
            field = node.getName();
        }
        return field;
    }
}
