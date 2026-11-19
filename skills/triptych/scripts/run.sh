#!/usr/bin/env bash
# run.sh <project-dir> [--package <p>]
#
# Turns a fresh Bare project (parent + app-server, see the bare-project skill) into a Triptych
# project: adds `core` and `inbound-http-jsonapi` with a domain-free example feature slice
# (ExampleReceiver -> ExampleHandler), the ArchUnit rules in app-server's devsupport package,
# and an Architecture section in README.md. Never commits.
#   --package  default: the package of app-server's AppServerBootTest
# Needs only bash, sed and awk.
set -euo pipefail

die() { echo "triptych: $*" >&2; exit 2; }

[ $# -ge 1 ] && [ "${1:0:1}" != - ] || die "usage: run.sh <project-dir> [--package <p>]"
dir=$1; shift
package=""
while [ $# -gt 0 ]; do
  case $1 in
    --package) package=${2:?--package needs a value}; shift ;;
    *) die "unknown argument $1" ;;
  esac
  shift
done

[ -f "$dir/pom.xml" ] || die "$dir has no pom.xml"
dir=$(cd "$dir" && pwd)
pom=$dir/pom.xml app_pom=$dir/app-server/pom.xml

# Only a fresh Bare project: anything else is the skill's apply branch, done by hand.
[ -f "$app_pom" ] || die "no app-server module: not a Bare project"
[ "$(grep -c '<module>' "$pom")" -eq 1 ] && grep -q '^    <module>app-server</module>$' "$pom" \
  || die "the parent pom must list only app-server (not a fresh Bare project)"
[ ! -e "$dir/core" ] || die "$dir/core already exists"
[ ! -d "$dir/app-server/src/main/java" ] || die "app-server has main sources (not a fresh Bare project)"

if [ -z "$package" ]; then
  boot=$(cd "$dir/app-server/src/test/java" 2>/dev/null && find . -name AppServerBootTest.java | head -1)
  [ -n "$boot" ] || die "no AppServerBootTest found: pass --package"
  package=$(dirname "${boot#./}" | tr / .)
fi
ident='[A-Za-z_][A-Za-z0-9_]*'
[[ $package =~ ^$ident(\.$ident)*$ ]] || die "invalid package '$package'"

# The project's own coordinates are the first of each element in the parent pom.
first() { sed -n "s|^ *<$1>\([^<]*\)</$1>.*|\1|p" "$pom" | head -1; }
group_id=$(first groupId) artifact_id=$(first artifactId) version=$(first version)
[ -n "$group_id" ] && [ -n "$artifact_id" ] && [ -n "$version" ] || die "cannot read the parent pom's coordinates"

templates=$(cd "$(dirname "$0")/../templates" && pwd)
frag=$templates/fragments

# insert <file> <before|after> <exact-line> <fragment-file>: at the first line equal to <exact-line>.
insert() {
  local file=$1 where=$2 anchor=$3 fragment=$4 tmp
  grep -qxF -- "$anchor" "$file" || die "anchor '$anchor' not found in $file"
  tmp=$(mktemp)
  awk -v where="$where" -v anchor="$anchor" -v fragment="$fragment" '
    !done && $0 == anchor {
      if (where == "after") print
      while ((getline l < fragment) > 0) print l
      if (where == "before") print
      done = 1; next
    }
    { print }' "$file" >"$tmp"
  cat "$tmp" >"$file"; rm -f "$tmp"
}

insert "$pom" before '    <module>app-server</module>' "$frag/parent-modules.xml"
insert "$pom" before '  </properties>' "$frag/parent-properties.xml"
insert "$pom" before '    </dependencies>' "$frag/parent-dependency-management.xml"
insert "$pom" after '    </pluginManagement>' "$frag/parent-plugins.xml"
insert "$app_pom" after '  <dependencies>' "$frag/app-server-dependencies.xml"
insert "$app_pom" before '  </dependencies>' "$frag/app-server-test-dependencies.xml"

pkg_path=${package//.//}
put() { mkdir -p "$(dirname "$dir/$2")"; cp "$templates/$1" "$dir/$2"; }
put core/pom.xml core/pom.xml
put core/ExampleHandler.java "core/src/main/java/$pkg_path/feature/example/ExampleHandler.java"
put inbound-http-jsonapi/pom.xml inbound-http-jsonapi/pom.xml
put inbound-http-jsonapi/ExampleReceiver.java "inbound-http-jsonapi/src/main/java/$pkg_path/feature/example/ExampleReceiver.java"
put app-server/ExampleReceiverTest.java "app-server/src/test/java/$pkg_path/feature/example/ExampleReceiverTest.java"
for f in TriptychArchitecture TriptychArchitectureConfig ArchitectureTest; do
  put "app-server/devsupport/$f.java" "app-server/src/test/java/$pkg_path/devsupport/$f.java"
done
put app-server/archunit.properties app-server/src/test/resources/archunit.properties
[ -f "$dir/README.md" ] || printf '# %s\n' "$artifact_id" >"$dir/README.md"
cat "$frag/readme-architecture.md" >>"$dir/README.md"

# Fill in the placeholders (| as sed delimiter: no value may contain it, as validated or read above).
grep -rlZ '@[A-Z_]*@' "$dir/pom.xml" "$dir/core" "$dir/inbound-http-jsonapi" "$dir/app-server/src" | xargs -0 sed -i \
  -e "s|@GROUP_ID@|$group_id|g" \
  -e "s|@ARTIFACT_ID@|$artifact_id|g" \
  -e "s|@VERSION@|$version|g" \
  -e "s|@PACKAGE@|$package|g"

echo "Triptych project $group_id:$artifact_id in $dir: added core, inbound-http-jsonapi, feature.example and the ArchUnit rules (package $package); nothing committed"
