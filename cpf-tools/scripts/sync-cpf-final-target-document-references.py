#!/usr/bin/env python3
"""Synchronize large/reference documents after CPF Final Target detail revision.

This script is intentionally hash/marker guarded. It never touches README.md or
cpf-docs/guides/**. It does not commit, push, switch branches, reset, restore,
clean, or stash.
"""
from __future__ import annotations
import argparse
import hashlib
import json
import subprocess
import sys
from pathlib import Path

BASELINE = "c1f273f1ea4fafac6fd5d23bd837adfc38a04497"
EXPECTED_BLOBS = {
    "cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md":
        "89b5e3aab77dcd15c39ca430c2b534cb80e76857",
    "cpf-docs/work/state/CPF_CHANGE_IMPACT_AND_VALIDATION_LEDGER.md":
        "83e17e014b8da74f3f8f95b5db7b3034e2309d32",
    "cpf-docs/work/current/CPF_20260731_QA32_NEXT_DEVELOPMENT_REQUIREMENTS.md":
        "e07cada024d4c4ffb8d7bcaae13241580a08fd93",
}
DOC_SYNC_MARKER = "## 1.0 최상위 Requirement 동기화 규칙"
LEDGER_MARKER = "## 2026-07-31 — Final Target 상세 Catalog·활성 정본 동기화"
STALE_CURRENT = "cpf-docs/work/current/CPF_20260731_QA32_NEXT_DEVELOPMENT_REQUIREMENTS.md"
HISTORY_POINTER = "cpf-docs/work/review/history/CPF_20260731_QA32_NEXT_DEVELOPMENT_REQUIREMENTS_SUPERSEDED.md"

def git_blob(path: Path) -> str:
    p = subprocess.run(["git", "hash-object", str(path)], text=True,
                       capture_output=True)
    if p.returncode != 0:
        raise RuntimeError(f"git hash-object failed: {path}: {p.stderr.strip()}")
    return p.stdout.strip()

def require_baseline_or_synced(root: Path, rel: str, marker: str | None = None):
    path = root / rel
    if not path.is_file():
        raise FileNotFoundError(rel)
    text = path.read_text(encoding="utf-8")
    if marker and marker in text:
        return "SYNCED"
    actual = git_blob(path)
    expected = EXPECTED_BLOBS[rel]
    if actual != expected:
        raise RuntimeError(
            f"baseline drift: {rel}; expected blob={expected}, actual={actual}. "
            "Do not overwrite another worker's changes; re-review and merge manually.")
    return "BASELINE"

def patch_documentation_standard(root: Path, apply: bool, failures: list[str]):
    rel = "cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md"
    path = root / rel
    try:
        state = require_baseline_or_synced(root, rel, DOC_SYNC_MARKER)
        text = path.read_text(encoding="utf-8")
        if state == "BASELINE":
            text = text.replace(
                "`CPF_FINAL_TARGET_REQUIREMENTS.md`",
                "`cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`")
            text = text.replace(
                "91_Gateway매뉴얼.md",
                "91_게이트웨이매뉴얼.md")
            insert = """\n\n## 1.0 최상위 Requirement 동기화 규칙\n\n- 최상위 제품 정본의 정확한 경로는 `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`다.\n- Canonical Product Requirement는 162개이며 상세 Owner·최소 목표·필수 완료 증명은 Final Target Catalog가 소유한다.\n- QA33의 138 Remediation Requirement와 414 Scenario는 작업 원장이며 Canonical Count에 합산하지 않는다.\n- Requirement 의미·Owner·완료 증명이 바뀌면 Final Target→Continuity Ledger→Architecture/Specification→Current Request→Guide 영향 순서로 갱신한다.\n- Guide는 Product Goal과 현재 구현·검증 상태를 구분하며, 구현되지 않은 목표를 현재 기능처럼 서술하지 않는다.\n- 문서 완료 상태는 `CPF_NO_PARTIAL_IMPLEMENTATION_COMPLETION_STANDARD.md`와 Final Target 공통 완료 축을 따른다.\n"""
            anchor = "# CPF 문서·가이드 작성 및 관리 표준\n"
            if anchor not in text:
                raise RuntimeError("documentation standard title marker missing")
            text = text.replace(anchor, anchor + insert, 1)
            if apply:
                path.write_text(text, encoding="utf-8")
        # Post conditions
        current = (path.read_text(encoding="utf-8") if apply else text)
        if DOC_SYNC_MARKER not in current:
            failures.append(f"missing sync marker:{rel}")
        if "`CPF_FINAL_TARGET_REQUIREMENTS.md`" in current:
            failures.append(f"bare final target path remains:{rel}")
        if "91_Gateway매뉴얼.md" in current:
            failures.append(f"wrong gateway guide filename remains:{rel}")
    except Exception as e:
        failures.append(str(e))

