#!/usr/bin/env bash
# add-infrastructure.sh <project-dir> [--package <p>]
#
# Adds the validation infrastructure to a Triptych project, skipping every piece that is already there:
#   - ConstructorValidation in core's cross.validation package
#   - quarkus-hibernate-validator in core/pom.xml
#   - ValidationArchitectureTest (rule: inbound modules construct Parsed* types only through parse())
#     and archunit.properties with failOnEmptyShould=true in app-server
#   - the validation.md link in README.md's "## Architecture" section
# Touches no endpoint and no consumer. Never commits.
#   --package  default: the package of app-server's AppServerBootTest
set -euo pipefail
source "$(dirname "$0")/lib.sh"

[ $# -ge 1 ] && [ "${1:0:1}" != - ] || die "usage: add-infrastructure.sh <project-dir> [--package <p>]"
dir=$1; shift
package=""
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
app=$dir/app-server

grep -q '<artifactId>archunit-junit5</artifactId>' "$app/pom.xml" \
  || die "app-server has no ArchUnit dependency: run the triptych conformance check first"

# ConstructorValidation: one copy per project, wherever it already lives.
if grep -rlq --include=ConstructorValidation.java 'class ConstructorValidation' "$dir/core/src/main/java" 2>/dev/null; then
  echo "kept existing ConstructorValidation"
else
  put core/ConstructorValidation.java "core/src/main/java/$pkg_path/cross/validation/ConstructorValidation.java"
fi
add_dep "$dir/core/pom.xml" io.quarkus quarkus-hibernate-validator

put app-server/devsupport/ValidationArchitectureTest.java "app-server/src/test/java/$pkg_path/devsupport/ValidationArchitectureTest.java"
props=$app/src/test/resources/archunit.properties
mkdir -p "$(dirname "$props")"
grep -qx 'archRule.failOnEmptyShould=true' "$props" 2>/dev/null || echo 'archRule.failOnEmptyShould=true' >>"$props"

# README: the link goes at the end of the "## Architecture" section (created if missing).
readme=$dir/README.md link=$(cat "$templates/fragments/readme-validation.md")
if grep -qF 'docs/architecture/validation.md' "$readme" 2>/dev/null; then
  echo "kept existing validation.md link"
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

fill "$dir/core/src/main/java/$pkg_path/cross/validation" "$app/src/test/java/$pkg_path/devsupport"

echo "Validation infrastructure in $dir (package $package): ConstructorValidation, quarkus-hibernate-validator in core, ValidationArchitectureTest, README link; nothing committed"
