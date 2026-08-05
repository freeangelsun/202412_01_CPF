#!/usr/bin/env python3
from __future__ import annotations
import argparse, csv, json, re, heapq
from collections import defaultdict
from pathlib import Path
from development_management_lib import ACTIVE_TARGET_STATES, read_csv, refresh_views, split_values, write_csv, write_json

ASSIGN_FIELDS=[
    "campaign_id","campaign_baseline_sha","assignment_revision","session_id","session_role","entity_id","entity_type",
    "canonical_requirement_id","priority","개발GPT_작업대상상태","requirement_map_ref","scenario_map_ref",
    "allowed_change_paths","protected_paths","supporting_sessions","dependency_entities","integration_owner","scope_status",
    "session_output_root","session_artifact_manifest","session_cleanup_command","overwrite_policy","baseline_conflict_policy"
]

ARTIFACT_FIELDS=[
    "campaign_id","campaign_baseline_sha","assignment_revision","session_id","path","artifact_kind","change_action",
    "existed_at_baseline","baseline_sha256","final_sha256","integration_required","integration_status",
    "cleanup_eligible","cleanup_reason","cleanup_command","retained_reason","owner","notes"
]

def safe_name(value:str)->str:
    value=re.sub(r"[^A-Za-z0-9]+","_",value.upper()).strip("_")
    return value or "GENERAL"

def prepare_immutable_output(path:Path)->None:
    if path.exists() and any(path.iterdir()):
        raise RuntimeError(f"immutable campaign output already exists and is not empty: {path}")
    path.mkdir(parents=True,exist_ok=True)

def session_result_root(campaign_id:str,session_id:str,revision:int)->str:
    return f"cpf-docs/work/current/development-session-results/{campaign_id}/{session_id}/REV-{revision:03d}"

def cleanup_command_for(path:str)->str:
    return f'if (Test-Path -LiteralPath "{path}") {{ Remove-Item -LiteralPath "{path}" -Recurse -Force }}'

