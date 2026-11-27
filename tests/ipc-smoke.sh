#!/usr/bin/env bash
set -euo pipefail
reader=br.com.pythoteca.experimental.reader
executor=br.com.pythoteca.experimental.executor
adb install reader/build/outputs/apk/debug/reader-debug.apk
adb install executor/build/outputs/apk/debug/executor-debug.apk
adb shell input keyevent 82
adb shell am start -W -n "$reader/.MainActivity"
# The shell UID is not granted the signature permission.
denial=$(adb shell am startservice -n "$executor/.ProbeService" 2>&1 || true)
printf '%s\n' "$denial"
grep -Ei 'permission denial|requires permission|not allowed.*permission' <<< "$denial"
echo PYTHOTECA_SHELL_PERMISSION_DENIED
# Locate the Compose button using accessibility bounds rather than fixed coordinates.
for i in $(seq 1 20); do
  adb shell uiautomator dump /sdcard/window.xml >/dev/null
  adb pull /sdcard/window.xml /tmp/window.xml >/dev/null
  if coords=$(python -c 'import xml.etree.ElementTree as E,re; n=next(n for n in E.parse("/tmp/window.xml").iter("node") if n.get("text")=="Testar IPC"); a,b,c,d=map(int,re.findall(r"\d+",n.get("bounds"))); print((a+c)//2,(b+d)//2)' 2>/dev/null); then
    adb shell input tap $coords
    break
  fi
  sleep 1
done
for i in $(seq 1 20); do
  adb shell uiautomator dump /sdcard/window.xml >/dev/null
  adb pull /sdcard/window.xml /tmp/window.xml >/dev/null
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
  adb shell uiautomator dump /sdcard/window.xml >/dev/null
  adb pull /sdcard/window.xml /tmp/window.xml >/dev/null
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
