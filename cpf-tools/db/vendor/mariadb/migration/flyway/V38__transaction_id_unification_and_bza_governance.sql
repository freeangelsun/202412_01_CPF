-- CPF V38 - transactionId 단일화 + BZA 조직/결재 거버넌스 보강
-- 기준: 536b229 / R4 semantic hardening
-- 주의: V1~V37 Historical Migration은 수정하지 않는다.
-- 정책: 동일 업무 흐름은 transactionId 하나를 승계하고 실행 계층은 transactionSegmentId/parentSegmentId로만 표현한다.
-- 안전: 34자를 초과하는 기존 transaction ID는 절단하지 않고 Migration을 실패시킨다.

DELIMITER $$

DROP PROCEDURE IF EXISTS cpf_r4_assert_column_max_length$$
CREATE PROCEDURE cpf_r4_assert_column_max_length(
    IN p_schema VARCHAR(64),
    IN p_table VARCHAR(64),
    IN p_column VARCHAR(64),
    IN p_max_length INT
)
BEGIN
    IF EXISTS (
        SELECT 1
          FROM information_schema.columns
         WHERE table_schema=p_schema
           AND table_name=p_table
           AND column_name=p_column
    ) THEN
        SET @cpf_r4_max_len = 0;
        SET @q = CONCAT(
            'SELECT COALESCE(MAX(CHAR_LENGTH(TRIM(`', p_column, '`))),0) ',
            'INTO @cpf_r4_max_len FROM `', p_schema, '`.`', p_table, '`'
        );
        PREPARE stmt FROM @q; EXECUTE stmt; DEALLOCATE PREPARE stmt;
        IF COALESCE(@cpf_r4_max_len, 0) > p_max_length THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'CPF V38 blocked: transaction id exceeds target length';
        END IF;
    END IF;
END$$

DROP PROCEDURE IF EXISTS cpf_r4_rename_transaction_column$$
CREATE PROCEDURE cpf_r4_rename_transaction_column(
    IN p_schema VARCHAR(64),
    IN p_table VARCHAR(64),
    IN p_old VARCHAR(64),
    IN p_new VARCHAR(64),
    IN p_nullable VARCHAR(16)
)
BEGIN
    DECLARE v_old_count INT DEFAULT 0;
    DECLARE v_new_count INT DEFAULT 0;

    SELECT COUNT(*) INTO v_old_count
      FROM information_schema.columns
     WHERE table_schema=p_schema AND table_name=p_table AND column_name=p_old;

    SELECT COUNT(*) INTO v_new_count
      FROM information_schema.columns
     WHERE table_schema=p_schema AND table_name=p_table AND column_name=p_new;

    IF v_old_count > 0 AND v_new_count = 0 THEN
        CALL cpf_r4_assert_column_max_length(p_schema, p_table, p_old, 34);
        SET @q = CONCAT(
            'ALTER TABLE `', p_schema, '`.`', p_table,
            '` CHANGE COLUMN `', p_old, '` `', p_new,
            '` CHAR(34) ', p_nullable, ' COMMENT ''CPF transactionId'''
        );
        PREPARE stmt FROM @q; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    ELSEIF v_new_count > 0 THEN
        CALL cpf_r4_assert_column_max_length(p_schema, p_table, p_new, 34);
        SET @q = CONCAT(
            'ALTER TABLE `', p_schema, '`.`', p_table,
            '` MODIFY COLUMN `', p_new,
            '` CHAR(34) ', p_nullable, ' COMMENT ''CPF transactionId'''
        );
        PREPARE stmt FROM @q; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
END$$

DROP PROCEDURE IF EXISTS cpf_r4_add_column_if_missing$$
CREATE PROCEDURE cpf_r4_add_column_if_missing(
    IN p_schema VARCHAR(64),
    IN p_table VARCHAR(64),
    IN p_column VARCHAR(64),
    IN p_definition TEXT
)
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
         WHERE table_schema=p_schema AND table_name=p_table
    )
    AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
         WHERE table_schema=p_schema AND table_name=p_table AND column_name=p_column
    ) THEN
        SET @q = CONCAT(
            'ALTER TABLE `', p_schema, '`.`', p_table,
            '` ADD COLUMN `', p_column, '` ', p_definition
        );
        PREPARE stmt FROM @q; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
