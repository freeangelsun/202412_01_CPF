UPDATE BAT_SCHEDULER_LEASE
   SET lease_until = (CURRENT_TIMESTAMP(6) AT TIME ZONE 'UTC') + (? * INTERVAL '1 microsecond'),
       last_heartbeat_at = (CURRENT_TIMESTAMP(6) AT TIME ZONE 'UTC'),
       updated_at = (CURRENT_TIMESTAMP(6) AT TIME ZONE 'UTC')
 WHERE scheduler_key = ?
   AND owner_instance_id = ?
   AND fencing_token = ?
   AND lease_until >= (CURRENT_TIMESTAMP(6) AT TIME ZONE 'UTC')
