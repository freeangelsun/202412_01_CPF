UPDATE bat_schedule_trigger
   SET execution_id = ?, trigger_status = 'DISPATCHED', dispatched_at = SYSTIMESTAMP,
       dispatch_lease_until = NULL, last_error_code = NULL, updated_at = SYSTIMESTAMP
 WHERE schedule_id = ? AND scheduled_fire_at = ?
   AND trigger_status = 'DISPATCHING' AND dispatch_owner = ? AND dispatch_token = ?
