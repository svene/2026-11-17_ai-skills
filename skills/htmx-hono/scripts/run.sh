#!/usr/bin/env bash
# run.sh <project-dir> [--package <p>] [--existing]
#
# Adds the htmx + Hono frontend to a Triptych project:
#   - a new inbound-http-json-vm module (registered in the parent's modules and dependencyManagement and as an
#     app-server dependency): typescript-generator, frontend-maven-plugin (Node, npm ci, tsc, esbuild), the vendored
#     htmx 4 and Bulma 1, index.html, the hono extension and the cross.htmlrender and cross.devreload infrastructure
#   - the example slice's guestbook page: ExampleGuestbookHandler in core; view models, ExampleUiReceiver,
#     ExampleActionResource and the Hono templates in inbound-http-json-vm; ExampleUiTest in app-server
#   - UiRouteReceiverUnitTest in inbound-http-json-vm (the route table's startup checks)
#   - the .gitignore entries and the README link
# Without --existing, the project must be fresh: triptych's example slice and nothing else.
# With --existing, any Triptych project without an inbound-http-json-vm module and without the example's classes.
# Never commits.
#   --package  default: the package of app-server's AppServerBootTest
set -euo pipefail
source "$(dirname "$0")/lib.sh"

[ $# -ge 1 ] && [ "${1:0:1}" != - ] || die "usage: run.sh <project-dir> [--package <p>] [--existing]"
dir=$1; shift
package="" existing=""
while [ $# -gt 0 ]; do
  case $1 in
    --package) package=${2:?--package needs a value}; shift ;;
    --existing) existing=1 ;;
    *) die "unknown argument $1" ;;
  esac
  shift
done

require_triptych "$dir"
dir=$(cd "$dir" && pwd)
[ -n "$package" ] || package=$(detect_package "$dir")
check_package "$package"
pkg_path=${package//.//}
group_id=$(pom_value "$dir/pom.xml" groupId)
artifact_id=$(pom_value "$dir/pom.xml" artifactId)
version=$(pom_value "$dir/pom.xml" version)
[ -n "$group_id" ] && [ -n "$artifact_id" ] && [ -n "$version" ] || die "cannot read groupId, artifactId, version of $dir/pom.xml"

m=inbound-http-json-vm
[ ! -e "$dir/$m" ] || die "$m already exists: merge by hand (references/existing-project.md) or run the conformance check"
grep -q '<artifactId>archunit-junit5</artifactId>' "$dir/app-server/pom.xml" \
  || die "app-server has no ArchUnit dependency: run the triptych conformance check first"
example=$pkg_path/feature/example
if [ -z "$existing" ]; then
  [ -f "$dir/core/src/main/java/$example/ExampleHandler.java" ] \
    || die "no triptych example slice (ExampleHandler): not a fresh Triptych project, see --existing"
  features=$(for d in "$dir"/*/src/main/java/"$pkg_path"/feature/*/; do [ -d "$d" ] && basename "$d"; done | sort -u)
  [ "$features" = example ] || die "feature slices other than example exist ($(echo $features)): not a fresh Triptych project, see --existing"
fi
for f in ExampleGuestbookHandler ExampleUiReceiver ExampleActionResource; do
  ! compgen -G "$dir/*/src/main/java/$example/$f.java" >/dev/null || die "$example/$f.java already exists"
done
grep -qx "    <module>app-server</module>" "$dir/pom.xml" || die "no '<module>app-server</module>' line in $dir/pom.xml"

# The module and its wiring
mkdir -p "$dir/$m"
for f in pom.xml package.json package-lock.json tsconfig.json; do put "$m/$f" "$m/$f"; done
frag=$(mktemp)
echo "    <module>$m</module>" >"$frag"
insert "$dir/pom.xml" before "    <module>app-server</module>" "$frag"
printf '      <dependency>\n        <groupId>${project.groupId}</groupId>\n        <artifactId>%s</artifactId>\n        <version>${project.version}</version>\n      </dependency>\n' "$m" >"$frag"
insert "$dir/pom.xml" before "    </dependencies>" "$frag"
rm -f "$frag"
add_dep "$dir/app-server/pom.xml" '${project.groupId}' "$m"

# Infrastructure
java=$m/src/main/java/$pkg_path test=$m/src/test/java/$pkg_path res=$m/src/main/resources/META-INF/resources
for f in "$templates"/infra/htmlrender/*; do put "infra/htmlrender/${f##*/}" "$java/cross/htmlrender/${f##*/}"; done
for f in "$templates"/infra-test/htmlrender/*; do put "infra-test/htmlrender/${f##*/}" "$test/cross/htmlrender/${f##*/}"; done
for f in "$templates"/infra/devreload/*; do put "infra/devreload/${f##*/}" "$java/cross/devreload/${f##*/}"; done
put infra/resources/index.html "$res/index.html"
put infra/resources/js/dev-reload.js "$res/js/dev-reload.js"
put infra/resources/js/htmx.js "$res/js/htmx/4.0.2/htmx.js"
put infra/resources/css/bulma.min.css "$res/css/bulma/1.0.4/bulma.min.css"

# The example slice
put example/core/ExampleGuestbookHandler.java "core/src/main/java/$example/ExampleGuestbookHandler.java"
for f in "$templates"/example/inbound/* "$templates"/example/ts/*; do
  put "example/${f#"$templates"/example/}" "$java/feature/example/${f##*/}"
done
put example/app-server/ExampleUiTest.java "app-server/src/test/java/$example/ExampleUiTest.java"

# .gitignore and README
grep -qxF "/$m/node/" "$dir/.gitignore" 2>/dev/null || cat "$templates/fragments/gitignore" >>"$dir/.gitignore"
readme=$dir/README.md link=$(cat "$templates/fragments/readme-htmx-hono.md")
if grep -qF 'hypermedia-quarkus-browser-hono' "$readme" 2>/dev/null; then
  echo "kept existing htmx + Hono link"
elif grep -qx '## Architecture' "$readme" 2>/dev/null; then
  tmp=$(mktemp)
  awk -v link="$link" '
    { lines[NR] = $0 }
    $0 == "## Architecture" { in_section = 1; last = NR; next }
    in_section && /^#/ { in_section = 0 }
    in_section && NF { last = NR }
    END { for (i = 1; i <= NR; i++) { print lines[i]; if (i == last) print link } }' "$readme" >"$tmp"
  cat "$tmp" >"$readme"; rm -f "$tmp"
else
  printf '\n## Architecture\n\n%s\n' "$link" >>"$readme"
fi

fill "$dir/$m" "$dir/core/src/main/java/$example" "$dir/app-server/src/test/java/$example"

echo "htmx + Hono in $dir (package $package): module $m, guestbook example at / (ExamplePage), UiRouteReceiverUnitTest, ExampleUiTest; nothing committed"
echo "Dev loop: ./mvnw quarkus:dev -pl app-server -am, and in $m: npm run watch (node/ holds the Node install)"
