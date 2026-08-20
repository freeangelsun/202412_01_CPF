-- CPF V131: System은 canonical transaction lineage 정본이며 Channel은 선택 Context로 분리합니다.
ALTER TABLE cpf_transaction_log ADD COLUMN caller_system_code VARCHAR(120);
ALTER TABLE cpf_transaction_log ADD COLUMN target_system_code VARCHAR(32);
ALTER TABLE cpf_transaction_log ADD COLUMN original_system_code VARCHAR(20);
ALTER TABLE cpf_transaction_log ADD COLUMN system_code VARCHAR(20);
UPDATE cpf_transaction_log
   SET caller_system_code = COALESCE(caller_system_code, caller_channel),
       target_system_code = COALESCE(target_system_code, target_channel),
       original_system_code = COALESCE(original_system_code, original_channel),
       system_code = COALESCE(system_code, current_channel);
DROP INDEX IF EXISTS ix_cpf_transaction_log_channel_time;
DROP INDEX IF EXISTS ix_cpf_transaction_log_target_operation;
CREATE INDEX ix_cpf_transaction_log_system_time ON cpf_transaction_log (system_code, start_time);
CREATE INDEX ix_cpf_transaction_log_target_operation ON cpf_transaction_log (target_system_code, target_operation_id, start_time);

ALTER TABLE cpf_transaction_segment ADD COLUMN system_code VARCHAR(30);
ALTER TABLE cpf_transaction_segment ADD COLUMN original_system_code VARCHAR(30);
ALTER TABLE cpf_transaction_segment ADD COLUMN caller_system_code VARCHAR(100);
ALTER TABLE cpf_transaction_segment ADD COLUMN target_system_code VARCHAR(32);
UPDATE cpf_transaction_segment
   SET system_code = COALESCE(system_code, current_channel),
       original_system_code = COALESCE(original_system_code, original_channel),
       caller_system_code = COALESCE(caller_system_code, caller_channel),
       target_system_code = COALESCE(target_system_code, target_channel);
DROP INDEX IF EXISTS ix_cpf_transaction_segment_client_channel;
DROP INDEX IF EXISTS ix_cpf_transaction_segment_target_operation;
CREATE INDEX ix_cpf_transaction_segment_client_system ON cpf_transaction_segment (client_id, caller_system_code, started_at);
CREATE INDEX ix_cpf_transaction_segment_target_operation ON cpf_transaction_segment (target_system_code, target_operation_id, started_at);

ALTER TABLE cpf_transaction_lineage ADD COLUMN system_code VARCHAR(64);
ALTER TABLE cpf_transaction_lineage ADD COLUMN target_system_code VARCHAR(128);
UPDATE cpf_transaction_lineage
   SET system_code = COALESCE(system_code, current_channel),
       target_system_code = COALESCE(target_system_code, target_channel);
ALTER TABLE cpf_transaction_lineage_archive ADD COLUMN system_code VARCHAR(64);
ALTER TABLE cpf_transaction_lineage_archive ADD COLUMN target_system_code VARCHAR(128);
UPDATE cpf_transaction_lineage_archive
   SET system_code = COALESCE(system_code, current_channel),
       target_system_code = COALESCE(target_system_code, target_channel);
