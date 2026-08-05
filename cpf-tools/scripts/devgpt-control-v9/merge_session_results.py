#!/usr/bin/env python3
from __future__ import annotations
import argparse, re
from datetime import datetime, timezone
from pathlib import Path
from devgpt_control_lib import ManagementError, read_csv, refresh_views, write_csv

ALLOWED_DEVELOPMENT_STATES={"완료","미완료","재개발 요청","재검수 요청","해당 없음"}
SHA40=re.compile(r"^[0-9a-f]{40}$")


def validate_merge_rows(results, by_id, session_id: str, development_request_id: str):
    if not results:
        raise ManagementError("empty development result file")
    seen=set()
    for result in results:
        eid=(result.get("entity_id") or "").strip()
        if not eid or eid not in by_id:
            raise ManagementError(f"unknown entity_id: {eid}")
        if eid in seen:
            raise ManagementError(f"duplicate entity_id in result file: {eid}")
        seen.add(eid)
        assigned=(by_id[eid].get("assigned_session_id") or "").strip()
        if assigned != session_id:
            raise ManagementError(f"entity {eid} is assigned to {assigned or '<none>'}, not {session_id}")
        result_session=(result.get("session_id") or "").strip()
        if result_session and result_session != session_id:
            raise ManagementError(f"result session_id mismatch for {eid}: {result_session}")
        request=(result.get("development_request_id") or "").strip()
        if request != development_request_id:
            raise ManagementError(f"development_request_id mismatch for {eid}: {request or '<empty>'}")
        for field in ("개발GPT_수행상태","개발GPT_자체검수상태"):
            value=(result.get(field) or "").strip()
            if value and value not in ALLOWED_DEVELOPMENT_STATES:
                raise ManagementError(f"unsupported {field} for {eid}: {value}")
        completion=(result.get("completion_candidate_sha") or "").strip()
        if completion and not SHA40.fullmatch(completion):
            raise ManagementError(f"invalid completion_candidate_sha for {eid}: {completion}")
    return seen


def merge_rows(rows, results, session_id: str, development_request_id: str):
    by_id={r["entity_id"]:r for r in rows}
    validate_merge_rows(results,by_id,session_id,development_request_id)
    now=datetime.now(timezone.utc).isoformat()
    changed=0
    for result in results:
        eid=result["entity_id"].strip(); row=by_id[eid]
        row["개발GPT_수행상태"]=result.get("개발GPT_수행상태") or row["개발GPT_수행상태"]
        row["개발GPT_자체검수상태"]=result.get("개발GPT_자체검수상태") or row["개발GPT_자체검수상태"]
        row["changed_paths"]=result.get("changed_paths",""); row["evidence_ref"]=result.get("evidence_ref","")
        row["open_issue"]=result.get("open_issue",""); row["next_action"]=result.get("next_action","")
        completion_sha=result.get("completion_candidate_sha","").strip()
        complete=(row["개발GPT_수행상태"]=="완료" and row["개발GPT_자체검수상태"]=="완료" and bool(result.get("evidence_ref","").strip()) and bool(completion_sha))
        if complete:
            row["개발GPT_완료기준SHA"]=completion_sha
            row["개발GPT_완료revision"]=str(int(row.get("개발GPT_완료revision") or 0)+1)
            row["evidence_valid"]="true"; row["impact_invalidated"]="false"; row["qa_reopen_action"]=""
            row["개발GPT_작업대상상태"]="완료 스킵"
            row["assigned_session_id"]=""
        else:
            row["evidence_valid"]="false"; row["개발GPT_작업대상상태"]="작업 대상"
            # Keep the same assignment for incomplete work so it cannot be silently
            # reassigned or reported as finished by another session.
            row["assigned_session_id"]=session_id
        row["state_revision"]=str(int(row.get("state_revision") or 0)+1)
        row["updated_at"]=now; row["updated_by"]="development-result-merge"
        changed+=1
    return changed


def main()->int:
    p=argparse.ArgumentParser(description="Merge Development GPT session results into Development-owned state")
    p.add_argument("--repo-root",default="."); p.add_argument("--management-dir",default="cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9")
    p.add_argument("--results",required=True); p.add_argument("--session-id",required=True); p.add_argument("--development-request-id",required=True)
    a=p.parse_args(); root=Path(a.repo_root).resolve(); mgmt=root/a.management_dir
    rows=read_csv(mgmt/"DEVELOPMENT_ITEM_STATE.csv"); results=read_csv(Path(a.results))
    try:
        changed=merge_rows(rows,results,a.session_id,a.development_request_id)
    except ManagementError as exc:
        raise SystemExit(str(exc))
    write_csv(mgmt/"DEVELOPMENT_ITEM_STATE.csv",rows,rows[0].keys()); views=refresh_views(mgmt)
    print(f"Development results merged: {changed}; session={a.session_id}; request={a.development_request_id}; views={views}"); return 0
if __name__=="__main__": raise SystemExit(main())
