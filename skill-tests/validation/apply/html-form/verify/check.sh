#!/usr/bin/env bash
# The item form parses: Item is a Parsed* value, the form Receiver calls Item.parse, the hidden check and the
# rule ran, nothing committed.
set -euo pipefail
p=org/acme/shop
for f in core/src/main/java/$p/cross/validation/ConstructorValidation.java \
         app-server/src/test/java/$p/devsupport/ValidationArchitectureTest.java; do
  [ -e "$f" ] || { echo "missing $f"; exit 1; }
done
grep -q 'implements ParsedItem' core/src/main/java/$p/feature/item/Item.java || { echo "Item does not implement ParsedItem"; exit 1; }
grep -q 'Item.parse(' inbound-http-json-vm/src/main/java/$p/feature/item/ItemFormReceiver.java || { echo "ItemFormReceiver does not call Item.parse"; exit 1; }
! grep -rq 'parseInt' inbound-http-json-vm/src || { echo "Integer.parseInt left in inbound-http-json-vm"; exit 1; }
! git log --format=%s | grep -qiv '^\(Bare project\|skilltest:\)' || { echo "unexpected commit"; exit 1; }
for t in ItemFormValidationCheckTest ValidationArchitectureTest; do
  grep -q 'Tests run: [1-9].*Failures: 0, Errors: 0, Skipped: 0' app-server/target/surefire-reports/*"$t".txt || { echo "$t did not run cleanly"; exit 1; }
done
