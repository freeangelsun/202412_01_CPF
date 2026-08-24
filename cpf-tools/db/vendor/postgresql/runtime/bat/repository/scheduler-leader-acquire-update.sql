UPDATE BAT_SCHEDULER_LEASE
   SET owner_instance_id = ?,
       fencing_token = fencing_token + 1,
       lease_until = ?,
       last_heartbeat_at = ?,
       updated_at = CURRENT_TIMESTAMP(6)
 WHERE scheduler_key = ?
   AND (owner_instance_id = ? OR lease_until < ?)
