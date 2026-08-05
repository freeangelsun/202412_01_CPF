#!/usr/bin/env python3
"""Build and fail-closed validate the DEVGPT-6F active scope.

Canonical source chain (V8):
DEVELOPMENT_ITEM_INDEX.csv + ACTIVE_DEVELOPMENT_SCOPE.csv
 -> markdown_file -> ledger_part
 -> CPF_REQUIREMENT_MASTER.parts -> CPF_SCENARIO_MASTER.parts.

The builder may read V7 only as an explicit compatibility fallback when V8 is absent.
It never infers completion from row counts; it emits deterministic scope inventory for
source/consumer/test/evidence review.
"""
from __future__ import annotations

import argparse
import csv
import json
import re
from collections import Counter, defaultdict
from pathlib import Path
from typing import Iterable

FULL_DOMAIN_FILES = {
    "10_ARCHITECTURE_MODULE_BOUNDARY.md",
    "40_BATCH_AGENT_WORKER.md",
    "41_CENTER_CUT.md",
    "80_DEVEX_GENERATOR_SAMPLE.md",
    "100_RELEASE_PRODUCT_GOVERNANCE.md",
}
PARTIAL_DOMAIN_OWNERS = {
    "90_API_QUALITY_TESTING.md": {
        "repository-wide test ownership",
        "cpf-tools quality gates",
    }
}
EXPECTED_COUNTS = {
    "work_items": 224,
    "canonical_requirements": 58,
    "cpf_fr": 5658,
    "cpf_sc": 7878,
    "engineering_gates": 21,
}


class ScopeError(RuntimeError):
    pass


def read_csv(path: Path) -> list[dict[str, str]]:
    if not path.is_file():
        raise ScopeError(f"required CSV missing: {path}")
    with path.open(encoding="utf-8-sig", newline="") as stream:
        rows = list(csv.DictReader(stream))
    if not rows:
        raise ScopeError(f"required CSV is empty: {path}")
    return rows


