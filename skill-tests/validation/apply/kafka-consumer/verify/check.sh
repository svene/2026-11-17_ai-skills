#!/usr/bin/env bash
# The orders consumer parses: Order is a Parsed* value, the receiver logs the invalid case at WARN, the channel
# has a dead-letter topic, the hidden check and the rule ran, nothing committed.
set -euo pipefail
p=org/acme/shop
r=inbound-kafka/src/main/java/$p/feature/order/OrderMessageReceiver.java
for f in core/src/main/java/$p/cross/validation/ConstructorValidation.java \
         app-server/src/test/java/$p/devsupport/ValidationArchitectureTest.java "$r"; do
  [ -e "$f" ] || { echo "missing $f"; exit 1; }
done
grep -q 'implements ParsedOrder' core/src/main/java/$p/feature/order/Order.java || { echo "Order does not implement ParsedOrder"; exit 1; }
grep -q 'Order.parse(' "$r" || { echo "OrderMessageReceiver does not call Order.parse"; exit 1; }
grep -Eq 'warn.*_INVALID' "$r" || { echo "no <OP>_INVALID warning in OrderMessageReceiver"; exit 1; }
props=app-server/src/main/resources/application.properties
grep -q 'failure-strategy=dead-letter-queue' $props || { echo "no dead-letter-queue strategy"; exit 1; }
grep -q 'dead-letter-queue.topic=orders-dlq' $props || { echo "dead-letter topic is not orders-dlq"; exit 1; }
! git log --format=%s | grep -qiv '^\(Bare project\|skilltest:\)' || { echo "unexpected commit"; exit 1; }
for t in OrderConsumerCheckTest ValidationArchitectureTest; do
  grep -q 'Tests run: [1-9].*Failures: 0, Errors: 0, Skipped: 0' app-server/target/surefire-reports/*"$t".txt || { echo "$t did not run cleanly"; exit 1; }
done
