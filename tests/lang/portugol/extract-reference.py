"""Stage a verified Studio archive, without running upstream code."""
import hashlib
from pathlib import Path, PurePosixPath
import shutil
import stat
import sys
import zipfile

archive, dest = map(Path, sys.argv[1:3])
if archive.stat().st_size != 132119906:
    raise SystemExit("Unexpected size")
with archive.open("rb") as f:
    if hashlib.file_digest(f, "sha256").hexdigest() != "989e2ea47116880748f0200434bd40f9ebb7b18a63311a76a782a39822a92296":
        raise SystemExit("Unexpected hash")
dest.mkdir(mode=0o755, exist_ok=False)
with zipfile.ZipFile(archive) as z:
    entries = z.infolist()
    if len(entries) > 20000 or sum(e.file_size for e in entries) > 1024**3:
        raise SystemExit("Archive exceeds limits")
    seen = set()
    for e in entries:
        p = PurePosixPath(e.filename)
        mode = e.external_attr >> 16
        if not p.parts or p.is_absolute() or ".." in p.parts or "\\" in e.filename or p in seen:
            raise SystemExit("Unsafe path")
        seen.add(p)
        if stat.S_IFMT(mode) not in (0, stat.S_IFREG, stat.S_IFDIR):
            raise SystemExit("Unsupported entry type")
        if p.parts[0] != "portugol-studio" or len(p.parts) < 2:
            continue
        target = dest.joinpath(*p.parts[1:])
        if e.is_dir():
            target.mkdir(parents=True, exist_ok=True)
        else:
            target.parent.mkdir(parents=True, exist_ok=True)
            with z.open(e) as src, target.open("xb") as out:
                shutil.copyfileobj(src, out)
            target.chmod(0o755 if mode & 0o111 else 0o644)
for name in ("java/java-linux/bin/java", "java/java-linux/bin/javac", "aplicacao/portugol-console.jar"):
    if not (dest / name).is_file():
        raise SystemExit("Missing " + name)
