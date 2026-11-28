import hashlib
import json
import os
from pathlib import Path
from zipfile import ZipFile
import io

OUT = Path("inventory")
OUT.mkdir(exist_ok=True)
APKS = sorted(Path(".").glob("*/build/outputs/apk/**/*.apk"))
if not APKS:
    raise SystemExit("No APKs found")
def digest(data):
    return hashlib.sha256(data).hexdigest()
def entries(z, depth=0):
    result = []
    for item in z.infolist():
        if item.is_dir():
            continue
        data = z.read(item)
        row = {"path": item.filename, "size": len(data), "sha256": digest(data),
               "elf": data.startswith(b"\x7fELF"), "compression": item.compress_type}
        if depth < 2 and data.startswith(b"PK") and len(data) <= 128 * 1024 * 1024:
            stream = io.BytesIO(data)
            from zipfile import is_zipfile
            if is_zipfile(stream):
                with ZipFile(stream) as nested:
                    row["entries"] = entries(nested, depth + 1)
        result.append(row)
    return result
report = {"tested_commit": os.environ.get("GITHUB_SHA"), "apks": []}
for apk in APKS:
    with ZipFile(apk) as z:
        report["apks"].append({"path": str(apk), "sha256": digest(apk.read_bytes()), "entries": entries(z)})
(OUT / "apk-contents.json").write_text(json.dumps(report, indent=2) + "\n")
print("APK_INVENTORY_OK", len(APKS))