END$$

DROP PROCEDURE IF EXISTS cpf_r4_drop_column_if_exists$$
CREATE PROCEDURE cpf_r4_drop_column_if_exists(
    IN p_schema VARCHAR(64),
    IN p_table VARCHAR(64),
    IN p_column VARCHAR(64)
)
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
         WHERE table_schema=p_schema AND table_name=p_table AND column_name=p_column
    ) THEN
        SET @q = CONCAT(
            'ALTER TABLE `', p_schema, '`.`', p_table,
            '` DROP COLUMN `', p_column, '`'
        );
        PREPARE stmt FROM @q; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
END$$

DROP PROCEDURE IF EXISTS cpf_r4_fill_transaction_from_legacy$$
CREATE PROCEDURE cpf_r4_fill_transaction_from_legacy(
    IN p_schema VARCHAR(64),
    IN p_table VARCHAR(64),
    IN p_first_legacy VARCHAR(64),
    IN p_second_legacy VARCHAR(64)
)
BEGIN
    DECLARE v_target_count INT DEFAULT 0;
    DECLARE v_first_count INT DEFAULT 0;
    DECLARE v_second_count INT DEFAULT 0;
    DECLARE v_expr TEXT DEFAULT '`transaction_id`';

    SELECT COUNT(*) INTO v_target_count
      FROM information_schema.columns
     WHERE table_schema=p_schema AND table_name=p_table AND column_name='transaction_id';

    IF v_target_count > 0 THEN
        IF p_first_legacy IS NOT NULL AND p_first_legacy <> '' THEN
            SELECT COUNT(*) INTO v_first_count
              FROM information_schema.columns
             WHERE table_schema=p_schema AND table_name=p_table AND column_name=p_first_legacy;
            IF v_first_count > 0 THEN
                CALL cpf_r4_assert_column_max_length(p_schema, p_table, p_first_legacy, 34);
                SET v_expr = CONCAT(
                    'NULLIF(TRIM(`', p_first_legacy, '`),''''),',
                    v_expr
                );
            END IF;
        END IF;

        IF p_second_legacy IS NOT NULL AND p_second_legacy <> '' THEN
            SELECT COUNT(*) INTO v_second_count
              FROM information_schema.columns
             WHERE table_schema=p_schema AND table_name=p_table AND column_name=p_second_legacy;
            IF v_second_count > 0 THEN
                CALL cpf_r4_assert_column_max_length(p_schema, p_table, p_second_legacy, 34);
                -- second legacy를 앞에 두어 parent 거래 ID가 있으면 child 거래 ID보다 우선한다.
                SET v_expr = CONCAT(
                    'NULLIF(TRIM(`', p_second_legacy, '`),''''),',
                    v_expr
                );
            END IF;
        END IF;

        IF v_first_count > 0 OR v_second_count > 0 THEN
            SET @q = CONCAT(
                'UPDATE `', p_schema, '`.`', p_table,
                '` SET `transaction_id` = COALESCE(', v_expr, ') ',
                'WHERE `transaction_id` IS NULL OR TRIM(`transaction_id`) = '''''
            );
            PREPARE stmt FROM @q; EXECUTE stmt; DEALLOCATE PREPARE stmt;
        END IF;
    END IF;
END$$

DROP PROCEDURE IF EXISTS cpf_r4_fill_segment_from_legacy$$
CREATE PROCEDURE cpf_r4_fill_segment_from_legacy(
    IN p_schema VARCHAR(64),
    IN p_table VARCHAR(64),
    IN p_legacy VARCHAR(64)
)
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
         WHERE table_schema=p_schema AND table_name=p_table AND column_name='transaction_segment_id'
    )
    AND p_legacy IS NOT NULL
    AND p_legacy <> ''
    AND EXISTS (
        SELECT 1 FROM information_schema.columns
         WHERE table_schema=p_schema AND table_name=p_table AND column_name=p_legacy
    ) THEN
        SET @q = CONCAT(
            'UPDATE `', p_schema, '`.`', p_table,
            '` SET `transaction_segment_id` = NULLIF(TRIM(`', p_legacy, '`),'''') ',
            'WHERE (`transaction_segment_id` IS NULL OR TRIM(`transaction_segment_id`)='''') ',
            'AND `', p_legacy, '` IS NOT NULL AND TRIM(`', p_legacy, '`)<>'''''
        );
        PREPARE stmt FROM @q; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
