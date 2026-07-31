#!/usr/bin/env python3
from __future__ import annotations

import argparse
import csv
from dataclasses import dataclass
from datetime import datetime, timezone
import json
from pathlib import Path
import re
import subprocess
from typing import Iterable

ALLOWED_STATUS = {"완료", "부분 구현", "미구현", "미검증", "실패", "재확인 필요"}


@dataclass(frozen=True)
class Evidence:
    path: str
    valid: bool
    exit_code: int
    requirements: frozenset[str]
    scenarios: frozenset[str]
    result_rows: frozenset[str]
    development_requirements: frozenset[str]


def git_sha(root: Path) -> str:
    result = subprocess.run(["git", "-C", str(root), "rev-parse", "HEAD"], text=True, capture_output=True)
    if result.returncode:
        raise SystemExit(result.stderr.strip() or "Git HEAD resolution failed")
    value = result.stdout.strip()
    if not re.fullmatch(r"[0-9a-f]{40}", value):
        raise SystemExit("exact Git SHA is required")
    return value


def as_set(value: object) -> frozenset[str]:
    if not isinstance(value, list):
        return frozenset()
    return frozenset(str(item).strip() for item in value if str(item).strip())


def load_evidence(root: Path, evidence_root: Path, source_sha: str) -> list[Evidence]:
    items: list[Evidence] = []
    for path in sorted(evidence_root.rglob("*.sanitized.json")):
        try:
            data = json.loads(path.read_text(encoding="utf-8-sig"))
        except Exception:
            continue
        exit_code = int(data.get("exitCode", 1))
        dirty = data.get("sourceDirty", data.get("dirty", True))
        valid = (
            data.get("sourceSha") == source_sha
            and data.get("resultSha") == source_sha
            and dirty is False
            and exit_code == 0
            and data.get("sanitized") is True
            and data.get("releaseEligible") is True
            and bool(data.get("startedAt"))
            and bool(data.get("finishedAt") or data.get("endedAt"))
            and bool(data.get("command") or data.get("commands") or data.get("results"))
        )
        items.append(Evidence(
            path=str(path.relative_to(root)).replace("\\", "/") if path.is_relative_to(root) else str(path),
            valid=valid,
            exit_code=exit_code,
            requirements=as_set(data.get("requirements")),
            scenarios=as_set(data.get("scenarios")),
            result_rows=as_set(data.get("resultRows")),
            development_requirements=as_set(data.get("developmentRequirements")),
        ))
    return items


def read_csv(path: Path) -> tuple[list[str], list[dict[str, str]]]:
    with path.open(encoding="utf-8-sig", newline="") as stream:
        reader = csv.DictReader(stream)
        return list(reader.fieldnames or []), [dict(row) for row in reader]


