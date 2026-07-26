UPDATE bat_runtime_instance
SET actual_state = ?,
    last_heartbeat_at = ?,
    fencing_token = ?,
    row_version = row_version + 1,
    updated_at = CURRENT_TIMESTAMP(6)
WHERE instance_id = ?
