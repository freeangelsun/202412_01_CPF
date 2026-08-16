USE admDB;
SET @table_count := (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='adm_approval_policy_history');
SET @index_count := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='adm_approval_policy_history' AND index_name='ix_adm_approval_policy_history_policy');
SET @column_count := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='adm_approval_policy_history' AND column_name IN ('POLICY_HISTORY_ID','POLICY_CODE','POLICY_VERSION','CHANGE_TYPE','CHANGE_REASON','BEFORE_HASH','AFTER_HASH','OPERATOR_ID','CREATED_AT'));
SET @assertion := IF(@table_count=1 AND @index_count>=1 AND @column_count=9,
  'SELECT ''PASS adm_approval_policy_history'' AS result',
  'SIGNAL SQLSTATE ''45000'' SET MESSAGE_TEXT = ''CPF R6 approval policy history schema verification failed''');
PREPARE cpf_r6_assert FROM @assertion;
EXECUTE cpf_r6_assert;
DEALLOCATE PREPARE cpf_r6_assert;
