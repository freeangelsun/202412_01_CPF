UPDATE bat_runtime_instance
SET desired_state = ?,
    row_version = row_version + 1,
    updated_at = CURRENT_TIMESTAMP(6)
WHERE instance_id = ?
  AND row_version = ?
