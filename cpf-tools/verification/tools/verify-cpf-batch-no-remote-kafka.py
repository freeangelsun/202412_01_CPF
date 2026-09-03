#!/usr/bin/env python3
"""Fail-closed gate: CPF Batch must not contain Kafka/Broker based Remote Execution surface.

Historical V87/R87 migration/rollback bytes are intentionally preserved.  Current product
source, current schema/query packs, runtime harness, publication/catalog, generator/sample/EDU
must not expose the retired Remote Execution API/SPI/configuration.
"""
from __future__ import annotations

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass
import argparse, json, re, sys
from pathlib import Path

FORBIDDEN = (
    "cpf-batch/remote-kafka",
    ":runtime:batch:remote-kafka",
    "cpf.batch.remote.",
    "REMOTE_PARTITION",
    "REMOTE_CHUNK",
    "REMOTE_STEP",
    "CpfBatchRemoteWorkerConfiguration",
    "CpfSynchronousWorkerChannel",
    "CpfDisabledRemoteChannel",
    "CpfBatchDynamicManagerFlowLifecycle",
    "CpfBatchContextCarrier",
    "CpfBatchRemoteMessageLedger",
    "JdbcCpfBatchRemoteMessageLedger",
    "CpfRemoteChunk",
    "CpfRemoteStep",
    "CpfRemoteWorker",
    "BatchRemoteDiagnosticController",
    "RemoteMessageReconciliationController",
    "BAT_REMOTE_MESSAGE_LEDGER",
)
BATCH_KAFKA_PATTERNS = (
    re.compile(r"org[.]springframework[.]kafka", re.I),
    re.compile(r"org[.]apache[.]kafka", re.I),
    re.compile(r"spring[.-]kafka", re.I),
    re.compile(r"kafka-clients", re.I),
    re.compile(r"spring[.]kafka", re.I),
    re.compile(r"cpf[.]messaging[.]kafka", re.I),
)
TEXT_SUFFIXES={'.java','.kt','.groovy','.gradle','.kts','.properties','.yml','.yaml','.json','.csv','.md','.ps1','.py','.xml','.txt','.template','.sql'}

# These files intentionally name retired symbols only to prove their absence.  They are not
# runtime/product consumers and must not be mistaken for a resurrected Remote Execution surface.
NEGATIVE_ASSERTION_ALLOWLIST = {
    'cpf-tools/verification/tools/verify-cpf-batch-no-remote-kafka.py': set(FORBIDDEN),
    'cpf-tools/verification/tools/verify-cpf-batch-unknown-reconciliation.py': {
        'CpfBatchRemoteMessageLedger','JdbcCpfBatchRemoteMessageLedger','RemoteMessageReconciliationController'
    },
    'cpf-batch/testkit/src/test/java/com/cpf/batch/qa32/Qa32RuntimeEnvironmentContractTest.java': {'BAT_REMOTE_MESSAGE_LEDGER'},
    'cpf-tools/runtime/tools/smoke-bat-two-worker-runtime.ps1': {'BAT_REMOTE_MESSAGE_LEDGER'},
    'cpf-tools/runtime/tools/tests/test_bat_two_worker_runtime_shell_contract.py': {'BAT_REMOTE_MESSAGE_LEDGER'},
}

# Current-source scopes. Historical work/evidence is not a product consumer and is excluded;
# governance is checked separately for the explicit retirement requirement.
SCOPES=(
    'settings.gradle','build.gradle','cpf-batch','cpf-tools/release','cpf-tools/runtime',
    'cpf-tools/verification','cpf-tools/db/canonical','cpf-tools/db/metadata',
    'cpf-tools/db/runtime-template','cpf-tools/db/generated/current','cpf-tools/db/vendor',
    'cpf-tools/generator','cpf-education','cpf-member','cpf-external',
)

def active_file(root:Path,p:Path)->bool:
    rel=p.relative_to(root).as_posix()
    if '/build/' in rel or '/.gradle/' in rel or '/__pycache__/' in rel: return False
    # released DB history must stay byte-immutable, so V87/R87 may contain retired names.
    if rel.startswith('cpf-tools/db/vendor/') and ('/migration/' in rel or '/rollback/' in rel): return False
    if rel.startswith('cpf-docs/work/evidence/') or rel.startswith('cpf-docs/work/archive/'): return False
    return p.is_file() and (p.suffix.lower() in TEXT_SUFFIXES or p.name in {'settings.gradle','build.gradle'})

def collect(root:Path):
    paths=[]
    for scope in SCOPES:
        p=root/scope
        if p.is_file(): paths.append(p)
        elif p.is_dir(): paths.extend(x for x in p.rglob('*') if active_file(root,x))
    # de-duplicate resolved paths while preserving order
    seen=set(); out=[]
    for p in paths:
        r=p.resolve()
        if r in seen or not active_file(root,p): continue
        seen.add(r); out.append(p)
    return out

