#!/usr/bin/env python3
from __future__ import annotations
import argparse,csv,json,re
from pathlib import Path
ALLOWED={"완료","부분 구현","미구현","미검증","실패","재확인 필요"}
def load(p,k):
 with p.open(encoding="utf-8-sig",newline="") as f:d=list(csv.DictReader(f))
 ids=[r[k] for r in d]
 if len(ids)!=len(set(ids)):raise ValueError("duplicate "+k)
 return d
def main():
 a=argparse.ArgumentParser();a.add_argument("--root",default=".");a.add_argument("--release",action="store_true");x=a.parse_args();r=Path(x.root).resolve();fail=[]
 q=load(r/"cpf-docs/quality/CPF_20260731_QA33_REQUIREMENT_MATRIX.csv","requirement_id")
 s=load(r/"cpf-docs/quality/CPF_20260731_QA33_SCENARIO_MATRIX.csv","scenario_id")
 p=r/"cpf-docs/quality/CPF_20260731_QA33_RESULT_MATRIX.csv"
 rows=[] if not p.is_file() else load(p,"record_id")
 if not p.is_file():fail.append("result matrix missing")
 by={(z.get("record_type"),z.get("record_id")):z for z in rows}
 for typ,items,key in [("REQUIREMENT",q,"requirement_id"),("SCENARIO",s,"scenario_id")]:
  for item in items:
   rid=item[key];z=by.get((typ,rid))
   if not z:fail.append(f"missing {typ}:{rid}");continue
   for f in ("development_status","verification_status"):
    if z.get(f) not in ALLOWED:fail.append(f"invalid status:{typ}:{rid}:{f}")
   if x.release and (z.get("development_status")!="완료" or z.get("verification_status")!="완료"):fail.append(f"not complete:{typ}:{rid}")
   if x.release:
    ev=z.get("evidence_path","");ep=r/ev
    if not ev or not ep.is_file():fail.append(f"evidence missing:{typ}:{rid}")
    else:
     data=json.loads(ep.read_text(encoding="utf-8"));sha=data.get("sourceSha","")
     if not re.fullmatch(r"[0-9a-f]{40}",sha):fail.append(f"bad sha:{typ}:{rid}")
     if data.get("exitCode")!=0:fail.append(f"nonzero evidence:{typ}:{rid}")
 out={"status":"PASS" if not fail else "FAIL","release":x.release,"failures":fail,"requirements":len(q),"scenarios":len(s)}
 print(json.dumps(out,ensure_ascii=False,indent=2));return 0 if not fail else 1
if __name__=="__main__":raise SystemExit(main())
