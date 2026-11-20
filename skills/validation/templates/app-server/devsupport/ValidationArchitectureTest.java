package @PACKAGE@.devsupport;

// Provenance: svene/2026-07-26_quarkus-hexagon-demo-mm-per-tech@b2f8493 app-server/.../devsupport/ArchitectureTest.java (seswdevskills validation skill; the parse rule only); owned by this project from here on.

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.ArchConfiguration;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Checks this project's input validation technique, "parse, don't validate" (see the "Input validation" link in
 * README.md): a domain value with constraints implements a {@code Parsed*} sealed interface, and untrusted input
 * reaches it only through its {@code parse(...)}, never through its constructor. The constructor throws on
 * invalid data; calling it at a boundary would turn bad input into a 500 instead of a reported violation.
 * <p>
 * The scan is set up as in {@code ArchitectureTest}: project JARs included, the module taken from the class file's
 * location.
 */
class ValidationArchitectureTest {
	private static final String PKG_ROOT = "@PACKAGE@";
	private static final String PKG_ROOT_PATH = "/" + PKG_ROOT.replace('.', '/') + "/";
	private static final Pattern INBOUND_MODULE_PATH = Pattern.compile("/inbound-[^/]+/");

	private static final ImportOption EXCLUDE_THIRD_PARTY_JARS =
		location -> !location.isJar() || location.contains(PKG_ROOT_PATH);

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

	/**
	 * Inbound adapters receive untrusted input, so they build domain values with {@code parse(...)} and switch over
	 * the result. Covers every class of an inbound-* module, so a mapper a receiver delegates to cannot slip through.
	 */
	@Test
	void inbound_adapters_construct_domain_values_only_via_parse() {
		DescribedPredicate<JavaConstructorCall> constructsParsedDomainValue = DescribedPredicate.describe(
			"a constructor of a Parsed* domain value",
			call -> implementsParsedInterface(call.getTargetOwner())
		);
		noClasses().that(RESIDE_IN_INBOUND_MODULE)
			.should().callConstructorWhere(constructsParsedDomainValue)
			.because("untrusted input must go through parse(), see the \"Input validation\" link in README.md")
			.check(importedClasses);
	}

	private static boolean implementsParsedInterface(JavaClass javaClass) {
		return javaClass.getAllRawInterfaces().stream().anyMatch(i -> i.getSimpleName().startsWith("Parsed"));
	}
}
