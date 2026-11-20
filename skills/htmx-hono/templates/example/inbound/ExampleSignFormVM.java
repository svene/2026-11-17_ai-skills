package @PACKAGE@.feature.example;

import java.util.Map;

/**
 * View model of the ExampleSignForm route: the entered value, and an error message per invalid field (keyed by the
 * field name, which the template types as a key of this view model).
 */
public record ExampleSignFormVM(String name, Map<String, String> errors) {

	public static ExampleSignFormVM empty() {
		return new ExampleSignFormVM("", Map.of());
	}
}
