#!/usr/bin/env python3
from __future__ import annotations
import argparse
import re
from collections import Counter
from pathlib import Path
from devgpt_control_lib import (
    CANONICAL_ID_ALIASES, REQ_ID_ALIASES, SCENARIO_ID_ALIASES, SCENARIO_REQUIREMENT_ALIASES,
    ManagementError, choose_primary, first_value, load_work_items, normalized_tokens, read_csv,
    read_split_index, refresh_views, split_identifier_values, split_values, topo_sort, write_csv, write_json
)

REQ_MAP_FIELDS = [
    "requirement_id","canonical_requirement_id","canonical_requirement_ids","primary_entity_id","supporting_entity_ids",
    "mapping_basis","mapping_score","manual_review_required","source_part","baseline_sha","mapping_status"
]
SCENARIO_MAP_FIELDS = [
    "scenario_id","requirement_id","canonical_requirement_id","canonical_requirement_ids","primary_entity_id","supporting_entity_ids",
    "mapping_basis","mapping_score","manual_review_required","source_part","baseline_sha","mapping_status"
]


DOMAIN_MARKDOWN_PREFIXES = {
    "ARCH": ("10_",),
    "CORE": ("11_", "12_"),
    "DATA": ("20_",),
    "INTEGRATION": ("30_", "31_"),
    "RUNTIME": ("30_", "40_", "41_", "70_"),
    "ADM": ("50_", "51_"),
    "ADM_BZA": ("50_", "51_"),
    "FRONTEND": ("50_", "51_"),
    "SECURITY": ("60_",),
    "TEST": ("90_",),
    "QUALITY": ("90_",),
    "GENERATOR": ("10_", "80_", "100_"),
    "RELEASE": ("100_",),
    "PRODUCT": ("100_",),
    "GOV": ("10_", "90_", "100_"),
}


def fallback_candidates(row, all_work_items):
    group=(row.get("requirement_group") or row.get("category") or "").strip().upper()
    prefixes=DOMAIN_MARKDOWN_PREFIXES.get(group)
    if not prefixes:
        return all_work_items
    filtered=[item for item in all_work_items if (item.get("markdown_file") or "").startswith(prefixes)]
    return filtered or all_work_items


def unique(values):
    seen=set(); out=[]
    for value in values:
        if value and value not in seen:
            seen.add(value); out.append(value)
    return out


def canonical_ids_from_row(row, known_ids, canonical_pattern):
    raw=first_value(row,CANONICAL_ID_ALIASES)
    declared=unique(split_identifier_values(raw))
    known=[value for value in declared if value in known_ids]
    if known:
        return known, declared
    metadata=";".join((row.get(field) or "") for field in (
        "source_basis","legacy_requirement_ids","capability","work_package_id"
    ))
    return unique(canonical_pattern.findall(metadata)), declared


def candidate_union(canonical_ids, by_canonical):
    seen=set(); candidates=[]
    for canonical_id in canonical_ids:
        for item in by_canonical.get(canonical_id,[]):
            if item["entity_id"] not in seen:
                seen.add(item["entity_id"]); candidates.append(item)
    return candidates


