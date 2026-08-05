#!/usr/bin/env python3
from __future__ import annotations
import argparse, csv, json, math, re, heapq
from collections import defaultdict
from pathlib import Path
from development_management_lib import ACTIVE_TARGET_STATES, read_csv, refresh_views, split_values, write_csv, write_json

ASSIGN_FIELDS=[
    "campaign_id","campaign_baseline_sha","assignment_revision","session_id","session_role","entity_id","entity_type",
    "canonical_requirement_id","priority","개발GPT_작업대상상태","requirement_map_ref","scenario_map_ref",
    "allowed_change_paths","protected_paths","supporting_sessions","dependency_entities","integration_owner","scope_status"
]

def safe_name(value:str)->str:
    value=re.sub(r"[^A-Za-z0-9]+","_",value.upper()).strip("_")
    return value or "GENERAL"

def main()->int:
    p=argparse.ArgumentParser(description="Generate conflict-aware Development GPT session requests")
    p.add_argument("--repo-root",default="."); p.add_argument("--management-dir",default="cpf-docs/work/current/CPF_DEVELOPMENT_MANAGEMENT_V8")
    p.add_argument("--campaign-id",required=True); p.add_argument("--baseline-sha",required=True); p.add_argument("--max-items-per-session",type=int,default=8)
    p.add_argument("--output-dir",default="")
    p.add_argument("--allow-prebootstrap",action="store_true",help="Allow request generation before FULL_ASSIGNMENT_VALIDATION=PASS; intended only for package smoke tests")
    a=p.parse_args(); root=Path(a.repo_root).resolve(); mgmt=root/a.management_dir
    out=Path(a.output_dir).resolve() if a.output_dir else mgmt/"generated"/"campaigns"/a.campaign_id
    out.mkdir(parents=True,exist_ok=True)
    full_validation=mgmt/"FULL_ASSIGNMENT_VALIDATION.json"
    if not a.allow_prebootstrap:
        if not full_validation.exists() or json.loads(full_validation.read_text(encoding="utf-8-sig")).get("status")!="PASS":
            raise SystemExit("FULL_ASSIGNMENT_VALIDATION.json is not PASS; run initialize-development-management.ps1 first")
    index={r["entity_id"]:r for r in read_csv(mgmt/"DEVELOPMENT_ITEM_INDEX.csv")}
    state_rows=read_csv(mgmt/"DEVELOPMENT_ITEM_STATE.csv")
    state={r["entity_id"]:r for r in state_rows}
    graph=read_csv(mgmt/"WORK_ITEM_DEPENDENCY_GRAPH.csv")
    active=[eid for eid,r in state.items() if r["개발GPT_작업대상상태"] in ACTIVE_TARGET_STATES and r.get("owner_resolved","true").lower()=="true" and r.get("external_blocked","false").lower()!="true"]
    active_set=set(active)
    edges=[(e["from_entity_id"],e["to_entity_id"]) for e in graph if e["mandatory"].lower()=="true" and e["from_entity_id"] in active_set and e["to_entity_id"] in active_set]
    priority_order={"P0":0,"P1":1,"P2":2}
    incoming={eid:0 for eid in active}; outgoing=defaultdict(list)
    for source,target in edges:
        outgoing[source].append(target); incoming[target]+=1
    ready=[]
    for eid,count in incoming.items():
        if count==0: heapq.heappush(ready,(priority_order.get(index[eid]["priority"],9),eid))
    order=[]
    while ready:
        _,eid=heapq.heappop(ready); order.append(eid)
        for target in sorted(outgoing[eid]):
            incoming[target]-=1
            if incoming[target]==0: heapq.heappush(ready,(priority_order.get(index[target]["priority"],9),target))
    cycles=sorted(eid for eid,count in incoming.items() if count>0)
    if cycles: raise SystemExit(f"mandatory dependency cycle: {cycles}")
    groups=defaultdict(list)
    for eid in order:
        item=index[eid]; module=item.get("owner_module") or "integration"
        groups[module].append(eid)
    rows=[]; session_files=[]; entity_to_session={}
    session_no=0
    for module in sorted(groups):
        items=groups[module]
        for start in range(0,len(items),max(1,a.max_items_per_session)):
            session_no+=1; chunk=items[start:start+a.max_items_per_session]
            sid=f"DEV-{safe_name(module)}-{session_no:03d}"
            for eid in chunk: entity_to_session[eid]=sid
    protected="cpf-docs/governance/**;cpf-docs/work/current/CPF_REQUIREMENT_MASTER*;cpf-docs/work/current/CPF_SCENARIO_MASTER*;cpf-docs/work/current/CPF_DEVELOPMENT_MANAGEMENT_V8/DEVELOPMENT_ITEM_STATE.csv;**/build/generated/**"
    for eid in order:
        item=index[eid]; st=state[eid]; sid=entity_to_session[eid]; module=item.get("owner_module") or "integration"
        deps=split_values(item.get("dependencies",""))
        supporting=sorted({entity_to_session[d] for d in deps if d in entity_to_session and entity_to_session[d]!=sid})
        allowed=(f"{module}/**" if module.startswith("cpf-") else "ASSIGNED_OWNER_PATHS_ONLY")
        rows.append({
            "campaign_id":a.campaign_id,"campaign_baseline_sha":a.baseline_sha,"assignment_revision":"1","session_id":sid,"session_role":"개발GPT",
            "entity_id":eid,"entity_type":item["entity_type"],"canonical_requirement_id":item.get("canonical_requirement_id",""),"priority":item["priority"],
            "개발GPT_작업대상상태":st["개발GPT_작업대상상태"],"requirement_map_ref":"generated/REQUIREMENT_WORK_ITEM_MAP.csv",
            "scenario_map_ref":"generated/SCENARIO_WORK_ITEM_MAP.csv","allowed_change_paths":allowed,"protected_paths":protected,
            "supporting_sessions":";".join(supporting),"dependency_entities":";".join(deps),"integration_owner":"INTEGRATION-OWNER","scope_status":"BLOCKED_BY_PREDECESSOR_SESSION" if supporting else "READY"
        })
    write_csv(out/"DEVELOPMENT_SESSION_ASSIGNMENTS.csv",rows,ASSIGN_FIELDS)
    sessions=defaultdict(list)
    for row in rows: sessions[row["session_id"]].append(row)
    for sid,assigned in sessions.items():
        lines=[f"# {sid} 개발 요청", "", f"- Campaign: `{a.campaign_id}`", f"- Baseline: `{a.baseline_sha}`", "- 역할: 개발GPT", "- 중앙 원장 직접 수정 금지: Session Result만 제출", "", "## 배정 항목"]
        for row in assigned:
            item=index[row["entity_id"]]
            lines += [f"### {row['entity_id']} — {item['title']}", f"- Priority: `{item['priority']}`", f"- Owner: `{item['owner']}`", f"- 상태: `{row['개발GPT_작업대상상태']}`", f"- Dependencies: `{row['dependency_entities'] or '없음'}`", f"- Worklist: `{item['markdown_file']}` / `{item['ledger_part']}`", "- 필수: 실제 Consumer·오류/경계/부분실패·복구·보안·운영·DB/Generator 영향·직접 실행 Evidence", "- 구현 방법: Worklist의 제안은 비강제이며 승인된 Architecture·Specification을 준수하는 동등 이상 대안 허용", ""]
        lines += ["## 변경 경계", f"- 허용: `{assigned[0]['allowed_change_paths']}`", f"- 보호: `{protected}`", "- Shared/Public/Generated 경로 변경은 `CROSS_SESSION_CHANGE_REQUEST_TEMPLATE.csv`로 Integration Owner에게 요청", "", "## 제출", "- `DEVELOPMENT_SESSION_RESULT_TEMPLATE.csv`", "- `DEVELOPMENT_REQUIREMENT_RESULT_TEMPLATE.csv`", "- `DEVELOPMENT_SCENARIO_RESULT_TEMPLATE.csv`", "- TEST/Evidence 및 changed-path 목록"]
        (out/f"{sid}_REQUEST.md").write_text("\n".join(lines)+"\n",encoding="utf-8")
        session_files.append(f"{sid}_REQUEST.md")
    for eid,sid in entity_to_session.items():
        row=state[eid]; row["assigned_session_id"]=sid; row["assignment_revision"]=str(int(row.get("assignment_revision") or 0)+1); row["state_revision"]=str(int(row.get("state_revision") or 0)+1); row["updated_by"]="development-request-generator"
    write_csv(mgmt/"DEVELOPMENT_ITEM_STATE.csv",state_rows,state_rows[0].keys())
    refresh_views(mgmt)
    manifest={"campaign_id":a.campaign_id,"baseline_sha":a.baseline_sha,"active_items":len(rows),"session_count":len(sessions),"max_items_per_session":a.max_items_per_session,"session_files":session_files,"full_assignment_required":not a.allow_prebootstrap,"note":"Session count is dynamically derived; it is not a fixed project partition."}
    write_json(out/"CAMPAIGN_MANIFEST.json",manifest)
    print(json.dumps(manifest,ensure_ascii=False,indent=2)); return 0
if __name__=="__main__": raise SystemExit(main())
