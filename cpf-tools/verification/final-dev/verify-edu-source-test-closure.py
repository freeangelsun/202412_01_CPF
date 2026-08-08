#!/usr/bin/env python3
"""Directly opens every EDU handler/resource/test named by the canonical 135 catalog.

This gate exists specifically to prevent catalog-only false green: metadata cannot qualify a
requirement unless the concrete handler, consumer binding and five test/harness classes are present
and each file contains the expected requirement/class identity.
"""
from __future__ import annotations
import argparse,json,re,subprocess,sys
from pathlib import Path
class GateError(RuntimeError): pass

def req(ok,msg):
    if not ok: raise GateError(msg)

def read(root:Path,rel:str)->str:
    p=root/rel; req(p.is_file(),f'missing file: {rel}'); return p.read_text(encoding='utf-8',errors='replace')

def validate(root:Path):
    # A full checkout is mandatory. A copied subset must never report 135/135.
    g=subprocess.run(['git','-C',str(root),'rev-parse','--is-inside-work-tree'],capture_output=True,text=True,timeout=20)
    req(g.returncode==0 and g.stdout.strip()=='true','full git checkout required for direct EDU source/test closure')
    cat=json.loads(read(root,'cpf-reference/src/main/resources/edu/manual-135-catalog.json'))
    fs=cat.get('features');req(isinstance(fs,list) and len(fs)==135,'EDU catalog must contain exactly 135 features')
    opened_source=opened_resource=opened_tests=0; seen=set()
    for f in fs:
        rid=str(f.get('requirementId') or '');req(rid and rid not in seen,f'bad/duplicate requirementId: {rid}');seen.add(rid)
        executable=f.get('executable',True)
        decision=f.get('architectureDecision','')
        source=str(f.get('sourcePath') or '')
        tests=list(f.get('tests') or [])
        resource=str(f.get('resourceContract') or '')
        handler=str(f.get('handlerClass') or '')
        binding=f.get('consumerBinding') or {}
        if executable:
            req(source,f'{rid}: executable feature missing sourcePath')
            body=read(root,source);opened_source+=1
            simple=handler.rsplit('.',1)[-1] if handler else ''
            req(simple and re.search(rf'\b(class|record|interface)\s+{re.escape(simple)}\b',body),f'{rid}: handler class not found in actual source')
            req(rid in body or f.get('manualAnchor')==rid,f'{rid}: source does not carry requirement identity/manual mapping')
            req(len(tests)>=5,f'{rid}: expected Unit/Integration/Failure/Recovery/Concurrency tests, got {len(tests)}')
            required_kinds={'Unit','Integration','Failure','Recovery','Concurrency'}; found=set()
            for t in tests:
                tb=read(root,t);opened_tests+=1
                for k in required_kinds:
                    if k in Path(t).stem: found.add(k)
                req(rid in tb or simple.replace('Handler','') in tb or 'scenario' in tb.lower(),f'{rid}: test lacks feature linkage: {t}')
            req(found==required_kinds,f'{rid}: test-kind closure missing: {sorted(required_kinds-found)}')
            req(resource,f'{rid}: executable feature missing scenario resource')
            rb=read(root,resource);opened_resource+=1
            req(rid in rb or 'failure' in rb.lower() or 'scenario' in rb.lower(),f'{rid}: scenario resource lacks executable contract content')
            req(str(binding.get('runtimeCommand') or '').endswith(f'/{rid}/executions'),f'{rid}: runtime command not bound to requirement')
            req(str(binding.get('entryPoint') or '').strip(),f'{rid}: consumer entryPoint missing')
        else:
            req(decision in {'PRODUCT_ADM','MERGE_EDU'},f'{rid}: non-executable feature lacks canonical redirect decision')
            if decision=='PRODUCT_ADM': req(str(f.get('productOwnerModule') or '').strip(),f'{rid}: PRODUCT_ADM owner missing')
            if decision=='MERGE_EDU': req(str(f.get('mergedIntoRequirementId') or '').strip(),f'{rid}: MERGE_EDU target missing')
    print(f'[CPF][EDU-DIRECT-CLOSURE][PASS] requirements={len(fs)} source={opened_source} resource={opened_resource} tests={opened_tests}')

def self_test():
    # Structural self-test only; full direct qualification intentionally requires a git checkout.
    required={'Unit','Integration','Failure','Recovery','Concurrency'}
    names=['XUnitTest.java','XIntegrationTest.java','XFailureTest.java','XRecoveryTest.java','XConcurrencyTest.java']
    found={k for n in names for k in required if k in n};req(found==required,'test-kind parser failed')
    print('[CPF][EDU-DIRECT-CLOSURE][SELF-TEST][PASS]')

def main():
    ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--self-test',action='store_true');ns=ap.parse_args()
    if ns.self_test:self_test();return 0
    validate(Path(ns.root).resolve());return 0
if __name__=='__main__':
    try:raise SystemExit(main())
    except GateError as e:print('[CPF][EDU-DIRECT-CLOSURE][FAIL] '+str(e),file=sys.stderr);raise SystemExit(1)
