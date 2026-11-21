#!/usr/bin/env bash
# Both drifts are fixed: the deleted guestbook spec is restored and retries is back to 0.
# All 4 tests green in npm test (already run). Nothing committed. (The final reply must name both findings:
# expect-result.)
set -euo pipefail
"$(dirname "$0")/../../../common-check.sh"
[ -e e2e-playwright/tests/guestbook.spec.ts ] || { echo "missing guestbook.spec.ts"; exit 1; }
! grep -q 'retries: process.env.CI' e2e-playwright/playwright.config.ts || { echo "retries on CI still set"; exit 1; }
grep -q '4 passed' "$RUN_LOG" || { echo "npm test did not pass 4 tests"; exit 1; }
