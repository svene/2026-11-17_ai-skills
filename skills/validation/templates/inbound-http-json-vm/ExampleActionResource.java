package @PACKAGE@.feature.example;

import @PACKAGE@.cross.htmlrender.HtmxHeaders;
import @PACKAGE@.cross.htmlrender.UiEventName;
import @PACKAGE@.cross.htmlrender.UiResponse;
import @PACKAGE@.cross.htmlrender.UiRouteName;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path.Node;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The example slice's mutations. The form's text becomes an {@link ExampleGuest} only through
 * {@link ExampleGuest#parse}. A success is 204 plus an HX-Trigger event the affected components re-fetch on;
 * invalid input is a 400 whose {route, vm} envelope re-renders the form with the submitted value and its errors.
 */
@Path("/example")
public class ExampleActionResource {

	private static final Logger LOG = Logger.getLogger(ExampleActionResource.class);

	@Inject
	ExampleGuestbookHandler guestbook;

	@POST
	@Path("/guests")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response sign(@FormParam("name") String name) {
		LOG.infof("EXAMPLE_GUEST_RECEIVED name=%s", name);
		return switch (ExampleGuest.parse(name)) {
			case ExampleGuest guest -> {
				guestbook.sign(guest);
				yield Response.noContent()
					.header(HtmxHeaders.HX_TRIGGER, UiEventName.ExampleGuestSigned.name())
					.build();
			}
			case ParsedExampleGuest.Invalid invalid -> {
				Map<String, String> errors = errors(invalid.violations());
				LOG.warnf("EXAMPLE_GUEST_INVALID %s", errors);
				var form = new ExampleSignFormVM(name == null ? "" : name, errors);
				yield Response.status(Response.Status.BAD_REQUEST)
					.entity(new UiResponse(UiRouteName.ExampleSignForm, form))
					.build();
			}
		};
	}

	/** Field (the last node of the property path) to message; the first message by sort order per field. */
	private static Map<String, String> errors(Set<? extends ConstraintViolation<?>> violations) {
		Map<String, String> errors = new LinkedHashMap<>();
		violations.stream()
			.sorted(Comparator.comparing((ConstraintViolation<?> v) -> field(v)).thenComparing(ConstraintViolation::getMessage))
			.forEach(v -> errors.putIfAbsent(field(v), v.getMessage()));
		return errors;
	}

	private static String field(ConstraintViolation<?> violation) {
		String field = "";
		for (Node node : violation.getPropertyPath()) {
			field = node.getName();
		}
		return field;
	}
}
