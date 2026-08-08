#!/usr/bin/env python3
"""Validates per-Review-ID Developer direct Source/Consumer/Test trace for Special Review 1000."""
from __future__ import annotations
import argparse,csv,re,subprocess,sys
from collections import Counter
from pathlib import Path
class GateError(RuntimeError): pass
REQUIRED=('review_id','source_path','symbol','consumer_path','test_or_harness','execution_readiness','development_status','verification_status','acceptance_link','evidence','basis_sha','direct_source_checked','direct_consumer_checked','direct_test_checked')
ALLOWED_DEV={'완료','미완료','재개발 요청','재검수 요청','해당 없음'}
ALLOWED_VER={'완료','미검증','실패','재확인 필요','해당 없음','미확인'}

def req(ok,msg):
    if not ok:raise GateError(msg)
def rows(path):
    with path.open(encoding='utf-8-sig',newline='') as f:return list(csv.DictReader(f))
def validate(root:Path, expected_head:str|None=None):
    special=root/'cpf-docs/work/v9i/final-control/SPECIAL_REVIEW_1000.csv'
    trace=root/'cpf-docs/work/v9i/dev-final/SOURCE_CONSUMER_TRACE.csv'
    req(special.is_file(),'missing central Special 1000 input');req(trace.is_file(),'missing developer SOURCE_CONSUMER_TRACE.csv')
    expected=rows(special); actual=rows(trace)
    req(len(expected)==1000,'central Special denominator must be 1000')
    req(actual and all(k in actual[0] for k in REQUIRED),'SOURCE_CONSUMER_TRACE schema missing Special direct fields')
    by_id={r['review_id']:r for r in actual if r.get('review_id','').startswith('CPF-RV-')}
    req(len(by_id)==1000,'developer trace must contain exactly one row for each CPF-RV id')
    expected_ids=[r['review_id'] for r in expected];req(set(by_id)==set(expected_ids),'Special Review ID set mismatch')
    if expected_head:
        head=expected_head.strip().lower()
        req(re.fullmatch(r'[0-9a-f]{40}',head) is not None,'--expected-head must be a 40-char commit SHA')
        mode='PREPUSH-COMPOSITE'
    else:
        resolved=subprocess.run(['git','-C',str(root),'rev-parse','HEAD'],capture_output=True,text=True,timeout=20)
        req(resolved.returncode==0,'full git checkout required unless explicit --expected-head is supplied for pre-push composite validation')
        head=resolved.stdout.strip().lower();mode='FULL-CHECKOUT'
    duplicate_evidence=Counter(); checked=0
    for base in expected:
        rid=base['review_id'];r=by_id[rid]
        req(r.get('development_status') in ALLOWED_DEV,f'{rid}: invalid development_status')
        req(r.get('verification_status') in ALLOWED_VER,f'{rid}: invalid verification_status')
        for key in ('source_path','symbol','consumer_path','test_or_harness','acceptance_link','evidence','basis_sha'):
            req(str(r.get(key,'')).strip(),f'{rid}: blank {key}')
        req(r.get('basis_sha','').strip().lower()==head,f'{rid}: basis_sha does not match qualification SHA {head}')
        # Direct check flags are strict. If any is N, the row cannot be Developer-complete.
        flags=[r.get('direct_source_checked'),r.get('direct_consumer_checked'),r.get('direct_test_checked')]
        if flags==['Y','Y','Y']:
            checked+=1
        else:
            req(r.get('development_status')!='완료',f'{rid}: completion claimed without direct Source/Consumer/Test confirmation')
            req(r.get('verification_status') in {'미확인','미검증','실패','재확인 필요'},f'{rid}: unchecked row has invalid verification state')
        if r.get('verification_status')=='미검증':
            ready=r.get('execution_readiness','')
            req('READY' in ready and 'COMMAND=' in ready and 'ENV=' in ready,f'{rid}: 미검증 requires ready command+environment, not missing implementation')
        # No generic copied evidence blob can stand in for an individual acceptance link.
        req(rid in r['evidence'] or rid in r['acceptance_link'],f'{rid}: evidence/acceptance must carry Review-ID identity')
        req(base['review_point'][:18] in r['acceptance_link'] or base['acceptance_criteria'][:18] in r['acceptance_link'],f'{rid}: acceptance linkage is generic/not point-specific')
        duplicate_evidence[r['evidence']]+=1
        # When a path looks repository-relative, open it. Multiple paths use ';'.
        if r.get('direct_source_checked')=='Y':
            for rel in [x.strip() for x in r['source_path'].split(';') if x.strip()]:
                req(not Path(rel).is_absolute() and '..' not in Path(rel).parts,f'{rid}: source path must be root-relative: {rel}')
                p=root/rel;req(p.is_file(),f'{rid}: directly checked source missing: {rel}');_ = p.read_text(encoding='utf-8',errors='replace')
        if r.get('direct_consumer_checked')=='Y':
            for rel in [x.strip() for x in r['consumer_path'].split(';') if x.strip()]:
                req(not Path(rel).is_absolute() and '..' not in Path(rel).parts,f'{rid}: consumer path must be root-relative: {rel}')
                p=root/rel;req(p.is_file(),f'{rid}: directly checked consumer missing: {rel}');_ = p.read_text(encoding='utf-8',errors='replace')
        if r.get('direct_test_checked')=='Y':
            for rel in [x.strip() for x in r['test_or_harness'].split(';') if x.strip()]:
                req(not Path(rel).is_absolute() and '..' not in Path(rel).parts,f'{rid}: test/harness path must be root-relative: {rel}')
                p=root/rel;req(p.is_file(),f'{rid}: directly checked test/harness missing: {rel}');_ = p.read_text(encoding='utf-8',errors='replace')
    req(checked==1000,'Special 1000 direct review is incomplete; unchecked rows cannot be hidden as complete')
    # Evidence paths can be shared; exact evidence text may not be a one-line generic claim for hundreds of points.
    maxdup=max(duplicate_evidence.values()) if duplicate_evidence else 0
    req(maxdup<100,'single generic evidence field reused for >=100 Special points')
    print(f'[CPF][SPECIAL-1000-DIRECT][PASS] rows=1000 direct={checked} head={head} mode={mode}')

def self_test():
    req('CPF-RV-0001'.startswith('CPF-RV-'),'id parser failed');print('[CPF][SPECIAL-1000-DIRECT][SELF-TEST][PASS]')
def main():
    ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--expected-head');ap.add_argument('--self-test',action='store_true');ns=ap.parse_args()
    if ns.self_test:self_test();return 0
    validate(Path(ns.root).resolve(),ns.expected_head);return 0
if __name__=='__main__':
    try:raise SystemExit(main())
    except GateError as e:print('[CPF][SPECIAL-1000-DIRECT][FAIL] '+str(e),file=sys.stderr);raise SystemExit(1)
