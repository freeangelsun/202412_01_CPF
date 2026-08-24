INSERT INTO BAT_JOB(
    job_id, job_name, job_type, description, created_by, updated_by
)
VALUES (?, ?, ?, ?, ?, ?)
ON CONFLICT (job_id) DO UPDATE SET
    job_name = EXCLUDED.job_name,
    job_type = EXCLUDED.job_type,
    description = EXCLUDED.description,
    updated_by = EXCLUDED.updated_by
