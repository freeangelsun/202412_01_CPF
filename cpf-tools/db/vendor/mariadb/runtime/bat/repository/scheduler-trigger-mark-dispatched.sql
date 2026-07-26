UPDATE bat_schedule_trigger
   SET execution_id = ?,
       trigger_status = 'DISPATCHED'
 WHERE schedule_id = ?
   AND scheduled_fire_at = ?
   AND fencing_token = ?
