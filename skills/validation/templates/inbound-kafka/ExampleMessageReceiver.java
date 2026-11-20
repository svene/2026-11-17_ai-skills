package @PACKAGE@.feature.example;

import @PACKAGE@.cross.messaging.UnprocessableMessageException;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

/**
 * Inbound adapter of the example slice for topic "example" (channel "example-in"). Three outcomes:
 * <ul>
 *   <li>valid: {@link ExampleHandler#accept}</li>
 *   <li>well-formed but invalid ({@link ParsedExample.Invalid}): logged as EXAMPLE_INVALID and skipped; a retry
 *       or the dead-letter topic would not make it valid</li>
 *   <li>unprocessable (tombstone here; undeserializable messages never arrive, SmallRye sends them to the
 *       dead-letter topic itself): {@link UnprocessableMessageException}, which skips the retries and goes to
 *       the dead-letter topic "example-dlq"</li>
 * </ul>
 * Any other exception is treated as transient: retried, then dead-lettered.
 */
@ApplicationScoped
public class ExampleMessageReceiver {

    private static final Logger LOG = Logger.getLogger(ExampleMessageReceiver.class);

    @Inject
    ExampleHandler exampleHandler;

    @Incoming("example-in")
    @Blocking
    @Retry(maxRetries = 3, delay = 1, delayUnit = ChronoUnit.SECONDS, abortOn = UnprocessableMessageException.class)
    public void receive(ExampleMessage message) {
        LOG.infof("EXAMPLE_RECEIVED %s", message == null ? "null payload (tombstone)" : message);
        rejectUnprocessable(message);
        switch (Example.parse(message.name(), message.count())) {
            case Example example -> exampleHandler.accept(example);
            case ParsedExample.Invalid invalid -> LOG.warnf("EXAMPLE_INVALID %s: %s", message, messages(invalid));
        }
    }

    private static void rejectUnprocessable(ExampleMessage message) {
        if (message == null) {
            throw new UnprocessableMessageException("null payload (tombstone)");
        }
    }

    private static String messages(ParsedExample.Invalid invalid) {
        return invalid.violations().stream()
            .map(v -> v.getPropertyPath() + " " + v.getMessage())
            .sorted()
            .collect(Collectors.joining(", "));
    }
}
