#!/usr/bin/env bash
# add-inbound-json-vm.sh <project-dir>: an inbound-http-json-vm module (core, quarkus-rest, quarkus-rest-jackson),
# registered in the parent's modules and dependencyManagement and as an app-server dependency, the way the
# triptych skill adds a Technology module by hand. The fixture adds the form resource.
set -euo pipefail
dir=$1
m=inbound-http-json-vm
mkdir -p "$dir/$m/src/main/java"
perl -0pe '
  s|<artifactId>inbound-http-jsonapi</artifactId>|<artifactId>inbound-http-json-vm</artifactId>|;
  s|<name>inbound-http-jsonapi</name>|<name>inbound-http-json-vm</name>|;
  s|<description>.*?</description>|<description>Inbound adapter: {route, vm} JSON envelopes for the htmx frontend.</description>|;
' "$dir/inbound-http-jsonapi/pom.xml" >"$dir/$m/pom.xml"
perl -0pi -e "s|(\n    <module>app-server</module>)|\n    <module>$m</module>\$1|" "$dir/pom.xml"
perl -0pi -e "s|(\n    </dependencies>\n  </dependencyManagement>)|\n      <dependency>\n        <groupId>\\\${project.groupId}</groupId>\n        <artifactId>$m</artifactId>\n        <version>\\\${project.version}</version>\n      </dependency>\$1|" "$dir/pom.xml"
perl -0pi -e "s|(      <artifactId>inbound-http-jsonapi</artifactId>\n    </dependency>)|\$1\n    <dependency>\n      <groupId>\\\${project.groupId}</groupId>\n      <artifactId>$m</artifactId>\n    </dependency>|" "$dir/app-server/pom.xml"
grep -q "<module>$m</module>" "$dir/pom.xml" || { echo "add-inbound-json-vm.sh: $m not in modules" >&2; exit 1; }
[ "$(grep -c "<artifactId>$m</artifactId>" "$dir/pom.xml")" = 1 ] || { echo "add-inbound-json-vm.sh: $m not in dependencyManagement" >&2; exit 1; }
grep -q "<artifactId>$m</artifactId>" "$dir/app-server/pom.xml" || { echo "add-inbound-json-vm.sh: $m not an app-server dependency" >&2; exit 1; }
grep -q '<version>${project.version}</version>' "$dir/pom.xml" || { echo "add-inbound-json-vm.sh: bad dependencyManagement entry" >&2; exit 1; }
