#!/usr/bin/env bash
# The module and its infrastructure exist, the item page follows the technique (route enum, routes.ts entry, API
# client, no literal mutation URL, ItemPage is the start page), the hidden check and an item UI test ran, the
# JSON API is untouched, nothing committed.
set -euo pipefail
m=inbound-http-json-vm p=org/acme/shop
h=$m/src/main/java/$p/cross/htmlrender
for f in $h/hx-hono.ts $h/routes.ts $h/api.ts $h/UiRouteName.java $h/UiRouteReceiver.java \
         $m/src/main/resources/META-INF/resources/index.html; do
  [ -e "$f" ] || { echo "missing $f"; exit 1; }
done
grep -q '^    ItemPage,\?$' $h/UiRouteName.java || { echo "ItemPage not in UiRouteName"; exit 1; }
grep -qE '^\s*ItemPage:' $h/routes.ts || { echo "ItemPage not in routes.ts"; exit 1; }
grep -q 'satisfies Record<UiRouteName, RouteDefinition>' $h/routes.ts || { echo "routes.ts lost satisfies"; exit 1; }
grep -q 'new ItemActionResourceClient(' $h/api.ts || { echo "no ItemActionResourceClient in api.ts"; exit 1; }
grep -q 'implements UiRouteSource' $m/src/main/java/$p/feature/item/*.java || { echo "no item UiRouteSource"; exit 1; }
! grep -rnE 'hx-(post|put|patch|delete)="[^$]' $m/src/main/java || { echo "literal mutation URL"; exit 1; }
grep -q 'hx-get="/uiroute/ItemPage"' $m/src/main/resources/META-INF/resources/index.html || { echo "ItemPage is not the start page"; exit 1; }
grep -q '@Valid' inbound-http-jsonapi/src/main/java/$p/feature/item/ItemReceiver.java || { echo "the JSON API ItemReceiver was changed"; exit 1; }
! git log --format=%s | grep -qiv '^\(Bare project\|skilltest:\)' || { echo "unexpected commit"; exit 1; }
grep -q 'Tests run: 4, Failures: 0, Errors: 0, Skipped: 0' app-server/target/surefire-reports/*ItemUiCheckTest.txt || { echo "ItemUiCheckTest did not run cleanly"; exit 1; }
ls app-server/target/surefire-reports/ | grep -v ItemUiCheckTest | grep -q 'Item.*Ui.*Test.txt' || { echo "no item UI test of the agent's own"; exit 1; }
grep -q 'Failures: 0, Errors: 0' app-server/target/surefire-reports/*ArchitectureTest.txt || { echo "ArchitectureTest did not run cleanly"; exit 1; }
