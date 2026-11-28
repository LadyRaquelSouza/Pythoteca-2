"""Acceptance gate for the existing synthetic console corpus."""
import json
import pathlib
import sys
root = pathlib.Path(__file__).resolve().parent
cases = json.loads((root / "corpus.json").read_text())["cases"]
rows = json.loads(pathlib.Path(sys.argv[1]).read_text())
assert [r["id"] for r in rows] == [c["id"] for c in cases]
for case, row in zip(cases, rows):
    if case["id"] == "P09":
        assert row["status"] == "syntax_or_compile_error" and row["errors"], row
        continue
    if case["id"] == "P10":
        assert row["status"] == "external_timeout" or (row["status"] == "resource_termination" and row["returncode"] == -24), row
        continue
    assert row["status"] == "completed", row
    events = row["events"]
    assert all(e["type"] in ("output", "input") for e in events), row
    assert "".join(e["text"] for e in events) == row["stdout"], row
    assert "".join(e["text"] for e in events if e["type"] == "output") == case["expected"]["stdout"] == row["programOutput"], row
    assert "".join(e["text"] for e in events if e["type"] == "input") == case["input"], row
    if case["id"] == "P22":
        assert events == [{"type":"output","text":"A:"},{"type":"input","text":"3\n"},{"type":"output","text":"B:"},{"type":"input","text":"4\n"},{"type":"output","text":"R:7"}], row
    print(case["id"], "CONSOLE_EVENTS_OK")
print("MOBILE_CONSOLE_EVENTS_OK")
