package @PACKAGE@.feature.example;

import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** parse() returns an Example or every violation; the constructor throws. No Quarkus needed. */
class ExampleTest {

    @Test
    void parse_returnsExample_forValidInput() {
        assertThat(Example.parse("widget", 3)).isEqualTo(new Example("widget", 3));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void parse_returnsInvalid_forBlankName(String name) {
        assertThat(messages(Example.parse(name, 3))).containsExactly("must not be blank");
    }

    @Test
    void parse_returnsInvalid_forNullName() {
        assertThat(messages(Example.parse(null, 3))).containsExactly("must not be blank");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void parse_returnsInvalid_forCountNotPositive(int count) {
        assertThat(messages(Example.parse("widget", count))).containsExactly("must be greater than 0");
    }

    @Test
    void parse_reportsEveryViolation() {
        assertThat(messages(Example.parse(" ", 0))).containsExactlyInAnyOrder("must not be blank", "must be greater than 0");
    }

    // --- parse(String, String): text input ---

    @ParameterizedTest
    @ValueSource(strings = {"1", " 42 ", "+7"})
    void parseText_returnsExample_forNumericCount(String count) {
        assertThat(Example.parse("widget", count)).isEqualTo(new Example("widget", Integer.parseInt(count.trim())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1.5", "1 2", "1234567890"})
    void parseText_returnsInvalid_forNonNumericCount(String count) {
        assertThat(messages(Example.parse("widget", count))).containsExactly("must be a number");
    }

    @Test
    void parseText_returnsInvalid_forMissingCount() {
        assertThat(messages(Example.parse("widget", (String) null))).containsExactly("must not be blank");
    }

    @Test
    void parseText_appliesTheIntConstraints_afterConversion() {
        assertThat(messages(Example.parse("widget", "0"))).containsExactly("must be greater than 0");
    }

    @Test
    void parseText_reportsBlankNameAndNonNumericCountTogether() {
        assertThat(messages(Example.parse("", "abc"))).containsExactlyInAnyOrder("must not be blank", "must be a number");
    }

    @Test
    void violations_nameTheField() {
        var invalid = (ParsedExample.Invalid) Example.parse("widget", "abc");
        // Receivers report the last node of the path ("Example.count") as the field.
        assertThat(invalid.violations()).extracting(v -> v.getPropertyPath().toString()).containsExactly("Example.count");
    }

    @Test
    void constructor_throws_forTrustedDataThatIsInvalid() {
        assertThatThrownBy(() -> new Example("widget", 0)).isInstanceOf(IllegalArgumentException.class);
    }

    private static List<String> messages(ParsedExample result) {
        assertThat(result).isInstanceOf(ParsedExample.Invalid.class);
        return ((ParsedExample.Invalid) result).violations().stream().map(ConstraintViolation::getMessage).toList();
    }
}
