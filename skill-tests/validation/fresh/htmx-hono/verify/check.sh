#!/usr/bin/env bash
# Validation after htmx-hono: the guestbook form parses into ExampleGuest. mvn verify (already run) proves the rule,
# ExampleGuestTest and ExampleUiTest's 204/400.
set -euo pipefail

p=org/acme/demoapp/feature/example
for f in core/src/main/java/$p/ExampleGuest.java \
         core/src/main/java/$p/ParsedExampleGuest.java \
         core/src/test/java/$p/ExampleGuestTest.java \
         core/src/main/java/$p/Example.java; do
  [ -e "$f" ] || { echo "missing $f"; exit 1; }
done
r=inbound-http-json-vm/src/main/java/$p/ExampleActionResource.java
grep -q 'ExampleGuest.parse(' "$r" || { echo "ExampleActionResource does not parse"; exit 1; }
grep -q 'EXAMPLE_GUEST_RECEIVED' "$r"
grep -q 'EXAMPLE_GUEST_INVALID' "$r"
grep -q 'public void sign(ExampleGuest guest)' core/src/main/java/$p/ExampleGuestbookHandler.java \
  || { echo "the handler still takes a String"; exit 1; }
grep -q 'Example.parse(' inbound-http-jsonapi/src/main/java/$p/ExampleReceiver.java || { echo "receiver does not parse"; exit 1; }
! grep -rq --include='*.java' '@[A-Z_]*@' core inbound-http-json-vm/src app-server/src || { echo "unfilled placeholder"; exit 1; }
! git log --format=%s | grep -qiv '^\(Bare project\|skilltest:\)' || { echo "unexpected commit"; exit 1; }
grep -q 'Tests run: [1-9].*Failures: 0, Errors: 0' core/target/surefire-reports/*ExampleGuestTest.txt || { echo "ExampleGuestTest did not run"; exit 1; }
for t in ValidationArchitectureTest ExampleUiTest ExampleReceiverTest; do
  grep -q 'Tests run: [1-9].*Failures: 0, Errors: 0' app-server/target/surefire-reports/*"$t".txt || { echo "$t did not run"; exit 1; }
done
