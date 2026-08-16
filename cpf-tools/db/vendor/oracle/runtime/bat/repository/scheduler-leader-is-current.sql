SELECT COUNT(*)
  FROM bat_scheduler_lease
 WHERE scheduler_key = ?
   AND owner_instance_id = ?
   AND fencing_token = ?
   AND lease_until >= CURRENT_TIMESTAMP(6)
