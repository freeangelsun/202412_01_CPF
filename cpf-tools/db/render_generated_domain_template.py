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
import argparse, hashlib, json, sys
from pathlib import Path
# import 하는 순간 Source Tree 에 __pycache__/*.pyc 가 생기고, clean-source 게이트가 이를
# garbage 로 판정한다. 호출자가 -B 를 주지 않아도 안전하도록 여기서 기록을 끈다.
sys.dont_write_bytecode = True
sys.path.insert(0,str(Path(__file__).resolve().parent))
import render_vendor_pack as r

VENDORS=r.OFFICIAL

def tokenise(s:str)->str:
    return s.replace('CPF_DOMAIN','@CPF_TABLE_PREFIX@')


def _sql_lower_literals(values: list[str]) -> str:
    return ', '.join(f"LOWER('{tokenise(value)}')" for value in values)


def _domain_contract(model: dict) -> dict:
    tables=sorted(model['tables'],key=lambda item:item['targetTableName'])
    columns=[]; indexes=[]; constraints=[]
    for table in tables:
        table_name=table['targetTableName']
        columns.extend(f"{table_name}.{column['name']}" for column in table['columns'])
        indexes.extend(f"{table_name}.{index['name']}" for index in table.get('indexes') or [])
        if table.get('primaryKey'):
            constraints.append((table_name,f"PK_{table_name}",'PRIMARY KEY'))
        constraints.extend((table_name,key['name'],'UNIQUE') for key in table.get('uniqueKeys') or [])
        constraints.extend((table_name,key['name'],'FOREIGN KEY') for key in table.get('foreignKeys') or [])
        constraints.extend((table_name,key['name'],'CHECK') for key in table.get('checks') or [])
    column_counts=[len(table['columns']) for table in tables]
    if len(tables)!=2 or column_counts!=[14,8] or len(columns)!=22 or len(indexes)!=5 or len(constraints)!=8:
        raise ValueError(
            'generated-domain canonical cardinality drift: '
            f'tables={len(tables)} columns={column_counts}/{len(columns)} '
            f'indexes={len(indexes)} constraints={len(constraints)}'
        )
    return {
        'tables':tables,
        'tableNames':[table['targetTableName'] for table in tables],
        'columns':columns,
        'columnCounts':column_counts,
        'indexes':indexes,
        'constraints':constraints,
    }


