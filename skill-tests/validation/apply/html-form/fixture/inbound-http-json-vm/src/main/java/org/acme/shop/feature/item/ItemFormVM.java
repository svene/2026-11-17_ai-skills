package org.acme.shop.feature.item;

import java.util.Map;

/** View model of the ItemForm route: the field values as entered, and an error message per invalid field. */
public record ItemFormVM(String name, String quantity, Map<String, String> errors) {

	public static ItemFormVM empty() {
		return new ItemFormVM("", "", Map.of());
	}
}
