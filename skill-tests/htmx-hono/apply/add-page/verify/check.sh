#!/usr/bin/env bash
# The note page follows the technique (route enum, routes.ts entry, API client, a UiRouteSource, no literal
# mutation URL, linked from the guestbook through nameUrl), the example still works, the hidden check and a note
# UI test of the agent's own ran, nothing committed.
set -euo pipefail
m=inbound-http-json-vm p=org/acme/notes
h=$m/src/main/java/$p/cross/htmlrender
ls core/src/main/java/$p/feature/note/*.java >/dev/null 2>&1 || { echo "no note Handler in core"; exit 1; }
grep -q '^    NotePage,\?$' $h/UiRouteName.java || { echo "NotePage not in UiRouteName"; exit 1; }
grep -qE '^\s*NotePage:' $h/routes.ts || { echo "NotePage not in routes.ts"; exit 1; }
grep -q 'satisfies Record<UiRouteName, RouteDefinition>' $h/routes.ts || { echo "routes.ts lost satisfies"; exit 1; }
grep -q 'new NoteActionResourceClient(' $h/api.ts || { echo "no NoteActionResourceClient in api.ts"; exit 1; }
grep -q 'implements UiRouteSource' $m/src/main/java/$p/feature/note/*.java || { echo "no note UiRouteSource"; exit 1; }
! grep -rnE 'hx-(post|put|patch|delete)="[^$]' $m/src/main/java || { echo "literal mutation URL"; exit 1; }
grep -rq 'nameUrl("NotePage")' $m/src/main/java/$p/feature/example/ || { echo "guestbook does not link NotePage via nameUrl"; exit 1; }
! git log --format=%s | grep -qiv '^\(Bare project\|skilltest:\)' || { echo "unexpected commit"; exit 1; }
r=app-server/target/surefire-reports
grep -q 'Tests run: 3, Failures: 0, Errors: 0, Skipped: 0' $r/*NoteUiCheckTest.txt || { echo "NoteUiCheckTest did not run cleanly"; exit 1; }
grep -q 'Tests run: 4, Failures: 0, Errors: 0, Skipped: 0' $r/*ExampleUiTest.txt || { echo "ExampleUiTest did not run cleanly"; exit 1; }
ls $r | grep -v NoteUiCheckTest | grep -q 'Note.*Test.txt' || { echo "no note test of the agent's own"; exit 1; }