def _mariadb_verify(contract: dict) -> str:
    table_names=contract['tableNames']
    table_literals=_sql_lower_literals(table_names)
    column_literals=_sql_lower_literals(contract['columns'])
    index_literals=_sql_lower_literals(contract['indexes'])
    constraint_identities=[]
    for table_name,constraint_name,constraint_type in contract['constraints']:
        # MariaDB exposes both explicitly named primary keys as PRIMARY.
        physical_name='PRIMARY' if constraint_type=='PRIMARY KEY' else constraint_name
        constraint_identities.append(f'{table_name}.{physical_name}.{constraint_type}')
    constraint_literals=_sql_lower_literals(constraint_identities)
    first,second=table_names
    return f"""-- GENERATED FILE. DO NOT EDIT.
-- Source: cpf-tools/db/canonical/generated-domain-schema.json
-- Contract: exact 2 tables, 22 columns (14+8), 5 explicit indexes, 8 named constraints
-- Role: CUSTOMER_BUSINESS_DB
-- Vendor: mariadb

DROP PROCEDURE IF EXISTS CPF_VERIFY_GENERATED_DOMAIN;
DELIMITER $$
CREATE PROCEDURE CPF_VERIFY_GENERATED_DOMAIN()
BEGIN
    DECLARE v_actual BIGINT DEFAULT 0;
    DECLARE v_matched BIGINT DEFAULT 0;

    SELECT COUNT(*) INTO v_actual
      FROM information_schema.tables
     WHERE table_schema = DATABASE()
       AND table_type = 'BASE TABLE'
       AND LEFT(LOWER(table_name), CHAR_LENGTH(LOWER('@CPF_TABLE_PREFIX@_'))) = LOWER('@CPF_TABLE_PREFIX@_');
    SELECT COUNT(*) INTO v_matched
      FROM information_schema.tables
     WHERE table_schema = DATABASE()
       AND table_type = 'BASE TABLE'
       AND LOWER(table_name) IN ({table_literals});
    IF v_actual <> 2 OR v_matched <> 2 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'CPF generated domain table contract mismatch';
    END IF;

    SELECT COUNT(*) INTO v_actual
      FROM information_schema.columns
     WHERE table_schema = DATABASE()
       AND LOWER(table_name) IN ({table_literals});
    SELECT COUNT(*) INTO v_matched
      FROM information_schema.columns
     WHERE table_schema = DATABASE()
       AND LOWER(CONCAT(table_name, '.', column_name)) IN ({column_literals});
    IF v_actual <> 22 OR v_matched <> 22 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'CPF generated domain column contract mismatch';
    END IF;
    SELECT COUNT(*) INTO v_actual FROM information_schema.columns
     WHERE table_schema = DATABASE() AND LOWER(table_name) = LOWER('{tokenise(first)}');
    IF v_actual <> 14 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'CPF generated domain sample column count mismatch';
    END IF;
    SELECT COUNT(*) INTO v_actual FROM information_schema.columns
     WHERE table_schema = DATABASE() AND LOWER(table_name) = LOWER('{tokenise(second)}');
    IF v_actual <> 8 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'CPF generated domain ledger column count mismatch';
    END IF;
    SELECT COUNT(*) INTO v_actual
      FROM information_schema.columns
     WHERE table_schema = DATABASE()
       AND LOWER(table_name) IN ({table_literals})
       AND LOWER(column_name) = 'transaction_id'
       AND LOWER(data_type) = 'char'
       AND character_maximum_length = 34;
    IF v_actual <> 2 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'CPF generated domain transaction_id sentinel mismatch';
    END IF;

    SELECT COUNT(DISTINCT CONCAT(LOWER(table_name), '.', LOWER(index_name))) INTO v_actual
      FROM information_schema.statistics
     WHERE table_schema = DATABASE()
       AND LOWER(table_name) IN ({table_literals})
       AND non_unique = 1;
    SELECT COUNT(DISTINCT CONCAT(LOWER(table_name), '.', LOWER(index_name))) INTO v_matched
      FROM information_schema.statistics
     WHERE table_schema = DATABASE()
       AND LOWER(CONCAT(table_name, '.', index_name)) IN ({index_literals});
    IF v_actual <> 5 OR v_matched <> 5 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'CPF generated domain index contract mismatch';
    END IF;

    SELECT COUNT(*) INTO v_actual
      FROM information_schema.table_constraints
     WHERE constraint_schema = DATABASE()
       AND LOWER(table_name) IN ({table_literals})
       AND constraint_type IN ('PRIMARY KEY', 'UNIQUE', 'FOREIGN KEY', 'CHECK');
    SELECT COUNT(*) INTO v_matched
      FROM information_schema.table_constraints
     WHERE constraint_schema = DATABASE()
       AND LOWER(CONCAT(table_name, '.', constraint_name, '.', constraint_type)) IN ({constraint_literals});
    IF v_actual <> 8 OR v_matched <> 8 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'CPF generated domain constraint contract mismatch';
    END IF;
END$$
CALL CPF_VERIFY_GENERATED_DOMAIN()$$
DROP PROCEDURE CPF_VERIFY_GENERATED_DOMAIN$$
DELIMITER ;

SELECT 'generated_domain_sample_verify' AS check_name, 1 AS passed;
SELECT 'generated_domain_idempotency_verify' AS check_name, 1 AS passed;
"""


