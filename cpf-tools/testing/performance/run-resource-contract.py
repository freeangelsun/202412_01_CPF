#!/usr/bin/env python3
"""Validate measured resource budgets and cleanup evidence; self-attested booleans never suffice."""
from __future__ import annotations
import sys
from pathlib import Path as _TrustPath
sys.path.insert(0,str(_TrustPath(__file__).resolve().parents[2]/'verification'))
from release_target_trust import verify_release_target, self_test as trust_self_test
import argparse, json, math, os, urllib.request, uuid
from pathlib import Path
from urllib.parse import urlparse

REQUIRED_FLAGS=(
    'memoryBounded','threadBounded','connectionBounded','queueBounded',
    'diskBounded','tempCleaned','streamingBounded','cleanupVerified'
)
REQUIRED_BUDGETS=('memoryBytes','threadCount','connectionCount','queueDepth','diskBytes','tempBytes','streamBufferBytes')

class ResourceError(RuntimeError): pass

def numeric(v,name):
    if isinstance(v,bool) or not isinstance(v,(int,float)) or not math.isfinite(float(v)) or float(v)<0:
        raise ResourceError(f'{name} must be a finite non-negative number')
    return float(v)

def validate_payload(data:dict, request_id:str)->dict:
    missing=[x for x in REQUIRED_FLAGS if data.get(x) is not True]
    if missing: raise ResourceError('resource proof missing: '+','.join(missing))
    if data.get('requestId') not in {None,request_id}: raise ResourceError('requestId mismatch')
    limits=data.get('limits'); observed=data.get('observed')
    if not isinstance(limits,dict) or not isinstance(observed,dict): raise ResourceError('limits/observed maps are required')
    comparisons={}
    for key in REQUIRED_BUDGETS:
        limit=numeric(limits.get(key),f'limits.{key}'); actual=numeric(observed.get(key),f'observed.{key}')
        comparisons[key]={'limit':limit,'observed':actual,'withinBudget':actual<=limit}
        if actual>limit: raise ResourceError(f'resource budget exceeded {key}: observed={actual} limit={limit}')
    workload=data.get('workload')
    if not isinstance(workload,dict): raise ResourceError('measured workload evidence is required')
    workload_id=str(workload.get('workloadId','')).strip()
    sample_count=workload.get('sampleCount')
    saturation=numeric(workload.get('peakConcurrency'),'workload.peakConcurrency')
    duration=numeric(workload.get('durationMs'),'workload.durationMs')
    if len(workload_id)<8 or not isinstance(sample_count,int) or isinstance(sample_count,bool) or sample_count<3 or saturation<=0 or duration<=0:
        raise ResourceError('workload must contain workloadId, >=3 samples, positive concurrency and duration')
    cleanup=data.get('cleanup')
    if not isinstance(cleanup,dict): raise ResourceError('cleanup before/after evidence is required')
    cleanup_result={}
    for key in ('tempBytes','openFiles','activeConnections','activeThreads'):
        before=numeric(cleanup.get('before',{}).get(key),f'cleanup.before.{key}') if isinstance(cleanup.get('before'),dict) else (_ for _ in ()).throw(ResourceError('cleanup.before map required'))
        after=numeric(cleanup.get('after',{}).get(key),f'cleanup.after.{key}') if isinstance(cleanup.get('after'),dict) else (_ for _ in ()).throw(ResourceError('cleanup.after map required'))
        allowed=numeric(cleanup.get('allowedResidual',{}).get(key,0),f'cleanup.allowedResidual.{key}') if isinstance(cleanup.get('allowedResidual'),dict) else 0
        if after>allowed: raise ResourceError(f'cleanup residual exceeded {key}: after={after} allowed={allowed}')
        cleanup_result[key]={'before':before,'after':after,'allowedResidual':allowed}
    if not data.get('backpressure') or not isinstance(data['backpressure'],dict): raise ResourceError('backpressure measurement is required')
    bp=data['backpressure']; limit=numeric(bp.get('queueLimit'),'backpressure.queueLimit'); peak=numeric(bp.get('peakQueueDepth'),'backpressure.peakQueueDepth')
    if not bp.get('saturationObserved') or peak>limit: raise ResourceError('backpressure saturation/queue bound evidence failed')
    return {'comparisons':comparisons,'workloadId':workload_id,'sampleCount':sample_count,'cleanup':cleanup_result,'backpressure':{'queueLimit':limit,'peakQueueDepth':peak}}

