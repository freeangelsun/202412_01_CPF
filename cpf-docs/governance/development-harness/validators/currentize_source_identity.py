#!/usr/bin/env python3
"""Currentize mutable Development Harness authorities to the exact current Product Source Identity.

The Product Source identity intentionally excludes mutable Harness current/evidence projections.
This utility computes the product identity from canonical source bytes, then updates only mutable
Harness authority/projection fields. Historical reviewer provenance is never rewritten.
"""
from __future__ import annotations

from pathlib import Path
import argparse
import csv
import importlib.util
import json
import re
import subprocess
import sys
from collections import Counter

ROOT = Path(__file__).resolve().parents[4]
H = ROOT / "cpf-docs/governance/development-harness"
C = H / "current"
SOURCE_STATE = ROOT / "cpf-tools/verification/tools/cpf-source-state.py"


def load_source_state():
    spec = importlib.util.spec_from_file_location("cpf_source_state", SOURCE_STATE)
    if not spec or not spec.loader:
        raise RuntimeError(f"Cannot load source state engine: {SOURCE_STATE}")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def csv_rows(path: Path) -> list[dict[str, str]]:
    with path.open(encoding="utf-8-sig", newline="") as handle:
        return list(csv.DictReader(handle))


def rewrite_csv_source_identity(path: Path, identity: str) -> bool:
    with path.open(encoding="utf-8-sig", newline="") as handle:
        reader = csv.DictReader(handle)
        fields = reader.fieldnames or []
        rows = list(reader)
    if "source_identity" not in fields:
        return False
    # Historical independent-review provenance must keep its original Source Identity.
    if path.name in {"CODEX_FINDING_CLOSURE.csv"}:
        return False
    changed = False
    for row in rows:
        if row.get("source_identity") and row["source_identity"] != identity:
            row["source_identity"] = identity
            changed = True
    if changed:
        with path.open("w", encoding="utf-8-sig", newline="") as handle:
            writer = csv.DictWriter(handle, fieldnames=fields, lineterminator="\n")
            writer.writeheader()
            writer.writerows(rows)
    return changed


def update_identity_file(path: Path, source: dict, git_sha: str) -> None:
    data = json.loads(path.read_text(encoding="utf-8"))
    data["finalReplayProductContentSha256"] = source["contentSha256"]
    data["finalReplayProductFileCount"] = source["fileCount"]
    data["currentWorkingTreeProductContentSha1"] = source["contentSha1"]
    data["currentWorkingTreeProductContentSha256"] = source["contentSha256"]
    data["currentWorkingTreeProductFileCount"] = source["fileCount"]
    data["currentWorkingTreeProductTotalBytes"] = source["totalBytes"]
    data["currentWorkingTreeGitSha"] = git_sha
    data["currentWorkingTreeStatus"] = "IN_PROGRESS"
    data["identityPolicy"] = source["identityPolicy"]
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def current_git_sha(root: Path) -> str:
    completed = subprocess.run(
        ["git", "rev-parse", "HEAD"],
        cwd=root,
        text=True,
        encoding="utf-8",
        errors="replace",
        capture_output=True,
        check=False,
    )
    return completed.stdout.strip() if completed.returncode == 0 else "UNAVAILABLE"


def update_text_current_identity(path: Path, identity: str) -> bool:
    if not path.is_file():
        return False
    text = path.read_text(encoding="utf-8")
    original = text
    labels = (
        "Current Product Source Identity",
        "Current Source Identity",
        "Product Source Identity",
        "Source Identity",
    )
    lines: list[str] = []
    for line in text.splitlines(keepends=True):
        if any(label in line for label in labels) and "Baseline" not in line:
            line = re.sub(r"[0-9a-f]{64}", identity, line, count=1)
        lines.append(line)
    text = "".join(lines)
    if text != original:
        path.write_text(text, encoding="utf-8")
        return True
    return False


def update_execution_summary(path: Path, source: dict) -> bool:
    if not path.is_file():
        return False
    data = json.loads(path.read_text(encoding="utf-8"))
    changed = False
    expected = {
        "sourceIdentitySha256": source["contentSha256"],
        "sourceFileCount": source["fileCount"],
    }
    for key, value in expected.items():
        if data.get(key) != value:
            data[key] = value
            changed = True
    if changed:
        path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return changed


def currentize_role_execution_summary(path: Path, source: dict, preserve_role_evidence: bool) -> bool:
    """Currentize a role-owned summary only when the invoking role is authorized to do so."""
    if preserve_role_evidence:
        return False
    return update_execution_summary(path, source)


def update_merge_control_state(path: Path, identity: str) -> bool:
    """Bind mutable Merge Control baseline to the exact current Product Source Identity.

    The state file lives under current/ and is excluded from Product Source Identity, so this
    currentization cannot create an identity feedback loop. Session provenance fields remain intact.
    """
    if not path.is_file():
        return False
    data = json.loads(path.read_text(encoding="utf-8"))
    if data.get("merge_baseline_source_identity") == identity:
        return False
    data["merge_baseline_source_identity"] = identity
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return True


