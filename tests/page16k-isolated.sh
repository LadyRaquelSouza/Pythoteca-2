#!/usr/bin/env bash
set -euo pipefail
mode=${1:?experiment}
trap 'rc=$?; if [ "$rc" -ne 0 ]; then adb logcat -b crash -d || true; adb logcat -d -b system -b events | tail -n 500 || true; fi' EXIT
test "$(adb shell getconf PAGE_SIZE | tr -d '\r')" = 16384
pid=$(adb shell pidof system_server | tr -d '\r')
test -n "$pid"
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
health() {
  test "$(adb shell pidof system_server | tr -d '\r')" = "$pid"
  test "$(adb shell getprop sys.boot_completed | tr -d '\r')" = 1
  adb shell pm list packages >/dev/null
}
health
case "$mode" in
  reader|both|ping|python)
    echo INSTALL_READER
    adb install reader/build/outputs/apk/debug/reader-debug.apk
    health ;;
  executor) ;;
  *) exit 2 ;;
esac
case "$mode" in
  executor|both|ping|python)
    echo INSTALL_EXECUTOR
    adb install executor/build/outputs/apk/debug/executor-debug.apk
    health ;;
esac
if [[ "$mode" == ping || "$mode" == python ]]; then
  adb install reader/build/outputs/apk/androidTest/debug/reader-debug-androidTest.apk
  health
  echo "INSTRUMENTATION mode=$mode"
  output=$(timeout 90 adb shell am instrument -w -r -e mode "$mode" br.com.pythoteca.experimental.reader.test/br.com.pythoteca.experimental.reader.ProtocolTest)
  printf '%s\n' "$output"
  grep -q 'INSTRUMENTATION_CODE: -1' <<< "$output"
  grep -q "PYTHOTECA_ISOLATED_${mode^^}_OK" <<< "$output"
fi
for i in $(seq 1 24); do
  health
  echo "ISOLATED mode=$mode sample=$i system_server=$pid"
  sleep 5
done
health
echo "PYTHOTECA_16K_ISOLATED_OK mode=$mode"
