#!/usr/bin/env python3
from __future__ import annotations
import argparse
from datetime import datetime, timezone
from pathlib import Path
from development_management_lib import read_csv, refresh_views, write_csv

ACTION_MAP={"REDEVELOP":"재개발 대상","REREVIEW":"재검수 대상","INVALIDATE_IMPACT":"재검수 대상","REOPEN_OWNER":"소유권 검토","EXTERNAL_BLOCK":"외부환경 차단"}

def main()->int:
    p=argparse.ArgumentParser(description="Apply QA reopen feed to Development GPT-owned state only")
    p.add_argument("--repo-root",default="."); p.add_argument("--management-dir",default="cpf-docs/work/current/CPF_DEVELOPMENT_MANAGEMENT_V8"); p.add_argument("--feed",required=True)
    a=p.parse_args(); root=Path(a.repo_root).resolve(); mgmt=root/a.management_dir
    rows=read_csv(mgmt/"DEVELOPMENT_ITEM_STATE.csv"); by_id={r["entity_id"]:r for r in rows}; feed=read_csv(Path(a.feed))
    changed=0
    for item in feed:
        eid=item.get("target_entity_id") or item.get("entity_id")
        if eid not in by_id: raise SystemExit(f"unknown target_entity_id: {eid}")
        action=(item.get("action") or "").upper()
        if action not in ACTION_MAP: raise SystemExit(f"unsupported QA action: {action}")
        row=by_id[eid]; row["qa_reopen_action"]=action; row["qa_reopen_revision"]=str(int(row.get("qa_reopen_revision") or 0)+1)
        row["개발GPT_작업대상상태"]=ACTION_MAP[action]
        row["개발GPT_작업대상사유"]=item.get("reason_summary") or item.get("reason_code") or action
        if action in {"REDEVELOP","REREVIEW","INVALIDATE_IMPACT"}:
            row["impact_invalidated"]="true"; row["impact_reason"]=item.get("reason_summary",""); row["evidence_valid"]="false"
        if action=="REOPEN_OWNER": row["owner_resolved"]="false"
        if action=="EXTERNAL_BLOCK": row["external_blocked"]="true"
        row["open_issue"]=item.get("reason_summary",""); row["next_action"]=item.get("required_fix","") or item.get("revalidation_commands","")
        row["state_revision"]=str(int(row.get("state_revision") or 0)+1); row["updated_at"]=datetime.now(timezone.utc).isoformat(); row["updated_by"]="qa-reopen-feed"
        changed+=1
    write_csv(mgmt/"DEVELOPMENT_ITEM_STATE.csv",rows,rows[0].keys()); views=refresh_views(mgmt)
    print(f"QA reopen applied: {changed}; views={views}"); return 0
if __name__=="__main__": raise SystemExit(main())
