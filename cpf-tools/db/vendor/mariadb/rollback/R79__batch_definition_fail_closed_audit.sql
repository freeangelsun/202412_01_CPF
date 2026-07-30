DROP INDEX ix_bat_job_definition_audit_approval ON bat_job_definition_audit;
ALTER TABLE bat_job_definition_audit
    DROP COLUMN after_json, DROP COLUMN before_json, DROP COLUMN trace_id,
    DROP COLUMN transaction_id, DROP COLUMN approval_request_id, DROP COLUMN requested_by;
