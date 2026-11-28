"""Validate ELF PT_LOAD alignment in APKs and nested ZIPs; no extraction."""
import io
import json
import struct
import sys
from pathlib import Path
from zipfile import ZipFile, is_zipfile

PAGE = 16384
MAX_DEPTH = 2
MAX_ENTRY = 128 * 1024 * 1024

def elf_loads(data):
    if len(data) < 16 or data[4] not in (1, 2) or data[5] not in (1, 2):
        raise ValueError("invalid ELF identification")
    order = "<" if data[5] == 1 else ">"
    wide = data[4] == 2
    header = struct.unpack_from(order + ("HHIQQQIHHHHHH" if wide else "HHIIIIIHHHHHH"), data, 16)
    phoff, entsize, count = header[4], header[8], header[9]
    fmt = order + ("IIQQQQQQ" if wide else "IIIIIIII")
    if not count or count == 65535 or entsize < struct.calcsize(fmt) or phoff + entsize * count > len(data):
        raise ValueError("invalid or unsupported program header table")
    loads = []
    for i in range(count):
        p = struct.unpack_from(fmt, data, phoff + i * entsize)
        if p[0] != 1:
            continue
        offset, addr, filesz, memsz, align = (p[2], p[3], p[5], p[6], p[7]) if wide else (p[1], p[2], p[4], p[5], p[7])
        valid = (align >= PAGE and align & (align - 1) == 0 and
                 offset % align == addr % align and offset + filesz <= len(data) and filesz <= memsz)
        loads.append({"offset": offset, "vaddr": addr, "filesz": filesz, "memsz": memsz, "align": align, "valid": valid})
    if not loads:
        raise ValueError("no PT_LOAD")
    return loads

def scan(z, prefix, rows, errors, notices, depth=0):
    for entry in z.infolist():
        if entry.is_dir():
            continue
        name = prefix + "!/" + entry.filename
        if entry.file_size > MAX_ENTRY:
            errors.append(name + ": entry exceeds inspection limit")
            continue
        data = z.read(entry)
        base = entry.filename.rsplit("/", 1)[-1].lower()
        if any(k in base for k in ("license", "licence", "notice", "copying", "copyright")):
            notices.append({"path": name, "size": len(data)})
        if data.startswith(b"\x7fELF"):
            try:
                loads = elf_loads(data)
                rows.append({"path": name, "loads": loads})
                if not all(x["valid"] for x in loads):
                    errors.append(name + ": invalid PT_LOAD alignment or bounds")
            except (ValueError, struct.error) as exc:
                errors.append(name + ": " + str(exc))
        elif data.startswith(b"PK") and is_zipfile(io.BytesIO(data)):
            if depth >= MAX_DEPTH:
                errors.append(name + ": nested ZIP exceeds inspection depth")
            else:
                with ZipFile(io.BytesIO(data)) as nested:
                    scan(nested, name, rows, errors, notices, depth + 1)

def main():
    rows, errors, notices = [], [], []
    for module in ("reader", "executor"):
        apk = Path(f"{module}/build/outputs/apk/debug/{module}-debug.apk")
        before = len(rows)
        with ZipFile(apk) as z:
            scan(z, str(apk), rows, errors, notices)
        if len(rows) == before:
            errors.append(str(apk) + ": no ELF found")
    out = Path("inventory")
    out.mkdir(exist_ok=True)
    (out / "native-validation.json").write_text(json.dumps({"elf": rows, "errors": errors, "notice_candidates": notices}, indent=2) + "\n")
    print(f"NATIVE_VALIDATION ELF={len(rows)} errors={len(errors)} notices={len(notices)}")
    if errors:
        print("\n".join(errors), file=sys.stderr)
        return 1
    return 0

if __name__ == "__main__":
    sys.exit(main())
