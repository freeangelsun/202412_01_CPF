#!/usr/bin/env python3
"""Fail-closed completion gate for the full CPF QA campaign.

The gate never infers QA completion from Work Package or common Evidence alone.
It requires one Requirement-status row for every logical Requirement and one
Scenario-status row for every logical Scenario, verifies exact-HEAD Evidence,
and applies separate Campaign-complete and Product-pass rules.
"""
from __future__ import annotations

import argparse
import csv
import hashlib
import json
import re
from collections import Counter, defaultdict
from pathlib import Path
from typing import Iterable

SHA_RE = re.compile(r"^[0-9a-f]{40}$")
ALLOWED_QA_RESULT = {"통과", "미통과"}
ALLOWED_OVERALL = {"완료", "부분 구현", "미구현", "미검증", "실패", "재확인 필요"}
ALLOWED_ROLE = {"완료", "미완료", "재개발 요청", "재검수 요청", "해당 없음"}
INDEX_REQUIRED = {
    "part_sequence", "part_path", "part_record_count", "first_record_id",
    "last_record_id", "size_bytes", "sha256", "logical_record_count",
}
REQUIREMENT_LEDGER_REQUIRED = {
    "requirement_id", "execution_order", "work_package_id", "acceptance_criteria",
    "scenario_ids", "owner_module", "owner_package", "source_paths",
    "actual_consumer", "call_path", "verification_method", "verification_level",
    "verified_acceptance", "unverified_acceptance", "QA_검수여부", "QA_검수결과",
    "QA_검수evidence", "QA_재개발요청여부", "개발GPT_상태",
    "개발GPT_자체검수상태", "Codex_검수보완상태", "development_status",
    "verification_status", "baseline_sha", "evidence_path", "evidence_sha256",
    "open_issue", "next_action", "state_revision", "updated_at", "updated_by",
}
SCENARIO_LEDGER_REQUIRED = {
    "scenario_id", "linked_requirement_id", "work_package_id", "scenario_type",
    "expected_result", "failure_criteria", "verification_level", "QA_검수여부",
    "QA_검수결과", "baseline_sha", "evidence_path", "evidence_sha256",
    "unverified_scope", "state_revision", "updated_at", "updated_by",
}


class CompletionError(RuntimeError):
    pass


def read_csv(path: Path) -> tuple[list[str], list[dict[str, str]]]:
    if not path.is_file():
        raise CompletionError(f"missing CSV: {path}")
    with path.open(encoding="utf-8-sig", newline="") as handle:
        reader = csv.DictReader(handle)
        fields = list(reader.fieldnames or [])
        rows = [{key: (value or "").strip() for key, value in row.items()} for row in reader]
    return fields, rows


def digest(path: Path) -> str:
    value = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            value.update(chunk)
    return value.hexdigest()


def load_split_master(root: Path, stem: str, id_field: str) -> list[dict[str, str]]:
    index_path = root / "cpf-docs/work/current" / f"{stem}.csv"
    fields, index = read_csv(index_path)
    missing = INDEX_REQUIRED - set(fields)
    if missing:
        raise CompletionError(f"{index_path}: missing index columns {sorted(missing)}")
    if not index:
        raise CompletionError(f"{index_path}: empty index")
    sequences = [int(row["part_sequence"]) for row in index]
    if sequences != list(range(1, len(index) + 1)):
        raise CompletionError(f"{index_path}: non-contiguous part_sequence")
    declared_counts = {int(row["logical_record_count"]) for row in index}
    if len(declared_counts) != 1:
        raise CompletionError(f"{index_path}: inconsistent logical_record_count")

    rows: list[dict[str, str]] = []
    seen: set[str] = set()
    for item in index:
        relative = item["part_path"]
        part = (root / relative).resolve()
        if root.resolve() not in part.parents or not part.is_file():
            raise CompletionError(f"{index_path}: unsafe/missing part {relative}")
        part_fields, part_rows = read_csv(part)
        if id_field not in part_fields:
            raise CompletionError(f"{relative}: missing {id_field}")
        ids = [row[id_field] for row in part_rows]
        if any(not value for value in ids):
            raise CompletionError(f"{relative}: blank {id_field}")
        duplicates = [value for value in ids if value in seen]
        if duplicates:
            raise CompletionError(f"{relative}: duplicate {id_field} {duplicates[0]}")
        seen.update(ids)
        if len(part_rows) != int(item["part_record_count"]):
            raise CompletionError(f"{relative}: part count mismatch")
        if ids[0] != item["first_record_id"] or ids[-1] != item["last_record_id"]:
            raise CompletionError(f"{relative}: first/last id mismatch")
        if part.stat().st_size != int(item["size_bytes"]):
            raise CompletionError(f"{relative}: size mismatch")
        if digest(part) != item["sha256"]:
            raise CompletionError(f"{relative}: SHA-256 mismatch")
        rows.extend(part_rows)
    declared = next(iter(declared_counts))
    if len(rows) != declared:
        raise CompletionError(f"{index_path}: logical count mismatch declared={declared} actual={len(rows)}")
    return rows


