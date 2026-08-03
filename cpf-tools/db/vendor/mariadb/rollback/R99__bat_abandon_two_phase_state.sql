-- CPF BAT abandon two-phase state lifecycle rollback.
-- Fail closed: rollback is forbidden while an execution is still in the transient ABANDONING state.
DELIMITER //
CREATE PROCEDURE cpf_assert_no_abandoning_r99()
BEGIN
  IF EXISTS (SELECT 1 FROM cpf_batch_execution_control WHERE control_status = 'ABANDONING') THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'CPF-BAT-ROLLBACK-ABANDONING: reconcile ABANDONING executions before R99 rollback';
  END IF;
END//
CALL cpf_assert_no_abandoning_r99()//
DROP PROCEDURE cpf_assert_no_abandoning_r99//
DELIMITER ;
ALTER TABLE cpf_batch_execution_control DROP CONSTRAINT ck_cpf_bat_control_status;
ALTER TABLE cpf_batch_execution_control
  ADD CONSTRAINT ck_cpf_bat_control_status
  CHECK (control_status IN ('RESERVED', 'STARTING', 'STARTED', 'STOPPING', 'STOPPED', 'COMPLETED', 'FAILED', 'UNKNOWN_RESULT', 'ABANDONED', 'REJECTED'));
