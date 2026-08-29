-- GENERATED FILE. DO NOT EDIT.
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
     WHERE SUBSTR(LOWER(table_name), 1, LENGTH(LOWER('MBR_'))) = LOWER('MBR_');
    SELECT COUNT(*) INTO v_matched
      FROM user_tables
     WHERE LOWER(table_name) IN (LOWER('MBR_sample_item'), LOWER('MBR_sample_item_idem'));
    IF v_actual <> 2 OR v_matched <> 2 THEN
        RAISE_APPLICATION_ERROR(-20001, 'CPF generated domain table contract mismatch');
    END IF;

    SELECT COUNT(*) INTO v_actual
      FROM user_tab_columns
     WHERE LOWER(table_name) IN (LOWER('MBR_sample_item'), LOWER('MBR_sample_item_idem'));
    SELECT COUNT(*) INTO v_matched
      FROM user_tab_columns
     WHERE LOWER(table_name || '.' || column_name) IN (LOWER('MBR_sample_item.sample_item_id'), LOWER('MBR_sample_item.sample_key'), LOWER('MBR_sample_item.item_name'), LOWER('MBR_sample_item.status_code'), LOWER('MBR_sample_item.version_no'), LOWER('MBR_sample_item.idempotency_key'), LOWER('MBR_sample_item.transaction_id'), LOWER('MBR_sample_item.transaction_sequence'), LOWER('MBR_sample_item.transaction_at'), LOWER('MBR_sample_item.deleted_yn'), LOWER('MBR_sample_item.created_by'), LOWER('MBR_sample_item.created_at'), LOWER('MBR_sample_item.updated_by'), LOWER('MBR_sample_item.updated_at'), LOWER('MBR_sample_item_idem.idempotency_key'), LOWER('MBR_sample_item_idem.operation_code'), LOWER('MBR_sample_item_idem.request_hash'), LOWER('MBR_sample_item_idem.sample_item_id'), LOWER('MBR_sample_item_idem.result_version'), LOWER('MBR_sample_item_idem.deleted_yn'), LOWER('MBR_sample_item_idem.transaction_id'), LOWER('MBR_sample_item_idem.created_at'));
    IF v_actual <> 22 OR v_matched <> 22 THEN
        RAISE_APPLICATION_ERROR(-20002, 'CPF generated domain column contract mismatch');
    END IF;
    SELECT COUNT(*) INTO v_actual FROM user_tab_columns
     WHERE LOWER(table_name) = LOWER('MBR_sample_item');
    IF v_actual <> 14 THEN
        RAISE_APPLICATION_ERROR(-20003, 'CPF generated domain sample column count mismatch');
    END IF;
    SELECT COUNT(*) INTO v_actual FROM user_tab_columns
     WHERE LOWER(table_name) = LOWER('MBR_sample_item_idem');
    IF v_actual <> 8 THEN
        RAISE_APPLICATION_ERROR(-20004, 'CPF generated domain ledger column count mismatch');
    END IF;
    SELECT COUNT(*) INTO v_actual
      FROM user_tab_columns
     WHERE LOWER(table_name) IN (LOWER('MBR_sample_item'), LOWER('MBR_sample_item_idem'))
       AND LOWER(column_name) = 'transaction_id'
       AND data_type = 'CHAR'
       AND char_length = 34;
    IF v_actual <> 2 THEN
        RAISE_APPLICATION_ERROR(-20005, 'CPF generated domain transaction_id sentinel mismatch');
    END IF;

    SELECT COUNT(*) INTO v_actual
      FROM user_indexes i
     WHERE LOWER(i.table_name) IN (LOWER('MBR_sample_item'), LOWER('MBR_sample_item_idem'))
       AND NOT EXISTS (
               SELECT 1
                 FROM user_constraints c
                WHERE c.index_name = i.index_name
                  AND c.constraint_type IN ('P', 'U'));
    SELECT COUNT(*) INTO v_matched
      FROM user_indexes
     WHERE LOWER(table_name || '.' || index_name) IN (LOWER('MBR_sample_item.ix_MBR_sample_item_idem'), LOWER('MBR_sample_item.ix_MBR_sample_item_status'), LOWER('MBR_sample_item.ix_MBR_sample_item_tx'), LOWER('MBR_sample_item_idem.ix_MBR_sample_idem_item'), LOWER('MBR_sample_item_idem.ix_MBR_sample_idem_tx'));
    IF v_actual <> 5 OR v_matched <> 5 THEN
        RAISE_APPLICATION_ERROR(-20006, 'CPF generated domain index contract mismatch');
    END IF;

    SELECT COUNT(*) INTO v_actual
      FROM user_constraints
     WHERE LOWER(table_name) IN (LOWER('MBR_sample_item'), LOWER('MBR_sample_item_idem'))
       AND constraint_type IN ('P', 'U', 'R', 'C')
       AND generated = 'USER NAME';
    SELECT COUNT(*) INTO v_matched
      FROM user_constraints
     WHERE generated = 'USER NAME'
       AND LOWER(table_name || '.' || constraint_name || '.' || constraint_type) IN (LOWER('MBR_sample_item.PK_MBR_sample_item.P'), LOWER('MBR_sample_item.uk_MBR_sample_item_key.U'), LOWER('MBR_sample_item.ck_MBR_sample_item_status.C'), LOWER('MBR_sample_item.ck_MBR_sample_item_deleted.C'), LOWER('MBR_sample_item_idem.PK_MBR_sample_item_idem.P'), LOWER('MBR_sample_item_idem.fk_MBR_sample_idem_item.R'), LOWER('MBR_sample_item_idem.ck_MBR_sample_idem_op.C'), LOWER('MBR_sample_item_idem.ck_MBR_sample_idem_deleted.C'));
    IF v_actual <> 8 OR v_matched <> 8 THEN
        RAISE_APPLICATION_ERROR(-20007, 'CPF generated domain constraint contract mismatch');
    END IF;
END;
/

SELECT 'generated_domain_sample_verify' AS check_name, 1 AS passed FROM dual;
SELECT 'generated_domain_idempotency_verify' AS check_name, 1 AS passed FROM dual;
