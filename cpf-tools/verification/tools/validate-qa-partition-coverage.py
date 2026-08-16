#!/usr/bin/env python3
"""Validate deterministic QA partition coverage against the actual logical ledgers.

Requirement IDs are identifiers, not row ordinals. Coverage is therefore proven by
logical row positions plus the unique Requirement ID set, never by arithmetic over
an ID suffix.
"""
from __future__ import annotations

import argparse
import csv
import json
from pathlib import Path


def read(path: str | Path) -> tuple[list[str], list[dict[str, str]]]:
    with Path(path).open(encoding="utf-8-sig", newline="") as handle:
        reader = csv.DictReader(handle)
        return list(reader.fieldnames or []), list(reader)


def require_columns(fields: list[str], required: set[str], label: str) -> None:
    missing = required - set(fields)
    if missing:
        raise ValueError(f"{label} missing columns {sorted(missing)}")


def positive_int(value: str, label: str) -> int:
    try:
        parsed = int(value)
    except ValueError as exc:
        raise ValueError(f"{label} must be an integer: {value!r}") from exc
    if parsed < 1:
        raise ValueError(f"{label} must be positive: {parsed}")
    return parsed


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--plan", required=True)
    parser.add_argument("--expected-total", type=int, required=True)
    parser.add_argument("--expected-sha", required=True)
    parser.add_argument("--requirement-ledger", required=True)
    parser.add_argument("--scenario-ledger", required=True)
    parser.add_argument("--json-output")
    args = parser.parse_args()
    try:
        plan_fields, plan = read(args.plan)
        require_columns(
            plan_fields,
            {"partition_id", "logical_start", "logical_end", "requirement_count", "baseline_sha"},
            "plan",
        )
        if not plan:
            raise ValueError("plan is empty")

        ranges: list[tuple[int, int, str, dict[str, str]]] = []
        partition_ids: set[str] = set()
        for row in plan:
            partition_id = row["partition_id"].strip()
            if not partition_id:
                raise ValueError("blank partition_id")
            if partition_id in partition_ids:
                raise ValueError(f"duplicate partition_id {partition_id}")
            partition_ids.add(partition_id)
            if row["baseline_sha"] != args.expected_sha:
                raise ValueError(f"baseline SHA mismatch for {partition_id}")
            start = positive_int(row["logical_start"], f"{partition_id}.logical_start")
            end = positive_int(row["logical_end"], f"{partition_id}.logical_end")
            count = positive_int(row["requirement_count"], f"{partition_id}.requirement_count")
            if end < start or count != end - start + 1:
                raise ValueError(f"invalid logical range/count for {partition_id}")
            ranges.append((start, end, partition_id, row))

        ranges.sort(key=lambda item: item[0])
        cursor = 1
        for start, end, partition_id, _ in ranges:
            if start != cursor:
                raise ValueError(
                    f"logical gap/overlap before {partition_id}: expected {cursor}, got {start}"
                )
            cursor = end + 1
        if cursor - 1 != args.expected_total:
            raise ValueError(f"logical coverage={cursor - 1} expected={args.expected_total}")

        requirement_fields, requirements = read(args.requirement_ledger)
        require_columns(requirement_fields, {"requirement_id", "baseline_sha"}, "requirement ledger")
        if len(requirements) != args.expected_total:
            raise ValueError(
                f"requirement ledger count={len(requirements)} expected={args.expected_total}"
            )
        requirement_ids = [row["requirement_id"].strip() for row in requirements]
        if any(not requirement_id for requirement_id in requirement_ids):
            raise ValueError("blank requirement_id")
        if len(set(requirement_ids)) != len(requirement_ids):
            raise ValueError("duplicate requirement_id")
        for row in requirements:
            if row.get("baseline_sha") != args.expected_sha:
                raise ValueError(f"requirement baseline mismatch {row.get('requirement_id')}")

        owner_by_requirement: dict[str, str] = {}
        per_partition: list[dict[str, object]] = []
        for start, end, partition_id, plan_row in ranges:
            assigned = requirements[start - 1 : end]
            actual_first = assigned[0]["requirement_id"]
            actual_last = assigned[-1]["requirement_id"]
            expected_first = plan_row.get("first_requirement_id", "").strip()
            expected_last = plan_row.get("last_requirement_id", "").strip()
            if expected_first and expected_first != actual_first:
                raise ValueError(
                    f"{partition_id} first Requirement mismatch expected={expected_first} actual={actual_first}"
                )
            if expected_last and expected_last != actual_last:
                raise ValueError(
                    f"{partition_id} last Requirement mismatch expected={expected_last} actual={actual_last}"
                )
            for row in assigned:
                requirement_id = row["requirement_id"]
                owner_by_requirement[requirement_id] = partition_id
                declared = row.get("partition_id", "").strip()
                if declared and declared != partition_id:
                    raise ValueError(
                        f"requirement partition mismatch {requirement_id}: {declared}/{partition_id}"
                    )
            per_partition.append(
                {
                    "partitionId": partition_id,
                    "logicalStart": start,
                    "logicalEnd": end,
                    "requirements": len(assigned),
                    "firstRequirementId": actual_first,
                    "lastRequirementId": actual_last,
                    "scenarios": 0,
                }
            )

        scenario_fields, scenarios = read(args.scenario_ledger)
        require_columns(
            scenario_fields,
            {"scenario_id", "linked_requirement_id", "baseline_sha"},
            "scenario ledger",
        )
        scenario_ids = [row["scenario_id"].strip() for row in scenarios]
        if any(not scenario_id for scenario_id in scenario_ids):
            raise ValueError("blank scenario_id")
        if len(set(scenario_ids)) != len(scenario_ids):
            raise ValueError("duplicate scenario_id")
        partition_counts = {item["partitionId"]: 0 for item in per_partition}
        for row in scenarios:
            scenario_id = row["scenario_id"]
            if row.get("baseline_sha") != args.expected_sha:
                raise ValueError(f"scenario baseline mismatch {scenario_id}")
            requirement_id = row.get("linked_requirement_id", "").strip()
            owner = owner_by_requirement.get(requirement_id)
            if not owner:
                raise ValueError(
                    f"scenario {scenario_id} links outside Requirement ledger: {requirement_id}"
                )
            declared = row.get("partition_id", "").strip()
            if declared and declared != owner:
                raise ValueError(
                    f"scenario partition mismatch {scenario_id}: {declared}/{owner}"
                )
            partition_counts[owner] += 1
        for item in per_partition:
            item["scenarios"] = partition_counts[item["partitionId"]]

        result = {
            "status": "PASS",
            "partitionCount": len(plan),
            "logicalRequirementCoverage": args.expected_total,
            "uniqueRequirementIds": len(requirement_ids),
            "scenarioCoverage": len(scenarios),
            "perPartition": per_partition,
            "meaning": "actual logical-ledger coverage; Requirement IDs were not treated as ordinals",
        }
        code = 0
    except Exception as exc:
        result = {"status": "FAIL", "message": str(exc)}
        code = 1
    text = json.dumps(result, ensure_ascii=False, indent=2)
    print(text)
    if args.json_output:
        Path(args.json_output).write_text(text + "\n", encoding="utf-8")
    return code


if __name__ == "__main__":
    raise SystemExit(main())
