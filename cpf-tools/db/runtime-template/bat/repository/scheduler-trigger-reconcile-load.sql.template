SELECT schedule_id, scheduled_fire_at, trigger_status, idempotency_key,
       attempt_count, last_error_code, dispatch_owner, dispatch_token
  FROM BAT_SCHEDULE_TRIGGER
 WHERE schedule_id = ?
   AND scheduled_fire_at = ?
