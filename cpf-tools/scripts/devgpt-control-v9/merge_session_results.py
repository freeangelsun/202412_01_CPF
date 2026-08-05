#!/usr/bin/env python3
from __future__ import annotations
import argparse
from datetime import datetime, timezone
from pathlib import Path
from devgpt_control_lib import read_csv, refresh_views, write_csv

def main()->int:
    p=argparse.ArgumentParser(description="Merge Development GPT session results into Development-owned state")
    p.add_argument("--repo-root",default="."); p.add_argument("--management-dir",default="cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9"); p.add_argument("--results",required=True)
    a=p.parse_args(); root=Path(a.repo_root).resolve(); mgmt=root/a.management_dir
    rows=read_csv(mgmt/"DEVELOPMENT_ITEM_STATE.csv"); by_id={r["entity_id"]:r for r in rows}; results=read_csv(Path(a.results)); changed=0
    for result in results:
        eid=result["entity_id"]
        if eid not in by_id: raise SystemExit(f"unknown entity_id: {eid}")
        row=by_id[eid]
        row["개발GPT_수행상태"]=result.get("개발GPT_수행상태") or row["개발GPT_수행상태"]
        row["개발GPT_자체검수상태"]=result.get("개발GPT_자체검수상태") or row["개발GPT_자체검수상태"]
        row["changed_paths"]=result.get("changed_paths",""); row["evidence_ref"]=result.get("evidence_ref",""); row["open_issue"]=result.get("open_issue",""); row["next_action"]=result.get("next_action","")
        completion_sha=result.get("completion_candidate_sha","").strip()
        complete=row["개발GPT_수행상태"]=="완료" and row["개발GPT_자체검수상태"]=="완료" and bool(result.get("evidence_ref","").strip()) and bool(completion_sha)
        if complete:
            row["개발GPT_완료기준SHA"]=completion_sha; row["개발GPT_완료revision"]=str(int(row.get("개발GPT_완료revision") or 0)+1); row["evidence_valid"]="true"; row["impact_invalidated"]="false"; row["qa_reopen_action"]=""; row["개발GPT_작업대상상태"]="완료 스킵"
        else:
            row["evidence_valid"]="false"; row["개발GPT_작업대상상태"]="작업 대상"
        row["assigned_session_id"]=""; row["state_revision"]=str(int(row.get("state_revision") or 0)+1); row["updated_at"]=datetime.now(timezone.utc).isoformat(); row["updated_by"]="development-result-merge"
        changed+=1
    write_csv(mgmt/"DEVELOPMENT_ITEM_STATE.csv",rows,rows[0].keys()); views=refresh_views(mgmt)
    print(f"Development results merged: {changed}; views={views}"); return 0
if __name__=="__main__": raise SystemExit(main())
