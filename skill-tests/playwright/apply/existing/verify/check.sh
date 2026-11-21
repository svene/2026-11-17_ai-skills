#!/usr/bin/env bash
# Existing project: e2e-playwright/ via --existing (smoke spec on /items, also the readiness URL), plus an item spec
# of the agent's own that names its data uniquely; all green in npm test (already run). Nothing committed.
set -euo pipefail
"$(dirname "$0")/../../../common-check.sh"
t=e2e-playwright/tests
[ -e $t/smoke.spec.ts ] || { echo "missing $t/smoke.spec.ts"; exit 1; }
grep -q '"/items"' $t/smoke.spec.ts || { echo "smoke spec does not visit /items"; exit 1; }
grep -q '/items`;' e2e-playwright/server.ts || { echo "readiness URL is not /items"; exit 1; }
[ ! -e $t/guestbook.spec.ts ] || { echo "guestbook.spec.ts in an existing project"; exit 1; }
specs=$(grep -l '/items' $t/*.spec.ts | grep -v smoke.spec.ts || true)
[ -n "$specs" ] || { echo "no item spec of the agent's own"; exit 1; }
grep -qE 'Date\.now|randomUUID|Math\.random|testInfo|workerIndex' $specs || { echo "item spec does not name its data uniquely"; exit 1; }
grep -qE '[0-9]+ passed' "$RUN_LOG" || { echo "npm test passed nothing"; exit 1; }
! grep -qE '[0-9]+ failed' "$RUN_LOG" || { echo "npm test failed"; exit 1; }
