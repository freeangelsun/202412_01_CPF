ALTER TABLE bat_job_definition_audit
    ADD COLUMN requested_by VARCHAR(100) NULL,
    ADD COLUMN approval_request_id VARCHAR(120) NULL,
    ADD COLUMN transaction_id CHAR(34) NULL,
    ADD COLUMN trace_id VARCHAR(64) NULL,
    ADD COLUMN before_json MEDIUMTEXT NULL,
    ADD COLUMN after_json MEDIUMTEXT NULL;
CREATE INDEX ix_bat_job_definition_audit_approval
    ON bat_job_definition_audit (approval_request_id, created_at);
