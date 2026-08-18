-- CPF V123 rollback: restore previous System vocabulary while preserving data.
ALTER TABLE CPF_TRANSACTION_LINEAGE_ARCHIVE CHANGE COLUMN target_channel remote_system VARCHAR(128) NULL, CHANGE COLUMN current_channel system_code VARCHAR(64) NULL;
ALTER TABLE CPF_TRANSACTION_LINEAGE CHANGE COLUMN target_channel remote_system VARCHAR(128) NULL, CHANGE COLUMN current_channel system_code VARCHAR(64) NULL;
DROP INDEX ix_cpf_transaction_segment_client_channel ON CPF_TRANSACTION_SEGMENT;
DROP INDEX ix_cpf_transaction_segment_target_operation ON CPF_TRANSACTION_SEGMENT;
ALTER TABLE CPF_TRANSACTION_SEGMENT CHANGE COLUMN target_channel target_system_code VARCHAR(32) NULL, CHANGE COLUMN caller_channel caller_system_code VARCHAR(100) NULL, CHANGE COLUMN current_channel system_code VARCHAR(30) NULL, CHANGE COLUMN original_channel original_system_code VARCHAR(30) NULL;
CREATE INDEX ix_cpf_transaction_segment_client_system ON CPF_TRANSACTION_SEGMENT (client_id, caller_system_code, started_at);
CREATE INDEX ix_cpf_transaction_segment_target_operation ON CPF_TRANSACTION_SEGMENT (target_system_code, target_operation_id, started_at);
DROP INDEX ix_cpf_transaction_log_channel_time ON CPF_TRANSACTION_LOG;
DROP INDEX ix_cpf_transaction_log_target_operation ON CPF_TRANSACTION_LOG;
ALTER TABLE CPF_TRANSACTION_LOG DROP COLUMN HOST_IP, CHANGE COLUMN CURRENT_CHANNEL SYSTEM_CODE VARCHAR(20) NULL, CHANGE COLUMN ORIGINAL_CHANNEL ORIGINAL_SYSTEM_CODE VARCHAR(20) NULL, CHANGE COLUMN TARGET_CHANNEL TARGET_SYSTEM_CODE VARCHAR(32) NULL, CHANGE COLUMN CALLER_CHANNEL CALLER_SYSTEM_CODE VARCHAR(120) NULL;
CREATE INDEX ix_cpf_transaction_log_system_time ON CPF_TRANSACTION_LOG (SYSTEM_CODE, START_TIME);
CREATE INDEX ix_cpf_transaction_log_target_operation ON CPF_TRANSACTION_LOG (TARGET_SYSTEM_CODE, TARGET_OPERATION_ID, START_TIME);
