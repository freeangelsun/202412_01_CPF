UPDATE bat_job_definition_version
SET definition_state = ?,
    definition_json = ?,
    row_version = row_version + 1,
    updated_by = ?,
    updated_at = CURRENT_TIMESTAMP
WHERE job_id = ?
  AND definition_version = ?
  AND row_version = ?
