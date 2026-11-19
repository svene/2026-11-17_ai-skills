package @PACKAGE@.devsupport;

// Provenance: svene/2026-07-26_quarkus-hexagon-demo-mm-per-tech@b2f8493 app-server/.../devsupport/TriptychArchitecture.java (seswdevskills triptych skill); owned by this project from here on.

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;
import com.tngtech.archunit.lang.Priority;

import java.util.List;
import java.util.Optional;

/**
 * Asserts the "Triptych" architecture: a hexagonal core sliced by <b>feature</b> (one restricted package per
 * feature, e.g. {@code feature.order}) and <b>cross</b> (cross-cutting concerns reachable from every feature,
 * e.g. {@code cross.auditlog}), with SPI/Handler/Receiver/Service naming determining where a class must live.
 * Mirrors the usage style of ArchUnit's own {@link com.tngtech.archunit.library.Architectures#onionArchitecture()}.
 * <pre>
 * triptychArchitecture("org.acme.app").check(importedClasses);
 * </pre>
 * Checks two structural guarantees:
 * <ol>
 *   <li>No two feature slices depend on each other - cross-cutting classes are exempt by construction,
 *       since they never match the feature slice pattern in the first place.</li>
 *   <li>{@code *SPI}s are public interfaces, and every {@code *SPI}/{@code *Handler}/{@code *Receiver}/
 *       {@code *Service} class resides under a feature or cross package - nothing is left outside the
 *       scheme.</li>
 * </ol>
 * The two SPI rules allow an empty match: a project without any outbound port yet is valid. Every other rule
 * keeps {@code archRule.failOnEmptyShould=true}, so an import that sees no classes still fails.
 * <p>
 * The root package, the feature/cross segment names, and the four naming suffixes all come from a
 * {@link TriptychArchitectureConfig}, defaulting to the Triptych convention via
 * {@link TriptychArchitectureConfig#defaultsFor(String)}; pass a customized config to
 * {@link #triptychArchitecture(TriptychArchitectureConfig)} for a non-default shape.
 */
public final class TriptychArchitecture implements ArchRule {

	private final TriptychArchitectureConfig config;

	private TriptychArchitecture(TriptychArchitectureConfig config) {
		this.config = config;
	}

	public static TriptychArchitecture triptychArchitecture(String rootPackage) {
		return triptychArchitecture(TriptychArchitectureConfig.defaultsFor(rootPackage));
	}

	/**
	 * For a non-default shape: build a {@link TriptychArchitectureConfig} (e.g. a copy of
	 * {@link TriptychArchitectureConfig#defaultsFor(String)} with other values) and pass it here.
	 */
	public static TriptychArchitecture triptychArchitecture(TriptychArchitectureConfig config) {
		return new TriptychArchitecture(config);
	}

	private String featureBucketPackage() {
		return config.rootPackage() + "." + config.featureSegment() + "..";
	}

	private String featureSlicePackage() {
		return config.rootPackage() + "." + config.featureSegment() + ".(*)..";
	}

	private String crossBucketPackage() {
		return config.rootPackage() + "." + config.crossSegment() + "..";
	}

	private List<ArchRule> rules() {
		List<ArchRule> rules = List.of(
			classes().that().haveNameMatching(".*" + config.spiSuffix()).should().beInterfaces().allowEmptyShould(true),
			classes().that().haveNameMatching(".*" + config.spiSuffix()).should().bePublic().allowEmptyShould(true),
			classes().that().haveNameMatching(".*(" + config.spiSuffix() + "|"
					+ config.handlerSuffix() + "|" + config.receiverSuffix() + "|" + config.serviceSuffix() + ")")
				.should().resideInAnyPackage(featureBucketPackage(), crossBucketPackage()),
			slices().matching(featureSlicePackage()).should().notDependOnEachOther()
		);
		return config.allowEmptyShould().map(allow -> rules.stream().map(rule -> rule.allowEmptyShould(allow)).toList())
			.orElse(rules);
	}

	@Override
	public void check(JavaClasses classes) {
		Assertions.check(this, classes);
	}

	@Override
	public EvaluationResult evaluate(JavaClasses classes) {
		EvaluationResult result = new EvaluationResult(this, Priority.MEDIUM);
		for (ArchRule rule : rules()) {
			result.add(rule.evaluate(classes));
		}
		return result;
	}

	@Override
	public TriptychArchitecture because(String reason) {
		return as(getDescription() + ", because " + reason);
	}

	@Override
	public TriptychArchitecture allowEmptyShould(boolean allowEmptyShould) {
		return new TriptychArchitecture(config.withAllowEmptyShould(Optional.of(allowEmptyShould)));
	}

	@Override
	public TriptychArchitecture as(String newDescription) {
		return new TriptychArchitecture(config.withOverriddenDescription(Optional.of(newDescription)));
	}

	@Override
	public String getDescription() {
		return config.overriddenDescription().orElseGet(() -> "Triptych architecture rooted at '" + config.rootPackage() + "': "
			+ "no two '" + config.featureSegment() + ".*' slices depend on each other; "
			+ "'*" + config.spiSuffix() + "' are public interfaces; "
			+ "every '*" + config.spiSuffix() + "|*" + config.handlerSuffix()
			+ "|*" + config.receiverSuffix() + "|*" + config.serviceSuffix() + "' class "
			+ "resides under '" + config.featureSegment() + "' or '" + config.crossSegment() + "'");
	}

	@Override
	public String toString() {
		return getDescription();
	}
}
