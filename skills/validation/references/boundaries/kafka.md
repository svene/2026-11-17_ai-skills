# Boundary: Kafka (`inbound-kafka`)

Nobody waits for the answer, so there is nobody to reject a message to. The template is
`${CLAUDE_SKILL_DIR}/templates/inbound-kafka/` plus `templates/fragments/kafka-application.properties`.
The background on poison pills, DLQs and when stopping is right is in
[kafka-unprocessable-messages.md](https://github.com/svene/2026-07-26_quarkus-hexagon-demo-mm-per-tech/blob/b2f849396eb60a3aae38918c7fab3033c93bb1d9/docs/architecture/kafka-unprocessable-messages.md).

## Ask, per consumer

Kafka cases exist only if `inbound-kafka` does. Without it, the skill is HTTP-only and does not stop. For each
consumer, ask which of these applies, and show 1 and 2 as the default:

1. **Invalid** (well-formed, but `parse()` returns `Invalid`): log `<OP>_INVALID` at WARN with the violations
   and topic/partition/offset when the consumer has them, then **skip**. Never retry: a retry would not make it valid.
2. **Unprocessable** (not deserializable, a tombstone, or a structure the Receiver cannot read, such as a missing
   list): goes to the **dead-letter topic** `<topic>-dlq`, which keeps the payload for replay.
3. *Optional:* **invalid messages go to the dead-letter topic too**, instead of 1. Choose this when an invalid
   message must not be lost. Implement it by throwing `UnprocessableMessageException` with the violations as
   its message in the invalid branch.

## The mechanics

```properties
mp.messaging.incoming.<x>-in.fail-on-deserialization-failure=false
mp.messaging.incoming.<x>-in.failure-strategy=dead-letter-queue
mp.messaging.incoming.<x>-in.dead-letter-queue.topic=<x>-dlq
mp.messaging.incoming.<x>-in.dead-letter-queue.value.serializer=io.quarkus.kafka.client.serialization.ObjectMapperSerializer
```

- **Deserialization failures** never reach the Receiver: SmallRye sends the original bytes to the DLQ itself,
  with `dead-letter-reason`, `dead-letter-topic` and the other headers. The value serializer must be set, because
  SmallRye cannot derive it from a custom deserializer.
- **Structural problems** the Receiver detects make it throw `cross.messaging.UnprocessableMessageException`.
  The message is nacked, and the exception message becomes the `dead-letter-reason` header. A tombstone is
  written to the DLQ as JSON `null` by `ObjectMapperSerializer`, not as a tombstone.
- **`@Retry(…, abortOn = UnprocessableMessageException.class)`** retries any other exception, which is treated as
  transient (the database is down). `@Blocking` keeps the retries off the event loop.

**Trade-off of the standard strategy.** After the retries, a transient failure **also goes to the DLQ**. The
source stopped the channel instead, so the message was processed again after a restart, using a custom
`dead-letter-or-fail-stop` strategy. If a transient failure must not land in the DLQ (ordered events, data that
must not be delayed silently), tell the user and offer that strategy; it is not part of the template.

The Receiver logs `<OP>_RECEIVED` first, with `null payload (tombstone)` for a null. A retry runs `receive()`
again, so `<OP>_RECEIVED` can appear more than once.

## Test

A `@QuarkusTest` in `app-server` with a real broker (Dev Services, needs Docker), with raw `String`
producers and consumers so the test can send what no well-behaved producer would. The template is
`templates/app-server/ExampleMessageReceiverTest.java`. Each case sends the bad message, then a valid one that
must still be processed:

- an invalid message is skipped: it does not reach the Handler and is not on the DLQ
- `{not json` lands on the DLQ unchanged, with `dead-letter-topic` set to the source topic
- a tombstone lands on the DLQ with its reason

Add `org.awaitility:awaitility` (test scope) to `app-server`.
