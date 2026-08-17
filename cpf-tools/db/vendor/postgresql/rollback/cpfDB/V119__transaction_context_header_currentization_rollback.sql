DROP INDEX IF EXISTS ix_cpf_transaction_log_target_operation;
DROP INDEX IF EXISTS ix_cpf_transaction_log_system_time;
DROP INDEX IF EXISTS ix_cpf_transaction_log_client;
ALTER TABLE cpf_transaction_log DROP COLUMN target_operation_id;
ALTER TABLE cpf_transaction_log DROP COLUMN target_system_code;
ALTER TABLE cpf_transaction_log RENAME COLUMN client_id TO client_app_id;
ALTER TABLE cpf_transaction_log RENAME COLUMN caller_system_code TO caller_service;
ALTER TABLE cpf_transaction_log RENAME COLUMN original_system_code TO original_channel_code;
ALTER TABLE cpf_transaction_log RENAME COLUMN system_code TO channel_code;
CREATE INDEX ix_cpf_transaction_log_client_app ON cpf_transaction_log (client_app_id, start_time);
CREATE INDEX ix_cpf_transaction_log_channel_time ON cpf_transaction_log (channel_code, start_time);

DROP INDEX IF EXISTS ix_cpf_transaction_segment_target_operation;
DROP INDEX IF EXISTS ix_cpf_transaction_segment_client_system;
ALTER TABLE cpf_transaction_segment DROP COLUMN target_operation_id;
ALTER TABLE cpf_transaction_segment DROP COLUMN target_system_code;
ALTER TABLE cpf_transaction_segment RENAME COLUMN client_id TO client_app_id;
ALTER TABLE cpf_transaction_segment RENAME COLUMN caller_system_code TO caller_service;
ALTER TABLE cpf_transaction_segment RENAME COLUMN original_system_code TO original_channel_code;
ALTER TABLE cpf_transaction_segment RENAME COLUMN system_code TO channel_code;
CREATE INDEX ix_cpf_transaction_segment_client ON cpf_transaction_segment (client_app_id, caller_service, started_at);

ALTER TABLE cpf_transaction_lineage RENAME COLUMN system_code TO channel_code;
ALTER TABLE cpf_transaction_lineage_archive RENAME COLUMN system_code TO channel_code;
