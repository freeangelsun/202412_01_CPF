#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, re, heapq
from collections import defaultdict
from pathlib import Path
from devgpt_control_lib import ACTIVE_TARGET_STATES, read_csv, refresh_views, split_values, write_csv, write_json

ASSIGN_FIELDS=[
    "campaign_id","campaign_baseline_sha","assignment_revision","development_request_id","session_id","session_role",
    "entity_id","entity_type","canonical_requirement_id","priority","개발GPT_작업대상상태",
    "requirement_map_ref","scenario_map_ref","allowed_change_paths","protected_paths",
    "supporting_sessions","dependency_entities","integration_owner","scope_status",
    "campaign_workspace_root","session_workspace_root","session_result_root",
    "session_evidence_root","session_temp_root","session_handover_path",
    "session_artifact_manifest","session_cleanup_command","campaign_cleanup_command",
    "overwrite_policy","baseline_conflict_policy"
]

ARTIFACT_FIELDS=[
    "campaign_id","campaign_baseline_sha","assignment_revision","session_id","path",
    "artifact_kind","change_action","existed_at_baseline","baseline_sha256","final_sha256",
    "integration_required","integration_status","cleanup_eligible","cleanup_reason",
    "cleanup_command","retained_reason","owner","notes"
]

SESSION_PLAN_FIELDS=[
    "entity_id","session_id","session_role","integration_owner","allowed_change_paths"
]

def load_explicit_session_plan(path: Path, active_ids: set[str]) -> dict[str, dict[str, str]]:
    """Load a fail-closed exact entity-to-session assignment plan.

    The campaign generator must never silently invent or drop fixed DEVGPT session
    assignments. Every active entity must occur exactly once; inactive/unknown rows,
    duplicate entity IDs, and empty session IDs are rejected.
    """
    rows=read_csv(path)
    if not rows:
        raise RuntimeError(f"session plan is empty: {path}")
    by_entity={}
    duplicate=[]
    unknown=[]
    empty_session=[]
    for row in rows:
        eid=(row.get("entity_id") or "").strip()
        sid=(row.get("session_id") or "").strip()
        if not eid:
            raise RuntimeError(f"session plan contains an empty entity_id: {path}")
        if eid in by_entity:
            duplicate.append(eid)
            continue
        if eid not in active_ids:
            unknown.append(eid)
        if not sid:
            empty_session.append(eid)
        normalized={field:(row.get(field) or "").strip() for field in SESSION_PLAN_FIELDS}
        normalized["entity_id"]=eid
        normalized["session_id"]=sid
        normalized["session_role"]=normalized["session_role"] or "개발GPT"
        normalized["integration_owner"]=normalized["integration_owner"] or sid
        by_entity[eid]=normalized
    missing=sorted(active_ids-set(by_entity))
    errors=[]
    if duplicate: errors.append("duplicate="+",".join(sorted(set(duplicate))[:20]))
    if unknown: errors.append("inactive_or_unknown="+",".join(sorted(set(unknown))[:20]))
    if empty_session: errors.append("empty_session="+",".join(sorted(set(empty_session))[:20]))
    if missing: errors.append("missing="+",".join(missing[:20]))
    if errors:
        raise RuntimeError("invalid explicit session plan: "+"; ".join(errors))
    return by_entity

def assign_entities_to_sessions(order: list[str], index: dict[str, dict[str, str]], max_items_per_session: int, plan: dict[str, dict[str, str]] | None=None) -> tuple[dict[str, str], dict[str, dict[str, str]]]:
    """Return exact entity/session assignments and optional per-entity plan metadata."""
    if plan is not None:
        return {eid:plan[eid]["session_id"] for eid in order}, plan
    groups=defaultdict(list)
    for eid in order:
        item=index[eid]
        groups[item.get("owner_module") or "integration"].append(eid)
    entity_to_session={}
    session_no=0
    for module in sorted(groups):
        items=groups[module]
        for start in range(0,len(items),max(1,max_items_per_session)):
            session_no+=1
            sid=f"DEV-{safe_name(module)}-{session_no:03d}"
            for eid in items[start:start+max_items_per_session]:
                entity_to_session[eid]=sid
    return entity_to_session, {}

def development_request_id(campaign_id: str, revision: int, session_id: str) -> str:
    """Return the immutable top-level request identifier for one campaign session."""
    return f"{campaign_id}-REV-{revision:03d}-{session_id}"


def safe_name(value:str)->str:
    value=re.sub(r"[^A-Za-z0-9]+","_",value.upper()).strip("_")
    return value or "GENERAL"

def relative_campaign_root(management_dir:str,campaign_id:str,revision:int)->str:
    return f"{management_dir}/_session_workspace/{campaign_id}/REV-{revision:03d}"

def relative_session_root(management_dir:str,campaign_id:str,session_id:str,revision:int)->str:
    return f"{relative_campaign_root(management_dir,campaign_id,revision)}/sessions/{session_id}"

