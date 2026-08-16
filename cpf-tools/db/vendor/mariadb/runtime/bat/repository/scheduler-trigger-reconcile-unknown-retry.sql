UPDATE bat_schedule_trigger
   SET trigger_status = 'FAILED',
       dispatch_owner = NULL,
       dispatch_token = NULL,
       dispatch_lease_until = NULL,
       last_error_code = ?,
       last_error_at = CURRENT_TIMESTAMP(6),
       updated_at = CURRENT_TIMESTAMP(6)
 WHERE schedule_id = ?
   AND scheduled_fire_at = ?
   AND trigger_status = 'UNKNOWN'
   AND idempotency_key = ?
   AND attempt_count = ?
