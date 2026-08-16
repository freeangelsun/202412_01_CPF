#!/usr/bin/env python3
"""Transient generation-state 기반 upgrade/remove/restore 안전 계약 정적 검증."""
from __future__ import annotations
import argparse
from pathlib import Path

REQUIRED = (
    "def _write_transient_state(",
    "generation-state.json",
    "def _read_transient_state(",
    "def upgrade(",
    "사용자 수정 Generated 파일이 있어 upgrade 중단",
    "def remove_plan(",
    "safeToRemove",
    "def remove_owned(",
    "사용자 변경 파일이 있어 remove 중단",
    "def restore(",
    "restore target이 비어 있지 않습니다",
    "현재 Generator Template이 remove 시점과 달라 restore할 수 없습니다",
    "metadataRequired':False",
)
FORBIDDEN = (
    "generator-ownership.json",
    "manifest/domain-manifest.json",
)

def validate(path: Path) -> list[str]:
    text=path.read_text(encoding="utf-8-sig",errors="ignore")
    errors=[f"missing token: {token}" for token in REQUIRED if token not in text]
    errors += [f"forbidden permanent metadata token: {token}" for token in FORBIDDEN if token in text]
    if text.index("def _read_transient_state(") > text.index("def upgrade("):
        errors.append("transient state reader must be defined before upgrade")
    return errors

def main() -> int:
    parser=argparse.ArgumentParser(); parser.add_argument("--root",type=Path,default=Path.cwd()); args=parser.parse_args()
    path=args.root/"cpf-tools/generator/engine/cpf_domain_generator.py"
    if not path.is_file(): print(f"FAIL missing {path}"); return 1
    errors=validate(path)
    if errors:
        for error in errors: print(f"FAIL {error}")
        return 1
    print("PASS generator lifecycle uses transient state and fail-closed upgrade/remove/restore")
    return 0
if __name__=="__main__": raise SystemExit(main())
