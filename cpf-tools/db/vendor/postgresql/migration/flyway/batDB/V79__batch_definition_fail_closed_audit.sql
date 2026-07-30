ALTER TABLE bat_job_definition_audit ADD COLUMN requested_by VARCHAR(100) NULL;
ALTER TABLE bat_job_definition_audit ADD COLUMN approval_request_id VARCHAR(120) NULL;
ALTER TABLE bat_job_definition_audit ADD COLUMN transaction_id CHAR(34) NULL;
ALTER TABLE bat_job_definition_audit ADD COLUMN trace_id VARCHAR(64) NULL;
ALTER TABLE bat_job_definition_audit ADD COLUMN before_json TEXT NULL;
ALTER TABLE bat_job_definition_audit ADD COLUMN after_json TEXT NULL;
CREATE INDEX ix_bat_job_definition_audit_approval
    ON bat_job_definition_audit (approval_request_id, created_at);