def write_csv(path: Path, fields: list[str], rows: Iterable[dict[str, str]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8-sig", newline="") as stream:
        writer = csv.DictWriter(stream, fieldnames=fields, extrasaction="ignore")
        writer.writeheader()
        writer.writerows(rows)


def impacted_qa33(root: Path) -> set[str]:
    register = root / "cpf-docs/quality/CPF_20260731_QA34_DEFECT_REGISTER.csv"
    impacted: set[str] = set()
    if register.is_file():
        _, rows = read_csv(register)
        for row in rows:
            for item in (row.get("related_requirements") or "").split(";"):
                item = item.strip()
                if item.startswith("QA33-REQ-"):
                    impacted.add(item)
    return impacted


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--evidence-root", required=True)
    parser.add_argument("--output-dir", required=True)
    parser.add_argument("--strict", action="store_true")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    evidence_root = Path(args.evidence_root).resolve()
    output_dir = Path(args.output_dir).resolve()
    source_sha = git_sha(root)

    req_path = root / "cpf-docs/quality/CPF_20260731_QA33_REQUIREMENT_MATRIX.csv"
    scenario_path = root / "cpf-docs/quality/CPF_20260731_QA33_SCENARIO_MATRIX.csv"
    result_path = root / "cpf-docs/quality/CPF_20260731_QA33_RESULT_MATRIX.csv"
    req_fields, requirements = read_csv(req_path)
    scenario_fields, scenarios = read_csv(scenario_path)
    result_fields, results = read_csv(result_path)
    if (len(requirements), len(scenarios), len(results)) != (138, 414, 552):
        raise SystemExit(f"QA33 canonical count mismatch: {len(requirements)}/{len(scenarios)}/{len(results)}")

    evidence = load_evidence(root, evidence_root, source_sha)
    valid = [item for item in evidence if item.valid]
    requirement_coverage = set().union(*(item.requirements for item in valid)) if valid else set()
    scenario_coverage = set().union(*(item.scenarios for item in valid)) if valid else set()
    row_coverage = set().union(*(item.result_rows for item in valid)) if valid else set()
    development_coverage = set().union(*(item.development_requirements for item in valid)) if valid else set()
    failed_requirements = set().union(*(item.requirements for item in evidence if item.exit_code != 0)) if evidence else set()
    failed_scenarios = set().union(*(item.scenarios for item in evidence if item.exit_code != 0)) if evidence else set()
    impacted = impacted_qa33(root)

    scenario_to_requirement = {row["scenario_id"]: row["requirement_id"] for row in scenarios}
    evidence_by_record: dict[str, list[str]] = {}
    unresolved: list[dict[str, str]] = []

    for row in results:
        record_id = row["record_id"]
        record_type = row["record_type"]
        requirement_id = record_id if record_type == "REQUIREMENT" else scenario_to_requirement.get(record_id, "")
        covered = (
            row["record_key"] in row_coverage
            or (record_type == "REQUIREMENT" and record_id in requirement_coverage)
            or (record_type == "SCENARIO" and record_id in scenario_coverage)
        )
        failed = (
            (record_type == "REQUIREMENT" and record_id in failed_requirements)
            or (record_type == "SCENARIO" and record_id in failed_scenarios)
        )
        matching = [item.path for item in valid if (
            row["record_key"] in item.result_rows
            or record_id in item.requirements
            or record_id in item.scenarios
        )]
        if matching:
            row["evidence_path"] = ";".join(sorted(set(matching)))
            evidence_by_record[record_id] = matching

        if failed:
            row["verification_status"] = "실패"
        elif covered:
            row["verification_status"] = "완료"
        else:
            row["verification_status"] = "미검증"

        if requirement_id in impacted and requirement_id not in development_coverage:
            row["development_status"] = "재확인 필요"
        elif row["development_status"] not in ALLOWED_STATUS:
            row["development_status"] = "재확인 필요"

        if row["development_status"] != "완료" or row["verification_status"] != "완료":
            unresolved.append({
                "record_key": row["record_key"],
                "record_type": record_type,
                "record_id": record_id,
                "requirement_id": requirement_id,
                "development_status": row["development_status"],
                "verification_status": row["verification_status"],
                "reason": "exact-SHA release evidence coverage is incomplete",
            })

    requirement_status = {
        row["record_id"]: row for row in results if row["record_type"] == "REQUIREMENT"
    }
    scenario_status = {
        row["record_id"]: row for row in results if row["record_type"] == "SCENARIO"
    }
    for row in requirements:
        status = requirement_status[row["requirement_id"]]
        row["initial_status"] = "완료" if status["development_status"] == status["verification_status"] == "완료" else status["verification_status"]
    for row in scenarios:
        row["status"] = scenario_status[row["scenario_id"]]["verification_status"]

    write_csv(output_dir / req_path.name, req_fields, requirements)
    write_csv(output_dir / scenario_path.name, scenario_fields, scenarios)
    write_csv(output_dir / result_path.name, result_fields, results)
    unresolved_fields = ["record_key", "record_type", "record_id", "requirement_id", "development_status", "verification_status", "reason"]
    write_csv(output_dir / "CPF_20260731_QA33_UNRESOLVED_REGISTER.csv", unresolved_fields, unresolved)

    summary = {
        "schemaVersion": 1,
        "sourceSha": source_sha,
        "generatedAt": datetime.now(timezone.utc).isoformat(),
        "canonicalCounts": {"requirements": 138, "scenarios": 414, "results": 552},
        "validEvidenceCount": len(valid),
        "completedRequirements": sum(1 for row in results if row["record_type"] == "REQUIREMENT" and row["development_status"] == row["verification_status"] == "완료"),
        "completedScenarios": sum(1 for row in results if row["record_type"] == "SCENARIO" and row["development_status"] == row["verification_status"] == "완료"),
        "unresolvedRows": len(unresolved),
        "releaseEligible": len(unresolved) == 0,
        "sanitized": True,
    }
    (output_dir / "CPF_20260731_QA33_RECLASSIFICATION_SUMMARY.sanitized.json").write_text(
        json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(summary, ensure_ascii=False))
    if args.strict and unresolved:
        raise SystemExit(f"QA33 reclassification unresolved rows: {len(unresolved)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
