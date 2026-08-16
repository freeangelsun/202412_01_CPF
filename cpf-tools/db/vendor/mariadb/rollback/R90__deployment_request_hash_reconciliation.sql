DELIMITER //
CREATE PROCEDURE cpf_r90_assert_global_idempotency()
BEGIN
    IF EXISTS (SELECT idempotency_key FROM bat_deployment_execution GROUP BY idempotency_key HAVING COUNT(*) > 1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'R90 blocked: scoped idempotency keys are not globally unique';
    END IF;
END//
DELIMITER ;
CALL cpf_r90_assert_global_idempotency();
DROP PROCEDURE cpf_r90_assert_global_idempotency;
DROP INDEX uk_bat_deploy_exec_scope_idem ON bat_deployment_execution;
CREATE UNIQUE INDEX uk_bat_deployment_execution_idempotency ON bat_deployment_execution(idempotency_key);
ALTER TABLE bat_deployment_execution ALTER COLUMN idempotency_scope SET DEFAULT 'LEGACY';
-- Audit/reconciliation columns are intentionally retained to prevent destructive evidence loss.
