# inbound-http-jsonapi

JSON REST endpoints. The fresh branch already creates this module with `ExampleReceiver`.

**Extensions:** `io.quarkus:quarkus-rest`, `io.quarkus:quarkus-rest-jackson`.

```java
package <root>.feature.<x>;

@Path("/api/<x>")
public class <X>Receiver {
	@Inject
	<X>Handler handler;

	record <X>Json(String message) {}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public <X>Json get(@QueryParam("name") String name) {
		return new <X>Json(handler.greet(name));
	}
}
```

The JSON types (records) live beside the Receiver in this module; `core` never sees them.

**Config:** none.

**Test:** `@QuarkusTest` in `app-server` with RestAssured (`io.rest-assured:rest-assured`, test scope): call the endpoint and assert on the status and the JSON body.
