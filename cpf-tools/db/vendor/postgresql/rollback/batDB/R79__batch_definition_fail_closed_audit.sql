DROP INDEX IF EXISTS ix_bat_job_definition_audit_approval;
ALTER TABLE bat_job_definition_audit DROP COLUMN after_json;
ALTER TABLE bat_job_definition_audit DROP COLUMN before_json;
ALTER TABLE bat_job_definition_audit DROP COLUMN trace_id;
ALTER TABLE bat_job_definition_audit DROP COLUMN transaction_id;
ALTER TABLE bat_job_definition_audit DROP COLUMN approval_request_id;
ALTER TABLE bat_job_definition_audit DROP COLUMN requested_by;
