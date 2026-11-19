#!/usr/bin/env bash
# Structure of a fresh Triptych project; mvn verify (already run) proves the rules, the HTTP slice and the boot.
set -euo pipefail

p=org/acme/demoapp
for f in core/pom.xml inbound-http-jsonapi/pom.xml \
         core/src/main/java/$p/feature/example/ExampleHandler.java \
         inbound-http-jsonapi/src/main/java/$p/feature/example/ExampleReceiver.java \
         app-server/src/test/java/$p/feature/example/ExampleReceiverTest.java \
         app-server/src/test/java/$p/devsupport/TriptychArchitecture.java \
         app-server/src/test/java/$p/devsupport/TriptychArchitectureConfig.java \
         app-server/src/test/java/$p/devsupport/ArchitectureTest.java \
         app-server/src/test/resources/archunit.properties; do
  [ -e "$f" ] || { echo "missing $f"; exit 1; }
done
grep -q '^archRule.failOnEmptyShould=true$' app-server/src/test/resources/archunit.properties
[ "$(grep -c '<module>' pom.xml)" -eq 3 ] || { echo "expected modules core, inbound-http-jsonapi, app-server"; exit 1; }
grep -q 'jandex-maven-plugin' pom.xml || { echo "no jandex plugin"; exit 1; }
! grep -rq '@[A-Z_]*@' pom.xml core inbound-http-jsonapi app-server/src README.md || { echo "unfilled placeholder"; exit 1; }
grep -q '^## Architecture$' README.md || { echo "README has no Architecture section"; exit 1; }
grep -q 'quarkus-hexagon-demo-mm-per-tech/blob/[0-9a-f]\{40\}/concepts.md' README.md || { echo "README lacks the concepts.md permalink"; exit 1; }
grep -q 'triptych-architecture-demo-sm/blob/[0-9a-f]\{40\}/readme.md#L7-L11' README.md || { echo "README lacks the naming permalink"; exit 1; }
# The skill never commits: only the Bare commit and the runner's own snapshots.
! git log --format=%s | grep -qiv '^\(Bare project\|skilltest:\)' || { echo "unexpected commit"; exit 1; }
for t in ArchitectureTest ExampleReceiverTest; do
  grep -q 'Tests run: [1-9].*Failures: 0, Errors: 0' app-server/target/surefire-reports/*"$t".txt || { echo "$t did not run"; exit 1; }
done
