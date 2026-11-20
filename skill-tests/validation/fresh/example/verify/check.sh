#!/usr/bin/env bash
# Structure of a fresh project with validation; mvn verify (already run) proves the rule, parse() and the 201/400.
set -euo pipefail

p=org/acme/demoapp
for f in core/src/main/java/$p/cross/validation/ConstructorValidation.java \
         core/src/main/java/$p/feature/example/Example.java \
         core/src/main/java/$p/feature/example/ParsedExample.java \
         core/src/test/java/$p/feature/example/ExampleTest.java \
         app-server/src/test/java/$p/devsupport/ValidationArchitectureTest.java; do
  [ -e "$f" ] || { echo "missing $f"; exit 1; }
done
grep -q '<artifactId>quarkus-hibernate-validator</artifactId>' core/pom.xml || { echo "core lacks quarkus-hibernate-validator"; exit 1; }
grep -q 'sealed interface ParsedExample permits Example, ParsedExample.Invalid' core/src/main/java/$p/feature/example/ParsedExample.java
grep -q 'Example.parse(' inbound-http-jsonapi/src/main/java/$p/feature/example/ExampleReceiver.java || { echo "receiver does not parse"; exit 1; }
! grep -rEq '^[[:space:]]*@Valid\b|[(,][[:space:]]*@Valid\b' inbound-http-jsonapi/src || { echo "@Valid at the boundary"; exit 1; }
grep -q 'EXAMPLE_RECEIVED' inbound-http-jsonapi/src/main/java/$p/feature/example/ExampleReceiver.java
grep -q 'EXAMPLE_PROCESSING' core/src/main/java/$p/feature/example/ExampleHandler.java
[ ! -e inbound-kafka ] || { echo "unexpected inbound-kafka"; exit 1; }
! grep -rq '@[A-Z_]*@' pom.xml core inbound-http-jsonapi app-server/src README.md || { echo "unfilled placeholder"; exit 1; }
[ "$(grep -c 'docs/architecture/validation.md' README.md)" -eq 1 ] || { echo "README needs exactly one validation.md link"; exit 1; }
grep -q 'quarkus-hexagon-demo-mm-per-tech/blob/[0-9a-f]\{40\}/docs/architecture/validation.md' README.md
# The link sits in the Architecture section, after the Triptych links.
awk '/^## Architecture$/{s=1;next} /^#/{s=0} s' README.md | grep -q 'validation.md' || { echo "link not in ## Architecture"; exit 1; }
# The skill never commits.
! git log --format=%s | grep -qiv '^\(Bare project\|skilltest:\)' || { echo "unexpected commit"; exit 1; }
grep -q 'Tests run: [1-9].*Failures: 0, Errors: 0' core/target/surefire-reports/*ExampleTest.txt || { echo "ExampleTest did not run"; exit 1; }
for t in ValidationArchitectureTest ExampleReceiverTest ArchitectureTest; do
  grep -q 'Tests run: [1-9].*Failures: 0, Errors: 0' app-server/target/surefire-reports/*"$t".txt || { echo "$t did not run"; exit 1; }
done
