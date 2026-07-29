DROP INDEX IF EXISTS ix_adm_file_job_approval;
ALTER TABLE adm_file_job
    DROP COLUMN IF EXISTS control_updated_at,
    DROP COLUMN IF EXISTS control_reason,
    DROP COLUMN IF EXISTS control_by,
    DROP COLUMN IF EXISTS resolved_by,
    DROP COLUMN IF EXISTS applied_by,
    DROP COLUMN IF EXISTS approval_id;
