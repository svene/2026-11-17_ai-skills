package @PACKAGE@.feature.example;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.lang.reflect.Constructor;
import java.util.Set;

import static @PACKAGE@.cross.validation.ConstructorValidation.declaredConstructor;
import static @PACKAGE@.cross.validation.ConstructorValidation.validateParameters;

/**
 * A domain value in the "parse, don't validate" shape: the constraints sit on the components, once, and an
 * {@code Example} exists only if they hold. Untrusted input goes through {@link #parse(String, int)} or
 * {@link #parse(String, String)}, which return either an {@code Example} or a {@link ParsedExample.Invalid};
 * {@code ValidationArchitectureTest} stops inbound modules from calling the constructor.
 */
public record Example(@NotBlank String name, @Positive int count) implements ParsedExample {

    private static final Constructor<Example> CANONICAL_CONSTRUCTOR = declaredConstructor(Example.class, String.class, int.class);
    private static final Constructor<Example> TEXT_CONSTRUCTOR = declaredConstructor(Example.class, String.class, String.class);

    /** For trusted data only: throws {@link IllegalArgumentException} on a violation. */
    public Example {
        Set<ConstraintViolation<Example>> violations = validate(name, count);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.iterator().next().getMessage());
        }
    }

    /**
     * Never invoked: it only exists so Bean Validation can check text input against its parameter constraints
     * (see {@link #parse(String, String)}). Records require it to delegate to the canonical one. A blank count
     * passes {@code @Pattern} on purpose, so it is reported only as "must not be blank".
     */
    private Example(@NotBlank String name,
                    @NotBlank @Pattern(regexp = "\\s*([+-]?\\d{1,9}\\s*)?", message = "must be a number") String count) {
        this(name, Integer.parseInt(count.trim()));
    }

    /**
     * For text input, e.g. a form field or a JSON string: checks that {@code count} is a number (at most 9 digits,
     * so it always fits an {@code int}), then applies the same constraints as {@link #parse(String, int)}.
     */
    public static ParsedExample parse(String name, String count) {
        Set<ConstraintViolation<Example>> violations = validateParameters(TEXT_CONSTRUCTOR, name, count);
        return violations.isEmpty()
            ? parse(name, Integer.parseInt(count.trim()))
            : new ParsedExample.Invalid(violations);
    }

    public static ParsedExample parse(String name, int count) {
        Set<ConstraintViolation<Example>> violations = validate(name, count);
        return violations.isEmpty()
            ? new Example(name, count)
            : new ParsedExample.Invalid(violations);
    }

    private static Set<ConstraintViolation<Example>> validate(String name, int count) {
        return validateParameters(CANONICAL_CONSTRUCTOR, name, count);
    }
}
