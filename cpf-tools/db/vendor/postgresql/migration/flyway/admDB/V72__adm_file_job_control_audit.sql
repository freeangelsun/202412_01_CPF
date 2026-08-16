-- V72 ADM File Job approval / 4-eyes control audit
ALTER TABLE adm_file_job
    ADD COLUMN IF NOT EXISTS approval_id VARCHAR(120),
    ADD COLUMN IF NOT EXISTS applied_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS resolved_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS control_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS control_reason VARCHAR(500),
    ADD COLUMN IF NOT EXISTS control_updated_at TIMESTAMP(6);
CREATE INDEX IF NOT EXISTS ix_adm_file_job_approval ON adm_file_job(approval_id, job_state);
