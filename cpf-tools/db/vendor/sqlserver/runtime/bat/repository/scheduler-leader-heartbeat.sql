UPDATE bat_scheduler_lease
   SET lease_until = ?,
       last_heartbeat_at = SYSUTCDATETIME(),
       updated_at = SYSUTCDATETIME()
 WHERE scheduler_key = ?
   AND owner_instance_id = ?
   AND fencing_token = ?
   AND lease_until >= SYSUTCDATETIME()
