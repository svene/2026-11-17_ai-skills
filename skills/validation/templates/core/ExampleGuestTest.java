package @PACKAGE@.feature.example;

import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** parse() returns an ExampleGuest or its violations; the constructor throws. No Quarkus needed. */
class ExampleGuestTest {

    @Test
    void parse_returnsGuest_withTheNameStripped() {
        assertThat(ExampleGuest.parse("  Ada ")).isEqualTo(new ExampleGuest("Ada"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void parse_returnsInvalid_forBlankName(String name) {
        assertThat(messages(ExampleGuest.parse(name))).containsExactly("must not be blank");
    }

    @Test
    void parse_returnsInvalid_forNullName() {
        assertThat(messages(ExampleGuest.parse(null))).containsExactly("must not be blank");
    }

    @Test
    void violations_nameTheField() {
        var invalid = (ParsedExampleGuest.Invalid) ExampleGuest.parse(" ");
        assertThat(invalid.violations()).extracting(v -> v.getPropertyPath().toString()).containsExactly("ExampleGuest.name");
    }

    @Test
    void constructor_throws_forTrustedDataThatIsInvalid() {
        assertThatThrownBy(() -> new ExampleGuest(" ")).isInstanceOf(IllegalArgumentException.class);
    }

    private static List<String> messages(ParsedExampleGuest result) {
        assertThat(result).isInstanceOf(ParsedExampleGuest.Invalid.class);
        return ((ParsedExampleGuest.Invalid) result).violations().stream().map(ConstraintViolation::getMessage).toList();
    }
}
