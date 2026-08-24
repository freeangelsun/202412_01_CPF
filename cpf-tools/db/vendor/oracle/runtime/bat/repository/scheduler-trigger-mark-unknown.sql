UPDATE BAT_SCHEDULE_TRIGGER
   SET trigger_status = 'UNKNOWN', last_error_code = ?, last_error_at = CURRENT_TIMESTAMP(6),
       dispatch_lease_until = NULL, updated_at = CURRENT_TIMESTAMP(6)
 WHERE schedule_id = ? AND scheduled_fire_at = ?
   AND trigger_status = 'DISPATCHING' AND dispatch_owner = ? AND dispatch_token = ?
