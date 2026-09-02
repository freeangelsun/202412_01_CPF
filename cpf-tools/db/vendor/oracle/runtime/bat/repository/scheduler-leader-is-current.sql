SELECT COUNT(*)
  FROM BAT_SCHEDULER_LEASE
 WHERE scheduler_key = ?
   AND owner_instance_id = ?
   AND fencing_token = ?
   AND lease_until >= SYS_EXTRACT_UTC(SYSTIMESTAMP)
