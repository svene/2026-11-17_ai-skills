#!/usr/bin/env bash
# The conformance check restored the rules and the README section, and committed nothing.
set -euo pipefail

p=org/acme/shop
for f in app-server/src/test/java/$p/devsupport/TriptychArchitecture.java \
         app-server/src/test/java/$p/devsupport/TriptychArchitectureConfig.java \
         app-server/src/test/java/$p/devsupport/ArchitectureTest.java \
         app-server/src/test/resources/archunit.properties; do
  [ -e "$f" ] || { echo "missing $f"; exit 1; }
done
grep -q '^archRule.failOnEmptyShould=true$' app-server/src/test/resources/archunit.properties
! grep -rq '@Disabled' app-server/src/test/java/$p/devsupport || { echo "architecture test @Disabled"; exit 1; }
grep -q '^## Architecture$' README.md || { echo "README has no Architecture section"; exit 1; }
grep -q 'quarkus-hexagon-demo-mm-per-tech/blob/[0-9a-f]\{40\}/concepts.md' README.md || { echo "README lacks the concepts.md permalink"; exit 1; }
grep -q 'triptych-architecture-demo-sm/blob/[0-9a-f]\{40\}/readme.md#L7-L11' README.md || { echo "README lacks the naming permalink"; exit 1; }
! git log --format=%s | grep -qiv '^\(Bare project\|skilltest:\)' || { echo "unexpected commit"; exit 1; }
grep -q 'Tests run: [1-9].*Failures: 0, Errors: 0, Skipped: 0' app-server/target/surefire-reports/*ArchitectureTest.txt || { echo "ArchitectureTest did not run cleanly"; exit 1; }
