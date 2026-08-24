MERGE INTO BAT_JOB target
USING (
    SELECT ? job_id, ? job_name, ? job_type, ? description, ? created_by, ? updated_by
    FROM dual
) source
ON (target.job_id = source.job_id)
WHEN MATCHED THEN UPDATE SET
    target.job_name = source.job_name,
    target.job_type = source.job_type,
    target.description = source.description,
    target.updated_by = source.updated_by
WHEN NOT MATCHED THEN INSERT (
    job_id, job_name, job_type, description, created_by, updated_by
) VALUES (
    source.job_id, source.job_name, source.job_type, source.description, source.created_by, source.updated_by
)
