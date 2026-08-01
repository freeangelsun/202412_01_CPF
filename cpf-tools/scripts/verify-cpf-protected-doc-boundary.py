#!/usr/bin/env python3
from __future__ import annotations
import argparse
from pathlib import Path
import re
import sys

PROTECTED = re.compile(r"(^|/)(readme(?:\.[^/]*)?|[^/]*(?:manual|guide)[^/]*)$", re.IGNORECASE)
REQUIRED_HANDOVER_TOKENS = (
    "README와 README에서 연결되는 Manual·Guide는 이번 개발 Overlay의 수정 대상이 아니다.",
    "개발 완료 판단의 Source of Truth로 사용하지 않는다.",
    "실제 Source → SQL/Migration → Public API/OpenAPI → 실제 Consumer → Test/Gate → exact-SHA Runtime Evidence",
)

def verify(root: Path, manifest: Path, handover: Path) -> None:
    errors: list[str] = []
    if not manifest.is_file():
        errors.append(f"manifest missing: {manifest}")
    else:
        for raw in manifest.read_text(encoding="utf-8-sig").splitlines():
            value = raw.strip().replace("\\", "/")
            if not value or value.startswith("#"):
                continue
            if PROTECTED.search(value):
                errors.append(f"protected README/Manual/Guide is included in overlay: {value}")
    if not handover.is_file():
        errors.append(f"handover missing: {handover}")
    else:
        text = handover.read_text(encoding="utf-8")
        for token in REQUIRED_HANDOVER_TOKENS:
            if token not in text:
                errors.append(f"handover protection rule missing: {token}")
    if errors:
        raise ValueError("\n".join(errors))

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--manifest", default="cpf-docs/work/manifest/CPF_20260801_01_CHANGED_FILES.txt")
    parser.add_argument("--handover", default="cpf-docs/work/handover/CPF_20260801_01_DEVELOPMENT_HANDOVER.md")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    try:
        verify(root, root / args.manifest, root / args.handover)
    except ValueError as exc:
        print(f"[FAIL] CPF protected document boundary\n{exc}", file=sys.stderr)
        return 1
    print("[PASS] CPF protected document boundary readme=false linkedManual=false completionTruth=source")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
