#!/usr/bin/env bash
# run.sh <project-dir> --group-id <g> [--artifact-id <a>] [--package <p>] [--java <n>]
#
# Creates a bare multi-module Quarkus project (parent + app-server) in <project-dir>
# from the bundled templates, then `git init` plus one initial commit.
#   --artifact-id  default: basename of <project-dir>
#   --package      default: <groupId>.<artifactId without non-alphanumerics, lower-cased>
#   --java         default: 25 (maven.compiler.release and the .sdkmanrc JDK)
#   QUARKUS_VERSION (env) overrides the pinned Quarkus platform version.
# Needs only bash, git and a JDK; the Maven wrapper fetches Maven on first use.
set -euo pipefail

DEFAULT_QUARKUS_VERSION=3.40.1.2
# --java <n> -> SDKMAN JDK identifier written to .sdkmanrc
declare -A SDKMAN_JAVA=([21]=21.0.7-tem [25]=25.0.1-tem)

die() { echo "bare-project: $*" >&2; exit 2; }

[ $# -ge 1 ] && [ "${1:0:1}" != - ] || die "usage: run.sh <project-dir> --group-id <g> [--artifact-id <a>] [--package <p>] [--java <n>]"
dir=$1; shift
group_id="" artifact_id="" package="" java=25
while [ $# -gt 0 ]; do
  case $1 in
    --group-id) group_id=${2:?--group-id needs a value}; shift ;;
    --artifact-id) artifact_id=${2:?--artifact-id needs a value}; shift ;;
    --package) package=${2:?--package needs a value}; shift ;;
    --java) java=${2:?--java needs a value}; shift ;;
    *) die "unknown argument $1" ;;
  esac
  shift
done

[ -n "$group_id" ] || die "--group-id is required"
[ -n "$artifact_id" ] || artifact_id=$(basename "$dir")
[ -n "$package" ] || package="$group_id.$(tr -cd '[:alnum:]' <<<"$artifact_id" | tr '[:upper:]' '[:lower:]')"
quarkus_version=${QUARKUS_VERSION:-$DEFAULT_QUARKUS_VERSION}

ident='[A-Za-z_][A-Za-z0-9_]*'
[[ $group_id =~ ^$ident(\.$ident)*$ ]] || die "invalid group id '$group_id' (expected e.g. org.acme)"
[[ $artifact_id =~ ^[A-Za-z0-9][A-Za-z0-9._-]*$ ]] || die "invalid artifact id '$artifact_id'"
[[ $package =~ ^$ident(\.$ident)*$ ]] || die "invalid package '$package' (pass --package explicitly)"
[[ $java =~ ^[0-9]+$ ]] && [ -n "${SDKMAN_JAVA[$java]:-}" ] \
  || die "unsupported --java '$java' (supported: ${!SDKMAN_JAVA[*]})"
command -v java >/dev/null || die "no JDK on PATH"
command -v git >/dev/null || die "git not found"

if [ -e "$dir" ] && [ -n "$(ls -A "$dir")" ]; then die "$dir exists and is not empty"; fi

templates=$(cd "$(dirname "$0")/../templates" && pwd)
mkdir -p "$dir"
dir=$(cd "$dir" && pwd)

# Copy the templates, turning dot-<name> into .<name> and placing the boot test in its package.
(cd "$templates" && tar cf - .) | (cd "$dir" && tar xf -)
for f in "$dir"/dot-*; do mv "$f" "$dir/.${f##*/dot-}"; done
test_dir="$dir/app-server/src/test/java/${package//.//}"
mkdir -p "$test_dir"
mv "$dir/app-server/AppServerBootTest.java" "$test_dir/"

# Fill in the placeholders (sed with | as delimiter: no value may contain it, as validated above).
grep -rlZ '@[A-Z_]*@' "$dir" | xargs -0 sed -i \
  -e "s|@GROUP_ID@|$group_id|g" \
  -e "s|@ARTIFACT_ID@|$artifact_id|g" \
  -e "s|@PACKAGE@|$package|g" \
  -e "s|@JAVA_RELEASE@|$java|g" \
  -e "s|@QUARKUS_VERSION@|$quarkus_version|g" \
  -e "s|@SDKMAN_JAVA@|${SDKMAN_JAVA[$java]}|g"

(cd "$dir" && git init -q -b main && git add -A && git commit -q -m "Bare project $artifact_id (Quarkus $quarkus_version, Java $java)")

echo "Created $dir: $group_id:$artifact_id, package $package, Quarkus $quarkus_version, Java $java"
