#!/usr/bin/env bash
# Structure of a fresh Bare project, then: the packaged app-server jar starts (no HTTP yet,
# so wait for Quarkus's startup log line).
set -euo pipefail

for f in mvnw .mvn/wrapper/maven-wrapper.properties .gitignore .sdkmanrc README.md \
         app-server/src/test/java/org/acme/demoapp/AppServerBootTest.java; do
  [ -e "$f" ] || { echo "missing $f"; exit 1; }
done
grep -q '<module>app-server</module>' pom.xml
[ "$(grep -c '<module>' pom.xml)" -eq 1 ] || { echo "parent must list only app-server"; exit 1; }
[ ! -d app-server/src/main/java ] || { echo "app-server must have no main sources"; exit 1; }
git log --format=%s | grep -q '^Bare project demo-app' || { echo "no initial Bare project commit"; exit 1; }

jar=app-server/target/quarkus-app/quarkus-run.jar
out=$(mktemp)
java -jar "$jar" >"$out" 2>&1 & pid=$!
trap 'kill $pid 2>/dev/null || true; rm -f "$out"' EXIT
for _ in $(seq 1 60); do
  if grep -q 'started in' "$out"; then echo "app-server started"; exit 0; fi
  kill -0 $pid 2>/dev/null || break
  sleep 0.5
done
cat "$out"; echo "app-server did not start within 30s"; exit 1
