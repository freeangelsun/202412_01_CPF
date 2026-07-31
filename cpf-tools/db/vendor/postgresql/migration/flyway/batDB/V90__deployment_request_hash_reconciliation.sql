ALTER TABLE bat_deployment_execution
    ADD COLUMN idempotency_scope VARCHAR(120),
    ADD COLUMN request_hash CHAR(64),
    ADD COLUMN expected_version BIGINT,
    ADD COLUMN approval_request_id VARCHAR(120),
    ADD COLUMN reconcile_requested_by VARCHAR(120),
    ADD COLUMN reconcile_approved_by VARCHAR(120),
    ADD COLUMN reconcile_approval_request_id VARCHAR(120),
    ADD COLUMN reconcile_reason VARCHAR(1000),
    ADD COLUMN reconciled_at TIMESTAMP WITH TIME ZONE;
UPDATE bat_deployment_execution
   SET idempotency_scope = cell_id,
       request_hash = repeat('0', 64)
 WHERE idempotency_scope IS NULL OR request_hash IS NULL;
ALTER TABLE bat_deployment_execution ALTER COLUMN idempotency_scope SET NOT NULL;
ALTER TABLE bat_deployment_execution ALTER COLUMN request_hash SET NOT NULL;
DROP INDEX IF EXISTS uk_bat_deployment_execution_idempotency;
CREATE UNIQUE INDEX uk_bat_deploy_exec_scope_idem ON bat_deployment_execution(idempotency_scope, idempotency_key);
CREATE INDEX ix_bat_deploy_exec_request_hash ON bat_deployment_execution(request_hash);
CREATE INDEX ix_bat_deploy_exec_reconciled ON bat_deployment_execution(execution_state, reconciled_at);
ALTER TABLE bat_deployment_execution ADD CONSTRAINT ck_bat_deploy_exec_request_hash
    CHECK (request_hash ~ '^[0-9a-f]{64}$');