def update_package_manifest(path: Path, source: dict) -> bool:
    """Currentize generated package projection identity/counts without changing completion semantics."""
    data = json.loads(path.read_text(encoding="utf-8"))
    requirements = csv_rows(C / "CANONICAL_PRODUCT_REQUIREMENTS.csv")
    work = csv_rows(C / "CURRENT_WORK_ITEM_REGISTRY.csv")
    roles = csv_rows(C / "ROLE_EXECUTION_LEDGER.csv")
    tests = csv_rows(C / "TEST_EXECUTION_LEDGER.csv")
    migrations = csv_rows(H / "CANONICAL_MIGRATION_MAP.csv")
    delete_manifest = csv_rows(H / "DELETE_MANIFEST.csv")
    tracking = [r for r in work if r.get("item_role", "TRACKING") == "TRACKING"]
    execution = [r for r in work if r.get("item_role") == "ROOT_CAUSE_EXECUTION"]
    delete_eligible = [
        r for r in delete_manifest
        if str(r.get("delete_eligible", "")).lower() in {"true", "1", "yes", "y"}
    ]
    protected_retain = [
        r for r in migrations
        if str(r.get("delete_eligible", "")).lower() not in {"true", "1", "yes", "y"}
    ]
    expected = {
        "currentSourceIdentity": source["contentSha256"],
        "sourceFileCount": source["fileCount"],
        "canonicalRequirementRows": len(requirements),
        "trackingWorkRows": len(tracking),
        "executionWorkRows": len(execution),
        "workItemRows": len(work),
        "roleRows": len(roles),
        "testRows": len(tests),
        "migrationRows": len(migrations),
        "deleteManifestRows": len(delete_eligible),
        "protectedRetainRows": len(protected_retain),
    }
    changed = False
    for key, value in expected.items():
        if data.get(key) != value:
            data[key] = value
            changed = True
    if changed:
        path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return changed


