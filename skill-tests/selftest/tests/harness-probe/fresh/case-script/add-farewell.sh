#!/usr/bin/env bash
# add-farewell.sh <project-dir> --name <name>: a case-local setup step
set -euo pipefail
[ "$2" = --name ] && echo "bye $3" > "$1/farewell.txt"
