-- CPF platform identifiers are lowercase. V82 used uppercase spelling, which is
-- physically distinct only when MariaDB lower_case_table_names=0 (Linux).
SET @cpf_sql = IF(
    @@lower_case_table_names = 0
    AND EXISTS (SELECT 1 FROM information_schema.tables
                 WHERE table_schema = DATABASE() AND BINARY table_name = 'CPF_BATCH_EXECUTION_CONTROL')
    AND NOT EXISTS (SELECT 1 FROM information_schema.tables
                     WHERE table_schema = DATABASE() AND BINARY table_name = 'cpf_batch_execution_control'),
    'RENAME TABLE CPF_BATCH_EXECUTION_CONTROL TO cpf_batch_execution_control',
    'SELECT 1');
PREPARE cpf_stmt FROM @cpf_sql;
EXECUTE cpf_stmt;
DEALLOCATE PREPARE cpf_stmt;

SET @cpf_sql = IF(
    @@lower_case_table_names = 0
    AND EXISTS (SELECT 1 FROM information_schema.tables
                 WHERE table_schema = DATABASE() AND BINARY table_name = 'CPF_BATCH_EXECUTION_LINK')
    AND NOT EXISTS (SELECT 1 FROM information_schema.tables
                     WHERE table_schema = DATABASE() AND BINARY table_name = 'cpf_batch_execution_link'),
    'RENAME TABLE CPF_BATCH_EXECUTION_LINK TO cpf_batch_execution_link',
    'SELECT 1');
PREPARE cpf_stmt FROM @cpf_sql;
EXECUTE cpf_stmt;
DEALLOCATE PREPARE cpf_stmt;

SET @cpf_sql = IF(
    @@lower_case_table_names = 0
    AND EXISTS (SELECT 1 FROM information_schema.tables
                 WHERE table_schema = DATABASE() AND BINARY table_name = 'CPF_BATCH_APPROVED_LAUNCH')
    AND NOT EXISTS (SELECT 1 FROM information_schema.tables
                     WHERE table_schema = DATABASE() AND BINARY table_name = 'cpf_batch_approved_launch'),
    'RENAME TABLE CPF_BATCH_APPROVED_LAUNCH TO cpf_batch_approved_launch',
    'SELECT 1');
PREPARE cpf_stmt FROM @cpf_sql;
EXECUTE cpf_stmt;
DEALLOCATE PREPARE cpf_stmt;
SET @cpf_sql = NULL;

ALTER TABLE cpf_batch_execution_control
  ADD CONSTRAINT ck_cpf_bat_control_status CHECK (
    control_status IN ('RESERVED','STARTING','STARTED','STOPPING','STOPPED','COMPLETED','FAILED','UNKNOWN_RESULT','ABANDONED','REJECTED')
  );
ALTER TABLE cpf_batch_execution_link
  ADD CONSTRAINT ck_cpf_bat_link_fencing CHECK (fencing_token > 0),
  DROP FOREIGN KEY FK_CPF_BAT_EXEC_LINK,
  ADD CONSTRAINT fk_cpf_bat_exec_link FOREIGN KEY (cpf_execution_id)
    REFERENCES cpf_batch_execution_control(cpf_execution_id) ON DELETE CASCADE;
ALTER TABLE cpf_batch_approved_launch
  ADD CONSTRAINT ck_cpf_bat_approval_version CHECK (row_version >= 0);