def exact_cleanup_command(relative_path:str)->str:
    return f'if (Test-Path -LiteralPath "{relative_path}") {{ Remove-Item -LiteralPath "{relative_path}" -Recurse -Force }}'

def prepare_immutable_output(path:Path)->None:
    if path.exists() and any(path.iterdir()):
        raise RuntimeError(f"isolated campaign revision already exists and is not empty: {path}")
    path.mkdir(parents=True,exist_ok=True)

def main()->int:
    p=argparse.ArgumentParser(description="Generate isolated, immutable Development GPT campaign workspaces")
    p.add_argument("--repo-root",default=".")
    p.add_argument("--management-dir",default="cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9")
    p.add_argument("--campaign-id",required=True)
    p.add_argument("--baseline-sha",required=True)
    p.add_argument("--assignment-revision",type=int,default=1)
    p.add_argument("--max-items-per-session",type=int,default=8)
    p.add_argument("--session-plan",help="CSV exact entity-to-session assignment plan; requires every active entity exactly once")
    p.add_argument("--allow-prebootstrap",action="store_true")
    a=p.parse_args()

    if a.assignment_revision < 1:
        raise SystemExit("assignment revision must be >= 1")

    root=Path(a.repo_root).resolve()
    mgmt=root/a.management_dir
    campaign_rel=relative_campaign_root(a.management_dir,a.campaign_id,a.assignment_revision)
    campaign_root=root/campaign_rel

    try:
        prepare_immutable_output(campaign_root)
    except RuntimeError as exc:
        raise SystemExit(str(exc))

    requests_dir=campaign_root/"requests"
    sessions_dir=campaign_root/"sessions"
    requests_dir.mkdir(parents=True,exist_ok=True)
    sessions_dir.mkdir(parents=True,exist_ok=True)

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
            "active items already assigned; merge or close the previous campaign before generating another: "
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

    explicit_plan=None
    if a.session_plan:
        plan_path=Path(a.session_plan)
        if not plan_path.is_absolute():
            plan_path=root/plan_path
        try:
            explicit_plan=load_explicit_session_plan(plan_path,active_set)
        except RuntimeError as exc:
            raise SystemExit(str(exc))
    entity_to_session,plan_metadata=assign_entities_to_sessions(
        order,index,a.max_items_per_session,explicit_plan
    )

    protected=(
        "cpf-docs/governance/**;"
        "cpf-docs/work/current/CPF_REQUIREMENT_MASTER*;"
        "cpf-docs/work/current/CPF_SCENARIO_MASTER*;"
        "cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/** except assigned _session_workspace path;"
        "cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/**;"
        "cpf-docs/work/current/development-session-results/**;"
        "**/build/generated/**"
    )

    rows=[]
    sessions=defaultdict(list)
    campaign_cleanup=exact_cleanup_command(campaign_rel)

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
        plan_row=plan_metadata.get(eid,{})
        allowed=plan_row.get("allowed_change_paths") or (f"{module}/**" if module.startswith("cpf-") else "ASSIGNED_OWNER_PATHS_ONLY")
        session_rel=relative_session_root(a.management_dir,a.campaign_id,sid,a.assignment_revision)
        result_rel=f"{session_rel}/results"
        evidence_rel=f"{session_rel}/evidence"
        temp_rel=f"{session_rel}/temp"
        handover_rel=f"{session_rel}/HANDOVER.md"
        artifact_rel=f"{session_rel}/SESSION_ARTIFACT_MANIFEST.csv"
        cleanup_rel=f"{session_rel}/SESSION_CLEANUP_COMMAND.ps1"

        row={
            "campaign_id":a.campaign_id,
            "campaign_baseline_sha":a.baseline_sha,
            "assignment_revision":str(a.assignment_revision),
            "development_request_id":development_request_id(a.campaign_id,a.assignment_revision,sid),
            "session_id":sid,
            "session_role":plan_row.get("session_role") or "개발GPT",
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
            "integration_owner":plan_row.get("integration_owner") or "INTEGRATION-OWNER",
            "scope_status":"BLOCKED_BY_PREDECESSOR_SESSION" if supporting else "READY",
            "campaign_workspace_root":campaign_rel,
            "session_workspace_root":session_rel,
            "session_result_root":result_rel,
            "session_evidence_root":evidence_rel,
            "session_temp_root":temp_rel,
            "session_handover_path":handover_rel,
            "session_artifact_manifest":artifact_rel,
            "session_cleanup_command":cleanup_rel,
            "campaign_cleanup_command":f"{campaign_rel}/CAMPAIGN_CLEANUP_COMMAND.ps1",
            "overwrite_policy":"ISOLATED_IMMUTABLE_APPEND_ONLY",
            "baseline_conflict_policy":"STOP_AND_REQUEST_INTEGRATION",
        }
        rows.append(row)
        sessions[sid].append(row)

    write_csv(requests_dir/"DEVELOPMENT_SESSION_ASSIGNMENTS.csv",rows,ASSIGN_FIELDS)

    for sid,assigned in sessions.items():
        session_rel=assigned[0]["session_workspace_root"]
        session_root=root/session_rel
        result_root=session_root/"results"
        evidence_root=session_root/"evidence"
        temp_root=session_root/"temp"
        for folder in [result_root,evidence_root,temp_root]:
            folder.mkdir(parents=True,exist_ok=True)

        session_cleanup=exact_cleanup_command(session_rel)
        (session_root/"SESSION_CLEANUP_COMMAND.ps1").write_text(
            "# USER APPROVAL REQUIRED. Deletes only this V9 session workspace.\n"
            + session_cleanup + "\n",
            encoding="utf-8"
        )
        write_csv(session_root/"SESSION_ARTIFACT_MANIFEST.csv",[],ARTIFACT_FIELDS)

        lines=[
            f"# {sid} 개발 요청",
            "",
            f"- Campaign: `{a.campaign_id}`",
            f"- Assignment Revision: `{a.assignment_revision}`",
            f"- Development Request ID: `{assigned[0]['development_request_id']}`",
            f"- Baseline: `{a.baseline_sha}`",
            f"- Campaign Workspace: `{campaign_rel}`",
            f"- Session Workspace: `{session_rel}`",
            "- 제품 파일 외 모든 산출물은 이 Session Workspace 안에서만 생성",
            "- 기존 V8 경로와 V9 중앙 관리 정본 직접 수정 금지",
            "- 같은 Campaign/Revision과 다른 Session 결과 덮어쓰기 금지",
            "- Baseline 이후 같은 제품 파일 변경 감지 시 중단 후 Integration 요청",
            "",
            "## 배정 항목",
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
                "",
            ]
        lines += [
            "## 산출물 위치",
            f"- Result: `{assigned[0]['session_result_root']}`",
            f"- Evidence: `{assigned[0]['session_evidence_root']}`",
            f"- Temp: `{assigned[0]['session_temp_root']}`",
            f"- Artifact Manifest: `{assigned[0]['session_artifact_manifest']}`",
            f"- Handover: `{assigned[0]['session_handover_path']}`",
            "",
            "## 종료 인수인계",
            "- 생성·수정 파일 전수 Manifest",
            "- 제품 필수 파일과 정리 가능 비제품 산출물 분류",
            "- 사용자 승인 후 실행할 exact-path PowerShell 한 줄 삭제 명령",
            "- 정리 대상이 없으면 `정리 대상 없음`",
            "",
            "## Session Workspace 한 줄 정리 명령",
            "사용자 승인 후에만 실행한다.",
            "",
            "```powershell",
            session_cleanup,
            "```",
        ]
        (session_root/"REQUEST.md").write_text("\n".join(lines)+"\n",encoding="utf-8")
        (session_root/"HANDOVER.md").write_text(
            "# 세션 인수인계 미작성\n\n사용자가 요청할 때 현재 실제 결과를 기준으로 작성한다.\n",
            encoding="utf-8"
        )

    (campaign_root/"CAMPAIGN_CLEANUP_COMMAND.ps1").write_text(
        "# USER APPROVAL REQUIRED. Run only after merge, evidence retention and QA handoff.\n"
        + campaign_cleanup + "\n",
        encoding="utf-8"
    )

    for eid,sid in entity_to_session.items():
        state[eid]["assigned_session_id"]=sid
        state[eid]["assignment_revision"]=str(a.assignment_revision)
        state[eid]["state_revision"]=str(int(state[eid].get("state_revision") or 0)+1)
        state[eid]["updated_by"]="devgpt-control-v9-request-generator"

    write_csv(mgmt/"DEVELOPMENT_ITEM_STATE.csv",state_rows,state_rows[0].keys())
    refresh_views(mgmt)

    manifest={
        "campaign_id":a.campaign_id,
        "baseline_sha":a.baseline_sha,
        "assignment_revision":a.assignment_revision,
        "campaign_workspace_root":campaign_rel,
        "immutable_output":True,
        "active_items":len(rows),
        "session_count":len(sessions),
        "development_request_ids":sorted(development_request_id(a.campaign_id,a.assignment_revision,sid) for sid in sessions),
        "max_items_per_session":a.max_items_per_session,
        "assignment_source":"EXPLICIT_SESSION_PLAN" if a.session_plan else "AUTO_OWNER_CHUNKS",
        "session_plan":str(a.session_plan or ""),
        "session_workspace_pattern":f"{campaign_rel}/sessions/<session-id>",
        "campaign_cleanup_command":campaign_cleanup,
        "full_assignment_required":not a.allow_prebootstrap,
        "note":"All non-product Development GPT artifacts are isolated inside one campaign revision root."
    }
    write_json(campaign_root/"CAMPAIGN_MANIFEST.json",manifest)
    print(json.dumps(manifest,ensure_ascii=False,indent=2))
    return 0

if __name__=="__main__":
    raise SystemExit(main())
