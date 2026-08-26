#!/usr/bin/env python3
"""Fail-closed structural gate for non-remote CPF Batch ownership/fencing boundaries."""
from __future__ import annotations
import argparse,json,sys
from pathlib import Path
CHECKS={
 "cpf-batch/scheduler/src/main/java/com/cpf/batch/scheduler/SchedulerCoordinator.java":("AtomicBoolean electionInProgress","JdbcSchedulerLeaderRepository.Lease current = lease.get();","if (!electionInProgress.compareAndSet(false, true)) {"),
 "cpf-batch/worker/src/main/java/com/cpf/batch/worker/ApprovedFileExecutor.java":(".cpf-claim.lock","FileChannel.open","BATCH_FILE_CLAIM_FENCE_CONFLICT","writeFenceToken(channel, token)"),
 "cpf-batch/worker/src/main/java/com/cpf/batch/worker/ApprovedShellExecutor.java":("Thread stdinWriter","terminateProcessTree","BATCH_SHELL_STDIN_INCOMPLETE"),
 "cpf-batch/center-cut/src/main/java/com/cpf/batch/centercut/runner/CenterCutTargetGenerator.java":("BATCH_CENTER_CUT_PROVIDER_PAGE_LIMIT_EXCEEDED","BATCH_CENTER_CUT_DUPLICATE_BUSINESS_KEY","BATCH_CENTER_CUT_NON_ADVANCING_CURSOR"),
 "cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/JdbcCenterCutClaimRepository.java":("TransactionTemplate","status.setRollbackOnly()","claimWithinTransaction("),
 "cpf-batch/runtime/src/main/java/com/cpf/batch/execution/CpfSpringBatchExecutionControl.java":("fencing.assertCurrent(jobId, cpfExecutionId, fencingToken);","BATCH_RECOVER_RESPONSE_UNKNOWN","operator.recover(previous)"),
}

def method_body(text,signature):
 s=text.find(signature)
 if s<0: raise ValueError(f'method signature missing: {signature}')
 b=text.find('{',s);depth=0
 for i in range(b,len(text)):
  if text[i]=='{':depth+=1
  elif text[i]=='}':
   depth-=1
   if depth==0:return text[b+1:i]
 raise ValueError(f'unterminated method body: {signature}')

def verify(root):
 errors=[]
 for rel,tokens in CHECKS.items():
  p=root/rel
  if not p.is_file(): errors.append(f'{rel}: source missing');continue
  text=p.read_text(encoding='utf-8');compact=''.join(text.split())
  for token in tokens:
   if ''.join(token.split()) not in compact:errors.append(f'{rel}: required token missing: {token}')
  if rel.endswith('CpfSpringBatchExecutionControl.java'):
   body=method_body(text,'public BatchExecutionLink recover(')
   if body.find('fencing.assertCurrent')>body.find('operator.recover(previous)'):errors.append(f'{rel}: recover fencing must precede recovery')
 if errors: raise ValueError('\n'.join(errors))
 return {'status':'PASS','verifiedSources':len(CHECKS),'checks':sum(map(len,CHECKS.values()))}

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--json-output');a=ap.parse_args()
 try:r=verify(Path(a.root).resolve())
 except ValueError as e:print(f'[FAIL] CPF Batch execution fencing contract\n{e}',file=sys.stderr);return 1
 if a.json_output:
  p=Path(a.json_output);p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(r,indent=2)+'\n',encoding='utf-8')
 print(f"[PASS] CPF Batch execution fencing contract sources={r['verifiedSources']} checks={r['checks']}");return 0
if __name__=='__main__':raise SystemExit(main())
