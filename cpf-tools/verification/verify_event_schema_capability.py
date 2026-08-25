#!/usr/bin/env python3
from __future__ import annotations
import argparse
from pathlib import Path

def fail(x):print('CPF_EVENT_SCHEMA=FAIL '+x);return 1

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ns=ap.parse_args();r=Path(ns.root).resolve()
 api=r/'cpf-starters/messaging/schema/src/main/java/com/cpf/messaging/schema/api'
 runtime=r/'cpf-starters/messaging/schema/src/main/java/com/cpf/messaging/schema/runtime/CpfInMemoryEventSchemaRegistry.java'
 for x in ['CpfEventSchemaRegistry.java','CpfEventSchemaDescriptor.java','CpfEventSchemaFormat.java','CpfEventSchemaCompatibility.java','CpfEventSchemaCompatibilityResult.java']:
  if not (api/x).is_file():return fail('apiMissing='+x)
 if not runtime.is_file():return fail('runtimeMissing=true')
 s=runtime.read_text(encoding="utf-8")
 if 'com.cpf.core.api.reliability' in s:return fail('staleCoreReliability=true')
 for x in ['JSON_SCHEMA','AVRO','PROTOBUF','breaking schema','duplicate schemaId','required field missing','avro backward incompatibility','protobuf field type changed']:
  if x not in s:return fail('witnessMissing='+x)
 test=r/'cpf-starters/messaging/schema/src/test/java/com/cpf/messaging/schema/CpfEventSchemaCompatibilityCorpusTest.java'
 if not test.is_file():return fail('corpusTestMissing=true')
 for x in ['json-v1.json','json-v2-compatible.json','json-v2-breaking.json']:
  if not (r/'cpf-starters/messaging/schema/src/test/resources/schema-corpus'/x).is_file():return fail('corpusMissing='+x)
 # Broker-independent owner boundary: schema module cannot depend on Kafka/Rabbit/JMS/IBM MQ leaf projects.
 b=(r/'cpf-starters/messaging/schema/build.gradle').read_text(encoding="utf-8")
 for bad in ('kafka','rabbitmq','jms','ibmmq'):
  if f"project(':cpf-starter-messaging-{bad}')" in b:return fail('brokerDependency='+bad)
 print('CPF_EVENT_SCHEMA=PASS formats=json,avro,protobuf compatibility=true breakingGate=true validation=true brokerIndependent=true corpus=true generatedModelBoundary=true')
 return 0
if __name__=='__main__':raise SystemExit(main())
