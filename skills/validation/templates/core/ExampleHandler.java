package @PACKAGE@.feature.example;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A domain-free example use case, so the architecture rules have something to check from the start.
 * Its public methods are its inbound port. Replace it with a real feature slice, then delete it.
 */
@ApplicationScoped
public class ExampleHandler {

    private static final Logger LOG = Logger.getLogger(ExampleHandler.class);

    /** Stands in for an SPI: a real slice would hand the accepted value to an outbound port. */
    private final List<Example> accepted = new CopyOnWriteArrayList<>();

    public String greet(String name) {
        return "Hello, " + (name == null || name.isBlank() ? "world" : name.strip());
    }

    /** Takes an {@link Example} only, so it can rely on the constraints: no checks of its own. */
    public String accept(Example example) {
        LOG.infof("EXAMPLE_PROCESSING name=%s count=%d", example.name(), example.count());
        accepted.add(example);
        return example.count() + " x " + example.name();
    }

    public List<Example> accepted() {
        return List.copyOf(accepted);
    }
}
