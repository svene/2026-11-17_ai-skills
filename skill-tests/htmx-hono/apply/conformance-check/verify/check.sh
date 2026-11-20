#!/usr/bin/env bash
# The three drifts are fixed: no literal mutation URL, the clear button uses hx-delete like its @DELETE method, and
# that method returns Response (204 + HX-Trigger: hidden test). Nothing committed. (The final reply must name all
# three findings: expect-result.)
set -euo pipefail
m=inbound-http-json-vm j=$m/src/main/java/org/acme/shop/feature/example
! grep -rnE 'hx-(post|put|patch|delete)="[^$]' $m/src/main/java || { echo "literal mutation URL"; exit 1; }
grep -q 'hx-delete="${exampleApi.clear()}"' $j/example-guest-list.ts || { echo "clear button does not use hx-delete with exampleApi.clear()"; exit 1; }
! grep -q 'hx-put' $j/example-guest-list.ts || { echo "hx-put still in the guest list"; exit 1; }
! grep -q 'public void' $j/ExampleActionResource.java || { echo "an action method still returns void"; exit 1; }
! git log --format=%s | grep -qiv '^\(Bare project\|skilltest:\)' || { echo "unexpected commit"; exit 1; }
r=app-server/target/surefire-reports
grep -q 'Tests run: 1, Failures: 0, Errors: 0, Skipped: 0' $r/*ExampleClearCheckTest.txt || { echo "ExampleClearCheckTest did not run cleanly"; exit 1; }
grep -q 'Failures: 0, Errors: 0, Skipped: 0' $r/*ExampleUiTest.txt || { echo "ExampleUiTest did not run cleanly"; exit 1; }
