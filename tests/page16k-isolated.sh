#!/usr/bin/env bash
set -euo pipefail
mode=${1:?experiment}
mkdir -p diagnostics
monitor=
cleanup() {
  rc=$?
  trap - EXIT
  if [[ -n "$monitor" ]]; then kill "$monitor" 2>/dev/null || true; wait "$monitor" 2>/dev/null || true; fi
  timeout 20 adb logcat -b all -d > diagnostics/logcat.txt 2>&1 || true
  timeout 15 adb shell dumpsys activity exit-info br.com.pythoteca.experimental.reader > diagnostics/reader-exit.txt 2>&1 || true
  timeout 15 adb shell dumpsys activity exit-info br.com.pythoteca.experimental.executor > diagnostics/executor-exit.txt 2>&1 || true
  if [[ "$rc" != 0 ]]; then tail -n 150 diagnostics/logcat.txt; fi
  exit "$rc"
}
trap cleanup EXIT
snapshot() {
  date -u
  timeout 10 adb shell 'cat /proc/meminfo; cat /proc/pressure/memory; ps -A | grep -E "system_server|pythoteca" || true'
}
free -m
adb shell getprop ro.build.fingerprint
snapshot
echo PREPARATION_BEGIN
test "$(adb shell getconf PAGE_SIZE | tr -d '\r')" = 16384
pid=$(adb shell pidof system_server | tr -d '\r')
test -n "$pid"
adb root
timeout 60 adb wait-for-device
test "$(adb shell id -u | tr -d '\r')" = 0
adb shell setprop bionic.linker.16kb.app_compat.enabled false
adb shell setprop pm.16kb.app_compat.disabled true
adb unroot
timeout 60 adb wait-for-device
test "$(adb shell id -u | tr -d '\r')" = 2000
test "$(adb shell getprop bionic.linker.16kb.app_compat.enabled | tr -d '\r')" = false
test "$(adb shell getprop pm.16kb.app_compat.disabled | tr -d '\r')" = true
health() {
  test "$(adb shell pidof system_server | tr -d '\r')" = "$pid"
  test "$(adb shell getprop sys.boot_completed | tr -d '\r')" = 1
  adb shell pm list packages >/dev/null
}
health
echo STABILIZATION_BEGIN
for i in $(seq 1 24); do
  health
  snapshot >> diagnostics/stabilization.txt 2>&1
  sleep 5
done
health
echo STABILIZATION_OK
(while true; do snapshot; sleep 1; done) > diagnostics/resources.txt 2>&1 &
monitor=$!
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
  timeout 90 adb shell am instrument -w -r -e mode "$mode" br.com.pythoteca.experimental.reader.test/br.com.pythoteca.experimental.reader.ProtocolTest | tee diagnostics/instrumentation.txt
  output=$(cat diagnostics/instrumentation.txt)
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
