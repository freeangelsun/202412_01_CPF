INSERT INTO BAT_JOB(
    job_id, job_name, job_type, description, created_by, updated_by
)
VALUES (?, ?, ?, ?, ?, ?)
ON DUPLICATE KEY UPDATE
    job_name = VALUES(job_name),
    job_type = VALUES(job_type),
    description = VALUES(description),
    updated_by = VALUES(updated_by)
