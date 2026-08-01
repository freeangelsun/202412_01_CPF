#!/usr/bin/env python3
"""Fail-closed CPF SELF/QA36 requirement structure and release traceability gate."""
from __future__ import annotations

import argparse
import csv
import json
import re
import subprocess
import sys
from collections import Counter
from pathlib import Path

VALID_DEVELOPMENT = {"완료", "부분 구현", "미구현", "미검증", "실패", "재확인 필요"}
VALID_VERIFICATION = {"완료", "미검증", "실패", "재확인 필요"}
SELF_IDS = [f"CPF-SELF-DEV-{index:03d}" for index in range(1, 31)]
SHA_RE = re.compile(r"^[0-9a-f]{40}$")


class GateError(RuntimeError):
    pass


def read_csv(path: Path) -> tuple[list[str], list[dict[str, str]]]:
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


def git_output(root: Path, *args: str) -> str:
    completed = subprocess.run(
        ["git", "-C", str(root), *args],
        check=False,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    if completed.returncode != 0:
        raise GateError(f"git {' '.join(args)} failed(exit={completed.returncode}): {completed.stderr.strip()}")
    return completed.stdout.strip()


def check_git(root: Path, expected_sha: str | None, require_clean: bool) -> str:
    if not (root / ".git").exists():
        if expected_sha or require_clean:
            raise GateError(f"Git working tree required: {root}")
        return "UNAVAILABLE"
    head = git_output(root, "rev-parse", "HEAD")
    if expected_sha:
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
            require_existing_repo_paths(root, path, row_no, requirement_id, "evidence_paths", row["evidence_paths"])
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


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path.cwd())
    parser.add_argument("--expected-sha")
    parser.add_argument("--require-clean", action="store_true")
    parser.add_argument("--release", action="store_true")
    parser.add_argument(
        "--result-matrix",
        default="cpf-docs/quality/CPF_20260801_INTEGRATED_RESULT_MATRIX.csv",
    )
    args = parser.parse_args()
    root = args.root.resolve()

    head = check_git(root, args.expected_sha, args.require_clean)

    self_path = root / "cpf-docs/quality/CPF_SELF_DEVELOPMENT_REQUIREMENT_MATRIX.csv"
    self_fields, self_rows = read_csv(self_path)
    require_columns(
        self_path,
        ["requirement_id", "source_type", "development_status", "verification_status"],
        self_fields,
    )
    self_ids = unique_ids(self_path, self_rows, "requirement_id")
    if self_ids != SELF_IDS:
        raise GateError(f"SELF IDs must be exactly continuous CPF-SELF-DEV-001..030: {self_ids}")
    if any(row["source_type"] != "SELF" for row in self_rows):
        raise GateError("SELF matrix contains non-SELF source_type")
    if any(re.search(r"QA\d+", json.dumps(row, ensure_ascii=False)) for row in self_rows):
        raise GateError("SELF matrix contains QA round identifier")
    check_statuses(self_path, self_rows)

    gap_path = root / "cpf-docs/quality/CPF_20260801_QA36_ACTIVE_GAP_REQUIREMENT_MATRIX.csv"
    gap_fields, gap_rows = read_csv(gap_path)
    require_columns(
        gap_path,
        ["requirement_id", "development_status", "verification_status"],
        gap_fields,
    )
    gap_ids = unique_ids(gap_path, gap_rows, "requirement_id")
    if len(gap_rows) != 85:
        raise GateError(f"QA36 active gap count must be 85, actual={len(gap_rows)}")
    check_statuses(gap_path, gap_rows)

    canonical_path = root / "cpf-docs/quality/CPF_20260801_QA36_CANONICAL_162_REQUIREMENT_MATRIX.csv"
    canonical_fields, canonical_rows = read_csv(canonical_path)
    require_columns(
        canonical_path,
        ["requirement_id", "development_status", "verification_status"],
        canonical_fields,
    )
    canonical_ids = unique_ids(canonical_path, canonical_rows, "requirement_id")
    if len(canonical_rows) != 162:
        raise GateError(f"Canonical requirement count must be 162, actual={len(canonical_rows)}")
    check_statuses(canonical_path, canonical_rows)

    scenario_path = root / "cpf-docs/quality/CPF_20260801_QA36_CANONICAL_MANDATORY_SCENARIO_MATRIX.csv"
    scenario_fields, scenario_rows = read_csv(scenario_path)
    require_columns(
        scenario_path,
        [
            "scenario_id",
            "requirement_id",
            "precondition",
            "action",
            "expected_result",
            "required_evidence",
            "development_status",
            "verification_status",
        ],
        scenario_fields,
    )
    unique_ids(scenario_path, scenario_rows, "scenario_id")
    check_statuses(scenario_path, scenario_rows)
    orphan_scenarios = sorted({row["requirement_id"] for row in scenario_rows} - set(canonical_ids))
    if orphan_scenarios:
        raise GateError(f"scenario references unknown canonical requirements={orphan_scenarios[:20]}")
    scenario_counts = Counter(row["requirement_id"] for row in scenario_rows)
    wrong_axis_counts = {key: count for key, count in scenario_counts.items() if count != 17}
    if wrong_axis_counts:
        raise GateError(f"Canonical scenario axis count must be 17: {dict(list(wrong_axis_counts.items())[:20])}")
    for row_no, row in enumerate(scenario_rows, start=2):
        for column in ("precondition", "action", "expected_result", "required_evidence"):
            value = row[column]
            if len(value) < 12 or value in {"TODO", "TBD", "미정", "미수집"}:
                raise GateError(f"{scenario_path}:{row_no}: non-executable {column}={value!r}")

    result_path = root / args.result_matrix
    required_result_ids = set(SELF_IDS) | set(gap_ids)
    completed, verified = check_result_matrix(root, result_path, required_result_ids, args.release, args.expected_sha)

    print(
        "[PASS] CPF requirement traceability "
        f"head={head} self={len(self_rows)} activeGap={len(gap_rows)} "
        f"canonical={len(canonical_rows)} scenarios={len(scenario_rows)} "
        f"resultRows={len(required_result_ids)} completed={completed} verified={verified} "
        f"release={args.release}"
    )
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except GateError as error:
        print(f"[FAIL] {error}", file=sys.stderr)
        raise SystemExit(1)
