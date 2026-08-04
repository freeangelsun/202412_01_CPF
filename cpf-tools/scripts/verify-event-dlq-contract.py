#!/usr/bin/env python3
import json, pathlib, re, sys
root=pathlib.Path(sys.argv[1])
op=(root/'cpf-starters/messaging/reliability-jdbc/src/main/java/com/cpf/starter/messaging/reliability/CpfBrokerReliabilityOperations.java').read_text()
repo=(root/'cpf-starters/messaging/reliability-jdbc/src/main/java/com/cpf/core/common/broker/JdbcCpfBrokerReliabilityRepository.java').read_text()
controller=(root/'cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmReliabilityController.java').read_text()
spec=json.loads((root/'cpf-admin/frontend/openapi/cpf-openapi.json').read_text())
orval=(root/'cpf-admin/frontend/src/generated/orval/cpf-api.ts').read_text()
assert '@Transactional\n    public CpfBrokerResult replay' in op
assert '@Transactional\n    public List<CpfBrokerResult> replayRange' in op
assert 'limit must be between 1 and ' in op
assert 'SELECT COUNT(*)' in repo and repo.index('SELECT COUNT(*)') < repo.index("SET replay_status = 'REQUESTED'")
assert '@RequestBody AdmReliabilityActionRequest request' in controller
operation=spec['paths']['/adm/api/reliability/broker/dlq/{messageId}/replay']['post']
assert operation['requestBody']['required'] is True
assert operation['requestBody']['content']['application/json']['schema']['$ref'].endswith('AdmReliabilityActionRequest')
schema=spec['components']['schemas']['AdmReliabilityActionRequest']
assert 'reason' in schema['required'] and schema['properties']['reason']['minLength']==1
assert 'admReliabilityActionRequest: AdmReliabilityActionRequest' in orval
assert 'data: admReliabilityActionRequest' in orval
print('PASS EVENT-DLQ contract')
