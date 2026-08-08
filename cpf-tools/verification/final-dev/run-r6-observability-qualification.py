#!/usr/bin/env python3
from __future__ import annotations
import sys
from pathlib import Path as _TrustPath
sys.path.insert(0,str(_TrustPath(__file__).resolve().parents[1]))
from release_target_trust import verify_release_target, self_test as trust_self_test
import argparse,hashlib,hmac,json,os,re,sys,urllib.parse,urllib.request,uuid
from pathlib import Path
from typing import Any

SHA40=re.compile(r'^[0-9a-f]{40}$'); HEX64=re.compile(r'^[0-9a-f]{64}$')
STORES=('metric','log','trace','alert','audit'); RECORD_KEYS=('records','items','results','events')
class ObsError(RuntimeError):pass

def request_json(url:str,method='GET',body=None,token='')->Any:
 u=urllib.parse.urlparse(url)
 if u.scheme not in {'http','https'} or not u.hostname:raise ObsError('observability URL must be http/https')
 if u.scheme!='https' and u.hostname not in {'127.0.0.1','localhost','::1'}:raise ObsError('non-local observability endpoint must use https')
 data=None if body is None else json.dumps(body,separators=(',',':')).encode();headers={'Accept':'application/json'}
 if data is not None:headers['Content-Type']='application/json'
 if token:headers['Authorization']='Bearer '+token
 try:
  with urllib.request.urlopen(urllib.request.Request(url,data=data,headers=headers,method=method),timeout=float(os.getenv('CPF_R6_OBSERVABILITY_TIMEOUT_SECONDS','60'))) as r:raw=r.read(4*1024*1024);status=r.status
 except Exception as e:raise ObsError(f'{type(e).__name__}: {url}') from e
 if not 200<=status<300:raise ObsError(f'HTTP {status}: {url}')
 try:return json.loads(raw.decode())
 except Exception as e:raise ObsError(f'endpoint must return JSON: {url}') from e

def field(record:dict[str,Any],*names:str)->Any:
 normalized={re.sub(r'[^a-z0-9]','',str(k).lower()):v for k,v in record.items()}
 for name in names:
  k=re.sub(r'[^a-z0-9]','',name.lower())
  if k in normalized:return normalized[k]
 return None

def records_from(payload:Any,store:str)->list[dict[str,Any]]:
 if isinstance(payload,list):source=payload
 elif isinstance(payload,dict):
  source=next((payload[k] for k in RECORD_KEYS if isinstance(payload.get(k),list)),None)
  if source is None and isinstance(payload.get('data'),list):source=payload['data']
  if source is None:raise ObsError(f'{store} endpoint must return concrete records, not self-attested booleans')
 else:raise ObsError(f'{store} endpoint returned unsupported JSON shape')
 records=[x for x in source if isinstance(x,dict)]
 if not records:raise ObsError(f'{store} endpoint returned no concrete records')
 return records

def sig_input(store:str,record_id:str,qid:str,txid:str,traceid:str,head:str,workload_evidence_id:str,authority:str)->bytes:
 return '|'.join((store,record_id,qid,txid,traceid,head,workload_evidence_id,authority)).encode()

def sign_record(store:str,r:dict,key:str)->str:
 return hmac.new(key.encode(),sig_input(store,str(r['recordId']),str(r['qualificationId']),str(r['transactionId']),str(r['traceId']),str(r['sourceSha']),str(r['workloadEvidenceId']),str(r['provenanceAuthority'])),hashlib.sha256).hexdigest()

def workload_sig_input(e:dict)->bytes:
 return '|'.join(str(e[k]) for k in ('workloadEvidenceId','requestId','qualificationId','transactionId','traceId','sourceSha','artifactSha256','provenanceAuthority')).encode()

