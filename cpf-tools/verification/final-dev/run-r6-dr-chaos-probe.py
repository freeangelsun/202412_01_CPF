#!/usr/bin/env python3
from __future__ import annotations
import argparse,datetime as dt,hashlib,json,os,re,sys,urllib.request,uuid
from pathlib import Path
from urllib.parse import urlparse
SHA40=re.compile(r'^[0-9a-f]{40}$'); HEX64=re.compile(r'^[0-9a-f]{64}$')
REQUIRED_FAULTS={'SPLIT_BRAIN','POWER_LOSS','SELECTIVE_ROLLBACK'}
class DrError(RuntimeError):pass

def iso(v,name):
 try:return dt.datetime.fromisoformat(str(v).replace('Z','+00:00'))
 except Exception as e:raise DrError(name+' must be ISO-8601') from e

def digest(v,name):
 s=str(v or '').lower()
 if not HEX64.fullmatch(s):raise DrError(name+' must be sha256 hex')
 return s

def validate_harness(e:dict,head:str,rid:str,run_id:str,max_rpo:float,max_rto:float)->dict:
 if e.get('schemaVersion')!=2 or e.get('sourceSha')!=head:raise DrError('independent harness evidence schema/sourceSha mismatch')
 if e.get('requestId')!=rid or e.get('chaosRunId')!=run_id:raise DrError('harness request/run identity mismatch')
 harness_id=str(e.get('harnessId','')).strip();authority=str(e.get('provenanceAuthority','')).strip()
 if len(harness_id)<8 or len(authority)<4:raise DrError('independent harness identity/provenance required')
 faults=e.get('faultEvents')
 if not isinstance(faults,list):raise DrError('faultEvents[] required')
 types={str(x.get('type','')).upper() for x in faults if isinstance(x,dict)}
 if not REQUIRED_FAULTS.issubset(types):raise DrError('independent fault events missing: '+','.join(sorted(REQUIRED_FAULTS-types)))
 for x in faults:
  if not isinstance(x,dict):continue
  if str(x.get('type','')).upper() in REQUIRED_FAULTS:
   if not x.get('injectedByHarness') or len(str(x.get('eventId','')))<8:raise DrError('fault must be injected by independent harness with eventId')
   if iso(x.get('recoveredAt'),'fault.recoveredAt') < iso(x.get('injectedAt'),'fault.injectedAt'):raise DrError('fault recoveredAt precedes injectedAt')
 before=digest(e.get('dataHashBefore'),'dataHashBefore');after=digest(e.get('dataHashAfter'),'dataHashAfter')
 if before!=after:raise DrError('restored data hash mismatch')
 ab=digest(e.get('artifactHashBefore'),'artifactHashBefore');aa=digest(e.get('artifactHashAfter'),'artifactHashAfter')
 if ab!=aa:raise DrError('restored artifact hash mismatch')
 last_committed=iso(e.get('lastCommittedAt'),'lastCommittedAt');recovered_data=iso(e.get('recoveredDataThrough'),'recoveredDataThrough');fault_at=iso(e.get('faultStartedAt'),'faultStartedAt');healthy=iso(e.get('serviceHealthyAt'),'serviceHealthyAt')
 rpo=max(0.0,(last_committed-recovered_data).total_seconds());rto=max(0.0,(healthy-fault_at).total_seconds())
 if rpo>max_rpo:raise DrError(f'RPO exceeded measured={rpo} max={max_rpo}')
 if rto>max_rto:raise DrError(f'RTO exceeded measured={rto} max={max_rto}')
 if not e.get('splitBrainFenceObserved') or not e.get('reconcileVerified'):raise DrError('fencing/reconcile independent observations required')
 canonical=json.dumps(e,sort_keys=True,separators=(',',':')).encode()
 return {'harnessId':harness_id,'provenanceAuthority':authority,'rpoSecondsMeasured':rpo,'rtoSecondsMeasured':rto,'dataSha256':before,'artifactSha256':ab,'harnessEvidenceSha256':hashlib.sha256(canonical).hexdigest(),'faultCount':len(faults)}

