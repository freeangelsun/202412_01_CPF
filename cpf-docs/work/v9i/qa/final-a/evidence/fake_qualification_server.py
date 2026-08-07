from http.server import BaseHTTPRequestHandler, HTTPServer
from urllib.parse import urlparse, parse_qs
import json
class H(BaseHTTPRequestHandler):
    def log_message(self,*a): pass
    def sendj(self,code,obj):
        raw=json.dumps(obj).encode(); self.send_response(code); self.send_header('Content-Type','application/json'); self.send_header('Content-Length',str(len(raw))); self.end_headers(); self.wfile.write(raw)
    def do_POST(self):
        n=int(self.headers.get('Content-Length','0') or 0); raw=self.rfile.read(n)
        try:d=json.loads(raw or b'{}')
        except:d={}
        p=urlparse(self.path).path
        if p=='/obs': return self.sendj(200,{'requestId':d.get('requestId'),'qualificationId':'fake-qualification-0001','transactionId':'2026080801010100012345678901234567','traceId':'fake-trace-00000001'})
        if p=='/dr': return self.sendj(200,{'splitBrainDetected':True,'splitBrainFenced':True,'powerLossRecovered':True,'selectiveRollbackSafe':True,'reconciled':True,'dataConsistent':True,'artifactConsistent':True,'rpoSeconds':0,'rtoSeconds':0})
        if p=='/resource': return self.sendj(200,{'requestId':d.get('requestId'),'memoryBounded':True,'threadBounded':True,'connectionBounded':True,'queueBounded':True,'diskBounded':True,'tempCleaned':True,'streamingBounded':True,'cleanupVerified':True,'limits':{'memory':1,'threads':1},'observed':{'memory':999999999,'threads':99999}})
        if p=='/batch': return self.sendj(200,{'requestId':d.get('requestId'),'launched':True,'observed':True,'restartSafe':True,'reconciled':True})
        if p=='/broker': return self.sendj(200,{'requestId':d.get('requestId'),'produced':True,'consumed':True,'reconnected':True,'backpressureBounded':True})
        if p=='/deny': return self.sendj(403,{'error':'forbidden'})
        return self.sendj(404,{'error':'not found'})
    def do_GET(self):
        u=urlparse(self.path); p=u.path; q=parse_qs(u.query)
        if p=='/deny': return self.sendj(403,{'error':'forbidden'})
        qid=q.get('qualificationId',['fake-qualification-0001'])[0]; tx=q.get('transactionId',['2026080801010100012345678901234567'])[0]; tr=q.get('traceId',['fake-trace-00000001'])[0]
        c={'qualificationId':qid,'transactionId':tx,'traceId':tr}
        if p=='/metric': rec={**c,'recordId':'m1','metricName':'fake.metric','value':1.0}
        elif p=='/log': rec={**c,'recordId':'l1','message':'fake failure','severity':'ERROR'}
        elif p=='/trace': rec={**c,'recordId':'t1','spanId':'fake-span-1'}
        elif p=='/audit': rec={**c,'recordId':'a1','action':'FAKE_ACTION','outcome':'SUCCESS'}
        elif p=='/alert': return self.sendj(200,{'records':[{**c,'recordId':'al1','state':'FIRING'},{**c,'recordId':'al2','state':'RESOLVED'}]})
        else:return self.sendj(404,{'error':'not found'})
        return self.sendj(200,{'records':[rec]})
HTTPServer(('127.0.0.1',18777),H).serve_forever()