END$$

DROP PROCEDURE IF EXISTS cpf_r4_promote_parent_transaction$$
CREATE PROCEDURE cpf_r4_promote_parent_transaction(
    IN p_schema VARCHAR(64),
    IN p_table VARCHAR(64),
    IN p_parent_column VARCHAR(64)
)
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
         WHERE table_schema=p_schema AND table_name=p_table AND column_name='transaction_id'
    )
    AND EXISTS (
        SELECT 1 FROM information_schema.columns
         WHERE table_schema=p_schema AND table_name=p_table AND column_name='transaction_segment_id'
    )
    AND EXISTS (
        SELECT 1 FROM information_schema.columns
         WHERE table_schema=p_schema AND table_name=p_table AND column_name=p_parent_column
    ) THEN
        CALL cpf_r4_assert_column_max_length(p_schema, p_table, p_parent_column, 34);

        -- 기존 child/execution transactionId는 legacy segment로 보존한 뒤 parent transactionId를 승계한다.
        SET @q = CONCAT(
            'UPDATE `', p_schema, '`.`', p_table,
            '` SET `transaction_segment_id` = COALESCE(NULLIF(TRIM(`transaction_segment_id`),''''), NULLIF(TRIM(`transaction_id`),'''')) ',
            'WHERE `', p_parent_column, '` IS NOT NULL AND TRIM(`', p_parent_column, '`)<>'''''
        );
        PREPARE stmt FROM @q; EXECUTE stmt; DEALLOCATE PREPARE stmt;

        SET @q = CONCAT(
            'UPDATE `', p_schema, '`.`', p_table,
            '` SET `transaction_id` = NULLIF(TRIM(`', p_parent_column, '`),'''') ',
            'WHERE `', p_parent_column, '` IS NOT NULL AND TRIM(`', p_parent_column, '`)<>'''''
        );
        PREPARE stmt FROM @q; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
END$$

DROP PROCEDURE IF EXISTS cpf_r4_sync_bat_center_cut_result$$
CREATE PROCEDURE cpf_r4_sync_bat_center_cut_result()
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
         WHERE table_schema='batDB' AND table_name='bat_center_cut_result'
    )
    AND EXISTS (
        SELECT 1 FROM information_schema.tables
         WHERE table_schema='batDB' AND table_name='bat_center_cut_item'
    ) THEN
        UPDATE batDB.bat_center_cut_result r
        LEFT JOIN batDB.bat_center_cut_item i
          ON i.center_cut_item_id = r.center_cut_item_id
        SET r.transaction_id = COALESCE(i.transaction_id, r.transaction_id),
            r.parent_segment_id = COALESCE(r.parent_segment_id, i.parent_segment_id),
            r.transaction_segment_id = COALESCE(r.transaction_segment_id, i.transaction_segment_id)
        WHERE r.transaction_id IS NULL
           OR r.parent_segment_id IS NULL
           OR r.transaction_segment_id IS NULL;
    END IF;
END$$

DROP PROCEDURE IF EXISTS cpf_r4_sync_ref_center_cut_result$$
CREATE PROCEDURE cpf_r4_sync_ref_center_cut_result()
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
         WHERE table_schema='refDB' AND table_name='ref_center_cut_sample_result'
    )
    AND EXISTS (
        SELECT 1 FROM information_schema.tables
         WHERE table_schema='refDB' AND table_name='ref_center_cut_sample_target'
    ) THEN
        UPDATE refDB.ref_center_cut_sample_result r
        LEFT JOIN refDB.ref_center_cut_sample_target t
          ON t.target_id = r.target_id
        SET r.transaction_id = COALESCE(r.transaction_id, t.transaction_id),
            r.parent_segment_id = COALESCE(r.parent_segment_id, t.parent_segment_id),
            r.transaction_segment_id = COALESCE(r.transaction_segment_id, t.transaction_segment_id)
        WHERE r.transaction_id IS NULL
           OR r.parent_segment_id IS NULL
           OR r.transaction_segment_id IS NULL;
    END IF;
END$$

DROP PROCEDURE IF EXISTS cpf_r4_add_index_if_missing$$
CREATE PROCEDURE cpf_r4_add_index_if_missing(
    IN p_schema VARCHAR(64),
    IN p_table VARCHAR(64),
    IN p_index VARCHAR(64),
    IN p_columns TEXT,
    IN p_unique BOOLEAN
)
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
         WHERE table_schema=p_schema AND table_name=p_table
    )
    AND NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
         WHERE table_schema=p_schema AND table_name=p_table AND index_name=p_index
    ) THEN
        SET @q = CONCAT(
            'ALTER TABLE `', p_schema, '`.`', p_table, '` ADD ',
            IF(p_unique,'UNIQUE ',''), 'INDEX `', p_index, '` (', p_columns, ')'
        );
        PREPARE stmt FROM @q; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
END$$

DROP PROCEDURE IF EXISTS cpf_r4_drop_index_if_exists$$
CREATE PROCEDURE cpf_r4_drop_index_if_exists(
    IN p_schema VARCHAR(64),
    IN p_table VARCHAR(64),
    IN p_index VARCHAR(64)
)
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.statistics
         WHERE table_schema=p_schema AND table_name=p_table AND index_name=p_index
    ) THEN
        SET @q = CONCAT(
            'ALTER TABLE `', p_schema, '`.`', p_table,
            '` DROP INDEX `', p_index, '`'
        );
        PREPARE stmt FROM @q; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

-- 1) cpfDB: 실행 인스턴스 식별자는 transaction_id 하나로 통일한다.
CALL cpf_r4_rename_transaction_column('cpfDB','cpf_transaction_segment','transaction_global_id','transaction_id','NOT NULL');
CALL cpf_r4_drop_column_if_exists('cpfDB','cpf_transaction_segment','root_transaction_global_id');
CALL cpf_r4_drop_column_if_exists('cpfDB','cpf_transaction_segment','parent_transaction_global_id');
CALL cpf_r4_drop_column_if_exists('cpfDB','cpf_transaction_segment','root_transaction_id');
CALL cpf_r4_drop_column_if_exists('cpfDB','cpf_transaction_segment','parent_transaction_id');
CALL cpf_r4_rename_transaction_column('cpfDB','cpf_service_call_history','transaction_global_id','transaction_id','NULL');
CALL cpf_r4_rename_transaction_column('cpfDB','cpf_broker_outbox','transaction_global_id','transaction_id','NULL');
CALL cpf_r4_rename_transaction_column('cpfDB','cpf_broker_dlq','transaction_global_id','transaction_id','NULL');
CALL cpf_r4_rename_transaction_column('cpfDB','cpf_file_transfer_history','transaction_global_id','transaction_id','NULL');
CALL cpf_r4_rename_transaction_column('cpfDB','cpf_unknown_result','transaction_global_id','transaction_id','NULL');

-- 2) BAT: 기존 parent/child 거래 ID는 transactionId 하나 + segment 계층으로 수렴한다.
CALL cpf_r4_rename_transaction_column('batDB','bat_on_demand_request','transaction_global_id','transaction_id','NOT NULL');
CALL cpf_r4_rename_transaction_column('batDB','bat_execution','transaction_global_id','transaction_id','NULL');
CALL cpf_r4_add_column_if_missing('batDB','bat_execution','parent_segment_id','VARCHAR(120) NULL COMMENT ''상위 거래 구간 ID''');
CALL cpf_r4_add_column_if_missing('batDB','bat_execution','transaction_segment_id','VARCHAR(120) NULL COMMENT ''배치 실행 거래 구간 ID''');
CALL cpf_r4_promote_parent_transaction('batDB','bat_execution','parent_transaction_id');
CALL cpf_r4_promote_parent_transaction('batDB','bat_execution','parent_transaction_global_id');
CALL cpf_r4_drop_index_if_exists('batDB','bat_execution','ix_bat_execution_parent_transaction');
CALL cpf_r4_drop_column_if_exists('batDB','bat_execution','parent_transaction_global_id');
CALL cpf_r4_drop_column_if_exists('batDB','bat_execution','parent_transaction_id');

