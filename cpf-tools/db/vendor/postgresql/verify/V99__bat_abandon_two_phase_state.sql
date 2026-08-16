-- Verify CPF BAT abandon two-phase state after V99.
DO $$
DECLARE
  v_definition TEXT;
BEGIN
  SELECT pg_get_constraintdef(c.oid) INTO v_definition
    FROM pg_constraint c
    JOIN pg_class t ON t.oid = c.conrelid
   WHERE t.relname = 'cpf_batch_execution_control'
     AND c.conname = 'ck_cpf_bat_control_status';
  IF v_definition IS NULL OR POSITION('ABANDONING' IN UPPER(v_definition)) = 0 THEN
    RAISE EXCEPTION 'CPF-BAT-V99-VERIFY-FAILED: ABANDONING state constraint is missing';
  END IF;
END
$$;
