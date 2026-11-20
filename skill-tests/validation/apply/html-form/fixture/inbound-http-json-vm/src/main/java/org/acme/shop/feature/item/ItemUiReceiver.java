package org.acme.shop.feature.item;

import org.acme.shop.cross.htmlrender.UiRouteName;
import org.acme.shop.cross.htmlrender.UiRouteSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.Map;
import java.util.function.Function;

/** The item slice's component routes. Read only. */
@ApplicationScoped
public class ItemUiReceiver implements UiRouteSource {

	@Override
	public Map<UiRouteName, Function<MultivaluedMap<String, String>, Object>> routes() {
		return Map.of(UiRouteName.ItemForm, query -> ItemFormVM.empty());
	}
}
