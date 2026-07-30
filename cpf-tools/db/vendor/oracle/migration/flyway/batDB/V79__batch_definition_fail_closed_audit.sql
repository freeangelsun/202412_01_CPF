ALTER TABLE bat_job_definition_audit ADD (
    requested_by VARCHAR2(100 CHAR) NULL,
    approval_request_id VARCHAR2(120 CHAR) NULL,
    transaction_id CHAR(34 CHAR) NULL,
    trace_id VARCHAR2(64 CHAR) NULL,
    before_json CLOB NULL,
    after_json CLOB NULL
);
CREATE INDEX ix_bat_job_definition_audit_approval
    ON bat_job_definition_audit (approval_request_id, created_at);
