DROP INDEX ix_bat_job_definition_audit_approval;
ALTER TABLE bat_job_definition_audit DROP (
    after_json, before_json, trace_id, transaction_id, approval_request_id, requested_by
);
