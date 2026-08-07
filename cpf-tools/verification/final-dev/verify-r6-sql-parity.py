#!/usr/bin/env python3
from pathlib import Path
import json,sys
R=Path(sys.argv[1]).resolve() if len(sys.argv)>1 else Path(__file__).resolve().parents[3]
vendors=('mariadb','postgresql','oracle')
PACKS={
 'approvalIntegrityR6Pack':(
   ('source/19_approval_integrity_r6.sql','install/08_approval_integrity_r6.sql','migration/V104__approval_integrity_r6.sql','rollback/R104__approval_integrity_r6.sql','verify/104_verify_approval_integrity_r6.sql','runtime/adm/approval_integrity_queries.sql'),
   ('ADM_APPROVAL_POLICY_HISTORY','POLICY_CODE','POLICY_VERSION','CHANGE_REASON','BEFORE_HASH','AFTER_HASH','OPERATOR_ID')),
 'approvalRuntimeHardeningR6Pack':(
   ('source/20_approval_runtime_hardening_r6.sql','install/09_approval_runtime_hardening_r6.sql','migration/V105__approval_runtime_hardening_r6.sql','rollback/R105__approval_runtime_hardening_r6.sql','verify/105_verify_approval_runtime_hardening_r6.sql','runtime/adm/approval_runtime_hardening_queries.sql'),
   ('ADM_APPROVAL_EXECUTION','LEASE_OWNER','LEASE_EXPIRES_AT','FENCE_TOKEN','ADM_APPROVAL_CAPABILITY_NONCE','ADM_APPROVAL_POLICY_LOCK')),
 'dataQualityPersistenceR6Pack':(
   ('source/21_data_quality_persistence_r6.sql','install/10_data_quality_persistence_r6.sql','migration/V106__data_quality_persistence_r6.sql','rollback/R106__data_quality_persistence_r6.sql','verify/106_verify_data_quality_persistence_r6.sql','runtime/integration-closure/data_quality_persistent_queries.sql'),
   ('CPF_DATA_QUALITY_RULE','PARAMETERS_PAYLOAD','CPF_DATA_QUALITY_QUARANTINE','VIOLATION_PAYLOAD','CPF_DATA_QUALITY_OPERATION','COMMAND_FINGERPRINT'))
}
errors=[]
for pack_name,(roles,tokens) in PACKS.items():
    normalized={}
    for vendor in vendors:
        root=R/'cpf-tools/db/vendor'/vendor
        pack=root/'pack.json'
        try: data=json.loads(pack.read_text(encoding='utf-8'))
        except Exception as e: errors.append(f'{vendor}: invalid pack.json: {e}'); continue
        entry=data.get(pack_name)
        if not isinstance(entry,dict) or entry.get('officialVendor') is not True:
            errors.append(f'{vendor}: {pack_name} missing/invalid'); continue
        paths=[entry.get('canonicalSource'),entry.get('freshInstall'),entry.get('migration'),entry.get('rollback'),entry.get('verify'),entry.get('runtimeQueries')]
        if sorted(paths)!=sorted(roles): errors.append(f'{vendor}: {pack_name} paths differ expected={roles} actual={paths}')
        normalized[vendor]={}
        for role in roles:
            p=root/role
            if not p.is_file(): errors.append(f'{vendor}: missing {role}'); continue
            raw=p.read_text(encoding='utf-8',errors='replace')
            if not raw.strip(): errors.append(f'{vendor}: empty {role}')
            upper=raw.upper()
            normalized[vendor][role]={t for t in tokens if t in upper}
            if role.startswith(('source/','install/','migration/')):
                for token in tokens:
                    if token not in upper: errors.append(f'{vendor}: {role} missing {token}')
            if role.startswith('rollback/') and not any(x in upper for x in ('DROP','ALTER TABLE')): errors.append(f'{vendor}: rollback has no DROP/ALTER')
            if role.startswith('verify/') and not any(x in upper for x in ('RAISE','SIGNAL','THROW','COUNT')):
                errors.append(f'{vendor}: verify has no assertion/query signal')
        if pack_name in ('approvalRuntimeHardeningR6Pack','dataQualityPersistenceR6Pack'):
            aggregate=root/'install/00_empty_install.sql'
            if not aggregate.is_file(): errors.append(f'{vendor}: missing install/00_empty_install.sql')
            else:
                upper=aggregate.read_text(encoding='utf-8',errors='replace').upper()
                for token in tokens:
                    if token not in upper: errors.append(f'{vendor}: install/00_empty_install.sql missing {token}')
    for role in roles:
        sets=[normalized.get(v,{}).get(role,set()) for v in vendors]
        if all(sets) and any(s!=sets[0] for s in sets[1:]):
            errors.append(f'{pack_name} DB3 semantic token parity mismatch: {role}: {sets}')

