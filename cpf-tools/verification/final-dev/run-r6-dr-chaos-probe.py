#!/usr/bin/env python3
from __future__ import annotations
import argparse,json,os,re,sys,urllib.request,uuid
from pathlib import Path
from urllib.parse import urlparse
SHA40=re.compile(r'^[0-9a-f]{40}$')
REQUIRED=('splitBrainFenced','powerLossRecovered','selectiveRollbackSafe','reconciled','dataConsistent','artifactConsistent')
class DrError(RuntimeError):pass

def number(v,name):
 if isinstance(v,bool) or not isinstance(v,(int,float)) or v<0:raise DrError(f'{name} must be non-negative numeric')
 return float(v)

def main()->int:
 ap=argparse.ArgumentParser();ap.add_argument('--expected-head',required=True);ap.add_argument('--output-json',required=True,type=Path);a=ap.parse_args();head=a.expected_head.lower().strip()
 if not SHA40.fullmatch(head):raise DrError('expected head must be a 40-char SHA')
 url=os.getenv('CPF_R6_DR_CHAOS_PROBE_URL','').strip();token=os.getenv('CPF_R6_DR_CHAOS_PROBE_TOKEN','').strip()
 if not url:raise DrError('CPF_R6_DR_CHAOS_PROBE_URL is required')
 u=urlparse(url)
 if u.scheme not in {'http','https'} or not u.hostname:raise DrError('DR chaos probe URL must be http/https')
 if u.scheme!='https' and u.hostname not in {'127.0.0.1','localhost','::1'}:raise DrError('non-local DR chaos probe must use https')
 rid=str(uuid.uuid4());body=json.dumps({'requestId':rid,'sourceSha':head,'scenario':'split-brain-power-loss-selective-rollback-reconcile'}).encode()
 req=urllib.request.Request(url,data=body,headers={'Content-Type':'application/json','X-Cpf-Request-Id':rid},method='POST')
 if token:req.add_header('Authorization','Bearer '+token)
 try:
  with urllib.request.urlopen(req,timeout=float(os.getenv('CPF_R6_DR_CHAOS_TIMEOUT_SECONDS','900'))) as r:raw=r.read(1024*1024);status=r.status
 except Exception as e:raise DrError(type(e).__name__) from e
 if not 200<=status<300:raise DrError(f'HTTP {status}')
 try:data=json.loads(raw.decode())
 except Exception as e:raise DrError('probe response must be JSON') from e
 missing=[x for x in REQUIRED if data.get(x) is not True]
 if missing:raise DrError('DR semantic proof missing: '+','.join(missing))
 if data.get('sourceSha') not in {None,head}:raise DrError('sourceSha mismatch')
 rpo=number(data.get('rpoSeconds'),'rpoSeconds');rto=number(data.get('rtoSeconds'),'rtoSeconds')
 max_rpo=float(os.getenv('CPF_R6_DR_MAX_RPO_SECONDS','300'));max_rto=float(os.getenv('CPF_R6_DR_MAX_RTO_SECONDS','1800'))
 if rpo>max_rpo:raise DrError(f'RPO exceeded actual={rpo} max={max_rpo}')
 if rto>max_rto:raise DrError(f'RTO exceeded actual={rto} max={max_rto}')
 result={'schemaVersion':1,'protocol':'CPF-R6-DR-CHAOS','sourceSha':head,'status':'PASS','checks':list(REQUIRED),'rpoSeconds':rpo,'rtoSeconds':rto,'maxRpoSeconds':max_rpo,'maxRtoSeconds':max_rto}
 a.output_json.parent.mkdir(parents=True,exist_ok=True);a.output_json.write_text(json.dumps(result,indent=2)+'\n')
 print(f'[CPF][R6I][DR-CHAOS][PASS] sourceSha={head} rpo={rpo} rto={rto}');return 0
if __name__=='__main__':
 try:raise SystemExit(main())
 except DrError as e:print(f'[CPF][R6I][DR-CHAOS][FAIL] {e}',file=sys.stderr);raise SystemExit(1)
