UPDATE BAT_SCHEDULER_LEASE
   SET owner_instance_id = ?,
       fencing_token = fencing_token + 1,
       lease_until = SYS_EXTRACT_UTC(SYSTIMESTAMP) + NUMTODSINTERVAL(? / 1000000, 'SECOND'),
       last_heartbeat_at = SYS_EXTRACT_UTC(SYSTIMESTAMP),
       updated_at = SYS_EXTRACT_UTC(SYSTIMESTAMP)
 WHERE scheduler_key = ?
   AND (owner_instance_id = ? OR lease_until < SYS_EXTRACT_UTC(SYSTIMESTAMP))
