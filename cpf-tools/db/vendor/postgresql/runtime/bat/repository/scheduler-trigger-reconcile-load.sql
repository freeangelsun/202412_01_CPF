SELECT schedule_id, scheduled_fire_at, job_id, execution_id, trigger_status, idempotency_key, attempt_count, updated_at
  FROM bat_schedule_trigger
 WHERE schedule_id = ?
   AND scheduled_fire_at = ?
   AND trigger_status = 'UNKNOWN'
