#!/usr/bin/env bash
# run.sh <project-dir> [--existing --path <p>] [--dev-port <n>]
#
# Adds e2e-playwright/ to a Triptych project: an npm project with a pinned @playwright/test (Chromium only), a config
# whose webServer builds and starts the app (E2E_SERVER=jar, the default) or runs quarkus:dev (E2E_SERVER=dev), the
# dev-mode warm-up, and the first specs. Then the README link.
# Without --existing, the project must be fresh (triptych's example slice and nothing else): the specs are
# example.spec.ts for GET /api/example and, when index.html bootstraps /uiroute/ExamplePage (htmx-hono's example),
# guestbook.spec.ts. With --existing, any Triptych project: smoke.spec.ts loads --path, which is also the readiness URL.
# Refuses a project that already has a Playwright setup. Never commits.
#   --dev-port  default: quarkus.http.port (%dev first) from app-server's application.properties, else 8080
set -euo pipefail

die() { echo "playwright: $*" >&2; exit 2; }
templates=$(cd "$(dirname "$0")/../templates" && pwd)

[ $# -ge 1 ] && [ "${1:0:1}" != - ] || die "usage: run.sh <project-dir> [--existing --path <p>] [--dev-port <n>]"
dir=$1; shift
existing="" path="" dev_port=""
while [ $# -gt 0 ]; do
  case $1 in
    --existing) existing=1 ;;
    --path) path=${2:?--path needs a value}; shift ;;
    --dev-port) dev_port=${2:?--dev-port needs a value}; shift ;;
    *) die "unknown argument $1" ;;
  esac
  shift
done

[ -f "$dir/pom.xml" ] || die "$dir has no pom.xml"
[ -f "$dir/core/pom.xml" ] && [ -f "$dir/app-server/pom.xml" ] && compgen -G "$dir/inbound-*/pom.xml" >/dev/null \
  || die "$dir is not a Triptych project (needs core, an inbound-* module and app-server): apply triptych first"
dir=$(cd "$dir" && pwd)
[ -x "$dir/mvnw" ] || die "no ./mvnw in $dir: the webServer command uses the Maven wrapper"

m=e2e-playwright
[ ! -e "$dir/$m" ] || die "$m already exists: run the conformance check (references/conformance-check.md)"
other=$(find "$dir" -maxdepth 3 -name node_modules -prune -o -name 'playwright.config.*' -print | head -1)
[ -z "$other" ] || die "a Playwright setup exists already ($other): align it by hand (references/existing-project.md)"

if [ -z "$existing" ]; then
  [ -z "$path" ] || die "--path goes with --existing"
  receiver=$(find "$dir"/inbound-*/src/main/java -path '*/feature/example/ExampleReceiver.java' 2>/dev/null | head -1)
  [ -n "$receiver" ] || die "no triptych example slice (ExampleReceiver): not a fresh Triptych project, see --existing"
  features=$(find "$dir"/*/src/main/java -type d -path '*/feature/*' -prune -print | xargs -r -n1 basename | sort -u)
  [ "$features" = example ] || die "feature slices other than example exist ($(echo $features)): not a fresh Triptych project, see --existing"
  path=/api/example
else
  [ -n "$path" ] || die "--existing needs --path: a page or endpoint that answers 2xx once the app is up"
fi
[[ $path =~ ^/[^\ \"\`\$\|]*$ ]] || die "invalid --path '$path' (expected e.g. /api/items)"

if [ -z "$dev_port" ]; then
  props=$dir/app-server/src/main/resources/application.properties
  for key in '%dev.quarkus.http.port' 'quarkus.http.port'; do
    [ -z "$dev_port" ] && [ -f "$props" ] && dev_port=$(sed -n "s|^${key//./\\.}[[:space:]]*=[[:space:]]*\([0-9]*\)[[:space:]]*\$|\1|p" "$props" | head -1)
  done
  dev_port=${dev_port:-8080}
fi
[[ $dev_port =~ ^[0-9]+$ ]] || die "invalid --dev-port '$dev_port'"

# The npm project
mkdir -p "$dir/$m/tests"
for f in package.json package-lock.json tsconfig.json playwright.config.ts server.ts global-setup.ts; do
  cp "$templates/$m/$f" "$dir/$m/$f"
done
cp "$templates/$m/gitignore" "$dir/$m/.gitignore"
specs=""
if [ -z "$existing" ]; then
  cp "$templates/$m/tests/example.spec.ts" "$dir/$m/tests/"; specs=example.spec.ts
  if grep -rqs --include=index.html '/uiroute/ExamplePage' "$dir"/inbound-*/src/main/resources/META-INF/resources; then
    cp "$templates/$m/tests/guestbook.spec.ts" "$dir/$m/tests/"; specs="$specs guestbook.spec.ts"
  fi
else
  cp "$templates/$m/tests/smoke.spec.ts" "$dir/$m/tests/"; specs=smoke.spec.ts
fi
sed -i -e "s|@DEV_PORT@|$dev_port|g" -e "s|@READY_PATH@|$path|g" -e "s|@SMOKE_PATH@|$path|g" \
  "$dir/$m/server.ts" "$dir/$m"/tests/*.spec.ts

# README
readme=$dir/README.md link=$(cat "$templates/fragments/readme-playwright.md")
if grep -qF 'Playwright end-to-end tests' "$readme" 2>/dev/null; then
  echo "kept existing Playwright link"
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

echo "Playwright in $dir/$m: specs $specs, readiness $path, dev port $dev_port; nothing committed"
echo "Run: cd $m && npm ci && npm run install:browsers && npm test   (E2E_SERVER=dev npm test for quarkus:dev)"
