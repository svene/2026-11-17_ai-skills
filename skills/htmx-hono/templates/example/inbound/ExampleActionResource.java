package @PACKAGE@.feature.example;

import @PACKAGE@.cross.htmlrender.HtmxHeaders;
import @PACKAGE@.cross.htmlrender.UiEventName;
import @PACKAGE@.cross.htmlrender.UiResponse;
import @PACKAGE@.cross.htmlrender.UiRouteName;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

/**
 * The example slice's mutations. A success is 204 plus an HX-Trigger event the affected components re-fetch on;
 * invalid input is a 400 whose {route, vm} envelope re-renders the form with its errors.
 */
@Path("/example")
public class ExampleActionResource {

	@Inject
	ExampleGuestbookHandler guestbook;

	@POST
	@Path("/guests")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response sign(@FormParam("name") String name) {
		// A demo check on a plain String. A real feature's form parses its input into a domain value with parse(),
		// per the validation skill.
		if (name == null || name.isBlank()) {
			var form = new ExampleSignFormVM(name == null ? "" : name, Map.of("name", "must not be blank"));
			return Response.status(Response.Status.BAD_REQUEST)
				.entity(new UiResponse(UiRouteName.ExampleSignForm, form))
				.build();
		}
		guestbook.sign(name);
		return Response.noContent()
			.header(HtmxHeaders.HX_TRIGGER, UiEventName.ExampleGuestSigned.name())
			.build();
	}
}
