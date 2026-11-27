#!/usr/bin/env bash
set -euo pipefail
page=$(adb shell getconf PAGE_SIZE | tr -d '\r')
echo "PAGE_SIZE=$page"
test "$page" = 16384
adb shell setprop bionic.linker.16kb.app_compat.enabled false
adb shell setprop pm.16kb.app_compat.disabled true
test "$(adb shell getprop bionic.linker.16kb.app_compat.enabled | tr -d '\r')" = false
test "$(adb shell getprop pm.16kb.app_compat.disabled | tr -d '\r')" = true
bash tests/ipc-smoke.sh
bash tests/ipc-negative.sh
echo PYTHOTECA_16K_RUNTIME_OK
