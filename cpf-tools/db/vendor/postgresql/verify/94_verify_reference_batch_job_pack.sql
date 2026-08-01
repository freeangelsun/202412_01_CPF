DO $$
DECLARE v_count integer;
BEGIN
  SELECT count(*) INTO v_count FROM information_schema.tables
   WHERE table_schema=current_schema() AND upper(table_name) IN ('CPF_REF_BAT_JOB_EXECUTION','CPF_REF_BAT_CHECKPOINT','CPF_REF_BAT_TARGET_RESULT');
  IF v_count <> 3 THEN RAISE EXCEPTION 'CPF REF Batch table count mismatch: %', v_count; END IF;
  SELECT count(*) INTO v_count FROM pg_indexes
   WHERE schemaname=current_schema() AND upper(indexname) IN ('IX_REF_BAT_JOB_STATE','IX_REF_BAT_JOB_BIZ','IX_REF_BAT_CP_STATE','IX_REF_BAT_TARGET_STATE');
  IF v_count <> 4 THEN RAISE EXCEPTION 'CPF REF Batch index count mismatch: %', v_count; END IF;
END $$;
