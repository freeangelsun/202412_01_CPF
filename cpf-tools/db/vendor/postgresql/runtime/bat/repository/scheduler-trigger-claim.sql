UPDATE bat_schedule_trigger
   SET trigger_status = 'DISPATCHING',
       dispatch_owner = ?,
       dispatch_token = ?,
       dispatch_lease_until = CURRENT_TIMESTAMP(6) + INTERVAL '60 seconds',
       attempt_count = attempt_count + 1,
       updated_at = CURRENT_TIMESTAMP(6)
 WHERE schedule_id = ?
   AND scheduled_fire_at = ?
   AND trigger_status IN ('CREATED','UNKNOWN','FAILED')
   AND (dispatch_lease_until IS NULL OR dispatch_lease_until < CURRENT_TIMESTAMP(6))
   AND EXISTS (
       SELECT 1 FROM bat_scheduler_lease
        WHERE scheduler_key = ? AND owner_instance_id = ? AND fencing_token = ? AND lease_until >= CURRENT_TIMESTAMP(6)
   )
