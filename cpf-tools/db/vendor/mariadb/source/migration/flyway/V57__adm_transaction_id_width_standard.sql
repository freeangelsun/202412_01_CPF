-- ADM Runtime transactionId를 CPF 34자리 단일 정본으로 통일합니다.
-- 기존 값이 34자를 초과하면 strict ALTER가 실패하므로 값 절단 없이 migration이 중단됩니다.
USE admDB;

SET @cpf_previous_sql_mode = @@SESSION.sql_mode;
SET SESSION sql_mode = CONCAT_WS(',', @@SESSION.sql_mode, 'STRICT_ALL_TABLES');

ALTER TABLE adm_audit_log
    MODIFY COLUMN TRANSACTION_ID CHAR(34) NULL COMMENT 'CPF 전역 transactionId';

ALTER TABLE adm_audit_delivery
    MODIFY COLUMN TRANSACTION_ID CHAR(34) NOT NULL COMMENT 'CPF 전역 transactionId';

SET SESSION sql_mode = @cpf_previous_sql_mode;
SET @cpf_previous_sql_mode = NULL;
