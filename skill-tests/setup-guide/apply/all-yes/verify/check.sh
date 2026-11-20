#!/usr/bin/env bash
# All yes: one commit per step, in chain order, a clean tree, and every Technique's footprint.
# ./mvnw verify and the Playwright suite have already run green.
set -euo pipefail
expected="Bare project shop
Apply triptych architecture
Add Kafka (inbound-kafka)
Apply validation
Add htmx + Hono frontend
Add Playwright browser tests"
actual=$(git log --reverse --format=%s | sed 's/ (Quarkus .*//')
[ "$actual" = "$expected" ] || { printf 'commits differ from the chain:\n%s\n' "$actual"; exit 1; }
[ -z "$(git status --porcelain)" ] || { echo "uncommitted changes:"; git status --short; exit 1; }
p=src/main/java/org/acme/shop
for f in core/$p/cross/validation/ConstructorValidation.java \
         inbound-kafka/$p/feature/example/ExampleMessageReceiver.java \
         inbound-http-json-vm/pom.xml \
         e2e-playwright/tests/guestbook.spec.ts; do
  [ -e "$f" ] || { echo "missing $f"; exit 1; }
done
[ "$(grep -rl '@Incoming' inbound-kafka/src/main/java | wc -l)" -eq 1 ] || { echo "inbound-kafka should hold exactly one consumer (validation's)"; exit 1; }
grep -qE '5 passed' "$RUN_LOG" || { echo "Playwright suite did not report 5 passed"; exit 1; }
