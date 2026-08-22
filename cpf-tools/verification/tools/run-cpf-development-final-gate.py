#!/usr/bin/env python3
"""Single owner for CPF development-environment final closure."""
from __future__ import annotations
import argparse, os, subprocess, sys
from pathlib import Path

def run(cmd:list[str], root:Path, env:dict[str,str])->int:
    cp=subprocess.run(cmd,cwd=root,env=env,text=True)
    return cp.returncode

def main()->int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--root',default='.')
    ap.add_argument('--matrix',default='cpf-docs/work/current/CPF_CANONICAL_DEVELOPMENT_CLOSURE_INVENTORY.csv')
    ap.add_argument('--expected-source-sha256',required=True)
    args=ap.parse_args()
    root=Path(args.root).resolve()
    env=os.environ.copy(); env['PYTHONDONTWRITEBYTECODE']='1'; env['PYTEST_ADDOPTS']=env.get('PYTEST_ADDOPTS','')+' -p no:cacheprovider'
    py=sys.executable
    stages=[
      ('CANONICAL_STATIC',[py,str(root/'cpf-tools/verification/tools/run-cpf-canonical-verifiers.py'),'--root',str(root)]),
      ('POST_CLEAN_SOURCE',[py,str(root/'cpf-tools/verification/tools/verify-cpf-clean-source-tree.py'),'--root',str(root)]),
      ('EVIDENCE_SEMANTICS',[py,str(root/'cpf-tools/verification/tools/verify-cpf-evidence-semantics.py'),'--root',str(root),'--matrix',args.matrix,'--expected-sha',args.expected_source_sha256]),
    ]
    failed=[]
    for name,cmd in stages:
      rc=run(cmd,root,env); print(f'[CPF][DEVELOPMENT-FINAL-GATE] {name} rc={rc}',flush=True)
      if rc!=0: failed.append(name)
    status='PASS' if not failed else 'FAIL'
    print(f'CPF_DEVELOPMENT_FINAL_GATE={status} failed={failed}',flush=True)
    return 0 if not failed else 1
if __name__=='__main__': raise SystemExit(main())
