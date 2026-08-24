UPDATE BAT_JOB_RUNTIME_PROJECTION
SET projection_status = 'RETIRED',
    retired_at = CURRENT_TIMESTAMP,
    row_version = row_version + 1
WHERE job_id = ?
  AND definition_version <> ?
  AND projection_status = 'ACTIVE'
