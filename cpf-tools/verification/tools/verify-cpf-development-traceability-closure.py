#!/usr/bin/env python3
"""Verify individual developer traceability closure for every Requirement.

PASS means every scoped Requirement has reproducible AC/Scenario/Source/Consumer/Evidence
traceability. It does not mean the Requirement implementation or QA validation passed.
"""
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
import csv
import json
import re
from collections import Counter
from pathlib import Path

SHA = re.compile(r"^[0-9a-f]{40}$")
MANDATORY = (
    "execution_order", "requirement_id", "work_package_id", "requirement", "acceptance_criteria",
    "verification_method", "scenario_count", "scenario_ids", "scenario_expected_results",
    "scenario_failure_criteria", "actual_consumer", "actual_source", "actual_call_path",
    "source_resolution", "evidence_level", "executed_test_runtime_evidence", "evidence_proves",
    "uncovered_acceptance", "development_status", "verification_status", "개발GPT_상태",
    "개발GPT_미완료사유", "개발GPT_실행및검증", "개발GPT_evidence", "verifiedAgainstSha",
)
FORBIDDEN_ROLE_PREFIXES = ("QA_", "Codex_")


class GateError(RuntimeError):
    pass


def read_csv(path: Path) -> tuple[list[str], list[dict[str, str]]]:
    with path.open(encoding="utf-8-sig", newline="") as handle:
        reader = csv.DictReader(handle)
        fields = list(reader.fieldnames or [])
        return fields, [{key: (value or "").strip() for key, value in row.items()} for row in reader]


def relative_evidence(root: Path, value: str) -> Path:
    path = Path(value)
    return path if path.is_absolute() else root / path


