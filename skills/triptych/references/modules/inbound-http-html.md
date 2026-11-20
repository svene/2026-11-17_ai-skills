# inbound-http-html

Server-rendered HTML pages. (For htmx pages with view models, use the htmx-hono skill instead; it owns `inbound-http-json-vm`.)

**Extensions:** `io.quarkus:quarkus-rest`, `io.quarkus:quarkus-rest-jackson`.

```java
package <root>.feature.<x>;

@Path("/<x>")
public class <X>PageReceiver {
	@Inject
	<X>Handler handler;

	@GET
	@Produces(MediaType.TEXT_HTML)
	public String page() {
		return "<!doctype html><title><X></title><p>" + escape(handler.summary()) + "</p>";
	}
}
```

Page-shaped data (a view model record) lives in this module and is built from what the Handler returns. Escape every value that comes from the domain or the request.

**Config:** none.

**Test:** `@QuarkusTest` with RestAssured: `GET /<x>` returns 200, `text/html`, and the body contains the expected text.
