#!/usr/bin/env python3
from __future__ import annotations
import argparse,csv,hashlib,json
from pathlib import Path
FILES=[
"cpf-docs/work/current/CPF_20260731_QA33_PACKAGE_INDEX.md",
"cpf-docs/work/current/CPF_20260731_QA33_GPT_DEVELOPMENT_INSTRUCTION.md",
"cpf-docs/work/current/CPF_20260731_QA33_DEVELOPMENT_AND_VERIFICATION_REQUEST.md",
"cpf-docs/work/review/CPF_20260731_QA32_INDEPENDENT_SOURCE_REVIEW.md",
"cpf-docs/quality/CPF_20260731_QA33_DEFECT_REGISTER.csv",
"cpf-docs/quality/CPF_20260731_QA33_REQUIREMENT_MATRIX.csv",
"cpf-docs/quality/CPF_20260731_QA33_SCENARIO_MATRIX.csv",
"cpf-docs/quality/CPF_20260731_QA33_SOURCE_INSPECTION_MATRIX.csv",
"cpf-docs/quality/CPF_20260731_QA33_EVIDENCE_MATRIX.csv",
"cpf-docs/work/handover/CPF_20260731_QA33_REVIEW_HANDOVER.md",
"cpf-docs/work/manifest/CPF_20260731_QA33_QA_REQUIREMENTS_MANIFEST.json"]
def rows(p,k):
 with p.open(encoding="utf-8-sig",newline="") as f:d=list(csv.DictReader(f))
 ids=[r[k] for r in d]
 if len(ids)!=len(set(ids)) or any(not x.strip() for x in ids):raise ValueError(f"invalid {k}:{p}")
 return d
def main():
 a=argparse.ArgumentParser();a.add_argument("--root",default=".");x=a.parse_args();r=Path(x.root).resolve();fail=[]
 for f in FILES:
  if not (r/f).is_file():fail.append("missing:"+f)
 try:
  d=rows(r/"cpf-docs/quality/CPF_20260731_QA33_DEFECT_REGISTER.csv","defect_id")
  q=rows(r/"cpf-docs/quality/CPF_20260731_QA33_REQUIREMENT_MATRIX.csv","requirement_id")
  s=rows(r/"cpf-docs/quality/CPF_20260731_QA33_SCENARIO_MATRIX.csv","scenario_id")
  i=rows(r/"cpf-docs/quality/CPF_20260731_QA33_SOURCE_INSPECTION_MATRIX.csv","inspection_id")
  e=rows(r/"cpf-docs/quality/CPF_20260731_QA33_EVIDENCE_MATRIX.csv","evidence_id")
  qids={z["requirement_id"] for z in q}; c={}
  for z in s:
   if z["requirement_id"] not in qids:fail.append("orphan:"+z["scenario_id"])
   c[z["requirement_id"]]=c.get(z["requirement_id"],0)+1
  for k in qids:
   if c.get(k)!=3:fail.append(f"scenario coverage:{k}:{c.get(k,0)}")
 except Exception as ex:
  fail.append(str(ex));d=q=s=i=e=[]
 m=r/"cpf-docs/work/manifest/CPF_20260731_QA33_QA_REQUIREMENTS_MANIFEST.json"
 if m.is_file():
  data=json.loads(m.read_text(encoding="utf-8"))
  for z in data.get("files",[]):
   p=r/z["path"]
   if not p.is_file():fail.append("manifest missing:"+z["path"])
   elif hashlib.sha256(p.read_bytes()).hexdigest()!=z["sha256"]:fail.append("hash mismatch:"+z["path"])
 out={"status":"PASS" if not fail else "FAIL","failures":fail,"counts":{"defects":len(d),"requirements":len(q),"scenarios":len(s),"inspections":len(i),"evidence":len(e)}}
 print(json.dumps(out,ensure_ascii=False,indent=2));return 0 if not fail else 1
if __name__=="__main__":raise SystemExit(main())
