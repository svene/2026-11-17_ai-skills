#!/usr/bin/env bash
# run.sh <project-dir> [--package <p>]
#
# Fresh mode: a Triptych project that holds only triptych's example slice gets the validation technique.
# Runs add-infrastructure.sh, then turns feature.example into the reference for "parse, don't validate":
#   - core: Example(@NotBlank name, @Positive count) + ParsedExample, ExampleHandler.accept, ExampleTest
#   - inbound-http-jsonapi: POST /api/example -> 201, or 400 with [{field, message}]
#   - inbound-kafka (only if the module exists): ExampleMessageReceiver on topic "example", invalid -> WARN
#     and skip, unprocessable -> dead-letter queue "example-dlq"; plus a Dev Services test (needs Docker)
#   - inbound-http-json-vm (only if htmx-hono's guestbook exists): ExampleGuest + ParsedExampleGuest in core,
#     ExampleGuestbookHandler.sign(ExampleGuest), ExampleActionResource parses the form -> 204, or 400 with errors
#   - app-server: ExampleReceiverTest covers 201 and 400
# Never commits.
#   --package  default: the package of app-server's AppServerBootTest
set -euo pipefail
source "$(dirname "$0")/lib.sh"

[ $# -ge 1 ] && [ "${1:0:1}" != - ] || die "usage: run.sh <project-dir> [--package <p>]"
dir=$1; shift
args=("$@") package=""
while [ $# -gt 0 ]; do
  case $1 in
    --package) package=${2:?--package needs a value}; shift ;;
    *) die "unknown argument $1" ;;
  esac
  shift
done

require_triptych "$dir"
dir=$(cd "$dir" && pwd)
[ -n "$package" ] || package=$(detect_package "$dir")
check_package "$package"
pkg_path=${package//.//}

# Only fresh: triptych's example slice and nothing else. Anything else is the skill's existing or
# conformance branch, done by hand.
example=$pkg_path/feature/example
[ -f "$dir/core/src/main/java/$example/ExampleHandler.java" ] \
  && [ -f "$dir/inbound-http-jsonapi/src/main/java/$example/ExampleReceiver.java" ] \
  || die "no triptych example slice (ExampleHandler, ExampleReceiver): not a fresh Triptych project"
features=$(for d in "$dir"/*/src/main/java/"$pkg_path"/feature/*/; do [ -d "$d" ] && basename "$d"; done | sort -u)
[ "$features" = example ] || die "feature slices other than example exist ($(echo $features)): not a fresh Triptych project"
! grep -rq --include='*.java' 'ConstructorValidation' "$dir"/*/src 2>/dev/null \
  || die "ConstructorValidation already exists: the project uses the technique, run the conformance check"
[ ! -e "$dir/core/src/main/java/$example/Example.java" ] || die "$example/Example.java already exists"

"$(dirname "$0")/add-infrastructure.sh" "$dir" "${args[@]}"

put core/Example.java "core/src/main/java/$example/Example.java"
put core/ParsedExample.java "core/src/main/java/$example/ParsedExample.java"
put core/ExampleHandler.java "core/src/main/java/$example/ExampleHandler.java" force
put core/ExampleTest.java "core/src/test/java/$example/ExampleTest.java"
add_dep "$dir/core/pom.xml" org.junit.jupiter junit-jupiter test
add_dep "$dir/core/pom.xml" org.assertj assertj-core test

put inbound-http-jsonapi/ExampleReceiver.java "inbound-http-jsonapi/src/main/java/$example/ExampleReceiver.java" force
put app-server/ExampleReceiverTest.java "app-server/src/test/java/$example/ExampleReceiverTest.java" force

kafka=""
if [ -f "$dir/inbound-kafka/pom.xml" ]; then
  kafka=", inbound-kafka ExampleMessageReceiver"
  for f in ExampleMessage ExampleMessageDeserializer ExampleMessageReceiver; do
    put "inbound-kafka/$f.java" "inbound-kafka/src/main/java/$example/$f.java"
  done
  put inbound-kafka/UnprocessableMessageException.java \
    "inbound-kafka/src/main/java/$pkg_path/cross/messaging/UnprocessableMessageException.java"
  for a in quarkus-messaging-kafka quarkus-jackson quarkus-smallrye-fault-tolerance; do
    add_dep "$dir/inbound-kafka/pom.xml" io.quarkus "$a"
  done
  put app-server/ExampleMessageReceiverTest.java "app-server/src/test/java/$example/ExampleMessageReceiverTest.java"
  add_dep "$dir/app-server/pom.xml" org.awaitility awaitility test
  props=$dir/app-server/src/main/resources/application.properties
  mkdir -p "$(dirname "$props")"
  grep -q '^mp.messaging.incoming.example-in\.' "$props" 2>/dev/null \
    || cat "$templates/fragments/kafka-application.properties" >>"$props"
  fill "$dir/inbound-kafka/src" "$props"
fi

guestbook=""
if [ -f "$dir/inbound-http-json-vm/src/main/java/$example/ExampleActionResource.java" ]; then
  [ -f "$dir/core/src/main/java/$example/ExampleGuestbookHandler.java" ] \
    || die "ExampleActionResource without ExampleGuestbookHandler: not htmx-hono's guestbook"
  [ ! -e "$dir/core/src/main/java/$example/ExampleGuest.java" ] || die "$example/ExampleGuest.java already exists"
  guestbook=", guestbook form via ExampleGuest.parse"
  put core/ExampleGuest.java "core/src/main/java/$example/ExampleGuest.java"
  put core/ParsedExampleGuest.java "core/src/main/java/$example/ParsedExampleGuest.java"
  put core/ExampleGuestbookHandler.java "core/src/main/java/$example/ExampleGuestbookHandler.java" force
  put core/ExampleGuestTest.java "core/src/test/java/$example/ExampleGuestTest.java"
  put inbound-http-json-vm/ExampleActionResource.java \
    "inbound-http-json-vm/src/main/java/$example/ExampleActionResource.java" force
  fill "$dir/inbound-http-json-vm/src/main/java/$example"
fi

fill "$dir/core/src" "$dir/inbound-http-jsonapi/src" "$dir/app-server/src"

echo "Validation in $dir (package $package): infrastructure, Example + ParsedExample, POST /api/example$kafka$guestbook, tests; nothing committed"
