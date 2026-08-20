#!/usr/bin/env python3
from __future__ import annotations
import argparse, shutil, subprocess, sys
from pathlib import Path

def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',required=True); ns=ap.parse_args()
    root=Path(ns.root).resolve(); here=Path(__file__).resolve().parent; work=here/'build'
    shutil.rmtree(work,ignore_errors=True); work.mkdir(parents=True); (work/'package.json').write_text('{"type":"commonjs"}',encoding='utf-8')
    sources=[
      root/'cpf-admin/frontend/src/features/break-glass/breakGlassWorkflow.ts',
      root/'cpf-admin/frontend/src/features/maintenance/maintenanceWorkflow.ts',
      root/'cpf-backoffice-web/frontend/src/features/employees/model/employeeModel.ts',
      root/'cpf-backoffice-web/frontend/src/features/approvals/model/approvalModel.ts',
    ]
    missing=[str(p) for p in sources if not p.is_file()]
    if missing:
      print('missing workflow source: '+', '.join(missing),file=sys.stderr); return 2
    cmd=['tsc','--target','ES2022','--module','commonjs','--moduleResolution','node','--lib','ES2022,DOM,DOM.Iterable','--strict','--skipLibCheck','--outDir',str(work),*map(str,sources)]
    cp=subprocess.run(cmd,text=True,capture_output=True)
    if cp.returncode:
      print(cp.stdout+cp.stderr); return cp.returncode
    run=subprocess.run(['node',str(here/'harness.cjs'),str(work)],text=True,capture_output=True)
    print(run.stdout,end=''); print(run.stderr,end='',file=sys.stderr)
    return run.returncode
if __name__=='__main__': raise SystemExit(main())
