#!/usr/bin/env python3
"""Validate live resource budgets/cleanup through an approved release probe endpoint."""
from __future__ import annotations
import argparse, json, os, sys, urllib.request, uuid
from pathlib import Path
from urllib.parse import urlparse

REQUIRED=(
    'memoryBounded','threadBounded','connectionBounded','queueBounded',
    'diskBounded','tempCleaned','streamingBounded','cleanupVerified'
)

def fail(msg:str,out:Path|None=None)->int:
    result={'status':'FAIL','reason':msg}
    if out: out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(result,indent=2)+'\n')
    print(json.dumps(result));return 2

def main()->int:
    ap=argparse.ArgumentParser();ap.add_argument('--output-json',type=Path);a=ap.parse_args()
    url=os.environ.get('CPF_PERF_RESOURCE_PROBE_URL','').strip()
    if not url:return fail('CPF_PERF_RESOURCE_PROBE_URL is required',a.output_json)
    u=urlparse(url)
    if u.scheme not in {'http','https'} or not u.hostname:return fail('resource probe URL must be http/https',a.output_json)
    if u.scheme!='https' and u.hostname not in {'127.0.0.1','localhost','::1'}:return fail('non-local resource probe must use https',a.output_json)
    request_id=str(uuid.uuid4())
    body=json.dumps({'requestId':request_id,'scenario':'resource-budget-cleanup'}).encode()
    req=urllib.request.Request(url,data=body,headers={'Content-Type':'application/json','X-Cpf-Request-Id':request_id},method='POST')
    token=os.environ.get('CPF_PERF_RESOURCE_PROBE_TOKEN','').strip()
    if token:req.add_header('Authorization','Bearer '+token)
    try:
        with urllib.request.urlopen(req,timeout=float(os.environ.get('CPF_PERF_RESOURCE_TIMEOUT_SECONDS','60'))) as r:
            raw=r.read(1024*1024);status=r.status
    except Exception as e:return fail(type(e).__name__,a.output_json)
    if not 200<=status<300:return fail(f'HTTP {status}',a.output_json)
    try:data=json.loads(raw.decode())
    except Exception:return fail('probe response must be JSON',a.output_json)
    missing=[x for x in REQUIRED if data.get(x) is not True]
    if missing:return fail('resource proof missing: '+','.join(missing),a.output_json)
    if data.get('requestId') not in {None,request_id}:return fail('requestId mismatch',a.output_json)
    limits=data.get('limits')
    observed=data.get('observed')
    if not isinstance(limits,dict) or not isinstance(observed,dict):return fail('limits/observed maps are required',a.output_json)
    result={'status':'PASS','requestId':request_id,'checks':list(REQUIRED),'limits':limits,'observed':observed}
    if a.output_json:a.output_json.parent.mkdir(parents=True,exist_ok=True);a.output_json.write_text(json.dumps(result,indent=2)+'\n')
    print(json.dumps(result));return 0
if __name__=='__main__':raise SystemExit(main())
