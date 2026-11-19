#!/usr/bin/env bash
# outbound-httpclient is wired in, its Service implements an SPI in core, the rules stay green, nothing is committed.
set -euo pipefail

p=org/acme/shop
[ -f outbound-httpclient/pom.xml ] || { echo "missing outbound-httpclient/pom.xml"; exit 1; }
grep -q '<module>outbound-httpclient</module>' pom.xml || { echo "outbound-httpclient not in parent modules"; exit 1; }
grep -q '<artifactId>outbound-httpclient</artifactId>' app-server/pom.xml || { echo "app-server does not depend on outbound-httpclient"; exit 1; }
grep -q '<artifactId>core</artifactId>' outbound-httpclient/pom.xml || { echo "outbound-httpclient does not depend on core"; exit 1; }
ls core/src/main/java/$p/feature/*/*SPI.java >/dev/null 2>&1 || { echo "no SPI in core"; exit 1; }
svc=$(find outbound-httpclient/src/main/java/$p -name '*Service.java' | head -1)
[ -n "$svc" ] || { echo "no Service in outbound-httpclient"; exit 1; }
grep -q 'implements .*SPI' "$svc" || { echo "$svc implements no SPI"; exit 1; }
! git log --format=%s | grep -qiv '^\(Bare project\|skilltest:\)' || { echo "unexpected commit"; exit 1; }
grep -q 'Tests run: [1-9].*Failures: 0, Errors: 0, Skipped: 0' app-server/target/surefire-reports/*ArchitectureTest.txt || { echo "ArchitectureTest did not run cleanly"; exit 1; }
