#!/usr/bin/env python3
from __future__ import annotations
import argparse,json,re,sys
from pathlib import Path
REQUIRED=('mergedRepositorySourceGate','java25FreshBuild','frontendCleanVerify','threeVendorRuntime','faultRecoveryRuntime','browserRuntime','supplyChain','exactResultShaEvidence')
def fail(m):print('[CPF][QA37][TRUTH][FAIL] '+m,file=sys.stderr);raise SystemExit(1)
def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');a=ap.parse_args();root=Path(a.root).resolve()
 p=root/'cpf-docs/evidence/qa37/QA37_COMPLETION_STATE.json'
 if not p.is_file():fail('completion state missing')
 d=json.loads(p.read_text(encoding='utf-8'))
 checks=d.get('completionChecks',{})
 for k in REQUIRED:
  if k not in checks:fail('completion check missing '+k)
 all_pass=all(checks[k].get('status')=='완료' and checks[k].get('exitCode')==0 for k in REQUIRED)
 overall=d.get('overallStatus')
 if all_pass and overall!='완료':fail('all completion checks passed but overall is not 완료')
 if not all_pass and overall=='완료':fail('overall completion claimed with incomplete checks')
 manifest=json.loads((root/'cpf-docs/work/manifest/CPF_20260801_QA37_PACKAGE_MANIFEST.json').read_text(encoding='utf-8'))
 if not all_pass and 'FULL' in str(manifest.get('packageId','')).upper():fail('FULL packageId forbidden before all completion checks pass')
 documents=[
  root/'cpf-docs/work/current/CPF_20260801_QA37_VERIFICATION_READINESS_REPORT.md',
  root/'cpf-docs/work/current/CPF_20260801_QA37_SOURCE_READINESS_REPORT.md',
  root/'cpf-docs/work/review/CPF_20260801_QA37_INTEGRATED_POST_REVIEW.md',
  root/'cpf-docs/work/handover/CPF_20260801_QA37_SESSION_HANDOVER.md',
  root/'cpf-docs/work/continuity/CPF_20260801_QA37_CONTINUITY.md']
 banned=('통합 개발 완료 보고','부분 구현: 0, 미구현: 0','전체 검증 완료','No source implementation blocker remains','Delete Manifest NONE','seven-table Golden Domain contract','7개 Table Golden Domain')
 for document in documents:
  text=document.read_text(encoding='utf-8')
  for phrase in banned:
   if phrase in text:fail(f'overclaim/stale phrase in {document.name}: {phrase}')
 if not all_pass:
  post=(root/'cpf-docs/work/review/CPF_20260801_QA37_INTEGRATED_POST_REVIEW.md').read_text(encoding='utf-8')
  if '현재 전체 판정은 `미검증`' not in post:fail('post review must state unverified overall status')
 missing=(root/'cpf-docs/quality/EDU_MISSING_OR_PARTIAL_IMPLEMENTATION.csv').read_text(encoding='utf-8-sig')
 if 'MERGED-REPOSITORY-RECHECK' not in missing:fail('missing/partial matrix must retain merged repository recheck')
 print(f'[CPF][QA37][TRUTH][PASS] overall={overall} allCompletionChecks={all_pass}')
if __name__=='__main__':main()
