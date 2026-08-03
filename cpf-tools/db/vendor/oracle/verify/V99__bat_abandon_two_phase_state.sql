-- Verify CPF BAT abandon two-phase state after V99.
DECLARE
  v_condition USER_CONSTRAINTS.SEARCH_CONDITION_VC%TYPE;
BEGIN
  SELECT search_condition_vc INTO v_condition
    FROM user_constraints
   WHERE table_name = 'CPF_BATCH_EXECUTION_CONTROL'
     AND constraint_name = 'CK_CPF_BAT_CONTROL_STATUS';
  IF v_condition IS NULL OR INSTR(UPPER(v_condition), 'ABANDONING') = 0 THEN
    RAISE_APPLICATION_ERROR(-20997, 'CPF-BAT-V99-VERIFY-FAILED: ABANDONING state constraint is missing');
  END IF;
EXCEPTION
  WHEN NO_DATA_FOUND THEN
    RAISE_APPLICATION_ERROR(-20997, 'CPF-BAT-V99-VERIFY-FAILED: control status constraint is missing');
END;
/