def render_status_projections(source: dict) -> list[Path]:
    requirements = csv_rows(C / "CANONICAL_PRODUCT_REQUIREMENTS.csv")
    trace = csv_rows(C / "CANONICAL_REQUIREMENT_TRACE.csv")
    bridge = csv_rows(C / "CURRENT_CANONICAL_DETAILED_BRIDGE.csv")
    work = csv_rows(C / "CURRENT_WORK_ITEM_REGISTRY.csv")
    status = csv_rows(C / "CURRENT_DEVELOPMENT_STATUS.csv")
    roles = csv_rows(C / "ROLE_EXECUTION_LEDGER.csv")
    tests = csv_rows(C / "TEST_EXECUTION_LEDGER.csv")
    controls = csv_rows(C / "CONTROL_EXECUTION_LEDGER.csv")
    findings = csv_rows(C / "PRODUCT_CONFORMANCE_FINDINGS.csv") if (C / "PRODUCT_CONFORMANCE_FINDINGS.csv").is_file() else []
    tracking = [r for r in work if r.get("item_role", "TRACKING") == "TRACKING"]
    execution = [r for r in work if r.get("item_role") == "ROOT_CAUSE_EXECUTION"]

    def counts(key: str) -> dict[str, int]:
        return dict(Counter(r.get(key, "") for r in status))

    work_lines = [
        "# CPF Current Work Request and Status — Generated Projection",
        "",
        "이 파일은 Authority가 아니라 `CURRENT_WORK_ITEM_REGISTRY.csv`와 `CURRENT_DEVELOPMENT_STATUS.csv`에서 생성되는 읽기용 Projection이다.",
        "",
        f"- Current Product Source Identity: `{source['contentSha256']}` / {source['fileCount']:,} product-source files",
        f"- Canonical Product Requirements: **{len(requirements)}**",
        f"- Canonical Trace: **{len(trace)} / {len(requirements)}**",
        f"- Detailed Bridge: **{len(bridge)}**",
        f"- Requirement/Tracking Work: **{len(tracking)}**",
        f"- Root Cause Execution WP: **{len(execution)}**",
        f"- Current Work Items: **{len(work)}**",
        f"- Role Ledger: **{len(roles)} = {len(work)} × 3**",
        f"- Test Execution Ledger: **{len(tests)}**",
        f"- Control Execution Ledger: **{len(controls)}**",
        f"- development_status: `{counts('development_status')}`",
        f"- verification_status: `{counts('verification_status')}`",
        f"- runtime_status: `{counts('runtime_status')}`",
        f"- overall_status: `{counts('overall_status')}`",
        f"- independent_reviewer_status: `{counts('independent_reviewer_status')}`",
        f"- qa_status: `{counts('qa_status')}`",
        "",
        "Root Cause Execution 순서는 `WP-H00 → WP-H01 → WP-H02 → WP-B01 → WP-B02 → WP-B03 → WP-CF01 → WP-RL01 → WP-DB01 → WP-CLI01 → WP-BAT01 → WP-ONE01 → WP-FE01 → WP-PF01 → WP-RL02 → WP-FIN01`이다.",
        "",
        "Static/Contract PASS는 Physical Requirement를 대체하지 않는다. 실제 Java25 Root Build/Publication, Fresh VS Code Error=0 Warning=0, DB3/Batch/One-WAS/Browser/Performance/Open Git/Fresh Replay, Independent Reviewer, QA가 미실행이면 전체 완료가 아니다.",
        "",
    ]
    open_findings = sum(1 for r in findings if (r.get("status") or "").upper() not in {"PASS", "CLOSED", "RESOLVED"})
    status_lines = [
        "# CPF Current Development Status — Generated Projection",
        "",
        f"- Source Identity: `{source['contentSha256']}`",
        f"- Canonical: **{len(requirements)}**, Trace: **{len(trace)}**, Bridge: **{len(bridge)}**",
        f"- Work: **{len(work)}** (Tracking {len(tracking)} / Execution {len(execution)}), Role: **{len(roles)}**, Test: **{len(tests)}**, Control: **{len(controls)}**",
        f"- development_status: `{counts('development_status')}`",
        f"- verification_status: `{counts('verification_status')}`",
        f"- runtime_status: `{counts('runtime_status')}`",
        f"- overall_status: `{counts('overall_status')}`",
        f"- Product Conformance OPEN: **{open_findings}**",
        "",
        "Authority는 CSV Registry/Ledger이며 이 Markdown은 읽기용 Projection이다.",
        "",
    ]
    targets = [
        (C / "CURRENT_WORK_REQUEST_AND_STATUS.md", "\n".join(work_lines)),
        (C / "CURRENT_DEVELOPMENT_STATUS.md", "\n".join(status_lines)),
    ]
    changed: list[Path] = []
    for path, body in targets:
        if not path.is_file() or path.read_text(encoding="utf-8") != body:
            path.write_text(body, encoding="utf-8")
            changed.append(path)
    return changed


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=str(ROOT))
    parser.add_argument("--skip-projections", action="store_true")
    parser.add_argument(
        "--preserve-role-evidence",
        action="store_true",
        help="Currentize shared authority/projections without rewriting DevGPT/QA-owned evidence.",
    )
    args = parser.parse_args()
    root = Path(args.root).resolve()
    if root != ROOT.resolve():
        raise SystemExit(f"This currentizer is source-bound to its repository root: expected={ROOT} actual={root}")

    source = load_source_state().snapshot(ROOT, "source")
    identity = source["contentSha256"]
    git_sha = current_git_sha(ROOT)
    changed: list[str] = []
    for path in sorted(C.glob("*.csv")):
        if rewrite_csv_source_identity(path, identity):
            changed.append(path.relative_to(ROOT).as_posix())
    for path in (H / "SOURCE_IDENTITY.json", C / "SOURCE_IDENTITY.json"):
        update_identity_file(path, source, git_sha)
        changed.append(path.relative_to(ROOT).as_posix())

    merge_state = C / "CURRENT_MERGE_CONTROL_STATE.json"
    if update_merge_control_state(merge_state, identity):
        changed.append(merge_state.relative_to(ROOT).as_posix())

    package_manifest = C / "PACKAGE_MANIFEST.json"
    if package_manifest.is_file() and update_package_manifest(package_manifest, source):
        changed.append(package_manifest.relative_to(ROOT).as_posix())

    for projection in (
        C / "CPF_DEVELOPMENT_HANDOVER.md",
        C / "TEST_AND_EVIDENCE.md",
        C / "CODEX_NEXT_WORK_INSTRUCTION.md",
        C / "QA_REWORK_REQUEST.md",
        C / "CODEX_RESULT_TO_NEXT_WORK_TRACE.md",
    ):
        if update_text_current_identity(projection, identity):
            changed.append(projection.relative_to(ROOT).as_posix())

    execution_summary = H / "evidence/devgpt/current/executions/DEVGPT_CURRENT_EXECUTION_SUMMARY.json"
    if currentize_role_execution_summary(execution_summary, source, args.preserve_role_evidence):
        changed.append(execution_summary.relative_to(ROOT).as_posix())

    for projection in render_status_projections(source):
        changed.append(projection.relative_to(ROOT).as_posix())

    if not args.skip_projections:
        cp = subprocess.run(
            [sys.executable, "-B", str(H / "validators/generate_detailed_review.py")],
            cwd=ROOT,
            text=True,
            encoding="utf-8",
            errors="replace",
            capture_output=True,
            check=False,
        )
        if cp.returncode != 0:
            print(cp.stdout, end="")
            print(cp.stderr, end="", file=sys.stderr)
            return cp.returncode
        print(cp.stdout, end="")

    after = load_source_state().snapshot(ROOT, "source")
    if after["contentSha256"] != identity:
        print(
            "HARNESS_SOURCE_IDENTITY_CURRENTIZE=FAIL "
            f"source_mutated_during_currentize before={identity} after={after['contentSha256']}"
        )
        return 1
    print(
        "HARNESS_SOURCE_IDENTITY_CURRENTIZE=PASS "
        f"identity={identity} files={source['fileCount']} mutable_files_updated={len(changed)}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
