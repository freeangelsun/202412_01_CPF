INSERT INTO bat_schedule_trigger (
    schedule_id, scheduled_fire_at, fencing_token, trigger_status,
    job_id, definition_version, definition_checksum, business_date, fire_zone, idempotency_key, created_at, updated_at
)
SELECT ?, ?, ?, 'CREATED', ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
  FROM bat_scheduler_lease
 WHERE scheduler_key = ?
   AND owner_instance_id = ?
   AND fencing_token = ?
   AND lease_until >= CURRENT_TIMESTAMP(6)