def unique_map(rows: Iterable[dict[str, str]], key: str, label: str) -> dict[str, dict[str, str]]:
    result: dict[str, dict[str, str]] = {}
    for line, row in enumerate(rows, 2):
        value = row.get(key, "")
        if not value:
            raise CompletionError(f"{label}:{line}: blank {key}")
        if value in result:
            raise CompletionError(f"{label}:{line}: duplicate {key}={value}")
        result[value] = row
    return result


def split_values(value: str) -> set[str]:
    return {item.strip() for item in re.split(r"[;,]", value) if item.strip()}


def verify_evidence(root: Path, row: dict[str, str], label: str) -> None:
    relative = row["evidence_path"]
    expected = row["evidence_sha256"]
    if not relative or not expected:
        raise CompletionError(f"{label}: evidence path/hash required")
    if not re.fullmatch(r"[0-9a-f]{64}", expected):
        raise CompletionError(f"{label}: invalid evidence SHA-256")
    evidence = (root / relative).resolve()
    if root.resolve() not in evidence.parents or not evidence.is_file():
        raise CompletionError(f"{label}: unsafe/missing evidence {relative}")
    actual = digest(evidence)
    if actual != expected:
        raise CompletionError(f"{label}: evidence hash mismatch expected={expected} actual={actual}")


def require_nonblank(row: dict[str, str], columns: Iterable[str], label: str) -> None:
    missing = [column for column in columns if not row.get(column, "")]
    if missing:
        raise CompletionError(f"{label}: blank required fields {missing}")


