#!/usr/bin/env bash
# The migrated shop: Triptych modules, rules active and green, no leftovers, no commits.
set -euo pipefail

p=org/acme/shop
for f in core/pom.xml inbound-http-jsonapi/pom.xml \
         app-server/src/test/java/$p/devsupport/ArchitectureTest.java \
         app-server/src/test/resources/archunit.properties; do
  [ -e "$f" ] || { echo "missing $f"; exit 1; }
done
grep -q '<module>core</module>' pom.xml || { echo "core not in parent modules"; exit 1; }
grep -q '<module>inbound-http-jsonapi</module>' pom.xml || { echo "inbound-http-jsonapi not in parent modules"; exit 1; }
ls core/src/main/java/$p/feature/*/*Handler.java >/dev/null 2>&1 || { echo "no Handler under core feature.*"; exit 1; }
ls inbound-http-jsonapi/src/main/java/$p/feature/*/*Receiver.java >/dev/null 2>&1 || { echo "no Receiver under inbound-http-jsonapi feature.*"; exit 1; }
if [ -d app-server/src/main/java ] && [ -n "$(find app-server/src/main/java -name '*.java')" ]; then
  echo "app-server still has main sources"; exit 1
fi
! grep -rq '@Disabled' app-server/src/test/java/$p/devsupport || { echo "architecture test still @Disabled"; exit 1; }
grep -q 'jandex-maven-plugin' pom.xml || { echo "no jandex plugin"; exit 1; }
grep -q '^## Architecture$' README.md || { echo "README has no Architecture section"; exit 1; }
! git log --format=%s | grep -qiv '^\(Bare project\|skilltest:\)' || { echo "unexpected commit"; exit 1; }
for t in ArchitectureTest ShopBehaviourCheckTest; do
  grep -q 'Tests run: [1-9].*Failures: 0, Errors: 0, Skipped: 0' app-server/target/surefire-reports/*"$t".txt || { echo "$t did not run cleanly"; exit 1; }
done