def main()->int:
    p=argparse.ArgumentParser(description="Generate immutable, conflict-aware Development GPT session requests")
    p.add_argument("--repo-root",default=".")
    p.add_argument("--management-dir",default="cpf-docs/work/current/CPF_DEVELOPMENT_MANAGEMENT_V8")
    p.add_argument("--campaign-id",required=True)
    p.add_argument("--baseline-sha",required=True)
    p.add_argument("--assignment-revision",type=int,default=1)
    p.add_argument("--max-items-per-session",type=int,default=8)
    p.add_argument("--output-dir",default="")
    p.add_argument("--allow-prebootstrap",action="store_true")
    a=p.parse_args()

    if a.assignment_revision < 1:
        raise SystemExit("assignment revision must be >= 1")

    root=Path(a.repo_root).resolve()
    mgmt=root/a.management_dir
    revision_dir=f"REV-{a.assignment_revision:03d}"
    out=Path(a.output_dir).resolve() if a.output_dir else mgmt/"generated"/"campaigns"/a.campaign_id/revision_dir
    try:
        prepare_immutable_output(out)
    except RuntimeError as exc:
        raise SystemExit(str(exc))

    full_validation=mgmt/"FULL_ASSIGNMENT_VALIDATION.json"
    if not a.allow_prebootstrap:
        if not full_validation.exists() or json.loads(full_validation.read_text(encoding="utf-8-sig")).get("status")!="PASS":
            raise SystemExit("FULL_ASSIGNMENT_VALIDATION.json is not PASS; run initialize-development-management.ps1 first")

    index={r["entity_id"]:r for r in read_csv(mgmt/"DEVELOPMENT_ITEM_INDEX.csv")}
    state_rows=read_csv(mgmt/"DEVELOPMENT_ITEM_STATE.csv")
    state={r["entity_id"]:r for r in state_rows}

    already_assigned=[
        eid for eid,row in state.items()
        if row.get("assigned_session_id","").strip()
        and row.get("개발GPT_작업대상상태") in ACTIVE_TARGET_STATES
    ]
    if already_assigned:
        raise SystemExit(
            "active items already have assigned_session_id; merge/close the previous campaign before generating another: "
            + ",".join(sorted(already_assigned)[:20])
        )

    graph=read_csv(mgmt/"WORK_ITEM_DEPENDENCY_GRAPH.csv")
    active=[
        eid for eid,r in state.items()
        if r["개발GPT_작업대상상태"] in ACTIVE_TARGET_STATES
        and r.get("owner_resolved","true").lower()=="true"
        and r.get("external_blocked","false").lower()!="true"
    ]
    active_set=set(active)
    edges=[
        (e["from_entity_id"],e["to_entity_id"])
        for e in graph
        if e["mandatory"].lower()=="true"
        and e["from_entity_id"] in active_set
        and e["to_entity_id"] in active_set
    ]

    priority_order={"P0":0,"P1":1,"P2":2}
    incoming={eid:0 for eid in active}
    outgoing=defaultdict(list)
    for source,target in edges:
        outgoing[source].append(target)
        incoming[target]+=1

    ready=[]
    for eid,count in incoming.items():
        if count==0:
            heapq.heappush(ready,(priority_order.get(index[eid]["priority"],9),eid))

    order=[]
    while ready:
        _,eid=heapq.heappop(ready)
        order.append(eid)
        for target in sorted(outgoing[eid]):
            incoming[target]-=1
            if incoming[target]==0:
                heapq.heappush(ready,(priority_order.get(index[target]["priority"],9),target))

    cycles=sorted(eid for eid,count in incoming.items() if count>0)
    if cycles:
        raise SystemExit(f"mandatory dependency cycle: {cycles}")

    groups=defaultdict(list)
    for eid in order:
        item=index[eid]
        module=item.get("owner_module") or "integration"
        groups[module].append(eid)

    rows=[]
    session_files=[]
    artifact_templates=[]
    cleanup_files=[]
    entity_to_session={}
    session_no=0

    for module in sorted(groups):
        items=groups[module]
        for start in range(0,len(items),max(1,a.max_items_per_session)):
            session_no+=1
            chunk=items[start:start+a.max_items_per_session]
            sid=f"DEV-{safe_name(module)}-{session_no:03d}"
            for eid in chunk:
                entity_to_session[eid]=sid

    protected=(
        "cpf-docs/governance/**;"
        "cpf-docs/work/current/CPF_REQUIREMENT_MASTER*;"
        "cpf-docs/work/current/CPF_SCENARIO_MASTER*;"
        "cpf-docs/work/current/CPF_DEVELOPMENT_MANAGEMENT_V8/DEVELOPMENT_ITEM_STATE.csv;"
        "cpf-docs/work/current/development-session-results/<other-session>/**;"
        "**/build/generated/**"
    )

    for eid in order:
        item=index[eid]
        st=state[eid]
        sid=entity_to_session[eid]
        module=item.get("owner_module") or "integration"
        deps=split_values(item.get("dependencies",""))
        supporting=sorted({
            entity_to_session[d] for d in deps
            if d in entity_to_session and entity_to_session[d]!=sid
        })
        allowed=f"{module}/**" if module.startswith("cpf-") else "ASSIGNED_OWNER_PATHS_ONLY"
        result_root=session_result_root(a.campaign_id,sid,a.assignment_revision)
        artifact_manifest=f"{result_root}/SESSION_ARTIFACT_MANIFEST.csv"
        cleanup_file=f"{result_root}/SESSION_CLEANUP_COMMAND.ps1"

        rows.append({
            "campaign_id":a.campaign_id,
            "campaign_baseline_sha":a.baseline_sha,
            "assignment_revision":str(a.assignment_revision),
            "session_id":sid,
            "session_role":"개발GPT",
            "entity_id":eid,
            "entity_type":item["entity_type"],
            "canonical_requirement_id":item.get("canonical_requirement_id",""),
            "priority":item["priority"],
            "개발GPT_작업대상상태":st["개발GPT_작업대상상태"],
            "requirement_map_ref":"generated/REQUIREMENT_WORK_ITEM_MAP.csv",
            "scenario_map_ref":"generated/SCENARIO_WORK_ITEM_MAP.csv",
            "allowed_change_paths":allowed,
            "protected_paths":protected,
            "supporting_sessions":";".join(supporting),
            "dependency_entities":";".join(deps),
            "integration_owner":"INTEGRATION-OWNER",
            "scope_status":"BLOCKED_BY_PREDECESSOR_SESSION" if supporting else "READY",
            "session_output_root":result_root,
            "session_artifact_manifest":artifact_manifest,
            "session_cleanup_command":cleanup_file,
            "overwrite_policy":"IMMUTABLE_APPEND_ONLY",
            "baseline_conflict_policy":"STOP_AND_REQUEST_INTEGRATION"
        })

    write_csv(out/"DEVELOPMENT_SESSION_ASSIGNMENTS.csv",rows,ASSIGN_FIELDS)

    sessions=defaultdict(list)
    for row in rows:
        sessions[row["session_id"]].append(row)

    for sid,assigned in sessions.items():
        result_root=assigned[0]["session_output_root"]
        cleanup_command=cleanup_command_for(result_root)
        lines=[
            f"# {sid} 개발 요청",
            "",
            f"- Campaign: `{a.campaign_id}`",
            f"- Assignment Revision: `{a.assignment_revision}`",
            f"- Baseline: `{a.baseline_sha}`",
            f"- Session Result Root: `{result_root}`",
            "- 역할: 개발GPT",
            "- 중앙 원장 직접 수정 금지: Session Result만 제출",
            "- 동일 Campaign/Revision 또는 다른 Session 결과 덮어쓰기 금지",
            "- Baseline 이후 동일 대상 파일 변경 감지 시 작업 중단 후 Integration 요청",
            "",
            "## 배정 항목"
        ]
        for row in assigned:
            item=index[row["entity_id"]]
            lines += [
                f"### {row['entity_id']} — {item['title']}",
                f"- Priority: `{item['priority']}`",
                f"- Owner: `{item['owner']}`",
                f"- 상태: `{row['개발GPT_작업대상상태']}`",
                f"- Dependencies: `{row['dependency_entities'] or '없음'}`",
                f"- Worklist: `{item['markdown_file']}` / `{item['ledger_part']}`",
                "- 필수: 실제 Consumer·오류/경계/부분실패·복구·보안·운영·DB/Generator 영향·직접 실행 Evidence",
                "- 구현 방법: 비강제 제안이며 승인 Architecture·Specification을 준수하는 동등 이상 대안 허용",
                ""
            ]
        lines += [
            "## 변경 경계",
            f"- 허용: `{assigned[0]['allowed_change_paths']}`",
            f"- 보호: `{protected}`",
            "- Shared/Public/Generated 경로 변경은 Integration Owner에게 요청",
            "",
            "## 제출",
            f"- `{result_root}/SESSION_ARTIFACT_MANIFEST.csv`",
            f"- `{result_root}/SESSION_CLEANUP_COMMAND.ps1`",
            f"- `{result_root}/DEVELOPMENT_SESSION_RESULT.csv`",
            f"- `{result_root}/DEVELOPMENT_REQUIREMENT_RESULT.csv`",
            f"- `{result_root}/DEVELOPMENT_SCENARIO_RESULT.csv`",
            f"- `{result_root}/TEST_AND_EVIDENCE.md`",
            f"- `{result_root}/HANDOVER.md`",
            "",
            "## 세션 전용 결과 정리 명령",
            "아래 명령은 세션 전용 결과 경로 전체를 정리하는 후보이며, 제품 Source가 이 경로 밖에 있으므로 제품 파일은 삭제하지 않는다. 사용자 승인 후에만 실행한다.",
            "",
            "```powershell",
            cleanup_command,
            "```"
        ]
        request_name=f"{sid}_REQUEST.md"
        (out/request_name).write_text("\n".join(lines)+"\n",encoding="utf-8")
        session_files.append(request_name)

        artifact_template_name=f"{sid}_SESSION_ARTIFACT_MANIFEST_TEMPLATE.csv"
        write_csv(out/artifact_template_name,[],ARTIFACT_FIELDS)
        artifact_templates.append(artifact_template_name)

        cleanup_name=f"{sid}_SESSION_CLEANUP_COMMAND.ps1"
        (out/cleanup_name).write_text(
            "# USER APPROVAL REQUIRED. Deletes only this session's result root.\n"
            + cleanup_command + "\n",
            encoding="utf-8"
        )
        cleanup_files.append(cleanup_name)

    # Update central assignment only after immutable request files are fully generated.
    for eid,sid in entity_to_session.items():
        row=state[eid]
        row["assigned_session_id"]=sid
        row["assignment_revision"]=str(a.assignment_revision)
        row["state_revision"]=str(int(row.get("state_revision") or 0)+1)
        row["updated_by"]="development-request-generator"

    write_csv(mgmt/"DEVELOPMENT_ITEM_STATE.csv",state_rows,state_rows[0].keys())
    refresh_views(mgmt)

    manifest={
        "campaign_id":a.campaign_id,
        "baseline_sha":a.baseline_sha,
        "assignment_revision":a.assignment_revision,
        "output_path":str(out.relative_to(root)).replace("\\","/"),
        "immutable_output":True,
        "active_items":len(rows),
        "session_count":len(sessions),
        "max_items_per_session":a.max_items_per_session,
        "session_files":session_files,
        "artifact_manifest_templates":artifact_templates,
        "cleanup_command_files":cleanup_files,
        "session_result_root_pattern":"cpf-docs/work/current/development-session-results/<campaign>/<session>/REV-<nnn>",
        "full_assignment_required":not a.allow_prebootstrap,
        "note":"Session count is dynamically derived. Existing campaign/revision output is never overwritten."
    }
    write_json(out/"CAMPAIGN_MANIFEST.json",manifest)
    print(json.dumps(manifest,ensure_ascii=False,indent=2))
    return 0

if __name__=="__main__":
    raise SystemExit(main())
