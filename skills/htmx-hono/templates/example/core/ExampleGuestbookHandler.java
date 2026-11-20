package @PACKAGE@.feature.example;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The use case behind the htmx example page: a guestbook kept in memory. Its public methods are its inbound port.
 * Replace it with a real feature slice, then delete it.
 */
@ApplicationScoped
public class ExampleGuestbookHandler {

	private final List<String> names = new CopyOnWriteArrayList<>();

	public List<String> names() {
		return List.copyOf(names);
	}

	public void sign(String name) {
		names.add(name.strip());
	}
}
