#!/usr/bin/env python3
from __future__ import annotations
import argparse
from pathlib import Path
import re
import sys

TYPE = re.compile(r"(?m)^public\s+(?:final\s+|abstract\s+)?(?:class|interface|record|enum)\s+([A-Za-z_$][\w$]*)")
TARGET = re.compile(r"(^|/)(?:src/main/java/.*/(?:api|spi|controller|service|incident|config)/|cpf-core/src/main/java/)")

def has_javadoc(text: str, start: int) -> bool:
    prefix = text[:start]
    lines = prefix.splitlines()
    i = len(lines) - 1
    while i >= 0 and (not lines[i].strip() or lines[i].strip().startswith("@")):
        i -= 1
    if i < 0 or "*/" not in lines[i]:
        return False
    while i >= 0:
        if "/**" in lines[i]:
            return True
        if "/*" in lines[i] and "/**" not in lines[i]:
            return False
        i -= 1
    return False

def verify(root: Path, manifest: Path) -> None:
    errors: list[str] = []
    if not manifest.is_file():
        raise ValueError(f"changed-file manifest missing: {manifest}")
    paths = []
    for raw in manifest.read_text(encoding="utf-8-sig").splitlines():
        rel = raw.strip().replace("\\", "/")
        if rel.endswith(".java") and TARGET.search(rel):
            paths.append(rel)
    for rel in sorted(set(paths)):
        path = root / rel
        if not path.is_file():
            errors.append(f"changed Java source missing: {rel}")
            continue
        text = path.read_text(encoding="utf-8")
        match = TYPE.search(text)
        if match and not has_javadoc(text, match.start()):
            errors.append(f"public type JavaDoc missing: {rel}:{match.group(1)}")
    if errors:
        raise ValueError("\n".join(errors))

def main() -> int:
    parser=argparse.ArgumentParser()
    parser.add_argument("--root",default=".")
    parser.add_argument("--manifest",default="cpf-docs/work/manifest/CPF_20260801_01_CHANGED_FILES.txt")
    args=parser.parse_args(); root=Path(args.root).resolve()
    try: verify(root,root/args.manifest)
    except ValueError as exc:
        print(f"[FAIL] CPF JavaDoc coverage\n{exc}",file=sys.stderr); return 1
    print("[PASS] CPF JavaDoc coverage changedPublicTypes=documented")
    return 0
if __name__=="__main__": raise SystemExit(main())