def write_csv(path: Path, rows: Iterable[dict[str, object]], fieldnames: list[str]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8-sig", newline="") as stream:
        writer = csv.DictWriter(stream, fieldnames=fieldnames, extrasaction="ignore")
        writer.writeheader()
        for row in rows:
            writer.writerow({key: row.get(key, "") for key in fieldnames})


def split_ids(value: str | None) -> set[str]:
    return {part.strip() for part in re.split(r"[;,|]", value or "") if part.strip()}


def duplicates(values: Iterable[str]) -> list[str]:
    counts = Counter(values)
    return sorted(value for value, count in counts.items() if count > 1)


def selected_owner(markdown_file: str, owner: str) -> bool:
    if markdown_file in FULL_DOMAIN_FILES:
        return True
    return owner in PARTIAL_DOMAIN_OWNERS.get(markdown_file, set())


def normalize_v8_index(row: dict[str, str]) -> dict[str, str]:
    return {
        "work_item_id": row["entity_id"],
        "canonical_requirement_id": row["canonical_requirement_id"],
        "priority": row["priority"],
        "work_type": row["work_type"],
        "axis": row["axis"],
        "axis_title": row["title"],
        "owner": row["owner"],
        "dependencies": row["dependencies"],
        "markdown_file": row["markdown_file"],
        "ledger_part": row["ledger_part"],
        "development_status": "",
        "verification_status": "",
        "next_action": "",
    }


def load_work_source(root: Path) -> tuple[Path, Path, list[dict[str, str]], list[dict[str, str]], str, str]:
    current = root / "cpf-docs/work/current"
    v8 = current / "CPF_DEVELOPMENT_MANAGEMENT_V8"
    v8_index = v8 / "DEVELOPMENT_ITEM_INDEX.csv"
    v8_active = v8 / "ACTIVE_DEVELOPMENT_SCOPE.csv"
    if v8_index.is_file() or v8_active.is_file():
        if not v8_index.is_file() or not v8_active.is_file():
            raise ScopeError("V8 management source is partially present")
        raw_index = read_csv(v8_index)
        active_rows = read_csv(v8_active)
        index_duplicates = duplicates(row["entity_id"] for row in raw_index)
        active_duplicates = duplicates(row["entity_id"] for row in active_rows)
        if index_duplicates:
            raise ScopeError(f"duplicate DEVELOPMENT_ITEM_INDEX IDs: {index_duplicates}")
        if active_duplicates:
            raise ScopeError(f"duplicate ACTIVE_DEVELOPMENT_SCOPE IDs: {active_duplicates}")
        index_ids = {row["entity_id"] for row in raw_index}
        active_ids = {row["entity_id"] for row in active_rows}
        missing_index = sorted(active_ids - index_ids)
        if missing_index:
            raise ScopeError(f"active scope IDs absent from index: {missing_index[:20]}")
        active_state_by_id = {row["entity_id"]: row for row in active_rows}
        normalized: list[dict[str, str]] = []
        excluded: list[dict[str, str]] = []
        for raw in raw_index:
            entity_id = raw["entity_id"]
            if entity_id not in active_ids:
                continue
            state = active_state_by_id[entity_id]
            if state.get("개발GPT_작업대상상태") != "작업 대상":
                raise ScopeError(
                    f"active scope contains non-active state: {entity_id}="
                    f"{state.get('개발GPT_작업대상상태')!r}"
                )
            if raw.get("entity_type") != "WORK_PACKAGE":
                excluded.append({
                    **normalize_v8_index(raw),
                    "exclude_reason": f"entity_type={raw.get('entity_type')}",
                })
                continue
            row = normalize_v8_index(raw)
            if selected_owner(row["markdown_file"], row["owner"]):
                normalized.append(row)
            else:
                excluded.append({**row, "exclude_reason": "different owner/session scope"})
        source_chain = (
            "CPF_DEVELOPMENT_MANAGEMENT_V8/DEVELOPMENT_ITEM_INDEX.csv + "
            "ACTIVE_DEVELOPMENT_SCOPE.csv -> markdown_file -> ledger_part -> "
            "CPF_REQUIREMENT_MASTER.parts -> CPF_SCENARIO_MASTER.parts"
        )
        return v8, v8_index, normalized, excluded, "V8", source_chain

    v7 = current / "CPF_DEVELOPMENT_WORKLIST_V7_1"
    index_path = v7 / "WORK_ITEM_INDEX.csv"
    raw_rows = read_csv(index_path)
    index_duplicates = duplicates(row["work_item_id"] for row in raw_rows)
    if index_duplicates:
        raise ScopeError(f"duplicate WORK_ITEM_INDEX IDs: {index_duplicates}")
    selected = [
        row for row in raw_rows
        if selected_owner(row["markdown_file"], row["owner"])
    ]
    excluded = [
        {**row, "exclude_reason": "different owner/session scope"}
        for row in raw_rows
        if not selected_owner(row["markdown_file"], row["owner"])
    ]
    source_chain = (
        "CPF_DEVELOPMENT_WORKLIST_V7_1/WORK_ITEM_INDEX.csv -> markdown_file -> "
        "ledger_part -> CPF_REQUIREMENT_MASTER.parts -> CPF_SCENARIO_MASTER.parts"
    )
    return v7, index_path, selected, excluded, "V7.1", source_chain


def build(root: Path, baseline_sha: str, output_dir: Path) -> dict[str, object]:
    worklist, index_path, selected, excluded, management_version, source_chain = load_work_source(root)
    findings: list[str] = []
    selected_ids = {row["work_item_id"] for row in selected}
    if len(selected_ids) != len(selected):
        findings.append("selected work item IDs are duplicated after normalization")

    ledger_rows_by_id: dict[str, list[dict[str, str]]] = defaultdict(list)
    for ledger_relative in sorted({row["ledger_part"] for row in selected}):
        ledger_path = worklist / ledger_relative
        for row in read_csv(ledger_path):
            ledger_rows_by_id[row["work_item_id"]].append(row)

    work_rows: list[dict[str, object]] = []
    for index_row in selected:
        work_id = index_row["work_item_id"]
        matches = ledger_rows_by_id.get(work_id, [])
        if len(matches) != 1:
            findings.append(f"{work_id}: ledger row count expected=1 actual={len(matches)}")
            continue
        ledger = matches[0]
        for key, ledger_key in (
            ("canonical_requirement_id", "canonical_requirement_id"),
            ("owner", "owner"),
            ("markdown_file", "domain_file"),
        ):
            if index_row[key] != ledger[ledger_key]:
                findings.append(
                    f"{work_id}: index/ledger mismatch {key}="
                    f"{index_row[key]!r}/{ledger[ledger_key]!r}"
                )
        markdown_path = worklist / index_row["markdown_file"]
        if not markdown_path.is_file():
            findings.append(f"{work_id}: markdown missing {index_row['markdown_file']}")
        else:
            markdown_text = markdown_path.read_text(encoding="utf-8-sig", errors="replace")
            if work_id not in markdown_text:
                findings.append(f"{work_id}: not found in markdown {index_row['markdown_file']}")
        work_rows.append(
            {
                **index_row,
                "canonical_goal": ledger.get("canonical_goal", ""),
                "mandatory_results": ledger.get("mandatory_results", ""),
                "scenario_classes": ledger.get("scenario_classes", ""),
                "applicable_gates": ledger.get("applicable_gates", ""),
                "standards": ledger.get("standards", ""),
                "source_chain": (
                    f"{index_path.relative_to(root).as_posix()} -> "
                    f"{index_row['markdown_file']} -> {index_row['ledger_part']}"
                ),
            }
        )

    canonical_ids = sorted({row["canonical_requirement_id"] for row in selected})
    canonical_set = set(canonical_ids)

    requirement_parts = sorted(
        (root / "cpf-docs/work/current/CPF_REQUIREMENT_MASTER.parts").glob("*.csv")
    )
    if not requirement_parts:
        raise ScopeError("CPF requirement parts are missing")
    requirement_rows: list[dict[str, str]] = []
    all_requirement_ids: list[str] = []
    for part in requirement_parts:
        for row in read_csv(part):
            all_requirement_ids.append(row["requirement_id"])
            if split_ids(row.get("canonical_requirement_ids")) & canonical_set:
                enriched = dict(row)
                enriched["source_part"] = part.relative_to(root).as_posix()
                requirement_rows.append(enriched)
    all_requirement_duplicates = duplicates(all_requirement_ids)
    if all_requirement_duplicates:
        findings.append(f"canonical requirement master duplicate IDs: {all_requirement_duplicates[:20]}")

    requirement_ids = {row["requirement_id"] for row in requirement_rows}
    requirement_duplicates = duplicates(row["requirement_id"] for row in requirement_rows)
    if requirement_duplicates:
        findings.append(f"scope CPF-FR duplicate IDs: {requirement_duplicates}")

    scenario_parts = sorted(
        (root / "cpf-docs/work/current/CPF_SCENARIO_MASTER.parts").glob("*.csv")
    )
    if not scenario_parts:
        raise ScopeError("CPF scenario parts are missing")
    scenario_rows: list[dict[str, str]] = []
    all_scenario_ids: list[str] = []
    for part in scenario_parts:
        for row in read_csv(part):
            all_scenario_ids.append(row["scenario_id"])
            if row["linked_requirement_id"] in requirement_ids:
                enriched = dict(row)
                enriched["source_part"] = part.relative_to(root).as_posix()
                scenario_rows.append(enriched)
    all_scenario_duplicates = duplicates(all_scenario_ids)
    if all_scenario_duplicates:
        findings.append(f"canonical scenario master duplicate IDs: {all_scenario_duplicates[:20]}")
    scenario_duplicates = duplicates(row["scenario_id"] for row in scenario_rows)
    if scenario_duplicates:
        findings.append(f"scope CPF-SC duplicate IDs: {scenario_duplicates}")

    scenarios_by_requirement: dict[str, list[str]] = defaultdict(list)
    for row in scenario_rows:
        scenarios_by_requirement[row["linked_requirement_id"]].append(row["scenario_id"])
    missing_scenario_requirements = sorted(requirement_ids - set(scenarios_by_requirement))
    if missing_scenario_requirements:
        findings.append(
            f"CPF-FR without direct CPF-SC mapping: {missing_scenario_requirements[:20]} "
            f"count={len(missing_scenario_requirements)}"
        )

    requirements_by_canonical: dict[str, list[str]] = defaultdict(list)
    scenarios_by_canonical: dict[str, list[str]] = defaultdict(list)
    work_items_by_canonical: dict[str, list[str]] = defaultdict(list)
    gates_by_canonical: dict[str, set[str]] = defaultdict(set)
    for row in requirement_rows:
        for canonical in split_ids(row.get("canonical_requirement_ids")) & canonical_set:
            requirements_by_canonical[canonical].append(row["requirement_id"])
    for row in scenario_rows:
        for canonical in split_ids(row.get("canonical_requirement_ids")) & canonical_set:
            scenarios_by_canonical[canonical].append(row["scenario_id"])
    for row in work_rows:
        canonical = str(row["canonical_requirement_id"])
        work_items_by_canonical[canonical].append(str(row["work_item_id"]))
        gates_by_canonical[canonical].update(split_ids(str(row.get("applicable_gates", ""))))

    missing_canonical_requirements = sorted(canonical_set - set(requirements_by_canonical))
    missing_canonical_scenarios = sorted(canonical_set - set(scenarios_by_canonical))
    if missing_canonical_requirements:
        findings.append(f"canonical IDs without CPF-FR: {missing_canonical_requirements}")
    if missing_canonical_scenarios:
        findings.append(f"canonical IDs without CPF-SC: {missing_canonical_scenarios}")

    gate_to_work: dict[str, set[str]] = defaultdict(set)
    gate_to_canonical: dict[str, set[str]] = defaultdict(set)
    for row in work_rows:
        for gate in split_ids(str(row.get("applicable_gates", ""))):
            gate_to_work[gate].add(str(row["work_item_id"]))
            gate_to_canonical[gate].add(str(row["canonical_requirement_id"]))
    gate_ids = sorted(gate_to_work)

    actual_counts = {
        "work_items": len(work_rows),
        "canonical_requirements": len(canonical_ids),
        "cpf_fr": len(requirement_rows),
        "cpf_sc": len(scenario_rows),
        "engineering_gates": len(gate_ids),
    }
    for key, expected in EXPECTED_COUNTS.items():
        if actual_counts[key] != expected:
            findings.append(
                f"scope count mismatch {key}: expected={expected} actual={actual_counts[key]}"
            )

    work_fields = [
        "work_item_id", "canonical_requirement_id", "priority", "work_type", "axis",
        "axis_title", "owner", "dependencies", "markdown_file", "ledger_part",
        "canonical_goal", "mandatory_results", "scenario_classes", "applicable_gates",
        "standards", "source_chain", "development_status", "verification_status", "next_action",
    ]
    write_csv(output_dir / "WORK_ITEM_SCOPE.csv", work_rows, work_fields)

    canonical_rows = []
    for canonical in canonical_ids:
        source = next(row for row in work_rows if row["canonical_requirement_id"] == canonical)
        canonical_rows.append(
            {
                "canonical_requirement_id": canonical,
                "owner": source["owner"],
                "markdown_file": source["markdown_file"],
                "work_item_count": len(work_items_by_canonical[canonical]),
                "work_item_ids": ";".join(sorted(work_items_by_canonical[canonical])),
                "cpf_fr_count": len(requirements_by_canonical[canonical]),
                "cpf_fr_ids": ";".join(sorted(requirements_by_canonical[canonical])),
                "cpf_sc_count": len(scenarios_by_canonical[canonical]),
                "cpf_sc_ids": ";".join(sorted(scenarios_by_canonical[canonical])),
                "engineering_gate_ids": ";".join(sorted(gates_by_canonical[canonical])),
            }
        )
    write_csv(
        output_dir / "CANONICAL_REQUIREMENT_SCOPE.csv",
        canonical_rows,
        [
            "canonical_requirement_id", "owner", "markdown_file", "work_item_count",
            "work_item_ids", "cpf_fr_count", "cpf_fr_ids", "cpf_sc_count",
            "cpf_sc_ids", "engineering_gate_ids",
        ],
    )

    fr_fields = [
        "requirement_id", "canonical_requirement_ids", "requirement", "priority",
        "owner_module", "owner_package", "acceptance_criteria", "actual_consumer",
        "verification_method", "regression_protection", "work_package_id",
        "linked_scenario_count", "linked_scenario_ids", "source_part",
    ]
    fr_output = []
    for row in sorted(requirement_rows, key=lambda item: item["requirement_id"]):
        item = dict(row)
        linked = sorted(scenarios_by_requirement[row["requirement_id"]])
        item["linked_scenario_count"] = len(linked)
        item["linked_scenario_ids"] = ";".join(linked)
        fr_output.append(item)
    write_csv(output_dir / "CPF_FR_SCOPE.csv", fr_output, fr_fields)

    sc_fields = [
        "scenario_id", "linked_requirement_id", "canonical_requirement_ids", "priority",
        "area", "scenario_type", "title", "actor", "preconditions", "test_data",
        "steps", "expected_result", "failure_criteria", "environment", "topology",
        "required_evidence", "work_package_id", "blocking_phase_gate_id", "source_part",
    ]
    write_csv(
        output_dir / "CPF_SC_SCOPE.csv",
        sorted(scenario_rows, key=lambda item: item["scenario_id"]),
        sc_fields,
    )

    gate_rows = [
        {
            "engineering_gate_id": gate,
            "work_item_count": len(gate_to_work[gate]),
            "canonical_requirement_count": len(gate_to_canonical[gate]),
            "work_item_ids": ";".join(sorted(gate_to_work[gate])),
            "canonical_requirement_ids": ";".join(sorted(gate_to_canonical[gate])),
        }
        for gate in gate_ids
    ]
    write_csv(
        output_dir / "ENGINEERING_GATE_SCOPE.csv",
        gate_rows,
        [
            "engineering_gate_id", "work_item_count", "canonical_requirement_count",
            "work_item_ids", "canonical_requirement_ids",
        ],
    )

    write_csv(
        output_dir / "EXCLUDED_WORK_ITEM_OWNER_VALIDATION.csv",
        excluded,
        [
            "work_item_id", "canonical_requirement_id", "priority", "work_type", "owner",
            "markdown_file", "ledger_part", "development_status", "verification_status",
            "next_action", "exclude_reason",
        ],
    )

    result: dict[str, object] = {
        "status": "PASS" if not findings else "FAIL",
        "session": "DEVGPT-6F",
        "management_version": management_version,
        "baseline_sha": baseline_sha,
        "source_chain": source_chain,
        "counts": actual_counts,
        "expected_counts": EXPECTED_COUNTS,
        "work_item_ids": sorted(selected_ids),
        "canonical_requirement_ids": canonical_ids,
        "cpf_fr_ids": sorted(requirement_ids),
        "cpf_sc_ids": sorted(row["scenario_id"] for row in scenario_rows),
        "engineering_gate_ids": gate_ids,
        "missing_or_duplicate": {
            "scope_work_item_duplicates": duplicates(row["work_item_id"] for row in work_rows),
            "scope_requirement_duplicates": requirement_duplicates,
            "scope_scenario_duplicates": scenario_duplicates,
            "cpf_fr_without_direct_scenario": missing_scenario_requirements,
            "canonical_without_cpf_fr": missing_canonical_requirements,
            "canonical_without_cpf_sc": missing_canonical_scenarios,
            "unattributed_selected_work_items": sorted(
                row["work_item_id"] for row in selected if not row.get("owner")
            ),
        },
        "excluded_work_item_count": len(excluded),
        "excluded_owner_counts": dict(sorted(Counter(row["owner"] for row in excluded).items())),
        "findings": findings,
    }
    output_dir.mkdir(parents=True, exist_ok=True)
    (output_dir / "SCOPE_COVERAGE_VALIDATION.json").write_text(
        json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    if findings:
        raise ScopeError(json.dumps(result, ensure_ascii=False))
    return result


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--baseline-sha", required=True)
    parser.add_argument("--output-dir", required=True)
    args = parser.parse_args()
    root = Path(args.root).resolve()
    output = Path(args.output_dir)
    if not output.is_absolute():
        output = root / output
    try:
        result = build(root, args.baseline_sha, output)
    except Exception as exc:
        print(f"[FAIL] DEVGPT-6F active scope: {exc}")
        return 1
    print(
        f"[PASS] DEVGPT-6F {result['management_version']} scope "
        + " ".join(f"{key}={value}" for key, value in result["counts"].items())
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
