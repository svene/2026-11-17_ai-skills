package @PACKAGE@.feature.example;

/**
 * The wire shape of a message on topic "example". Everything is text, so a non-number count reaches
 * {@link Example#parse(String, String)} and is reported as a violation instead of failing deserialization.
 */
public record ExampleMessage(String name, String count) {
}
