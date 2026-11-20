#!/usr/bin/env bash
# Structure of a fresh project with htmx-hono; mvn verify (already run) proves codegen, tsc, the bundle and the
# envelopes, the e2e suite (already run) the browser side.
set -euo pipefail

p=org/acme/demoapp m=inbound-http-json-vm
j=$m/src/main/java/$p
for f in $m/pom.xml $m/package.json $m/package-lock.json $m/tsconfig.json \
         $j/cross/htmlrender/{UiResponse,UiRouteName,UiEventName,HtmxHeaders,UiRouteSource,UiRouteReceiver}.java \
         $j/cross/htmlrender/{hx-hono,routes,render,route-types,api}.ts \
         $j/cross/htmlrender/generated/types/{vm-types.d.ts,web-api.ts} \
         $j/cross/devreload/{DevReloadSSE,JsBundleWatcher}.java \
         $j/feature/example/{ExampleUiReceiver,ExampleActionResource}.java \
         $m/src/main/resources/META-INF/resources/{index.html,js/dev-reload.js,js/htmx/4.0.0/htmx.js} \
         $m/target/classes/META-INF/resources/js/hono/hx-hono.js \
         core/src/main/java/$p/feature/example/ExampleGuestbookHandler.java \
         app-server/src/test/java/$p/feature/example/ExampleUiTest.java; do
  [ -e "$f" ] || { echo "missing $f"; exit 1; }
done
grep -qx "    <module>$m</module>" pom.xml || { echo "$m not in the parent's modules"; exit 1; }
awk '/<module>/{print $1}' pom.xml | tr '\n' ' ' | grep -q "<module>$m</module> <module>app-server</module>" \
  || { echo "$m not right before app-server"; exit 1; }
grep -q "<artifactId>$m</artifactId>" app-server/pom.xml || { echo "app-server does not depend on $m"; exit 1; }
! grep -rq '@[A-Z_]*@' pom.xml $m/pom.xml $m/package.json $m/src core/src app-server/src README.md \
  || { echo "unfilled placeholder"; exit 1; }
grep -q '<title>demo-app</title>' $m/src/main/resources/META-INF/resources/index.html || { echo "index.html title not filled"; exit 1; }
for g in "/$m/node/" "node_modules/" "**/cross/htmlrender/generated/"; do
  grep -qxF "$g" .gitignore || { echo ".gitignore lacks $g"; exit 1; }
done
# Generated and installed files stay out of git.
[ -z "$(git status --porcelain -- $m/node $m/node_modules $j/cross/htmlrender/generated)" ] \
  || { echo "generated or installed files not ignored"; exit 1; }
[ "$(grep -c 'hypermedia-quarkus-browser-hono' README.md)" -eq 1 ] || { echo "README needs exactly one htmx + Hono link"; exit 1; }
grep -q 'hypermedia-quarkus-browser-hono/blob/[0-9a-f]\{40\}/architecture.md' README.md
awk '/^## Architecture$/{s=1;next} /^#/{s=0} s' README.md | grep -q 'hypermedia-quarkus-browser-hono' || { echo "link not in ## Architecture"; exit 1; }
# The skill never commits.
! git log --format=%s | grep -qiv '^\(Bare project\|skilltest:\)' || { echo "unexpected commit"; exit 1; }
grep -q 'Tests run: 4, Failures: 0, Errors: 0' app-server/target/surefire-reports/*ExampleUiTest.txt || { echo "ExampleUiTest did not run its 4 tests"; exit 1; }
for t in ArchitectureTest ExampleReceiverTest; do
  grep -q 'Tests run: [1-9].*Failures: 0, Errors: 0' app-server/target/surefire-reports/*"$t".txt || { echo "$t did not run"; exit 1; }
done
