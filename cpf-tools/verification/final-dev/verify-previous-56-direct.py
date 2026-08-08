#!/usr/bin/env python3
from __future__ import annotations
import argparse,csv,shutil,tempfile
from pathlib import Path
class E(RuntimeError): pass
def req(x,m):
 if not x: raise E(m)
def verify(root,head):
 p=root/'cpf-docs/work/v9i/dev-final/PREVIOUS_56_FINDING_RESULT.csv';req(p.is_file(),'missing previous56')
 with p.open(encoding='utf-8-sig',newline='') as f: rows=list(csv.DictReader(f))
 req(len(rows)==56,f'rows={len(rows)}'); req(len({r['finding_id'] for r in rows})==56,'duplicate finding id')
 opened=set()
 for r in rows:
  fid=r['finding_id']; req(r['basis_sha']==head,fid+' sha'); req(r['development_status']=='완료',fid+' dev');
  for flag in ('direct_source_checked','direct_consumer_checked','direct_test_checked'): req(r.get(flag)=='Y',fid+' '+flag)
  for key in ('source_path','consumer_path','test_or_harness'):
   rel=r.get(key,''); req(rel and '..' not in Path(rel).parts,fid+' '+key); fp=root/rel; req(fp.is_file(),fid+' missing '+rel);req(fp.read_bytes().strip(),fid+' empty '+rel);opened.add(rel)
  req(fid in r.get('resolution',''),fid+' resolution not id-specific'); req(r.get('call_path','').count('->')>=2,fid+' call path')
 return len(opened)
def mutate(root,col,value):
 p=root/'cpf-docs/work/v9i/dev-final/PREVIOUS_56_FINDING_RESULT.csv'
 with p.open(encoding='utf-8-sig',newline='') as f: rows=list(csv.DictReader(f));fields=list(rows[0])
 rows[0][col]=value
 with p.open('w',encoding='utf-8-sig',newline='') as f:w=csv.DictWriter(f,fieldnames=fields);w.writeheader();w.writerows(rows)
def main():
 a=argparse.ArgumentParser();a.add_argument('--root',type=Path,default=Path('.'));a.add_argument('--expected-head',required=True);a.add_argument('--self-test',action='store_true');x=a.parse_args();root=x.root.resolve();n=verify(root,x.expected_head)
 if x.self_test:
  for name,col,val in [('path','consumer_path','missing/nope'),('flag','direct_test_checked','N'),('resolution','resolution','generic')]:
   with tempfile.TemporaryDirectory(prefix='cpf-prev56-mut-') as td:
    mr=Path(td)/'root';shutil.copytree(root,mr);mutate(mr,col,val)
    try:verify(mr,x.expected_head)
    except E:pass
    else:raise E(name+' mutation survived')
 print(f'[CPF][FINAL][PREVIOUS56][PASS] rows=56 directSource=56 directConsumer=56 directTest=56 openedFiles={n} head={x.expected_head} selfTest={str(x.self_test).lower()}')
if __name__=='__main__':
 try:main()
 except E as e:print('[CPF][FINAL][PREVIOUS56][FAIL] '+str(e));raise SystemExit(1)
