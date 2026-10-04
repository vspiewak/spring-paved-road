#!/bin/bash
#
# Render the modules diagram for the README, in both GitHub themes.

set -euo pipefail
cd "$(dirname "$0")"

for theme in default:light dark:dark; do
  npx -y @mermaid-js/mermaid-cli@11 -i modules.mmd -o "../images/modules-${theme#*:}.png" \
    -t "${theme%%:*}" -b transparent -s 2
done
