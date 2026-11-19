package @PACKAGE@.devsupport;

// Provenance: svene/2026-07-26_quarkus-hexagon-demo-mm-per-tech@b2f8493 app-server/.../devsupport/ArchitectureTest.java (seswdevskills triptych skill; triptych rules only); owned by this project from here on.

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static @PACKAGE@.devsupport.TriptychArchitecture.triptychArchitecture;

import com.tngtech.archunit.ArchConfiguration;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Checks this project's "Triptych" architecture (see {@link TriptychArchitecture}). The Maven module graph
 * already enforces the hexagonal layering (core depends on no adapter module, adapters not on each other), but
 * it cannot see across features inside the shared core module, nor stop an inbound adapter from injecting an SPI.
 * Those two concerns are checked here.
 * <p>
 * Every module reaches app-server as a JAR dependency, so the scan must include project JARs: the predefined
 * DO_NOT_INCLUDE_JARS option would leave only app-server's own classes and make every rule vacuous.
 */
class ArchitectureTest {
	private static final String PKG_ROOT = "@PACKAGE@";
	private static final String PKG_ROOT_PATH = "/" + PKG_ROOT.replace('.', '/') + "/";
	private static final Pattern INBOUND_MODULE_PATH = Pattern.compile("/inbound-[^/]+/");

	/** Skips third-party JARs during the scan; project JARs (core, every adapter module) stay included. */
	private static final ImportOption EXCLUDE_THIRD_PARTY_JARS =
		location -> !location.isJar() || location.contains(PKG_ROOT_PATH);

	/**
	 * Packages cannot tell modules apart (all of them share the feature/cross packages), so the module is taken
	 * from the class file's location: {@code .../inbound-x/target/classes/...}, {@code .../inbound-x/target/inbound-x-<v>.jar!/...}
	 * or {@code .../inbound-x/<v>/inbound-x-<v>.jar!/...} in the local repository.
	 */
	private static final DescribedPredicate<JavaClass> RESIDE_IN_INBOUND_MODULE = DescribedPredicate.describe(
		"reside in an inbound-* module",
		javaClass -> javaClass.getSource()
			.map(source -> INBOUND_MODULE_PATH.matcher(source.getUri().toString()).find())
			.orElse(false)
	);

	JavaClasses importedClasses;

	@BeforeEach
	void beforeEach() {
		ArchConfiguration.get().setProperty("import.dependencyResolutionProcess.maxIterationsForMemberTypes", "0");
		ArchConfiguration.get().setProperty("import.dependencyResolutionProcess.maxIterationsForAccessToTypes", "0");
		ArchConfiguration.get().setProperty("import.dependencyResolutionProcess.maxIterationsForSupertypes", "0");
		ArchConfiguration.get().setProperty("import.dependencyResolutionProcess.maxIterationsForEnclosingTypes", "0");
		ArchConfiguration.get().setProperty("import.dependencyResolutionProcess.maxIterationsForGenericSignatureTypes", "0");

		importedClasses = new ClassFileImporter(List.of(
			ImportOption.Predefined.DO_NOT_INCLUDE_TESTS,
			EXCLUDE_THIRD_PARTY_JARS
		)).importPackages(PKG_ROOT);
	}

	@Test
	void triptych_architecture_is_respected() {
		triptychArchitecture(PKG_ROOT).check(importedClasses);
	}

	/**
	 * Inbound adapters drive the domain through a {@code *Handler} only, never through an outbound port: a
	 * receiver injecting an SPI would compile (SPIs live in core, which every inbound module depends on) and CDI
	 * would hand it the outbound adapter directly, bypassing the use case. Covers every class of an inbound-*
	 * module, not just {@code *Receiver}s, so a helper a receiver delegates to cannot slip through.
	 */
	@Test
	void inbound_adapters_do_not_use_spis() {
		noClasses().that(RESIDE_IN_INBOUND_MODULE)
			.should().dependOnClassesThat().haveSimpleNameEndingWith("SPI")
			.because("inbound adapters must go through a Handler, not bypass it via an outbound port")
			.check(importedClasses);
	}
}