def verify(
    root: Path,
    requirement_status: Path,
    work_package_status: Path,
    source_review: Path,
    expected_requirements: int,
    expected_work_packages: int,
    expected_sha: str,
) -> dict[str, object]:
    if not SHA.fullmatch(expected_sha):
        raise GateError(f"invalid expected SHA: {expected_sha}")
    fields, requirements = read_csv(requirement_status)
    missing_fields = [field for field in MANDATORY if field not in fields]
    forbidden = [field for field in fields if field.startswith(FORBIDDEN_ROLE_PREFIXES)]
    findings: list[str] = []
    if missing_fields:
        findings.append(f"mandatory columns missing: {missing_fields}")
    if forbidden:
        findings.append(f"developer matrix contains QA/Codex-owned columns: {forbidden}")
    if len(requirements) != expected_requirements:
        findings.append(f"requirement count mismatch expected={expected_requirements} actual={len(requirements)}")

    requirement_ids: set[str] = set()
    execution_orders: set[str] = set()
    work_packages: Counter[str] = Counter()
    evidence_files: set[str] = set()
    implementation_rows = 0
    traceability_rows = 0
    for number, row in enumerate(requirements, 2):
        requirement_id = row.get("requirement_id", "")
        execution_order = row.get("execution_order", "")
        work_package = row.get("work_package_id", "")
        if not requirement_id:
            findings.append(f"row {number}: blank requirement_id")
            continue
        if requirement_id in requirement_ids:
            findings.append(f"{requirement_id}: duplicate requirement_id")
        requirement_ids.add(requirement_id)
        if not execution_order or execution_order in execution_orders:
            findings.append(f"{requirement_id}: blank/duplicate execution_order={execution_order!r}")
        execution_orders.add(execution_order)
        if not work_package:
            findings.append(f"{requirement_id}: blank work_package_id")
        work_packages[work_package] += 1

        for field in MANDATORY:
            if field not in row or not row[field]:
                if field in {"uncovered_acceptance", "개발GPT_미완료사유"} and row.get("개발GPT_상태") == "완료":
                    continue
                findings.append(f"{requirement_id}: blank {field}")
        try:
            scenario_count = int(row.get("scenario_count", ""))
        except ValueError:
            scenario_count = -1
        scenario_ids = [value for value in row.get("scenario_ids", "").split(";") if value]
        if scenario_count <= 0 or scenario_count != len(scenario_ids):
            findings.append(f"{requirement_id}: scenario mismatch count={scenario_count} ids={len(scenario_ids)}")
        if row.get("source_resolution") != "EXACT_SNAPSHOT_FILES":
            findings.append(f"{requirement_id}: source not resolved to exact snapshot files")
        if row.get("verifiedAgainstSha") != expected_sha:
            findings.append(f"{requirement_id}: SHA mismatch expected={expected_sha} actual={row.get('verifiedAgainstSha')}")
        level = row.get("evidence_level")
        if level == "IMPLEMENTATION_SUBSTITUTE_RUNTIME":
            implementation_rows += 1
        elif level == "TRACEABILITY_ONLY":
            traceability_rows += 1
        else:
            findings.append(f"{requirement_id}: invalid evidence_level={level}")
        if row.get("개발GPT_상태") == "완료":
            if row.get("uncovered_acceptance") or row.get("개발GPT_미완료사유"):
                findings.append(f"{requirement_id}: completed developer row still has uncovered acceptance")
            if row.get("verification_status") != "완료":
                findings.append(f"{requirement_id}: completed developer row lacks completed verification")
        elif not row.get("uncovered_acceptance"):
            findings.append(f"{requirement_id}: incomplete row must describe uncovered acceptance")

        evidence = [value for value in row.get("executed_test_runtime_evidence", "").split(";") if value]
        if not evidence:
            findings.append(f"{requirement_id}: no evidence path")
        for value in evidence:
            evidence_files.add(value)

    if len(work_packages) != expected_work_packages:
        findings.append(f"work package count mismatch expected={expected_work_packages} actual={len(work_packages)}")

    missing_evidence: list[str] = []
    for value in sorted(evidence_files):
        path = relative_evidence(root, value)
        if not path.is_file() or path.stat().st_size == 0:
            missing_evidence.append(value)
    if missing_evidence:
        findings.append(f"missing/empty evidence files: {missing_evidence[:20]}")

    _, work_package_rows = read_csv(work_package_status)
    if len(work_package_rows) != expected_work_packages:
        findings.append(f"WORK_PACKAGE_STATUS count mismatch expected={expected_work_packages} actual={len(work_package_rows)}")
    total_from_work_packages = sum(int(row.get("requirement_count", "0")) for row in work_package_rows)
    if total_from_work_packages != expected_requirements:
        findings.append(f"WORK_PACKAGE_STATUS requirement total mismatch expected={expected_requirements} actual={total_from_work_packages}")

    _, source_review_rows = read_csv(source_review)
    if len(source_review_rows) != expected_work_packages:
        findings.append(f"source review count mismatch expected={expected_work_packages} actual={len(source_review_rows)}")
    unresolved = [row.get("work_package_id", "") for row in source_review_rows if row.get("source_resolution") != "EXACT_SNAPSHOT_FILES"]
    if unresolved:
        findings.append(f"unresolved work package sources: {unresolved}")

    result = {
        "status": "PASS" if not findings else "FAIL",
        "meaning": "PASS confirms per-Requirement developer traceability only; it is not implementation completion or QA pass",
        "baselineSha": expected_sha,
        "requirementCount": len(requirements),
        "uniqueRequirementCount": len(requirement_ids),
        "workPackageCount": len(work_packages),
        "implementationEvidenceRows": implementation_rows,
        "traceabilityOnlyRows": traceability_rows,
        "evidenceFileCount": len(evidence_files),
        "missingEvidenceFiles": missing_evidence,
        "findings": findings,
    }
    if findings:
        raise GateError(json.dumps(result, ensure_ascii=False, indent=2))
    return result


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--requirement-status", required=True)
    parser.add_argument("--work-package-status", required=True)
    parser.add_argument("--source-review", required=True)
    parser.add_argument("--expected-requirements", type=int, default=10_558)
    parser.add_argument("--expected-work-packages", type=int, default=291)
    parser.add_argument("--expected-sha", required=True)
    parser.add_argument("--json-output")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    requirement_status = Path(args.requirement_status)
    work_package_status = Path(args.work_package_status)
    source_review = Path(args.source_review)
    requirement_status = requirement_status if requirement_status.is_absolute() else root / requirement_status
    work_package_status = work_package_status if work_package_status.is_absolute() else root / work_package_status
    source_review = source_review if source_review.is_absolute() else root / source_review
    try:
        result = verify(root, requirement_status, work_package_status, source_review, args.expected_requirements, args.expected_work_packages, args.expected_sha)
        code = 0
    except Exception as error:
        try:
            result = json.loads(str(error))
        except json.JSONDecodeError:
            result = {"status": "FAIL", "message": str(error)}
        code = 1
    if args.json_output:
        output = Path(args.json_output)
        output = output if output.is_absolute() else root / output
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False))
    return code


if __name__ == "__main__":
    raise SystemExit(main())
