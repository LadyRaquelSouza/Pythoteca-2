"""Prepare assets from the signed experimental APKs, then verify the built Suite."""
import hashlib
import os
import re
import shutil
import subprocess
import sys
from pathlib import Path
from zipfile import ZipFile

tools = Path(os.environ["ANDROID_HOME"]) / "build-tools/35.0.0"
assets = Path("installer/build/generated/suite-assets")
prefix = "br.com.pythoteca.experimental."
def command(tool, *args):
    return subprocess.check_output([str(tools / tool), *map(str, args)], text=True)
def cert(apk):
    output = command("apksigner", "verify", "--print-certs", apk)
    matches = re.findall(r"^Signer #\d+ certificate SHA-256 digest: ([0-9a-f]+)$", output, re.M)
    if len(matches) != 1:
        raise ValueError("expected exactly one signer")
    return matches[0]
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def prepare():
    assets.mkdir(parents=True, exist_ok=True)
    lines, signers = [], set()
    for module in ("reader", "executor"):
        apk = Path(f"{module}/build/outputs/apk/debug/{module}-debug.apk")
        badging = command("aapt", "dump", "badging", apk)
        m = re.search(r"^package: name='([^']+)' versionCode='(\d+)'", badging, re.M)
        if not m or m[1] != prefix + module or m[2] != "1":
            raise ValueError("unexpected package identity or version")
        signer = cert(apk)
        signers.add(signer)
        with ZipFile(apk) as z:
            abis = sorted({n.split("/")[1] for n in z.namelist() if n.startswith("lib/") and n.endswith(".so")})
        if not {"arm64-v8a", "x86_64"}.issubset(abis):
            raise ValueError("missing required ABI")
        shutil.copyfile(apk, assets / (module + ".apk"))
        lines += [f"{module}.sha256={digest(apk)}", f"{module}.cert={signer}", f"{module}.version=1", f"{module}.abis={','.join(abis)}"]
    if len(signers) != 1:
        raise ValueError("component signer mismatch")
    (assets / "suite.properties").write_text("\n".join(lines) + "\n")
def verify():
    suite = Path("installer/build/outputs/apk/debug/installer-debug.apk")
    command("zipalign", "-c", "-P", "16", "4", suite)
    permissions = command("aapt", "dump", "permissions", suite)
    if "android.permission.INTERNET" in permissions or "android.permission.REQUEST_INSTALL_PACKAGES" not in permissions:
        raise ValueError("unexpected installer permissions")
    with ZipFile(suite) as z:
        for name in ("reader.apk", "executor.apk", "suite.properties"):
            if z.read("assets/" + name) != (assets / name).read_bytes():
                raise ValueError("embedded content mismatch")
        for name in ("reader", "executor"):
            if cert(suite) != cert(assets / (name + ".apk")):
                raise ValueError("Suite signer mismatch")
    print("SUITE_PACKAGING_OK")
if __name__ == "__main__":
    {"prepare": prepare, "verify": verify}[sys.argv[1]]()
