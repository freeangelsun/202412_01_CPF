DROP INDEX ix_adm_file_job_approval;
ALTER TABLE adm_file_job DROP COLUMN control_updated_at;
ALTER TABLE adm_file_job DROP COLUMN control_reason;
ALTER TABLE adm_file_job DROP COLUMN control_by;
ALTER TABLE adm_file_job DROP COLUMN resolved_by;
ALTER TABLE adm_file_job DROP COLUMN applied_by;
ALTER TABLE adm_file_job DROP COLUMN approval_id;
