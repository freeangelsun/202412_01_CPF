UPDATE bat_schedule
   SET last_fire_at = ?,
       next_fire_at = ?,
       updated_at = SYSUTCDATETIME()
 WHERE schedule_id = ?
   AND ((next_fire_at IS NULL AND ? IS NULL) OR next_fire_at = ?)
   AND EXISTS (
       SELECT 1
         FROM bat_scheduler_lease l
        WHERE l.scheduler_key = ?
          AND l.owner_instance_id = ?
          AND l.fencing_token = ?
          AND l.lease_until >= SYSUTCDATETIME()
   )