CALL cpf_r4_add_column_if_missing('batDB','bat_center_cut_item','transaction_id','CHAR(34) NULL COMMENT ''센터컷 전체가 승계하는 CPF transactionId''');
CALL cpf_r4_add_column_if_missing('batDB','bat_center_cut_item','parent_segment_id','VARCHAR(120) NULL COMMENT ''상위 거래 구간 ID''');
CALL cpf_r4_add_column_if_missing('batDB','bat_center_cut_item','transaction_segment_id','VARCHAR(120) NULL COMMENT ''센터컷 Item 거래 구간 ID''');
CALL cpf_r4_fill_segment_from_legacy('batDB','bat_center_cut_item','child_transaction_id');
CALL cpf_r4_fill_segment_from_legacy('batDB','bat_center_cut_item','child_transaction_global_id');
CALL cpf_r4_fill_transaction_from_legacy('batDB','bat_center_cut_item','child_transaction_id','parent_transaction_id');
CALL cpf_r4_fill_transaction_from_legacy('batDB','bat_center_cut_item','child_transaction_global_id','parent_transaction_global_id');
CALL cpf_r4_drop_index_if_exists('batDB','bat_center_cut_item','ix_bat_center_cut_item_transaction');
CALL cpf_r4_drop_column_if_exists('batDB','bat_center_cut_item','parent_transaction_global_id');
CALL cpf_r4_drop_column_if_exists('batDB','bat_center_cut_item','child_transaction_global_id');
CALL cpf_r4_drop_column_if_exists('batDB','bat_center_cut_item','parent_transaction_id');
CALL cpf_r4_drop_column_if_exists('batDB','bat_center_cut_item','child_transaction_id');
CALL cpf_r4_add_index_if_missing('batDB','bat_center_cut_item','ix_bat_center_cut_item_transaction','`transaction_id`,`transaction_segment_id`',FALSE);
CALL cpf_r4_add_index_if_missing('batDB','bat_center_cut_item','ix_bat_center_cut_item_parent_segment','`parent_segment_id`',FALSE);

