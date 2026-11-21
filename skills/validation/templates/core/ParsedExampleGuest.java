package @PACKAGE@.feature.example;

import jakarta.validation.ConstraintViolation;

import java.util.Set;

/** The result of {@link ExampleGuest#parse}: the {@link ExampleGuest} itself, or {@link Invalid} with every violation. */
public sealed interface ParsedExampleGuest permits ExampleGuest, ParsedExampleGuest.Invalid {

    record Invalid(Set<ConstraintViolation<ExampleGuest>> violations) implements ParsedExampleGuest {
    }
}
