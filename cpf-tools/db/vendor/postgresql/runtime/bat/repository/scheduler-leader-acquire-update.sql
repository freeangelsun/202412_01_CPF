UPDATE BAT_SCHEDULER_LEASE
   SET owner_instance_id = ?,
       fencing_token = fencing_token + 1,
       lease_until = (CURRENT_TIMESTAMP(6) AT TIME ZONE 'UTC') + (? * INTERVAL '1 microsecond'),
       last_heartbeat_at = (CURRENT_TIMESTAMP(6) AT TIME ZONE 'UTC'),
       updated_at = (CURRENT_TIMESTAMP(6) AT TIME ZONE 'UTC')
 WHERE scheduler_key = ?
   AND (owner_instance_id = ? OR lease_until < (CURRENT_TIMESTAMP(6) AT TIME ZONE 'UTC'))