# V105 overlap protection must be DB-enforced and concurrency-safe, not only application pre-checks.
v105={v: R/'cpf-tools/db/vendor'/v/'migration/V105__approval_runtime_hardening_r6.sql' for v in vendors}
for vendor,p in v105.items():
    if not p.is_file(): errors.append(f'{vendor}: missing V105 migration'); continue
    u=p.read_text(encoding='utf-8',errors='replace').upper()
    if 'ADM_APPROVAL_POLICY_LOCK' not in u or 'FOR UPDATE' not in u:
        errors.append(f'{vendor}: V105 overlap trigger does not serialize on policy lock row')
    if 'TR_ADM_APPROVAL_POLICY_NO_OVERLAP' not in u:
        errors.append(f'{vendor}: V105 overlap trigger missing')
    if vendor=='oracle':
        if 'COMPOUND TRIGGER' not in u:
            errors.append('oracle: V105 overlap enforcement must use compound trigger to avoid mutating-table reads')
        if 'AFTER STATEMENT IS' not in u:
            errors.append('oracle: V105 overlap compound trigger missing after-statement verification')
    verify=R/'cpf-tools/db/vendor'/vendor/'verify/105_verify_approval_runtime_hardening_r6.sql'
    vu=verify.read_text(encoding='utf-8',errors='replace').upper() if verify.is_file() else ''
    if not any(x in vu for x in ('RAISE EXCEPTION','RAISE_APPLICATION_ERROR','SIGNAL SQLSTATE')):
        errors.append(f'{vendor}: V105 verify is not fail-closed')

# V102 integration-closure canonical source must be vendor-native, not a copied dialect.
source17={
 'postgresql': R/'cpf-tools/db/vendor/postgresql/source/17_integration_closure_lifecycle.sql',
 'mariadb': R/'cpf-tools/db/vendor/mariadb/source/17_integration_closure_lifecycle.sql',
 'oracle': R/'cpf-tools/db/vendor/oracle/source/17_integration_closure_lifecycle.sql',
}
for vendor,p in source17.items():
    if not p.is_file(): errors.append(f'{vendor}: missing V102 source {p.name}'); continue
    u=p.read_text(encoding='utf-8',errors='replace').upper()
    if vendor=='postgresql':
        for forbidden in ('LONGBLOB','VARCHAR2(','NUMBER(19)'):
            if forbidden in u: errors.append(f'postgresql: V102 source contains foreign dialect token {forbidden}')
        for required in ('BYTEA','TIMESTAMPTZ'):
            if required not in u: errors.append(f'postgresql: V102 source missing native token {required}')
    elif vendor=='mariadb':
        for required in ('LONGBLOB','TIMESTAMP(6)'):
            if required not in u: errors.append(f'mariadb: V102 source missing native token {required}')
        for forbidden in ('BYTEA','VARCHAR2('):
            if forbidden in u: errors.append(f'mariadb: V102 source contains foreign dialect token {forbidden}')
    else:
        for required in ('BLOB','VARCHAR2(','TIMESTAMP(6) WITH TIME ZONE'):
            if required not in u: errors.append(f'oracle: V102 source missing native token {required}')
        for forbidden in ('LONGBLOB','BYTEA'):
            if forbidden in u: errors.append(f'oracle: V102 source contains foreign dialect token {forbidden}')

# Current canonical target must include V102 integration tables plus the V106 durable DQ operation ledger.
canonical=R/'cpf-tools/db/canonical/platform-schema.json'
try:
    c=json.loads(canonical.read_text(encoding='utf-8'))
    tables={str(t.get('name','')).upper():t for t in c.get('tables',[])}
    if c.get('tableCount') != len(c.get('tables',[])): errors.append('canonical tableCount mismatch')
    required_tables={
      'CPF_FIELD_ENCRYPTION_LEDGER':(),
      'CPF_DATA_QUALITY_RULE':('PARAMETERS_PAYLOAD',),
      'CPF_DATA_QUALITY_QUARANTINE':('VIOLATION_PAYLOAD','ROW_VERSION'),
      'CPF_WEBHOOK_ENDPOINT':(),
      'CPF_WEBHOOK_DELIVERY':(),
      'CPF_INTEGRATION_CLOSURE_AUDIT':(),
      'CPF_DATA_QUALITY_OPERATION':('OPERATION_ID','COMMAND_FINGERPRINT','RESULT_PAYLOAD','QUARANTINE_ID'),
    }
    for tn,cols in required_tables.items():
      t=tables.get(tn)
      if not t: errors.append(f'canonical missing table {tn}'); continue
      actual={str(x.get('name','')).upper() for x in t.get('columns',[])}
      for col in cols:
        if col not in actual: errors.append(f'canonical {tn} missing column {col}')
    op=tables.get('CPF_DATA_QUALITY_OPERATION')
    if op:
      if [x.upper() for x in op.get('primaryKey',[])] != ['OPERATION_ID']: errors.append('canonical DQ operation primary key mismatch')
      fks={str(x.get('name','')).upper():x for x in op.get('foreignKeys',[])}
      if 'FK_CPF_DQ_OPERATION_QUARANTINE' not in fks: errors.append('canonical DQ operation FK missing')
except Exception as e:
    errors.append(f'canonical schema invalid: {e}')

for e in errors: print('FAIL',e)
if errors: raise SystemExit(1)
print(f'PASS DB3 R6 lifecycle packs={len(PACKS)} vendors={len(vendors)} canonicalTables={len(tables)}')
