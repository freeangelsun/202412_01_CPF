SELECT schedule_id, scheduled_fire_at, fencing_token, job_id, definition_version,
       definition_checksum, business_date, fire_zone, idempotency_key
  FROM bat_schedule_trigger
 WHERE trigger_status IN ('CREATED','UNKNOWN','FAILED')
   AND (dispatch_lease_until IS NULL OR dispatch_lease_until < CURRENT_TIMESTAMP(6))
 ORDER BY scheduled_fire_at, schedule_id
 LIMIT ?
