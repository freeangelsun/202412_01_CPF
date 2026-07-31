#!/usr/bin/env python3
from pathlib import Path
import argparse
p=argparse.ArgumentParser();p.add_argument('--root',default='.');a=p.parse_args();root=Path(a.root)
base=root/'cpf-batch/execution-runtime/src'
files={x:(base/x).read_text() for x in [
'main/java/com/cpf/batch/execution/CpfBatchKafkaRemoteConfiguration.java',
'main/java/com/cpf/batch/execution/CpfBatchKafkaInboundBridge.java',
'main/java/com/cpf/batch/execution/CpfBatchKafkaWorkerListener.java',
'main/java/com/cpf/batch/execution/CpfSynchronousWorkerChannel.java',
'test/java/com/cpf/batch/execution/CpfBatchKafkaWorkerListenerAckBoundaryTest.java']}
joined='\n'.join(files.values()); required=['MANUAL_IMMEDIATE','setCommitRecovered(true)','CpfSynchronousWorkerChannel','ledger.complete','bridge.request(json);acknowledgment.acknowledge()','handlerOrLedgerFailureNeverAcknowledges']
missing=[t for t in required if t not in joined]
if 'new DirectChannel()' in files['main/java/com/cpf/batch/execution/CpfBatchKafkaRemoteConfiguration.java']: missing.append('legacy DirectChannel')
if missing: raise SystemExit('Kafka ACK contract failed: '+', '.join(missing))
print('CPF Kafka handler/ledger/ACK contract: PASS')
