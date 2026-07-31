ALTER TABLE bat_deployment_execution
    ADD COLUMN idempotency_scope VARCHAR(120) NULL AFTER cell_id,
    ADD COLUMN request_hash CHAR(64) NULL AFTER idempotency_key,
    ADD COLUMN expected_version BIGINT NULL AFTER strategy_code,
    ADD COLUMN approval_request_id VARCHAR(120) NULL AFTER expected_version,
    ADD COLUMN reconcile_requested_by VARCHAR(120) NULL,
    ADD COLUMN reconcile_approved_by VARCHAR(120) NULL,
    ADD COLUMN reconcile_approval_request_id VARCHAR(120) NULL,
    ADD COLUMN reconcile_reason VARCHAR(1000) NULL,
    ADD COLUMN reconciled_at DATETIME(6) NULL;
UPDATE bat_deployment_execution
   SET idempotency_scope = cell_id,
       request_hash = REPEAT('0', 64)
 WHERE idempotency_scope IS NULL OR request_hash IS NULL;
ALTER TABLE bat_deployment_execution
    MODIFY COLUMN idempotency_scope VARCHAR(120) NOT NULL,
    MODIFY COLUMN request_hash CHAR(64) NOT NULL;
DROP INDEX uk_bat_deployment_execution_idempotency ON bat_deployment_execution;
CREATE UNIQUE INDEX uk_bat_deploy_exec_scope_idem ON bat_deployment_execution(idempotency_scope, idempotency_key);
CREATE INDEX ix_bat_deploy_exec_request_hash ON bat_deployment_execution(request_hash);
CREATE INDEX ix_bat_deploy_exec_reconciled ON bat_deployment_execution(execution_state, reconciled_at);
ALTER TABLE bat_deployment_execution ADD CONSTRAINT ck_bat_deploy_exec_request_hash
    CHECK (request_hash REGEXP '^[0-9a-f]{64}$');
