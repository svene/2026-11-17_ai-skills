package @PACKAGE@.devsupport;

// Provenance: svene/2026-07-26_quarkus-hexagon-demo-mm-per-tech@b2f8493 app-server/.../devsupport/TriptychArchitectureConfig.java (seswdevskills triptych skill; @RecordBuilder replaced by hand-written withers); owned by this project from here on.

import java.util.Optional;

/**
 * Configuration for {@link TriptychArchitecture}: the root package, the feature/cross segment names,
 * and the four naming suffixes.
 */
public record TriptychArchitectureConfig(
	String rootPackage,
	String featureSegment,
	String crossSegment,
	String spiSuffix,
	String handlerSuffix,
	String receiverSuffix,
	String serviceSuffix,
	Optional<Boolean> allowEmptyShould,
	Optional<String> overriddenDescription
) {

	public static TriptychArchitectureConfig defaultsFor(String rootPackage) {
		return new TriptychArchitectureConfig(rootPackage, "feature", "cross",
			"SPI", "Handler", "Receiver", "Service", Optional.empty(), Optional.empty());
	}

	public TriptychArchitectureConfig withAllowEmptyShould(Optional<Boolean> value) {
		return new TriptychArchitectureConfig(rootPackage, featureSegment, crossSegment,
			spiSuffix, handlerSuffix, receiverSuffix, serviceSuffix, value, overriddenDescription);
	}

	public TriptychArchitectureConfig withOverriddenDescription(Optional<String> value) {
		return new TriptychArchitectureConfig(rootPackage, featureSegment, crossSegment,
			spiSuffix, handlerSuffix, receiverSuffix, serviceSuffix, allowEmptyShould, value);
	}
}
