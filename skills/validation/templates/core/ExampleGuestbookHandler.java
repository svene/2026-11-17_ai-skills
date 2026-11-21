package @PACKAGE@.feature.example;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The use case behind the htmx example page: a guestbook kept in memory. Its public methods are its inbound port.
 * Replace it with a real feature slice, then delete it.
 */
@ApplicationScoped
public class ExampleGuestbookHandler {

	private static final Logger LOG = Logger.getLogger(ExampleGuestbookHandler.class);

	private final List<String> names = new CopyOnWriteArrayList<>();

	public List<String> names() {
		return List.copyOf(names);
	}

	/** Takes an {@link ExampleGuest} only, so it can rely on the constraints: no checks of its own. */
	public void sign(ExampleGuest guest) {
		LOG.infof("EXAMPLE_GUEST_PROCESSING name=%s", guest.name());
		names.add(guest.name());
	}
}
