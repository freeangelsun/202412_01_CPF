#!/usr/bin/env python3
"""Canonical CPF requirement/scenario/execution traceability gate.

This gate intentionally consumes the split logical masters.  It preserves the
legacy command-line options used by CI, but it no longer hardcodes QA round
counts or treats a split index row as a requirement row.
"""
from __future__ import annotations

import argparse
import csv
import importlib.util
import json
import re
import subprocess
import sys
from collections import Counter
from pathlib import Path

SHA_RE = re.compile(r"^[0-9a-f]{40}$")
VALID_DEVELOPMENT = {"완료", "부분 구현", "미구현", "미검증", "실패", "재확인 필요"}
VALID_VERIFICATION = {"완료", "미검증", "실패", "재확인 필요"}


class GateError(RuntimeError):
    pass


def read_csv(path: Path) -> tuple[list[str], list[dict[str, str]]]:
    """Read a UTF-8 CSV while preserving the legacy verifier API."""
    if not path.is_file():
        raise GateError(f"required CSV missing: {path}")
    with path.open(encoding="utf-8-sig", newline="") as handle:
        reader = csv.DictReader(handle)
        rows = [{key: (value or "").strip() for key, value in row.items()} for row in reader]
        return list(reader.fieldnames or []), rows


def require_columns(path: Path, columns: list[str], fieldnames: list[str]) -> None:
    missing = [column for column in columns if column not in fieldnames]
    if missing:
        raise GateError(f"{path}: missing columns={missing}")


def unique_ids(path: Path, rows: list[dict[str, str]], key: str) -> list[str]:
    values = [row.get(key, "") for row in rows]
    if any(not value for value in values):
        raise GateError(f"{path}: blank {key}")
    duplicates = [value for value, count in Counter(values).items() if count > 1]
    if duplicates:
        raise GateError(f"{path}: duplicate {key}={duplicates[:20]}")
    return values


def check_statuses(path: Path, rows: list[dict[str, str]]) -> None:
    for row_no, row in enumerate(rows, start=2):
        development = row.get("development_status", "")
        verification = row.get("verification_status", "")
        if development not in VALID_DEVELOPMENT:
            raise GateError(f"{path}:{row_no}: invalid development_status={development!r}")
        if verification not in VALID_VERIFICATION:
            raise GateError(f"{path}:{row_no}: invalid verification_status={verification!r}")
        if verification == "완료" and development != "완료":
            raise GateError(
                f"{path}:{row_no}: verification 완료 cannot precede development 완료 "
                f"({development!r})"
            )


def load_split_module(script_dir: Path):
    path = script_dir / "verify-cpf-split-master-dataset.py"
    if not path.is_file():
        raise GateError(f"split master verifier missing: {path}")
    spec = importlib.util.spec_from_file_location("cpf_split_master_gate", path)
    if spec is None or spec.loader is None:
        raise GateError(f"cannot load split master verifier: {path}")
    module = importlib.util.module_from_spec(spec)
    sys.modules[spec.name] = module
    spec.loader.exec_module(module)
    return module


def git_output(root: Path, *args: str) -> str:
    completed = subprocess.run(
        ["git", "-C", str(root), *args], check=False, text=True,
        stdout=subprocess.PIPE, stderr=subprocess.PIPE,
    )
    if completed.returncode != 0:
        raise GateError(
            f"git {' '.join(args)} failed(exit={completed.returncode}): {completed.stderr.strip()}"
        )
    return completed.stdout.strip()


def check_git(root: Path, expected_sha: str | None, require_clean: bool) -> str:
    git_dir = root / ".git"
    if not git_dir.exists():
        if expected_sha or require_clean:
            raise GateError(f"Git working tree required: {root}")
        return "UNAVAILABLE"
    head = git_output(root, "rev-parse", "HEAD").lower()
    if expected_sha:
        expected_sha = expected_sha.lower()
        if not SHA_RE.fullmatch(expected_sha):
            raise GateError(f"Expected SHA must be 40 lowercase hex: {expected_sha}")
        if head != expected_sha:
            raise GateError(f"exact SHA mismatch: expected={expected_sha} actual={head}")
    if require_clean:
        dirty = git_output(root, "status", "--porcelain=v1", "--untracked-files=all")
        if dirty:
            raise GateError("working tree is not clean:\n" + dirty)
    return head