CALL cpf_r4_add_column_if_missing('batDB','bat_center_cut_result','transaction_id','CHAR(34) NULL COMMENT ''센터컷 전체가 승계하는 CPF transactionId''');
CALL cpf_r4_add_column_if_missing('batDB','bat_center_cut_result','parent_segment_id','VARCHAR(120) NULL COMMENT ''상위 거래 구간 ID''');
CALL cpf_r4_add_column_if_missing('batDB','bat_center_cut_result','transaction_segment_id','VARCHAR(120) NULL COMMENT ''센터컷 Result 거래 구간 ID''');
CALL cpf_r4_fill_segment_from_legacy('batDB','bat_center_cut_result','child_transaction_id');
CALL cpf_r4_fill_segment_from_legacy('batDB','bat_center_cut_result','child_transaction_global_id');
CALL cpf_r4_sync_bat_center_cut_result();
CALL cpf_r4_fill_transaction_from_legacy('batDB','bat_center_cut_result','child_transaction_id','parent_transaction_id');
CALL cpf_r4_fill_transaction_from_legacy('batDB','bat_center_cut_result','child_transaction_global_id','parent_transaction_global_id');
CALL cpf_r4_drop_column_if_exists('batDB','bat_center_cut_result','parent_transaction_global_id');
CALL cpf_r4_drop_column_if_exists('batDB','bat_center_cut_result','child_transaction_global_id');
CALL cpf_r4_drop_column_if_exists('batDB','bat_center_cut_result','parent_transaction_id');
CALL cpf_r4_drop_column_if_exists('batDB','bat_center_cut_result','child_transaction_id');
CALL cpf_r4_add_index_if_missing('batDB','bat_center_cut_result','ix_bat_center_cut_result_transaction','`transaction_id`,`transaction_segment_id`',FALSE);
CALL cpf_r4_add_index_if_missing('batDB','bat_center_cut_result','ix_bat_center_cut_result_parent_segment','`parent_segment_id`',FALSE);

