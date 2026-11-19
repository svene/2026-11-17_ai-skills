# outbound-httpclient

REST clients through the Quarkus REST client.

**Extensions:** `io.quarkus:quarkus-rest-client`, `io.quarkus:quarkus-rest-client-jackson`.

```java
// outbound-httpclient: <root>.feature.<x>.<Partner>Client (the HTTP contract)
@Path("/")
@RegisterRestClient(configKey = "<partner>")
public interface <Partner>Client {
	@POST
	@Path("/orders")
	@Consumes(MediaType.APPLICATION_JSON)
	void placeOrder(OrderRequest request);
}

public record OrderRequest(String productName, int quantity) {}

// core: <root>.feature.<x>.<Partner>SPI
public interface <Partner>SPI {
	void placeOrder(Order order);
}

// outbound-httpclient: <root>.feature.<x>.<Partner>Service
@ApplicationScoped
public class <Partner>Service implements <Partner>SPI {
	@Inject
	@RestClient
	<Partner>Client client;

	@Override
	public void placeOrder(Order order) {
		client.placeOrder(new OrderRequest(order.productName(), order.quantity()));
	}
}
```

The client interface and its request/response records stay in this module; the SPI speaks domain types.

**Config:** `quarkus.rest-client.<partner>.url=https://<partner>.example.com`, overridden in `%test` and `%dev`.

**Test:** stub the partner. Either use a JAX-RS resource in `app-server/src/test/java` (with `%test.quarkus.rest-client.<partner>.url=http://localhost:${quarkus.http.test-port}/stub/<partner>`), or use WireMock. Call the SPI and assert on what the stub received.
