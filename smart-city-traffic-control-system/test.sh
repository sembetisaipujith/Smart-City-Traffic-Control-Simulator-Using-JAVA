#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
./build.sh
mkdir -p test-bin
javac -encoding UTF-8 -cp bin -d test-bin tests/com/smartcity/traffic/SmokeTests.java
java -ea -Djava.awt.headless=true -cp "bin:test-bin" com.smartcity.traffic.SmokeTests
