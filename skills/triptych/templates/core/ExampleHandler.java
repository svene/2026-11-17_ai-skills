package @PACKAGE@.feature.example;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * A domain-free example use case, so the architecture rules have something to check from the start.
 * Its public methods are its inbound port. Replace it with a real feature slice, then delete it.
 */
@ApplicationScoped
public class ExampleHandler {

    public String greet(String name) {
        return "Hello, " + (name == null || name.isBlank() ? "world" : name.strip());
    }
}
