package org.acme.shop.feature.item;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

/** The "add item" form: GET answers the empty form's envelope, POST adds the item and triggers "itemAdded". */
@Path("/vm/items")
public class ItemFormReceiver {

    @Inject
    ItemHandler itemHandler;

    /** The envelope the Hono client renders: the template named by {@code route}, filled with {@code vm}. */
    public record RouteJson<T>(String route, T vm) {
    }

    /** The form's view model: the field values as entered, and an error message per field. */
    public record ItemFormVm(String name, String quantity, Map<String, String> errors) {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public RouteJson<ItemFormVm> form() {
        return new RouteJson<>("ItemForm", new ItemFormVm("", "", Map.of()));
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(@FormParam("name") String name, @FormParam("quantity") String quantity) {
        itemHandler.add(new Item(name, Integer.parseInt(quantity)));
        return Response.noContent().header("HX-Trigger", "itemAdded").build();
    }
}
