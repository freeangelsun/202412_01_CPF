-- V72 ADM File Job approval / 4-eyes control audit
ALTER TABLE adm_file_job ADD (
    approval_id VARCHAR2(120 CHAR),
    applied_by VARCHAR2(100 CHAR),
    resolved_by VARCHAR2(100 CHAR),
    control_by VARCHAR2(100 CHAR),
    control_reason VARCHAR2(500 CHAR),
    control_updated_at TIMESTAMP(6)
);
CREATE INDEX ix_adm_file_job_approval ON adm_file_job(approval_id, job_state);
