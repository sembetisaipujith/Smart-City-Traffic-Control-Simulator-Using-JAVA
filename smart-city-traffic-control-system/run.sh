#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if [[ ! -f bin/com/smartcity/traffic/Main.class ]]; then
  ./build.sh
fi
echo "Starting Smart City Traffic Control System..."
exec java -cp bin com.smartcity.traffic.Main