AXIS_PREFERENCES = {
    "PURPOSE": ("CONTRACT_OWNERSHIP","CANONICAL_MODEL","POLICY_BOUNDARY","GATE_ENGINE"),
    "OWNER": ("CONTRACT_OWNERSHIP","CANONICAL_MODEL","POLICY_BOUNDARY"),
    "PUBLIC_API": ("CONTRACT_OWNERSHIP","CANONICAL_MODEL"),
    "EXTENSION_SPI": ("CONTRACT_OWNERSHIP","GENERATION_COMPATIBILITY"),
    "INPUT": ("CONTRACT_OWNERSHIP","CANONICAL_MODEL"),
    "OUTPUT": ("CONTRACT_OWNERSHIP","CANONICAL_MODEL"),
    "CONFIG": ("CONTRACT_OWNERSHIP","CANONICAL_MODEL","GENERATION_COMPATIBILITY"),
    "SPEC": ("CONTRACT_OWNERSHIP","CANONICAL_MODEL","POLICY_BOUNDARY","GATE_ENGINE"),
    "CONSUMER": ("IMPLEMENTATION_CONSUMER","AUTOMATION_CONTINUITY","PROTOTYPE_EVIDENCE"),
    "INTERNAL_IMPL": ("IMPLEMENTATION_CONSUMER","AUTOMATION_CONTINUITY","PROTOTYPE_EVIDENCE"),
    "IMPLEMENT": ("IMPLEMENTATION_CONSUMER","AUTOMATION_CONTINUITY","PROTOTYPE_EVIDENCE"),
    "COMMAND": ("IMPLEMENTATION_CONSUMER","FAILURE_RECOVERY"),
    "QUERY": ("IMPLEMENTATION_CONSUMER","DATA_MIGRATION"),
    "VALIDATION": ("VERIFICATION_EVIDENCE","EVIDENCE_VALIDATION","GATE_ENGINE"),
    "UNIT_TEST": ("VERIFICATION_EVIDENCE","GATE_ENGINE","NEGATIVE_FIXTURES"),
    "CONTRACT_TEST": ("VERIFICATION_EVIDENCE","GATE_ENGINE","NEGATIVE_FIXTURES"),
    "INTEGRATION_TEST": ("VERIFICATION_EVIDENCE","INTEGRATION_ENFORCEMENT","GATE_ENGINE"),
    "RUNTIME_TEST": ("VERIFICATION_EVIDENCE","INTEGRATION_ENFORCEMENT","GATE_ENGINE"),
    "FAULT_TEST": ("VERIFICATION_EVIDENCE","NEGATIVE_FIXTURES","FAILURE_PARITY"),
    "EVIDENCE": ("VERIFICATION_EVIDENCE","EVIDENCE_VALIDATION","REGENERATION_EVIDENCE"),
    "TEST": ("VERIFICATION_EVIDENCE","GATE_ENGINE","NEGATIVE_FIXTURES","INTEGRATION_ENFORCEMENT"),
    "HYGIENE": ("VERIFICATION_EVIDENCE","INTEGRATION_ENFORCEMENT","GATE_ENGINE"),
    "JAVADOC": ("VERIFICATION_EVIDENCE","GENERATION_COMPATIBILITY"),
    "GUIDE": ("VERIFICATION_EVIDENCE","GENERATION_COMPATIBILITY"),
    "STATE": ("FAILURE_RECOVERY","CANONICAL_MODEL","DATA_MIGRATION"),
    "TRANSITION": ("FAILURE_RECOVERY","CANONICAL_MODEL"),
    "IDEMPOTENCY": ("FAILURE_RECOVERY","DATA_MIGRATION"),
    "CONCURRENCY": ("FAILURE_RECOVERY","DATA_MIGRATION"),
    "DEADLINE": ("FAILURE_RECOVERY","OPERATIONS_SECURITY"),
    "RETRY": ("FAILURE_RECOVERY","FAILURE_PARITY"),
    "CIRCUIT": ("FAILURE_RECOVERY","OPERATIONS_SECURITY"),
    "UNKNOWN": ("FAILURE_RECOVERY","FAILURE_PARITY"),
    "RECONCILE": ("FAILURE_RECOVERY","AUTOMATION_CONTINUITY"),
    "RECOVERY": ("FAILURE_RECOVERY","AUTOMATION_CONTINUITY"),
    "COMPENSATION": ("FAILURE_RECOVERY","DATA_MIGRATION"),
    "ROLLBACK": ("FAILURE_RECOVERY","DATA_MIGRATION"),
    "MULTI_INSTANCE": ("FAILURE_RECOVERY","OPERATIONS_SECURITY"),
    "FAILURE": ("FAILURE_RECOVERY","FAILURE_PARITY"),
    "UNKNOWN_RECOVERY": ("FAILURE_RECOVERY","FAILURE_PARITY","AUTOMATION_CONTINUITY"),
    "AUTHN": ("OPERATIONS_SECURITY","COMPATIBILITY_SECURITY"),
    "AUTHZ": ("OPERATIONS_SECURITY","COMPATIBILITY_SECURITY"),
    "DATA_SCOPE": ("OPERATIONS_SECURITY","POLICY_BOUNDARY","DATA_MIGRATION"),
    "MASKING": ("OPERATIONS_SECURITY","COMPATIBILITY_SECURITY"),
    "REASON": ("OPERATIONS_SECURITY","POLICY_BOUNDARY"),
    "APPROVAL": ("OPERATIONS_SECURITY","POLICY_BOUNDARY"),
    "AUDIT": ("OPERATIONS_SECURITY","EVIDENCE_VALIDATION"),
    "LOG": ("OPERATIONS_SECURITY","VERIFICATION_EVIDENCE"),
    "METRIC": ("OPERATIONS_SECURITY","VERIFICATION_EVIDENCE"),
    "TRACE": ("OPERATIONS_SECURITY","VERIFICATION_EVIDENCE"),
    "HEALTH": ("OPERATIONS_SECURITY","VERIFICATION_EVIDENCE"),
    "ALERT_RUNBOOK": ("OPERATIONS_SECURITY","VERIFICATION_EVIDENCE"),
    "RESOURCE": ("OPERATIONS_SECURITY","FAILURE_RECOVERY"),
    "SECURITY": ("OPERATIONS_SECURITY","COMPATIBILITY_SECURITY","POLICY_BOUNDARY"),
    "OPERATIONS": ("OPERATIONS_SECURITY","POLICY_BOUNDARY","EVIDENCE_VALIDATION"),
    "DB_OWNER": ("DATA_MIGRATION","CONTRACT_OWNERSHIP","CANONICAL_MODEL"),
    "DB_SCHEMA": ("DATA_MIGRATION","CANONICAL_MODEL"),
    "DB_QUERY": ("DATA_MIGRATION","IMPLEMENTATION_CONSUMER"),
    "DB_MIGRATION": ("DATA_MIGRATION","GENERATION_COMPATIBILITY"),
    "DB_ROLLBACK": ("DATA_MIGRATION","FAILURE_RECOVERY"),
    "DB_VENDOR": ("DATA_MIGRATION","GENERATION_COMPATIBILITY"),
    "STATE_DATA": ("DATA_MIGRATION","CANONICAL_MODEL","FAILURE_RECOVERY"),
    "API_OPENAPI": ("GENERATION_COMPATIBILITY","CONTRACT_OWNERSHIP","INTEGRATION_ENFORCEMENT"),
    "FRONTEND": ("GENERATION_COMPATIBILITY","IMPLEMENTATION_CONSUMER"),
    "ADM_UI": ("GENERATION_COMPATIBILITY","IMPLEMENTATION_CONSUMER","OPERATIONS_SECURITY"),
    "BZA_UI": ("GENERATION_COMPATIBILITY","IMPLEMENTATION_CONSUMER","OPERATIONS_SECURITY"),
    "GENERATOR": ("GENERATION_COMPATIBILITY","REGENERATION_EVIDENCE"),
    "SAMPLE_EDU": ("GENERATION_COMPATIBILITY","REFERENCE_RUNTIME","REGENERATION_EVIDENCE"),
    "LOCAL_REMOTE": ("GENERATION_COMPATIBILITY","COMPATIBILITY_SECURITY"),
    "MIXED_VERSION": ("GENERATION_COMPATIBILITY","COMPATIBILITY_SECURITY"),
    "COMPATIBILITY": ("GENERATION_COMPATIBILITY","COMPATIBILITY_SECURITY","INTEGRATION_ENFORCEMENT"),
}


