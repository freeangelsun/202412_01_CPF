UPDATE bat_schedule_trigger
   SET trigger_status = 'UNKNOWN', last_error_code = ?, last_error_at = CURRENT_TIMESTAMP,
       dispatch_lease_until = NULL, updated_at = CURRENT_TIMESTAMP
 WHERE schedule_id = ? AND scheduled_fire_at = ?
   AND trigger_status = 'DISPATCHING' AND dispatch_owner = ? AND dispatch_token = ?
