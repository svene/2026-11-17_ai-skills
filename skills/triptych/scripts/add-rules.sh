#!/usr/bin/env bash
# add-rules.sh <project-dir> --package <p>
#
# Copies the Triptych ArchUnit rules (TriptychArchitecture, TriptychArchitectureConfig, ArchitectureTest)
# into app-server/src/test/java/<package>/devsupport and archunit.properties into
# app-server/src/test/resources, filling in the root package. Skips every file that already exists and
# names it. Touches no pom: the archunit-junit5 and jandex entries are added by hand (see SKILL.md).
# Never commits. Needs only bash and sed.
set -euo pipefail

die() { echo "triptych add-rules: $*" >&2; exit 2; }

[ $# -ge 1 ] && [ "${1:0:1}" != - ] || die "usage: add-rules.sh <project-dir> --package <p>"
dir=$1; shift
package=""
while [ $# -gt 0 ]; do
  case $1 in
    --package) package=${2:?--package needs a value}; shift ;;
    *) die "unknown argument $1" ;;
  esac
  shift
done
[ -n "$package" ] || die "--package is required"
ident='[A-Za-z_][A-Za-z0-9_]*'
[[ $package =~ ^$ident(\.$ident)*$ ]] || die "invalid package '$package'"
[ -f "$dir/app-server/pom.xml" ] || die "$dir has no app-server module"

templates=$(cd "$(dirname "$0")/../templates/app-server" && pwd)
target_java=$dir/app-server/src/test/java/${package//.//}/devsupport
copy() {
  if [ -e "$2" ]; then echo "skipped (exists): ${2#"$dir"/}"; return; fi
  mkdir -p "$(dirname "$2")"
  sed "s|@PACKAGE@|$package|g" "$1" >"$2"
  echo "added: ${2#"$dir"/}"
}
for f in TriptychArchitecture TriptychArchitectureConfig ArchitectureTest; do
  copy "$templates/devsupport/$f.java" "$target_java/$f.java"
done
copy "$templates/archunit.properties" "$dir/app-server/src/test/resources/archunit.properties"
