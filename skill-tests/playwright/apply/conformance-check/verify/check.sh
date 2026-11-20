#!/usr/bin/env bash
# Both drifts are fixed: the guestbook spec is added (htmx-hono came after Playwright) and retries is back to 0.
# All 5 tests green in npm test (already run). Nothing committed. (The final reply must name both findings:
# expect-result.)
set -euo pipefail
"$(dirname "$0")/../../../common-check.sh"
[ -e e2e-playwright/tests/guestbook.spec.ts ] || { echo "missing guestbook.spec.ts"; exit 1; }
! grep -q 'retries: process.env.CI' e2e-playwright/playwright.config.ts || { echo "retries on CI still set"; exit 1; }
grep -q '5 passed' "$RUN_LOG" || { echo "npm test did not pass 5 tests"; exit 1; }
