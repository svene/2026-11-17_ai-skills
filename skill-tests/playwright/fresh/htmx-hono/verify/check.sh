#!/usr/bin/env bash
# Fresh + htmx-hono + Playwright: the guestbook spec only, green in npm test (already run); readiness on the page route.
set -euo pipefail
"$(dirname "$0")/../../../common-check.sh"
[ "$(ls e2e-playwright/tests | tr '\n' ' ')" = "guestbook.spec.ts " ] \
  || { echo "expected guestbook.spec.ts only: $(ls e2e-playwright/tests)"; exit 1; }
grep -q '/uiroute/ExamplePage`;' e2e-playwright/server.ts || { echo "readiness URL is not /uiroute/ExamplePage"; exit 1; }
grep -q '4 passed' "$RUN_LOG" || { echo "npm test did not pass 4 tests"; exit 1; }
