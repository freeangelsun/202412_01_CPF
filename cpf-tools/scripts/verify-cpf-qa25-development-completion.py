#!/usr/bin/env python3
"""Run the complete developer-side QA25 revalidation suite on a CPF working tree."""
from __future__ import annotations
import argparse,json,os,subprocess,sys,time
from pathlib import Path

def run(name,cmd,root,outdir):
 start=time.time();p=subprocess.run(cmd,cwd=root,text=True,capture_output=True);duration=round(time.time()-start,3)
 (outdir/f'{name}.stdout.log').write_text(p.stdout,encoding='utf-8');(outdir/f'{name}.stderr.log').write_text(p.stderr,encoding='utf-8')
 return {'name':name,'command':cmd,'exitCode':p.returncode,'durationSeconds':duration,'stdout':(outdir/f'{name}.stdout.log').relative_to(root).as_posix(),'stderr':(outdir/f'{name}.stderr.log').relative_to(root).as_posix()}

def main():
 a=argparse.ArgumentParser();a.add_argument('--root',default='.');a.add_argument('--expected-sha',required=True);a.add_argument('--review-dir',required=True);a.add_argument('--evidence-dir',required=True);a.add_argument('--require-clean',action='store_true');a.add_argument('--json-output');ns=a.parse_args()
 root=Path(ns.root).resolve();review=Path(ns.review_dir);review=review if review.is_absolute() else root/review;out=Path(ns.evidence_dir);out=out if out.is_absolute() else root/out;out.mkdir(parents=True,exist_ok=True)
 py=sys.executable
 commands=[
  ('python_gate_tests',[py,'-m','unittest','discover','-s','cpf-tools/scripts/tests','-p','test_verify_*.py','-v']),
  ('frontend_api_runtime',[py,'cpf-tools/verification/frontend-api-runtime/run-frontend-api-runtime-harness.py']),
  ('frontend_workflow_runtime',[py,'cpf-tools/verification/frontend-workflow-runtime/run-frontend-workflow-runtime-harness.py','--root',str(root)]),
  ('java21_controller',[py,'cpf-tools/verification/java21/controller-runtime/run-controller-runtime-harness.py']),
  ('java21_network',[py,'cpf-tools/verification/java21/network-runtime/run-network-runtime-harness.py']),
  ('java21_transaction',[py,'cpf-tools/verification/java21/transaction-runtime/run-transaction-runtime-harness.py']),
  ('java21_persistence',[py,'cpf-tools/verification/java21/persistence-runtime/run-persistence-runtime-harness.py']),
  ('java21_db_less',[py,'cpf-tools/verification/java21/db-less-runtime/run-db-less-runtime-harness.py']),
  ('java21_runtime_command',[py,'cpf-tools/verification/java21/runtime-command-runtime/run-runtime-command-harness.py','--root',str(root)]),
  ('java21_batch_abandon',[py,'cpf-tools/verification/java21/batch-abandon-runtime/run-batch-abandon-runtime-harness.py','--root',str(root)]),
  ('java21_audit_multi_process',[py,'cpf-tools/verification/java21/audit-runtime/run-audit-runtime-harness.py','--work-dir',str(out/'audit-runtime-work'),'--source-head',ns.expected_sha]),
  ('split_master',[py,'cpf-tools/scripts/verify-cpf-split-master-dataset.py','--root',str(root),'--expected-sha',ns.expected_sha,'--json-output',str(out/'split-master.json')]),
  ('owner_boundaries',[py,'cpf-tools/scripts/verify-cpf-owner-boundaries.py','--root',str(root),'--json-output',str(out/'owner-boundaries.json')]),
  ('transaction_standard',[py,'cpf-tools/scripts/verify-cpf-transaction-id-standard.py','--root',str(root),'--json-output',str(out/'transaction-standard.json')]),
  ('db_less',[py,'cpf-tools/scripts/verify-cpf-db-less-fail-closed.py','--root',str(root),'--json-output',str(out/'db-less.json')]),
  ('operator_trust',[py,'cpf-tools/scripts/verify-cpf-operator-trust-boundary.py','--root',str(root),'--json-output',str(out/'operator-trust.json')]),
  ('network_policy',[py,'cpf-tools/scripts/verify-cpf-network-policy-consumers.py','--root',str(root),'--json-output',str(out/'network-policy.json')]),
  ('db_vendor',[py,'cpf-tools/scripts/verify-cpf-db-vendor-semantic-parity.py','--root',str(root),'--json-output',str(out/'db-vendor.json')]),
  ('starter_catalog',[py,'cpf-tools/scripts/verify-cpf-starter-catalog-truth.py','--root',str(root),'--expected-sha',ns.expected_sha,'--json-output',str(out/'starter-catalog.json')]),
  ('traceability',[py,'cpf-tools/scripts/verify-cpf-requirement-traceability.py','--root',str(root),'--result-matrix',str(review/'REQUIREMENT_STATUS.csv'),'--expected-sha',ns.expected_sha,'--json-output',str(out/'traceability.json')]),
 ]
 if ns.require_clean:
  for name in ('split_master','starter_catalog','traceability'):
   for i,(n,c) in enumerate(commands):
    if n==name:commands[i]=(n,c+['--require-clean'])
 results=[run(n,c,root,out) for n,c in commands]
 # Manifest/evidence integrity is deliberately last so all evidence exists.
 results.append(run('evidence_integrity',[py,'cpf-tools/scripts/verify-cpf-development-evidence-integrity.py','--root',str(root),'--review-dir',str(review),'--expected-sha',ns.expected_sha,'--expected-requirements','10558','--expected-findings','25','--json-output',str(out/'evidence-integrity.json')],root,out))
 summary={'status':'PASS' if all(x['exitCode']==0 for x in results) else 'FAIL','expectedSha':ns.expected_sha,'workingTreeStatus':subprocess.run(['git','-C',str(root),'status','--short','--branch'],text=True,capture_output=True).stdout,'results':results,'failed':[x['name'] for x in results if x['exitCode']!=0]}
 dest=Path(ns.json_output) if ns.json_output else out/'qa25-development-completion.json';dest=dest if dest.is_absolute() else root/dest;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(summary,ensure_ascii=False));return 0 if summary['status']=='PASS' else 1
if __name__=='__main__':raise SystemExit(main())
