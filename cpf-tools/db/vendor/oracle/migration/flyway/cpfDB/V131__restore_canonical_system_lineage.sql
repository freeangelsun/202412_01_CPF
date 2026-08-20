-- CPF V131: System은 canonical transaction lineage 정본이며 Channel은 선택 Context로 분리합니다.
ALTER TABLE CPF_TRANSACTION_LOG ADD (
  CALLER_SYSTEM_CODE VARCHAR2(120 CHAR), TARGET_SYSTEM_CODE VARCHAR2(32 CHAR),
  ORIGINAL_SYSTEM_CODE VARCHAR2(20 CHAR), SYSTEM_CODE VARCHAR2(20 CHAR));
UPDATE CPF_TRANSACTION_LOG
   SET CALLER_SYSTEM_CODE = COALESCE(CALLER_SYSTEM_CODE, CALLER_CHANNEL),
       TARGET_SYSTEM_CODE = COALESCE(TARGET_SYSTEM_CODE, TARGET_CHANNEL),
       ORIGINAL_SYSTEM_CODE = COALESCE(ORIGINAL_SYSTEM_CODE, ORIGINAL_CHANNEL),
       SYSTEM_CODE = COALESCE(SYSTEM_CODE, CURRENT_CHANNEL);
DROP INDEX ix_cpf_transaction_log_channel_time;
DROP INDEX ix_cpf_transaction_log_target_operation;
CREATE INDEX ix_cpf_transaction_log_system_time ON CPF_TRANSACTION_LOG (SYSTEM_CODE, START_TIME);
CREATE INDEX ix_cpf_transaction_log_target_operation ON CPF_TRANSACTION_LOG (TARGET_SYSTEM_CODE, TARGET_OPERATION_ID, START_TIME);

ALTER TABLE CPF_TRANSACTION_SEGMENT ADD (
  system_code VARCHAR2(30 CHAR), original_system_code VARCHAR2(30 CHAR),
  caller_system_code VARCHAR2(100 CHAR), target_system_code VARCHAR2(32 CHAR));
UPDATE CPF_TRANSACTION_SEGMENT
   SET system_code = COALESCE(system_code, current_channel),
       original_system_code = COALESCE(original_system_code, original_channel),
       caller_system_code = COALESCE(caller_system_code, caller_channel),
       target_system_code = COALESCE(target_system_code, target_channel);
DROP INDEX ix_cpf_transaction_segment_client_channel;
DROP INDEX ix_cpf_transaction_segment_target_operation;
CREATE INDEX ix_cpf_transaction_segment_client_system ON CPF_TRANSACTION_SEGMENT (client_id, caller_system_code, started_at);
CREATE INDEX ix_cpf_transaction_segment_target_operation ON CPF_TRANSACTION_SEGMENT (target_system_code, target_operation_id, started_at);

ALTER TABLE CPF_TRANSACTION_LINEAGE ADD (system_code VARCHAR2(64 CHAR), target_system_code VARCHAR2(128 CHAR));
UPDATE CPF_TRANSACTION_LINEAGE
   SET system_code = COALESCE(system_code, current_channel),
       target_system_code = COALESCE(target_system_code, target_channel);
ALTER TABLE CPF_TRANSACTION_LINEAGE_ARCHIVE ADD (system_code VARCHAR2(64 CHAR), target_system_code VARCHAR2(128 CHAR));
UPDATE CPF_TRANSACTION_LINEAGE_ARCHIVE
   SET system_code = COALESCE(system_code, current_channel),
       target_system_code = COALESCE(target_system_code, target_channel);