def patch_impact_ledger(root: Path, apply: bool, failures: list[str]):
    rel = "cpf-docs/work/state/CPF_CHANGE_IMPACT_AND_VALIDATION_LEDGER.md"
    path = root / rel
    try:
        state = require_baseline_or_synced(root, rel, LEDGER_MARKER)
        text = path.read_text(encoding="utf-8")
        if state == "BASELINE":
            block = """\n\n## 2026-07-31 — Final Target 상세 Catalog·활성 정본 동기화\n\n| 항목 | 내용 |\n|---|---|\n| Change ID | `CHG-20260731-FINAL-TARGET-DOC-SYNC-001` |\n| Review baseline | `c1f273f1ea4fafac6fd5d23bd837adfc38a04497` |\n| Canonical Requirement | 162개, 증감 없음 |\n| Legacy Alias | 8개, 완료율 제외 |\n| QA33 작업 원장 | Remediation Requirement 138, Scenario 414; Canonical Count와 별도 |\n| 직접 영향 | Governance, Architecture Decision, Current Request, Continuity, QA33 Package reference |\n| Large-file safe patch | Documentation Standard의 Final Target full path와 Gateway 실제 파일명, 동기화 규칙 삽입 |\n| Historical cleanup | QA32 Next Development를 current에서 해제하고 Git history pointer 보존 |\n| README/Guide | 수정하지 않음 |\n| Source/SQL/API Runtime | 변경 없음; 완료 Evidence로 사용 금지 |\n| 검증 | document consistency, QA33 integrity, manifest hash |\n| 사용자 Git write | 없음 |\n\n이 Change는 제품 Source 구현 완료를 의미하지 않는다. 기존 Ledger 본문은 역사 기록으로 아래에 그대로 보존한다.\n"""
            anchor = "# CPF Change Impact and Validation Ledger\n"
            if anchor not in text:
                raise RuntimeError("impact ledger title marker missing")
            text = text.replace(anchor, anchor + block, 1)
            if apply:
                path.write_text(text, encoding="utf-8")
        current = path.read_text(encoding="utf-8") if apply else text
        if LEDGER_MARKER not in current:
            failures.append(f"missing ledger checkpoint:{rel}")
    except Exception as e:
        failures.append(str(e))

def retire_stale_current(root: Path, apply: bool, failures: list[str]):
    stale = root / STALE_CURRENT
    pointer = root / HISTORY_POINTER
    if not pointer.is_file():
        failures.append(f"missing history pointer:{HISTORY_POINTER}")
        return
    if stale.exists():
        try:
            actual = git_blob(stale)
            expected = EXPECTED_BLOBS[STALE_CURRENT]
            if actual != expected:
                failures.append(
                    f"stale current changed by another worker; expected={expected}, actual={actual}")
                return
            if apply:
                stale.unlink()
        except Exception as e:
            failures.append(str(e))
    if (root / STALE_CURRENT).exists() and apply:
        failures.append(f"stale current still exists:{STALE_CURRENT}")

def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=".")
    mode = ap.add_mutually_exclusive_group(required=True)
    mode.add_argument("--apply", action="store_true")
    mode.add_argument("--check", action="store_true")
    args = ap.parse_args()
    root = Path(args.root).resolve()
    failures: list[str] = []
    apply = args.apply

    # Explicit exclusion guard.
    forbidden = [root / "README.md", root / "cpf-docs/guides"]
    patch_documentation_standard(root, apply, failures)
    patch_impact_ledger(root, apply, failures)
    retire_stale_current(root, apply, failures)

    # Check mode requires final state.
    if args.check:
        doc = root / "cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md"
        ledger = root / "cpf-docs/work/state/CPF_CHANGE_IMPACT_AND_VALIDATION_LEDGER.md"
        if doc.is_file():
            text = doc.read_text(encoding="utf-8")
            if DOC_SYNC_MARKER not in text: failures.append("documentation sync not applied")
            if "`CPF_FINAL_TARGET_REQUIREMENTS.md`" in text:
                failures.append("bare final target path remains")
            if "91_Gateway매뉴얼.md" in text:
                failures.append("wrong Gateway guide filename remains")
        if ledger.is_file() and LEDGER_MARKER not in ledger.read_text(encoding="utf-8"):
            failures.append("impact ledger checkpoint missing")
        if (root / STALE_CURRENT).exists():
            failures.append("superseded QA32 document remains in current")

    report = {
        "status": "PASS" if not failures else "FAIL",
        "mode": "apply" if apply else "check",
        "reviewBaseline": BASELINE,
        "failures": failures,
        "mutatesReadmeOrGuides": False,
        "gitCommitPushBranchTagPr": False,
    }
    print(json.dumps(report, ensure_ascii=False, indent=2))
    return 0 if not failures else 1

if __name__ == "__main__":
    raise SystemExit(main())
