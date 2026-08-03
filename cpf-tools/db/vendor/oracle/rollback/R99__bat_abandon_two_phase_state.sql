-- CPF BAT abandon two-phase state lifecycle rollback.
-- Fail closed: rollback is forbidden while an execution is still in the transient ABANDONING state.
DECLARE
  v_abandoning_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_abandoning_count
    FROM cpf_batch_execution_control
   WHERE control_status = 'ABANDONING';
  IF v_abandoning_count > 0 THEN
    RAISE_APPLICATION_ERROR(-20998, 'CPF-BAT-ROLLBACK-ABANDONING: reconcile ABANDONING executions before R99 rollback');
  END IF;
END;
/
ALTER TABLE cpf_batch_execution_control DROP CONSTRAINT ck_cpf_bat_control_status;
ALTER TABLE cpf_batch_execution_control
  ADD CONSTRAINT ck_cpf_bat_control_status
  CHECK (control_status IN ('RESERVED', 'STARTING', 'STARTED', 'STOPPING', 'STOPPED', 'COMPLETED', 'FAILED', 'UNKNOWN_RESULT', 'ABANDONED', 'REJECTED'));