def _postgresql_verify(contract: dict) -> str:
    table_names=contract['tableNames']
    table_literals=_sql_lower_literals(table_names)
    column_literals=_sql_lower_literals(contract['columns'])
    index_literals=_sql_lower_literals(contract['indexes'])
    pg_types={'PRIMARY KEY':'p','UNIQUE':'u','FOREIGN KEY':'f','CHECK':'c'}
    constraint_literals=_sql_lower_literals([
        f'{table_name}.{constraint_name}.{pg_types[constraint_type]}'
        for table_name,constraint_name,constraint_type in contract['constraints']
    ])
    first,second=table_names
    return f"""-- GENERATED FILE. DO NOT EDIT.
-- Source: cpf-tools/db/canonical/generated-domain-schema.json
-- Contract: exact 2 tables, 22 columns (14+8), 5 explicit indexes, 8 named constraints
-- Role: CUSTOMER_BUSINESS_DB
-- Vendor: postgresql

DO $cpf_generated_domain_verify$
DECLARE
    v_actual BIGINT;
    v_matched BIGINT;
BEGIN
    SELECT COUNT(*) INTO v_actual
      FROM information_schema.tables
     WHERE table_schema = current_schema()
       AND table_type = 'BASE TABLE'
       AND LEFT(LOWER(table_name), LENGTH(LOWER('@CPF_TABLE_PREFIX@_'))) = LOWER('@CPF_TABLE_PREFIX@_');
    SELECT COUNT(*) INTO v_matched
      FROM information_schema.tables
     WHERE table_schema = current_schema()
       AND table_type = 'BASE TABLE'
       AND LOWER(table_name) IN ({table_literals});
    IF v_actual <> 2 OR v_matched <> 2 THEN
        RAISE EXCEPTION 'CPF generated domain table contract mismatch';
    END IF;

    SELECT COUNT(*) INTO v_actual
      FROM information_schema.columns
     WHERE table_schema = current_schema()
       AND LOWER(table_name) IN ({table_literals});
    SELECT COUNT(*) INTO v_matched
      FROM information_schema.columns
     WHERE table_schema = current_schema()
       AND LOWER(table_name || '.' || column_name) IN ({column_literals});
    IF v_actual <> 22 OR v_matched <> 22 THEN
        RAISE EXCEPTION 'CPF generated domain column contract mismatch';
    END IF;
    SELECT COUNT(*) INTO v_actual FROM information_schema.columns
     WHERE table_schema = current_schema() AND LOWER(table_name) = LOWER('{tokenise(first)}');
    IF v_actual <> 14 THEN
        RAISE EXCEPTION 'CPF generated domain sample column count mismatch';
    END IF;
    SELECT COUNT(*) INTO v_actual FROM information_schema.columns
     WHERE table_schema = current_schema() AND LOWER(table_name) = LOWER('{tokenise(second)}');
    IF v_actual <> 8 THEN
        RAISE EXCEPTION 'CPF generated domain ledger column count mismatch';
    END IF;
    SELECT COUNT(*) INTO v_actual
      FROM information_schema.columns
     WHERE table_schema = current_schema()
       AND LOWER(table_name) IN ({table_literals})
       AND LOWER(column_name) = 'transaction_id'
       AND LOWER(data_type) = 'character'
       AND character_maximum_length = 34;
    IF v_actual <> 2 THEN
        RAISE EXCEPTION 'CPF generated domain transaction_id sentinel mismatch';
    END IF;

    SELECT COUNT(*) INTO v_actual
      FROM pg_index i
      JOIN pg_class idx ON idx.oid = i.indexrelid
      JOIN pg_class t ON t.oid = i.indrelid
      JOIN pg_namespace n ON n.oid = t.relnamespace
      LEFT JOIN pg_constraint c ON c.conindid = i.indexrelid
     WHERE n.nspname = current_schema()
       AND LOWER(t.relname) IN ({table_literals})
       AND c.oid IS NULL
       AND i.indisvalid;
    SELECT COUNT(*) INTO v_matched
      FROM pg_indexes
     WHERE schemaname = current_schema()
       AND LOWER(tablename || '.' || indexname) IN ({index_literals});
    IF v_actual <> 5 OR v_matched <> 5 THEN
        RAISE EXCEPTION 'CPF generated domain index contract mismatch';
    END IF;

    SELECT COUNT(*) INTO v_actual
      FROM pg_constraint c
      JOIN pg_class t ON t.oid = c.conrelid
      JOIN pg_namespace n ON n.oid = t.relnamespace
     WHERE n.nspname = current_schema()
       AND LOWER(t.relname) IN ({table_literals})
       AND c.contype IN ('p', 'u', 'f', 'c');
    SELECT COUNT(*) INTO v_matched
      FROM pg_constraint c
      JOIN pg_class t ON t.oid = c.conrelid
      JOIN pg_namespace n ON n.oid = t.relnamespace
     WHERE n.nspname = current_schema()
       AND LOWER(t.relname || '.' || c.conname || '.' || c.contype) IN ({constraint_literals});
    IF v_actual <> 8 OR v_matched <> 8 THEN
        RAISE EXCEPTION 'CPF generated domain constraint contract mismatch';
    END IF;
END
$cpf_generated_domain_verify$;

SELECT 'generated_domain_sample_verify' AS check_name, 1 AS passed;
SELECT 'generated_domain_idempotency_verify' AS check_name, 1 AS passed;
"""


