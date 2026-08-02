UPDATE bat_schedule_trigger
   SET execution_id = ?, trigger_status = 'DISPATCHED', dispatched_at = CURRENT_TIMESTAMP(6),
       dispatch_lease_until = NULL, last_error_code = NULL, updated_at = CURRENT_TIMESTAMP(6)
 WHERE schedule_id = ? AND scheduled_fire_at = ?
   AND trigger_status = 'DISPATCHING' AND dispatch_owner = ? AND dispatch_token = ?
