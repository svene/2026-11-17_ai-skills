package org.acme.shop.feature.item;

import org.acme.shop.cross.htmlrender.HtmxHeaders;
import org.acme.shop.cross.htmlrender.UiEventName;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** The item slice's mutations: adding an item answers 204 plus the ItemAdded event. */
@Path("/item")
public class ItemActionResource {

	@Inject
	ItemHandler itemHandler;

	@POST
	@Path("/items")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response add(@FormParam("name") String name, @FormParam("quantity") String quantity) {
		itemHandler.add(new Item(name, Integer.parseInt(quantity)));
		return Response.noContent()
			.header(HtmxHeaders.HX_TRIGGER, UiEventName.ItemAdded.name())
			.build();
	}
}
