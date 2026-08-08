#!/usr/bin/env python3
"""Batch process-kill/restart/reconcile semantic qualification. Boolean-only responses fail closed."""
from __future__ import annotations
import argparse,json,os,urllib.request,uuid
from urllib.parse import urlparse
class BatchError(RuntimeError):pass

def nonempty(v,name,minlen=3):
 s=str(v or '').strip()
 if len(s)<minlen: raise BatchError(name+' missing')
 return s

def validate_payload(p:dict,request_id:str)->dict:
 if p.get('requestId') not in {None,request_id}:raise BatchError('requestId mismatch')
 original=nonempty(p.get('originalExecutionId'),'originalExecutionId'); restart=nonempty(p.get('restartExecutionId'),'restartExecutionId')
 if original==restart:raise BatchError('restart must use a distinct executionId while retaining restart lineage')
 if str(p.get('restartOfExecutionId',''))!=original:raise BatchError('restartOfExecutionId must equal originalExecutionId')
 tx=nonempty(p.get('transactionId'),'transactionId',8)
 transitions=p.get('stateTransitions')
 if not isinstance(transitions,list) or len(transitions)<5:raise BatchError('stateTransitions must prove original failure and restarted completion')
 seq=[str(x.get('state','')).upper() for x in transitions if isinstance(x,dict)]
 for required in ('STARTING','RUNNING','FAILED','RESTARTING','COMPLETED'):
  if required not in seq:raise BatchError('stateTransitions missing '+required)
 fault=p.get('faultEvidence')
 if not isinstance(fault,dict) or str(fault.get('type','')).upper() not in {'PROCESS_KILL','JVM_KILL','OWNER_KILL'}:raise BatchError('process-kill fault evidence required')
 killed=nonempty(fault.get('killedOwner'),'faultEvidence.killedOwner'); takeover=nonempty(fault.get('takeoverOwner'),'faultEvidence.takeoverOwner')
 if killed==takeover:raise BatchError('takeover owner must differ from killed owner')
 old_token=fault.get('oldFenceToken'); new_token=fault.get('newFenceToken')
 if not isinstance(old_token,int) or isinstance(old_token,bool) or not isinstance(new_token,int) or isinstance(new_token,bool) or new_token<=old_token:raise BatchError('fencing token must increase across takeover')
 reconcile=p.get('reconcile')
 if not isinstance(reconcile,dict) or str(reconcile.get('transactionId',''))!=tx or str(reconcile.get('originalExecutionId',''))!=original or str(reconcile.get('restartExecutionId',''))!=restart:raise BatchError('reconcile identity/lineage mismatch')
 if reconcile.get('duplicateMutationCount')!=0 or str(reconcile.get('finalState','')).upper()!='COMPLETED':raise BatchError('reconcile must prove zero duplicate mutations and COMPLETED')
 db=p.get('dbEvidence'); runtime=p.get('runtimeEvidence')
 if not isinstance(db,dict) or len(str(db.get('queryId','')))<4 or len(str(db.get('rowHashSha256','')))!=64:raise BatchError('DB evidence queryId/rowHashSha256 required')
 if not isinstance(runtime,list) or len(runtime)<2 or any(len(str(r.get('recordId',''))) < 4 for r in runtime if isinstance(r,dict)):raise BatchError('runtime evidence record IDs required')
 return {'originalExecutionId':original,'restartExecutionId':restart,'transactionId':tx,'transitionCount':len(transitions),'killedOwner':killed,'takeoverOwner':takeover,'finalState':'COMPLETED'}

def fail(m):print(json.dumps({'status':'FAIL','reason':m},ensure_ascii=False));return 2

def self_test():
 bad={'requestId':'r','launched':True,'observed':True,'restartSafe':True,'reconciled':True}
 try:validate_payload(bad,'r')
 except BatchError:pass
 else:return fail('boolean-only batch evidence survived')
 good={'requestId':'r','originalExecutionId':'exec-001','restartExecutionId':'exec-002','restartOfExecutionId':'exec-001','transactionId':'T'*34,'stateTransitions':[{'state':'STARTING'},{'state':'RUNNING'},{'state':'FAILED'},{'state':'RESTARTING'},{'state':'COMPLETED'}],'faultEvidence':{'type':'PROCESS_KILL','killedOwner':'node-a','takeoverOwner':'node-b','oldFenceToken':3,'newFenceToken':4},'reconcile':{'transactionId':'T'*34,'originalExecutionId':'exec-001','restartExecutionId':'exec-002','duplicateMutationCount':0,'finalState':'COMPLETED'},'dbEvidence':{'queryId':'batch-query-1','rowHashSha256':'a'*64},'runtimeEvidence':[{'recordId':'run-1'},{'recordId':'run-2'}]}
 validate_payload(good,'r');print('[CPF][BATCH][PASS] selfTest=true identity=true processKill=true reconcile=true');return 0

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--self-test',action='store_true');a=ap.parse_args()
 if a.self_test:return self_test()
 url=os.environ.get('CPF_PERF_BATCH_PROBE_URL','').strip()
 if not url:return fail('CPF_PERF_BATCH_PROBE_URL is required')
 u=urlparse(url)
 if u.scheme not in {'http','https'} or not u.hostname:return fail('batch probe URL must be http/https')
 if u.scheme!='https' and u.hostname not in {'127.0.0.1','localhost','::1'}:return fail('non-local batch probe must use https')
 rid=str(uuid.uuid4());body=json.dumps({'requestId':rid,'scenario':'launch-process-kill-restart-reconcile'}).encode();req=urllib.request.Request(url,data=body,headers={'Content-Type':'application/json','X-Cpf-Request-Id':rid},method='POST')
 token=os.environ.get('CPF_PERF_BATCH_PROBE_TOKEN','').strip()
 if token:req.add_header('Authorization','Bearer '+token)
 try:
  with urllib.request.urlopen(req,timeout=float(os.environ.get('CPF_PERF_BATCH_TIMEOUT_SECONDS','120'))) as res:raw=res.read(2*1024*1024);status=res.status
 except Exception as e:return fail(type(e).__name__)
 if not 200<=status<300:return fail(f'HTTP {status}')
 try:p=json.loads(raw.decode())
 except Exception:return fail('probe response must be JSON')
 try:ev=validate_payload(p,rid)
 except BatchError as e:return fail(str(e))
 print(json.dumps({'schemaVersion':2,'protocol':'CPF-BATCH-IDENTITY-RESTART','status':'PASS','requestId':rid,**ev},ensure_ascii=False));return 0
if __name__=='__main__':raise SystemExit(main())
