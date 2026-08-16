#!/usr/bin/env python3
"""Broker produce/consume/reconnect/retry-DLQ/backpressure semantic qualification."""
from __future__ import annotations
import sys
from pathlib import Path as _TrustPath
sys.path.insert(0,str(_TrustPath(__file__).resolve().parents[1]/'verification'))
from release_target_trust import verify_release_target, self_test as trust_self_test
import argparse,json,math,os,urllib.request,uuid
from urllib.parse import urlparse
class BrokerError(RuntimeError):pass

def num(v,n):
 if isinstance(v,bool) or not isinstance(v,(int,float)) or not math.isfinite(float(v)) or float(v)<0:raise BrokerError(n+' must be non-negative numeric')
 return float(v)
def text(v,n,minlen=3):
 s=str(v or '').strip()
 if len(s)<minlen:raise BrokerError(n+' missing')
 return s

def validate_payload(p:dict,rid:str)->dict:
 if p.get('requestId') not in {None,rid}:raise BrokerError('requestId mismatch')
 mid=text(p.get('messageId'),'messageId',8);topic=text(p.get('topic'),'topic');partition=p.get('partition')
 if not isinstance(partition,int) or isinstance(partition,bool) or partition<0:raise BrokerError('partition must be integer >=0')
 prod=p.get('producerEvidence');cons=p.get('consumerEvidence')
 if not isinstance(prod,dict) or not isinstance(cons,dict):raise BrokerError('producerEvidence/consumerEvidence required')
 if str(prod.get('messageId',''))!=mid or str(cons.get('messageId',''))!=mid:raise BrokerError('messageId must correlate producer and consumer')
 po=num(prod.get('offset'),'producer.offset');co=num(cons.get('offset'),'consumer.offset')
 if co<po:raise BrokerError('consumer offset cannot precede produced offset')
 reconnect=p.get('reconnectEvidence')
 if not isinstance(reconnect,dict) or not reconnect.get('faultInjected'):raise BrokerError('reconnect fault injection evidence required')
 before=text(reconnect.get('connectionBefore'),'connectionBefore');after=text(reconnect.get('connectionAfter'),'connectionAfter')
 if before==after:raise BrokerError('reconnect must establish a new connection identity')
 delivery=p.get('deliveryEvidence')
 if not isinstance(delivery,dict):raise BrokerError('retry/DLQ delivery evidence required')
 attempts=delivery.get('attempts')
 if not isinstance(attempts,list) or len(attempts)<2:raise BrokerError('at least two delivery attempts required')
 if str(delivery.get('finalOutcome','')).upper() not in {'CONSUMED','DLQ'}:raise BrokerError('finalOutcome must be CONSUMED or DLQ')
 if delivery.get('finalOutcome','').upper()=='DLQ' and len(str(delivery.get('dlqMessageId','')))<8:raise BrokerError('DLQ outcome requires dlqMessageId')
 bp=p.get('backpressure')
 if not isinstance(bp,dict) or not bp.get('saturationObserved'):raise BrokerError('measured backpressure saturation required')
 limit=num(bp.get('queueLimit'),'backpressure.queueLimit');peak=num(bp.get('peakQueueDepth'),'backpressure.peakQueueDepth');lag=num(bp.get('peakConsumerLag'),'backpressure.peakConsumerLag')
 if peak>limit:raise BrokerError(f'queue depth exceeded limit peak={peak} limit={limit}')
 if len(str(bp.get('sampleSeriesId','')))<8:raise BrokerError('backpressure sampleSeriesId required')
 return {'messageId':mid,'topic':topic,'partition':partition,'producerOffset':po,'consumerOffset':co,'connectionBefore':before,'connectionAfter':after,'deliveryAttempts':len(attempts),'peakQueueDepth':peak,'queueLimit':limit,'peakConsumerLag':lag}

def fail(m):print(json.dumps({'status':'FAIL','reason':m},ensure_ascii=False));return 2
def self_test():
 bad={'requestId':'r','produced':True,'consumed':True,'reconnected':True,'backpressureBounded':True}
 try:validate_payload(bad,'r')
 except BrokerError:pass
 else:return fail('boolean-only broker evidence survived')
 good={'requestId':'r','messageId':'message-001','topic':'cpf.events','partition':0,'producerEvidence':{'messageId':'message-001','offset':10},'consumerEvidence':{'messageId':'message-001','offset':10},'reconnectEvidence':{'faultInjected':True,'connectionBefore':'conn-a','connectionAfter':'conn-b'},'deliveryEvidence':{'attempts':[{'attempt':1,'status':'FAILED'},{'attempt':2,'status':'CONSUMED'}],'finalOutcome':'CONSUMED'},'backpressure':{'saturationObserved':True,'queueLimit':100,'peakQueueDepth':90,'peakConsumerLag':12,'sampleSeriesId':'series-001'}}
 validate_payload(good,'r');print('[CPF][BROKER][PASS] selfTest=true idsOffsets=true reconnect=true retryDlq=true backpressure=true');return 0

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--self-test',action='store_true');ap.add_argument('--expected-head',default=os.environ.get('CPF_EXPECTED_HEAD',''));a=ap.parse_args()
 if a.self_test:
  trust_self_test();return self_test()
 url=os.environ.get('CPF_PERF_BROKER_PROBE_URL','').strip()
 if len(a.expected_head.strip())!=40:return fail('expected checkout HEAD is required')
 if not url:return fail('CPF_PERF_BROKER_PROBE_URL is required')
 u=urlparse(url)
 if u.scheme not in {'http','https'} or not u.hostname:return fail('broker probe URL must be http/https')
 if u.scheme!='https' and u.hostname not in {'127.0.0.1','localhost','::1'}:return fail('non-local broker probe must use https')
 verify_release_target(url,a.expected_head)
 rid=str(uuid.uuid4());body=json.dumps({'requestId':rid,'scenario':'produce-consume-fault-reconnect-retry-dlq-backpressure'}).encode();req=urllib.request.Request(url,data=body,headers={'Content-Type':'application/json','X-Cpf-Request-Id':rid},method='POST');token=os.environ.get('CPF_PERF_BROKER_PROBE_TOKEN','').strip()
 if token:req.add_header('Authorization','Bearer '+token)
 try:
  with urllib.request.urlopen(req,timeout=float(os.environ.get('CPF_PERF_BROKER_TIMEOUT_SECONDS','60'))) as res:raw=res.read(2*1024*1024);status=res.status
 except Exception as e:return fail(type(e).__name__)
 if not 200<=status<300:return fail(f'HTTP {status}')
 try:p=json.loads(raw.decode())
 except Exception:return fail('probe response must be JSON')
 try:ev=validate_payload(p,rid)
 except BrokerError as e:return fail(str(e))
 print(json.dumps({'schemaVersion':2,'protocol':'CPF-BROKER-IDENTITY-FAULT','status':'PASS','requestId':rid,**ev},ensure_ascii=False));return 0
if __name__=='__main__':raise SystemExit(main())
