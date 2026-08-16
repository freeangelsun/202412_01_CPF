#!/usr/bin/env python3
from pathlib import Path
import sys

root=Path(sys.argv[1] if len(sys.argv)>1 else '.').resolve()
fail=[]

ctx=root/'cpf-core/src/main/java/com/cpf/core/api/context/CpfContext.java'
if not ctx.is_file():
    fail.append('CPF_CONTEXT_MISSING')
else:
    text=ctx.read_text(errors='ignore')
    if 'return new CpfContext(transaction,' not in text:
        fail.append('CHILD_TRANSACTION_REUSE_MISSING')
    for token in ['UUID.randomUUID','System.currentTimeMillis','Instant.now()']:
        if token in text:
            fail.append('CORE_GENERATES_RUNTIME_VALUE:'+token)

# 최신 정본: Core는 transactionId 공개 의미/Generator Contract를 소유할 수 있고 실제 생성 구현은 Foundation/Base가 소유한다.
core_contract=root/'cpf-core/src/main/java/com/cpf/core/api/transaction/CpfTransactionIdGenerator.java'
core_ids=root/'cpf-core/src/main/java/com/cpf/core/api/transaction/CpfTransactionIds.java'
if not core_contract.is_file(): fail.append('TXID_CORE_CONTRACT_MISSING')
if not core_ids.is_file(): fail.append('TXID_CORE_VALIDATOR_MISSING')
else:
    text=core_ids.read_text(errors='ignore')
    if '34자리' not in text or 'isCanonical' not in text:
        fail.append('TXID_CORE_34_CONTRACT_MISSING')

base_impl=root/'cpf-starters/base/runtime/src/main/java/com/cpf/foundation/id/DefaultCpfTransactionIdGenerator.java'
if not base_impl.is_file():
    fail.append('TXID_DEFAULT_IMPLEMENTATION_MISSING_BASE_RUNTIME')
else:
    text=base_impl.read_text(errors='ignore')
    for token in ['CpfTransactionIds.requireCanonical','yyyyMMddHHmmssSSS','SEQUENCE_DIGITS = 7','cpf.framework.was-id']:
        if token not in text:
            fail.append('TXID_BASE_IMPLEMENTATION_WITNESS_MISSING:'+token)
    if 'UUID.randomUUID' in text:
        fail.append('TXID_BASE_UUID_DEFAULT_FORBIDDEN')

auto=root/'cpf-starters/base/runtime/src/main/java/com/cpf/starter/runtime/CpfStarterAutoConfiguration.java'
if not auto.is_file():
    fail.append('TXID_BASE_AUTOCONFIG_MISSING')
else:
    text=auto.read_text(errors='ignore')
    if 'DefaultCpfTransactionIdGenerator' not in text:
        fail.append('TXID_BASE_AUTOCONFIG_NOT_CANONICAL')
    if '"TX-" + UUID.randomUUID()' in text:
        fail.append('TXID_LEGACY_UUID_BEAN_REMAINS')

scheduler=root/'cpf-batch/scheduler/src/main/java/com/cpf/batch/scheduler/SchedulerDispatchService.java'
if scheduler.is_file() and '"BAT-TX-" + java.util.UUID.randomUUID()' in scheduler.read_text(errors='ignore'):
    fail.append('TXID_BATCH_UUID_FALLBACK_REMAINS')

web=root/'cpf-starters/web/src/main/java/com/cpf/web/context/CpfHttpInboundContextAdapter.java'
if not web.is_file():
    fail.append('TXID_WEB_INGRESS_MISSING')
else:
    text=web.read_text(errors='ignore')
    for token in ['CpfTransactionIds.isCanonical','TRUSTED_INTERNAL','rawInboundTransactionId','correlation = rawInboundTransactionId']:
        if token not in text:
            fail.append('TXID_WEB_TRUST_WITNESS_MISSING:'+token)


# 실제 transactionId 변수/인수에 UUID를 직접 넣는 우회 경로를 전역에서 차단한다.
for java in root.rglob('*.java'):
    rel=java.relative_to(root).as_posix()
    if '/build/' in rel or rel.startswith('cpf-tools/verification/'):
        continue
    text=java.read_text(errors='ignore')
    for line_no,line in enumerate(text.splitlines(),1):
        compact=''.join(line.split())
        if 'transactionId=' in compact and 'UUID.randomUUID()' in compact:
            fail.append(f'TXID_UUID_ASSIGNMENT_FORBIDDEN:{rel}:{line_no}')

gateway=root/'cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgPrimaryHandler.java'
if gateway.is_file():
    text=gateway.read_text(errors='ignore')
    if 'newCanonicalTransactionId()' not in text or 'CpfTransactionIds.requireCanonical(transactionIds.newTransactionId())' not in text:
        fail.append('TXID_GATEWAY_DENIED_CANONICAL_MISSING')

file_gateway=root/'cpf-starters/file/sftp/src/main/java/com/cpf/file/common/filetransfer/CpfFileExchangeGateway.java'
if file_gateway.is_file():
    text=file_gateway.read_text(errors='ignore')
    if 'CpfTransactionIds.requireCanonical(transactionIds.newTransactionId())' not in text:
        fail.append('TXID_FILE_EXCHANGE_CANONICAL_MISSING')

sftp=root/'cpf-starters/file/sftp/src/main/java/com/cpf/file/sftp/CpfSftpClient.java'
if sftp.is_file() and 'CpfTransactionIds.isCanonical(transactionId)' not in sftp.read_text(errors='ignore'):
    fail.append('TXID_SFTP_BOUNDARY_CANONICAL_VALIDATION_MISSING')

reconcile=root/'cpf-starters/integration/resilience/src/main/java/com/cpf/platform/operations/reconciliation/CpfReconciliationWorker.java'
if reconcile.is_file():
    text=reconcile.read_text(errors='ignore')
    if 'CpfTransactionIds.requireCanonical(record.transactionId())' not in text or '"reconciliation-" + sha256(correlationSeed)' in text:
        fail.append('TXID_RECONCILIATION_MUST_REUSE_ORIGINAL')

# retry child context may create execution ids, but must not generate a new transaction id.
res='\n'.join(p.read_text(errors='ignore') for p in (root/'cpf-starters/integration').rglob('*.java')) if (root/'cpf-starters/integration').exists() else ''
if 'newTransactionId' in res:
    fail.append('RETRY_NEW_TRANSACTION_ID_SUSPECT')

if fail:
    print('CPF_TXID_CONTRACT=FAIL')
    print('\n'.join(fail))
    sys.exit(1)
print('CPF_TXID_CONTRACT=PASS core=contract foundation=default-implementation canonical34=true trustBoundary=true retryNewTxId=0')
