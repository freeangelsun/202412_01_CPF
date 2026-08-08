#!/usr/bin/env python3
from __future__ import annotations
import argparse,csv,re,shutil,tempfile
from pathlib import Path
class E(RuntimeError): pass
def req(x,m):
    if not x: raise E(m)
def canonical(root):
    p=root/'cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md'; req(p.is_file(),'missing canonical requirements')
    out=[]
    for line in p.read_text(encoding='utf-8').splitlines():
        m=re.match(r'^\| `([^`]+)` \| ([^|]+) \| ([^|]+) \| ([^|]+) \|$',line)
        if m: out.append(tuple(x.strip() for x in m.groups()))
    req(len(out)==169,f'canonical count={len(out)}')
    return out
def verify(root:Path,head:str):
    expected=canonical(root); p=root/'cpf-docs/work/v9i/dev-final/CANONICAL_169_STATUS.csv'; req(p.is_file(),'missing CANONICAL_169_STATUS.csv')
    with p.open(encoding='utf-8-sig',newline='') as f: rows=list(csv.DictReader(f))
    req(len(rows)==169,f'ledger rows={len(rows)}')
    req([r['canonical_id'] for r in rows]==[x[0] for x in expected],'canonical exact order/id drift')
    byid={x[0]:x for x in expected}; opened=set()
    for r in rows:
        rid=r['canonical_id']; owner,goal,proof=byid[rid][1:]
        req(r.get('basis_sha')==head,rid+' basis sha mismatch')
        req(r.get('development_status')=='완료',rid+' development not complete')
        req(r.get('overall_status') in {'미검증','완료'},rid+' invalid overall')
        for flag in ('direct_source_checked','direct_consumer_checked','direct_test_checked'): req(r.get(flag)=='Y',rid+' '+flag+' != Y')
        for key in ('source_path','consumer_path','test_or_harness'):
            rel=r.get(key,'').strip(); req(rel and not rel.startswith('/') and '..' not in Path(rel).parts,rid+' invalid '+key)
            fp=root/rel; req(fp.is_file(),rid+' missing '+key+': '+rel); data=fp.read_bytes(); req(data.strip(),rid+' empty '+key); opened.add(rel)
        link=r.get('acceptance_link',''); req(rid in link,rid+' acceptance missing id'); req(goal[:18] in link,rid+' acceptance not tied to canonical goal')
        req(r.get('call_path','').count('->')>=2,rid+' call path incomplete')
    return len(opened)
def main():
    ap=argparse.ArgumentParser();ap.add_argument('--root',type=Path,default=Path('.'));ap.add_argument('--expected-head',required=True);ap.add_argument('--self-test',action='store_true');a=ap.parse_args();root=a.root.resolve(); opened=verify(root,a.expected_head)
    if a.self_test:
        muts=[
          ('path',lambda r: mutate_csv(r,'source_path','missing/NOPE.java')),
          ('flag',lambda r: mutate_csv(r,'direct_consumer_checked','N')),
          ('acceptance',lambda r: mutate_csv(r,'acceptance_link','generic evidence')),
        ]
        for name,mut in muts:
          with tempfile.TemporaryDirectory(prefix='cpf-canonical169-mut-') as td:
            mr=Path(td)/'root';shutil.copytree(root,mr);mut(mr)
            try: verify(mr,a.expected_head)
            except E: pass
            else: raise E(name+' mutation survived')
    print(f'[CPF][FINAL][CANONICAL169][PASS] rows=169 directSource=169 directConsumer=169 directTest=169 openedFiles={opened} head={a.expected_head} selfTest={str(a.self_test).lower()}')
def mutate_csv(root:Path,col:str,value:str):
    p=root/'cpf-docs/work/v9i/dev-final/CANONICAL_169_STATUS.csv'
    with p.open(encoding='utf-8-sig',newline='') as f: rows=list(csv.DictReader(f)); fields=list(rows[0])
    rows[0][col]=value
    with p.open('w',encoding='utf-8-sig',newline='') as f: w=csv.DictWriter(f,fieldnames=fields);w.writeheader();w.writerows(rows)
if __name__=='__main__':
    try: main()
    except E as e: print('[CPF][FINAL][CANONICAL169][FAIL] '+str(e));raise SystemExit(1)
