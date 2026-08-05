#!/usr/bin/env python3
from __future__ import annotations
import argparse
from collections import Counter, defaultdict
from pathlib import Path
from development_management_lib import (
    CANONICAL_ID_ALIASES, REQ_ID_ALIASES, SCENARIO_ID_ALIASES, SCENARIO_REQUIREMENT_ALIASES,
    ManagementError, choose_primary, first_value, load_work_items, read_csv, read_split_index,
    refresh_views, split_values, write_csv, write_json
)

REQ_MAP_FIELDS = [
    "requirement_id","canonical_requirement_id","primary_entity_id","supporting_entity_ids",
    "mapping_basis","mapping_score","manual_review_required","source_part","baseline_sha","mapping_status"
]
SCENARIO_MAP_FIELDS = [
    "scenario_id","requirement_id","canonical_requirement_id","primary_entity_id","supporting_entity_ids",
    "mapping_basis","mapping_score","manual_review_required","source_part","baseline_sha","mapping_status"
]


def main() -> int:
    parser = argparse.ArgumentParser(description="Build full CPF Requirement/Scenario-to-development-item assignment")
    parser.add_argument("--repo-root", default=".")
    parser.add_argument("--management-dir", default="cpf-docs/work/current/CPF_DEVELOPMENT_MANAGEMENT_V8")
    parser.add_argument("--baseline-sha", required=True)
    parser.add_argument("--expected-requirements", type=int, default=30558)
    parser.add_argument("--expected-scenarios", type=int, default=40763)
    parser.add_argument("--max-manual-review", type=int, default=-1, help="-1 records but does not fail on low-confidence mapping")
    args = parser.parse_args()

    root = Path(args.repo_root).resolve()
    management = root / args.management_dir
    generated = management / "generated"
    generated.mkdir(parents=True, exist_ok=True)
    work_items, by_id, by_canonical = load_work_items(management)
    valid_work_item_ids = {item["entity_id"] for item in work_items}

    requirements, req_dataset = read_split_index(root / "cpf-docs/work/current/CPF_REQUIREMENT_MASTER.csv", root)
    scenarios, sc_dataset = read_split_index(root / "cpf-docs/work/current/CPF_SCENARIO_MASTER.csv", root)
    if len(requirements) != args.expected_requirements:
        raise ManagementError(f"Requirement count mismatch: expected={args.expected_requirements}; actual={len(requirements)}")
    if len(scenarios) != args.expected_scenarios:
        raise ManagementError(f"Scenario count mismatch: expected={args.expected_scenarios}; actual={len(scenarios)}")

    requirement_map = []
    req_by_id = {}
    unresolved = []
    for row in requirements:
        req_id = first_value(row, REQ_ID_ALIASES, required=True, label="requirement_id")
        canonical_id = first_value(row, CANONICAL_ID_ALIASES, required=True, label="canonical_requirement_id")
        primary, supporting, basis, score, manual = choose_primary(row, by_canonical.get(canonical_id, []), valid_work_item_ids)
        status = "ASSIGNED" if primary else "UNASSIGNED"
        mapped = {
            "requirement_id": req_id,
            "canonical_requirement_id": canonical_id,
            "primary_entity_id": primary,
            "supporting_entity_ids": ";".join(supporting),
            "mapping_basis": basis,
            "mapping_score": str(score),
            "manual_review_required": str(manual).lower(),
            "source_part": row.get("__source_part", ""),
            "baseline_sha": args.baseline_sha,
            "mapping_status": status,
        }
        requirement_map.append(mapped)
        req_by_id[req_id] = mapped
        if status != "ASSIGNED" or canonical_id not in by_canonical:
            unresolved.append({"record_type":"REQUIREMENT","record_id":req_id,"canonical_requirement_id":canonical_id,"reason":basis})

    scenario_map = []
    orphan_scenarios = 0
    for row in scenarios:
        sc_id = first_value(row, SCENARIO_ID_ALIASES, required=True, label="scenario_id")
        req_id = first_value(row, SCENARIO_REQUIREMENT_ALIASES)
        req_mapping = req_by_id.get(req_id)
        canonical_id = first_value(row, CANONICAL_ID_ALIASES) or (req_mapping or {}).get("canonical_requirement_id", "")
        if req_mapping:
            primary = req_mapping["primary_entity_id"]
            supporting = split_values(req_mapping["supporting_entity_ids"])
            basis, score, manual = "PARENT_REQUIREMENT_MAPPING", 1000, req_mapping["manual_review_required"] == "true"
        else:
            orphan_scenarios += 1
            primary, supporting, basis, score, manual = choose_primary(row, by_canonical.get(canonical_id, []), valid_work_item_ids)
        status = "ASSIGNED" if primary else "UNASSIGNED"
        mapped = {
            "scenario_id": sc_id,
            "requirement_id": req_id,
            "canonical_requirement_id": canonical_id,
            "primary_entity_id": primary,
            "supporting_entity_ids": ";".join(supporting),
            "mapping_basis": basis,
            "mapping_score": str(score),
            "manual_review_required": str(manual).lower(),
            "source_part": row.get("__source_part", ""),
            "baseline_sha": args.baseline_sha,
            "mapping_status": status,
        }
        scenario_map.append(mapped)
        if status != "ASSIGNED" or canonical_id not in by_canonical or not req_id:
            unresolved.append({"record_type":"SCENARIO","record_id":sc_id,"canonical_requirement_id":canonical_id,"reason":basis if req_id else "MISSING_REQUIREMENT_LINK"})

    write_csv(generated / "REQUIREMENT_WORK_ITEM_MAP.csv", requirement_map, REQ_MAP_FIELDS)
    write_csv(generated / "SCENARIO_WORK_ITEM_MAP.csv", scenario_map, SCENARIO_MAP_FIELDS)
    write_csv(generated / "UNRESOLVED_MAPPING.csv", unresolved, ["record_type","record_id","canonical_requirement_id","reason"])

    req_primary = Counter(row["primary_entity_id"] for row in requirement_map if row["primary_entity_id"])
    req_support = Counter(item for row in requirement_map for item in split_values(row["supporting_entity_ids"]))
    sc_primary = Counter(row["primary_entity_id"] for row in scenario_map if row["primary_entity_id"])
    sc_support = Counter(item for row in scenario_map for item in split_values(row["supporting_entity_ids"]))
    state = {row["entity_id"]: row for row in read_csv(management / "DEVELOPMENT_ITEM_STATE.csv")}
    index = read_csv(management / "DEVELOPMENT_ITEM_INDEX.csv")
    summary_rows = []
    for item in index:
        eid = item["entity_id"]
        target = state[eid]["개발GPT_작업대상상태"]
        summary_rows.append({
            "entity_id":eid,"entity_type":item["entity_type"],"canonical_requirement_id":item["canonical_requirement_id"],
            "priority":item["priority"],"owner_module":item["owner_module"],"owner_package":item["owner_package"],
            "requirement_primary_count":req_primary[eid],"requirement_support_count":req_support[eid],
            "scenario_primary_count":sc_primary[eid],"scenario_support_count":sc_support[eid],
            "active_requirement_count":req_primary[eid] if target in {"작업 대상","재개발 대상","재검수 대상"} else 0,
            "active_scenario_count":sc_primary[eid] if target in {"작업 대상","재개발 대상","재검수 대상"} else 0,
            "unreviewed_requirement_count":sum(1 for r in requirement_map if r["primary_entity_id"]==eid and r["manual_review_required"]=="true"),
            "unreviewed_scenario_count":sum(1 for r in scenario_map if r["primary_entity_id"]==eid and r["manual_review_required"]=="true"),
            "scope_mapping_status":"ASSIGNED" if item["entity_type"]=="WORK_PACKAGE" else "NOT_REQUIRED",
            "개발GPT_작업대상상태":target,"assigned_session_id":state[eid].get("assigned_session_id", ""),
            "dependency_entities":item.get("dependencies", ""),"open_issue":state[eid].get("open_issue", "")
        })
    write_csv(management / "WORK_ITEM_SCOPE_SUMMARY.csv", summary_rows)
    view_result = refresh_views(management)

    validation = {
        "schema_version":1,"status":"PASS" if not unresolved else "FAIL","baseline_sha":args.baseline_sha,
        "requirements":{"expected":args.expected_requirements,"actual":len(requirement_map),"unassigned":sum(r["mapping_status"]!="ASSIGNED" for r in requirement_map),"manual_review":sum(r["manual_review_required"]=="true" for r in requirement_map)},
        "scenarios":{"expected":args.expected_scenarios,"actual":len(scenario_map),"unassigned":sum(r["mapping_status"]!="ASSIGNED" for r in scenario_map),"manual_review":sum(r["manual_review_required"]=="true" for r in scenario_map),"orphan_parent_links":orphan_scenarios},
        "unknown_canonical_ids":sorted({r["canonical_requirement_id"] for r in requirement_map if r["canonical_requirement_id"] not in by_canonical}),
        "unknown_work_item_ids":sorted({r["primary_entity_id"] for r in requirement_map+scenario_map if r["primary_entity_id"] and r["primary_entity_id"] not in valid_work_item_ids}),
        "unresolved_rows":len(unresolved),"state_views":view_result,"requirement_dataset":req_dataset,"scenario_dataset":sc_dataset,
        "note":"Low-confidence/manual-review is not a false PASS: it remains explicitly counted and must be reviewed before final QA handoff."
    }
    if args.max_manual_review >= 0 and (validation["requirements"]["manual_review"] + validation["scenarios"]["manual_review"]) > args.max_manual_review:
        validation["status"] = "FAIL"
    write_json(management / "FULL_ASSIGNMENT_VALIDATION.json", validation)
    print(f"FULL_ASSIGNMENT {validation['status']}: requirements={len(requirement_map)}, scenarios={len(scenario_map)}, unresolved={len(unresolved)}")
    return 0 if validation["status"] == "PASS" else 2

if __name__ == "__main__":
    raise SystemExit(main())
