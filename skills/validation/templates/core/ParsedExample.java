package @PACKAGE@.feature.example;

import jakarta.validation.ConstraintViolation;

import java.util.Set;

/**
 * The result of {@link Example#parse}: the {@link Example} itself, or {@link Invalid} with every violation.
 * A top-level file of its own: a record cannot implement an interface nested inside itself (cyclic inheritance).
 */
public sealed interface ParsedExample permits Example, ParsedExample.Invalid {

    record Invalid(Set<ConstraintViolation<Example>> violations) implements ParsedExample {
    }
}
