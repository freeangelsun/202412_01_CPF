#!/usr/bin/env python3
from __future__ import annotations
import argparse,shutil,subprocess
from pathlib import Path

def require(text:str,token:str,label:str):
 if token not in text:raise SystemExit(f"{label}: missing {token}")
def forbid(text:str,token:str,label:str):
 if token in text:raise SystemExit(f"{label}: forbidden {token}")
def main()->int:
 ap=argparse.ArgumentParser();ap.add_argument('--root',required=True);a=ap.parse_args();root=Path(a.root).resolve();base=root/'cpf-tools/verification/frontend-workflow-runtime';shutil.rmtree(base/'build',ignore_errors=True)
 subprocess.run(['tsc','-p',str(base/'tsconfig.json')],cwd=root,check=True)
 (base/'build/package.json').write_text('{\"type\":\"commonjs\"}\n',encoding='utf-8')
 subprocess.run(['node',str(base/'harness.cjs'),str(base/'build')],cwd=root,check=True)
 pages={
  'BreakGlassPage':root/'cpf-admin/frontend/src/features/break-glass/BreakGlassPage.vue',
  'AttachmentsPage':root/'cpf-biz-admin/frontend/src/features/attachments/AttachmentsPage.vue',
  'SessionsPage':root/'cpf-biz-admin/frontend/src/features/sessions/SessionsPage.vue',
  'RouteOperationWorkbench':root/'cpf-biz-admin/frontend/src/components/RouteOperationWorkbench.vue'}
 for label,p in pages.items():
  if not p.is_file():raise SystemExit(f'{label}: page missing')
  text=p.read_text(encoding='utf-8')
  forbid(text,'prompt(',label);forbid(text,'window.prompt',label)
  require(text,'role="alert"',label);require(text,'role="status"',label)
 breakglass=pages['BreakGlassPage'].read_text(encoding='utf-8')
 for op in ('admBreakGlassFindSessions','admBreakGlassOpenSession','admBreakGlassReviewSession','admBreakGlassCloseSession'):require(breakglass,op,'BreakGlassPage')
 require(breakglass,'session.canWrite("breakGlass"','BreakGlassPage')
 attachments=pages['AttachmentsPage'].read_text(encoding='utf-8')
 for op in ('bzaSupportFindAttachments','bzaSupportUploadAttachment','bzaSupportRecheckAttachment','bzaSupportUpdateAttachmentSecurity','/api/bza/attachments/${encodeURIComponent(attachmentId)}/download'):require(attachments,op,'AttachmentsPage')
 sessions=pages['SessionsPage'].read_text(encoding='utf-8')
 for op in ('bzaAuthSessions','bzaAuthRevokeSession'):require(sessions,op,'SessionsPage')
 route=pages['RouteOperationWorkbench'].read_text(encoding='utf-8')
 require(route,'attachDangerousReason', 'RouteOperationWorkbench');require(route,'validatePathValues','RouteOperationWorkbench')
 print('CPF_FRONTEND_WORKFLOW_CONTRACT_PASS pages=4 operations>=12');return 0
if __name__=='__main__':raise SystemExit(main())
