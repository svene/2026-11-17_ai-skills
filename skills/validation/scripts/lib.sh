# Shared helpers for the validation scripts; sourced, not run. Needs only bash, sed, awk and grep.

die() { echo "validation: $*" >&2; exit 2; }

templates=$(cd "$(dirname "${BASH_SOURCE[0]}")/../templates" && pwd)

# require_triptych <dir>: the precondition of every mode.
require_triptych() {
  [ -f "$1/pom.xml" ] || die "$1 has no pom.xml"
  [ -f "$1/core/pom.xml" ] && [ -f "$1/app-server/pom.xml" ] && compgen -G "$1/inbound-*/pom.xml" >/dev/null \
    || die "$1 is not a Triptych project (needs core, an inbound-* module and app-server): apply triptych first"
}

# detect_package <dir>: the package of app-server's AppServerBootTest.
detect_package() {
  local boot
  boot=$(cd "$1/app-server/src/test/java" 2>/dev/null && find . -name AppServerBootTest.java | head -1)
  [ -n "$boot" ] || die "no AppServerBootTest found: pass --package"
  dirname "${boot#./}" | tr / .
}

check_package() {
  local ident='[A-Za-z_][A-Za-z0-9_]*'
  [[ $1 =~ ^$ident(\.$ident)*$ ]] || die "invalid package '$1'"
}

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

# add_dep <module-pom> <groupId> <artifactId> [scope]: adds a BOM-managed dependency unless the artifactId is
# already there. Module poms only: it anchors on the two-space-indented </dependencies> of the project.
add_dep() {
  local pom=$1 g=$2 a=$3 scope=${4:-} frag
  grep -q "<artifactId>$a</artifactId>" "$pom" && return 0
  frag=$(mktemp)
  {
    echo '    <dependency>'
    echo "      <groupId>$g</groupId>"
    echo "      <artifactId>$a</artifactId>"
    [ -n "$scope" ] && echo "      <scope>$scope</scope>"
    echo '    </dependency>'
  } >"$frag"
  insert "$pom" before '  </dependencies>' "$frag"
  rm -f "$frag"
}

# put <template> <target> [force]: copies a template; an existing target is kept unless force is given.
put() {
  [ -e "$dir/$2" ] && [ -z "${3:-}" ] && { echo "kept existing $2"; return 0; }
  mkdir -p "$(dirname "$dir/$2")"; cp "$templates/$1" "$dir/$2"
}

# fill <path>...: replaces @PACKAGE@ (| as the sed delimiter; a validated package cannot contain it).
fill() {
  { grep -rlZ '@PACKAGE@' "$@" 2>/dev/null || true; } | xargs -0 -r sed -i -e "s|@PACKAGE@|$package|g"
}
