#!/usr/bin/env bash
set -euo pipefail
mode=${1:?default or strict}
test "$mode" = default || test "$mode" = strict
trap 'rc=$?; if [ "$rc" -ne 0 ]; then adb logcat -b crash -d || true; fi' EXIT
adb shell getprop ro.build.fingerprint
test "$(adb shell getconf PAGE_SIZE | tr -d '\r')" = 16384
test "$(adb shell id -u | tr -d '\r')" = 2000
! adb shell pm list packages | grep -q br.com.pythoteca.experimental
pid=$(adb shell pidof system_server | tr -d '\r')
test -n "$pid"
if [ "$mode" = strict ]; then
  adb root
  adb wait-for-device
  test "$(adb shell id -u | tr -d '\r')" = 0
  adb shell setprop bionic.linker.16kb.app_compat.enabled false
  adb shell setprop pm.16kb.app_compat.disabled true
  adb unroot
  adb wait-for-device
  test "$(adb shell id -u | tr -d '\r')" = 2000
  test "$(adb shell getprop bionic.linker.16kb.app_compat.enabled | tr -d '\r')" = false
  test "$(adb shell getprop pm.16kb.app_compat.disabled | tr -d '\r')" = true
fi
adb shell getprop bionic.linker.16kb.app_compat.enabled
adb shell getprop pm.16kb.app_compat.disabled
for i in $(seq 1 24); do
  test "$(adb shell pidof system_server | tr -d '\r')" = "$pid"
  test "$(adb shell getprop sys.boot_completed | tr -d '\r')" = 1
  adb shell pm list packages >/dev/null
  echo "CONTROL mode=$mode sample=$i system_server=$pid"
  sleep 5
done
test "$(adb shell pidof system_server | tr -d '\r')" = "$pid"
echo "PYTHOTECA_16K_CONTROL_OK mode=$mode"
