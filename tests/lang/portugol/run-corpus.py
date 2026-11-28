"""Run fixed synthetic cases, each with an external deadline and bounded log files."""
import json
import pathlib
import subprocess
import tempfile
import resource
import os

root = pathlib.Path(__file__).resolve().parent
cases = json.loads((root / "corpus.json").read_text())["cases"]
def limits():
    resource.setrlimit(resource.RLIMIT_CPU, (5, 6))
    resource.setrlimit(resource.RLIMIT_FSIZE, (256 * 1024, 256 * 1024))
    resource.setrlimit(resource.RLIMIT_CORE, (0, 0))

results = []
for case in cases:
    with tempfile.TemporaryFile() as out, tempfile.TemporaryFile() as err:
        try:
            p = subprocess.run(["node", "--max-old-space-size=128", str(root / "run-case.cjs"), case["id"]],
                               cwd=root, stdout=out, stderr=err, timeout=10, preexec_fn=limits,
                               env={"PATH": os.environ["PATH"], "HOME": "/tmp", "LANG": "C.UTF-8"})
            out.seek(0); err.seek(0)
            raw = out.read().decode("utf-8", errors="replace")
            stderr = err.read().decode("utf-8", errors="replace")
            try:
                row = json.loads(raw) if p.returncode == 0 else {"status": "process_error"}
                if row.get("id") != case["id"] and p.returncode == 0:
                    raise ValueError("invalid fixture response")
            except (ValueError, AttributeError):
                row = {"status": "invalid_response"}
            row.update(id=case["id"], returncode=p.returncode, raw=raw, stderr=stderr)
            if p.returncode in (-24, -9):
                row["status"] = "resource_termination"
        except subprocess.TimeoutExpired:
            row = {"id": case["id"], "status": "external_timeout"}
        expected = case["expected"]
        row["expected"] = expected
        row["exact_match"] = row["status"] == expected["status"] and (
            "stdout" not in expected or row.get("stdout") == expected["stdout"])
        results.append(row)
        print(case["id"], row["status"], "exact" if row["exact_match"] else "review", flush=True)
pathlib.Path("/results/portugol-results.json").write_text(json.dumps(results, ensure_ascii=False, indent=2) + "\n")
