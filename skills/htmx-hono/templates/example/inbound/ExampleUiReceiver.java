package @PACKAGE@.feature.example;

import @PACKAGE@.cross.htmlrender.UiRouteName;
import @PACKAGE@.cross.htmlrender.UiRouteSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.Map;
import java.util.function.Function;

/** The example slice's component routes: each builds the view model its template renders. Read only. */
@ApplicationScoped
public class ExampleUiReceiver implements UiRouteSource {

	@Inject
	ExampleGuestbookHandler guestbook;

	@Override
	public Map<UiRouteName, Function<MultivaluedMap<String, String>, Object>> routes() {
		return Map.of(
			UiRouteName.ExamplePage, query -> new ExamplePageVM(guestList(), ExampleSignFormVM.empty()),
			UiRouteName.ExampleGuestList, query -> guestList(),
			UiRouteName.ExampleSignForm, query -> ExampleSignFormVM.empty());
	}

	private ExampleGuestListVM guestList() {
		return new ExampleGuestListVM(guestbook.names());
	}
}