-- 3) REF/Generated Reference: 같은 transactionId 계약으로 이관한다.
CALL cpf_r4_rename_transaction_column('refDB','ref_sample_item','transaction_global_id','transaction_id','NULL');

CALL cpf_r4_add_column_if_missing('refDB','ref_center_cut_sample_target','transaction_id','CHAR(34) NULL COMMENT ''CPF transactionId''');
CALL cpf_r4_add_column_if_missing('refDB','ref_center_cut_sample_target','parent_segment_id','VARCHAR(120) NULL COMMENT ''상위 segment ID''');
CALL cpf_r4_add_column_if_missing('refDB','ref_center_cut_sample_target','transaction_segment_id','VARCHAR(120) NULL COMMENT ''현재 segment ID''');
CALL cpf_r4_fill_segment_from_legacy('refDB','ref_center_cut_sample_target','child_transaction_id');
CALL cpf_r4_fill_segment_from_legacy('refDB','ref_center_cut_sample_target','child_transaction_global_id');
CALL cpf_r4_fill_transaction_from_legacy('refDB','ref_center_cut_sample_target','child_transaction_id','parent_transaction_id');
CALL cpf_r4_fill_transaction_from_legacy('refDB','ref_center_cut_sample_target','child_transaction_global_id','parent_transaction_global_id');
CALL cpf_r4_drop_index_if_exists('refDB','ref_center_cut_sample_target','ix_ref_center_cut_sample_target_global');
CALL cpf_r4_drop_column_if_exists('refDB','ref_center_cut_sample_target','parent_transaction_global_id');
CALL cpf_r4_drop_column_if_exists('refDB','ref_center_cut_sample_target','child_transaction_global_id');
CALL cpf_r4_drop_column_if_exists('refDB','ref_center_cut_sample_target','parent_transaction_id');
CALL cpf_r4_drop_column_if_exists('refDB','ref_center_cut_sample_target','child_transaction_id');
CALL cpf_r4_add_index_if_missing('refDB','ref_center_cut_sample_target','ix_ref_center_cut_sample_target_transaction','`transaction_id`,`transaction_segment_id`',FALSE);

