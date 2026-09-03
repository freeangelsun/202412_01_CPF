#!/usr/bin/env python3
from __future__ import annotations

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass
import argparse,re
from pathlib import Path

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');a=ap.parse_args();r=Path(a.root).resolve();fails=[]
 manifest=r/'cpf-docs/deliverables/DELETE_MANIFEST.csv'; deleted=set()
 if manifest.is_file(): deleted={line.strip().replace('\\','/').strip('/') for line in manifest.read_text(encoding='utf-8-sig').splitlines() if line.strip() and not line.lstrip().startswith('#')}
 seen={}
 allowed_unsupported={
  'cpf-starters/messaging/kafka/src/main/java/com/cpf/messaging/kafka/KafkaCpfBrokerBridgeAdapter.java',
  'cpf-batch/api/src/main/java/com/cpf/batch/spi/BatchExecutionLedgerPort.java',
  'cpf-starters/security/secret/src/main/java/com/cpf/security/secret/CpfKeyManagementService.java',
  'cpf-starters/platform-operations/observability/src/main/java/com/cpf/platform/operations/observability/api/remotelog/CpfRemoteLogArtifactPort.java',
  'cpf-starters/platform-operations/src/main/java/com/cpf/platform/operations/api/reliability/CpfReliabilityOperationsPort.java',
  'cpf-gateway/src/main/java/com/cpf/gateway/api/CpfGatewayRateLimitCounterPort.java',
 }
 marker_allowlist={
  'cpf-tools/db/verify_canonical_vendor_render.py',
  'cpf-tools/runtime/tools/runtime-common.ps1',
  'cpf-tools/governance/tools/verify-cpf-requirement-traceability.py',
 }
 for p in r.rglob('*'):
  if not p.is_file():continue
  rel=p.relative_to(r).as_posix()
  # cpf-release/ 는 릴리즈 생성 산출물이며 canonical Source Identity(cpf-source-state.py 의
  # GENERATED_PARTS)도 제품 Source 로 계산하지 않는다. Open Git 릴리즈는 cpf-education 등을
  # 의도적으로 projection 하므로 함께 스캔하면 원본과 사본이 DUPLICATE_FQCN 으로 잡히고,
  # 릴리즈 생성 여부에 따라 같은 Source 가 PASS/FAIL 로 갈리는 비결정 Gate 가 된다.
  if rel in deleted or rel.startswith('cpf-tools/verification/') or rel.startswith('cpf-release/') or rel.startswith('cpf-docs/governance/development-harness/evidence/platform/current/generated/') or any(x in p.parts for x in ('build','__pycache__','.git','node_modules','dist')):continue
  if '/src/main/java/resources/' in '/'+rel:fails.append('JAVA_RESOURCES_TREE:'+rel)
  if p.suffix.lower() in {'.java','.kt','.ts','.tsx','.vue','.py','.ps1'}:
   t=p.read_text(encoding='utf-8',errors='ignore')
   if '/src/test/' not in '/'+rel and '/tests/' not in '/'+rel and rel not in marker_allowlist and re.search(r'\b(TODO|FIXME|NOT_IMPLEMENTED)\b|not implemented|placeholder implementation',t,re.I):fails.append('PARTIAL_MARKER:'+rel)
   if '/src/test/' not in '/'+rel and rel not in allowed_unsupported and 'throw new UnsupportedOperationException' in t:fails.append('UNSUPPORTED_STUB:'+rel)
   if p.suffix.lower()=='.java' and '/src/test/' not in '/'+rel:
    pm=re.search(r'\bpackage\s+([\w.]+)\s*;',t); cm=re.search(r'\b(?:public\s+)?(?:final\s+|abstract\s+)?(?:class|interface|record|enum|@interface)\s+(\w+)',t)
    if pm and cm:
     fq=pm.group(1)+'.'+cm.group(1)
     if fq in seen:fails.append('DUPLICATE_FQCN:'+fq+':'+seen[fq]+':'+rel)
     else:seen[fq]=rel
 if fails:
  print('CPF_NO_PARTIAL_IMPLEMENTATION=FAIL errors='+str(len(fails)));[print(x) for x in fails[:100]];return 1
 print(f'CPF_NO_PARTIAL_IMPLEMENTATION=PASS activeJavaFqcn={len(seen)} todo=0 unsupportedStub=0 duplicateFqcn=0 malformedResourceTree=0')
 return 0
if __name__=='__main__':raise SystemExit(main())
