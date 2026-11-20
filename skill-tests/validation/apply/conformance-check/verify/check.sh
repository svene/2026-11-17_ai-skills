#!/usr/bin/env bash
# The rule is back and green, the sample receiver no longer builds Example with its constructor, nothing
# committed. (The final reply must name both findings: expect-result.)
set -euo pipefail
p=org/acme/shop
f=app-server/src/test/java/$p/devsupport/ValidationArchitectureTest.java
[ -e "$f" ] || { echo "missing $f"; exit 1; }
grep -q 'inbound_adapters_construct_domain_values_only_via_parse' "$f" || { echo "rule missing from ValidationArchitectureTest"; exit 1; }
! grep -q '@Disabled' "$f" || { echo "ValidationArchitectureTest is @Disabled"; exit 1; }
! grep -rq 'new Example(' inbound-http-jsonapi/src/main || { echo "inbound still calls new Example("; exit 1; }
! git log --format=%s | grep -qiv '^\(Bare project\|skilltest:\)' || { echo "unexpected commit"; exit 1; }
grep -q 'Tests run: [1-9].*Failures: 0, Errors: 0, Skipped: 0' app-server/target/surefire-reports/*ValidationArchitectureTest.txt || { echo "ValidationArchitectureTest did not run cleanly"; exit 1; }
