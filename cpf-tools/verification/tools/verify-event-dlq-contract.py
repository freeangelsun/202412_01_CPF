#!/usr/bin/env python3
import argparse
import json
import pathlib

def parse_args():
    parser = argparse.ArgumentParser(description="Verify EVENT/DLQ approval and owner contract")
    parser.add_argument("root_positional", nargs="?", help="repository root (backward-compatible positional form)")
    parser.add_argument("--root", dest="root_option", help="repository root")
    args = parser.parse_args()
    return pathlib.Path(args.root_option or args.root_positional or ".").resolve()

root=parse_args()
op=(root/'cpf-starters/messaging/reliability/jdbc/src/main/java/com/cpf/messaging/reliability/api/jdbc/CpfBrokerReliabilityOperations.java').read_text(encoding='utf-8')
repo=(root/'cpf-starters/messaging/reliability/jdbc/src/main/java/com/cpf/messaging/reliability/api/jdbc/internal/JdbcCpfBrokerReliabilityRepository.java').read_text(encoding='utf-8')
controller=(root/'cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmReliabilityController.java').read_text(encoding='utf-8')
dto=(root/'cpf-admin/src/main/java/com/cpf/admin/opr/dto/AdmReliabilityActionRequest.java').read_text(encoding='utf-8')
spec=json.loads((root/'cpf-admin/frontend/openapi/cpf-openapi.json').read_text(encoding='utf-8'))
generated=(root/'cpf-admin/frontend/src/generated/cpf-api.ts').read_text(encoding='utf-8')
orval_generated=(root/'cpf-admin/frontend/src/generated/orval/cpf-api.ts').read_text(encoding='utf-8')
# Owner mutation must be atomic/CAS and require operator+reason.
assert '@Transactional(transactionManager="cpfTransactionManager")\n    public ChangeResult requestDlqReplay' in op
assert 'required(operatorId,"operatorId")' in op and 'required(reason,"reason")' in op
assert "replay_status IN ('WAITING','FAILED')" in op and "outbox_status IN ('FAILED','UNKNOWN')" in op
assert 'if(outbox!=1)' in op and 'if(dlq!=1)' in op
# The historical low-level replay SPI remains deliberately fail-closed.
assert 'public CpfBrokerResult replay(String messageId)' in repo
assert 'DLQ replay requires an approved owner command' in repo
assert 'public List<CpfBrokerResult> replayRange' in repo
assert 'DLQ range replay requires per-target approved snapshots' in repo
assert 'limit must be between 1 and 5000' in repo
# HTTP consumer requests approval; it does not mutate the DLQ directly.
assert '@Valid @RequestBody AdmReliabilityActionRequest request' in controller
assert 'auditLogService.requireReason(request.reason())' in controller
assert 'requestDlqReplayApproval(' in controller
assert '@NotBlank @Size(max = 500) String reason' in dto
operation=spec['paths']['/adm/api/reliability/broker/dlq/{messageId}/replay']['post']
assert operation['requestBody']['required'] is True
assert operation['requestBody']['content']['application/json']['schema']['$ref'].endswith('AdmReliabilityActionRequest')
schema=spec['components']['schemas']['AdmReliabilityActionRequest']
assert 'reason' in schema.get('required',[]) and schema['properties']['reason'].get('minLength',0) >= 1
assert 'function requestAdmBrokerDlqReplay' in generated
assert 'orvalRequestAdmBrokerDlqReplay(options.path["messageId"], options.data' in generated
assert 'export const requestAdmBrokerDlqReplay' in orval_generated
orval_start=orval_generated.index('export const requestAdmBrokerDlqReplay')
orval_end=orval_generated.find('// CPF PRE-RUNTIME FALLBACK END requestAdmBrokerDlqReplay', orval_start)
if orval_end < 0:
    orval_end=orval_generated.find('export const ', orval_start + 1)
if orval_end < 0:
    orval_end=len(orval_generated)
orval_block=orval_generated[orval_start:orval_end]
assert "method: 'POST'" in orval_block and 'data,' in orval_block
print('PASS EVENT-DLQ approval/owner contract')
