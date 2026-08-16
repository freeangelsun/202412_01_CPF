ALTER TABLE cpf_batch_approved_launch
  DROP CONSTRAINT ck_cpf_bat_approval_version;
ALTER TABLE cpf_batch_execution_link
  DROP FOREIGN KEY fk_cpf_bat_exec_link,
  DROP CONSTRAINT ck_cpf_bat_link_fencing,
  ADD CONSTRAINT FK_CPF_BAT_EXEC_LINK FOREIGN KEY (cpf_execution_id)
    REFERENCES cpf_batch_execution_control(cpf_execution_id);
ALTER TABLE cpf_batch_execution_control
  DROP CONSTRAINT ck_cpf_bat_control_status;

SET @cpf_sql = IF(
    @@lower_case_table_names = 0
    AND EXISTS (SELECT 1 FROM information_schema.tables
                 WHERE table_schema = DATABASE() AND BINARY table_name = 'cpf_batch_approved_launch')
    AND NOT EXISTS (SELECT 1 FROM information_schema.tables
                     WHERE table_schema = DATABASE() AND BINARY table_name = 'CPF_BATCH_APPROVED_LAUNCH'),
    'RENAME TABLE cpf_batch_approved_launch TO CPF_BATCH_APPROVED_LAUNCH',
    'SELECT 1');
PREPARE cpf_stmt FROM @cpf_sql;
EXECUTE cpf_stmt;
DEALLOCATE PREPARE cpf_stmt;

SET @cpf_sql = IF(
    @@lower_case_table_names = 0
    AND EXISTS (SELECT 1 FROM information_schema.tables
                 WHERE table_schema = DATABASE() AND BINARY table_name = 'cpf_batch_execution_link')
    AND NOT EXISTS (SELECT 1 FROM information_schema.tables
                     WHERE table_schema = DATABASE() AND BINARY table_name = 'CPF_BATCH_EXECUTION_LINK'),
    'RENAME TABLE cpf_batch_execution_link TO CPF_BATCH_EXECUTION_LINK',
    'SELECT 1');
PREPARE cpf_stmt FROM @cpf_sql;
EXECUTE cpf_stmt;
DEALLOCATE PREPARE cpf_stmt;

SET @cpf_sql = IF(
    @@lower_case_table_names = 0
    AND EXISTS (SELECT 1 FROM information_schema.tables
                 WHERE table_schema = DATABASE() AND BINARY table_name = 'cpf_batch_execution_control')
    AND NOT EXISTS (SELECT 1 FROM information_schema.tables
                     WHERE table_schema = DATABASE() AND BINARY table_name = 'CPF_BATCH_EXECUTION_CONTROL'),
    'RENAME TABLE cpf_batch_execution_control TO CPF_BATCH_EXECUTION_CONTROL',
    'SELECT 1');
PREPARE cpf_stmt FROM @cpf_sql;
EXECUTE cpf_stmt;
DEALLOCATE PREPARE cpf_stmt;
SET @cpf_sql = NULL;
