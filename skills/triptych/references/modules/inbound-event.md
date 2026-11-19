# inbound-event

In-process triggers: CDI events and scheduled jobs. Either one drives a Handler just like an HTTP request does.

**Extensions:** `io.quarkus:quarkus-scheduler` (for scheduled jobs; CDI events need only `quarkus-arc`, which comes with `core`).

```java
package <root>.feature.<x>;

@ApplicationScoped
public class <X>EventReceiver {
	@Inject
	<X>Handler handler;

	void on<Something>(@ObservesAsync <Something>Happened event) {
		try {
			handler.react(event.id());
		} catch (RuntimeException e) {
			Log.errorf(e, "Reacting to %s failed", event);
		}
	}
}

@ApplicationScoped
public class <X>ScheduleReceiver {
	@Inject
	<X>Handler handler;

	@Scheduled(every = "${<x>.interval:1h}")
	void tick() {
		handler.runPeriodic();
	}
}
```

The event record (`<Something>Happened`) is a domain type in `core`. The Handler that fires it injects `Event<<Something>Happened>` and calls `fireAsync`.

**Config:** the interval property, if any.

**Test:** `@QuarkusTest` that fires the event (or calls the Handler that fires it) and waits with Awaitility for the effect. For scheduled jobs, test the Handler method directly, and set the interval in `%test` to something that never triggers during tests (e.g. `%test.<x>.interval=off`, with `@Scheduled(every = "${<x>.interval}")`).
