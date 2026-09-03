#!/usr/bin/env python3
from __future__ import annotations

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass
import argparse
from pathlib import Path
import re
import sys
import os
import subprocess

PROTECTED = re.compile(r"(^|/)(readme(?:\.[^/]*)?|[^/]*(?:manual|guide)[^/]*)$", re.IGNORECASE)
REQUIRED_HANDOVER_TOKENS = (
    "Canonical Target:",
    "Latest local integration one-line command",
    "Tee-Object",
    "사용자 승인 없는 commit/push",
)

def verify(root: Path, manifest: Path, handover: Path) -> None:
    errors: list[str] = []
    if not manifest.is_file():
        errors.append(f"manifest missing: {manifest}")
    else:
        lines = manifest.read_text(encoding="utf-8-sig").splitlines()
        for index, raw in enumerate(lines):
            value = raw.strip().replace("\\", "/")
            if not value or value.startswith("#"):
                continue
            if index == 0 and value.startswith("path,"):
                continue
            if "," in value and manifest.suffix.lower() == ".csv":
                value = value.split(",", 1)[0].strip().strip('"')
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
    parser.add_argument("--manifest")
    parser.add_argument("--handover", default="cpf-docs/governance/development-harness/current/CPF_DEVELOPMENT_HANDOVER.md")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    try:
        if args.manifest:
            manifest = root / args.manifest
        else:
            base = os.getenv("GITHUB_BASE_REF")
            cmd = ["git", "diff", "--name-only", f"origin/{base}...HEAD"] if base else ["git", "diff-tree", "--no-commit-id", "--name-only", "-r", "HEAD"]
            cp = subprocess.run(cmd, cwd=root, text=True, capture_output=True)
            if cp.returncode != 0:
                raise ValueError(cp.stderr.strip() or "git changed-file query failed")
            manifest = root / "build/verification/current-protected-doc-changed-files.txt"
            manifest.parent.mkdir(parents=True, exist_ok=True)
            manifest.write_text("\n".join(x.strip() for x in cp.stdout.splitlines() if x.strip()) + "\n", encoding="utf-8")
        verify(root, manifest, root / args.handover)
    except ValueError as exc:
        print(f"[FAIL] CPF protected document boundary\n{exc}", file=sys.stderr)
        return 1
    print("[PASS] CPF protected document boundary readme=false linkedManual=false completionTruth=source")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
