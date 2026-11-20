package @PACKAGE@.cross.messaging;

// Provenance: svene/2026-07-26_quarkus-hexagon-demo-mm-per-tech@b2f8493 inbound-kafka/.../cross/kafka/UnprocessableMessageException.java (seswdevskills validation skill); owned by this project from here on.

/**
 * Thrown by a receiver for a message that can never be processed, however often it is retried (a tombstone, a
 * broken structure). Receivers name it in {@code @Retry(abortOn = ...)}, so it skips the retries and the channel's
 * {@code failure-strategy=dead-letter-queue} sends the message to the dead-letter topic at once.
 * <p>
 * Not for constraint violations: a message that parses into {@code Parsed*.Invalid} is well-formed but wrong,
 * and is logged and skipped by the receiver.
 */
public class UnprocessableMessageException extends RuntimeException {

    public UnprocessableMessageException(String message) {
        super(message);
    }
}
