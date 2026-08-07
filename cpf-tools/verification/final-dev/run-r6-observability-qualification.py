#!/usr/bin/env python3
from __future__ import annotations
import argparse, hashlib, json, os, re, sys, urllib.request, uuid
from pathlib import Path
from urllib.parse import urlparse

SHA40=re.compile(r'^[0-9a-f]{40}$')
REQUIRED=('correlationEndToEnd','sliSloRecorded','cardinalityBounded','maskingEnforced','alertObserved','auditIntegrityVerified','tamperRejected')
class ObsError(RuntimeError):pass

def main()->int:
 ap=argparse.ArgumentParser();ap.add_argument('--expected-head',required=True);ap.add_argument('--output-json',required=True,type=Path);a=ap.parse_args()
 head=a.expected_head.lower().strip()
 if not SHA40.fullmatch(head):raise ObsError('expected head must be a 40-char SHA')
 url=os.getenv('CPF_R6_OBSERVABILITY_PROBE_URL','').strip();token=os.getenv('CPF_R6_OBSERVABILITY_PROBE_TOKEN','').strip()
 if not url:raise ObsError('CPF_R6_OBSERVABILITY_PROBE_URL is required')
 u=urlparse(url)
 if u.scheme not in {'http','https'} or not u.hostname:raise ObsError('observability probe URL must be http/https')
 if u.scheme!='https' and u.hostname not in {'127.0.0.1','localhost','::1'}:raise ObsError('non-local observability probe must use https')
 rid=str(uuid.uuid4());body=json.dumps({'requestId':rid,'sourceSha':head,'scenario':'correlation-sli-cardinality-masking-alert-audit-tamper'}).encode()
 req=urllib.request.Request(url,data=body,headers={'Content-Type':'application/json','X-Cpf-Request-Id':rid},method='POST')
 if token:req.add_header('Authorization','Bearer '+token)
 try:
  with urllib.request.urlopen(req,timeout=float(os.getenv('CPF_R6_OBSERVABILITY_TIMEOUT_SECONDS','60'))) as r: raw=r.read(1024*1024);status=r.status
 except Exception as e:raise ObsError(type(e).__name__) from e
 if not 200<=status<300:raise ObsError(f'HTTP {status}')
 try:data=json.loads(raw.decode())
 except Exception as e:raise ObsError('probe response must be JSON') from e
 missing=[x for x in REQUIRED if data.get(x) is not True]
 if missing:raise ObsError('observability proof missing: '+','.join(missing))
 if data.get('sourceSha') not in {None,head}:raise ObsError('sourceSha mismatch')
 if data.get('requestId') not in {None,rid}:raise ObsError('requestId mismatch')
 trace=str(data.get('traceId','')).strip();audit=str(data.get('auditHash','')).strip()
 if len(trace)<8 or len(audit)<16:raise ObsError('traceId/auditHash evidence missing')
 result={'schemaVersion':1,'protocol':'CPF-R6-OBSERVABILITY','sourceSha':head,'status':'PASS','checks':list(REQUIRED),'traceIdSha256':hashlib.sha256(trace.encode()).hexdigest(),'auditHashSha256':hashlib.sha256(audit.encode()).hexdigest()}
 a.output_json.parent.mkdir(parents=True,exist_ok=True);a.output_json.write_text(json.dumps(result,indent=2)+'\n')
 print(f'[CPF][R6I][OBS][PASS] sourceSha={head} checks={len(REQUIRED)}');return 0
if __name__=='__main__':
 try:raise SystemExit(main())
 except ObsError as e:print(f'[CPF][R6I][OBS][FAIL] {e}',file=sys.stderr);raise SystemExit(1)
