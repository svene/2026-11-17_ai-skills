# outbound-kafka

Kafka producers.

**Extensions:** `io.quarkus:quarkus-messaging-kafka`, `io.quarkus:quarkus-jackson`.

```java
// core: <root>.feature.<x>.<X>PublisherSPI
public interface <X>PublisherSPI {
	void publish(Order order);
}

// outbound-kafka: <root>.feature.<x>.<X>Message
public record <X>Message(String productName, int quantity) {}

// outbound-kafka: <root>.feature.<x>.<X>PublisherService
@ApplicationScoped
public class <X>PublisherService implements <X>PublisherSPI {
	@Inject
	@Channel("<x>-out")
	Emitter<<X>Message> emitter;

	@Override
	public void publish(Order order) {
		emitter.send(new <X>Message(order.productName(), order.quantity()));
	}
}
```

**Config:**

```properties
mp.messaging.outgoing.<x>-out.connector=smallrye-kafka
mp.messaging.outgoing.<x>-out.topic=<x>
mp.messaging.outgoing.<x>-out.value.serializer=io.quarkus.kafka.client.serialization.ObjectMapperSerializer
```

Leave `kafka.bootstrap.servers` unset: Dev Services starts a broker in dev and test (needs Docker).

**Test:** `@QuarkusTest` with a test-only `@Incoming` consumer on the same topic (`mp.messaging.incoming.<x>-test-in…` in `%test`) that collects messages. Call the SPI, then wait with Awaitility until the message arrives.
