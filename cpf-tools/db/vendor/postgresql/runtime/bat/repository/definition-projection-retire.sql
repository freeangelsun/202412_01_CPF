UPDATE bat_job_runtime_projection
SET projection_status = 'RETIRED',
    retired_at = CURRENT_TIMESTAMP,
    row_version = row_version + 1
WHERE job_id = ?
  AND definition_version = ?
  AND definition_checksum = ?
  AND projection_status = 'ACTIVE'