def validate_workload_evidence(e:dict,head:str,rid:str,qid:str,txid:str,traceid:str,authority:str,key:str)->dict:
 if e.get('schemaVersion')!=2:raise ObsError('workload evidence schemaVersion=2 required')
 expected={'requestId':rid,'qualificationId':qid,'transactionId':txid,'traceId':traceid,'sourceSha':head,'provenanceAuthority':authority}
 for k,v in expected.items():
  if str(e.get(k,''))!=str(v):raise ObsError('workload evidence identity mismatch: '+k)
 wid=str(e.get('workloadEvidenceId','')).strip();artifact=str(e.get('artifactSha256','')).lower();signature=str(e.get('provenanceSignature','')).lower()
 if len(wid)<8 or not HEX64.fullmatch(artifact):raise ObsError('workload evidence immutable ID/artifact hash required')
 expected_sig=hmac.new(key.encode(),workload_sig_input(e),hashlib.sha256).hexdigest()
 if not hmac.compare_digest(signature,expected_sig):raise ObsError('workload provenance signature invalid')
 if not isinstance(e.get('events'),list) or len(e['events'])<2:raise ObsError('workload-side success+failure events required')
 kinds={str(x.get('kind','')).upper() for x in e['events'] if isinstance(x,dict)}
 if not {'SUCCESS','FAILURE'}.issubset(kinds):raise ObsError('workload evidence must contain SUCCESS and FAILURE traffic')
 return {'workloadEvidenceId':wid,'artifactSha256':artifact,'eventCount':len(e['events']),'evidenceSha256':hashlib.sha256(json.dumps(e,sort_keys=True,separators=(',',':')).encode()).hexdigest()}

def validate_store(store:str,records:list[dict[str,Any]],qid:str,txid:str,traceid:str,head:str,wid:str,authority:str,key:str)->dict[str,Any]:
 qualified=[]
 for r in records:
  record_id=str(field(r,'recordId','evidenceId','eventId','id') or '').strip();q=str(field(r,'qualificationId') or '');tx=str(field(r,'transactionId') or '');tr=str(field(r,'traceId') or '');sha=str(field(r,'sourceSha') or '').lower();rw=str(field(r,'workloadEvidenceId') or '');ra=str(field(r,'provenanceAuthority') or '');signature=str(field(r,'provenanceSignature') or '').lower()
  if not record_id or q!=qid or tx!=txid or tr!=traceid or sha!=head or rw!=wid or ra!=authority:continue
  temp={'recordId':record_id,'qualificationId':q,'transactionId':tx,'traceId':tr,'sourceSha':sha,'workloadEvidenceId':rw,'provenanceAuthority':ra}
  expected=sign_record(store,temp,key)
  if not hmac.compare_digest(signature,expected):continue
  qualified.append(r)
 if not qualified:raise ObsError(f'{store} store has no signed exact-SHA workload-correlated record')
 if store=='metric' and not any(field(r,'metricName','name') and isinstance(field(r,'value','metricValue'),(int,float)) and not isinstance(field(r,'value','metricValue'),bool) for r in qualified):raise ObsError('metric store lacks named numeric sample')
 if store=='log' and not any(field(r,'message','eventName','eventType') and field(r,'level','severity') for r in qualified):raise ObsError('log store lacks event/severity')
 if store=='trace' and not any(str(field(r,'traceId') or '')==traceid and field(r,'spanId') for r in qualified):raise ObsError('trace store lacks spanId')
 if store=='alert':
  states={str(field(r,'state','status','alertState') or '').upper() for r in qualified}
  if not (states & {'FIRING','FIRED','ACTIVE','OPEN','TRIGGERED'}) or not (states & {'RESOLVED','CLOSED','RECOVERED','CLEARED'}):raise ObsError('alert store must prove fired+resolved lifecycle')
 if store=='audit' and not any(field(r,'action','eventName','eventType') and field(r,'outcome','result','status') for r in qualified):raise ObsError('audit store lacks action/outcome')
 ids=[str(field(r,'recordId','evidenceId','eventId','id')) for r in qualified]
 if len(ids)!=len(set(ids)):raise ObsError(f'{store} record IDs are not immutable/unique')
 return {'recordCount':len(qualified),'recordIdSha256':[hashlib.sha256(x.encode()).hexdigest() for x in ids[:20]],'provenanceAuthority':authority}

def self_test(head:str)->int:
 qid='qualification-001';tx='T'*34;trace='trace-0001';wid='workload-001';authority='metric-store';key='metric-secret-001'
 fake={'recordId':'record-001','qualificationId':qid,'transactionId':tx,'traceId':trace,'sourceSha':head,'workloadEvidenceId':wid,'provenanceAuthority':authority,'metricName':'cpf.tx','value':1}
 try:validate_store('metric',[fake],qid,tx,trace,head,wid,authority,key)
 except ObsError:pass
 else:raise ObsError('unsigned authoritative-looking fake record mutation survived')
 fake['provenanceSignature']=sign_record('metric',fake,key)
 validate_store('metric',[fake],qid,tx,trace,head,wid,authority,key)
 print('[CPF][OBS][PASS] selfTest=true signedProvenance=true workloadCorrelation=true exactSha=true');return 0

