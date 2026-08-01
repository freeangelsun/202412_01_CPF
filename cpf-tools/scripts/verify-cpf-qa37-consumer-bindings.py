#!/usr/bin/env python3
from __future__ import annotations
import argparse,json,re,sys
from collections import Counter
from pathlib import Path
EXPECTED={'JDBC_COMMAND':51,'JDBC_QUERY':6,'HTTP':5,'OUTBOX':6,'FILE':6,'PROCESS':17,'SPRING_BATCH':30,'REFERENCE_GATEWAY':14}
def fail(m):print('[CPF][QA37][CONSUMER][FAIL] '+m,file=sys.stderr);raise SystemExit(1)
def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');a=ap.parse_args();root=Path(a.root).resolve();ref=root/'cpf-reference'
 cat=json.loads((ref/'src/main/resources/edu/manual-135-catalog.json').read_text(encoding='utf-8'));counts=Counter()
 for f in cat['features']:
  b=f.get('consumerBinding') or {};t=b.get('type');counts[t]+=1
  if b.get('ownerModule')!='cpf-reference':fail('binding owner '+f['requirementId'])
  if t=='PROCESS' and not (root/b.get('entryPoint','')).is_file():fail('allowlisted process target missing '+f['requirementId'])
  if t=='HTTP':
   if b.get('configurationKey')!='cpf.edu.counterparty.base-url':fail('HTTP must use REF counterparty property '+f['requirementId'])
   if not str(b.get('entryPoint','')).startswith('/external/'):fail('HTTP entrypoint outside independent counterparty '+f['requirementId'])
  if t=='SPRING_BATCH':
   job=b.get('entryPoint','');matches=list((ref/'src/main/java/com/cpf/reference/batch').rglob('*JobConfiguration.java'))
   if not any(job in p.read_text(encoding='utf-8',errors='ignore') for p in matches):fail('Spring Batch Job bean missing '+f['requirementId'])
 if dict(counts)!=EXPECTED:fail('binding distribution '+str(counts))
 required={
  'JDBC_COMMAND':'src/main/java/com/cpf/reference/edu/runtime/consumer/jdbc/JdbcEduBusinessConsumer.java',
  'JDBC_QUERY':'src/main/java/com/cpf/reference/edu/runtime/consumer/jdbc/JdbcQueryEduBusinessConsumer.java',
  'HTTP':'src/main/java/com/cpf/reference/edu/runtime/consumer/http/HttpEduBusinessConsumer.java',
  'OUTBOX':'src/main/java/com/cpf/reference/edu/runtime/consumer/outbox/OutboxEduBusinessConsumer.java',
  'FILE':'src/main/java/com/cpf/reference/edu/runtime/consumer/file/FileEduBusinessConsumer.java',
  'PROCESS':'src/main/java/com/cpf/reference/edu/runtime/consumer/process/ProcessEduBusinessConsumer.java',
  'SPRING_BATCH':'src/main/java/com/cpf/reference/batch/operation/SpringBatchEduBusinessConsumer.java',
  'REFERENCE_GATEWAY':'src/main/java/com/cpf/reference/optional/gateway/runtime/ReferenceGatewayBusinessConsumer.java'}
 for t,rel in required.items():
  p=ref/rel
  if not p.is_file() or 'implements EduBusinessConsumer' not in p.read_text(encoding='utf-8'):fail('concrete adapter missing '+t)
 for rel in ('src/main/java/com/cpf/reference/edu/counterparty/api/ReferenceCounterpartyController.java','src/main/java/com/cpf/reference/edu/counterparty/application/ReferenceCounterpartyService.java','src/main/java/com/cpf/reference/edu/counterparty/persistence/JdbcReferenceCounterpartyStore.java','src/test/java/com/cpf/reference/edu/counterparty/application/ReferenceCounterpartyServiceTest.java'):
  if not (ref/rel).is_file():fail('counterparty implementation missing '+rel)
 config=(ref/'src/main/java/com/cpf/reference/edu/runtime/configuration/EduRuntimeConfiguration.java').read_text(encoding='utf-8')
 for cls in ('JdbcEduBusinessConsumer','JdbcQueryEduBusinessConsumer','FileEduBusinessConsumer','HttpEduBusinessConsumer','ProcessEduBusinessConsumer','OutboxEduBusinessConsumer'):
  if cls not in config:fail('mandatory adapter bean missing '+cls)
 print('[CPF][QA37][CONSUMER][PASS] bindings=135 distribution='+str(dict(counts))+' productAdapters=8 counterparty=REF-owned')
if __name__=='__main__':main()
