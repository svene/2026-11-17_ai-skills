#!/usr/bin/env bash
set -e
grep -qx 'hello world' greeting.txt
if [ -e planted.txt ]; then echo "PLANTED-VIOLATION found"; exit 1; fi
