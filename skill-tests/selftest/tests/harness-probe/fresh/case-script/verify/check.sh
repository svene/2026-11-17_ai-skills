#!/usr/bin/env bash
set -e
grep -qx 'hello world' greeting.txt
grep -qx 'bye moon' farewell.txt
