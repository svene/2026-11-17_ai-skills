#!/usr/bin/env bash
# The item slice parses instead of validating: infrastructure present, Item is a Parsed* value, no @Valid left
# in the inbound module, the hidden check and the rule ran, nothing committed.
set -euo pipefail
p=org/acme/shop
for f in core/src/main/java/$p/cross/validation/ConstructorValidation.java \
         app-server/src/test/java/$p/devsupport/ValidationArchitectureTest.java; do
  [ -e "$f" ] || { echo "missing $f"; exit 1; }
done
grep -q 'implements ParsedItem' core/src/main/java/$p/feature/item/Item.java || { echo "Item does not implement ParsedItem"; exit 1; }
grep -q 'Item.parse(' inbound-http-jsonapi/src/main/java/$p/feature/item/ItemReceiver.java || { echo "ItemReceiver does not call Item.parse"; exit 1; }
! grep -rEq '^[[:space:]]*@Valid\b|[(,][[:space:]]*@Valid\b' inbound-http-jsonapi/src || { echo "@Valid left in inbound-http-jsonapi"; exit 1; }
! git log --format=%s | grep -qiv '^\(Bare project\|skilltest:\)' || { echo "unexpected commit"; exit 1; }
for t in ItemValidationCheckTest ValidationArchitectureTest; do
  grep -q 'Tests run: [1-9].*Failures: 0, Errors: 0, Skipped: 0' app-server/target/surefire-reports/*"$t".txt || { echo "$t did not run cleanly"; exit 1; }
done
