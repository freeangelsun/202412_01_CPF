#!/usr/bin/env python3
"""Generated Domain 멱등 원장과 3개 DB Vendor Template의 정적 정합성을 검증한다."""
from __future__ import annotations

import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1] if len(sys.argv) > 1 else '.').resolve()
generator = (root / 'cpf-tools/scripts/create-domain.ps1').read_text(encoding='utf-8')
errors: list[str] = []

required_generator_tokens = (
    'CPF_IDEMPOTENCY_RESULT_TYPE',
    '${FeatureClassPrefix}IdempotencyEntry',
    'sameIdempotencyKeyAndSameRequestReplaysResult',
    'sameIdempotencyKeyAndDifferentRequestIsRejected',
    'findIdempotency',
    'insertIdempotency',
    'requestHash("DELETE"',
    'commandPort.delete(id,c.expectedVersion(),x.tx(),x.idem(),x.seq(),x.actor())',
)
for token in required_generator_tokens:
    if token not in generator:
        errors.append(f'generator missing: {token}')

if re.search(r'findByIdempotencyKey', generator):
    errors.append('legacy business-row idempotency lookup remains')
if re.search(r'DeleteResult delete\([^)]*String transactionId,\s*long transactionSequence', generator):
    errors.append('delete command does not carry idempotencyKey')

for vendor in ('mariadb', 'postgresql', 'oracle'):
    base = root / f'cpf-tools/db/vendor/{vendor}/domain-template'
    mapper = base / 'runtime/mybatis/__MAPPER__.xml.template'
    xml_text = mapper.read_text(encoding='utf-8')
    xml_without_doctype = re.sub(r'<!DOCTYPE[^>]+>', '', xml_text, count=1)
    try:
        tree = ET.fromstring(xml_without_doctype)
    except ET.ParseError as exc:
        errors.append(f'{vendor} mapper XML: {exc}')
        continue
    ids = {node.attrib.get('id') for node in tree if node.attrib.get('id')}
    for statement in ('findById', 'findIdempotency', 'insertIdempotency', 'insert', 'updateWithVersion', 'logicalDeleteWithVersion'):
        if statement not in ids:
            errors.append(f'{vendor} mapper statement missing: {statement}')
    if '@CPF_IDEMPOTENCY_RESULT_TYPE@' not in xml_text:
        errors.append(f'{vendor} mapper idempotency result type missing')

    lifecycle = {
        'install': base / 'install/10_empty_install.sql.template',
        'migration': base / 'migration/V1____DOMAIN___domain.sql.template',
        'rollback': base / 'rollback/R1__remove___DOMAIN___domain.sql.template',
        'verify': base / 'verify/90_verify.sql.template',
    }
    for role, path in lifecycle.items():
        if not path.is_file():
            errors.append(f'{vendor} {role} template missing')
            continue
        text = path.read_text(encoding='utf-8')
        if '@CPF_TABLE_PREFIX@_sample_item_idem' not in text:
            errors.append(f'{vendor} {role} does not manage idempotency ledger')
        if role in ('install', 'migration', 'verify') and 'request_hash' not in text:
            errors.append(f'{vendor} {role} request_hash missing')
    install_text = lifecycle['install'].read_text(encoding='utf-8')
    if re.search(r'UNIQUE[^\n]*(?:sample_item_)?idem[^\n]*\(idempotency_key\)', install_text, re.I):
        errors.append(f'{vendor} business table still treats last idempotency key as global unique ledger')

if errors:
    for error in errors:
        print(f'[FAIL] {error}')
    print(f'GENERATOR_IDEMPOTENCY_TEMPLATE=FAIL errors={len(errors)}')
    raise SystemExit(1)

print('[PASS] Generator Query/Command/Delete idempotency contract')
print('[PASS] Local/DB adapter request-hash replay and conflict guards')
print('[PASS] MariaDB/PostgreSQL/Oracle mapper + install/migration/rollback/verify parity')
print('GENERATOR_IDEMPOTENCY_TEMPLATE=PASS vendors=3')
