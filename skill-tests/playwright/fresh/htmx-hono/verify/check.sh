#!/usr/bin/env bash
# Fresh + htmx-hono + Playwright: the example and the guestbook spec, both green in npm test (already run).
set -euo pipefail
"$(dirname "$0")/../../../common-check.sh"
[ "$(ls e2e-playwright/tests | tr '\n' ' ')" = "example.spec.ts guestbook.spec.ts " ] \
  || { echo "expected example.spec.ts and guestbook.spec.ts: $(ls e2e-playwright/tests)"; exit 1; }
grep -q '5 passed' "$RUN_LOG" || { echo "npm test did not pass 5 tests"; exit 1; }
