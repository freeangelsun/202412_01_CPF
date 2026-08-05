#!/usr/bin/env python3
from __future__ import annotations
import argparse, csv, json, re
from collections import Counter
from pathlib import Path
from development_management_lib import (
    ACTIVE_TARGET_STATES, SKIP_TARGET_STATES, ManagementError, read_csv, split_values, topo_sort, write_json
)

EXPECTED = {"work_packages":775,"stabilization":28,"gaps":24,"managed_items":827,"canonical":169,"requirements":30558,"scenarios":40763}


def main() -> int:
    parser=argparse.ArgumentParser(description="Validate CPF Development Management V8")
    parser.add_argument("--repo-root",default=".")
    parser.add_argument("--management-dir",default="cpf-docs/work/current/CPF_DEVELOPMENT_MANAGEMENT_V8")
    parser.add_argument("--require-full-assignment",action="store_true")
    args=parser.parse_args()
    root=Path(args.repo_root).resolve(); mgmt=root/args.management_dir
    errors=[]; warnings=[]; checks={}
    required_files=[
        "DEVELOPMENT_ITEM_INDEX.csv","DEVELOPMENT_ITEM_STATE.csv","ACTIVE_DEVELOPMENT_SCOPE.csv",
        "COMPLETED_SKIP_SCOPE.csv","WORK_ITEM_DEPENDENCY_GRAPH.csv","FILE_OWNERSHIP_AND_CHANGE_POLICY.csv",
        "PROPOSED_REQUIREMENT_ADDITIONS.csv","REQUIRED_CANONICAL_PATCHES.csv","DELETE_MANIFEST.csv",
        "REVIEW_INDEX.md","TEST_AND_EVIDENCE.md","OPEN_ISSUES.md"
    ]
    for name in required_files:
        if not (mgmt/name).exists(): errors.append(f"missing required file: {name}")
    if errors:
        print("\n".join(errors)); return 2
    index=read_csv(mgmt/"DEVELOPMENT_ITEM_INDEX.csv")
    state=read_csv(mgmt/"DEVELOPMENT_ITEM_STATE.csv")
    index_ids=[r["entity_id"] for r in index]; state_ids=[r["entity_id"] for r in state]
    counts=Counter(r["entity_type"] for r in index)
    checks["entity_counts"]=dict(counts)
    if len(index)!=EXPECTED["managed_items"]: errors.append(f"managed item count {len(index)} != 827")
    if len(set(index_ids))!=len(index_ids): errors.append("duplicate entity_id in DEVELOPMENT_ITEM_INDEX.csv")
    if set(index_ids)!=set(state_ids): errors.append("index/state entity sets differ")
    if counts["WORK_PACKAGE"]!=EXPECTED["work_packages"]: errors.append(f"work package count {counts['WORK_PACKAGE']} != 775")
    if counts["BASELINE_STABILIZATION"]!=EXPECTED["stabilization"]: errors.append(f"stabilization count {counts['BASELINE_STABILIZATION']} != 28")
    if counts["REQUIREMENT_GAP"]!=EXPECTED["gaps"]: errors.append(f"gap count {counts['REQUIREMENT_GAP']} != 24")
    canonical={r["canonical_requirement_id"] for r in index if r["entity_type"]=="WORK_PACKAGE"}
    if len(canonical)!=EXPECTED["canonical"]: errors.append(f"canonical coverage {len(canonical)} != 169")
    for row in state:
        if any(k.startswith("QA_") or k.startswith("Codex_") for k in row): errors.append("development state illegally contains QA/Codex-owned columns")
        if row["개발GPT_작업대상상태"] in SKIP_TARGET_STATES and row["assigned_session_id"]:
            errors.append(f"skipped item is assigned: {row['entity_id']}")
    active=read_csv(mgmt/"ACTIVE_DEVELOPMENT_SCOPE.csv")
    skipped=read_csv(mgmt/"COMPLETED_SKIP_SCOPE.csv")
    expected_active={r["entity_id"] for r in state if r["개발GPT_작업대상상태"] in ACTIVE_TARGET_STATES}
    expected_skipped={r["entity_id"] for r in state if r["개발GPT_작업대상상태"] in SKIP_TARGET_STATES}
    if {r["entity_id"] for r in active}!=expected_active: errors.append("ACTIVE_DEVELOPMENT_SCOPE.csv is stale")
    if {r["entity_id"] for r in skipped}!=expected_skipped: errors.append("COMPLETED_SKIP_SCOPE.csv is stale")
    graph=read_csv(mgmt/"WORK_ITEM_DEPENDENCY_GRAPH.csv")
    known=set(index_ids); edges=[]
    for edge in graph:
        if edge["from_entity_id"] not in known: errors.append(f"unknown dependency source {edge['from_entity_id']}")
        if edge["to_entity_id"] not in known: errors.append(f"unknown dependency target {edge['to_entity_id']}")
        if edge["mandatory"].lower()=="true": edges.append((edge["from_entity_id"],edge["to_entity_id"]))
    _,cycles=topo_sort(index_ids,edges)
    if cycles: errors.append(f"mandatory dependency cycle: {cycles[:20]}")
    policy=read_csv(mgmt/"FILE_OWNERSHIP_AND_CHANGE_POLICY.csv")
    if not any(r["path_pattern"]=="**" for r in policy): errors.append("file policy has no fallback rule")
    full=mgmt/"FULL_ASSIGNMENT_VALIDATION.json"
    if full.exists():
        data=json.loads(full.read_text(encoding="utf-8-sig"))
        checks["full_assignment_status"]=data.get("status")
        if args.require_full_assignment and data.get("status")!="PASS": errors.append("full assignment has not passed")
        if data.get("status")=="PASS":
            req=data.get("requirements",{}); sc=data.get("scenarios",{})
            if req.get("actual")!=EXPECTED["requirements"] or req.get("unassigned")!=0: errors.append("Requirement full assignment count/unassigned mismatch")
            if sc.get("actual")!=EXPECTED["scenarios"] or sc.get("unassigned")!=0: errors.append("Scenario full assignment count/unassigned mismatch")
    elif args.require_full_assignment: errors.append("FULL_ASSIGNMENT_VALIDATION.json missing")
    result={"schema_version":1,"status":"PASS" if not errors else "FAIL","errors":errors,"warnings":warnings,"checks":checks}
    write_json(mgmt/"MANAGEMENT_VALIDATION_RESULT.json",result)
    print(json.dumps(result,ensure_ascii=False,indent=2))
    return 0 if not errors else 2
if __name__=="__main__": raise SystemExit(main())
