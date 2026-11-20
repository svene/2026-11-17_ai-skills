#!/usr/bin/env bash
# The item form parses: Item is a Parsed* value, ItemActionResource calls Item.parse and answers invalid input
# with the ItemForm envelope, the hidden check and the
# rule ran, nothing committed.
set -euo pipefail
p=org/acme/shop
for f in core/src/main/java/$p/cross/validation/ConstructorValidation.java \
         app-server/src/test/java/$p/devsupport/ValidationArchitectureTest.java; do
  [ -e "$f" ] || { echo "missing $f"; exit 1; }
done
grep -q 'implements ParsedItem' core/src/main/java/$p/feature/item/Item.java || { echo "Item does not implement ParsedItem"; exit 1; }
r=inbound-http-json-vm/src/main/java/$p/feature/item/ItemActionResource.java
grep -q 'Item.parse(' $r || { echo "ItemActionResource does not call Item.parse"; exit 1; }
grep -q 'UiResponse(UiRouteName.ItemForm' $r || { echo "ItemActionResource does not answer with UiResponse(UiRouteName.ItemForm, …)"; exit 1; }
! grep -rq --include='*.java' 'Integer.parseInt' inbound-http-json-vm/src || { echo "Integer.parseInt left in inbound-http-json-vm"; exit 1; }
! git log --format=%s | grep -qiv '^\(Bare project\|skilltest:\)' || { echo "unexpected commit"; exit 1; }
for t in ItemFormValidationCheckTest ValidationArchitectureTest; do
  grep -q 'Tests run: [1-9].*Failures: 0, Errors: 0, Skipped: 0' app-server/target/surefire-reports/*"$t".txt || { echo "$t did not run cleanly"; exit 1; }
done