def split_repo_paths(value: str) -> list[str]:
    return [item.strip().replace("\\", "/") for item in value.split(";") if item.strip()]


def require_existing_repo_paths(root: Path, matrix: Path, row_no: int, requirement_id: str, column: str, value: str) -> None:
    entries = split_repo_paths(value)
    if not entries:
        raise GateError(f"{matrix}:{row_no}: completed {requirement_id} missing {column}")
    for entry in entries:
        candidate = Path(entry)
        if candidate.is_absolute() or re.match(r"^[A-Za-z]:[/\\]", entry):
            raise GateError(f"{matrix}:{row_no}: absolute path forbidden in {column}: {entry}")
        if any(part == ".." for part in candidate.parts):
            raise GateError(f"{matrix}:{row_no}: parent traversal forbidden in {column}: {entry}")
        resolved = (root / candidate).resolve()
        try:
            resolved.relative_to(root)
        except ValueError as exc:
            raise GateError(f"{matrix}:{row_no}: path escapes repository in {column}: {entry}") from exc
        if not resolved.is_file():
            raise GateError(f"{matrix}:{row_no}: referenced file missing in {column}: {entry}")


def check_result_matrix(
    root: Path,
    path: Path,
    required_ids: set[str],
    release: bool,
    expected_sha: str | None,
) -> tuple[int, int]:
    """Preserve the established strict result-matrix contract for external callers.

    The canonical split-master main flow uses ``check_optional_result_matrix`` because
    the integrated result matrix may be sparse during development.  Existing CI and
    tests still call this strict API directly, so coverage, source type, paths, status,
    and exact SHA checks remain unchanged.
    """
    fields, rows = read_csv(path)
    require_columns(
        path,
        [
            "requirement_id",
            "source_type",
            "development_status",
            "verification_status",
            "source_paths",
            "consumer_paths",
            "test_paths",
            "evidence_paths",
            "source_sha",
            "result_sha",
        ],
        fields,
    )
    ids = unique_ids(path, rows, "requirement_id")
    missing = sorted(required_ids - set(ids))
    extras = sorted(set(ids) - required_ids)
    if missing or extras:
        raise GateError(f"{path}: result coverage mismatch missing={missing[:20]} extras={extras[:20]}")
    check_statuses(path, rows)
    completed = 0
    verified = 0
    for row_no, row in enumerate(rows, start=2):
        requirement_id = row["requirement_id"]
        expected_source_type = "SELF" if requirement_id.startswith("CPF-SELF-DEV-") else "QA"
        if row["source_type"] != expected_source_type:
            raise GateError(f"{path}:{row_no}: source_type mismatch for {requirement_id}")
        development = row["development_status"]
        verification = row["verification_status"]
        if development == "완료":
            completed += 1
            for column in ("source_paths", "consumer_paths", "test_paths"):
                require_existing_repo_paths(root, path, row_no, requirement_id, column, row[column])
        if verification == "완료":
            verified += 1
            require_existing_repo_paths(
                root, path, row_no, requirement_id, "evidence_paths", row["evidence_paths"]
            )
            source_sha = row["source_sha"]
            result_sha = row["result_sha"]
            if not SHA_RE.fullmatch(source_sha) or not SHA_RE.fullmatch(result_sha):
                raise GateError(f"{path}:{row_no}: verified row requires exact 40-char SHA")
            if source_sha != result_sha:
                raise GateError(f"{path}:{row_no}: source_sha/result_sha mismatch")
            if expected_sha and source_sha != expected_sha:
                raise GateError(f"{path}:{row_no}: evidence SHA differs from expected SHA")
        if release and (development != "완료" or verification != "완료"):
            raise GateError(
                f"release closure blocked by {requirement_id}: "
                f"development={development}, verification={verification}"
            )
    return completed, verified


