# inbound-kafka

Kafka consumers.

**Extensions:** `io.quarkus:quarkus-messaging-kafka`, `io.quarkus:quarkus-jackson`, `io.quarkus:quarkus-smallrye-fault-tolerance` (for `@Retry`).

```java
package <root>.feature.<x>;

public record <X>Message(String id, int quantity) {}

public class <X>MessageDeserializer extends ObjectMapperDeserializer<<X>Message> {
	public <X>MessageDeserializer() { super(<X>Message.class); }
}

@ApplicationScoped
public class <X>Receiver {
	@Inject
	<X>Handler handler;

	@Incoming("<x>-in")
	@Retry(maxRetries = 3, delay = 1, delayUnit = ChronoUnit.SECONDS)
	public void receive(<X>Message message) {
		if (message == null) return; // tombstone or undeserializable payload
		handler.record(message.id(), message.quantity());
	}
}
```

The message types and deserializers live in this module; the Receiver maps the message to domain values before it calls the Handler.

**Config** (`application.properties` in `app-server`):

```properties
mp.messaging.incoming.<x>-in.connector=smallrye-kafka
mp.messaging.incoming.<x>-in.topic=<x>
mp.messaging.incoming.<x>-in.value.deserializer=<root>.feature.<x>.<X>MessageDeserializer
mp.messaging.incoming.<x>-in.fail-on-deserialization-failure=false
mp.messaging.incoming.<x>-in.failure-strategy=dead-letter-queue
mp.messaging.incoming.<x>-in.dead-letter-queue.topic=<x>-dlq
```

Leave `kafka.bootstrap.servers` unset: Dev Services starts a broker in dev and test (needs Docker).

**Test:** `@QuarkusTest` that sends a message through an `@Channel` `Emitter` declared in the test (with an `mp.messaging.outgoing.<x>-test-out` channel on the same topic), then waits with Awaitility (`org.awaitility:awaitility`, test scope) until the Handler's effect is visible.
