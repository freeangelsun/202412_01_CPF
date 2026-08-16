UPDATE bat_job_runtime_projection_outbox
SET delivery_status = 'CLAIMED',
    lease_owner = ?,
    lease_until = ?,
    fencing_token = ?,
    attempt_count = attempt_count + 1,
    last_error_code = NULL
WHERE outbox_id = ?
  AND delivery_status IN ('PENDING','RETRY')
  AND (lease_until IS NULL OR lease_until < CURRENT_TIMESTAMP)
