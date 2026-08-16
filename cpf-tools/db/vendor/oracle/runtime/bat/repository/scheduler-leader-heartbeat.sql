UPDATE bat_scheduler_lease
   SET lease_until = ?,
       last_heartbeat_at = CURRENT_TIMESTAMP(6),
       updated_at = CURRENT_TIMESTAMP(6)
 WHERE scheduler_key = ?
   AND owner_instance_id = ?
   AND fencing_token = ?
   AND lease_until >= CURRENT_TIMESTAMP(6)
