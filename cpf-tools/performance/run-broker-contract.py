#!/usr/bin/env python3
"""Live broker semantic probe used only by the R6 release performance workload.
The configured endpoint must perform produce -> consume -> reconnect/backpressure validation
and return the required semantic booleans. Secrets are accepted only through environment headers.
"""
from __future__ import annotations
import json, os, sys, urllib.request, urllib.error, uuid
from urllib.parse import urlparse

def fail(msg: str) -> int:
    print(json.dumps({'status':'FAIL','reason':msg}, ensure_ascii=False))
    return 2

def main() -> int:
    url=os.environ.get('CPF_PERF_BROKER_PROBE_URL','').strip()
    if not url: return fail('CPF_PERF_BROKER_PROBE_URL is required')
    parsed=urlparse(url)
    if parsed.scheme not in {'http','https'} or not parsed.hostname: return fail('broker probe URL must be http/https')
    if parsed.scheme!='https' and parsed.hostname not in {'127.0.0.1','localhost','::1'}: return fail('non-local broker probe must use https')
    request_id=str(uuid.uuid4())
    body=json.dumps({'requestId':request_id,'scenario':'produce-consume-reconnect-backpressure'}).encode()
    req=urllib.request.Request(url,data=body,headers={'Content-Type':'application/json','X-Cpf-Request-Id':request_id},method='POST')
    token=os.environ.get('CPF_PERF_BROKER_PROBE_TOKEN','').strip()
    if token: req.add_header('Authorization','Bearer '+token)
    try:
        with urllib.request.urlopen(req,timeout=float(os.environ.get('CPF_PERF_BROKER_TIMEOUT_SECONDS','20'))) as res:
            raw=res.read(1024*1024); status=res.status
    except Exception as exc:
        return fail(type(exc).__name__)
    if status < 200 or status >= 300: return fail(f'HTTP {status}')
    try: payload=json.loads(raw.decode('utf-8'))
    except Exception: return fail('probe response must be JSON')
    required=('produced','consumed','reconnected','backpressureBounded')
    missing=[k for k in required if payload.get(k) is not True]
    if missing: return fail('semantic proof missing: '+','.join(missing))
    if payload.get('requestId') not in {None,request_id}: return fail('requestId mismatch')
    print(json.dumps({'status':'PASS','requestId':request_id,'checks':list(required)},ensure_ascii=False))
    return 0
if __name__=='__main__': raise SystemExit(main())
