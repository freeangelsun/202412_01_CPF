UPDATE BAT_SCHEDULER_LEASE
   SET owner_instance_id = ?,
       fencing_token = fencing_token + 1,
       lease_until = TIMESTAMPADD(MICROSECOND, ?, UTC_TIMESTAMP(6)),
       last_heartbeat_at = UTC_TIMESTAMP(6),
       updated_at = UTC_TIMESTAMP(6)
 WHERE scheduler_key = ?
   AND (owner_instance_id = ? OR lease_until < UTC_TIMESTAMP(6))
