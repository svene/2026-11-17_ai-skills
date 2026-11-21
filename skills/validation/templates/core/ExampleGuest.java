package @PACKAGE@.feature.example;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.NotBlank;

import java.lang.reflect.Constructor;
import java.util.Set;

import static @PACKAGE@.cross.validation.ConstructorValidation.declaredConstructor;
import static @PACKAGE@.cross.validation.ConstructorValidation.validateParameters;

/**
 * The guestbook's domain value, in the same "parse, don't validate" shape as {@link Example}: a guest exists only
 * with a name that is not blank. The sign form's text goes through {@link #parse(String)};
 * {@code ValidationArchitectureTest} stops inbound modules from calling the constructor.
 */
public record ExampleGuest(@NotBlank String name) implements ParsedExampleGuest {

    private static final Constructor<ExampleGuest> CANONICAL_CONSTRUCTOR = declaredConstructor(ExampleGuest.class, String.class);

    /** For trusted data only: throws {@link IllegalArgumentException} on a violation. Strips the name. */
    public ExampleGuest {
        Set<ConstraintViolation<ExampleGuest>> violations = validateParameters(CANONICAL_CONSTRUCTOR, name);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.iterator().next().getMessage());
        }
        name = name.strip();
    }

    public static ParsedExampleGuest parse(String name) {
        Set<ConstraintViolation<ExampleGuest>> violations = validateParameters(CANONICAL_CONSTRUCTOR, name);
        return violations.isEmpty()
            ? new ExampleGuest(name)
            : new ParsedExampleGuest.Invalid(violations);
    }
}
