import json, os, sys, hmac, hashlib, urllib.parse
from http.server import ThreadingHTTPServer, BaseHTTPRequestHandler
from datetime import datetime, timezone, timedelta

PORT=int(os.environ.get('QA_PORT','18765'))
HEAD=os.environ['QA_HEAD']
DR_FILE=os.environ.get('QA_DR_FILE','/mnt/data/qa_dr_harness.json')
OBS_WORKLOAD=os.environ.get('QA_OBS_WORKLOAD','/mnt/data/qa_obs_workload.json')
WORK_KEY=os.environ.get('QA_WORK_KEY','workload-key-12345')
WORK_AUTH=os.environ.get('QA_WORK_AUTH','fake-workload-authority')
STORE_KEYS={s:os.environ.get('QA_'+s.upper()+'_KEY',s+'-key-123456789') for s in ['metric','log','trace','alert','audit']}
STORE_AUTHS={s:os.environ.get('QA_'+s.upper()+'_AUTH','fake-'+s+'-authority') for s in STORE_KEYS}
state={}

def write_json(path,obj):
    with open(path,'w',encoding='utf-8') as f: json.dump(obj,f,ensure_ascii=False,indent=2)

def workloads_sig(e):
    msg='|'.join(str(e[k]) for k in ('workloadEvidenceId','requestId','qualificationId','transactionId','traceId','sourceSha','artifactSha256','provenanceAuthority')).encode()
    return hmac.new(WORK_KEY.encode(),msg,hashlib.sha256).hexdigest()

def rec_sig(store,r):
    msg='|'.join([store]+[str(r[k]) for k in ('recordId','qualificationId','transactionId','traceId','sourceSha','workloadEvidenceId','provenanceAuthority')]).encode()
    return hmac.new(STORE_KEYS[store].encode(),msg,hashlib.sha256).hexdigest()

