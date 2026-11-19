#!/usr/bin/env bash
set -e
grep -qx 'hello apply' greeting.txt
test -f NOTES.md
