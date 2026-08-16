UPDATE bat_job_runtime_projection_outbox
SET delivery_status = 'DELIVERED',
    delivered_at = CURRENT_TIMESTAMP,
    lease_owner = NULL,
    lease_until = NULL,
    last_error_code = NULL
WHERE outbox_id = ?
  AND delivery_status = 'CLAIMED'
  AND lease_owner = ?
  AND fencing_token = ?
