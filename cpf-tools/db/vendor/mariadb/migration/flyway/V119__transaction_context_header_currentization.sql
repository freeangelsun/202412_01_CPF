-- CPF transaction/header canonical naming currentization. Historical columns are renamed in place; data is preserved.
ALTER TABLE CPF_TRANSACTION_LOG
    CHANGE COLUMN CLIENT_APP_ID CLIENT_ID VARCHAR(80) NULL COMMENT '클라이언트/Application 식별자',
    CHANGE COLUMN CALLER_SERVICE CALLER_SYSTEM_CODE VARCHAR(120) NULL COMMENT '직전 호출 시스템 코드',
    CHANGE COLUMN ORIGINAL_CHANNEL_CODE ORIGINAL_SYSTEM_CODE VARCHAR(20) NULL COMMENT '최초 거래 발생 시스템 코드',
    CHANGE COLUMN CHANNEL_CODE SYSTEM_CODE VARCHAR(20) NULL COMMENT '현재 요청 처리 시스템 코드',
    ADD COLUMN TARGET_SYSTEM_CODE VARCHAR(32) NULL COMMENT '현재 호출 대상 시스템 코드' AFTER CALLER_SYSTEM_CODE,
    ADD COLUMN TARGET_OPERATION_ID VARCHAR(160) NULL COMMENT '호출 대상 Canonical operationId' AFTER TARGET_SYSTEM_CODE;
DROP INDEX ix_cpf_transaction_log_client_app ON CPF_TRANSACTION_LOG;
DROP INDEX ix_cpf_transaction_log_channel_time ON CPF_TRANSACTION_LOG;
CREATE INDEX ix_cpf_transaction_log_client ON CPF_TRANSACTION_LOG (CLIENT_ID, START_TIME);
CREATE INDEX ix_cpf_transaction_log_system_time ON CPF_TRANSACTION_LOG (SYSTEM_CODE, START_TIME);
CREATE INDEX ix_cpf_transaction_log_target_operation ON CPF_TRANSACTION_LOG (TARGET_SYSTEM_CODE, TARGET_OPERATION_ID, START_TIME);

ALTER TABLE CPF_TRANSACTION_SEGMENT
    CHANGE COLUMN client_app_id client_id VARCHAR(100) NULL COMMENT '클라이언트/Application 식별자',
    CHANGE COLUMN caller_service caller_system_code VARCHAR(100) NULL COMMENT '직전 호출 시스템 코드',
    CHANGE COLUMN original_channel_code original_system_code VARCHAR(30) NULL COMMENT '최초 거래 발생 시스템 코드',
    CHANGE COLUMN channel_code system_code VARCHAR(30) NULL COMMENT '현재 요청 처리 시스템 코드',
    ADD COLUMN target_system_code VARCHAR(32) NULL COMMENT '현재 호출 대상 시스템 코드' AFTER caller_system_code,
    ADD COLUMN target_operation_id VARCHAR(160) NULL COMMENT '호출 대상 Canonical operationId' AFTER target_system_code;
DROP INDEX ix_cpf_transaction_segment_client ON CPF_TRANSACTION_SEGMENT;
CREATE INDEX ix_cpf_transaction_segment_client_system ON CPF_TRANSACTION_SEGMENT (client_id, caller_system_code, started_at);
CREATE INDEX ix_cpf_transaction_segment_target_operation ON CPF_TRANSACTION_SEGMENT (target_system_code, target_operation_id, started_at);

ALTER TABLE CPF_TRANSACTION_LINEAGE
    CHANGE COLUMN channel_code system_code VARCHAR(64) NULL COMMENT '현재 처리 System 코드';
ALTER TABLE CPF_TRANSACTION_LINEAGE_ARCHIVE
    CHANGE COLUMN channel_code system_code VARCHAR(64) NULL COMMENT '현재 처리 System 코드';
