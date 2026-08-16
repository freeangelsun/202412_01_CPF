UPDATE bat_job_runtime_projection_outbox
SET delivery_status = 'RETRY',
    lease_owner = NULL,
    lease_until = NULL,
    next_attempt_at = ?,
    last_error_code = ?
WHERE outbox_id = ?
  AND lease_owner = ?
  AND fencing_token = ?
