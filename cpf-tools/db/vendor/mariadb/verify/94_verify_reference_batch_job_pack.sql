SET @cpf_ref_bat_table_count := (
  SELECT COUNT(*) FROM information_schema.tables
   WHERE table_schema = DATABASE()
     AND table_name IN ('CPF_REF_BAT_JOB_EXECUTION','CPF_REF_BAT_CHECKPOINT','CPF_REF_BAT_TARGET_RESULT'));
SET @cpf_ref_bat_assert := IF(@cpf_ref_bat_table_count = 3, 'SELECT 1',
  'SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'CPF REF Batch table count mismatch'');
PREPARE cpf_ref_bat_stmt FROM @cpf_ref_bat_assert;
EXECUTE cpf_ref_bat_stmt;
DEALLOCATE PREPARE cpf_ref_bat_stmt;
