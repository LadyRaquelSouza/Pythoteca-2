#!/usr/bin/env bash
set -euo pipefail
tools="$ANDROID_HOME/build-tools/35.0.0"
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
for app in reader executor; do
  apk="$app/build/outputs/apk/debug/$app-debug.apk"
  "$tools/zipalign" -c -P 16 4 "$apk"
  "$tools/apksigner" verify --print-certs "$apk" | tee "$tmp/$app-cert"
  unzip -q "$apk" 'lib/*' -d "$tmp/$app"
  for abi in arm64-v8a x86_64; do
    test -d "$tmp/$app/lib/$abi"
    count=0
    while IFS= read -r -d '' so; do
      count=$((count+1))
      readelf -Wl "$so" > "$tmp/headers"
      gawk '$1=="LOAD" { print; n++; if (strtonum($NF)<16384) bad=1 } END { if (!n || bad) exit 1 }' "$tmp/headers"
      echo "ELF_LOAD_16K_OK $app $abi $(basename "$so")"
    done < <(find "$tmp/$app/lib/$abi" -name '*.so' -print0)
    test "$count" -gt 0
  done
done
grep '^Signer #1 certificate SHA-256 digest:' "$tmp/reader-cert" > "$tmp/reader-sha"
grep '^Signer #1 certificate SHA-256 digest:' "$tmp/executor-cert" > "$tmp/executor-sha"
cmp "$tmp/reader-sha" "$tmp/executor-sha"
echo PYTHOTECA_APK_ALIGNMENT_AND_SIGNATURE_OK
