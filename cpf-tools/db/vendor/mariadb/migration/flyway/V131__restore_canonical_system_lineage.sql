-- CPF V131: System은 canonical transaction lineage 정본이며 Channel은 선택 Context로 분리합니다.
-- V123이 과도기적으로 System 컬럼을 Channel 명칭으로 rename한 설치본을 안전하게 currentize합니다.
ALTER TABLE CPF_TRANSACTION_LOG
  ADD COLUMN CALLER_SYSTEM_CODE VARCHAR(120) NULL AFTER CLIENT_VERSION,
  ADD COLUMN TARGET_SYSTEM_CODE VARCHAR(32) NULL AFTER CALLER_SYSTEM_CODE,
  ADD COLUMN ORIGINAL_SYSTEM_CODE VARCHAR(20) NULL AFTER TARGET_SYSTEM_CODE,
  ADD COLUMN SYSTEM_CODE VARCHAR(20) NULL AFTER ORIGINAL_SYSTEM_CODE;
UPDATE CPF_TRANSACTION_LOG
   SET CALLER_SYSTEM_CODE = COALESCE(CALLER_SYSTEM_CODE, CALLER_CHANNEL),
       TARGET_SYSTEM_CODE = COALESCE(TARGET_SYSTEM_CODE, TARGET_CHANNEL),
       ORIGINAL_SYSTEM_CODE = COALESCE(ORIGINAL_SYSTEM_CODE, ORIGINAL_CHANNEL),
       SYSTEM_CODE = COALESCE(SYSTEM_CODE, CURRENT_CHANNEL);
DROP INDEX ix_cpf_transaction_log_channel_time ON CPF_TRANSACTION_LOG;
DROP INDEX ix_cpf_transaction_log_target_operation ON CPF_TRANSACTION_LOG;
CREATE INDEX ix_cpf_transaction_log_system_time ON CPF_TRANSACTION_LOG (SYSTEM_CODE, START_TIME);
CREATE INDEX ix_cpf_transaction_log_target_operation ON CPF_TRANSACTION_LOG (TARGET_SYSTEM_CODE, TARGET_OPERATION_ID, START_TIME);

ALTER TABLE CPF_TRANSACTION_SEGMENT
  ADD COLUMN system_code VARCHAR(30) NULL AFTER operator_id_masked,
  ADD COLUMN original_system_code VARCHAR(30) NULL AFTER system_code,
  ADD COLUMN caller_system_code VARCHAR(100) NULL AFTER original_system_code,
  ADD COLUMN target_system_code VARCHAR(32) NULL AFTER caller_system_code;
UPDATE CPF_TRANSACTION_SEGMENT
   SET system_code = COALESCE(system_code, current_channel),
       original_system_code = COALESCE(original_system_code, original_channel),
       caller_system_code = COALESCE(caller_system_code, caller_channel),
       target_system_code = COALESCE(target_system_code, target_channel);
DROP INDEX ix_cpf_transaction_segment_client_channel ON CPF_TRANSACTION_SEGMENT;
DROP INDEX ix_cpf_transaction_segment_target_operation ON CPF_TRANSACTION_SEGMENT;
CREATE INDEX ix_cpf_transaction_segment_client_system ON CPF_TRANSACTION_SEGMENT (client_id, caller_system_code, started_at);
CREATE INDEX ix_cpf_transaction_segment_target_operation ON CPF_TRANSACTION_SEGMENT (target_system_code, target_operation_id, started_at);

ALTER TABLE CPF_TRANSACTION_LINEAGE
  ADD COLUMN system_code VARCHAR(64) NULL AFTER tenant_id,
  ADD COLUMN target_system_code VARCHAR(128) NULL AFTER system_code;
UPDATE CPF_TRANSACTION_LINEAGE
   SET system_code = COALESCE(system_code, current_channel),
       target_system_code = COALESCE(target_system_code, target_channel);
ALTER TABLE CPF_TRANSACTION_LINEAGE_ARCHIVE
  ADD COLUMN system_code VARCHAR(64) NULL AFTER tenant_id,
  ADD COLUMN target_system_code VARCHAR(128) NULL AFTER system_code;
UPDATE CPF_TRANSACTION_LINEAGE_ARCHIVE
   SET system_code = COALESCE(system_code, current_channel),
       target_system_code = COALESCE(target_system_code, target_channel);
