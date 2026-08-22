#!/usr/bin/env python3
"""Run CPF canonical source/static verifiers with one consistent root contract."""
from __future__ import annotations
import argparse, json, os, subprocess, sys, time
from pathlib import Path


def main() -> int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--root', default='.')
    ap.add_argument('--registry', default='cpf-tools/verification/contracts/cpf-verifier-registry.json')
    ap.add_argument('--json-output')
    ap.add_argument('--child-timeout', type=float, default=45.0)
    args=ap.parse_args()
    root=Path(args.root).resolve()
    if not root.is_dir():
        print(json.dumps({'status':'FAIL','message':f'root not found: {root}'},ensure_ascii=False)); return 2
    registry_path=(root/args.registry).resolve()
    if root not in registry_path.parents or not registry_path.is_file():
        print(json.dumps({'status':'FAIL','message':'registry missing or escapes root'},ensure_ascii=False)); return 2
    registry=json.loads(registry_path.read_text(encoding='utf-8'))
    items=registry.get('verifiers') or []
    if not items: raise SystemExit('empty verifier registry')
    results=[]; failed=0
    env=os.environ.copy(); env['PYTHONDONTWRITEBYTECODE']='1'; env['PYTEST_ADDOPTS']=env.get('PYTEST_ADDOPTS','')+' -p no:cacheprovider'
    for item in items:
        rel=item['path']; target=(root/rel).resolve()
        if root not in target.parents or not target.is_file():
            result={'id':item['id'],'status':'FAIL','exitCode':127,'message':f'missing verifier: {rel}'}
            failed += 1; results.append(result); continue
        cmd=[sys.executable,str(target),'--root',str(root),*[str(x) for x in item.get('args',[])]]
        started=time.monotonic()
        try:
            cp=subprocess.run(cmd,cwd=root,env=env,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,
                              timeout=args.child_timeout)
            elapsed=round(time.monotonic()-started,3)
            status='PASS' if cp.returncode==0 else 'FAIL'
            if cp.returncode and item.get('required',True): failed += 1
            results.append({'id':item['id'],'status':status,'exitCode':cp.returncode,'elapsedSeconds':elapsed,
                            'command':cmd,'output':cp.stdout[-12000:]})
            print(f"[CPF][CANONICAL-VERIFIER][{status}] {item['id']} rc={cp.returncode} elapsed={elapsed}s", flush=True)
        except subprocess.TimeoutExpired as exc:
            elapsed=round(time.monotonic()-started,3)
            if item.get('required',True): failed += 1
            output=(exc.stdout or '')
            if isinstance(output, bytes): output=output.decode('utf-8','replace')
            results.append({'id':item['id'],'status':'FAIL','exitCode':124,'elapsedSeconds':elapsed,
                            'command':cmd,'output':output[-12000:],'message':'child verifier timeout'})
            print(f"[CPF][CANONICAL-VERIFIER][FAIL] {item['id']} rc=124 timeout={elapsed}s", flush=True)
    payload={'status':'PASS' if failed==0 else 'FAIL','registryId':registry.get('registryId'),'total':len(results),'failed':failed,'results':results}
    if args.json_output:
        out=Path(args.json_output); out.parent.mkdir(parents=True,exist_ok=True); out.write_text(json.dumps(payload,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps({k:v for k,v in payload.items() if k!='results'},ensure_ascii=False))
    return 0 if failed==0 else 1

if __name__=='__main__': raise SystemExit(main())