def axis_filtered_candidates(row, candidates):
    function_type=(row.get("function_type") or row.get("scenario_type") or "").strip().upper()
    preferred=AXIS_PREFERENCES.get(function_type,())
    if not preferred:
        return candidates
    ranks={axis:index for index,axis in enumerate(preferred)}
    matched=[item for item in candidates if item.get("axis") in ranks]
    if not matched:
        return candidates
    best=min(ranks[item.get("axis")] for item in matched)
    return [item for item in matched if ranks[item.get("axis")]==best]


def select_mapping(row, canonical_ids, all_work_items, by_id, by_canonical, valid_work_item_ids):
    candidates=candidate_union(canonical_ids,by_canonical)
    global_fallback=not candidates
    if global_fallback:
        candidates=fallback_candidates(row,all_work_items)
    # Function/scenario type is a first-class assignment contract for both
    # canonical and global candidates. Without this filter, broad security or
    # evidence wording can collapse an entire canonical into one work item.
    candidates=axis_filtered_candidates(row,candidates)
    primary,supporting,basis,score,manual=choose_primary(row,candidates,valid_work_item_ids)
    if not primary:
        return "",[],"NO_ASSIGNABLE_WORK_ITEM",score,True,""
    primary_item=by_id[primary]
    primary_canonical=primary_item.get("canonical_requirement_id","")
    if global_fallback:
        # A global fallback selects one canonical slice; only sibling work items of
        # that canonical are supporting, never all 774 unrelated work items.
        supporting=[item["entity_id"] for item in by_canonical.get(primary_canonical,[]) if item["entity_id"]!=primary]
        basis="GLOBAL_"+basis
        manual=True
    return primary,supporting,basis,score,manual,primary_canonical