def main()->int:
 ap=argparse.ArgumentParser();ap.add_argument('--expected-head',default='');ap.add_argument('--output-json',type=Path);ap.add_argument('--self-test',action='store_true');a=ap.parse_args();head=(a.expected_head or ('0'*40 if a.self_test else '')).lower().strip()
 if not SHA40.fullmatch(head):raise ObsError('expected head must be a 40-char SHA')
 if a.self_test:
  trust_self_test();return self_test(head)
 if not a.output_json:raise ObsError('--output-json is required')
 if not a.self_test and len(head)!=40: raise ObsError('--expected-head exact checkout SHA is required')
 probe=os.getenv('CPF_R6_OBSERVABILITY_PROBE_URL','').strip();probe_token=os.getenv('CPF_R6_OBSERVABILITY_PROBE_TOKEN','').strip()
 if not probe:raise ObsError('CPF_R6_OBSERVABILITY_PROBE_URL is required')
 verify_release_target(probe,head)
 rid=str(uuid.uuid4());proof=request_json(probe,'POST',{'requestId':rid,'sourceSha':head,'scenario':'known-traffic-and-failure'},probe_token)
 if not isinstance(proof,dict) or proof.get('sourceSha') not in {None,head} or proof.get('requestId') not in {None,rid}:raise ObsError('probe identity mismatch')
 qid=str(proof.get('qualificationId','')).strip();txid=str(proof.get('transactionId','')).strip();traceid=str(proof.get('traceId','')).strip()
 if min(map(len,(qid,txid,traceid)))<8:raise ObsError('probe must return qualificationId/transactionId/traceId')
 workload_path=os.getenv('CPF_R6_OBSERVABILITY_WORKLOAD_EVIDENCE_JSON','').strip();workload_key=os.getenv('CPF_R6_OBSERVABILITY_WORKLOAD_PROVENANCE_KEY','').strip();workload_authority=os.getenv('CPF_R6_OBSERVABILITY_WORKLOAD_PROVENANCE_AUTHORITY','').strip()
 if not workload_path or len(workload_key)<12 or len(workload_authority)<4:raise ObsError('independent workload evidence path/key/authority are required')
 workload=json.loads(Path(workload_path).read_text(encoding='utf-8-sig'));workload_ev=validate_workload_evidence(workload,head,rid,qid,txid,traceid,workload_authority,workload_key);wid=workload_ev['workloadEvidenceId']
 evidence={};authorities={workload_authority}
 for store in STORES:
  prefix='CPF_R6_OBSERVABILITY_'+store.upper();base=os.getenv(prefix+'_QUERY_URL','').strip();token=os.getenv(prefix+'_QUERY_TOKEN','').strip();key=os.getenv(prefix+'_PROVENANCE_KEY','').strip();authority=os.getenv(prefix+'_PROVENANCE_AUTHORITY','').strip()
  if not base or not token or len(key)<12 or len(authority)<4:raise ObsError(prefix+' query URL/token/provenance key/authority are required')
  if authority in authorities:raise ObsError('provenance authorities must be independent: '+authority)
  authorities.add(authority);sep='&' if '?' in base else '?';url=base+sep+urllib.parse.urlencode({'qualificationId':qid,'transactionId':txid,'traceId':traceid,'sourceSha':head,'workloadEvidenceId':wid});payload=request_json(url,'GET',None,token);evidence[store]=validate_store(store,records_from(payload,store),qid,txid,traceid,head,wid,authority,key)
 all_ids=[x for e in evidence.values() for x in e['recordIdSha256']]
 if len(all_ids)!=len(set(all_ids)):raise ObsError('record identities collide across independent stores')
 result={'schemaVersion':4,'protocol':'CPF-R6-OBS-INDEPENDENT-SIGNED-PROVENANCE','sourceSha':head,'status':'PASS','qualificationIdSha256':hashlib.sha256(qid.encode()).hexdigest(),'transactionIdSha256':hashlib.sha256(txid.encode()).hexdigest(),'traceIdSha256':hashlib.sha256(traceid.encode()).hexdigest(),'workload':workload_ev,'stores':evidence};a.output_json.parent.mkdir(parents=True,exist_ok=True);a.output_json.write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8');print(f'[CPF][R6][OBS][PASS] sourceSha={head} stores={len(STORES)} independentAuthorities={len(authorities)}');return 0
if __name__=='__main__':
 try:raise SystemExit(main())
 except (ObsError,OSError,json.JSONDecodeError) as e:print(f'[CPF][R6][OBS][FAIL] {e}',file=sys.stderr);raise SystemExit(1)
