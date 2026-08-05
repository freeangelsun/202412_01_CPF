#!/usr/bin/env python3
from __future__ import annotations
import argparse
from datetime import datetime, timezone
from pathlib import Path
from devgpt_control_lib import ManagementError, read_csv, refresh_views, write_csv

ACTION_MAP={"REDEVELOP":"재개발 대상","REREVIEW":"재검수 대상","INVALIDATE_IMPACT":"재검수 대상","REOPEN_OWNER":"소유권 검토","EXTERNAL_BLOCK":"외부환경 차단"}


def validate_feed(feed, by_id):
    if not feed:
        raise ManagementError("empty QA reopen feed")
    seen=set()
    for item in feed:
        eid=(item.get("target_entity_id") or item.get("entity_id") or "").strip()
        if eid not in by_id: raise ManagementError(f"unknown target_entity_id: {eid}")
        if eid in seen: raise ManagementError(f"duplicate target_entity_id: {eid}")
        seen.add(eid)
        action=(item.get("action") or "").upper()
        if action not in ACTION_MAP: raise ManagementError(f"unsupported QA action: {action}")
    return seen


def apply_feed(rows, feed):
    by_id={r["entity_id"]:r for r in rows}; validate_feed(feed,by_id)
    now=datetime.now(timezone.utc).isoformat(); changed=0
    for item in feed:
        eid=(item.get("target_entity_id") or item.get("entity_id")).strip(); action=(item.get("action") or "").upper(); row=by_id[eid]
        row["qa_reopen_action"]=action; row["qa_reopen_revision"]=str(int(row.get("qa_reopen_revision") or 0)+1)
        row["개발GPT_작업대상상태"]=ACTION_MAP[action]
        row["개발GPT_작업대상사유"]=item.get("reason_summary") or item.get("reason_code") or action
        row["assigned_session_id"]=""; row["assignment_revision"]="0"
        if action=="REDEVELOP":
            row["개발GPT_수행상태"]="재개발 요청"; row["개발GPT_자체검수상태"]="미완료"
        elif action in {"REREVIEW","INVALIDATE_IMPACT"}:
            row["개발GPT_자체검수상태"]="재검수 요청"
        elif action=="REOPEN_OWNER":
            row["owner_resolved"]="false"; row["개발GPT_수행상태"]="미완료"; row["개발GPT_자체검수상태"]="미완료"
        elif action=="EXTERNAL_BLOCK":
            row["external_blocked"]="true"
        if action in {"REDEVELOP","REREVIEW","INVALIDATE_IMPACT"}:
            row["impact_invalidated"]="true"; row["impact_reason"]=item.get("reason_summary",""); row["evidence_valid"]="false"
        row["open_issue"]=item.get("reason_summary",""); row["next_action"]=item.get("required_fix","") or item.get("revalidation_commands","")
        row["state_revision"]=str(int(row.get("state_revision") or 0)+1); row["updated_at"]=now; row["updated_by"]="qa-reopen-feed"; changed+=1
    return changed


def main()->int:
    p=argparse.ArgumentParser(description="Apply QA reopen feed to Development GPT-owned state only")
    p.add_argument("--repo-root",default="."); p.add_argument("--management-dir",default="cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9"); p.add_argument("--feed",required=True)
    a=p.parse_args(); root=Path(a.repo_root).resolve(); mgmt=root/a.management_dir
    rows=read_csv(mgmt/"DEVELOPMENT_ITEM_STATE.csv"); feed=read_csv(Path(a.feed))
    try: changed=apply_feed(rows,feed)
    except ManagementError as exc: raise SystemExit(str(exc))
    write_csv(mgmt/"DEVELOPMENT_ITEM_STATE.csv",rows,rows[0].keys()); views=refresh_views(mgmt)
    print(f"QA reopen applied: {changed}; views={views}"); return 0
if __name__=="__main__": raise SystemExit(main())
