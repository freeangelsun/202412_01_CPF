#!/usr/bin/env python3
"""Fail closed when BAT risk commands trust browser-supplied approval identity."""
from __future__ import annotations
import argparse,json,re
from pathlib import Path

class ApprovalTrustError(RuntimeError):pass

def read(path:Path)->str:
 if not path.is_file():raise ApprovalTrustError(f'missing {path}')
 return path.read_text(encoding='utf-8-sig',errors='replace')

def verify(root:Path)->dict:
 root=root.resolve()
 frontend_path=root/'cpf-admin/frontend/src/features/batch-runtime-control/api.ts'
 controller_path=root/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java'
 executor_path=root/'cpf-batch/control-server/src/main/java/com/cpf/batch/control/RuntimeCommandExecutor.java'
 frontend=read(frontend_path);controller=read(controller_path);executor=read(executor_path)
 frontend_exposes_approved_by=bool(re.search(r'interface\s+BatchRuntimeCommandRequest.*?\bapprovedBy\s*:',frontend,re.S))
 client_approved_by=bool(re.search(r'requireCommandField\s*\(\s*request\s*,\s*"approvedBy"\s*\)',controller))
 strips_approved_by=bool(re.search(r'CLIENT_ACTOR_FIELDS\s*=.*?approvedBy',controller,re.S))
 trusted_controller=bool(re.search(r'(approvalVerifier|approvalService|approvedCommandDispatcher)\s*\.',controller))
 separation_only=bool(re.search(r'approvedBy\(\).*?equals\s*\(\s*command\.requestedBy\(\)\s*\)',executor,re.S))
 trusted_executor=bool(re.search(r'(approvalVerifier|approvalValidationPort|approvedCommandVerifier)\s*\.',executor))
 checks={
  'frontend_does_not_collect_approved_by':not frontend_exposes_approved_by,
  'browser_does_not_supply_approved_by':not client_approved_by or (strips_approved_by and trusted_controller),
  'controller_uses_trusted_approval_resolution':trusted_controller,
  'executor_verifies_approval_reference':trusted_executor,
  'separation_is_not_the_only_approval_check':not separation_only or trusted_executor,
 }
 missing=[k for k,v in checks.items() if not v]
 result={'status':'PASS' if not missing else 'FAIL','files':[frontend_path.relative_to(root).as_posix(),controller_path.relative_to(root).as_posix(),executor_path.relative_to(root).as_posix()],'checks':checks,'findings':missing,'risk':'browser-supplied approver/approval id can authorize BAT runtime command'}
 if missing:raise ApprovalTrustError(json.dumps(result,ensure_ascii=False,indent=2))
 return result

def main()->int:
 p=argparse.ArgumentParser();p.add_argument('--root',default='.');p.add_argument('--json-output');a=p.parse_args()
 try:r=verify(Path(a.root));c=0
 except Exception as e:
  try:r=json.loads(str(e))
  except:r={'status':'FAIL','message':str(e)}
  c=1
 text=json.dumps(r,ensure_ascii=False,indent=2)
 if a.json_output:
  o=Path(a.json_output);o=o if o.is_absolute() else Path(a.root).resolve()/o;o.parent.mkdir(parents=True,exist_ok=True);o.write_text(text+'\n',encoding='utf-8')
 print(text);return c
if __name__=='__main__':raise SystemExit(main())
