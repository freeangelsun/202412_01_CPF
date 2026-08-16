ALTER TABLE bat_deployment_execution ADD (
    idempotency_scope VARCHAR2(120 CHAR),
    request_hash CHAR(64 CHAR),
    expected_version NUMBER(19),
    approval_request_id VARCHAR2(120 CHAR),
    reconcile_requested_by VARCHAR2(120 CHAR),
    reconcile_approved_by VARCHAR2(120 CHAR),
    reconcile_approval_request_id VARCHAR2(120 CHAR),
    reconcile_reason VARCHAR2(1000 CHAR),
    reconciled_at TIMESTAMP WITH TIME ZONE
);
UPDATE bat_deployment_execution
   SET idempotency_scope = cell_id,
       request_hash = RPAD('0', 64, '0')
 WHERE idempotency_scope IS NULL OR request_hash IS NULL;
ALTER TABLE bat_deployment_execution MODIFY (
    idempotency_scope NOT NULL,
    request_hash NOT NULL
);
BEGIN
    EXECUTE IMMEDIATE 'DROP INDEX uk_bat_deployment_execution_idempotency';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -1418 THEN RAISE; END IF;
END;
/
CREATE UNIQUE INDEX uk_bat_deploy_exec_scope_idem ON bat_deployment_execution(idempotency_scope, idempotency_key);
CREATE INDEX ix_bat_deploy_exec_request_hash ON bat_deployment_execution(request_hash);
CREATE INDEX ix_bat_deploy_exec_reconciled ON bat_deployment_execution(execution_state, reconciled_at);
ALTER TABLE bat_deployment_execution ADD CONSTRAINT ck_bat_deploy_exec_request_hash
    CHECK (REGEXP_LIKE(request_hash, '^[0-9a-f]{64}$'));
