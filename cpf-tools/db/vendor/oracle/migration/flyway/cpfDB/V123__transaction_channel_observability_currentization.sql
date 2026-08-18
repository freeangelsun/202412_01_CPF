-- CPF V123: currentize business transaction observability identity from System Header vocabulary to Channel vocabulary.
ALTER TABLE CPF_TRANSACTION_LOG RENAME COLUMN CALLER_SYSTEM_CODE TO CALLER_CHANNEL;
ALTER TABLE CPF_TRANSACTION_LOG RENAME COLUMN TARGET_SYSTEM_CODE TO TARGET_CHANNEL;
ALTER TABLE CPF_TRANSACTION_LOG RENAME COLUMN ORIGINAL_SYSTEM_CODE TO ORIGINAL_CHANNEL;
ALTER TABLE CPF_TRANSACTION_LOG RENAME COLUMN SYSTEM_CODE TO CURRENT_CHANNEL;
ALTER TABLE CPF_TRANSACTION_LOG ADD (HOST_IP VARCHAR2(128 CHAR));
DROP INDEX ix_cpf_transaction_log_system_time;
DROP INDEX ix_cpf_transaction_log_target_operation;
CREATE INDEX ix_cpf_transaction_log_channel_time ON CPF_TRANSACTION_LOG (CURRENT_CHANNEL, START_TIME);
CREATE INDEX ix_cpf_transaction_log_target_operation ON CPF_TRANSACTION_LOG (TARGET_CHANNEL, TARGET_OPERATION_ID, START_TIME);
ALTER TABLE CPF_TRANSACTION_SEGMENT RENAME COLUMN original_system_code TO original_channel;
ALTER TABLE CPF_TRANSACTION_SEGMENT RENAME COLUMN system_code TO current_channel;
ALTER TABLE CPF_TRANSACTION_SEGMENT RENAME COLUMN caller_system_code TO caller_channel;
ALTER TABLE CPF_TRANSACTION_SEGMENT RENAME COLUMN target_system_code TO target_channel;
DROP INDEX ix_cpf_transaction_segment_client_system; DROP INDEX ix_cpf_transaction_segment_target_operation;
CREATE INDEX ix_cpf_transaction_segment_client_channel ON CPF_TRANSACTION_SEGMENT (client_id, caller_channel, started_at);
CREATE INDEX ix_cpf_transaction_segment_target_operation ON CPF_TRANSACTION_SEGMENT (target_channel, target_operation_id, started_at);
ALTER TABLE CPF_TRANSACTION_LINEAGE RENAME COLUMN system_code TO current_channel; ALTER TABLE CPF_TRANSACTION_LINEAGE RENAME COLUMN remote_system TO target_channel;
ALTER TABLE CPF_TRANSACTION_LINEAGE_ARCHIVE RENAME COLUMN system_code TO current_channel; ALTER TABLE CPF_TRANSACTION_LINEAGE_ARCHIVE RENAME COLUMN remote_system TO target_channel;