def self_test(head):
 fake={'splitBrainFenced':True,'powerLossRecovered':True,'selectiveRollbackSafe':True,'reconciled':True,'dataConsistent':True,'artifactConsistent':True,'rpoSeconds':0,'rtoSeconds':0}
 try:validate_harness(fake,head,'r','run',300,1800)
 except DrError:pass
 else:raise DrError('self-attested DR booleans/RPO/RTO mutation survived')
 print('[CPF][DR][PASS] selfTest=true externalHarness=true measuredHashesRpoRto=true');return 0

def main()->int:
 ap=argparse.ArgumentParser();ap.add_argument('--expected-head',required=True);ap.add_argument('--output-json',type=Path);ap.add_argument('--self-test',action='store_true');a=ap.parse_args();head=a.expected_head.lower().strip()
 if not SHA40.fullmatch(head):raise DrError('expected head must be a 40-char SHA')
 if a.self_test:return self_test(head)
 if not a.output_json:raise DrError('--output-json is required')
 url=os.getenv('CPF_R6_DR_CHAOS_PROBE_URL','').strip();token=os.getenv('CPF_R6_DR_CHAOS_PROBE_TOKEN','').strip();ev_path=os.getenv('CPF_R6_DR_HARNESS_EVIDENCE_JSON','').strip()
 if not url or not ev_path:raise DrError('CPF_R6_DR_CHAOS_PROBE_URL and CPF_R6_DR_HARNESS_EVIDENCE_JSON are required')
 u=urlparse(url)
 if u.scheme not in {'http','https'} or not u.hostname:raise DrError('DR chaos probe URL must be http/https')
 if u.scheme!='https' and u.hostname not in {'127.0.0.1','localhost','::1'}:raise DrError('non-local DR chaos probe must use https')
 rid=str(uuid.uuid4());body=json.dumps({'requestId':rid,'sourceSha':head,'scenario':'external-harness-split-brain-power-loss-selective-rollback'}).encode();req=urllib.request.Request(url,data=body,headers={'Content-Type':'application/json','X-Cpf-Request-Id':rid},method='POST')
 if token:req.add_header('Authorization','Bearer '+token)
 try:
  with urllib.request.urlopen(req,timeout=float(os.getenv('CPF_R6_DR_CHAOS_TIMEOUT_SECONDS','900'))) as r:raw=r.read(1024*1024);status=r.status
 except Exception as e:raise DrError(type(e).__name__) from e
 if not 200<=status<300:raise DrError(f'HTTP {status}')
 try:ack=json.loads(raw.decode())
 except Exception as e:raise DrError('probe response must be JSON') from e
 if ack.get('sourceSha') not in {None,head} or ack.get('requestId') not in {None,rid}:raise DrError('probe identity mismatch')
 run_id=str(ack.get('chaosRunId','')).strip()
 if len(run_id)<8:raise DrError('probe must return chaosRunId; self-attested result booleans are ignored')
 p=Path(ev_path);e=json.loads(p.read_text(encoding='utf-8-sig'));max_rpo=float(os.getenv('CPF_R6_DR_MAX_RPO_SECONDS','300'));max_rto=float(os.getenv('CPF_R6_DR_MAX_RTO_SECONDS','1800'));measured=validate_harness(e,head,rid,run_id,max_rpo,max_rto)
 result={'schemaVersion':2,'protocol':'CPF-R6-DR-INDEPENDENT-HARNESS','sourceSha':head,'status':'PASS','chaosRunId':run_id,'maxRpoSeconds':max_rpo,'maxRtoSeconds':max_rto,**measured};a.output_json.parent.mkdir(parents=True,exist_ok=True);a.output_json.write_text(json.dumps(result,indent=2)+'\n');print(f"[CPF][R6][DR-CHAOS][PASS] sourceSha={head} rpo={measured['rpoSecondsMeasured']} rto={measured['rtoSecondsMeasured']}");return 0
if __name__=='__main__':
 try:raise SystemExit(main())
 except (DrError,OSError,json.JSONDecodeError) as e:print(f'[CPF][R6][DR-CHAOS][FAIL] {e}',file=sys.stderr);raise SystemExit(1)