def verify(root:Path)->dict:
    errors=[]; scanned=0
    for p in collect(root):
        rel=p.relative_to(root).as_posix()
        try: text=p.read_text(encoding='utf-8-sig')
        except UnicodeDecodeError: continue
        scanned+=1
        allowed_negative = NEGATIVE_ASSERTION_ALLOWLIST.get(rel, set())
        for token in FORBIDDEN:
            if token in text:
                # migration intent and explicit negative-assertion gates may name the retired surface only to prove absence.
                if rel=='cpf-tools/db/canonical/migration-intent-catalog.json' and token=='BAT_REMOTE_MESSAGE_LEDGER': continue
                if token in allowed_negative: continue
                errors.append(f'{rel}:retired-surface:{token}')
        if rel.startswith('cpf-batch/'):
            for pattern in BATCH_KAFKA_PATTERNS:
                if pattern.search(text): errors.append(f'{rel}:batch-kafka-dependency:{pattern.pattern}')
    # physical module and current DB/query surface must be absent.
    if (root/'cpf-batch/remote-kafka').exists(): errors.append('cpf-batch/remote-kafka:physical-module-still-exists')
    topology=(root/'cpf-batch/api/src/main/java/com/cpf/batch/api/BatchExecutionTopology.java').read_text(encoding='utf-8')
    if not all(x in topology for x in ('LOCAL','PARALLEL_STEPS','LOCAL_PARTITION')): errors.append('BatchExecutionTopology:general-topologies-missing')
    # Historical migration/rollback is required to remain, and V140/R140 must provide upgrade/recovery.
    history={
      'mariadb':('migration/flyway/V87__batch_remote_message_ledger.sql','rollback/R87__batch_remote_message_ledger.sql','migration/flyway/V140__remove_batch_remote_kafka_execution.sql','rollback/R140__remove_batch_remote_kafka_execution.sql'),
      'postgresql':('migration/flyway/batDB/V87__batch_remote_message_ledger.sql','rollback/batDB/R87__batch_remote_message_ledger.sql','migration/flyway/cpfDB/V140__remove_batch_remote_kafka_execution.sql','rollback/cpfDB/R140__remove_batch_remote_kafka_execution.sql'),
      'oracle':('migration/flyway/batDB/V87__batch_remote_message_ledger.sql','rollback/batDB/R87__batch_remote_message_ledger.sql','migration/flyway/cpfDB/V140__remove_batch_remote_kafka_execution.sql','rollback/cpfDB/R140__remove_batch_remote_kafka_execution.sql'),
    }
    for vendor,rels in history.items():
        for rel in rels:
            if not (root/f'cpf-tools/db/vendor/{vendor}'/rel).is_file(): errors.append(f'{vendor}:{rel}:missing')
    # Operational orchestration must also keep Batch Kafka-free. Generic Messaging Kafka
    # reliability is allowed only as its own independent stage/target.
    env_script = root/'cpf-tools/environment/docker-development-test/cpf-env.ps1'
    if env_script.is_file():
        env_text = env_script.read_text(encoding='utf-8-sig')
        for target in ('batch-mariadb', 'batch-postgresql', 'batch-oracle'):
            match = re.search(r'\"' + re.escape(target) + r'\"\s*=\s*@\(([^)]*)\)', env_text)
            if not match:
                errors.append(f'{env_script.relative_to(root).as_posix()}:missing-batch-target:{target}')
            elif re.search(r'kafka', match.group(1), re.I):
                errors.append(f'{env_script.relative_to(root).as_posix()}:batch-target-kafka-coupling:{target}')
    else:
        errors.append('cpf-tools/environment/docker-development-test/cpf-env.ps1:missing')

    full_runtime = root/'cpf-tools/verification/tools/run-cpf-local-full-validation.ps1'
    if full_runtime.is_file():
        runtime_text = full_runtime.read_text(encoding='utf-8-sig')
        if 'MESSAGING_KAFKA_RELIABILITY' not in runtime_text:
            errors.append(f'{full_runtime.relative_to(root).as_posix()}:independent-messaging-kafka-stage-missing')
        batch_start = runtime_text.find('$batchDbEnv=')
        batch_end = runtime_text.find("}else{Skip-CpfStage 'RUNTIME_DOCKER_CLOSURE'", batch_start)
        if batch_start < 0 or batch_end < 0:
            errors.append(f'{full_runtime.relative_to(root).as_posix()}:batch-two-worker-stage-missing')
        else:
            window = runtime_text[batch_start:batch_end]
            if re.search(r'Start-CpfDockerTarget\s+[\'"]kafka[\'"]|batchKafkaState|DOCKER_kafka_(?:START|READINESS)', window, re.I):
                errors.append(f'{full_runtime.relative_to(root).as_posix()}:batch-stage-kafka-coupling')
    else:
        errors.append('cpf-tools/verification/tools/run-cpf-local-full-validation.ps1:missing')

    # Common messaging Kafka provider may remain, but Batch must not own or depend on it.
    common_kafka=root/'cpf-starters/messaging/kafka'
    if not common_kafka.is_dir(): errors.append('cpf-starters/messaging/kafka:independent-common-provider-missing')
    # Governance must encode the non-regression steering.
    gov=(root/'cpf-docs/governance/development-harness/product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md')
    if gov.is_file():
        g=gov.read_text(encoding='utf-8-sig')
        for token in ('BAT-NO-REMOTE-KAFKA','새 Remote Transport','일반 Batch','Center-Cut'):
            if token not in g: errors.append(f'governance:missing:{token}')
    else: errors.append('governance:missing')
    return {'status':'PASS' if not errors else 'FAIL','scannedFiles':scanned,'errors':errors}

def main()->int:
    ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--json-output');a=ap.parse_args()
    result=verify(Path(a.root).resolve())
    if a.json_output:
        p=Path(a.json_output);p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(result,ensure_ascii=False,indent=2))
    return 0 if result['status']=='PASS' else 1
if __name__=='__main__': raise SystemExit(main())