def check_optional_result_matrix(
    root: Path,
    matrix_path: Path,
    canonical_ids: set[str],
    expected_sha: str | None,
) -> dict[str, int]:
    if not matrix_path.is_file():
        return {"rows": 0, "completed": 0, "verified": 0}
    with matrix_path.open(encoding="utf-8-sig", newline="") as handle:
        reader = csv.DictReader(handle)
        fields = list(reader.fieldnames or [])
        rows = [{key: (value or "").strip() for key, value in row.items()} for row in reader]
    required = {"requirement_id", "development_status", "verification_status"}
    missing = sorted(required - set(fields))
    if missing:
        raise GateError(f"{matrix_path}: missing columns={missing}")
    ids = [row["requirement_id"] for row in rows]
    duplicates = [value for value, count in Counter(ids).items() if count > 1]
    if duplicates:
        raise GateError(f"{matrix_path}: duplicate requirement_id={duplicates[:20]}")
    unknown = sorted(set(ids) - canonical_ids)
    if unknown:
        raise GateError(f"{matrix_path}: unknown canonical requirement IDs={unknown[:20]}")
    completed = verified = 0
    for row_no, row in enumerate(rows, start=2):
        requirement_id = row["requirement_id"]
        if row["development_status"] == "완료":
            completed += 1
            for column in ("source_paths", "consumer_paths", "test_paths"):
                if column in fields:
                    require_existing_repo_paths(root, matrix_path, row_no, requirement_id, column, row[column])
        if row["verification_status"] == "완료":
            verified += 1
            if row["development_status"] != "완료":
                raise GateError(f"{matrix_path}:{row_no}: verification 완료 before development 완료")
            if "evidence_paths" in fields:
                require_existing_repo_paths(root, matrix_path, row_no, requirement_id, "evidence_paths", row["evidence_paths"])
            for column in ("source_sha", "result_sha"):
                if column in fields and not SHA_RE.fullmatch(row[column]):
                    raise GateError(f"{matrix_path}:{row_no}: verified row requires exact {column}")
            if "source_sha" in fields and "result_sha" in fields and row["source_sha"] != row["result_sha"]:
                raise GateError(f"{matrix_path}:{row_no}: source_sha/result_sha mismatch")
            if expected_sha and "source_sha" in fields and row["source_sha"] != expected_sha:
                raise GateError(f"{matrix_path}:{row_no}: evidence SHA differs from expected SHA")
    return {"rows": len(rows), "completed": completed, "verified": verified}


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path.cwd())
    parser.add_argument("--expected-sha")
    parser.add_argument("--require-clean", action="store_true")
    parser.add_argument("--release", action="store_true")
    parser.add_argument("--scope-limit", type=int, default=10027)
    parser.add_argument("--json-output", type=Path)
    parser.add_argument(
        "--result-matrix",
        default="cpf-docs/quality/CPF_20260801_INTEGRATED_RESULT_MATRIX.csv",
    )
    args = parser.parse_args()
    root = args.root.resolve()
    head = check_git(root, args.expected_sha, args.require_clean)
    split = load_split_module(Path(__file__).resolve().parent)

    results = {}
    rows = {}
    for kind in ("requirement", "scenario", "execution"):
        result, logical_rows, _ = split.validate_dataset(root, kind, False)
        results[kind] = result
        rows[kind] = logical_rows
    split.validate_statuses(rows["requirement"], rows["scenario"], rows["execution"])
    cross_links = split.validate_cross_links(rows["requirement"], rows["scenario"], rows["execution"])
    scope = split.build_scope(rows["execution"], rows["scenario"], args.scope_limit)

    canonical_ids = {row["requirement_id"] for row in rows["requirement"]}
    matrix = Path(args.result_matrix)
    if not matrix.is_absolute():
        matrix = root / matrix
    result_matrix = check_optional_result_matrix(root, matrix, canonical_ids, args.expected_sha)

    if args.release:
        blocked = [
            row["requirement_id"] for row in rows["requirement"]
            if row.get("development_status") != "완료" or row.get("verification_status") != "완료"
        ]
        if blocked:
            raise GateError(
                f"release closure blocked by {len(blocked)} canonical requirements; first={blocked[:20]}"
            )

    output = {
        "status": "PASS",
        "head": head,
        "requirement_count": len(rows["requirement"]),
        "scenario_count": len(rows["scenario"]),
        "execution_count": len(rows["execution"]),
        "cross_links": cross_links,
        "scope": scope,
        "result_matrix": result_matrix,
        "release": args.release,
    }
    if args.json_output:
        path = args.json_output
        if not path.is_absolute():
            path = root / path
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(output, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("[PASS] CPF requirement traceability " + json.dumps(output, ensure_ascii=False, separators=(",", ":")))
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (GateError, Exception) as error:
        # Split validator raises its own ValidationError type; keep one fail-closed exit path.
        print(f"[FAIL] {error}", file=sys.stderr)
        raise SystemExit(1)
