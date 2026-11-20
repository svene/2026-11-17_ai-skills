package @PACKAGE@.cross.htmlrender;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test of the route table: no Quarkus, no HTTP. The sources are stubs serving any set of {@link UiRouteName}s,
 * so the test holds however many routes the project has. The real sources are checked at boot (AppServerBootTest).
 */
class UiRouteReceiverUnitTest {

	@Test
	void answersTheEnvelope_withTheVmOfTheRoute_builtFromTheQuery() {
		var receiver = new UiRouteReceiver();
		receiver.buildTable(List.of(serving(EnumSet.allOf(UiRouteName.class))));
		UiRouteName route = UiRouteName.values()[0];
		MultivaluedMap<String, String> query = new MultivaluedHashMap<>();
		query.putSingle("id", "42");

		assertThat(receiver.respond(route.name(), query)).isEqualTo(new UiResponse(route, route.name() + "?id=42"));
	}

	@Test
	void unknownRoute_isNotFound() {
		var receiver = new UiRouteReceiver();
		receiver.buildTable(List.of(serving(EnumSet.allOf(UiRouteName.class))));

		assertThatThrownBy(() -> receiver.respond("NoSuchRoute", new MultivaluedHashMap<>())).isInstanceOf(NotFoundException.class);
	}

	@Test
	void aRouteServedTwice_failsTheTable() {
		var all = serving(EnumSet.allOf(UiRouteName.class));

		assertThatThrownBy(() -> new UiRouteReceiver().buildTable(List.of(all, all)))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("more than one UiRouteSource");
	}

	@Test
	void aRouteServedByNoSource_failsTheTable() {
		Set<UiRouteName> allButFirst = EnumSet.allOf(UiRouteName.class);
		allButFirst.remove(UiRouteName.values()[0]);

		assertThatThrownBy(() -> new UiRouteReceiver().buildTable(List.of(serving(allButFirst))))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("not served by any UiRouteSource");
	}

	/** A source whose view model of each route is "<route>?id=<the id query parameter>". */
	private static UiRouteSource serving(Set<UiRouteName> routes) {
		Map<UiRouteName, Function<MultivaluedMap<String, String>, Object>> table = new EnumMap<>(UiRouteName.class);
		routes.forEach(route -> table.put(route, query -> route.name() + "?id=" + query.getFirst("id")));
		return () -> table;
	}
}
