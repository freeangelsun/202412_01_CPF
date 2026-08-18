#!/usr/bin/env python3
from pathlib import Path
import argparse,sys
ap=argparse.ArgumentParser(); ap.add_argument('--root',required=True); ns=ap.parse_args(); root=Path(ns.root).resolve()
witnesses={
 'WEB':'cpf-starters/web/src/main/java/com/cpf/web/api/CpfBaseController.java',
 'GATEWAY':'cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgPrimaryHandler.java',
 'MESSAGE':'cpf-starters/messaging/src/main/java/com/cpf/messaging/context/CpfMessageContextAdapter.java',
 'FILE':'cpf-starters/file/src/main/java/com/cpf/file/context/CpfFileContextSupport.java',
 'AI':'cpf-starters/integration/ai/src/main/java/com/cpf/integration/ai/CpfAiRouter.java',
 'WEBHOOK':'cpf-starters/integration/webhook/src/main/java/com/cpf/integration/webhook/CpfWebhookContextSupport.java',
 'SOAP':'cpf-starters/integration/soap/src/main/java/com/cpf/integration/soap/CpfSoapClient.java',
 'TCP':'cpf-starters/integration/tcp/src/main/java/com/cpf/integration/tcp/CpfTcpClient.java',
 'BATCH':'cpf-tools/verification/harness/context/CpfBatchContextHarness.java',
 'SECURITY':'cpf-starters/web/src/main/java/com/cpf/web/context/CpfHttpInboundContextAdapter.java',
}
fail=[]
for name,rel in witnesses.items():
 p=root/rel
 if not p.is_file(): fail.append('MISSING:'+name+':'+rel); continue
 text=p.read_text(errors='ignore')
 if name not in {'SECURITY'} and not any(t in text for t in ['CpfContexts','CpfContextExecutionFactory','transactionId']): fail.append('NO_CONTEXT_WITNESS:'+name)
 if name=='SECURITY' and not all(t in text for t in ('authenticated','CpfIdentityContext','TRUSTED_INTERNAL')): fail.append('NO_TRUSTED_SECURITY_CONTEXT_WITNESS:SECURITY')
# no legacy dynamic context mechanisms in actual product sources
for p in root.rglob('*.java'):
 rel=p.relative_to(root).as_posix()
 if rel.startswith('cpf-tools/verification/') or '/build/' in rel: continue
 text=p.read_text(errors='ignore')
 for token in ['CpfContextRegistry','CpfContextFactory','CpfContextAccessor']:
  if token in text: fail.append('LEGACY:'+token+':'+rel)
print('CPF_TXID_ALL_CHANNEL_STATIC='+('PASS' if not fail else 'FAIL'))
print('witnesses='+str(len(witnesses))+' failures='+str(len(fail)))
for x in fail: print(x)
sys.exit(1 if fail else 0)
