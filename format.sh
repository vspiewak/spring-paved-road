#!/bin/bash
#
# Apply the paved-road formatting on every module.
# bom and the root aggregator live outside the parent chain — no spotless there.

set -euo pipefail
cd "$(dirname "$0")"

./mvnw -q -ntp -pl '!:spring-paved-road,!:bom' \
  com.diffplug.spotless:spotless-maven-plugin:apply

echo "✨ formatted"