def _oracle_verify(contract: dict) -> str:
    table_names=contract['tableNames']
    table_literals=_sql_lower_literals(table_names)
    column_literals=_sql_lower_literals(contract['columns'])
    index_literals=_sql_lower_literals(contract['indexes'])
    oracle_types={'PRIMARY KEY':'P','UNIQUE':'U','FOREIGN KEY':'R','CHECK':'C'}
    constraint_literals=_sql_lower_literals([
        f'{table_name}.{constraint_name}.{oracle_types[constraint_type]}'
        for table_name,constraint_name,constraint_type in contract['constraints']
    ])
    first,second=table_names
    return f"""-- GENERATED FILE. DO NOT EDIT.
-- Source: cpf-tools/db/canonical/generated-domain-schema.json
-- Contract: exact 2 tables, 22 columns (14+8), 5 explicit indexes, 8 named constraints
-- Role: CUSTOMER_BUSINESS_DB
-- Vendor: oracle

DECLARE
    v_actual PLS_INTEGER;
    v_matched PLS_INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_actual
      FROM user_tables
     WHERE SUBSTR(LOWER(table_name), 1, LENGTH(LOWER('@CPF_TABLE_PREFIX@_'))) = LOWER('@CPF_TABLE_PREFIX@_');
    SELECT COUNT(*) INTO v_matched
      FROM user_tables
     WHERE LOWER(table_name) IN ({table_literals});
    IF v_actual <> 2 OR v_matched <> 2 THEN
        RAISE_APPLICATION_ERROR(-20001, 'CPF generated domain table contract mismatch');
    END IF;

    SELECT COUNT(*) INTO v_actual
      FROM user_tab_columns
     WHERE LOWER(table_name) IN ({table_literals});
    SELECT COUNT(*) INTO v_matched
      FROM user_tab_columns
     WHERE LOWER(table_name || '.' || column_name) IN ({column_literals});
    IF v_actual <> 22 OR v_matched <> 22 THEN
        RAISE_APPLICATION_ERROR(-20002, 'CPF generated domain column contract mismatch');
    END IF;
    SELECT COUNT(*) INTO v_actual FROM user_tab_columns
     WHERE LOWER(table_name) = LOWER('{tokenise(first)}');
    IF v_actual <> 14 THEN
        RAISE_APPLICATION_ERROR(-20003, 'CPF generated domain sample column count mismatch');
    END IF;
    SELECT COUNT(*) INTO v_actual FROM user_tab_columns
     WHERE LOWER(table_name) = LOWER('{tokenise(second)}');
    IF v_actual <> 8 THEN
        RAISE_APPLICATION_ERROR(-20004, 'CPF generated domain ledger column count mismatch');
    END IF;
    SELECT COUNT(*) INTO v_actual
      FROM user_tab_columns
     WHERE LOWER(table_name) IN ({table_literals})
       AND LOWER(column_name) = 'transaction_id'
       AND data_type = 'CHAR'
       AND char_length = 34;
    IF v_actual <> 2 THEN
        RAISE_APPLICATION_ERROR(-20005, 'CPF generated domain transaction_id sentinel mismatch');
    END IF;

    SELECT COUNT(*) INTO v_actual
      FROM user_indexes i
     WHERE LOWER(i.table_name) IN ({table_literals})
       AND NOT EXISTS (
               SELECT 1
                 FROM user_constraints c
                WHERE c.index_name = i.index_name
                  AND c.constraint_type IN ('P', 'U'));
    SELECT COUNT(*) INTO v_matched
      FROM user_indexes
     WHERE LOWER(table_name || '.' || index_name) IN ({index_literals});
    IF v_actual <> 5 OR v_matched <> 5 THEN
        RAISE_APPLICATION_ERROR(-20006, 'CPF generated domain index contract mismatch');
    END IF;

    SELECT COUNT(*) INTO v_actual
      FROM user_constraints
     WHERE LOWER(table_name) IN ({table_literals})
       AND constraint_type IN ('P', 'U', 'R', 'C')
       AND generated = 'USER NAME';
    SELECT COUNT(*) INTO v_matched
      FROM user_constraints
     WHERE generated = 'USER NAME'
       AND LOWER(table_name || '.' || constraint_name || '.' || constraint_type) IN ({constraint_literals});
    IF v_actual <> 8 OR v_matched <> 8 THEN
        RAISE_APPLICATION_ERROR(-20007, 'CPF generated domain constraint contract mismatch');
    END IF;
END;
/

SELECT 'generated_domain_sample_verify' AS check_name, 1 AS passed FROM dual;
SELECT 'generated_domain_idempotency_verify' AS check_name, 1 AS passed FROM dual;
"""


