DECLARE
    duplicate_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO duplicate_count FROM (
        SELECT idempotency_key FROM bat_deployment_execution GROUP BY idempotency_key HAVING COUNT(*) > 1
    );
    IF duplicate_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20090, 'R90 blocked: scoped idempotency keys are not globally unique');
    END IF;
END;
/
DROP INDEX uk_bat_deploy_exec_scope_idem;
CREATE UNIQUE INDEX uk_bat_deployment_execution_idempotency ON bat_deployment_execution(idempotency_key);
ALTER TABLE bat_deployment_execution MODIFY (idempotency_scope DEFAULT 'LEGACY');
-- Audit/reconciliation columns are intentionally retained to prevent destructive evidence loss.