CALL cpf_r4_add_column_if_missing('refDB','ref_center_cut_sample_result','transaction_id','CHAR(34) NULL COMMENT ''CPF transactionId''');
CALL cpf_r4_add_column_if_missing('refDB','ref_center_cut_sample_result','parent_segment_id','VARCHAR(120) NULL COMMENT ''상위 segment ID''');
CALL cpf_r4_add_column_if_missing('refDB','ref_center_cut_sample_result','transaction_segment_id','VARCHAR(120) NULL COMMENT ''현재 segment ID''');
CALL cpf_r4_fill_segment_from_legacy('refDB','ref_center_cut_sample_result','child_transaction_id');
CALL cpf_r4_fill_segment_from_legacy('refDB','ref_center_cut_sample_result','child_transaction_global_id');
CALL cpf_r4_fill_transaction_from_legacy('refDB','ref_center_cut_sample_result','child_transaction_id','parent_transaction_id');
CALL cpf_r4_fill_transaction_from_legacy('refDB','ref_center_cut_sample_result','child_transaction_global_id','parent_transaction_global_id');
CALL cpf_r4_sync_ref_center_cut_result();
CALL cpf_r4_drop_index_if_exists('refDB','ref_center_cut_sample_result','ix_ref_center_cut_sample_result_global');
CALL cpf_r4_drop_column_if_exists('refDB','ref_center_cut_sample_result','parent_transaction_global_id');
CALL cpf_r4_drop_column_if_exists('refDB','ref_center_cut_sample_result','child_transaction_global_id');
CALL cpf_r4_drop_column_if_exists('refDB','ref_center_cut_sample_result','parent_transaction_id');
CALL cpf_r4_drop_column_if_exists('refDB','ref_center_cut_sample_result','child_transaction_id');
CALL cpf_r4_add_index_if_missing('refDB','ref_center_cut_sample_result','ix_ref_center_cut_sample_result_transaction','`transaction_id`,`transaction_segment_id`',FALSE);

CALL cpf_r4_rename_transaction_column('mbrDB','mbr_sample_item','transaction_global_id','transaction_id','NOT NULL');
CALL cpf_r4_add_column_if_missing('mbrDB','mbr_sample_item','transaction_sequence','BIGINT NOT NULL DEFAULT 1 COMMENT ''동일 거래 내 변경 순번''');
CALL cpf_r4_add_column_if_missing('mbrDB','mbr_sample_item','transaction_at','DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT ''마지막 거래 시각''');
CALL cpf_r4_add_column_if_missing('mbrDB','mbr_sample_item','deleted_yn','CHAR(1) NOT NULL DEFAULT ''N'' COMMENT ''논리 삭제 여부''');

-- 4) BZA: 단일 transactionId + 조직 책임/승인 Snapshot/감사/첨부 Governance.
CALL cpf_r4_rename_transaction_column('bzaDB','bza_login_history','transaction_global_id','transaction_id','NULL');
CALL cpf_r4_rename_transaction_column('bzaDB','bza_refresh_token','transaction_global_id','transaction_id','NULL');
CALL cpf_r4_rename_transaction_column('bzaDB','bza_business_audit','transaction_global_id','transaction_id','NULL');
CALL cpf_r4_rename_transaction_column('bzaDB','bza_download_audit','transaction_global_id','transaction_id','NULL');
CALL cpf_r4_rename_transaction_column('bzaDB','bza_approval_document','transaction_global_id','transaction_id','NULL');
CALL cpf_r4_rename_transaction_column('bzaDB','bza_approval_history','transaction_global_id','transaction_id','NULL');

