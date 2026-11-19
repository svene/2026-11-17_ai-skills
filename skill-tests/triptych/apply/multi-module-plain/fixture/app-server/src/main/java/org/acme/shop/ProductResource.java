package org.acme.shop;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/products")
public class ProductResource {

    @Inject
    ProductStore store;

    @GET
    public List<Product> list() {
        return store.all();
    }

    @POST
    public Response add(Product product) {
        if (product.priceCents() < 0) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        store.add(product);
        return Response.status(Response.Status.CREATED).entity(product).build();
    }
}
