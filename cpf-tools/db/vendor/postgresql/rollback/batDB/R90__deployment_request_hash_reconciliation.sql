DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM bat_deployment_execution GROUP BY idempotency_key HAVING COUNT(*) > 1) THEN
        RAISE EXCEPTION 'R90 blocked: scoped idempotency keys are not globally unique';
    END IF;
END $$;
DROP INDEX IF EXISTS uk_bat_deploy_exec_scope_idem;
CREATE UNIQUE INDEX uk_bat_deployment_execution_idempotency ON bat_deployment_execution(idempotency_key);
ALTER TABLE bat_deployment_execution ALTER COLUMN idempotency_scope SET DEFAULT 'LEGACY';
-- Audit/reconciliation columns are intentionally retained to prevent destructive evidence loss.
