#!/usr/bin/env bash
set -euo pipefail
adb install reader/build/outputs/apk/androidTest/debug/reader-debug-androidTest.apk
adb install ipc-test/build/outputs/apk/debug/ipc-test-debug.apk
adb install ipc-test/build/outputs/apk/androidTest/debug/ipc-test-debug-androidTest.apk
run_test() {
  local output
  output=$(timeout 240 adb shell am instrument -w -r "$1")
  printf '%s\n' "$output"
  shift
  for marker in "$@"; do grep -q "$marker" <<< "$output"; done
  grep -q 'INSTRUMENTATION_CODE: -1' <<< "$output"
}
run_test br.com.pythoteca.experimental.ipctest.test/br.com.pythoteca.experimental.ipctest.ProbeTest PYTHOTECA_FOREIGN_UID_DENIED
run_test br.com.pythoteca.experimental.reader.test/br.com.pythoteca.experimental.reader.ProtocolTest PYTHOTECA_INVALID_MESSAGES_DENIED PYTHOTECA_PYTHON_EXCEPTION_RECOVERED PYTHOTECA_PYTHON_NETWORK_DENIED PYTHOTECA_WATCHDOG_OK PYTHOTECA_BUSY_OK PYTHOTECA_PROCESS_RECOVERY_OK
