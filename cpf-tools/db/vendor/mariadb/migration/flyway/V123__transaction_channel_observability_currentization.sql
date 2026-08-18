-- CPF V123: currentize business transaction observability identity from System Header vocabulary to Channel vocabulary.
ALTER TABLE CPF_TRANSACTION_LOG
  CHANGE COLUMN CALLER_SYSTEM_CODE CALLER_CHANNEL VARCHAR(120) NULL,
  CHANGE COLUMN TARGET_SYSTEM_CODE TARGET_CHANNEL VARCHAR(32) NULL,
  CHANGE COLUMN ORIGINAL_SYSTEM_CODE ORIGINAL_CHANNEL VARCHAR(20) NULL,
  CHANGE COLUMN SYSTEM_CODE CURRENT_CHANNEL VARCHAR(20) NULL,
  ADD COLUMN HOST_IP VARCHAR(128) NULL AFTER HOST_NAME;
DROP INDEX ix_cpf_transaction_log_system_time ON CPF_TRANSACTION_LOG;
DROP INDEX ix_cpf_transaction_log_target_operation ON CPF_TRANSACTION_LOG;
CREATE INDEX ix_cpf_transaction_log_channel_time ON CPF_TRANSACTION_LOG (CURRENT_CHANNEL, START_TIME);
CREATE INDEX ix_cpf_transaction_log_target_operation ON CPF_TRANSACTION_LOG (TARGET_CHANNEL, TARGET_OPERATION_ID, START_TIME);
ALTER TABLE CPF_TRANSACTION_SEGMENT
  CHANGE COLUMN original_system_code original_channel VARCHAR(30) NULL,
  CHANGE COLUMN system_code current_channel VARCHAR(30) NULL,
  CHANGE COLUMN caller_system_code caller_channel VARCHAR(100) NULL,
  CHANGE COLUMN target_system_code target_channel VARCHAR(32) NULL;
DROP INDEX ix_cpf_transaction_segment_client_system ON CPF_TRANSACTION_SEGMENT;
DROP INDEX ix_cpf_transaction_segment_target_operation ON CPF_TRANSACTION_SEGMENT;
CREATE INDEX ix_cpf_transaction_segment_client_channel ON CPF_TRANSACTION_SEGMENT (client_id, caller_channel, started_at);
CREATE INDEX ix_cpf_transaction_segment_target_operation ON CPF_TRANSACTION_SEGMENT (target_channel, target_operation_id, started_at);
ALTER TABLE CPF_TRANSACTION_LINEAGE
  CHANGE COLUMN system_code current_channel VARCHAR(64) NULL,
  CHANGE COLUMN remote_system target_channel VARCHAR(128) NULL;
ALTER TABLE CPF_TRANSACTION_LINEAGE_ARCHIVE
  CHANGE COLUMN system_code current_channel VARCHAR(64) NULL,
  CHANGE COLUMN remote_system target_channel VARCHAR(128) NULL;
