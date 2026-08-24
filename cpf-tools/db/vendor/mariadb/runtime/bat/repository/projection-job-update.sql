UPDATE BAT_JOB
SET job_name = ?,
    job_type = ?,
    published_definition_version = ?,
    published_definition_checksum = ?,
    executor_reference = ?,
    definition_published_at = CURRENT_TIMESTAMP,
    description = ?,
    restartable_yn = ?,
    use_yn = 'Y',
    updated_by = ?,
    updated_at = CURRENT_TIMESTAMP
WHERE job_id = ?
