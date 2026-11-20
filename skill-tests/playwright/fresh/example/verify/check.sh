#!/usr/bin/env bash
# A fresh Triptych project with Playwright: the example spec only.
set -euo pipefail
"$(dirname "$0")/../../../common-check.sh"
[ "$(ls e2e-playwright/tests)" = example.spec.ts ] || { echo "expected only example.spec.ts: $(ls e2e-playwright/tests)"; exit 1; }
grep -q '/api/example`;' e2e-playwright/server.ts || { echo "readiness URL is not /api/example"; exit 1; }
grep -q "1 passed" "$RUN_LOG" || { echo "npm test did not pass 1 test"; exit 1; }
