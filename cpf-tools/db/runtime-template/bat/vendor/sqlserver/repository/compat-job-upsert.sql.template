MERGE INTO bat_job AS target
USING (VALUES (?, ?, ?, ?, ?, ?))
    AS source(job_id, job_name, job_type, description, created_by, updated_by)
ON target.job_id = source.job_id
WHEN MATCHED THEN UPDATE SET
    job_name = source.job_name,
    job_type = source.job_type,
    description = source.description,
    updated_by = source.updated_by
WHEN NOT MATCHED THEN INSERT (
    job_id, job_name, job_type, description, created_by, updated_by
) VALUES (
    source.job_id, source.job_name, source.job_type, source.description, source.created_by, source.updated_by
);
