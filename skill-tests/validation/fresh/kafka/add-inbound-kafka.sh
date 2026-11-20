#!/usr/bin/env bash
# add-inbound-kafka.sh <project-dir>: an empty inbound-kafka module (depends on core), registered in the
# parent's modules and dependencyManagement and as an app-server dependency, the way the triptych skill adds
# a Technology module by hand. The validation skill then fills it.
set -euo pipefail
dir=$1
# The new pom is inbound-http-jsonapi's, renamed, with only the core dependency.
mkdir -p "$dir/inbound-kafka/src/main/java"
perl -0pe '
  s|<artifactId>inbound-http-jsonapi</artifactId>|<artifactId>inbound-kafka</artifactId>|;
  s|<name>inbound-http-jsonapi</name>|<name>inbound-kafka</name>|;
  s|<description>.*?</description>|<description>Inbound adapter: Kafka consumers.</description>|;
  s|\n    <dependency>\n      <groupId>io.quarkus</groupId>\n      <artifactId>quarkus-rest[^<]*</artifactId>\n    </dependency>||g;
' "$dir/inbound-http-jsonapi/pom.xml" >"$dir/inbound-kafka/pom.xml"
perl -0pi -e 's|(\n    <module>app-server</module>)|\n    <module>inbound-kafka</module>$1|' "$dir/pom.xml"
perl -0pi -e 's|(\n    </dependencies>\n  </dependencyManagement>)|\n      <dependency>\n        <groupId>\${project.groupId}</groupId>\n        <artifactId>inbound-kafka</artifactId>\n        <version>\${project.version}</version>\n      </dependency>$1|' "$dir/pom.xml"
perl -0pi -e 's|(      <artifactId>inbound-http-jsonapi</artifactId>\n    </dependency>)|$1\n    <dependency>\n      <groupId>\${project.groupId}</groupId>\n      <artifactId>inbound-kafka</artifactId>\n    </dependency>|' "$dir/app-server/pom.xml"
for f in pom.xml app-server/pom.xml; do
  grep -q 'inbound-kafka' "$dir/$f" || { echo "add-inbound-kafka.sh: inbound-kafka not registered in $f" >&2; exit 1; }
done
! grep -q quarkus-rest "$dir/inbound-kafka/pom.xml" || { echo "add-inbound-kafka.sh: REST dependencies left in inbound-kafka" >&2; exit 1; }