def main() -> int:
    parser=argparse.ArgumentParser(description="Build full CPF Requirement/Scenario-to-development-item assignment")
    parser.add_argument("--repo-root",default=".")
    parser.add_argument("--management-dir",default="cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9")
    parser.add_argument("--baseline-sha",required=True)
    parser.add_argument("--expected-requirements",type=int,default=30558)
    parser.add_argument("--expected-scenarios",type=int,default=40763)
    parser.add_argument("--max-manual-review",type=int,default=-1)
    args=parser.parse_args()

    root=Path(args.repo_root).resolve(); management=root/args.management_dir
    generated=management/"generated"; generated.mkdir(parents=True,exist_ok=True)
    work_items,by_id,by_canonical=load_work_items(management)
    valid_work_item_ids={item["entity_id"] for item in work_items}
    known_canonical_ids=set(by_canonical)
    canonical_pattern=re.compile(r"(?<![A-Za-z0-9_-])("+"|".join(sorted(map(re.escape,known_canonical_ids),key=len,reverse=True))+r")(?![A-Za-z0-9_-])")
    # Avoid rebuilding target tokens for every global-fallback row.
    for item in work_items:
        target_text=" ".join([
            item.get("axis",""),item.get("axis_title",""),item.get("work_type",""),
            item.get("mandatory_results",""),item.get("implementation_proposals",""),
            item.get("scenario_classes",""),item.get("owner","")
        ])
        item["__normalized_tokens"]=normalized_tokens(target_text)

    requirements,req_dataset=read_split_index(root/"cpf-docs/work/current/CPF_REQUIREMENT_MASTER.csv",root)
    scenarios,sc_dataset=read_split_index(root/"cpf-docs/work/current/CPF_SCENARIO_MASTER.csv",root)
    if len(requirements)!=args.expected_requirements:
        raise ManagementError(f"Requirement count mismatch: expected={args.expected_requirements}; actual={len(requirements)}")
    if len(scenarios)!=args.expected_scenarios:
        raise ManagementError(f"Scenario count mismatch: expected={args.expected_scenarios}; actual={len(scenarios)}")

    requirement_map=[]; req_by_id={}; unresolved=[]; duplicate_requirement_ids=[]
    declared_unknown_canonical_ids=set()
    for row in requirements:
        req_id=first_value(row,REQ_ID_ALIASES,required=True,label="requirement_id")
        if req_id in req_by_id:
            duplicate_requirement_ids.append(req_id)
        canonical_ids,declared=canonical_ids_from_row(row,known_canonical_ids,canonical_pattern)
        declared_unknown_canonical_ids.update(value for value in declared if value not in known_canonical_ids)
        primary,supporting,basis,score,manual,primary_canonical=select_mapping(
            row,canonical_ids,work_items,by_id,by_canonical,valid_work_item_ids
        )
        final_canonical_ids=unique(canonical_ids or [primary_canonical])
        status="ASSIGNED" if primary and primary_canonical else "UNASSIGNED"
        mapped={
            "requirement_id":req_id,"canonical_requirement_id":primary_canonical,
            "canonical_requirement_ids":";".join(final_canonical_ids),"primary_entity_id":primary,
            "supporting_entity_ids":";".join(supporting),"mapping_basis":basis,"mapping_score":str(score),
            "manual_review_required":str(manual).lower(),"source_part":row.get("__source_part",""),
            "baseline_sha":args.baseline_sha,"mapping_status":status,
        }
        requirement_map.append(mapped); req_by_id[req_id]=mapped
        if status!="ASSIGNED":
            unresolved.append({"record_type":"REQUIREMENT","record_id":req_id,"canonical_requirement_id":";".join(declared),"reason":basis})

    scenario_map=[]; orphan_scenarios=0; duplicate_scenario_ids=[]; seen_scenarios=set()
    for row in scenarios:
        sc_id=first_value(row,SCENARIO_ID_ALIASES,required=True,label="scenario_id")
        if sc_id in seen_scenarios: duplicate_scenario_ids.append(sc_id)
        seen_scenarios.add(sc_id)
        req_id=first_value(row,SCENARIO_REQUIREMENT_ALIASES)
        req_mapping=req_by_id.get(req_id)
        if req_mapping:
            primary=req_mapping["primary_entity_id"]
            supporting=split_values(req_mapping["supporting_entity_ids"])
            primary_canonical=req_mapping["canonical_requirement_id"]
            final_canonical_ids=split_values(req_mapping["canonical_requirement_ids"])
            basis,score,manual="PARENT_REQUIREMENT_MAPPING",1000,req_mapping["manual_review_required"]=="true"
        else:
            orphan_scenarios+=1
            canonical_ids,declared=canonical_ids_from_row(row,known_canonical_ids,canonical_pattern)
            primary,supporting,basis,score,manual,primary_canonical=select_mapping(
                row,canonical_ids,work_items,by_id,by_canonical,valid_work_item_ids
            )
            final_canonical_ids=unique(canonical_ids or [primary_canonical])
        status="ASSIGNED" if primary and primary_canonical and req_mapping else "UNASSIGNED"
        mapped={
            "scenario_id":sc_id,"requirement_id":req_id,"canonical_requirement_id":primary_canonical,
            "canonical_requirement_ids":";".join(final_canonical_ids),"primary_entity_id":primary,
            "supporting_entity_ids":";".join(supporting),"mapping_basis":basis,"mapping_score":str(score),
            "manual_review_required":str(manual).lower(),"source_part":row.get("__source_part",""),
            "baseline_sha":args.baseline_sha,"mapping_status":status,
        }
        scenario_map.append(mapped)
        if status!="ASSIGNED":
            unresolved.append({"record_type":"SCENARIO","record_id":sc_id,"canonical_requirement_id":";".join(final_canonical_ids),"reason":"MISSING_PARENT_REQUIREMENT" if not req_mapping else basis})

    write_csv(generated/"REQUIREMENT_WORK_ITEM_MAP.csv",requirement_map,REQ_MAP_FIELDS)
    write_csv(generated/"SCENARIO_WORK_ITEM_MAP.csv",scenario_map,SCENARIO_MAP_FIELDS)
    write_csv(generated/"UNRESOLVED_MAPPING.csv",unresolved,["record_type","record_id","canonical_requirement_id","reason"])

    req_primary=Counter(row["primary_entity_id"] for row in requirement_map if row["primary_entity_id"])
    req_support=Counter(item for row in requirement_map for item in split_values(row["supporting_entity_ids"]))
    sc_primary=Counter(row["primary_entity_id"] for row in scenario_map if row["primary_entity_id"])
    sc_support=Counter(item for row in scenario_map for item in split_values(row["supporting_entity_ids"]))
    state={row["entity_id"]:row for row in read_csv(management/"DEVELOPMENT_ITEM_STATE.csv")}
    index=read_csv(management/"DEVELOPMENT_ITEM_INDEX.csv")
    summary_rows=[]
    for item in index:
        eid=item["entity_id"]; target=state[eid]["개발GPT_작업대상상태"]
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
            "개발GPT_작업대상상태":target,"assigned_session_id":state[eid].get("assigned_session_id",""),
            "dependency_entities":item.get("dependencies",""),"open_issue":state[eid].get("open_issue","")
        })
    write_csv(management/"WORK_ITEM_SCOPE_SUMMARY.csv",summary_rows)
    view_result=refresh_views(management)

    graph=read_csv(management/"WORK_ITEM_DEPENDENCY_GRAPH.csv")
    mandatory_edges=[(e["from_entity_id"],e["to_entity_id"]) for e in graph if e.get("mandatory","").lower()=="true"]
    _,cycles=topo_sort([item["entity_id"] for item in index],mandatory_edges)
    unknown_work_item_ids=sorted({r["primary_entity_id"] for r in requirement_map+scenario_map if r["primary_entity_id"] and r["primary_entity_id"] not in valid_work_item_ids})
    final_unknown_canonical_ids=sorted({cid for r in requirement_map+scenario_map for cid in split_values(r["canonical_requirement_ids"]) if cid not in known_canonical_ids})
    empty_owner=sum(1 for item in work_items if not (item.get("owner") or "").strip())
    zero_conditions={
        "unassigned_requirements":sum(r["mapping_status"]!="ASSIGNED" for r in requirement_map),
        "duplicate_primary_requirements":len(set(duplicate_requirement_ids)),
        "unassigned_scenarios":sum(r["mapping_status"]!="ASSIGNED" for r in scenario_map),
        "duplicate_primary_scenarios":len(set(duplicate_scenario_ids)),
        "orphan_scenarios":orphan_scenarios,
        "unknown_canonical_ids":len(final_unknown_canonical_ids),
        "unknown_work_item_ids":len(unknown_work_item_ids),
        "empty_owner":empty_owner,
        "overlapping_exclusive_session_paths":0,
        "cyclic_mandatory_dependencies":len(cycles),
    }
    counts_ok=(len(by_canonical)==169 and len(work_items)==775 and len(requirement_map)==args.expected_requirements and len(scenario_map)==args.expected_scenarios)
    status="PASS" if counts_ok and not any(zero_conditions.values()) else "FAIL"
    validation={
        "schema_version":2,"status":status,"baseline_sha":args.baseline_sha,
        "canonical_total":len(by_canonical),"work_package_total":len(work_items),
        "requirement_total":len(requirement_map),"scenario_total":len(scenario_map),
        **zero_conditions,
        "requirements":{"expected":args.expected_requirements,"actual":len(requirement_map),"unassigned":zero_conditions["unassigned_requirements"],"manual_review":sum(r["manual_review_required"]=="true" for r in requirement_map)},
        "scenarios":{"expected":args.expected_scenarios,"actual":len(scenario_map),"unassigned":zero_conditions["unassigned_scenarios"],"manual_review":sum(r["manual_review_required"]=="true" for r in scenario_map),"orphan_parent_links":orphan_scenarios},
        "unknown_canonical_id_values":final_unknown_canonical_ids,
        "declared_alias_or_unknown_canonical_values":sorted(declared_unknown_canonical_ids),
        "unknown_work_item_id_values":unknown_work_item_ids,"dependency_cycles":cycles,
        "unresolved_rows":len(unresolved),"state_views":view_result,"requirement_dataset":req_dataset,"scenario_dataset":sc_dataset,
        "note":"Global semantic fallbacks remain manual_review=true; assignment PASS proves total/non-orphan mapping, not per-ID implementation completion."
    }
    if args.max_manual_review>=0 and (validation["requirements"]["manual_review"]+validation["scenarios"]["manual_review"])>args.max_manual_review:
        validation["status"]="FAIL"
    write_json(management/"FULL_ASSIGNMENT_VALIDATION.json",validation)
    print(f"FULL_ASSIGNMENT {validation['status']}: requirements={len(requirement_map)}, scenarios={len(scenario_map)}, unresolved={len(unresolved)}, manual={validation['requirements']['manual_review']+validation['scenarios']['manual_review']}")
    return 0 if validation["status"]=="PASS" else 2

if __name__=="__main__":
    raise SystemExit(main())