def verify(args: argparse.Namespace) -> dict:
    root = Path(args.root).resolve()
    expected_sha = args.expected_sha
    if not SHA_RE.fullmatch(expected_sha):
        raise CompletionError("expected SHA must be 40 lowercase hex")

    requirements = load_split_master(root, "CPF_REQUIREMENT_MASTER", "requirement_id")
    scenarios = load_split_master(root, "CPF_SCENARIO_MASTER", "scenario_id")
    execution = load_split_master(root, "CPF_EXECUTION_SEQUENCE", "execution_order")
    requirement_master = unique_map(requirements, "requirement_id", "Requirement Master")
    scenario_master = unique_map(scenarios, "scenario_id", "Scenario Master")
    execution_by_requirement = unique_map(execution, "requirement_id", "Execution Sequence")
    if set(execution_by_requirement) != set(requirement_master):
        missing = sorted(set(requirement_master) - set(execution_by_requirement))
        extra = sorted(set(execution_by_requirement) - set(requirement_master))
        raise CompletionError(f"execution/requirement mismatch missing={missing[:5]} extra={extra[:5]}")

    linked_scenarios: dict[str, set[str]] = defaultdict(set)
    for scenario_id, row in scenario_master.items():
        requirement_id = row.get("linked_requirement_id", "")
        if requirement_id not in requirement_master:
            raise CompletionError(f"{scenario_id}: unknown linked requirement {requirement_id}")
        linked_scenarios[requirement_id].add(scenario_id)
    no_scenario = [requirement_id for requirement_id in requirement_master if not linked_scenarios[requirement_id]]
    if no_scenario:
        raise CompletionError(f"requirements without scenarios: {no_scenario[:5]}")

    requirement_ledger_path = root / args.requirement_ledger
    requirement_fields, requirement_rows = read_csv(requirement_ledger_path)
    missing_columns = REQUIREMENT_LEDGER_REQUIRED - set(requirement_fields)
    if missing_columns:
        raise CompletionError(f"{requirement_ledger_path}: missing columns {sorted(missing_columns)}")
    requirement_ledger = unique_map(requirement_rows, "requirement_id", str(requirement_ledger_path))
    if set(requirement_ledger) != set(requirement_master):
        missing = sorted(set(requirement_master) - set(requirement_ledger))
        extra = sorted(set(requirement_ledger) - set(requirement_master))
        raise CompletionError(f"Requirement ledger coverage mismatch missing={missing[:5]} extra={extra[:5]}")

    scenario_ledger_path = root / args.scenario_ledger
    scenario_fields, scenario_rows = read_csv(scenario_ledger_path)
    missing_columns = SCENARIO_LEDGER_REQUIRED - set(scenario_fields)
    if missing_columns:
        raise CompletionError(f"{scenario_ledger_path}: missing columns {sorted(missing_columns)}")
    scenario_ledger = unique_map(scenario_rows, "scenario_id", str(scenario_ledger_path))
    if set(scenario_ledger) != set(scenario_master):
        missing = sorted(set(scenario_master) - set(scenario_ledger))
        extra = sorted(set(scenario_ledger) - set(scenario_master))
        raise CompletionError(f"Scenario ledger coverage mismatch missing={missing[:5]} extra={extra[:5]}")

    requirement_results = Counter()
    work_packages: dict[str, list[str]] = defaultdict(list)
    for requirement_id, master in requirement_master.items():
        row = requirement_ledger[requirement_id]
        label = f"Requirement {requirement_id}"
        sequence = execution_by_requirement[requirement_id]
        if row["execution_order"] != sequence["execution_order"]:
            raise CompletionError(f"{label}: execution_order mismatch")
        if row["work_package_id"] != sequence["work_package_id"]:
            raise CompletionError(f"{label}: work_package_id mismatch")
        if row["baseline_sha"] != expected_sha:
            raise CompletionError(f"{label}: stale baseline SHA {row['baseline_sha']}")
        if row["QA_검수여부"] != "예":
            raise CompletionError(f"{label}: QA review not performed")
        if row["QA_검수결과"] not in ALLOWED_QA_RESULT:
            raise CompletionError(f"{label}: invalid QA result {row['QA_검수결과']}")
        if row["development_status"] not in ALLOWED_OVERALL or row["verification_status"] not in ALLOWED_OVERALL:
            raise CompletionError(f"{label}: invalid overall status")
        for column in ("개발GPT_상태", "개발GPT_자체검수상태", "Codex_검수보완상태"):
            if row[column] not in ALLOWED_ROLE:
                raise CompletionError(f"{label}: invalid {column}={row[column]}")
        require_nonblank(
            row,
            (
                "acceptance_criteria", "owner_module", "owner_package", "source_paths",
                "actual_consumer", "call_path", "verification_method", "verification_level",
                "verified_acceptance", "QA_검수evidence", "state_revision", "updated_at", "updated_by",
            ),
            label,
        )
        declared_scenarios = split_values(row["scenario_ids"])
        if declared_scenarios != linked_scenarios[requirement_id]:
            raise CompletionError(
                f"{label}: scenario set mismatch expected={sorted(linked_scenarios[requirement_id])} "
                f"actual={sorted(declared_scenarios)}"
            )
        verify_evidence(root, row, label)
        requirement_results[row["QA_검수결과"]] += 1
        work_packages[row["work_package_id"]].append(requirement_id)

        if args.mode == "product-pass":
            if row["QA_검수결과"] != "통과":
                raise CompletionError(f"{label}: product pass requires QA 통과")
            if row["development_status"] != "완료" or row["verification_status"] != "완료":
                raise CompletionError(f"{label}: product pass requires completed development and verification")
            if row["개발GPT_상태"] not in {"완료", "해당 없음"}:
                raise CompletionError(f"{label}: developer review still open")
            if row["개발GPT_자체검수상태"] not in {"완료", "해당 없음"}:
                raise CompletionError(f"{label}: developer self-review still open")
            if row["Codex_검수보완상태"] not in {"완료", "해당 없음"}:
                raise CompletionError(f"{label}: Codex review still open")
            if row["unverified_acceptance"] or row["open_issue"] or row["next_action"]:
                raise CompletionError(f"{label}: unresolved scope/issue/action remains")
            if row["QA_재개발요청여부"] not in {"아니오", "해당 없음"}:
                raise CompletionError(f"{label}: rework request remains open")

    scenario_results = Counter()
    for scenario_id, master in scenario_master.items():
        row = scenario_ledger[scenario_id]
        label = f"Scenario {scenario_id}"
        requirement_id = master["linked_requirement_id"]
        if row["linked_requirement_id"] != requirement_id:
            raise CompletionError(f"{label}: linked requirement mismatch")
        expected_wp = execution_by_requirement[requirement_id]["work_package_id"]
        if row["work_package_id"] != expected_wp:
            raise CompletionError(f"{label}: work package mismatch")
        if row["baseline_sha"] != expected_sha:
            raise CompletionError(f"{label}: stale baseline SHA")
        if row["QA_검수여부"] != "예" or row["QA_검수결과"] not in ALLOWED_QA_RESULT:
            raise CompletionError(f"{label}: QA result missing/invalid")
        require_nonblank(
            row,
            (
                "scenario_type", "expected_result", "failure_criteria", "verification_level",
                "state_revision", "updated_at", "updated_by",
            ),
            label,
        )
        verify_evidence(root, row, label)
        scenario_results[row["QA_검수결과"]] += 1
        if args.mode == "product-pass":
            if row["QA_검수결과"] != "통과" or row["unverified_scope"]:
                raise CompletionError(f"{label}: product pass requires full scenario verification")

    work_package_results = {
        work_package: {
            "requirements": len(requirement_ids),
            "passed": sum(requirement_ledger[item]["QA_검수결과"] == "통과" for item in requirement_ids),
            "failed": sum(requirement_ledger[item]["QA_검수결과"] == "미통과" for item in requirement_ids),
        }
        for work_package, requirement_ids in work_packages.items()
    }
    partial_work_packages = [
        work_package for work_package, result in work_package_results.items()
        if result["passed"] + result["failed"] != result["requirements"]
    ]
    if partial_work_packages:
        raise CompletionError(f"partial work packages: {partial_work_packages[:5]}")

    if sum(requirement_results.values()) != len(requirement_master):
        raise CompletionError("Requirement progress invariant failed")
    if sum(scenario_results.values()) != len(scenario_master):
        raise CompletionError("Scenario progress invariant failed")

    return {
        "status": "PASS",
        "mode": args.mode,
        "verifiedAgainstSha": expected_sha,
        "requirements": {
            "total": len(requirement_master),
            "passed": requirement_results["통과"],
            "failed": requirement_results["미통과"],
            "unreviewed": 0,
        },
        "scenarios": {
            "total": len(scenario_master),
            "passed": scenario_results["통과"],
            "failed": scenario_results["미통과"],
            "unreviewed": 0,
        },
        "workPackages": {
            "total": len(work_package_results),
            "complete": len(work_package_results),
            "partial": 0,
            "unstarted": 0,
        },
        "requirementLedger": args.requirement_ledger,
        "scenarioLedger": args.scenario_ledger,
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--expected-sha", required=True)
    parser.add_argument(
        "--mode", choices=("campaign-complete", "product-pass"), default="product-pass"
    )
    parser.add_argument(
        "--requirement-ledger",
        default="cpf-docs/work/current/REQUIREMENT_STATUS.csv",
    )
    parser.add_argument(
        "--scenario-ledger",
        default="cpf-docs/work/current/SCENARIO_STATUS.csv",
    )
    parser.add_argument("--json-output")
    args = parser.parse_args()
    try:
        result = verify(args)
        code = 0
    except Exception as failure:
        result = {"status": "FAIL", "mode": args.mode, "message": str(failure)}
        code = 1
    if args.json_output:
        root = Path(args.root).resolve()
        output = Path(args.json_output)
        output = output if output.is_absolute() else root / output
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False))
    return code


if __name__ == "__main__":
    raise SystemExit(main())
