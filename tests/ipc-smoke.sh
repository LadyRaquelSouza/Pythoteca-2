#!/usr/bin/env bash
set -euo pipefail
reader=br.com.pythoteca.experimental.reader
executor=br.com.pythoteca.experimental.executor
capture_ui() {
  rm -f /tmp/window.xml
  adb shell rm -f /sdcard/window.xml || return 1
  adb shell uiautomator dump /sdcard/window.xml >/dev/null || return 1
  adb pull /sdcard/window.xml /tmp/window.xml >/dev/null || return 1
  test -s /tmp/window.xml
}
diagnostics() {
  adb logcat -b crash -d || true
  adb logcat -b all -d > /tmp/pythoteca-python-logcat.txt || true
  grep -Ei 'pythoteca|chaquopy|linker|dlopen|fatal signal|AndroidRuntime|UnsatisfiedLinkError|16kb|16k page|avc: denied' /tmp/pythoteca-python-logcat.txt || true
  adb shell dumpsys activity exit-info "$executor" || true
  adb shell dumpsys window | grep -E 'mCurrentFocus|mFocusedApp' || true
}
trap 'rc=$?; if [ "$rc" -ne 0 ]; then diagnostics; fi' EXIT
adb install reader/build/outputs/apk/debug/reader-debug.apk
adb install executor/build/outputs/apk/debug/executor-debug.apk
adb shell input keyevent 82
adb shell am force-stop com.google.android.apps.nexuslauncher
adb shell input keyevent KEYCODE_HOME
sleep 15
adb shell am start -W -n "$reader/.MainActivity"
# Verify actual navigation, not just the presence of the home screen.
tap_label() {
  local label="$1" coords
  for i in $(seq 1 15); do
    if ! capture_ui; then sleep 1; continue; fi
    if coords=$(python -c 'import sys,xml.etree.ElementTree as E,re; n=next(n for n in E.parse("/tmp/window.xml").iter("node") if n.get("text")==sys.argv[1]); a,b,c,d=map(int,re.findall(r"\d+",n.get("bounds"))); print((a+c)//2,(b+d)//2)' "$label" 2>/dev/null); then
      adb shell input tap $coords
      return
    fi
    sleep 1
  done
  return 1
}
tap_label "Abrir trilha"
tap_label "Voltar ao início"
tap_label "Biblioteca"
tap_label "Arquitetura de Computadores"
tap_label "Voltar à biblioteca"
tap_label "Voltar ao início"
echo PYTHOTECA_STUDY_NAVIGATION_OK
# The shell UID is not granted the signature permission.
denial=$(adb shell am startservice -n "$executor/.ProbeService" 2>&1 || true)
printf '%s\n' "$denial"
grep -Ei 'permission denial|requires permission|not allowed.*permission' <<< "$denial"
echo PYTHOTECA_SHELL_PERMISSION_DENIED
# Locate the Compose button using accessibility bounds rather than fixed coordinates.
for i in $(seq 1 20); do
  if ! capture_ui; then sleep 2; continue; fi
  if coords=$(python -c 'import xml.etree.ElementTree as E,re; n=next(n for n in E.parse("/tmp/window.xml").iter("node") if n.get("text")=="Testar IPC"); a,b,c,d=map(int,re.findall(r"\d+",n.get("bounds"))); print((a+c)//2,(b+d)//2)' 2>/dev/null); then
    adb shell input tap $coords
    break
  fi
  sleep 1
done
for i in $(seq 1 20); do
  if ! capture_ui; then sleep 2; continue; fi
  if grep -q 'IPC autenticado: PONG' /tmp/window.xml; then
    echo PYTHOTECA_IPC_PONG_OK
    break
  fi
  sleep 1
done
grep -q 'IPC autenticado: PONG' /tmp/window.xml
coords=$(python -c 'import xml.etree.ElementTree as E,re; n=next(n for n in E.parse("/tmp/window.xml").iter("node") if n.get("text")=="Testar Python"); a,b,c,d=map(int,re.findall(r"\d+",n.get("bounds"))); print((a+c)//2,(b+d)//2)')
adb logcat -c
adb logcat -c
adb shell input tap $coords
for i in $(seq 1 20); do
  if ! capture_ui; then sleep 2; continue; fi
  if grep -q 'Python: 42' /tmp/window.xml; then
    echo PYTHOTECA_PYTHON_42_OK
    exit 0
  fi
  if grep -Eq 'Executor desconectado|Falha Python|Tempo limite|Falha de comunicação' /tmp/window.xml; then
    echo PYTHOTECA_PYTHON_UI_FAILURE
    break
  fi
  if grep -Eq 'Executor desconectado|Falha Python|Tempo limite' /tmp/window.xml; then
    echo "Python probe failed; collecting immediate diagnostics"
    break
  fi
  sleep 1
done
cat /tmp/window.xml
adb logcat -d -b crash || true
adb logcat -d -b main -b system -b events > /tmp/python-failure.log || true
grep -Ei 'pythoteca|chaquopy|linker|fatal signal|fatal exception|dlopen|am_crash|am_proc_died|16kb|page size' /tmp/python-failure.log || true
adb shell dumpsys activity exit-info "$executor" || true
exit 1
