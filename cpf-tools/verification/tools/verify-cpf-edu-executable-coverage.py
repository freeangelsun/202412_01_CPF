#!/usr/bin/env python3
"""Canonical CPF Education 135 executable catalog gate.

현재 135개 Education Capability마다 실제 Source/Test/Resource/공개 Consumer EntryPoint가
물리 파일로 연결되는지 검증합니다. 외부 Runtime을 실행하지 않은 verificationStatus=미검증은
PASS로 가장하지 않고 별도 runtimePending 수치로 보고합니다.
"""
from __future__ import annotations
import argparse,json,re
from pathlib import Path
ALLOWED={'완료','미검증'}
ID=re.compile(r'^EDU-(DEV|BAT|ADM|OPS|BZA|GW)-\d{2}$')
FORBIDDEN=('cpf-reference','com.cpf.reference','/scenario/')
def load(p:Path): return json.loads(p.read_text(encoding='utf-8'))
def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');a=ap.parse_args();r=Path(a.root).resolve();fail=[]
 cp=r/'cpf-tools/governance/cpf-edu-executable-catalog.json'; mp=r/'cpf-education/src/main/resources/verification/manual-135-catalog.json'
 try:c=load(cp);m=load(mp)
 except Exception as e: print('[FAIL] catalog unreadable:',e);return 1
 cf=c.get('features') or []; mf=m.get('features') or []
 if c.get('featureCount')!=135 or len(cf)!=135:fail.append(f'governance catalog must contain 135 features: {len(cf)}')
 if m.get('featureCount')!=135 or len(mf)!=135:fail.append(f'manual catalog must contain 135 features: {len(mf)}')
 ids=[x.get('requirementId') for x in cf]; mids=[x.get('requirementId') for x in mf]
 if len(set(ids))!=135 or any(not ID.fullmatch(str(x or '')) for x in ids):fail.append('requirementId uniqueness/format violation')
 if ids!=mids:fail.append('governance/manual catalog requirement order mismatch')
 manual={x.get('requirementId'):x for x in mf}; runtime_pending=0
 for x in cf:
  rid=x.get('requirementId') or '<missing>'; y=manual.get(rid) or {}
  if x.get('developmentStatus')!='완료' or y.get('developmentStatus')!='완료':fail.append(f'{rid}: developmentStatus must be 완료')
  if x.get('verificationStatus') not in ALLOWED or y.get('verificationStatus') not in ALLOWED:fail.append(f'{rid}: invalid verificationStatus')
  if x.get('verificationStatus')!='완료' or y.get('verificationStatus')!='완료':runtime_pending+=1
  paths=[x.get('sourcePath'),x.get('resourceContract')]+list(x.get('tests') or [])
  if len(x.get('tests') or [])<5:fail.append(f'{rid}: normal/error/recovery/concurrency/integration test anchors incomplete')
  binding=x.get('consumerBinding') or {}
  for raw in paths:
   if not raw or not (r/raw).is_file():fail.append(f'{rid}: missing Source/Test/Resource anchor {raw}')
   if raw and any(t in raw for t in FORBIDDEN):fail.append(f'{rid}: retired path in catalog {raw}')
  for key in ('type','ownerModule','entryPoint','operation','publicContract','runtimeCommand'):
   if not str(binding.get(key) or '').strip():fail.append(f'{rid}: consumerBinding.{key} missing')
  if binding.get('ownerModule')!='cpf-education':fail.append(f'{rid}: ownerModule must be cpf-education')
  if rid not in str(binding.get('runtimeCommand') or ''):fail.append(f'{rid}: runtimeCommand must address its capability id')
  if binding.get('type')=='PROCESS' and not (r/str(binding.get('entryPoint'))).is_file():fail.append(f'{rid}: PROCESS entryPoint file missing')
  if binding.get('type') in {'HTTP','FILE','SPRING_BATCH','REFERENCE_GATEWAY'} and not str(binding.get('configurationKey') or '').strip():fail.append(f'{rid}: {binding.get("type")} configurationKey missing')
  # 모든 Handler가 Spring contributor에 실제 등록되어 Generic Controller 실행 경로에 연결되어야 합니다.
  hcls=str(y.get('handlerClass') or '').rsplit('.',1)[-1]
  contributors=list((r/'cpf-education/src/main/java/com/cpf/education').rglob('*Contributor.java'))
  if not hcls or not any(hcls in q.read_text(encoding='utf-8',errors='ignore') for q in contributors):fail.append(f'{rid}: Handler is not registered by an actual EduCapabilityContributor')
  # manual catalog must describe actual same Source/Handler contract.
  for key in ('sourcePath','resourceContract','handlerClass','implementationPackage'):
   if not str(y.get(key) or '').strip():fail.append(f'{rid}: manual {key} missing')
 for e in sorted(set(fail)):print('[FAIL]',e)
 if fail:return 1
 print(f'[PASS] CPF EDU executable coverage features=135 source=135 tests=675 resources=135 entryPoints=135 runtimePending={runtime_pending}')
 return 0
if __name__=='__main__':raise SystemExit(main())
