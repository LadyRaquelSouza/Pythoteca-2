#!/usr/bin/env bash
set -euo pipefail
adb install reader/build/outputs/apk/androidTest/debug/reader-debug-androidTest.apk
adb install ipc-test/build/outputs/apk/debug/ipc-test-debug.apk
adb install ipc-test/build/outputs/apk/androidTest/debug/ipc-test-debug-androidTest.apk
run_test() {
  local output
  output=$(timeout 60 adb shell am instrument -w "$1")
  printf '%s\n' "$output"
  grep -q "$2" <<< "$output"
  grep -q 'INSTRUMENTATION_CODE: -1' <<< "$output"
}
run_test br.com.pythoteca.experimental.ipctest.test/br.com.pythoteca.experimental.ipctest.ProbeTest PYTHOTECA_FOREIGN_UID_DENIED
run_test br.com.pythoteca.experimental.reader.test/br.com.pythoteca.experimental.reader.ProtocolTest PYTHOTECA_INVALID_MESSAGES_DENIED
