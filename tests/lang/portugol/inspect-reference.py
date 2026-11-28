"""Inspect and safely stage a fixed official release for later isolated execution."""
import hashlib
import json
import os
from pathlib import Path, PurePosixPath
import stat
import sys
import zipfile

archive, output = map(Path, sys.argv[1:3])
if archive.stat().st_size != 132119906:
    raise SystemExit("Unexpected release size")
digest = hashlib.file_digest(archive.open("rb"), "sha256").hexdigest()
if digest != "989e2ea47116880748f0200434bd40f9ebb7b18a63311a76a782a39822a92296":
    raise SystemExit("Release hash changed")
with zipfile.ZipFile(archive) as z:
    entries = z.infolist()
    if len(entries) > 20000 or sum(e.file_size for e in entries) > 1024**3:
        raise SystemExit("Archive exceeds limits")
    seen = set()
    candidates = []
    for e in entries:
        p = PurePosixPath(e.filename)
        mode = e.external_attr >> 16
        if p.is_absolute() or ".." in p.parts or "\\" in e.filename or not p.parts or p in seen:
            raise SystemExit("Unsafe or duplicate archive path")
        seen.add(p)
        if stat.S_IFMT(mode) not in (0, stat.S_IFREG, stat.S_IFDIR, stat.S_IFLNK):
            raise SystemExit("Unsupported archive entry type")
        if p.name in ("portugol-console.jar", "java", "javac", "portugol-studio.jar"):
            candidates.append({"path": str(p), "bytes": e.file_size, "mode": oct(mode)})
    evidence = {"release": "v2.7.4", "assetId": 15620565,
        "sha256": digest, "bytes": archive.stat().st_size,
        "entries": len(entries), "uncompressedBytes": sum(e.file_size for e in entries),
        "symlinks": [e.filename for e in entries if stat.S_ISLNK(e.external_attr >> 16)],
        "candidates": candidates,
        "entryNames": sorted(e.filename for e in entries)}
output.write_text(json.dumps(evidence, indent=2) + "\n")
print("REFERENCE_ARCHIVE_INSPECTED", digest)
