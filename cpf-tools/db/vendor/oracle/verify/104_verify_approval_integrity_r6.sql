DECLARE
  table_count NUMBER; index_count NUMBER; column_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO table_count FROM user_tables WHERE table_name='ADM_APPROVAL_POLICY_HISTORY';
  SELECT COUNT(*) INTO index_count FROM user_indexes WHERE table_name='ADM_APPROVAL_POLICY_HISTORY' AND index_name='IX_ADM_APPROVAL_POLICY_HISTORY_POLICY';
  SELECT COUNT(*) INTO column_count FROM user_tab_columns WHERE table_name='ADM_APPROVAL_POLICY_HISTORY' AND column_name IN ('POLICY_HISTORY_ID','POLICY_CODE','POLICY_VERSION','CHANGE_TYPE','CHANGE_REASON','BEFORE_HASH','AFTER_HASH','OPERATOR_ID','CREATED_AT');
  IF table_count <> 1 OR index_count < 1 OR column_count <> 9 THEN
    RAISE_APPLICATION_ERROR(-20041, 'CPF R6 approval policy history schema verification failed');
  END IF;
END;
/
SELECT 'PASS adm_approval_policy_history' AS result FROM dual;
