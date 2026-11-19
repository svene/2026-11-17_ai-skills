#!/usr/bin/env bash
# run.sh <project-dir> [--name <name>]: write greeting.txt
set -euo pipefail
dir=$1; shift; name=world
while [ $# -gt 0 ]; do case $1 in --name) name=$2; shift;; *) echo "unknown $1" >&2; exit 2;; esac; shift; done
mkdir -p "$dir"; echo "hello $name" > "$dir/greeting.txt"
