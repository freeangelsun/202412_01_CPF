#!/usr/bin/env python3
from __future__ import annotations
import argparse, subprocess
from pathlib import Path

def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',required=True); ns=ap.parse_args()
    root=Path(ns.root).resolve(); launcher=Path(__file__).with_name('run-frontend-workflow-harness.py')
    cp=subprocess.run(['python',str(launcher),'--root',str(root)],cwd=root,text=True)
    if cp.returncode: return cp.returncode
    required={
      'EmployeesPage':root/'cpf-backoffice-web/frontend/src/features/employees/pages/EmployeesPage.vue',
      'ApprovalInboxPage':root/'cpf-backoffice-web/frontend/src/features/approvals/pages/ApprovalInboxPage.vue',
      'AuthorizationPage':root/'cpf-backoffice-web/frontend/src/features/authorization/pages/AuthorizationPage.vue',
      'DashboardPage':root/'cpf-backoffice-web/frontend/src/features/dashboard/pages/DashboardPage.vue',
    }
    for label,p in required.items():
      if not p.is_file(): raise SystemExit(f'{label}: page missing')
      text=p.read_text(encoding='utf-8')
      if 'window.prompt' in text or 'prompt(' in text: raise SystemExit(f'{label}: native prompt forbidden')
    print('CPF_FRONTEND_REFERENCE_WORKFLOW_CONTRACT_PASS pages=4')
    return 0
if __name__=='__main__': raise SystemExit(main())