def fail(msg:str,out:Path|None=None)->int:
    result={'status':'FAIL','reason':msg}
    if out: out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(result,indent=2)+'\n')
    print(json.dumps(result)); return 2

def self_test()->int:
    bad={k:True for k in REQUIRED_FLAGS}; bad.update({'requestId':'r','limits':{k:1 for k in REQUIRED_BUDGETS},'observed':{k:1 for k in REQUIRED_BUDGETS}}); bad['observed']['memoryBytes']=999999999
    try: validate_payload(bad,'r')
    except ResourceError: pass
    else: return fail('mutation survived: limit=1 observed=999999999')
    good={k:True for k in REQUIRED_FLAGS}; good.update({'requestId':'r','limits':{k:100 for k in REQUIRED_BUDGETS},'observed':{k:50 for k in REQUIRED_BUDGETS},'workload':{'workloadId':'workload-001','sampleCount':4,'peakConcurrency':8,'durationMs':1000},'cleanup':{'before':{'tempBytes':50,'openFiles':4,'activeConnections':3,'activeThreads':8},'after':{'tempBytes':0,'openFiles':0,'activeConnections':0,'activeThreads':0},'allowedResidual':{'tempBytes':0,'openFiles':0,'activeConnections':0,'activeThreads':0}},'backpressure':{'saturationObserved':True,'queueLimit':100,'peakQueueDepth':90}})
    validate_payload(good,'r')
    print('[CPF][RESOURCE][PASS] selfTest=true numericBudget=true cleanup=true backpressure=true'); return 0

def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--output-json',type=Path); ap.add_argument('--self-test',action='store_true'); ap.add_argument('--expected-head',default=os.environ.get('CPF_EXPECTED_HEAD','')); a=ap.parse_args()
    if a.self_test:
        trust_self_test(); return self_test()
    url=os.environ.get('CPF_PERF_RESOURCE_PROBE_URL','').strip()
    if len(a.expected_head.strip())!=40:return fail('expected checkout HEAD is required',a.output_json)
    if not url:return fail('CPF_PERF_RESOURCE_PROBE_URL is required',a.output_json)
    u=urlparse(url)
    if u.scheme not in {'http','https'} or not u.hostname:return fail('resource probe URL must be http/https',a.output_json)
    if u.scheme!='https' and u.hostname not in {'127.0.0.1','localhost','::1'}:return fail('non-local resource probe must use https',a.output_json)
    verify_release_target(url,a.expected_head)
    request_id=str(uuid.uuid4()); body=json.dumps({'requestId':request_id,'scenario':'resource-budget-cleanup'}).encode()
    req=urllib.request.Request(url,data=body,headers={'Content-Type':'application/json','X-Cpf-Request-Id':request_id},method='POST')
    token=os.environ.get('CPF_PERF_RESOURCE_PROBE_TOKEN','').strip()
    if token:req.add_header('Authorization','Bearer '+token)
    try:
        with urllib.request.urlopen(req,timeout=float(os.environ.get('CPF_PERF_RESOURCE_TIMEOUT_SECONDS','60'))) as r: raw=r.read(1024*1024); status=r.status
    except Exception as e:return fail(type(e).__name__,a.output_json)
    if not 200<=status<300:return fail(f'HTTP {status}',a.output_json)
    try:data=json.loads(raw.decode())
    except Exception:return fail('probe response must be JSON',a.output_json)
    try: evidence=validate_payload(data,request_id)
    except ResourceError as e:return fail(str(e),a.output_json)
    result={'schemaVersion':2,'protocol':'CPF-RESOURCE-MEASURED-BUDGET','status':'PASS','requestId':request_id,**evidence}
    if a.output_json:a.output_json.parent.mkdir(parents=True,exist_ok=True);a.output_json.write_text(json.dumps(result,indent=2)+'\n')
    print(json.dumps(result));return 0
if __name__=='__main__':raise SystemExit(main())
