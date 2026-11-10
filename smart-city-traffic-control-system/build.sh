#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
command -v javac >/dev/null 2>&1 || { echo "[ERROR] Install JDK 17+ and add javac to PATH."; exit 1; }
mkdir -p bin
rm -rf bin/com
echo "Compiling Java source files..."
javac -encoding UTF-8 -d bin src/com/smartcity/traffic/*.java
test -f bin/com/smartcity/traffic/Main.class
echo "[SUCCESS] Compilation completed. Run ./run.sh."
