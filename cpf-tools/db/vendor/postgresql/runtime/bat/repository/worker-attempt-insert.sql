INSERT INTO bat_execution_attempt (
    execution_id, attempt_no, definition_version, definition_checksum,
    worker_id, fencing_token, attempt_status, started_at
) VALUES (?, ?, ?, ?, ?, ?, 'RUNNING', CURRENT_TIMESTAMP)
