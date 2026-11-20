package org.acme.shop.feature.item;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/items")
public class ItemReceiver {

    @Inject
    ItemHandler itemHandler;

    public record ItemJson(@NotBlank String name, @Positive int quantity) {
    }

    public record AddedJson(String message) {
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(@Valid ItemJson request) {
        String message = itemHandler.add(new Item(request.name(), request.quantity()));
        return Response.status(Response.Status.CREATED).entity(new AddedJson(message)).build();
    }
}
