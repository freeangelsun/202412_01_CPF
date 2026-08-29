-- GENERATED FILE. DO NOT EDIT.
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
       AND LEFT(LOWER(table_name), LENGTH(LOWER('EXS_'))) = LOWER('EXS_');
    SELECT COUNT(*) INTO v_matched
      FROM information_schema.tables
     WHERE table_schema = current_schema()
       AND table_type = 'BASE TABLE'
       AND LOWER(table_name) IN (LOWER('EXS_sample_item'), LOWER('EXS_sample_item_idem'));
    IF v_actual <> 2 OR v_matched <> 2 THEN
        RAISE EXCEPTION 'CPF generated domain table contract mismatch';
    END IF;

    SELECT COUNT(*) INTO v_actual
      FROM information_schema.columns
     WHERE table_schema = current_schema()
       AND LOWER(table_name) IN (LOWER('EXS_sample_item'), LOWER('EXS_sample_item_idem'));
    SELECT COUNT(*) INTO v_matched
      FROM information_schema.columns
     WHERE table_schema = current_schema()
       AND LOWER(table_name || '.' || column_name) IN (LOWER('EXS_sample_item.sample_item_id'), LOWER('EXS_sample_item.sample_key'), LOWER('EXS_sample_item.item_name'), LOWER('EXS_sample_item.status_code'), LOWER('EXS_sample_item.version_no'), LOWER('EXS_sample_item.idempotency_key'), LOWER('EXS_sample_item.transaction_id'), LOWER('EXS_sample_item.transaction_sequence'), LOWER('EXS_sample_item.transaction_at'), LOWER('EXS_sample_item.deleted_yn'), LOWER('EXS_sample_item.created_by'), LOWER('EXS_sample_item.created_at'), LOWER('EXS_sample_item.updated_by'), LOWER('EXS_sample_item.updated_at'), LOWER('EXS_sample_item_idem.idempotency_key'), LOWER('EXS_sample_item_idem.operation_code'), LOWER('EXS_sample_item_idem.request_hash'), LOWER('EXS_sample_item_idem.sample_item_id'), LOWER('EXS_sample_item_idem.result_version'), LOWER('EXS_sample_item_idem.deleted_yn'), LOWER('EXS_sample_item_idem.transaction_id'), LOWER('EXS_sample_item_idem.created_at'));
    IF v_actual <> 22 OR v_matched <> 22 THEN
        RAISE EXCEPTION 'CPF generated domain column contract mismatch';
    END IF;
    SELECT COUNT(*) INTO v_actual FROM information_schema.columns
     WHERE table_schema = current_schema() AND LOWER(table_name) = LOWER('EXS_sample_item');
    IF v_actual <> 14 THEN
        RAISE EXCEPTION 'CPF generated domain sample column count mismatch';
    END IF;
    SELECT COUNT(*) INTO v_actual FROM information_schema.columns
     WHERE table_schema = current_schema() AND LOWER(table_name) = LOWER('EXS_sample_item_idem');
    IF v_actual <> 8 THEN
        RAISE EXCEPTION 'CPF generated domain ledger column count mismatch';
    END IF;
    SELECT COUNT(*) INTO v_actual
      FROM information_schema.columns
     WHERE table_schema = current_schema()
       AND LOWER(table_name) IN (LOWER('EXS_sample_item'), LOWER('EXS_sample_item_idem'))
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
       AND LOWER(t.relname) IN (LOWER('EXS_sample_item'), LOWER('EXS_sample_item_idem'))
       AND c.oid IS NULL
       AND i.indisvalid;
    SELECT COUNT(*) INTO v_matched
      FROM pg_indexes
     WHERE schemaname = current_schema()
       AND LOWER(tablename || '.' || indexname) IN (LOWER('EXS_sample_item.ix_EXS_sample_item_idem'), LOWER('EXS_sample_item.ix_EXS_sample_item_status'), LOWER('EXS_sample_item.ix_EXS_sample_item_tx'), LOWER('EXS_sample_item_idem.ix_EXS_sample_idem_item'), LOWER('EXS_sample_item_idem.ix_EXS_sample_idem_tx'));
    IF v_actual <> 5 OR v_matched <> 5 THEN
        RAISE EXCEPTION 'CPF generated domain index contract mismatch';
    END IF;

    SELECT COUNT(*) INTO v_actual
      FROM pg_constraint c
      JOIN pg_class t ON t.oid = c.conrelid
      JOIN pg_namespace n ON n.oid = t.relnamespace
     WHERE n.nspname = current_schema()
       AND LOWER(t.relname) IN (LOWER('EXS_sample_item'), LOWER('EXS_sample_item_idem'))
       AND c.contype IN ('p', 'u', 'f', 'c');
    SELECT COUNT(*) INTO v_matched
      FROM pg_constraint c
      JOIN pg_class t ON t.oid = c.conrelid
      JOIN pg_namespace n ON n.oid = t.relnamespace
     WHERE n.nspname = current_schema()
       AND LOWER(t.relname || '.' || c.conname || '.' || c.contype) IN (LOWER('EXS_sample_item.PK_EXS_sample_item.p'), LOWER('EXS_sample_item.uk_EXS_sample_item_key.u'), LOWER('EXS_sample_item.ck_EXS_sample_item_status.c'), LOWER('EXS_sample_item.ck_EXS_sample_item_deleted.c'), LOWER('EXS_sample_item_idem.PK_EXS_sample_item_idem.p'), LOWER('EXS_sample_item_idem.fk_EXS_sample_idem_item.f'), LOWER('EXS_sample_item_idem.ck_EXS_sample_idem_op.c'), LOWER('EXS_sample_item_idem.ck_EXS_sample_idem_deleted.c'));
    IF v_actual <> 8 OR v_matched <> 8 THEN
        RAISE EXCEPTION 'CPF generated domain constraint contract mismatch';
    END IF;
END
$cpf_generated_domain_verify$;

SELECT 'generated_domain_sample_verify' AS check_name, 1 AS passed;
SELECT 'generated_domain_idempotency_verify' AS check_name, 1 AS passed;
