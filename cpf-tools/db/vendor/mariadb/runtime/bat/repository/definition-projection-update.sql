UPDATE bat_job_runtime_projection
SET definition_checksum = ?,
    projection_status = 'ACTIVE',
    executor_type = ?,
    executor_reference = ?,
    trigger_type = ?,
    trigger_expression = ?,
    timezone_id = ?,
    projection_json = ?,
    projection_hash = ?,
    effective_from = ?,
    effective_until = ?,
    published_by = ?,
    published_at = CURRENT_TIMESTAMP,
    retired_at = NULL,
    row_version = row_version + 1
WHERE job_id = ?
  AND definition_version = ?
