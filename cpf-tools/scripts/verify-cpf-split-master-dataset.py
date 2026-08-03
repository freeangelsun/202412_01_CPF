#!/usr/bin/env python3
"""Strict split-master verifier. Git HEAD is runtime evidence; no hard-coded baseline is accepted."""
from __future__ import annotations
import argparse,csv,hashlib,json,re,subprocess,sys
from pathlib import Path
INDEXES={
 'requirement':('cpf-docs/work/current/CPF_REQUIREMENT_MASTER.csv','requirement_id',re.compile(r'^CPF-(?:FR|NFR|QA|SELF)-[A-Z0-9-]+$')),
 'scenario':('cpf-docs/work/current/CPF_SCENARIO_MASTER.csv','scenario_id',re.compile(r'^CPF-SC-[A-Z0-9-]+$')),
 'execution':('cpf-docs/work/current/CPF_EXECUTION_SEQUENCE.csv','execution_order',re.compile(r'^\d{2}-\d{8}$')),
}
REQUIRED_INDEX={'part_sequence','part_path','part_record_count','first_record_id','last_record_id','size_bytes','sha256','logical_record_count'}
class GateError(RuntimeError):pass
def git(root,*args):
 p=subprocess.run(['git','-C',str(root),*args],text=True,capture_output=True)
 if p.returncode:raise GateError(f"git {' '.join(args)} failed: {p.stderr.strip()}")
 return p.stdout.strip()
def sha(p):
 h=hashlib.sha256();h.update(p.read_bytes());return h.hexdigest()
def rows(p):
 with p.open(encoding='utf-8-sig',newline='') as f:r=csv.DictReader(f);return list(r.fieldnames or []),[{k:(v or '').strip() for k,v in x.items()} for x in r]
def verify(root:Path,expected_sha:str|None=None,require_clean=False):
 head=git(root,'rev-parse','HEAD')
 if expected_sha and head!=expected_sha:raise GateError(f'HEAD mismatch expected={expected_sha} actual={head}')
 status=git(root,'status','--porcelain')
 if require_clean and status:raise GateError('working tree is not clean')
 result={'status':'PASS','verifiedAgainstSha':head,'workingTreeClean':not bool(status),'datasets':{},'findings':[]}
 logical={}
 for kind,(idxrel,idcol,idrx) in INDEXES.items():
  idx=root/idxrel
  fields,index=rows(idx)
  missing=REQUIRED_INDEX-set(fields)
  if missing:raise GateError(f'{idxrel}: missing columns {sorted(missing)}')
  seq=[int(x['part_sequence']) for x in index]
  if seq!=list(range(1,len(index)+1)):raise GateError(f'{idxrel}: part sequence is not contiguous')
  allrows=[];seen=set();partmeta=[]
  for x in index:
   rel=x['part_path'];p=(root/rel).resolve()
   if root not in p.parents or not p.is_file():raise GateError(f'unsafe/missing part {rel}')
   pf,pr=rows(p)
   if idcol not in pf:raise GateError(f'{rel}: missing {idcol}')
   ids=[]
   for n,row in enumerate(pr,2):
    rid=row.get(idcol,'')
    if not idrx.fullmatch(rid):raise GateError(f'{rel}:{n}: malformed {idcol}={rid!r}')
    if rid in seen:raise GateError(f'{rel}:{n}: duplicate {idcol}={rid}')
    seen.add(rid);ids.append(rid);allrows.append(row)
   if len(pr)!=int(x['part_record_count']) or (ids and (ids[0]!=x['first_record_id'] or ids[-1]!=x['last_record_id'])):raise GateError(f'{rel}: declared part metadata mismatch')
   if p.stat().st_size!=int(x['size_bytes']) or sha(p)!=x['sha256']:raise GateError(f'{rel}: byte/hash metadata mismatch')
   partmeta.append({'path':rel,'count':len(pr),'sha256':sha(p)})
  declared=int(index[0]['logical_record_count'])
  if declared!=len(allrows):raise GateError(f'{idxrel}: logical count mismatch declared={declared} actual={len(allrows)}')
  logical[kind]=allrows;result['datasets'][kind]={'count':len(allrows),'parts':partmeta}
 req={r['requirement_id'] for r in logical['requirement']};sc={r['scenario_id'] for r in logical['scenario']}
 previous=None;non_gate={};wp_positions={}
 for pos,row in enumerate(logical['execution'],1):
  order=row['execution_order'];phase=row.get('phase_id','')
  if phase and order[:2]!=phase.removeprefix('P').zfill(2):raise GateError(f'execution row {pos}: phase/order mismatch {phase}/{order}')
  numeric=(int(order[:2]),int(order[3:]));
  if previous is not None and numeric<=previous:raise GateError(f'execution row {pos}: order not strictly ascending {order}')
  previous=numeric
  if numeric[1]!=99999999:
   expected=non_gate.get(numeric[0],numeric[1]);
   if numeric[1]!=expected:raise GateError(f'execution row {pos}: non-gate sequence gap/reversal {order} expected={numeric[0]:02d}-{expected:08d}')
   non_gate[numeric[0]]=numeric[1]+1
  rid=row.get('requirement_id','');sid=row.get('scenario_id','')
  if rid not in req:raise GateError(f'execution row {pos}: unknown requirement {rid}')
  if sid and sid not in sc:raise GateError(f'execution row {pos}: unknown scenario {sid}')
  wp=row.get('work_package_id','')
  if wp:wp_positions.setdefault(wp,[]).append(pos)
 for wp,positions in wp_positions.items():
  if positions!=list(range(min(positions),max(positions)+1)):raise GateError(f'work package not contiguous: {wp}')
 result['crossLinks']={'requirementCount':len(req),'scenarioCount':len(sc),'executionCount':len(logical['execution']),'missing':0}
 return result

def main():
 p=argparse.ArgumentParser();p.add_argument('--root',default='.');p.add_argument('--expected-sha');p.add_argument('--require-clean',action='store_true');p.add_argument('--json-output');a=p.parse_args();root=Path(a.root).resolve()
 try:r=verify(root,a.expected_sha,a.require_clean);code=0
 except Exception as e:r={'status':'FAIL','message':str(e)};code=1
 if a.json_output:
  o=Path(a.json_output);o=o if o.is_absolute() else root/o;o.parent.mkdir(parents=True,exist_ok=True);o.write_text(json.dumps(r,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(json.dumps(r,ensure_ascii=False));return code
if __name__=='__main__':raise SystemExit(main())
