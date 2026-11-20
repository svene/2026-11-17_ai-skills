#!/usr/bin/env bash
# The Kafka consumer of a fresh project with validation; mvn verify (already run, Dev Services broker) proves
# invalid -> skipped, undeserializable and tombstone -> dead-letter queue, and the channel keeps consuming.
set -euo pipefail

p=org/acme/demoapp
k=inbound-kafka/src/main/java/$p
for f in $k/feature/example/ExampleMessage.java \
         $k/feature/example/ExampleMessageDeserializer.java \
         $k/feature/example/ExampleMessageReceiver.java \
         $k/cross/messaging/UnprocessableMessageException.java \
         app-server/src/test/java/$p/feature/example/ExampleMessageReceiverTest.java; do
  [ -e "$f" ] || { echo "missing $f"; exit 1; }
done
r=$k/feature/example/ExampleMessageReceiver.java
grep -q 'Example.parse(' $r || { echo "consumer does not parse"; exit 1; }
grep -q 'abortOn = UnprocessableMessageException.class' $r || { echo "@Retry does not abort on unprocessable"; exit 1; }
grep -q 'EXAMPLE_RECEIVED' $r && grep -q 'EXAMPLE_INVALID' $r || { echo "consumer logging conventions"; exit 1; }
for a in quarkus-messaging-kafka quarkus-jackson quarkus-smallrye-fault-tolerance; do
  grep -q "<artifactId>$a</artifactId>" inbound-kafka/pom.xml || { echo "inbound-kafka lacks $a"; exit 1; }
done
props=app-server/src/main/resources/application.properties
grep -q '^mp.messaging.incoming.example-in.failure-strategy=dead-letter-queue$' $props || { echo "no dead-letter-queue strategy"; exit 1; }
grep -q '^mp.messaging.incoming.example-in.value.deserializer=org.acme.demoapp.feature.example.ExampleMessageDeserializer$' $props
! grep -rq '@[A-Z_]*@' inbound-kafka app-server/src || { echo "unfilled placeholder"; exit 1; }
! git log --format=%s | grep -qiv '^\(Bare project\|skilltest:\)' || { echo "unexpected commit"; exit 1; }
for t in ExampleMessageReceiverTest ExampleReceiverTest ValidationArchitectureTest ArchitectureTest; do
  grep -q 'Tests run: [1-9].*Failures: 0, Errors: 0' app-server/target/surefire-reports/*"$t".txt || { echo "$t did not run"; exit 1; }
done
