#!/usr/bin/env python3
from __future__ import annotations
import argparse,csv,json,subprocess,sys
from pathlib import Path

def ids(path,key):
    with path.open(encoding='utf-8-sig',newline='') as f:return [r[key] for r in csv.DictReader(f)]
def main():
    ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--release',action='store_true');ap.add_argument('--json-report');a=ap.parse_args();root=Path(a.root).resolve();fail=[]
    req=ids(root/'cpf-docs/quality/CPF_20260730_QA32_REQUIREMENT_MATRIX.csv','requirement_id')
    defects=ids(root/'cpf-docs/quality/CPF_20260730_QA32_DEFECT_REGISTER.csv','defect_id')
    scenarios=ids(root/'cpf-docs/quality/CPF_20260730_QA32_SCENARIO_MATRIX.csv','scenario_id')
    result=root/'cpf-docs/quality/CPF_20260730_QA32_RESULT_MATRIX.csv'
    if not result.is_file(): fail.append('result matrix missing'); rows=[]
    else:
      with result.open(encoding='utf-8-sig',newline='') as f: rows=list(csv.DictReader(f))
    by={(r['record_type'],r['record_id']):r for r in rows}
    for typ,values in [('REQUIREMENT',req),('DEFECT',defects),('SCENARIO',scenarios)]:
      for i in values:
        r=by.get((typ,i))
        if not r: fail.append(f'missing result row {typ}:{i}');continue
        if r['development_status'] not in ('완료','DEVELOPMENT_COMPLETE'):fail.append(f'development incomplete {typ}:{i}:{r["development_status"]}')
        if a.release and r['verification_status'] not in ('완료','VERIFIED'):fail.append(f'unverified release row {typ}:{i}:{r["verification_status"]}')
    p=subprocess.run([sys.executable,str(root/'cpf-tools/scripts/verify-cpf-qa32-primary-engines.py'),'--root',str(root)],capture_output=True,text=True)
    if p.returncode: fail.append('primary engine gate failed')
    report={'requirements':len(req),'defects':len(defects),'scenarios':len(scenarios),'releaseMode':a.release,'failures':fail,'status':'PASS' if not fail else 'FAIL'}
    if a.json_report:
      out=Path(a.json_report);out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(report,ensure_ascii=False,indent=2));return 0 if not fail else 1
if __name__=='__main__':raise SystemExit(main())
