#!/usr/bin/env bash
# Shared by every case's check.sh: the structure of e2e-playwright/. verify (already run) proved npm test green.
set -euo pipefail

m=e2e-playwright
for f in package.json package-lock.json tsconfig.json playwright.config.ts server.ts global-setup.ts .gitignore; do
  [ -e "$m/$f" ] || { echo "missing $m/$f"; exit 1; }
done
grep -q '"@playwright/test": "[0-9][0-9.]*"' $m/package.json || { echo "@playwright/test not pinned exactly"; exit 1; }
grep -q 'retries: 0' $m/playwright.config.ts || { echo "retries is not 0"; exit 1; }
! grep -rq '@[A-Z_]*@' $m --include='*.ts' --exclude-dir=node_modules || { echo "unfilled placeholder"; exit 1; }
grep -q 'DEV_PORT = 8080;' $m/server.ts || { echo "dev port not 8080"; exit 1; }
[ -z "$(git status --porcelain -- $m/node_modules $m/test-results $m/playwright-report)" ] \
  || { echo "installed or report files not ignored"; exit 1; }
[ "$(grep -c 'Playwright end-to-end tests' README.md)" -eq 1 ] || { echo "README needs exactly one Playwright link"; exit 1; }
awk '/^## Architecture$/{s=1;next} /^#/{s=0} s' README.md | grep -q 'hypermedia-quarkus-browser-hono/blob/[0-9a-f]\{40\}/development.md#end-to-end-tests' \
  || { echo "Playwright link not in ## Architecture or not a permalink"; exit 1; }
# The skill never commits.
! git log --format=%s | grep -qiv '^\(Bare project\|skilltest:\)' || { echo "unexpected commit"; exit 1; }