def render_domain_verify(vendor: str, model: dict) -> str:
    contract=_domain_contract(model)
    if vendor=='mariadb':
        return _mariadb_verify(contract)
    if vendor=='postgresql':
        return _postgresql_verify(contract)
    if vendor=='oracle':
        return _oracle_verify(contract)
    raise ValueError(f'unsupported generated-domain vendor: {vendor}')


def _normalized_bytes(content: str) -> bytes:
    return (content.rstrip()+'\n').encode('utf-8')

def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--check',action='store_true'); args=ap.parse_args()
    root=Path(args.root).resolve(); db=root/'cpf-tools/db'; model=json.loads((db/'canonical/generated-domain-schema.json').read_text(encoding='utf-8-sig'))
    schema={'schemaVersion':model['schemaVersion'],'tables':[]}
    for t in model['tables']:
        x=dict(t); x['targetDatabaseRole']='CUSTOMER_BUSINESS_DB'; x['productionDefault']=True; schema['tables'].append(x)
    for vendor in VENDORS:
        out=db/'generated/domain-template'/vendor
        install=tokenise(r.render_schema(vendor,schema,'CUSTOMER_BUSINESS_DB'))
        rollback=tokenise(r.render_rollback(vendor,schema,'CUSTOMER_BUSINESS_DB'))
        verify=render_domain_verify(vendor,model)
        files={
          'install/10_empty_install.sql.template':install,
          'migration/V1____DOMAIN___domain.sql.template':install,
          'seed/20_product_seed.sql.template':'-- GENERATED FROM cpf-tools/db/canonical/generated-domain-schema.json\n-- No business sample rows are inserted by framework seed.\n',
          'rollback/R1__remove___DOMAIN___domain.sql.template':rollback,
          'verify/90_verify.sql.template':verify,
        }
        hashes={rel:hashlib.sha256(_normalized_bytes(content)).hexdigest() for rel,content in files.items()}
        manifest={'schemaVersion':1,'vendor':vendor,'generated':True,'canonicalSource':'cpf-tools/db/canonical/generated-domain-schema.json','businessDatabaseRole':'CUSTOMER_BUSINESS_DB','artifacts':hashes,'runtimeDialectOwner':f'cpf-starters/data/persistence/dialect/{vendor}/generated-domain'}
        if args.check:
            drift=[]
            for rel,content in files.items():
                p=out/rel
                if not p.is_file() or p.read_text(encoding='utf-8-sig')!=content.rstrip()+'\n': drift.append(rel)
            manifest_path=out/'manifest.json'
            if not manifest_path.is_file() or json.loads(manifest_path.read_text(encoding='utf-8-sig'))!=manifest:
                drift.append('manifest.json')
            if drift: raise SystemExit(f'{vendor} generated-domain template drift: {drift}')
        else:
            written_hashes={rel:r.write_text(out/rel,content) for rel,content in files.items()}
            if written_hashes!=hashes:
                raise RuntimeError(f'{vendor} generated-domain write hash mismatch')
            r.write_text(out/'manifest.json',json.dumps(manifest,ensure_ascii=False,indent=2))
    print('CPF_GENERATED_DOMAIN_DB_TEMPLATE_RENDER=PASS vendors='+','.join(VENDORS))
if __name__=='__main__': main()
