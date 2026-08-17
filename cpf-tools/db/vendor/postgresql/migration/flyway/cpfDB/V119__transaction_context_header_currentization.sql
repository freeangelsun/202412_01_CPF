-- CPF transaction/header canonical naming currentization. Historical columns are renamed in place; data is preserved.
ALTER TABLE cpf_transaction_log RENAME COLUMN client_app_id TO client_id;
ALTER TABLE cpf_transaction_log RENAME COLUMN caller_service TO caller_system_code;
ALTER TABLE cpf_transaction_log RENAME COLUMN original_channel_code TO original_system_code;
ALTER TABLE cpf_transaction_log RENAME COLUMN channel_code TO system_code;
ALTER TABLE cpf_transaction_log ADD COLUMN target_system_code VARCHAR(32);
ALTER TABLE cpf_transaction_log ADD COLUMN target_operation_id VARCHAR(160);
COMMENT ON COLUMN cpf_transaction_log.client_id IS '클라이언트/Application 식별자';
COMMENT ON COLUMN cpf_transaction_log.caller_system_code IS '직전 호출 시스템 코드';
COMMENT ON COLUMN cpf_transaction_log.original_system_code IS '최초 거래 발생 시스템 코드';
COMMENT ON COLUMN cpf_transaction_log.system_code IS '현재 요청 처리 시스템 코드';
COMMENT ON COLUMN cpf_transaction_log.target_system_code IS '현재 호출 대상 시스템 코드';
COMMENT ON COLUMN cpf_transaction_log.target_operation_id IS '호출 대상 Canonical operationId';
DROP INDEX IF EXISTS ix_cpf_transaction_log_client_app;
DROP INDEX IF EXISTS ix_cpf_transaction_log_channel_time;
CREATE INDEX ix_cpf_transaction_log_client ON cpf_transaction_log (client_id, start_time);
CREATE INDEX ix_cpf_transaction_log_system_time ON cpf_transaction_log (system_code, start_time);
CREATE INDEX ix_cpf_transaction_log_target_operation ON cpf_transaction_log (target_system_code, target_operation_id, start_time);

ALTER TABLE cpf_transaction_segment RENAME COLUMN client_app_id TO client_id;
ALTER TABLE cpf_transaction_segment RENAME COLUMN caller_service TO caller_system_code;
ALTER TABLE cpf_transaction_segment RENAME COLUMN original_channel_code TO original_system_code;
ALTER TABLE cpf_transaction_segment RENAME COLUMN channel_code TO system_code;
ALTER TABLE cpf_transaction_segment ADD COLUMN target_system_code VARCHAR(32);
ALTER TABLE cpf_transaction_segment ADD COLUMN target_operation_id VARCHAR(160);
COMMENT ON COLUMN cpf_transaction_segment.client_id IS '클라이언트/Application 식별자';
COMMENT ON COLUMN cpf_transaction_segment.caller_system_code IS '직전 호출 시스템 코드';
COMMENT ON COLUMN cpf_transaction_segment.original_system_code IS '최초 거래 발생 시스템 코드';
COMMENT ON COLUMN cpf_transaction_segment.system_code IS '현재 요청 처리 시스템 코드';
COMMENT ON COLUMN cpf_transaction_segment.target_system_code IS '현재 호출 대상 시스템 코드';
COMMENT ON COLUMN cpf_transaction_segment.target_operation_id IS '호출 대상 Canonical operationId';
DROP INDEX IF EXISTS ix_cpf_transaction_segment_client;
CREATE INDEX ix_cpf_transaction_segment_client_system ON cpf_transaction_segment (client_id, caller_system_code, started_at);
CREATE INDEX ix_cpf_transaction_segment_target_operation ON cpf_transaction_segment (target_system_code, target_operation_id, started_at);

ALTER TABLE cpf_transaction_lineage RENAME COLUMN channel_code TO system_code;
ALTER TABLE cpf_transaction_lineage_archive RENAME COLUMN channel_code TO system_code;
COMMENT ON COLUMN cpf_transaction_lineage.system_code IS '현재 처리 System 코드';
COMMENT ON COLUMN cpf_transaction_lineage_archive.system_code IS '현재 처리 System 코드';
