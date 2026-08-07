DO $$
DECLARE table_count INTEGER; index_count INTEGER; column_count INTEGER;
BEGIN
  SELECT COUNT(*) INTO table_count FROM information_schema.tables WHERE table_schema=current_schema() AND table_name='adm_approval_policy_history';
  SELECT COUNT(*) INTO index_count FROM pg_indexes WHERE schemaname=current_schema() AND tablename='adm_approval_policy_history' AND indexname='ix_adm_approval_policy_history_policy';
  SELECT COUNT(*) INTO column_count FROM information_schema.columns WHERE table_schema=current_schema() AND table_name='adm_approval_policy_history' AND column_name IN ('policy_history_id','policy_code','policy_version','change_type','change_reason','before_hash','after_hash','operator_id','created_at');
  IF table_count <> 1 OR index_count < 1 OR column_count <> 9 THEN
    RAISE EXCEPTION 'CPF R6 approval policy history schema verification failed table=% index=% columns=%', table_count, index_count, column_count;
  END IF;
END $$;
SELECT 'PASS adm_approval_policy_history' AS result;
