UPDATE BAT_JOB
SET use_yn = 'N',
    updated_by = ?,
    updated_at = CURRENT_TIMESTAMP
WHERE job_id = ?
  AND published_definition_version = ?