CREATE TABLE IF NOT EXISTS bzaDB.bza_organization_responsibility (
    responsibility_id BIGINT NOT NULL AUTO_INCREMENT,
    organization_code VARCHAR(50) NOT NULL,
    responsibility_type VARCHAR(30) NOT NULL DEFAULT 'MANAGER',
    employee_no VARCHAR(50) NOT NULL,
    effective_from DATETIME(3) NOT NULL,
    effective_to DATETIME(3) NULL,
    priority_no INT NOT NULL DEFAULT 1,
    use_yn CHAR(1) NOT NULL DEFAULT 'Y',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (responsibility_id),
    INDEX ix_bza_org_responsibility_active (organization_code, responsibility_type, use_yn, effective_to, priority_no),
    INDEX ix_bza_org_responsibility_employee (employee_no, use_yn, effective_to),
    CONSTRAINT fk_bza_org_responsibility_org FOREIGN KEY (organization_code) REFERENCES bzaDB.bza_organization(organization_code),
    CONSTRAINT fk_bza_org_responsibility_employee FOREIGN KEY (employee_no) REFERENCES bzaDB.bza_employee(employee_no),
    CONSTRAINT ck_bza_org_responsibility_type CHECK (responsibility_type IN ('MANAGER','DEPUTY','ACTING','APPROVAL_OWNER')),
    CONSTRAINT ck_bza_org_responsibility_use CHECK (use_yn IN ('Y','N')),
    CONSTRAINT ck_bza_org_responsibility_priority CHECK (priority_no >= 1),
    CONSTRAINT ck_bza_org_responsibility_effective CHECK (effective_to IS NULL OR effective_to > effective_from)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CALL cpf_r4_add_column_if_missing('bzaDB','bza_business_audit','previous_record_hash','CHAR(64) NULL');
CALL cpf_r4_add_column_if_missing('bzaDB','bza_business_audit','record_hash','CHAR(64) NULL');
CALL cpf_r4_add_column_if_missing('bzaDB','bza_attachment','data_classification','VARCHAR(30) NOT NULL DEFAULT ''INTERNAL''');
CALL cpf_r4_add_column_if_missing('bzaDB','bza_attachment','retention_until','DATETIME(3) NULL');
CALL cpf_r4_add_column_if_missing('bzaDB','bza_attachment','quarantine_yn','CHAR(1) NOT NULL DEFAULT ''N''');
CALL cpf_r4_add_column_if_missing('bzaDB','bza_approval_document','policy_snapshot_json','LONGTEXT NULL');
CALL cpf_r4_add_column_if_missing('bzaDB','bza_approval_document','payload_hash','CHAR(64) NULL');
CALL cpf_r4_add_column_if_missing('bzaDB','bza_approval_document','request_idempotency_key','VARCHAR(120) NULL');
CALL cpf_r4_add_column_if_missing('bzaDB','bza_approval_document','submitted_at','DATETIME(3) NULL');
CALL cpf_r4_add_column_if_missing('bzaDB','bza_approval_document','completed_at','DATETIME(3) NULL');
CALL cpf_r4_add_column_if_missing('bzaDB','bza_approval_participant','approver_name_snapshot','VARCHAR(100) NULL');
CALL cpf_r4_add_column_if_missing('bzaDB','bza_approval_participant','resolution_source','VARCHAR(30) NOT NULL DEFAULT ''DIRECT''');

CALL cpf_r4_add_index_if_missing('bzaDB','bza_business_audit','ix_bza_business_audit_transaction','`transaction_id`,`created_at`',FALSE);
CALL cpf_r4_add_index_if_missing('bzaDB','bza_approval_document','ix_bza_approval_document_transaction','`transaction_id`,`created_at`',FALSE);
CALL cpf_r4_add_index_if_missing('bzaDB','bza_approval_document','uk_bza_approval_document_idempotency','`request_idempotency_key`',TRUE);
CALL cpf_r4_add_index_if_missing('bzaDB','bza_attachment','ix_bza_attachment_retention','`quarantine_yn`,`retention_until`',FALSE);

DROP PROCEDURE IF EXISTS cpf_r4_assert_column_max_length;
DROP PROCEDURE IF EXISTS cpf_r4_rename_transaction_column;
DROP PROCEDURE IF EXISTS cpf_r4_add_column_if_missing;
DROP PROCEDURE IF EXISTS cpf_r4_drop_column_if_exists;
DROP PROCEDURE IF EXISTS cpf_r4_fill_transaction_from_legacy;
DROP PROCEDURE IF EXISTS cpf_r4_fill_segment_from_legacy;
DROP PROCEDURE IF EXISTS cpf_r4_promote_parent_transaction;
DROP PROCEDURE IF EXISTS cpf_r4_sync_bat_center_cut_result;
DROP PROCEDURE IF EXISTS cpf_r4_sync_ref_center_cut_result;
DROP PROCEDURE IF EXISTS cpf_r4_add_index_if_missing;
DROP PROCEDURE IF EXISTS cpf_r4_drop_index_if_exists;
