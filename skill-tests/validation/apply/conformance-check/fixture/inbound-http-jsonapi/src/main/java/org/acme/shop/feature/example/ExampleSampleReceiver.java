package org.acme.shop.feature.example;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/** Accepts a sample example, named by a query parameter. */
@Path("/api/example/sample")
public class ExampleSampleReceiver {

    @Inject
    ExampleHandler exampleHandler;

    public record SampleJson(String message) {
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public SampleJson sample(@QueryParam("name") String name) {
        return new SampleJson(exampleHandler.accept(new Example(name, 1)));
    }
}
