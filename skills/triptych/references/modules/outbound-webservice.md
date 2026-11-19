# outbound-webservice

SOAP clients through Quarkus CXF.

**Extension:** `io.quarkiverse.cxf:quarkus-cxf:${quarkus.cxf.version}`. Add the property `<quarkus.cxf.version>3.33.2</quarkus.cxf.version>` to the parent pom, and pick the quarkus-cxf release that matches the project's Quarkus version.

```java
// outbound-webservice: <root>.feature.<x>.<Partner>OrderService (the SOAP contract)
@WebService(name = "<Partner>OrderService", targetNamespace = "http://<partner>.example.com/")
public interface <Partner>OrderService {
	@WebMethod
	void placeOrder(@WebParam(name = "productName") String productName, @WebParam(name = "quantity") int quantity);
}

// core: <root>.feature.<x>.<Partner>SupplierSPI
public interface <Partner>SupplierSPI {
	void placeOrder(Order order);
}

// outbound-webservice: <root>.feature.<x>.<Partner>SupplierService
@ApplicationScoped
public class <Partner>SupplierService implements <Partner>SupplierSPI {
	@Inject
	@CXFClient("<partner>")
	<Partner>OrderService client;

	@Override
	public void placeOrder(Order order) {
		client.placeOrder(order.productName(), order.quantity());
	}
}
```

The contract interface carries the SOAP service's name, so it ends in `Service` without being a Triptych role. It stays in this module beside the real adapter.

**Config:**

```properties
quarkus.cxf.client.<partner>.client-endpoint-url=http://localhost:8089/soap/<partner>
quarkus.cxf.client.<partner>.service-interface=<root>.feature.<x>.<Partner>OrderService
```

**Test:** publish a stub implementation of the contract in the test (`quarkus.cxf.endpoint."/<partner>".implementor=…` in `%test`, with the client URL pointing at `http://localhost:${quarkus.http.test-port}/soap/<partner>`), call the SPI and assert on what the stub received.
