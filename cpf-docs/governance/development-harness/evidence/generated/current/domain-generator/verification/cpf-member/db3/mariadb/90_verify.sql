-- GENERATED FILE. DO NOT EDIT.
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
       AND LEFT(LOWER(table_name), CHAR_LENGTH(LOWER('MBR_'))) = LOWER('MBR_');
    SELECT COUNT(*) INTO v_matched
      FROM information_schema.tables
     WHERE table_schema = DATABASE()
       AND table_type = 'BASE TABLE'
       AND LOWER(table_name) IN (LOWER('MBR_sample_item'), LOWER('MBR_sample_item_idem'));
    IF v_actual <> 2 OR v_matched <> 2 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'CPF generated domain table contract mismatch';
    END IF;

    SELECT COUNT(*) INTO v_actual
      FROM information_schema.columns
     WHERE table_schema = DATABASE()
       AND LOWER(table_name) IN (LOWER('MBR_sample_item'), LOWER('MBR_sample_item_idem'));
    SELECT COUNT(*) INTO v_matched
      FROM information_schema.columns
     WHERE table_schema = DATABASE()
       AND LOWER(CONCAT(table_name, '.', column_name)) IN (LOWER('MBR_sample_item.sample_item_id'), LOWER('MBR_sample_item.sample_key'), LOWER('MBR_sample_item.item_name'), LOWER('MBR_sample_item.status_code'), LOWER('MBR_sample_item.version_no'), LOWER('MBR_sample_item.idempotency_key'), LOWER('MBR_sample_item.transaction_id'), LOWER('MBR_sample_item.transaction_sequence'), LOWER('MBR_sample_item.transaction_at'), LOWER('MBR_sample_item.deleted_yn'), LOWER('MBR_sample_item.created_by'), LOWER('MBR_sample_item.created_at'), LOWER('MBR_sample_item.updated_by'), LOWER('MBR_sample_item.updated_at'), LOWER('MBR_sample_item_idem.idempotency_key'), LOWER('MBR_sample_item_idem.operation_code'), LOWER('MBR_sample_item_idem.request_hash'), LOWER('MBR_sample_item_idem.sample_item_id'), LOWER('MBR_sample_item_idem.result_version'), LOWER('MBR_sample_item_idem.deleted_yn'), LOWER('MBR_sample_item_idem.transaction_id'), LOWER('MBR_sample_item_idem.created_at'));
    IF v_actual <> 22 OR v_matched <> 22 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'CPF generated domain column contract mismatch';
    END IF;
    SELECT COUNT(*) INTO v_actual FROM information_schema.columns
     WHERE table_schema = DATABASE() AND LOWER(table_name) = LOWER('MBR_sample_item');
    IF v_actual <> 14 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'CPF generated domain sample column count mismatch';
    END IF;
    SELECT COUNT(*) INTO v_actual FROM information_schema.columns
     WHERE table_schema = DATABASE() AND LOWER(table_name) = LOWER('MBR_sample_item_idem');
    IF v_actual <> 8 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'CPF generated domain ledger column count mismatch';
    END IF;
    SELECT COUNT(*) INTO v_actual
      FROM information_schema.columns
     WHERE table_schema = DATABASE()
       AND LOWER(table_name) IN (LOWER('MBR_sample_item'), LOWER('MBR_sample_item_idem'))
       AND LOWER(column_name) = 'transaction_id'
       AND LOWER(data_type) = 'char'
       AND character_maximum_length = 34;
    IF v_actual <> 2 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'CPF generated domain transaction_id sentinel mismatch';
    END IF;

    SELECT COUNT(DISTINCT CONCAT(LOWER(table_name), '.', LOWER(index_name))) INTO v_actual
      FROM information_schema.statistics
     WHERE table_schema = DATABASE()
       AND LOWER(table_name) IN (LOWER('MBR_sample_item'), LOWER('MBR_sample_item_idem'))
       AND non_unique = 1;
    SELECT COUNT(DISTINCT CONCAT(LOWER(table_name), '.', LOWER(index_name))) INTO v_matched
      FROM information_schema.statistics
     WHERE table_schema = DATABASE()
       AND LOWER(CONCAT(table_name, '.', index_name)) IN (LOWER('MBR_sample_item.ix_MBR_sample_item_idem'), LOWER('MBR_sample_item.ix_MBR_sample_item_status'), LOWER('MBR_sample_item.ix_MBR_sample_item_tx'), LOWER('MBR_sample_item_idem.ix_MBR_sample_idem_item'), LOWER('MBR_sample_item_idem.ix_MBR_sample_idem_tx'));
    IF v_actual <> 5 OR v_matched <> 5 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'CPF generated domain index contract mismatch';
    END IF;

    SELECT COUNT(*) INTO v_actual
      FROM information_schema.table_constraints
     WHERE constraint_schema = DATABASE()
       AND LOWER(table_name) IN (LOWER('MBR_sample_item'), LOWER('MBR_sample_item_idem'))
       AND constraint_type IN ('PRIMARY KEY', 'UNIQUE', 'FOREIGN KEY', 'CHECK');
    SELECT COUNT(*) INTO v_matched
      FROM information_schema.table_constraints
     WHERE constraint_schema = DATABASE()
       AND LOWER(CONCAT(table_name, '.', constraint_name, '.', constraint_type)) IN (LOWER('MBR_sample_item.PRIMARY.PRIMARY KEY'), LOWER('MBR_sample_item.uk_MBR_sample_item_key.UNIQUE'), LOWER('MBR_sample_item.ck_MBR_sample_item_status.CHECK'), LOWER('MBR_sample_item.ck_MBR_sample_item_deleted.CHECK'), LOWER('MBR_sample_item_idem.PRIMARY.PRIMARY KEY'), LOWER('MBR_sample_item_idem.fk_MBR_sample_idem_item.FOREIGN KEY'), LOWER('MBR_sample_item_idem.ck_MBR_sample_idem_op.CHECK'), LOWER('MBR_sample_item_idem.ck_MBR_sample_idem_deleted.CHECK'));
    IF v_actual <> 8 OR v_matched <> 8 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'CPF generated domain constraint contract mismatch';
    END IF;
END$$
CALL CPF_VERIFY_GENERATED_DOMAIN()$$
DROP PROCEDURE CPF_VERIFY_GENERATED_DOMAIN$$
DELIMITER ;

SELECT 'generated_domain_sample_verify' AS check_name, 1 AS passed;
SELECT 'generated_domain_idempotency_verify' AS check_name, 1 AS passed;
