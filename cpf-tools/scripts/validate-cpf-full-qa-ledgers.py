#!/usr/bin/env python3
from __future__ import annotations
import argparse,csv,hashlib,json,re
from pathlib import Path
REQ_RE=re.compile(r"^(?:CPF-FR-\d{6}|CPF-GATE-\d{2})$"); SC_RE=re.compile(r"^CPF-SC-\d{6}$")
def read(path):
    with Path(path).open(encoding="utf-8-sig",newline="") as h: return list(csv.DictReader(h))
def digest(path): return hashlib.sha256(Path(path).read_bytes()).hexdigest()
def main()->int:
    p=argparse.ArgumentParser(); p.add_argument("--requirements",required=True); p.add_argument("--scenarios",required=True); p.add_argument("--expected-requirements",type=int,required=True); p.add_argument("--expected-scenarios",type=int,required=True); p.add_argument("--expected-sha",required=True); p.add_argument("--json-output"); a=p.parse_args()
    try:
        req=read(a.requirements); sc=read(a.scenarios)
        if len(req)!=a.expected_requirements: raise ValueError(f"requirements={len(req)} expected={a.expected_requirements}")
        if len(sc)!=a.expected_scenarios: raise ValueError(f"scenarios={len(sc)} expected={a.expected_scenarios}")
        req_ids=[r.get("requirement_id","") for r in req]; sc_ids=[r.get("scenario_id","") for r in sc]
        req_set=set(req_ids); sc_set=set(sc_ids)
        if any(not REQ_RE.fullmatch(x) for x in req_ids) or len(req_set)!=len(req_ids): raise ValueError("invalid/duplicate requirement IDs")
        if any(not SC_RE.fullmatch(x) for x in sc_ids) or len(sc_set)!=len(sc_ids): raise ValueError("invalid/duplicate scenario IDs")
        for row in req:
            if row.get("baseline_sha")!=a.expected_sha: raise ValueError(f"requirement baseline mismatch {row.get('requirement_id')}")
            linked={x for x in row.get("scenario_ids","").split(";") if x}
            if not linked: raise ValueError(f"requirement without scenario_ids {row.get('requirement_id')}")
            if not linked.issubset(sc_set): raise ValueError(f"unknown scenario link {row.get('requirement_id')}")
        scenario_by_req={rid:0 for rid in req_set}
        for row in sc:
            if row.get("baseline_sha")!=a.expected_sha: raise ValueError(f"scenario baseline mismatch {row.get('scenario_id')}")
            rid=row.get("linked_requirement_id","")
            if rid not in req_set: raise ValueError(f"unknown linked requirement {rid}")
            scenario_by_req[rid]+=1
        missing=[rid for rid,n in scenario_by_req.items() if n==0]
        if missing: raise ValueError(f"requirements without scenarios: {missing[:5]}")
        result={"status":"PASS","requirements":len(req),"scenarios":len(sc),"requirementSha256":digest(a.requirements),"scenarioSha256":digest(a.scenarios),"qaReviewedRequirements":sum(r.get('QA_검수여부') in ('예','완료') for r in req),"qaReviewedScenarios":sum(r.get('QA_검수여부') in ('예','완료') for r in sc),"meaning":"coverage/traceability validation only"}; code=0
    except Exception as e: result={"status":"FAIL","message":str(e)}; code=1
    text=json.dumps(result,ensure_ascii=False,indent=2); print(text)
    if a.json_output: Path(a.json_output).write_text(text+"\n",encoding="utf-8")
    return code
if __name__=="__main__": raise SystemExit(main())