class H(BaseHTTPRequestHandler):
    def log_message(self, fmt,*args): pass
    def sendj(self,status,obj,headers=None):
        raw=json.dumps(obj).encode(); self.send_response(status); self.send_header('Content-Type','application/json'); self.send_header('Content-Length',str(len(raw)))
        if headers:
            for k,v in headers.items(): self.send_header(k,v)
        self.end_headers(); self.wfile.write(raw)
    def do_POST(self):
        n=int(self.headers.get('Content-Length','0')); body=self.rfile.read(n) if n else b'{}'
        try: p=json.loads(body or b'{}')
        except: p={}
        path=urllib.parse.urlparse(self.path).path
        rid=p.get('requestId') or self.headers.get('X-Cpf-Request-Id') or 'request-unknown'
        # Generic performance resource fake
        if path=='/resource':
            keys=['memoryBytes','threadCount','connectionCount','queueDepth','diskBytes','tempBytes','streamBufferBytes']
            obj={k:True for k in ['memoryBounded','threadBounded','connectionBounded','queueBounded','diskBounded','tempCleaned','streamingBounded','cleanupVerified']}
            obj.update({'requestId':rid,'limits':{k:100 for k in keys},'observed':{k:50 for k in keys},
                        'workload':{'workloadId':'fake-workload-001','sampleCount':4,'peakConcurrency':8,'durationMs':1000},
                        'cleanup':{'before':{'tempBytes':50,'openFiles':4,'activeConnections':3,'activeThreads':8},'after':{'tempBytes':0,'openFiles':0,'activeConnections':0,'activeThreads':0},'allowedResidual':{'tempBytes':0,'openFiles':0,'activeConnections':0,'activeThreads':0}},
                        'backpressure':{'saturationObserved':True,'queueLimit':100,'peakQueueDepth':90}})
            return self.sendj(200,obj)
        if path=='/batch':
            tx='T'*34
            obj={'requestId':rid,'originalExecutionId':'fake-exec-001','restartExecutionId':'fake-exec-002','restartOfExecutionId':'fake-exec-001','transactionId':tx,
                 'stateTransitions':[{'state':'STARTING'},{'state':'RUNNING'},{'state':'FAILED'},{'state':'RESTARTING'},{'state':'COMPLETED'}],
                 'faultEvidence':{'type':'PROCESS_KILL','killedOwner':'fake-node-a','takeoverOwner':'fake-node-b','oldFenceToken':3,'newFenceToken':4},
                 'reconcile':{'transactionId':tx,'originalExecutionId':'fake-exec-001','restartExecutionId':'fake-exec-002','duplicateMutationCount':0,'finalState':'COMPLETED'},
                 'dbEvidence':{'queryId':'fake-batch-query','rowHashSha256':'a'*64},'runtimeEvidence':[{'recordId':'fake-run-1'},{'recordId':'fake-run-2'}]}
            return self.sendj(200,obj)
        if path=='/broker':
            mid='fake-message-001'
            obj={'requestId':rid,'messageId':mid,'topic':'fake.topic','partition':0,
                 'producerEvidence':{'messageId':mid,'offset':10},'consumerEvidence':{'messageId':mid,'offset':10},
                 'reconnectEvidence':{'faultInjected':True,'connectionBefore':'fake-conn-a','connectionAfter':'fake-conn-b'},
                 'deliveryEvidence':{'attempts':[{'attempt':1,'status':'FAILED'},{'attempt':2,'status':'CONSUMED'}],'finalOutcome':'CONSUMED'},
                 'backpressure':{'saturationObserved':True,'queueLimit':100,'peakQueueDepth':90,'peakConsumerLag':12,'sampleSeriesId':'fake-series-001'}}
            return self.sendj(200,obj)
        if path=='/dr':
            run='fake-chaos-run-001'; now=datetime.now(timezone.utc); t0=now; t1=now+timedelta(seconds=1)
            ev={'schemaVersion':2,'sourceSha':HEAD,'requestId':rid,'chaosRunId':run,'harnessId':'fake-harness-001','provenanceAuthority':'fake-dr-authority',
                'faultEvents':[{'type':x,'injectedByHarness':True,'eventId':'fake-'+x.lower()+'-event','injectedAt':t0.isoformat(),'recoveredAt':t1.isoformat()} for x in ['SPLIT_BRAIN','POWER_LOSS','SELECTIVE_ROLLBACK']],
                'dataHashBefore':'b'*64,'dataHashAfter':'b'*64,'artifactHashBefore':'c'*64,'artifactHashAfter':'c'*64,
                'lastCommittedAt':t0.isoformat(),'recoveredDataThrough':t0.isoformat(),'faultStartedAt':t0.isoformat(),'serviceHealthyAt':t1.isoformat(),
                'splitBrainFenceObserved':True,'reconcileVerified':True}
            write_json(DR_FILE,ev)
            return self.sendj(200,{'sourceSha':HEAD,'requestId':rid,'chaosRunId':run})
        if path=='/obs-probe':
            qid='fake-qualification-001'; tx='X'*34; tr='fake-trace-001'; wid='fake-workload-evidence-001'
            state.update({'rid':rid,'qid':qid,'tx':tx,'tr':tr,'wid':wid})
            ev={'schemaVersion':2,'workloadEvidenceId':wid,'requestId':rid,'qualificationId':qid,'transactionId':tx,'traceId':tr,'sourceSha':HEAD,'artifactSha256':'d'*64,'provenanceAuthority':WORK_AUTH,'events':[{'kind':'SUCCESS'},{'kind':'FAILURE'}]}
            ev['provenanceSignature']=workloads_sig(ev); write_json(OBS_WORKLOAD,ev)
            return self.sendj(200,{'sourceSha':HEAD,'requestId':rid,'qualificationId':qid,'transactionId':tx,'traceId':tr})
        # security: always block, not CPF
        if path.startswith('/security'):
            return self.sendj(403,{'status':'blocked'}, {'Set-Cookie':'SESSION=newfake; HttpOnly'})
        return self.sendj(404,{'error':'not found'})
    def do_GET(self):
        path=urllib.parse.urlparse(self.path).path
        if path.startswith('/obs-'):
            store=path.split('/obs-',1)[1]
            s=state; base={'recordId':'fake-'+store+'-record-001','qualificationId':s.get('qid',''),'transactionId':s.get('tx',''),'traceId':s.get('tr',''),'sourceSha':HEAD,'workloadEvidenceId':s.get('wid',''),'provenanceAuthority':STORE_AUTHS[store]}
            if store=='metric': base.update(metricName='cpf.fake.metric',value=1)
            elif store=='log': base.update(message='fake log',level='INFO')
            elif store=='trace': base.update(spanId='fake-span-001')
            elif store=='audit': base.update(action='FAKE',outcome='SUCCESS')
            elif store=='alert':
                r1=base.copy();r1['recordId']='fake-alert-record-001';r1['state']='FIRING';r1['provenanceSignature']=rec_sig(store,r1)
                r2=base.copy();r2['recordId']='fake-alert-record-002';r2['state']='RESOLVED';r2['provenanceSignature']=rec_sig(store,r2)
                return self.sendj(200,{'records':[r1,r2]})
            base['provenanceSignature']=rec_sig(store,base); return self.sendj(200,{'records':[base]})
        return self.sendj(404,{'error':'not found'})

ThreadingHTTPServer(('127.0.0.1',PORT),H).serve_forever()
