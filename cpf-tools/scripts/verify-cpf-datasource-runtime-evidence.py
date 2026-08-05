#!/usr/bin/env python3
"""Fail-closed validator for CPF DataSource routing, concurrency and capacity evidence."""
from __future__ import annotations
import argparse,hashlib,json,re
from pathlib import Path
from typing import Any
HEX40=re.compile(r"^[0-9a-f]{40}$",re.I); HEX64=re.compile(r"^[0-9a-f]{64}$",re.I); SECRET=re.compile(r"password|secret|credential|access.?token|private.?key",re.I)
def load(p:Path)->dict[str,Any]:
 v=json.loads(p.read_text(encoding='utf-8-sig'))
 if not isinstance(v,dict):raise ValueError('JSON root must be object')
 return v
def sha(p:Path)->str:return hashlib.sha256(p.read_bytes()).hexdigest()
def secret_paths(v:Any,path='$')->list[str]:
 out=[]
 if isinstance(v,dict):
  for k,c in v.items():
   cp=f'{path}.{k}';out += [cp] if SECRET.search(str(k)) else [];out += secret_paths(c,cp)
 elif isinstance(v,list):
  for i,c in enumerate(v):out += secret_paths(c,f'{path}[{i}]')
 return out
def evaluate(p:dict[str,Any],e:dict[str,Any])->dict[str,Any]:
 r=[]
 if e.get('vendor') not in p['officialVendors']:r.append('unsupported vendor')
 if not HEX40.fullmatch(str(e.get('sourceSha',''))):r.append('sourceSha must be exact 40-hex SHA')
 if not str(e.get('operationId','')).strip():r.append('operationId is required')
 if len(str(e.get('reason','')).strip())<10:r.append('reason must contain at least 10 characters')
 if not e.get('operator') or not e.get('approvedBy'):r.append('operator and approvedBy are required')
 if e.get('operator')==e.get('approvedBy'):r.append('independent approval is required')
 if e.get('sanitized') is not True:r.append('sanitized must be true')
 leaks=secret_paths(e)
 if leaks:r.append('secret-bearing evidence keys are prohibited: '+','.join(leaks))
 if e.get('status') not in p['resultStates']:r.append('invalid status')
 if e.get('status')!='PASS':r.append('runtime evidence status must be PASS')
 route=e.get('routing')
 if not isinstance(route,dict):r.append('routing must be object')
 else:
  if route.get('writeTarget')!='PRIMARY':r.append('writes must target PRIMARY')
  consistency=route.get('consistency')
  if consistency not in p['readReplicaRouting']['allowedConsistency']:r.append('unsupported consistency')
  if consistency=='STRONG' and route.get('readTarget')!='PRIMARY':r.append('STRONG reads must target PRIMARY')
  if route.get('readTarget')=='REPLICA' and route.get('readOnly') is not True:r.append('replica route must be readOnly')
  lag=route.get('replicaLagMs')
  if not isinstance(lag,int) or lag<0:r.append('replicaLagMs must be non-negative integer')
  elif lag>p['replicaLag']['maxLagMs'] and route.get('readTarget')!='PRIMARY':r.append('lagged replica must fall back to PRIMARY')
  if not str(route.get('decisionAuditId','')).strip():r.append('route decision audit is required')
 ds=e.get('multiDataSource')
 if not isinstance(ds,dict):r.append('multiDataSource must be object')
 else:
  if not str(ds.get('owner','')).strip():r.append('DataSource owner is required')
  if ds.get('crossOwnerWrite') is True:r.append('cross-owner write is prohibited')
  if ds.get('resourceCount',0)>1 and ds.get('writeOperation') is True and not str(ds.get('compensationPlan','')).strip():r.append('multi-resource write requires compensation plan')
 pool=e.get('connectionPool')
 if not isinstance(pool,dict):r.append('connectionPool must be object')
 else:
  size=pool.get('maxPoolSize');instances=pool.get('instanceCount');budget=pool.get('databaseConnectionBudget');reserved=pool.get('reservedConnections')
  vals=[size,instances,budget,reserved]
  if any(not isinstance(x,int) or x<0 for x in vals):r.append('connection pool numeric fields are invalid')
  elif size<p['connectionPool']['minimumSize'] or size>p['connectionPool']['maximumPoolPerInstance']:r.append('maxPoolSize outside policy')
  elif size*instances>budget-reserved:r.append('connection pool exceeds database connection budget')
  if not isinstance(pool.get('connectionTimeoutMs'),int) or pool['connectionTimeoutMs']<=0 or pool['connectionTimeoutMs']>p['connectionPool']['connectionTimeoutMsMax']:r.append('connectionTimeoutMs outside policy')
  if not isinstance(pool.get('validationTimeoutMs'),int) or pool['validationTimeoutMs']<=0 or pool['validationTimeoutMs']>p['connectionPool']['validationTimeoutMsMax']:r.append('validationTimeoutMs outside policy')
 tx=e.get('transaction')
 if not isinstance(tx,dict):r.append('transaction must be object')
 else:
  isolation=tx.get('isolation')
  if isolation not in p['transactionIsolation']['allowed']:r.append('unsupported transaction isolation')
  if isolation=='SERIALIZABLE' and len(str(tx.get('serializableReason','')).strip())<10:r.append('SERIALIZABLE requires reason')
  if tx.get('deadlockDetected') is True:
   if not tx.get('idempotencyKey'):r.append('deadlock retry requires idempotencyKey')
   if not isinstance(tx.get('attemptCount'),int) or tx['attemptCount']<1 or tx['attemptCount']>p['deadlockHandling']['maxAttempts']:r.append('deadlock attemptCount outside policy')
   if tx.get('commitOutcome')=='UNKNOWN' and tx.get('retried') is True:r.append('UNKNOWN commit must not be blindly retried')
 timeouts=e.get('timeouts')
 if not isinstance(timeouts,dict):r.append('timeouts must be object')
 else:
  q=timeouts.get('queryTimeoutMs');l=timeouts.get('lockTimeoutMs')
  if not isinstance(q,int) or q<=0 or q>p['timeouts']['queryTimeoutMsMax']:r.append('queryTimeoutMs outside policy')
  if not isinstance(l,int) or l<=0 or l>p['timeouts']['lockTimeoutMsMax']:r.append('lockTimeoutMs outside policy')
 slow=e.get('slowQuery')
 if not isinstance(slow,dict):r.append('slowQuery must be object')
 else:
  if not isinstance(slow.get('durationMs'),(int,float)) or slow['durationMs']<0:r.append('slowQuery.durationMs invalid')
  if slow.get('durationMs',0)>=p['slowQueryAlert']['thresholdMs']:
   if slow.get('alertState') not in ['WARN','CRITICAL']:r.append('slow query above threshold requires alert')
   if not HEX64.fullmatch(str(slow.get('planSha256',''))):r.append('slow query planSha256 required')
  if slow.get('bindValuesSanitized') is not True:r.append('slow query bind values must be sanitized')
 cap=e.get('capacityForecast')
 if not isinstance(cap,dict):r.append('capacityForecast must be object')
 else:
  days=cap.get('observationDays');horizon=cap.get('forecastHorizonDays');headroom=cap.get('headroomPercent');growth=cap.get('growthRatePerDay')
  if not isinstance(days,int) or days<p['capacityForecast']['minimumObservationDays']:r.append('capacity observation window too short')
  if horizon!=p['capacityForecast']['forecastHorizonDays']:r.append('capacity forecast horizon mismatch')
  if not isinstance(headroom,(int,float)) or headroom<p['capacityForecast']['headroomPercentMin']:r.append('capacity headroom below policy')
  if not isinstance(growth,(int,float)):r.append('capacity growthRatePerDay required')
  elif growth>0 and not str(cap.get('estimatedExhaustionDate','')).strip():r.append('positive growth requires estimatedExhaustionDate')
  if cap.get('state') not in p['capacityForecast']['allowedStates']:r.append('capacity state invalid')
 return {'schemaVersion':1,'status':'PASS' if not r else 'FAIL','vendor':e.get('vendor'),'operationId':e.get('operationId'),'reasons':r}
def main()->int:
 ap=argparse.ArgumentParser();ap.add_argument('--policy',required=True);ap.add_argument('--evidence',required=True);ap.add_argument('--expected-evidence-sha256',required=True);ap.add_argument('--output');a=ap.parse_args();ep=Path(a.evidence);actual=sha(ep);expected=a.expected_evidence_sha256.lower()
 result={'schemaVersion':1,'status':'FAIL','reasons':[f'evidence sha256 mismatch expected={expected} actual={actual}']} if actual!=expected else evaluate(load(Path(a.policy)),load(ep));result['evidenceSha256']=actual
 if a.output:Path(a.output).write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(json.dumps(result,ensure_ascii=False,sort_keys=True));return 0 if result['status']=='PASS' else 1
if __name__=='__main__':raise SystemExit(main())
