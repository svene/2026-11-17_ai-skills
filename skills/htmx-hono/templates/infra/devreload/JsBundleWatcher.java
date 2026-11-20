package @PACKAGE@.cross.devreload;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Dev only: polls the browser bundle that {@code npm run watch} (in inbound-http-json-vm) rewrites, and broadcasts a
 * reload when it changes. Java changes need no help: Quarkus dev mode restarts on the next request.
 */
@IfBuildProfile("dev")
@ApplicationScoped
@Startup
public class JsBundleWatcher {

	private static final Logger LOG = Logger.getLogger(JsBundleWatcher.class);

	/** Relative to the dev-mode working directory, which is app-server. */
	@ConfigProperty(name = "dev-reload.bundle",
		defaultValue = "../inbound-http-json-vm/target/classes/META-INF/resources/js/hono/hx-hono.js")
	String bundle;

	@Inject
	DevReloadSSE devReloadSSE;

	private long lastModified = -1;

	@Scheduled(every = "1s")
	void checkBundle() throws IOException {
		if (!Files.exists(Path.of(bundle))) return;
		long current = Files.getLastModifiedTime(Path.of(bundle)).toMillis();
		if (current != lastModified) {
			if (lastModified != -1) {
				LOG.info("browser bundle changed: broadcasting reload");
				devReloadSSE.broadcastReload();
			}
			lastModified = current;
		}
	}
}
