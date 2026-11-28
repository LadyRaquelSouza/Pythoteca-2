"""Run the official Studio console in a disposable offline Linux container.
Requires an independently verified standalone Studio installation.
Never run this against a personal installation or with private data mounted.
"""
import hashlib
import json
import os
from pathlib import Path
import resource
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parent
STUDIO = Path("/studio")
APP = STUDIO / "aplicacao"
JAVA = STUDIO / "java/java-linux/bin/java"
JAR = APP / "portugol-console.jar"
if not JAVA.is_file() or not JAR.is_file():
    raise SystemExit("Verified standalone Studio layout required")
def limits():
    resource.setrlimit(resource.RLIMIT_CPU, (20, 21))
    resource.setrlimit(resource.RLIMIT_FSIZE, (256 * 1024, 256 * 1024))
    resource.setrlimit(resource.RLIMIT_CORE, (0, 0))
results = []
for case in json.loads((ROOT / "corpus.json").read_text())["cases"]:
    with tempfile.TemporaryDirectory(dir="/tmp") as tmp:
        source = Path(tmp) / "fixture.por"
        source.write_text(case["source"], encoding="utf-8")
        with tempfile.TemporaryFile() as out, tempfile.TemporaryFile() as err:
            row = {"id": case["id"], "expected": case["expected"]}
            try:
                p = subprocess.run([str(JAVA), "-Xmx256m", "-Djava.awt.headless=true",
                    "-Dfile.encoding=UTF-8", "-jar", str(JAR), str(source), "-no-wait"],
                    input=case["input"].encode("utf-8"), stdout=out, stderr=err,
                    cwd=APP, env={"PATH": str(JAVA.parent) + ":/usr/bin:/bin", "HOME": "/tmp",
                    "LANG": "C.UTF-8"}, timeout=30, preexec_fn=limits)
                row.update(returncode=p.returncode, status="review_required")
            except subprocess.TimeoutExpired:
                row.update(status="external_timeout")
            out.seek(0)
            err.seek(0)
            row.update(stdout=out.read().decode("utf-8", errors="replace"),
                       stderr=err.read().decode("utf-8", errors="replace"))
            results.append(row)
Path("/results/reference-results.json").write_text(json.dumps({
    "consoleJarSha256": hashlib.sha256(JAR.read_bytes()).hexdigest(),
    "cases": results}, ensure_ascii=False, indent=2) + "\n")
